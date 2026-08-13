package com.festival.flyer.postermaker.adManager;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

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

    /**
     * Constructor
     */
    public MailER_AppOpenManager(MyApplication myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    /**
     * Request an ad
     */
    public void fetchAd() {
        if (BuildConfig.DEBUG) return;
        if (AppOpenAdShow == 0) {
            return;
        }
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(AppOpenAd ad) {
                MailER_AppOpenManager.this.appOpenAd = ad;
                MailER_AppOpenManager.this.loadTime = (new Date()).getTime();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
                fetchAdX();
            }
        };

        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(myApplication);
        }
        AD_UNIT_ID1 = preferenceClass.getAdsId("AppOpenID");
        AD_UNIT_ID2 = preferenceClass.getAdsId("AdxAppOpenID");

        AdRequest request = getAdRequest();
        AppOpenAd.load(myApplication, AD_UNIT_ID1, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    public void fetchAdX() {
        if (isAdAvailable()) {
            return;
        }
        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                MailER_AppOpenManager.this.appOpenAd = ad;
                MailER_AppOpenManager.this.loadTime = new Date().getTime();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

            }
        };
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(myApplication);
        }
        AD_UNIT_ID1 = preferenceClass.getAdsId("AppOpenID");
        AD_UNIT_ID2 = preferenceClass.getAdsId("AdxAppOpenID");

        AdRequest request = getAdRequest();
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
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            if (MyApplication.isShowingAppOpen) {
                FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        MailER_AppOpenManager.this.appOpenAd = null;
                        isShowingAd = false;
                        fetchAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        isShowingAd = true;
                    }
                };

                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.show(currentActivity);
            }
        } else {
            fetchAd();
        }
    }

    public void showAdIfSplashAvailable(@NonNull final Activity activity, @NonNull MyApplication.OnShowAdCompleteListener onShowAdCompleteListener) {
        if (BuildConfig.DEBUG) {
            onShowAdCompleteListener.onShowAdComplete();
            return;
        }
        if (!isShowingAd && isAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    fetchAd();
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                }
            };
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(activity);
        } else {
            loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(AppOpenAd ad) {
                    MailER_AppOpenManager.this.appOpenAd = ad;
                    MailER_AppOpenManager.this.loadTime = (new Date()).getTime();

                    FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            MailER_AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            fetchAd();
                            onShowAdCompleteListener.onShowAdComplete();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            onShowAdCompleteListener.onShowAdComplete();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };
                    appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                    appOpenAd.show(currentActivity);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    onShowAdCompleteListener.onShowAdComplete();
                }
            };
            if (preferenceClass == null) {
                preferenceClass = new MailER_PreferenceClass(myApplication);
            }
            AD_UNIT_ID1 = preferenceClass.getAdsId("AppOpenID");
            AD_UNIT_ID2 = preferenceClass.getAdsId("AdxAppOpenID");
            AdRequest request = getAdRequest();
            AppOpenAd.load(myApplication, AD_UNIT_ID1, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
        }
    }

    public void showAdIfAvailable(@NonNull final Activity activity, @NonNull MyApplication.OnShowAdCompleteListener onShowAdCompleteListener) {
        if (BuildConfig.DEBUG) {
            onShowAdCompleteListener.onShowAdComplete();
            return;
        }
        if (!isShowingAd && isAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    MailER_AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    onShowAdCompleteListener.onShowAdComplete();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
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