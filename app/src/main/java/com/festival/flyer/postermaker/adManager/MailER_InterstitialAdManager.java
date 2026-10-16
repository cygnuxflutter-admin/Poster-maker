package com.festival.flyer.postermaker.adManager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MailER_InterstitialAdManager {
    private final Context context;
    private final MailER_PreferenceClass preferenceClass;
    private String admobInterstitialAdId;
    private android.app.Dialog progressDialog;
    
    private InterstitialAd mInterstitialAd;
    private boolean isLoading = false;
    private Activity pendingActivity = null;
    private OnAdLoadInterface pendingInterface = null;

    private boolean isADTimer = true;
    public static long InterAdTimer = 2000;

    public MailER_InterstitialAdManager(Context context) {
        this.context = context;
        preferenceClass = new MailER_PreferenceClass(context);
        admobInterstitialAdId = getAdUnitId();
    }

    private String getAdUnitId() {
        if (preferenceClass == null) return "";
        boolean isOnboardingDone = preferenceClass.getRateSubmited("isOnboardingDone");
        String id = "";
        if (!isOnboardingDone) {
            id = preferenceClass.getAdsId("OnboardingInterAdID");
        }
        if (id == null || id.trim().isEmpty()) {
            id = preferenceClass.getAdsId("interstitial_ad_id");
        }
        if (id == null || id.trim().isEmpty()) {
            id = preferenceClass.getAdsId("OnboardingInterAdID");
        }
        if (id == null || id.trim().isEmpty()) {
            id = preferenceClass.getAdsId("InterstitalAdunitID");
        }
        if (id == null || id.trim().isEmpty()) {
            id = preferenceClass.getDataType("InterstitalAdunitID");
        }
        return id != null ? id.trim() : "";
    }
    
    public void loadAd() {
        if (mInterstitialAd != null || isLoading) {
            return;
        }

        boolean isEnabled = preferenceClass.getBoolean("interstitial_enabled",
                preferenceClass.getBoolean("interstitial_global_enabled", true));
        boolean isOnbEnabled = preferenceClass.getBoolean("is_onboarding_inter_enabled", true);
        String adType = preferenceClass.getDataType("interstitial_ad_type", "interstitial");
        if ((!isEnabled && !isOnbEnabled) || "none".equalsIgnoreCase(adType) || "off".equalsIgnoreCase(adType) || "false".equalsIgnoreCase(adType)) {
            Log.d("AdTracker", "Interstitial Ad is disabled or type is none. Skipping load.");
            return;
        }

        admobInterstitialAdId = getAdUnitId();
        if (admobInterstitialAdId.isEmpty()) {
            Log.d("AdTracker", "Interstitial Ad ID is empty. Skipping load.");
            return;
        }

        isLoading = true;
        AdRequest request = new AdRequest.Builder().build();
        Log.d("AdTracker", "Preloading Interstitial Ad (AdMob)... ID: " + admobInterstitialAdId);
        
        InterstitialAd.load(context, admobInterstitialAdId, request, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                Log.d("AdTracker", "AdMob Interstitial Ad Preloaded Successfully!");
                mInterstitialAd = ad;
                isLoading = false;
                com.festival.flyer.postermaker.utils.MailER_AnalyticsUtils.logAdEvent(context, "ad_load_success", "interstitial", admobInterstitialAdId, null, null);
                
                if (pendingActivity != null && pendingInterface != null) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Activity act = pendingActivity;
                    OnAdLoadInterface inf = pendingInterface;
                    pendingActivity = null;
                    pendingInterface = null;
                    loadAndShowAdmob(act, inf);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdTracker", "AdMob Interstitial Ad Failed to Preload: " + loadAdError.getMessage());
                mInterstitialAd = null;
                isLoading = false;
                com.festival.flyer.postermaker.utils.MailER_AnalyticsUtils.logAdEvent(context, "ad_load_failed", "interstitial", admobInterstitialAdId, null, loadAdError.getMessage());
                
                if (pendingActivity != null && pendingInterface != null) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    OnAdLoadInterface inf = pendingInterface;
                    pendingActivity = null;
                    pendingInterface = null;
                    inf.onAdClose();
                }
            }
        });
    }

    private void loadAndShowAdmob(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.d("AdTracker", "Failed to show AdMob Interstitial: " + adError.getMessage());
                    mInterstitialAd = null;
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                    loadAd();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    MailER_AppOpenManager.lastInterstitialShowTime = System.currentTimeMillis();
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    MailER_AppOpenManager.notifyAdClicked();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                    isADTimer = false;
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> isADTimer = true, InterAdTimer);
                    loadAd();
                }
            });
            mInterstitialAd.show(activity);
        } else {
            pendingActivity = activity;
            pendingInterface = onAdLoadInterface;
            boolean isSplashScreen = (activity != null && activity.getClass().getSimpleName().contains("SplashScreen"));
            if (!isSplashScreen) {
                if (progressDialog == null || !progressDialog.isShowing()) {
                    progressDialog = com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (pendingActivity != null && pendingInterface != null) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        try { progressDialog.dismiss(); } catch (Exception ignored) {}
                    }
                    OnAdLoadInterface inf = pendingInterface;
                    pendingActivity = null;
                    pendingInterface = null;
                    if (inf != null) inf.onAdClose();
                }
            }, 4000);
            if (!isLoading) {
                loadAd();
            }
        }
    }

    public void showAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        if (!isADTimer) {
            if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
            return;
        }

        // 1. Check Global Interstitial Enabled & Ad Type from Firebase Remote Config
        boolean isEnabled = preferenceClass.getBoolean("interstitial_enabled",
                preferenceClass.getBoolean("interstitial_global_enabled", true));
        String adType = preferenceClass.getDataType("interstitial_ad_type", "interstitial");

        if (!isEnabled || "none".equalsIgnoreCase(adType) || "off".equalsIgnoreCase(adType) || "false".equalsIgnoreCase(adType)) {
            Log.d("AdTracker", "Interstitial Ad is disabled via Remote Config (enabled=" + isEnabled + ", adType=" + adType + ")");
            if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
            return;
        }

        // 2. Editing Screen Exemption:
        // Editing screens (e.g. PosterEditActivity) have NO role in the global click counter
        // and must remain completely separate with their own flows.
        boolean isEditorScreen = (activity != null && activity.getClass().getSimpleName().contains("PosterEdit"));
        if (isEditorScreen) {
            Log.d("AdTracker", "Editor screen requested interstitial - bypassing global click counter");
            loadAndShowAdmob(activity, onAdLoadInterface);
            return;
        }

        // 3. Target Click Count from Remote Config (default 4)
        int targetInterval = preferenceClass.getInt("interstitial_count", 0);
        if (targetInterval <= 0) {
            targetInterval = preferenceClass.getAdsStatus("interstitalAdStatus");
        }
        if (targetInterval <= 0) {
            targetInterval = 4;
        }

        // 4. Global Click Counter across all other screens in the app
        int currentCount = preferenceClass.getInt("getClickCount", 0) + 1;
        Log.d("AdTracker", "▶️ Global Interstitial Click Count: " + currentCount + " / " + targetInterval);

        if (currentCount < targetInterval) {
            preferenceClass.setInt("getClickCount", currentCount);
            if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
            return;
        }

        // Target reached! Reset global counter to 0 and show the ad
        preferenceClass.setInt("getClickCount", 0);
        Log.d("AdTracker", "⚡ Global Interstitial Target Reached (" + targetInterval + ")! Showing Ad...");
        loadAndShowAdmob(activity, onAdLoadInterface);
    }

    public void showInterstitialAd(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        boolean isOnboarding = (activity != null && activity.getClass().getSimpleName().contains("Onboarding"));
        if (isOnboarding) {
            boolean isOnbEnabled = preferenceClass.getBoolean("is_onboarding_inter_enabled", true);
            if (!isOnbEnabled) {
                if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                return;
            }
        } else {
            boolean isEnabled = preferenceClass.getBoolean("interstitial_enabled",
                    preferenceClass.getBoolean("interstitial_global_enabled", true));
            String adType = preferenceClass.getDataType("interstitial_ad_type", "interstitial");

            if (!isEnabled || "none".equalsIgnoreCase(adType) || "off".equalsIgnoreCase(adType) || "false".equalsIgnoreCase(adType)) {
                if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                return;
            }
        }
        loadAndShowAdmob(activity, onAdLoadInterface);
    }

    public void showEDitAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        int interstitalAdStatus = preferenceClass.getAdsStatus("EditScreenAdCount");
        
        if (interstitalAdStatus <= 0) {
            onAdLoadInterface.onAdClose();
            return;
        }

        int getClickCount = preferenceClass.getInt("getEDitClickCount", 0) + 1;
        
        if (getClickCount < interstitalAdStatus) {
            preferenceClass.setInt("getEDitClickCount", getClickCount);
            onAdLoadInterface.onAdClose();
            return;
        }

        preferenceClass.setInt("getEDitClickCount", 0);
        loadAndShowAdmob(activity, onAdLoadInterface);
    }

    public interface OnAdLoadInterface {
        void onAdClose();
    }

    public interface OnRewardAdLoadInterface {
        void onAdClose();
    }
}
