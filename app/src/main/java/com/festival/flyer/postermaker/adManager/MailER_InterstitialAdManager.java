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
    private final String admobInterstitialAdId;
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
        admobInterstitialAdId = preferenceClass.getAdsId("InterstitalAdunitID");
        loadAd();
    }
    
    public void loadAd() {
        if (mInterstitialAd != null || isLoading) {
            return;
        }
        isLoading = true;
        AdRequest request = new AdRequest.Builder().build();
        Log.d("AdTracker", "Preloading Interstitial Ad (AdMob)...");
        
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
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                    isADTimer = false;
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> isADTimer = true, InterAdTimer);
                    loadAd(); // Preload next ad immediately
                }
            });
            mInterstitialAd.show(activity);
        } else {
            pendingActivity = activity;
            pendingInterface = onAdLoadInterface;
            if (progressDialog == null || !progressDialog.isShowing()) {
                progressDialog = com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            if (!isLoading) {
                loadAd();
            }
        }
    }

    public void showAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        if (!isADTimer) {
            onAdLoadInterface.onAdClose();
            return;
        }

        int interstitalAdStatus = preferenceClass.getAdsStatus("interstitalAdStatus");
        
        if (interstitalAdStatus <= 0) {
            onAdLoadInterface.onAdClose();
            return;
        }

        int getClickCount = preferenceClass.getInt("getClickCount") + 1;
        
        if (getClickCount < interstitalAdStatus) {
            preferenceClass.setInt("getClickCount", getClickCount);
            onAdLoadInterface.onAdClose();
            return;
        }

        preferenceClass.setInt("getClickCount", 0);
        loadAndShowAdmob(activity, onAdLoadInterface);
    }

    public void showInterstitialAd(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        loadAndShowAdmob(activity, onAdLoadInterface);
    }

    public void showEDitAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        int interstitalAdStatus = preferenceClass.getAdsStatus("EditScreenAdCount");
        
        if (interstitalAdStatus <= 0) {
            onAdLoadInterface.onAdClose();
            return;
        }

        int getClickCount = preferenceClass.getInt("getEDitClickCount") + 1;
        
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
