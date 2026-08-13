package com.festival.flyer.postermaker.adManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

public class MailER_RewardVideoManager {
    private static MailER_PreferenceClass preferenceClass;
    private static String AD_google_Rw;
    private static AlertDialog alertDialog;
    public static RewardedInterstitialAd mRewardedAd;
    public static com.facebook.ads.InterstitialAd interstitialFB;


    public static void showRewardVideoAd(final Activity context, MailER_InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
        if (BuildConfig.DEBUG) {
            onAdLoadInterface.onAdClose();
            return;
        }
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(context);
        }
        AD_google_Rw = preferenceClass.getAdsId("RewardVideoUnitID");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.spawner_lottie_anim_dialog, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        if (!((Activity) context).isFinishing()) {
            try {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(context, AD_google_Rw, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedInterstitialAd ad) {
                mRewardedAd = ad;
                if (alertDialog != null) {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
                if (mRewardedAd != null) {
                    mRewardedAd.show(context, rewardItem -> {
                    });
                    mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            onAdLoadInterface.onAdClose();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            onAdLoadInterface.onAdClose();
                        }
                    });
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
//                if (alertDialog != null) {
//                    if (alertDialog.isShowing()) {
//                        alertDialog.dismiss();
//                    }
//                }
                fbInterstitial(context, onAdLoadInterface);
//                onAdLoadInterface.onAdClose();
            }
        });


