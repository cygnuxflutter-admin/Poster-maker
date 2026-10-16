package com.festival.flyer.postermaker.adManager;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class MailER_AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = "AppOpenManager";
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final MyApplication myApplication;
    private static boolean isShowingAd = false;
    private Activity currentActivity;
    private long loadTime = 0;
    public static Integer AppOpenAdShow = 0;
    private static MailER_PreferenceClass preferenceClass;
    private String AD_UNIT_ID1, AD_UNIT_ID2;
    private static long lastAppOpenShowTime = 0;
    public static long lastInterstitialShowTime = 0;
    public static long lastAdClickTime = 0;
    public static long lastIntentActionTime = 0;
    public static long APPOPEN_COOLDOWN_MS = 60000; // Default 60 sec, overridden by Firebase "splashAdTimer"
    public static long INTERSTITIAL_CLASH_MS = 30000; // Default 30 sec, overridden by Firebase "InterAdTimer"

    public static void notifyAdClicked() {
        lastAdClickTime = System.currentTimeMillis();
        Log.d("[ADS_LOG]", "👉 Ad Click Logged! AppOpen ad will be suppressed on return.");
    }

    public static void notifyExternalActionStarted() {
        lastIntentActionTime = System.currentTimeMillis();
        Log.d("[ADS_LOG]", "📷 External Action (Camera/Gallery/Share) Started! AppOpen ad will be suppressed on return.");
    }

    /**
     * Constructor
     */
    public MailER_AppOpenManager(MyApplication myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private static boolean isLoadingAd = false;

    /**
     * Request an ad
     */
    public void fetchAd() {
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(myApplication);
        }
        if (MyApplication.isAdsSplash) {
            boolean isSplashEnabled = preferenceClass.getBoolean("splash_app_open_enabled", true)
                    && (preferenceClass.getInt("SplashAppOpen", 1) == 1)
                    && (AppOpenAdShow == 1);
            if (!isSplashEnabled || isAdAvailable() || isLoadingAd) {
                return;
            }
        } else {
            boolean isForegroundEnabled = preferenceClass.getBoolean("foreground_app_open_enabled", true);
            if (!isForegroundEnabled || isAdAvailable() || isLoadingAd) {
                return;
            }
        }
        isLoadingAd = true;

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(AppOpenAd ad) {
                isLoadingAd = false;
                Log.d("AdTracker", "AppOpen Ad (AdMob) Loaded Successfully using ID: " + AD_UNIT_ID1);
                MailER_AppOpenManager.this.appOpenAd = ad;
                MailER_AppOpenManager.this.loadTime = (new Date()).getTime();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                isLoadingAd = false;
                Log.d("AdTracker", "AppOpen Ad (AdMob) Failed to Load! Error: " + loadAdError.getMessage());
                // fetchAdX();
            }
        };

        String customForegroundId = preferenceClass.getAdsId("ForegroundAppOpenID");
        if (customForegroundId != null && !customForegroundId.trim().isEmpty()) {
            AD_UNIT_ID1 = customForegroundId.trim();
        } else {
            AD_UNIT_ID1 = preferenceClass.getAdsId("AppOpenID");
        }
        AD_UNIT_ID2 = preferenceClass.getAdsId("AdxAppOpenID");

        AdRequest request = getAdRequest();
        Log.d("AdTracker", "Requesting AppOpen Ad (AdMob) with Firebase ID: " + AD_UNIT_ID1);
        AppOpenAd.load(myApplication, AD_UNIT_ID1, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    public void fetchAdX() {
        if (isAdAvailable()) {
            return;
        }
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(myApplication);
        }
        if (MyApplication.isAdsSplash) {
            boolean isSplashEnabled = preferenceClass.getBoolean("splash_app_open_enabled", true)
                    && (preferenceClass.getInt("SplashAppOpen", 1) == 1)
                    && (AppOpenAdShow == 1);
            if (!isSplashEnabled) return;
        } else {
            boolean isForegroundEnabled = preferenceClass.getBoolean("foreground_app_open_enabled", true);
            if (!isForegroundEnabled) return;
        }
        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                Log.d("AdTracker", "AppOpen Ad (AdX) Loaded Successfully using ID: " + AD_UNIT_ID2);
                MailER_AppOpenManager.this.appOpenAd = ad;
                MailER_AppOpenManager.this.loadTime = new Date().getTime();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdTracker", "AppOpen Ad (AdX) Failed to Load! Error: " + loadAdError.getMessage());
            }
        };
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(myApplication);
        }
        AD_UNIT_ID1 = preferenceClass.getAdsId("AppOpenID");
        AD_UNIT_ID2 = preferenceClass.getAdsId("AdxAppOpenID");

        AdRequest request = getAdRequest();
        Log.d("AdTracker", "Requesting AppOpen Ad (AdX) with Firebase ID: " + AD_UNIT_ID2);
        AppOpenAd.load(myApplication, AD_UNIT_ID2, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    public void sendRequest() {
        if (!isShowingAd && isAdAvailable()) {
            if (MyApplication.isShowingAppOpen) {

            }
        } else {
            fetchAd();
        }
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable() {
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(myApplication);
        }
        boolean isForegroundEnabled = preferenceClass.getBoolean("foreground_app_open_enabled", true);
        long cooldownSec = preferenceClass.getInt("foreground_app_open_cooldown", 60);
        boolean suppressOnAdClick = preferenceClass.getBoolean("suppress_app_open_on_ad_click", true);
        boolean suppressOnIntent = preferenceClass.getBoolean("suppress_app_open_on_intent", true);

        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            if (rc != null) {
                String masterJson = MailER_PreferenceClass.getMasterConfigJson(rc);
                if (masterJson != null && !masterJson.isEmpty()) {
                    org.json.JSONObject root = new org.json.JSONObject(masterJson);
                    org.json.JSONObject resumeObj = root.optJSONObject("foreground_app_open");
                    if (resumeObj == null) resumeObj = root.optJSONObject("background_app_open");
                    if (resumeObj == null) resumeObj = root.optJSONObject("app_resume_ad");
                    if (resumeObj == null) resumeObj = root.optJSONObject("app_open_resume");
                    if (resumeObj == null) resumeObj = root.optJSONObject("app_resume");
                    if (resumeObj != null) {
                        isForegroundEnabled = resumeObj.optBoolean("enabled", isForegroundEnabled);
                        cooldownSec = resumeObj.optLong("cooldown_seconds", cooldownSec);
                        if (resumeObj.has("suppress_on_ad_click")) {
                            suppressOnAdClick = resumeObj.optBoolean("suppress_on_ad_click", true);
                        } else if (resumeObj.has("suppress_app_open_on_ad_click")) {
                            suppressOnAdClick = resumeObj.optBoolean("suppress_app_open_on_ad_click", true);
                        }
                        if (resumeObj.has("suppress_on_intent")) {
                            suppressOnIntent = resumeObj.optBoolean("suppress_on_intent", true);
                        } else if (resumeObj.has("suppress_app_open_on_intent")) {
                            suppressOnIntent = resumeObj.optBoolean("suppress_app_open_on_intent", true);
                        }
                    }
                    if (root.has("suppress_app_open_on_ad_click")) {
                        suppressOnAdClick = root.optBoolean("suppress_app_open_on_ad_click", suppressOnAdClick);
                    }
                    if (root.has("suppress_app_open_on_intent")) {
                        suppressOnIntent = root.optBoolean("suppress_app_open_on_intent", suppressOnIntent);
                    }
                } else {
                    if (rc.getValue("suppress_app_open_on_ad_click").getSource() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                        suppressOnAdClick = rc.getBoolean("suppress_app_open_on_ad_click");
                    }
                    if (rc.getValue("suppress_app_open_on_intent").getSource() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                        suppressOnIntent = rc.getBoolean("suppress_app_open_on_intent");
                    }
                }
            }
        } catch (Exception ignored) {}

        if (!isForegroundEnabled || !MyApplication.isShowingAppOpen) {
            Log.d("[ADS_LOG]", "⚠️ Foreground/Background AppOpen Ad Disabled via Remote Config.");
            return;
        }
        if (!isShowingAd && isAdAvailable()) {
            long cooldownMs = cooldownSec > 0 ? (cooldownSec * 1000L) : APPOPEN_COOLDOWN_MS;

            // Cooldown check - don't show if last AppOpen was less than cooldownMs ago
            long timeSinceLastAppOpen = System.currentTimeMillis() - lastAppOpenShowTime;
            if (lastAppOpenShowTime > 0 && timeSinceLastAppOpen < cooldownMs) {
                Log.d("[ADS_LOG]", "⏳ Foreground AppOpen Ad Skipped! Cooldown active (" + (timeSinceLastAppOpen / 1000) + "s / " + (cooldownMs / 1000) + "s)");
                return;
            }
            // Clash check - don't show if Interstitial was shown in last 30 sec
            long timeSinceInterstitial = System.currentTimeMillis() - lastInterstitialShowTime;
            if (lastInterstitialShowTime > 0 && timeSinceInterstitial < INTERSTITIAL_CLASH_MS) {
                Log.d("[ADS_LOG]", "⏳ Foreground AppOpen Ad Skipped! Interstitial shown " + (timeSinceInterstitial / 1000) + "s ago (need " + (INTERSTITIAL_CLASH_MS / 1000) + "s gap)");
                return;
            }

            // 1. Check if returning from an Ad click (Play Store, Browser, Blinkit, etc.)
            if (suppressOnAdClick) {
                long timeSinceAdClick = System.currentTimeMillis() - lastAdClickTime;
                long adClickCooldownMs = 60000; // 60 seconds protection
                if (lastAdClickTime > 0 && timeSinceAdClick < adClickCooldownMs) {
                    lastAdClickTime = 0;
                    Log.d("[ADS_LOG]", "⏳ Foreground AppOpen Ad Skipped! User returned after clicking an Ad " + (timeSinceAdClick / 1000) + "s ago (suppress_app_open_on_ad_click active)");
                    return;
                }
            }

            // 2. Check if returning from Camera / Gallery / Share
            if (suppressOnIntent) {
                long timeSinceIntent = System.currentTimeMillis() - lastIntentActionTime;
                long intentCooldownMs = 60000; // 60 seconds protection
                if (lastIntentActionTime > 0 && timeSinceIntent < intentCooldownMs) {
                    lastIntentActionTime = 0;
                    Log.d("[ADS_LOG]", "⏳ Foreground AppOpen Ad Skipped! User returned from Camera/Gallery/Share " + (timeSinceIntent / 1000) + "s ago (suppress_app_open_on_intent active)");
                    return;
                }
            }

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("[ADS_LOG]", "❌ AppOpen Ad Dismissed by User.");
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    fetchAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d("[ADS_LOG]", "🔴 AppOpen Ad Failed to Show: " + adError.getMessage());
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    fetchAd();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("[ADS_LOG]", "📺 AppOpen Ad Displayed on Screen!");
                    isShowingAd = true;
                    lastAppOpenShowTime = System.currentTimeMillis();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: AppOpen Ad IMPRESSION Logged!");
                }
            };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);
        } else {
            fetchAd();
        }
    }

    public void showAdIfSplashAvailable(@NonNull final Activity activity, @NonNull MyApplication.OnShowAdCompleteListener onShowAdCompleteListener) {
        if (!isShowingAd && isAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("[ADS_LOG]", "❌ Splash AppOpen Ad Dismissed by User.");
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    boolean isForegroundEnabled = preferenceClass != null && preferenceClass.getBoolean("foreground_app_open_enabled", true);
                    if (isForegroundEnabled) {
                        fetchAd();
                    }
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d("[ADS_LOG]", "🔴 Splash AppOpen Ad Failed to Show: " + adError.getMessage());
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    boolean isForegroundEnabled = preferenceClass != null && preferenceClass.getBoolean("foreground_app_open_enabled", true);
                    if (isForegroundEnabled) {
                        fetchAd();
                    }
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("[ADS_LOG]", "📺 Splash AppOpen Ad Displayed on Screen!");
                    isShowingAd = true;
                    lastAppOpenShowTime = System.currentTimeMillis();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: Splash AppOpen Ad IMPRESSION Logged!");
                }
            };
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(activity);
        } else {
            onShowAdCompleteListener.onShowAdComplete();
        }
    }

    public void showAdIfAvailable(@NonNull final Activity activity, @NonNull MyApplication.OnShowAdCompleteListener onShowAdCompleteListener) {
        if (!isShowingAd && isAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("[ADS_LOG]", "❌ AppOpen Ad (Overload) Dismissed by User.");
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d("[ADS_LOG]", "🔴 AppOpen Ad (Overload) Failed to Show: " + adError.getMessage());
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    fetchAd();
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("[ADS_LOG]", "📺 AppOpen Ad (Overload) Displayed on Screen!");
                    isShowingAd = true;
                    lastAppOpenShowTime = System.currentTimeMillis();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: AppOpen Ad (Overload) IMPRESSION Logged!");
                }
            };
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(activity);
        } else {
            onShowAdCompleteListener.onShowAdComplete();
        }
    }

    /**
     * ActivityLifecycleCallback methods
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (!MyApplication.isAdsSplash) {
            showAdIfAvailable();
        }
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

}