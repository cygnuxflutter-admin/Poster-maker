package com.festival.flyer.postermaker.adManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.UUID;

public class MailER_LoadAds {

    public static ShimmerFrameLayout shimmerFrameLayout;

    private static void destroyExistingBannerView(android.view.ViewGroup container) {
        if (container == null) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof AdView) {
                try {
                    ((AdView) child).destroy();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static boolean hasLoadedAdView(android.view.ViewGroup container) {
        if (container == null) return false;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof AdView) {
                return true;
            }
        }
        return false;
    }

    public static void hideParentBannerContainer(View view) {
        if (view == null) return;
        try {
            view.setVisibility(View.GONE);
            android.view.ViewParent parent = view.getParent();
            while (parent instanceof View) {
                View pv = (View) parent;
                int id = pv.getId();
                if (id == R.id.btm || id == R.id.rl_banner_ad || id == R.id.rl_collapsible
                        || id == R.id.ad_container_wrapper || id == R.id.ad_layout_container
                        || id == R.id.rl_native_ad || id == R.id.ad_layout || id == R.id.rlMain) {
                    pv.setVisibility(View.GONE);
                }
                if (pv instanceof androidx.cardview.widget.CardView) {
                    if (pv.findViewById(R.id.button1) == null
                            && pv.findViewById(R.id.button2) == null
                            && pv.findViewById(R.id.btn_dialog_watch) == null) {
                        pv.setVisibility(View.GONE);
                        android.view.ViewGroup.LayoutParams lp = pv.getLayoutParams();
                        if (lp != null) {
                            lp.width = 0;
                            lp.height = 0;
                            pv.setLayoutParams(lp);
                        }
                    }
                }
                if (id == android.R.id.content) {
                    break;
                }
                parent = pv.getParent();
            }
        } catch (Exception ignored) {}
    }

    public static void showParentBannerContainer(View view) {
        if (view == null) return;
        try {
            android.view.ViewParent parent = view.getParent();
            while (parent instanceof View) {
                View pv = (View) parent;
                int id = pv.getId();
                if (id == R.id.btm || id == R.id.rl_banner_ad || id == R.id.rl_collapsible
                        || id == R.id.ad_container_wrapper || id == R.id.ad_layout_container
                        || id == R.id.rl_native_ad || id == R.id.ad_layout || id == R.id.rlMain) {
                    pv.setVisibility(View.VISIBLE);
                }
                if (pv instanceof androidx.cardview.widget.CardView) {
                    if (pv.findViewById(R.id.button1) == null
                            && pv.findViewById(R.id.button2) == null
                            && pv.findViewById(R.id.btn_dialog_watch) == null) {
                        pv.setVisibility(View.VISIBLE);
                        android.view.ViewGroup.LayoutParams lp = pv.getLayoutParams();
                        if (lp != null && (lp.width <= 0 || lp.height <= 0)) {
                            lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                            lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                            pv.setLayoutParams(lp);
                        }
                    }
                }
                if (id == android.R.id.content) {
                    break;
                }
                parent = pv.getParent();
            }
        } catch (Exception ignored) {}
    }

    public static void loadCollapsibleBanner(Activity activity, FrameLayout mainLayout,RelativeLayout relativeLayout, ShimmerFrameLayout shimmer_view_container) {
        loadCollapsibleBanner(activity, mainLayout, relativeLayout, shimmer_view_container, null);
    }

    public static void loadCollapsibleBanner(Activity activity, FrameLayout mainLayout, RelativeLayout relativeLayout, ShimmerFrameLayout shimmer_view_container, String customAdUnitId) {
        if (new MailER_PreferenceClass(activity).getAdsStatus("bannerAdStatus") == 0) {
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(android.view.View.GONE);
            }
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
            if (relativeLayout != null) {
                relativeLayout.setVisibility(View.GONE);
            }
            hideParentBannerContainer(mainLayout);
            return;
        }
        String CollapsiblebannerID = (customAdUnitId != null && !customAdUnitId.trim().isEmpty())
                ? customAdUnitId.trim()
                : new MailER_PreferenceClass(activity).getAdsId("CollapsibleBannerID");
        if (CollapsiblebannerID == null || CollapsiblebannerID.trim().isEmpty()) {
            CollapsiblebannerID = new MailER_PreferenceClass(activity).getAdsId("BannerAdunitID");
        }

        CollapsiblebannerID = MailER_PreferenceClass.getSafeAdUnitId(CollapsiblebannerID, "collapsible");