//        if (preferenceClass == null) {
//            preferenceClass = new PreferenceClass(context);
//        }
//        AD_google_Rw = preferenceClass.getAdsId("google_Rw_ID");//"ca-app-pub-3940256099942544/5224354917" test key
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.lottie_anim_dialog, null);
//        dialogBuilder.setView(dialogView);
//        alertDialog = dialogBuilder.create();
//        alertDialog.setCancelable(false);
//        alertDialog.setCanceledOnTouchOutside(false);
//        if (!((Activity) context).isFinishing()) {
//            try {
//                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                alertDialog.show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        AdRequest.Builder builder = new AdRequest.Builder();
//
//        RewardedAd.load(context, AD_google_Rw, builder.build(), new RewardedAdLoadCallback() {
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                mRewardedAd = null;
//                if (alertDialog != null) {
//                    if (alertDialog.isShowing()) {
//                        alertDialog.dismiss();
//                    }
//                }
//                onAdLoadInterface.onAdFail();
////                faceBookReward(context,onAdLoadInterface);
//            }
//
//            @Override
//            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                mRewardedAd = rewardedAd;
//                if (alertDialog != null) {
//                    if (alertDialog.isShowing()) {
//                        alertDialog.dismiss();
//                    }
//                }
//
//                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                    @Override
//                    public void onAdShowedFullScreenContent() {
//                        super.onAdShowedFullScreenContent();
//                        mRewardedAd = null;
//                    }
//
//                    @Override
//                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
//                        super.onAdFailedToShowFullScreenContent(adError);
//                        if (alertDialog != null) {
//                            if (alertDialog.isShowing()) {
//                                alertDialog.dismiss();
//                            }
//                        }
//                        onAdLoadInterface.onAdFail();
//                    }
//
//                    @Override
//                    public void onAdDismissedFullScreenContent() {
//                        super.onAdDismissedFullScreenContent();
//                        if (alertDialog != null) {
//                            if (alertDialog.isShowing()) {
//                                alertDialog.dismiss();
//                            }
//                        }
//                        onAdLoadInterface.onAdClose();
//                    }
//                });
//
//                if (mRewardedAd != null) {
//                    mRewardedAd.show(context, new OnUserEarnedRewardListener() {
//                        @Override
//                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//
//                        }
//                    });
//                } else {
//                    if (alertDialog != null) {
//                        if (alertDialog.isShowing()) {
//                            alertDialog.dismiss();
//                        }
//                    }
//                    onAdLoadInterface.onAdFail();
//                    Log.d("TAG", "The rewarded ad wasn't ready yet.");
//                }
//            }
//        });
    }


    public static void fbInterstitial(Context context, MailER_InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
        if (BuildConfig.DEBUG) {
            onAdLoadInterface.onAdClose();
            return;
        }
        interstitialFB = new com.facebook.ads.InterstitialAd(context, preferenceClass.getAdsId("fbInterstitalAdunitID"));
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
//                    Log.e("#1", "" + ad.toString());
                // Interstitial ad displayed callback
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
//                    Log.e("#2", "" + ad.toString());
                // Interstitial dismissed callback
                if (alertDialog != null) {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
                onAdLoadInterface.onAdClose();
            }

            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                Log.e("#3", "" + adError.getErrorMessage());
                Log.e("#3_1", "" + adError.getErrorCode());
                // Ad error callback
                if (alertDialog != null) {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
                onAdLoadInterface.onAdClose();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                // Show the ad
//                    Log.e("#2", "" + ad.toString());
                if (alertDialog != null) {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
                interstitialFB.show();

            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };
        com.facebook.ads.InterstitialAd interstitialAd = interstitialFB;
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
    }



//    public static void faceBookReward(final Activity context , InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
//        interstitialFB = new com.facebook.ads.InterstitialAd(context, AD_Facebook_Rw);
//        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
//            @Override
//            public void onInterstitialDisplayed(Ad ad) {
//                // Interstitial ad displayed callback
//            }
//
//            @Override
//            public void onInterstitialDismissed(Ad ad) {
//                if (alertDialog != null) {
//                    if (alertDialog.isShowing()) {
//                        alertDialog.dismiss();
//                    }
//                }
//                onAdLoadInterface.onAdClose();
//            }
//
//            @Override
//            public void onError(Ad ad, com.facebook.ads.AdError adError) {
//                if (alertDialog != null) {
//                    if (alertDialog.isShowing()) {
//                        alertDialog.dismiss();
//                    }
//                }
//                onAdLoadInterface.onAdFail();
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                interstitialFB.show();
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//                // Ad clicked callback
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//                // Ad impression logged callback
//            }
//        };
//        interstitialFB.loadAd(interstitialFB.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
//    }

//    public static void showRewardVideoAd(final Activity context, InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface){
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.lottie_anim_dialog, null);
//        dialogBuilder.setView(dialogView);
//        alertDialog = dialogBuilder.create();
//        alertDialog.setCancelable(false);
//        alertDialog.setCanceledOnTouchOutside(false);
//
//        if (mRewardedAd!=null){
//
//            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    super.onAdShowedFullScreenContent();
//                    if (alertDialog != null) {
//                        if (alertDialog.isShowing()) {
//                            alertDialog.dismiss();
//                        }
//                    }
//                    mRewardedAd = null;
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
//                    super.onAdFailedToShowFullScreenContent(adError);
//                    if (alertDialog != null) {
//                        if (alertDialog.isShowing()) {
//                            alertDialog.dismiss();
//                        }
//                    }
//                    onAdLoadInterface.onAdFail();
//                }
//
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent();
//                    if (alertDialog != null) {
//                        if (alertDialog.isShowing()) {
//                            alertDialog.dismiss();
//                        }
//                    }
//                    loadGoogleRewardVideoAd(context);
//                    onAdLoadInterface.onAdClose();
//                }
//            });
//
//                mRewardedAd.show(context, new OnUserEarnedRewardListener() {
//                    @Override
//                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//
//                    }
//                });
//
//        } else {
//            if (alertDialog != null) {
//                if (alertDialog.isShowing()) {
//                    alertDialog.dismiss();
//                }
//            }
//            loadGoogleRewardVideoAd(context);
//            onAdLoadInterface.onAdFail();
//            Log.d("TAG", "The rewarded ad wasn't ready yet.");
//        }
//    }
//    public static void loadGoogleRewardVideoAd(final Activity context) {
//        if (preferenceClass == null) {
//            preferenceClass = new PreferenceClass(context);
//        }
//        AD_google_Rw = preferenceClass.getAdsId("google_Rw_ID");
//        AD_Facebook_Rw = preferenceClass.getAdsId("AD_FB_Rw_ID");
//
//        AdRequest.Builder builder = new AdRequest.Builder();
//
//        RewardedAd.load(context, AD_google_Rw, builder.build(), new RewardedAdLoadCallback() {
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                mRewardedAd = null;
//            }
//
//            @Override
//            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                mRewardedAd = rewardedAd;
//            }
//        });
//    }
}
