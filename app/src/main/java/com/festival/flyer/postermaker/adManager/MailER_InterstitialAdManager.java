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

    private final String admobInterstitialAdId, fbInterstitialAdId;
    private final Context context;
    private final MailER_PreferenceClass preferenceClass;
    private final String adXInterstitialAdId;
    private InterstitialAd admobInterstitialAd;
    private com.facebook.ads.InterstitialAd fbInterstitialAd;
    private OnAdLoadInterface onAdLoadInterface;
    private boolean isFailed = false;
    private boolean isADTimer = true;
    private ProgressDialog progressDialog;
    public static Integer InterAdTimer = 20000;
    public MailER_InterstitialAdManager(Context context) {
        this.context = context;
        preferenceClass = new MailER_PreferenceClass(this.context);
        admobInterstitialAdId = preferenceClass.getAdsId("InterstitalAdunitID");
        adXInterstitialAdId = preferenceClass.getAdsId("AdxInterstitalAdunitID");
        fbInterstitialAdId = preferenceClass.getAdsId("fbInterstitalAdunitID");
        fetchAdMobAd();
    }

    private void fetchFbAd() {

        fbInterstitialAd = new com.facebook.ads.InterstitialAd(context, fbInterstitialAdId);

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
                isFailed = true;
            }

            @Override
            public void onAdLoaded(Ad ad) {
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

    public void fetchAdMobAd() {
        if (BuildConfig.DEBUG) return;
        if (isAdmobAdAvailable()) {
            return;
        }

        InterstitialAdLoadCallback loadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                admobInterstitialAd = ad;

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                fetchAdXAd();
            }
        };
        AdRequest request = getAdRequest();
        InterstitialAd.load(context, admobInterstitialAdId, request, loadCallback);
    }

    public void fetchAdXAd() {
        if (isAdmobAdAvailable()) {
            return;
        }

        InterstitialAdLoadCallback loadCallback = new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                admobInterstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                fetchFbAd();
            }
        };

        AdRequest request = getAdRequest();
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

        if (BuildConfig.DEBUG) {
            if (onAdLoadInterface != null) {
                onAdLoadInterface.onAdClose();
            }
            return;
        }

        if (!isADTimer) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
            return;
        }

        if (isFailed) {
            isFailed = false;
            fetchAdMobAd();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
            return;
        }

        int interstitalAdStatus = preferenceClass.getAdsStatus("interstitalAdStatus");

        int getClickCount = preferenceClass.getInt("getClickCount");
        if (getClickCount < interstitalAdStatus) {
            preferenceClass.setInt("getClickCount", getClickCount + 1);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
            return;
        }

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Ad Showing...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                preferenceClass.setInt("getClickCount", 0);

                if (isAdmobAdAvailable()) {
                    FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            admobInterstitialAd = null;
                            isFailed = true;
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            onAdLoadInterface.onAdClose();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            admobInterstitialAd = null;
                            fetchAdMobAd();
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            onAdLoadInterface.onAdClose();
                            Log.e("TAG", "onAdDismissedFullScreenContent: isADTimer 1= " + isADTimer);
                            isADTimer = false;
                            Log.e("TAG", "onAdDismissedFullScreenContent: isADTimer 2= " + isADTimer);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isADTimer = true;
                                    Log.e("TAG", "onAdDismissedFullScreenContent: isADTimer 3= " + isADTimer);
                                }
                            }, InterAdTimer);
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }
                    };
                    admobInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);

                    admobInterstitialAd.show(activity);
                } else if (isFbAdAvailable()) {
                    fbInterstitialAd.show();
                } else {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onAdLoadInterface.onAdClose();
                }
            }
        }, 2000);
    }

    public void showInterstitialAd(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        this.onAdLoadInterface = onAdLoadInterface;

        if (BuildConfig.DEBUG) {
            if (onAdLoadInterface != null) {
                onAdLoadInterface.onAdClose();
            }
            return;
        }

        if (isFailed) {
            isFailed = false;
            fetchAdMobAd();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
            return;
        }
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Ad Showing...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (isAdmobAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    admobInterstitialAd = null;
                    isFailed = true;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    admobInterstitialAd = null;
                    fetchAdMobAd();
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onAdLoadInterface.onAdClose();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }
            };
            admobInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            admobInterstitialAd.show(activity);
        } else if (isFbAdAvailable()) {
            fbInterstitialAd.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
        }

    }

    public void showEDitAdIfAvailable(Activity activity, OnAdLoadInterface onAdLoadInterface) {
        this.onAdLoadInterface = onAdLoadInterface;

        if (BuildConfig.DEBUG) {
            if (onAdLoadInterface != null) {
                onAdLoadInterface.onAdClose();
            }
            return;
        }

        if (isFailed) {
            isFailed = false;
            fetchAdMobAd();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
            return;
        }

        int interstitalAdStatus = preferenceClass.getAdsStatus("EditScreenAdCount");

        int getClickCount = preferenceClass.getInt("getEDitClickCount");
        if (getClickCount < interstitalAdStatus) {
            preferenceClass.setInt("getEDitClickCount", getClickCount + 1);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onAdLoadInterface.onAdClose();
            return;
        }

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Ad Showing...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                preferenceClass.setInt("getEDitClickCount", 0);

                if (isAdmobAdAvailable()) {
                    FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            admobInterstitialAd = null;
                            isFailed = true;
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            onAdLoadInterface.onAdClose();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            admobInterstitialAd = null;
                            fetchAdMobAd();
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            onAdLoadInterface.onAdClose();
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }
                    };
                    admobInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                    admobInterstitialAd.show(activity);
                } else if (isFbAdAvailable()) {
                    fbInterstitialAd.show();
                } else {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    onAdLoadInterface.onAdClose();
                }

            }
        }, 2000);
    }

    public interface OnAdLoadInterface {
        void onAdClose();
    }

    public interface OnRewardAdLoadInterface {
        void onAdClose();
    }
}