        shimmerFrameLayout = shimmer_view_container;
        boolean showShimmerOnFail = MailER_PreferenceClass.isBannerShimmerOnFail(activity);
        if (showShimmerOnFail) {
            if (shimmer_view_container != null) {
                shimmer_view_container.setVisibility(View.VISIBLE);
                shimmer_view_container.startShimmer();
            }
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
            if (relativeLayout != null) {
                relativeLayout.setVisibility(View.VISIBLE);
            }
            showParentBannerContainer(shimmer_view_container != null ? shimmer_view_container : mainLayout);
        } else {
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(View.GONE);
            }
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
            if (relativeLayout != null) {
                relativeLayout.setVisibility(View.GONE);
            }
            hideParentBannerContainer(mainLayout);
        }

        // Skip reload if banner already loaded in this container
        if (hasLoadedAdView(mainLayout)) {
            Log.d("[ADS_LOG]", "♻️ Collapsible Banner already loaded in container. Skipping reload.");
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(View.GONE);
            }
            if (mainLayout != null) mainLayout.setVisibility(View.VISIBLE);
            if (relativeLayout != null) relativeLayout.setVisibility(View.VISIBLE);
            showParentBannerContainer(mainLayout);
            return;
        }
        destroyExistingBannerView(mainLayout);
        mainLayout.removeAllViews();
        String bannerAdunitID = CollapsiblebannerID ;
        if (bannerAdunitID != null && !bannerAdunitID.trim().isEmpty()) {
            AdView adView = new AdView(activity);
            AdSize adSize = getAdSize(activity,mainLayout);
            adView.setAdSize(adSize);
            adView.setAdUnitId(bannerAdunitID);
            Bundle extras = new Bundle();
            extras.putString("collapsible", "bottom");
            extras.putString("collapsible_request_id", UUID.randomUUID().toString());
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();

            // Step 1: Add view to container FIRST
            mainLayout.addView(adView);

            // Step 2: Set listener BEFORE loading ad
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d("[ADS_LOG]", "🔴 Collapsible Banner Ad Failed to Load: " + loadAdError.getMessage());
                    Log.d("AdTracker", "Collapsible Banner (AdMob) Failed to Load! Error: " + loadAdError.getMessage());
                    boolean showShimmer = MailER_PreferenceClass.isBannerShimmerOnFail(activity);
                    if (showShimmer) {
                        if (shimmer_view_container != null) {
                            shimmer_view_container.startShimmer();
                            shimmer_view_container.setVisibility(View.VISIBLE);
                        }
                        if (mainLayout != null) {
                            mainLayout.setVisibility(View.GONE);
                        }
                        if (relativeLayout != null) {
                            relativeLayout.setVisibility(View.VISIBLE);
                        }
                        showParentBannerContainer(shimmer_view_container != null ? shimmer_view_container : mainLayout);
                    } else {
                        if (shimmer_view_container != null) {
                            shimmer_view_container.stopShimmer();
                            shimmer_view_container.setVisibility(View.GONE);
                            shimmer_view_container.hideShimmer();
                        }
                        if (mainLayout != null) {
                            mainLayout.setVisibility(View.GONE);
                        }
                        if (relativeLayout != null) {
                            relativeLayout.setVisibility(View.GONE);
                        }
                        hideParentBannerContainer(mainLayout);
                    }
                }
                @Override
                public void onAdLoaded() {
                    Log.d("[ADS_LOG]", "🟢 Collapsible Banner Ad Loaded & Displayed Successfully!");
                    Log.d("AdTracker", "AdMob Collapsible Banner Ad Loaded Successfully!");
                    super.onAdLoaded();
                    if (shimmer_view_container != null) {
                        shimmer_view_container.stopShimmer();
                        shimmer_view_container.setVisibility(View.GONE);
                        shimmer_view_container.hideShimmer();
                    }
                    if (mainLayout != null) {
                        mainLayout.setVisibility(View.VISIBLE);
                    }
                    if (relativeLayout != null) {
                        relativeLayout.setVisibility(View.VISIBLE);
                    }
                    showParentBannerContainer(mainLayout);
                }
                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: Collapsible Banner Ad IMPRESSION Logged!");
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    MailER_AppOpenManager.notifyAdClicked();
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    MailER_AppOpenManager.notifyAdClicked();
                }
            });

            // Step 3: Load ad LAST (after view added + listener set)
            try {
                Log.d("AdTracker", "Requesting Collapsible Banner (AdMob) with ID: " + bannerAdunitID);
                adView.loadAd(adRequest);
            } catch (Exception e) {
                Log.e("TAG", "loadCollapsibleBanner: Catch"+ e.getMessage() );
            }
        } else {
            boolean showShimmer = MailER_PreferenceClass.isBannerShimmerOnFail(activity);
            if (showShimmer) {
                if (shimmer_view_container != null) {
                    shimmer_view_container.startShimmer();
                    shimmer_view_container.setVisibility(View.VISIBLE);
                }
                if (mainLayout != null) {
                    mainLayout.setVisibility(View.GONE);
                }
                if (relativeLayout != null) {
                    relativeLayout.setVisibility(View.VISIBLE);
                }
                showParentBannerContainer(shimmer_view_container != null ? shimmer_view_container : mainLayout);
            } else {
                if (shimmer_view_container != null) {
                    shimmer_view_container.stopShimmer();
                    shimmer_view_container.setVisibility(View.GONE);
                }
                if (mainLayout != null) {
                    mainLayout.setVisibility(View.GONE);
                }
                if (relativeLayout != null) {
                    relativeLayout.setVisibility(View.GONE);
                }
                hideParentBannerContainer(mainLayout);
            }
        }

    }

    private static AdSize getAdSize(Activity activity, FrameLayout mainLayout) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = mainLayout.getWidth();


        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public static void loadAdmobBannerAd(Activity activity, RelativeLayout mainLayout, ShimmerFrameLayout shimmer_view_container) {
        loadAdmobBannerAd(activity, mainLayout, shimmer_view_container, null);
    }

    public static void loadAdmobBannerAd(Activity activity, RelativeLayout mainLayout, ShimmerFrameLayout shimmer_view_container, String customAdUnitId) {
        if (new MailER_PreferenceClass(activity).getAdsStatus("bannerAdStatus") == 0) {
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(android.view.View.GONE);
            }
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
            hideParentBannerContainer(mainLayout);
            return;
        }
        shimmerFrameLayout = shimmer_view_container;

        boolean showShimmerOnFail = MailER_PreferenceClass.isBannerShimmerOnFail(activity);
        if (showShimmerOnFail) {
            if (shimmer_view_container != null) {
                shimmer_view_container.setVisibility(View.VISIBLE);
                shimmer_view_container.startShimmer();
            }
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
            showParentBannerContainer(shimmer_view_container != null ? shimmer_view_container : mainLayout);
        } else {
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(View.GONE);
            }
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
            hideParentBannerContainer(mainLayout);
        }

        // Skip reload if banner already loaded in this container
        if (hasLoadedAdView(mainLayout)) {
            Log.d("[ADS_LOG]", "♻️ Banner Ad already loaded in container. Skipping reload.");
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(View.GONE);
            }
            mainLayout.setVisibility(View.VISIBLE);
            showParentBannerContainer(mainLayout);
            return;
        }
        destroyExistingBannerView(mainLayout);
        mainLayout.removeAllViews();
        String bannerAdunitID = (customAdUnitId != null && !customAdUnitId.trim().isEmpty())
                ? customAdUnitId.trim()
                : new MailER_PreferenceClass(activity).getAdsId("BannerAdunitID");
        bannerAdunitID = MailER_PreferenceClass.getSafeAdUnitId(bannerAdunitID, "banner");
        if (bannerAdunitID != null && !bannerAdunitID.trim().isEmpty()) {
            AdView adView = new AdView(activity);
            AdSize adSize = getAdSize(activity);
            adView.setAdSize(adSize);
            adView.setAdUnitId(bannerAdunitID);

            AdRequest adRequest = new AdRequest.Builder().build();

            // Step 1: Add view FIRST
            RelativeLayout.LayoutParams bannerParameters =
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            bannerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mainLayout.addView(adView, bannerParameters);

            // Step 2: Set listener BEFORE load
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d("[ADS_LOG]", "🔴 Banner Ad (AdMob) Failed to Load: " + loadAdError.getMessage());
                    boolean showShimmer = MailER_PreferenceClass.isBannerShimmerOnFail(activity);
                    if (showShimmer) {
                        if (shimmer_view_container != null) {
                            shimmer_view_container.setVisibility(View.VISIBLE);
                            shimmer_view_container.startShimmer();
                        }
                        if (mainLayout != null) {
                            mainLayout.setVisibility(View.GONE);
                        }
                        showParentBannerContainer(shimmer_view_container != null ? shimmer_view_container : mainLayout);
                    } else {
                        if (shimmer_view_container != null) {
                            shimmer_view_container.stopShimmer();
                            shimmer_view_container.setVisibility(View.GONE);
                            shimmer_view_container.hideShimmer();
                        }
                        if (mainLayout != null) {
                            mainLayout.setVisibility(View.GONE);
                        }
                        hideParentBannerContainer(mainLayout);
                    }
                }
                @Override
                public void onAdLoaded() {
                    Log.d("[ADS_LOG]", "🟢 AdMob Banner Ad Loaded & Displayed Successfully!");
                    super.onAdLoaded();
                    // Ad loaded — now show the container cleanly
                    if (shimmer_view_container!= null) {
                        shimmer_view_container.stopShimmer();
                        shimmer_view_container.setVisibility(View.GONE);
                        shimmer_view_container.hideShimmer();
                    }
                    if (mainLayout != null) {
                        mainLayout.setVisibility(View.VISIBLE);
                    }
                    showParentBannerContainer(mainLayout);
                }
                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.d("[ADS_LOG]", "🟢 SUCCESS: Banner Ad IMPRESSION Logged!");
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    MailER_AppOpenManager.notifyAdClicked();
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    MailER_AppOpenManager.notifyAdClicked();
                }
            });

            // Step 3: Load ad LAST
            try {
                Log.d("AdTracker", "Requesting Banner Ad (AdMob) with ID: " + bannerAdunitID);
                adView.loadAd(adRequest);
            } catch (Exception e) {

            }
        } else {
            boolean showShimmer = MailER_PreferenceClass.isBannerShimmerOnFail(activity);
            if (showShimmer) {
                if (shimmer_view_container != null) {
                    shimmer_view_container.setVisibility(View.VISIBLE);
                    shimmer_view_container.startShimmer();
                }
                if (mainLayout != null) {
                    mainLayout.setVisibility(View.GONE);
                }
                showParentBannerContainer(shimmer_view_container != null ? shimmer_view_container : mainLayout);
            } else {
                if (shimmer_view_container != null) {
                    shimmer_view_container.stopShimmer();
                    shimmer_view_container.setVisibility(View.GONE);
                }
                if (mainLayout != null) {
                    mainLayout.setVisibility(View.GONE);
                }
                hideParentBannerContainer(mainLayout);
            }
        }
    }

    private static void loadADXBannerAd(Activity activity, RelativeLayout mainLayout) {
        destroyExistingBannerView(mainLayout);
        mainLayout.removeAllViews();
        String AdxBannerAdunitID = new MailER_PreferenceClass(activity).getAdsId("AdxBannerAdunitID");
        if (AdxBannerAdunitID != null) {
            AdView adView = new AdView(activity);
            AdSize adSize = getAdSize(activity);
            adView.setAdSize(adSize);
            adView.setAdUnitId(AdxBannerAdunitID);

            AdRequest adRequest = new AdRequest.Builder().build();
            Log.d("AdTracker", "Requesting Banner Ad (AdX) with ID: " + AdxBannerAdunitID);
            adView.loadAd(adRequest);

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d("AdTracker", "Banner Ad (AdX) Failed to Load! Error: " + loadAdError.getMessage());
                    // loadFBBannerAd(activity, mainLayout);
                }
                @Override
                public void onAdLoaded() {
                    Log.d("AdTracker", "AdX Banner Ad Loaded Successfully!");
                    super.onAdLoaded();
                    if (shimmerFrameLayout!= null) {
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.hideShimmer();
                    }
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    MailER_AppOpenManager.notifyAdClicked();
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    MailER_AppOpenManager.notifyAdClicked();
                }
            });

            RelativeLayout.LayoutParams bannerParameters =
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            bannerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mainLayout.addView(adView, bannerParameters);
        }
    }

    private static void loadFBBannerAd(Activity activity, RelativeLayout mainLayout) {
        mainLayout.removeAllViews();
        String fbBannerAdunitID = new MailER_PreferenceClass(activity).getAdsId("fbBannerAdunitID");
        Log.d("AdTracker", "Requesting Banner Ad (Facebook) with ID: " + fbBannerAdunitID);
        com.facebook.ads.AdView fbBannerView = new com.facebook.ads.AdView(activity, fbBannerAdunitID, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        mainLayout.addView(fbBannerView);

        mainLayout.setGravity(Gravity.BOTTOM);

        com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d("AdTracker", "Banner Ad (Facebook) Failed to Load! Error: " + adError.getErrorMessage());
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
                mainLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d("AdTracker", "Facebook Banner Ad Loaded Successfully!");
                if (shimmerFrameLayout!= null) {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.hideShimmer();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                MailER_AppOpenManager.notifyAdClicked();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };

        fbBannerView.loadAd(fbBannerView.buildLoadAdConfig().withAdListener(adListener).build());

    }

    private static AdSize getAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

}
