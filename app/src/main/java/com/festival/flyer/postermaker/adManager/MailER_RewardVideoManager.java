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
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import android.widget.Toast;

public class MailER_RewardVideoManager {
    private static MailER_PreferenceClass preferenceClass;
    private static String AD_google_Rw;
    private static AlertDialog alertDialog;
    public static com.facebook.ads.InterstitialAd interstitialFB;
    private static boolean isLoading = false;

    public static void showRewardVideoAd(final Activity context, MailER_InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
        if (preferenceClass == null) {
            preferenceClass = new MailER_PreferenceClass(context);
        }
        AD_google_Rw = preferenceClass.getAdsId("RewardVideoUnitID");

        if (AD_google_Rw == null || AD_google_Rw.isEmpty()) {
            if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
            return;
        }

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
        Log.d("[ADS_LOG]", "🎁 Requesting Reward Video ON DEMAND...");
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, AD_google_Rw, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                isLoading = false;
                Log.d("[ADS_LOG]", "🟢 Reward Video Ad Loaded Successfully!");
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                final boolean[] isEarned = new boolean[]{false};
                ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        Log.d("[ADS_LOG]", "❌ Reward Video Ad Closed by User.");
                        if (isEarned[0]) {
                            Log.d("[ADS_LOG]", "✅ User earned reward! Proceeding...");
                            if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                        } else {
                            Log.d("[ADS_LOG]", "⚠️ User skipped Ad! Reward not given.");
                            Toast.makeText(context, "You must watch the full ad to unlock feature!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        Log.d("[ADS_LOG]", "🔴 Reward Video Failed to Show: " + adError.getMessage());
                        if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        Log.d("[ADS_LOG]", "📺 Reward Video Displayed on Screen!");
                    }
                });
                ad.show(context, rewardItem -> {
                    isEarned[0] = true;
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoading = false;
                Log.d("[ADS_LOG]", "🔴 Reward Video Failed to Load: " + loadAdError.getMessage() + ". Executing Fallback (Free Access).");
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                // FAIL OPEN: Grant reward if ad fails to load
                if (onAdLoadInterface != null) onAdLoadInterface.onAdClose();
            }
        });
    }

    public static void fbInterstitial(Context context, MailER_InterstitialAdManager.OnRewardAdLoadInterface onAdLoadInterface) {
        interstitialFB = new com.facebook.ads.InterstitialAd(context, preferenceClass.getAdsId("fbInterstitalAdunitID"));
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {}

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
            public void onAdClicked(Ad ad) {}

            @Override
            public void onLoggingImpression(Ad ad) {}
        };
        com.facebook.ads.InterstitialAd interstitialAd = interstitialFB;
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
    }
}
