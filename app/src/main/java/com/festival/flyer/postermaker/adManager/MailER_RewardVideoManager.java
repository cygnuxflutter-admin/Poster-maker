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

import android.widget.Toast;

public class MailER_RewardVideoManager {
    private static MailER_PreferenceClass preferenceClass;
    private static String AD_google_Rw;
    private static AlertDialog alertDialog;
    public static RewardedInterstitialAd mRewardedAd;
    public static com.facebook.ads.InterstitialAd interstitialFB;
    private static boolean isLoading = false;

    public static void preloadRewardVideoAd(Context context) {
        if (mRewardedAd != null || isLoading) return;

        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(context);
        }
        AD_google_Rw = preferenceClass.getAdsId("RewardVideoUnitID");

        if (AD_google_Rw == null || AD_google_Rw.isEmpty()) return;

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(context, AD_google_Rw, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedInterstitialAd ad) {
                mRewardedAd = ad;
                isLoading = false;
                Log.d("AdTracker", "Rewarded Video Ad Pre-loaded successfully.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mRewardedAd = null;
                isLoading = false;
                Log.d("AdTracker", "Rewarded Video Ad Pre-load failed: " + loadAdError.getMessage());
            }
        });
    }

    public static void showRewardVideoAd(final Activity context, MailER_InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
        if (mRewardedAd != null) {
            final boolean[] isEarned = new boolean[]{false};
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    mRewardedAd = null;
                    preloadRewardVideoAd(context); // Preload next
                    if (isEarned[0]) {
                        if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                    } else {
                        Toast.makeText(context, "You must watch the full ad to unlock feature!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    mRewardedAd = null;
                    preloadRewardVideoAd(context); // Preload next
                    if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                }
            });
            mRewardedAd.show(context, rewardItem -> {
                isEarned[0] = true;
            });
            return;
        }

        // Fallback: If not preloaded, show loading dialog and load
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

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(context, AD_google_Rw, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedInterstitialAd ad) {
                mRewardedAd = ad;
                isLoading = false;
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                if (mRewardedAd != null) {
                    final boolean[] isEarned = new boolean[]{false};
                    mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            mRewardedAd = null;
                            preloadRewardVideoAd(context); // Preload next
                            if (isEarned[0]) {
                                if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                            } else {
                                Toast.makeText(context, "You must watch the full ad to unlock feature!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            mRewardedAd = null;
                            preloadRewardVideoAd(context);
                            if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                        }
                    });
                    mRewardedAd.show(context, rewardItem -> {
                        isEarned[0] = true;
                    });
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                isLoading = false;
                Log.d("AdTracker", "Reward Video (AdMob) Failed to Load! Error: " + loadAdError.getMessage());
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                // Fallback to close
                onAdLoadInterface.onAdClose();
            }
        });
    }

    public static void fbInterstitial(Context context, MailER_InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
        interstitialFB = new com.facebook.ads.InterstitialAd(context, preferenceClass.getAdsId("fbInterstitalAdunitID"));
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                onAdLoadInterface.onAdClose();
            }

            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                onAdLoadInterface.onAdClose();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                interstitialFB.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };
        com.facebook.ads.InterstitialAd interstitialAd = interstitialFB;
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
    }
}
