package com.festival.flyer.postermaker.adManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MailER_InterstitialAdManager {

    private String admobInterstitialAdId, fbInterstitialAdId;
    private final Context context;
    private final MailER_PreferenceClass preferenceClass;
    private String adXInterstitialAdId;
    private InterstitialAd admobInterstitialAd;
    private com.facebook.ads.InterstitialAd fbInterstitialAd;
    private OnAdLoadInterface onAdLoadInterface;
    private boolean isFailed = false;
    private boolean isADTimer = true;
    private com.afollestad.materialdialogs.MaterialDialog progressDialog;
    public static Integer InterAdTimer = 20000;
    public MailER_InterstitialAdManager(Context context) {
        this.context = context;
        preferenceClass = new MailER_PreferenceClass(this.context);
        admobInterstitialAdId = preferenceClass.getAdsId("InterstitalAdunitID");
        adXInterstitialAdId = preferenceClass.getAdsId("AdxInterstitalAdunitID");
        fbInterstitialAdId = preferenceClass.getAdsId("fbInterstitalAdunitID");
        // Ad will now load only when click count is close to target (Just-in-Time Pre-loading)
    }

    private void fetchFbAd() {

        fbInterstitialAd = new com.facebook.ads.InterstitialAd(context, fbInterstitialAdId);
        Log.d("AdTracker", "Requesting Interstitial Ad (Facebook) with ID: " + fbInterstitialAdId);

        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                fetchAdMobAd();
                if (onAdLoadInterface != null) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onAdLoadInterface.onAdClose();
                }
            }

            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                Log.d("AdTracker", "Interstitial Ad (Facebook) Failed to Load! Error: " + adError.getErrorMessage());
                isFailed = true;
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d("AdTracker", "Facebook Interstitial Ad Loaded Successfully!");
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        fbInterstitialAd.loadAd(fbInterstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());

    }

    private boolean isLoadingAdMob = false;

    public void fetchAdMobAd() {
        if (isAdmobAdAvailable() || isLoadingAdMob) {
            return;
        }
        if (preferenceClass != null) {
            admobInterstitialAdId = preferenceClass.getAdsId("InterstitalAdunitID");
            if (admobInterstitialAdId == null || admobInterstitialAdId.trim().isEmpty()) {
                admobInterstitialAdId = preferenceClass.getDataType("InterstitalAdunitID");
            }
        }
        if (admobInterstitialAdId == null || admobInterstitialAdId.trim().isEmpty()) {
            Log.e("AdTracker", "Interstitial Ad (AdMob) ID is empty! Skipping request.");
            return;
        }
        isLoadingAdMob = true;

        InterstitialAdLoadCallback loadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                isLoadingAdMob = false;
                Log.d("AdTracker", "AdMob Interstitial Ad Loaded Successfully! ID: " + admobInterstitialAdId);
                admobInterstitialAd = ad;

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoadingAdMob = false;
                Log.d("AdTracker", "Interstitial Ad (AdMob) Failed to Load! Error: " + loadAdError.getMessage());
                // fetchAdXAd();
            }
        };
        AdRequest request = getAdRequest();
        Log.d("AdTracker", "Requesting Interstitial Ad (AdMob) with ID: " + admobInterstitialAdId);
        InterstitialAd.load(context, admobInterstitialAdId, request, loadCallback);
    }

    public void fetchAdXAd() {
        if (isAdmobAdAvailable()) {
            return;
        }

        InterstitialAdLoadCallback loadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                Log.d("AdTracker", "AdX Interstitial Ad Loaded Successfully! ID: " + adXInterstitialAdId);
                admobInterstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdTracker", "Interstitial Ad (AdX) Failed to Load! Error: " + loadAdError.getMessage());
                // fetchFbAd();
            }
        };

        AdRequest request = getAdRequest();
        Log.d("AdTracker", "Requesting Interstitial Ad (AdX) with ID: " + adXInterstitialAdId);
        InterstitialAd.load(context, adXInterstitialAdId, request, loadCallback);
    }


    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdmobAdAvailable() {
        return admobInterstitialAd != null;
    }

    public boolean isFbAdAvailable() {
        return fbInterstitialAd != null && fbInterstitialAd.isAdLoaded() && !fbInterstitialAd.isAdInvalidated();
    }

    public void showAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        this.onAdLoadInterface = onAdLoadInterface;

        if (!isADTimer) {
            onAdLoadInterface.onAdClose();
            return;
        }

        if (isFailed) {
            isFailed = false;
            fetchAdMobAd();
        }

        int interstitalAdStatus = preferenceClass.getAdsStatus("interstitalAdStatus");
        int getClickCount = preferenceClass.getInt("getClickCount");
        
        if (getClickCount + 1 < interstitalAdStatus) {
            int currentClick = getClickCount + 1;
            preferenceClass.setInt("getClickCount", currentClick);

            // Just-in-Time Pre-loading: Load ad one click before target so it's ready when needed
            if (currentClick == interstitalAdStatus - 1 && !isAdmobAdAvailable()) {
                Log.d("[ADS_LOG]", "🔄 Pre-loading Interstitial Ad at Click " + currentClick + " / " + interstitalAdStatus + " (Target)");
                fetchAdMobAd();
            }

            Log.d("[ADS_LOG]", "▶️ Click Count: " + currentClick + " / " + interstitalAdStatus + " (Required). Skipping Ad Show.");
            onAdLoadInterface.onAdClose();
            return;
        }

        preferenceClass.setInt("getClickCount", 0);
        Log.d("[ADS_LOG]", "⚡ Click Count Target (" + interstitalAdStatus + ") Reached! Showing Interstitial Ad...");

        if (isAdmobAdAvailable()) {
            progressDialog = com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.show();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d("[ADS_LOG]", "🔴 Interstitial Ad Failed to Show: " + adError.getMessage());
                    admobInterstitialAd = null;
                    isFailed = true;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.d("[ADS_LOG]", "📺 Interstitial Ad Displayed on Screen!");
                    MailER_AppOpenManager.lastInterstitialShowTime = System.currentTimeMillis();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.d("[ADS_LOG]", "❌ Interstitial Ad Dismissed by User. Pre-fetching next Ad...");
                    admobInterstitialAd = null;
                    fetchAdMobAd();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                    
                    isADTimer = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isADTimer = true;
                        }
                    }, InterAdTimer);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: Interstitial Ad IMPRESSION Logged!");
                }
            };
            admobInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            admobInterstitialAd.show(activity);
        } else if (isFbAdAvailable()) {
            fbInterstitialAd.show();
        } else {
            onAdLoadInterface.onAdClose();
        }
    }

    public void showInterstitialAd(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        this.onAdLoadInterface = onAdLoadInterface;

        if (isFailed) {
            isFailed = false;
            fetchAdMobAd();
        }

        if (isAdmobAdAvailable()) {
            progressDialog = com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.show();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d("[ADS_LOG]", "🔴 Direct Interstitial Ad Failed to Show: " + adError.getMessage());
                    admobInterstitialAd = null;
                    isFailed = true;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.d("[ADS_LOG]", "📺 Direct Interstitial Ad Displayed on Screen!");
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.d("[ADS_LOG]", "❌ Direct Interstitial Ad Dismissed by User.");
                    admobInterstitialAd = null;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: Direct Interstitial Ad IMPRESSION Logged!");
                }
            };
            admobInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            admobInterstitialAd.show(activity);
        } else if (isFbAdAvailable()) {
            fbInterstitialAd.show();
        } else {
            // No ad available, load one for next time
            fetchAdMobAd();
            onAdLoadInterface.onAdClose();
        }
    }

    public void showEDitAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        this.onAdLoadInterface = onAdLoadInterface;

        if (isFailed) {
            isFailed = false;
            fetchAdMobAd();
        }

        int interstitalAdStatus = preferenceClass.getAdsStatus("EditScreenAdCount");
        int getClickCount = preferenceClass.getInt("getEDitClickCount");
        
        if (getClickCount < interstitalAdStatus) {
            int currentClick = getClickCount + 1;
            preferenceClass.setInt("getEDitClickCount", currentClick);

            // Just-in-Time Pre-loading: Load ad one click before target so it's ready when needed
            if (currentClick >= interstitalAdStatus - 1 && !isAdmobAdAvailable()) {
                Log.d("[ADS_LOG]", "🔄 Pre-loading EditScreen Ad at Click " + currentClick + " / " + interstitalAdStatus + " (Target)");
                fetchAdMobAd();
            }

            onAdLoadInterface.onAdClose();
            return;
        }

        preferenceClass.setInt("getEDitClickCount", 0);

        if (isAdmobAdAvailable()) {
            progressDialog = com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.show();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d("[ADS_LOG]", "🔴 EditScreen Interstitial Ad Failed to Show: " + adError.getMessage());
                    admobInterstitialAd = null;
                    isFailed = true;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.d("[ADS_LOG]", "📺 EditScreen Interstitial Ad Displayed on Screen!");
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.d("[ADS_LOG]", "❌ EditScreen Interstitial Ad Dismissed by User.");
                    admobInterstitialAd = null;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: EditScreen Interstitial Ad IMPRESSION Logged!");
                }
            };
            admobInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            admobInterstitialAd.show(activity);
        } else if (isFbAdAvailable()) {
            fbInterstitialAd.show();
        } else {
            onAdLoadInterface.onAdClose();
        }
    }

    public interface OnAdLoadInterface {
        void onAdClose();
    }

    public interface OnRewardAdLoadInterface {
        void onAdClose();
    }
}
