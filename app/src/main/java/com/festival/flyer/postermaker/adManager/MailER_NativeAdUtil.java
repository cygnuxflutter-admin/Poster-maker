package com.festival.flyer.postermaker.adManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

public class MailER_NativeAdUtil {

    private final Context context;
    private final MailER_PreferenceClass preferenceClass;
    private final int width;
    private final int height;
    private NativeAdView adView;
    private NativeAd nativeAd;
    public static ShimmerFrameLayout shimmerFrameLayout;
    public MailER_NativeAdUtil(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.preferenceClass = new MailER_PreferenceClass(context);
    }

    public MailER_NativeAdUtil(Context context) {
        this.context = context;
        this.width = -1;
        this.height = -1;
        this.preferenceClass = new MailER_PreferenceClass(context);
    }

    public static void loadNativeAd(RelativeLayout nativeAdContainer, Activity context, ShimmerFrameLayout shimmer_view_container) {
        shimmerFrameLayout = shimmer_view_container;
        nativeAdContainer.setVisibility(View.VISIBLE);
        MailER_NativeAdUtil nativeAdUtil = new MailER_NativeAdUtil(context);
        nativeAdUtil.fillAdmobNativeAd(nativeAdContainer);
    }

    public void fillAdmobNativeAd(final RelativeLayout nativeAdContainer) {
        String nativeId = preferenceClass.getAdsId("NativeUnitID");
        Log.d("AdTracker", "Requesting Native Ad (AdMob) with ID: " + nativeId);
        AdLoader.Builder builder = new AdLoader.Builder(context, nativeId);

        builder.forNativeAd(nativeAd -> {
            if (this.nativeAd != null) {
                this.nativeAd.destroy();
            }
            this.nativeAd = nativeAd;
            adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.spawner_native_ad_layout, null);
            populateUnifiedNativeAdView(nativeAd, adView);
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            nativeAdContainer.setBackgroundColor(Color.parseColor("#151515"));
        });

        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdTracker", "Native Ad (AdMob) Failed to Load! Error: " + loadAdError.getMessage());
                Log.e("AdMob_Error", "AdMob Native Ad failed to load. Error: " + loadAdError.getMessage() + " | Code: " + loadAdError.getCode());
                // fillAdXNativeAd(nativeAdContainer);
            }
            @Override
            public void onAdLoaded() {
                Log.d("AdTracker", "AdMob Native Ad Loaded Successfully!");
                Log.e("AdMob_Error", "AdMob Native Ad Loaded Successfully!");
                super.onAdLoaded();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

    }

    public void fillAdXNativeAd(final RelativeLayout nativeAdContainer) {

        String adxNativeId = preferenceClass.getAdsId("AdxNativeUnitID");
        Log.d("AdTracker", "Requesting Native Ad (AdX) with ID: " + adxNativeId);
        AdLoader.Builder builder = new AdLoader.Builder(context, adxNativeId);

        builder.forNativeAd(nativeAd -> {
            if (this.nativeAd != null) {
                this.nativeAd.destroy();
            }
            this.nativeAd = nativeAd;
            adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.spawner_native_ad_layout, null);
            populateUnifiedNativeAdView(nativeAd, adView);
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            nativeAdContainer.setBackgroundColor(Color.parseColor("#151515"));
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdTracker", "Native Ad (AdX) Failed to Load! Error: " + loadAdError.getMessage());
                Log.e("AdMob_Error", "AdX Native Ad failed to load. Error: " + loadAdError.getMessage() + " | Code: " + loadAdError.getCode());
                // fbNativeAd(nativeAdContainer);
            }
            @Override
            public void onAdLoaded() {
                Log.d("AdTracker", "AdX Native Ad Loaded Successfully!");
                Log.e("AdMob_Error", "AdX Native Ad Loaded Successfully!");
                super.onAdLoaded();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

    }

    private void fbNativeAd(final RelativeLayout nativeAdContainer) {
        String fbNativeId = preferenceClass.getAdsId("fbNativeUnitID");
        Log.d("AdTracker", "Requesting Native Ad (Facebook) with ID: " + fbNativeId);
        com.facebook.ads.NativeAd nativeAd = new com.facebook.ads.NativeAd(context, fbNativeId);

        Log.e("TAG", "fb fetch native ad");
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.d("AdTracker", "Native Ad (Facebook) Failed to Load! Error: " + adError.getErrorMessage());
                if (shimmerFrameLayout!= null){
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
                nativeAdContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d("AdTracker", "Facebook Native Ad Loaded Successfully!");
                if (nativeAd != ad) {
                    return;
                }
                if (shimmerFrameLayout!= null){
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.hideShimmer();
                }
                nativeAdContainer.removeAllViews();
                View adView = com.facebook.ads.NativeAdView.render(context, nativeAd);
                nativeAdContainer.addView(adView);
                nativeAdContainer.setBackgroundColor(Color.parseColor("#151515"));

                // Native ad is loaded and ready to be displayed
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
            }
        };

        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());

    }

    public void populateUnifiedNativeAdView(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {

        RelativeLayout relativeLayout = unifiedNativeAdView.findViewById(R.id.parentLyt);
        if (shimmerFrameLayout!= null){
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            shimmerFrameLayout.hideShimmer();
        }
        /*if (width != -1 && height != -1) {
            relativeLayout.getLayoutParams().width = width;
            relativeLayout.getLayoutParams().height = 300;
            relativeLayout.invalidate();
        }*/

        MediaView mediaView = unifiedNativeAdView.findViewById(R.id.ad_media);
        unifiedNativeAdView.setMediaView(mediaView);

        unifiedNativeAdView.setHeadlineView(unifiedNativeAdView.findViewById(R.id.ad_headline));
        unifiedNativeAdView.setBodyView(unifiedNativeAdView.findViewById(R.id.ad_body));
        unifiedNativeAdView.setCallToActionView(unifiedNativeAdView.findViewById(R.id.ad_call_to_action));

        ImageView imageView = unifiedNativeAdView.findViewById(R.id.unified_image_view);

        populateNativeAdView(unifiedNativeAd, unifiedNativeAdView, mediaView, imageView);
    }

    private void populateNativeAdView(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView, MediaView mediaView, ImageView imageView) {
        int i = 0;
       /* MediaContent mediaContent = unifiedNativeAd.getMediaContent();
        if (mediaContent != null) {
            boolean hasVideo = mediaContent.getVideoController().hasVideoContent();
            if (hasVideo) {

                unifiedNativeAdView.setMediaView(mediaView);
                imageView.setVisibility(View.GONE);
            } else {
                unifiedNativeAdView.setImageView(imageView);
                mediaView.setVisibility(View.GONE);
                List<NativeAd.Image> images = unifiedNativeAd.getImages();
                if (images.size() > 0) {
                    while (true) {
                        if (i >= images.size()) {
                            break;
                        }
                        NativeAd.Image image = images.get(i);
                        if (image != null) {
                            Drawable drawable = image.getDrawable();
                            imageView.setImageDrawable(drawable);
                            break;
                        }
                        i++;
                    }
                }
            }
        } else {
            unifiedNativeAdView.setImageView(imageView);
            mediaView.setVisibility(View.GONE);
            List<NativeAd.Image> images = unifiedNativeAd.getImages();
            if (images.size() > 0) {
                while (true) {
                    if (i >= images.size()) {
                        break;
                    }
                    NativeAd.Image image = images.get(i);
                    if (image != null) {
                        Drawable drawable = image.getDrawable();
                        imageView.setImageDrawable(drawable);
                        break;
                    }
                    i++;
                }
            }
        }*/

        TextView headlineView = (TextView) unifiedNativeAdView.getHeadlineView();
        if (headlineView != null) {
            headlineView.setText(unifiedNativeAd.getHeadline());
        }

        View bodyView = unifiedNativeAdView.getBodyView();
        if (unifiedNativeAd.getBody() == null) {
            if (bodyView != null) {
                bodyView.setVisibility(View.INVISIBLE);
            }
        } else {
            if (bodyView != null) {
                bodyView.setVisibility(View.VISIBLE);
                ((TextView) bodyView).setText(unifiedNativeAd.getBody());
            }
        }

        Button callToActionView = (Button) unifiedNativeAdView.getCallToActionView();
        if (callToActionView != null) {
            callToActionView.setText(unifiedNativeAd.getCallToAction());
        }

        unifiedNativeAdView.setNativeAd(unifiedNativeAd);
    }
}
