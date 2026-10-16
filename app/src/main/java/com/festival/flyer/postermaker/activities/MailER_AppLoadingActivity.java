package com.festival.flyer.postermaker.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MailER_AppLoadingActivity extends AppCompatActivity {

    private static final String TAG = "AppLoadingActivity";

    // Top Canvas & Magic Pencil Animation views
    private FrameLayout stagePencilCanvas;
    private View ivCanvasGlow;
    private View viewCanvasSketchOutline;
    private CardView cardPosterArtwork;
    private ImageView ivSparkle1;
    private ImageView ivSparkle2;
    private ImageView ivSparkle3;
    private ImageView ivSparkle4;
    private ImageView ivDrawingPencil;
    private TextView tvAppTitle;
    private TextView tvAppSubtitle;

    // Middle progress loader views
    private TextView tvLoadingStatus;
    private TextView tvLoadingPercentage;
    private ProgressBar loadingProgressBar;

    // Bottom native ad views
    private CardView cardNativeAd;
    private RelativeLayout nativeAdContainer;
    private ShimmerFrameLayout nativeShimmerContainer;

    // Animators
    private ValueAnimator progressAnimator;
    private ObjectAnimator glowPulseAnimator;
    private final List<Animator> activeAnimators = new ArrayList<>();
    private boolean sparklesTriggered = false;

    private MailER_PreferenceClass preferenceClass;
    private boolean isNavigated = false;
    private int currentMessageIndex = -1;

    private final String[] statusMessages = new String[]{
            "✏️ Sketching creative layout...",
            "🎨 Painting vibrant colors & typography...",
            "🖼️ Curating festive graphics & stickers...",
            "✨ Adding magic sparkle effects...",
            "🎉 Studio Ready! Opening workspace..."
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_app_loading);

        preferenceClass = new MailER_PreferenceClass(this);

        // Warm up cache for Hero banners & Trending festivals while animations play
        com.festival.flyer.postermaker.utils.MailER_AppCacheManager.prefetchHomeData(this);

        initViews();
        startPencilStageAnimation();
        loadNativeAdIfEnabled();
        startProgressAnimation();
    }

    private void initViews() {
        // Animation views
        stagePencilCanvas = findViewById(R.id.stage_pencil_canvas);
        ivCanvasGlow = findViewById(R.id.iv_canvas_glow);
        viewCanvasSketchOutline = findViewById(R.id.view_canvas_sketch_outline);
        cardPosterArtwork = findViewById(R.id.card_poster_artwork);
        ivSparkle1 = findViewById(R.id.iv_sparkle_1);
        ivSparkle2 = findViewById(R.id.iv_sparkle_2);
        ivSparkle3 = findViewById(R.id.iv_sparkle_3);
        ivSparkle4 = findViewById(R.id.iv_sparkle_4);
        ivDrawingPencil = findViewById(R.id.iv_drawing_pencil);
        tvAppTitle = findViewById(R.id.tv_app_title);
        tvAppSubtitle = findViewById(R.id.tv_app_subtitle);

        // Progress loader views
        tvLoadingStatus = findViewById(R.id.tv_loading_status);
        tvLoadingPercentage = findViewById(R.id.tv_loading_percentage);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        // Native Ad views
        cardNativeAd = findViewById(R.id.card_native_ad);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        nativeShimmerContainer = findViewById(R.id.native_shimmer_container);

        if (cardPosterArtwork != null) {
            cardPosterArtwork.setAlpha(0f);
        }
        if (viewCanvasSketchOutline != null) {
            viewCanvasSketchOutline.setAlpha(0.25f);
        }

        // Initialize sparkles hidden
        ImageView[] sparkles = new ImageView[]{ivSparkle1, ivSparkle2, ivSparkle3, ivSparkle4};
        for (ImageView sp : sparkles) {
            if (sp != null) {
                sp.setAlpha(0f);
                sp.setScaleX(0f);
                sp.setScaleY(0f);
            }
        }

        if (loadingProgressBar != null) {
            loadingProgressBar.setMax(100);
            loadingProgressBar.setProgress(0);
        }

        if (tvLoadingPercentage != null) {
            tvLoadingPercentage.setText("0%");
        }

        if (statusMessages.length > 0 && tvLoadingStatus != null) {
            tvLoadingStatus.setText(statusMessages[0]);
            currentMessageIndex = 0;
        }
    }

    private void startPencilStageAnimation() {
        try {
            // Set pivot of pencil right at graphite tip (bottom-left area of pencil vector)
            if (ivDrawingPencil != null) {
                ivDrawingPencil.post(() -> {
                    int pW = ivDrawingPencil.getWidth() > 0 ? ivDrawingPencil.getWidth() : dpToPx(38);
                    int pH = ivDrawingPencil.getHeight() > 0 ? ivDrawingPencil.getHeight() : dpToPx(38);
                    ivDrawingPencil.setPivotX(pW * 0.12f);
                    ivDrawingPencil.setPivotY(pH * 0.95f);
                    // Initial position at top-left corner of canvas
                    updatePencilAndCanvas(0);
                });
            }

            // Gentle breathing glow behind canvas
            if (ivCanvasGlow != null) {
                glowPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                        ivCanvasGlow,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 0.92f, 1.08f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.92f, 1.08f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 0.35f, 0.65f)
                );
                glowPulseAnimator.setDuration(1600);
                glowPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
                glowPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
                glowPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                glowPulseAnimator.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting pencil stage animations: " + e.getMessage());
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void loadNativeAdIfEnabled() {
        boolean isNativeEnabled = true;
        String customAdId = "";
        String adType = "video"; // Default to full video/large media ad format
        int customHeightDp = 280; // Default full video ad container height (280dp)

        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterJson = MailER_PreferenceClass.getMasterConfigJson(rc);
            if (masterJson != null && !masterJson.trim().isEmpty()) {
                JSONObject root = new JSONObject(masterJson);
                if (root.has("loading_screen")) {
                    JSONObject loadObj = root.getJSONObject("loading_screen");
                    if (loadObj.has("native_ad")) {
                        JSONObject nObj = loadObj.getJSONObject("native_ad");
                        isNativeEnabled = nObj.optBoolean("enabled", nObj.optBoolean("is_show", true));
                        customAdId = nObj.optString("ad_id", nObj.optString("native_ad_id", ""));
                        adType = nObj.optString("type", nObj.optString("ad_type", adType));
                        int configuredHeight = nObj.optInt("height", -1);
                        if (configuredHeight > 0) {
                            customHeightDp = configuredHeight;
                        }
                    }
                    if (loadObj.has("height")) {
                        int h = loadObj.optInt("height", -1);
                        if (h > 0) customHeightDp = h;
                    }
                }
            } else if (rc.getKeysByPrefix("").contains("loading_screen_native_enabled")) {
                isNativeEnabled = rc.getBoolean("loading_screen_native_enabled");
                if (rc.getKeysByPrefix("").contains("loading_screen_native_ad_id")) {
                    customAdId = rc.getString("loading_screen_native_ad_id");
                }
                if (rc.getKeysByPrefix("").contains("loading_screen_native_height")) {
                    long h = rc.getLong("loading_screen_native_height");
                    if (h > 0) customHeightDp = (int) h;
                }
                if (rc.getKeysByPrefix("").contains("loading_screen_native_type")) {
                    adType = rc.getString("loading_screen_native_type");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking remote config native ad: " + e.getMessage());
        }

        if (preferenceClass != null) {
            isNativeEnabled = preferenceClass.getBoolean("loading_screen_native_enabled", isNativeEnabled);
            String savedAdId = preferenceClass.getAdsId("LoadingScreenNativeAdID");
            if (customAdId.isEmpty() && savedAdId != null && !savedAdId.isEmpty()) {
                customAdId = savedAdId;
            }
            int savedHeight = preferenceClass.getInt("loading_screen_native_height", -1);
            if (savedHeight > 0) {
                customHeightDp = savedHeight;
            }
            String savedType = preferenceClass.getDataType("loading_screen_native_type", "");
            if (!savedType.isEmpty()) {
                adType = savedType;
            }
        }

        if (isNativeEnabled && cardNativeAd != null) {
            cardNativeAd.setVisibility(View.VISIBLE);

            // Apply dynamic height from Remote Config JSON or defaults
            if (customHeightDp > 0) {
                int hPx = dpToPx(customHeightDp);
                if (nativeAdContainer != null) {
                    ViewGroup.LayoutParams aLp = nativeAdContainer.getLayoutParams();
                    if (aLp != null) {
                        aLp.height = hPx;
                        nativeAdContainer.setLayoutParams(aLp);
                    }
                }
                if (nativeShimmerContainer != null) {
                    ViewGroup.LayoutParams sLp = nativeShimmerContainer.getLayoutParams();
                    if (sLp != null) {
                        sLp.height = hPx;
                        nativeShimmerContainer.setLayoutParams(sLp);
                    }
                }
            }

            if (nativeShimmerContainer != null) {
                nativeShimmerContainer.setVisibility(View.VISIBLE);
                nativeShimmerContainer.startShimmer();
            }

            // Load ad format: Large Video / Media Native Ad by default
            if ("banner".equalsIgnoreCase(adType)) {
                MailER_LoadAds.loadAdmobBannerAd(this, nativeAdContainer, nativeShimmerContainer, customAdId.isEmpty() ? null : customAdId);
            } else if ("medium".equalsIgnoreCase(adType) || "native_medium".equalsIgnoreCase(adType)) {
                MailER_NativeAdUtil.loadMediumNativeAd(nativeAdContainer, this, nativeShimmerContainer, customAdId);
            } else {
                // Default: Large Full Video Native Ad with MediaView (spawner_native_ad_layout.xml)
                MailER_NativeAdUtil.loadNativeAd(nativeAdContainer, this, nativeShimmerContainer, customAdId);
            }
        } else {
            if (cardNativeAd != null) {
                cardNativeAd.setVisibility(View.GONE);
            }
        }
    }

    private int getTimerDurationSeconds() {
        int durationSec = 4; // Default 4 seconds

        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterJson = MailER_PreferenceClass.getMasterConfigJson(rc);
            if (masterJson != null && !masterJson.trim().isEmpty()) {
                JSONObject root = new JSONObject(masterJson);
                if (root.has("loading_screen")) {
                    JSONObject loadObj = root.getJSONObject("loading_screen");
                    durationSec = loadObj.optInt("timer_seconds", 4);
                    return Math.max(durationSec, 2);
                }
            } else if (rc.getKeysByPrefix("").contains("loading_screen_timer_seconds")) {
                long sec = rc.getLong("loading_screen_timer_seconds");
                if (sec > 0) return (int) Math.max(sec, 2);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking remote config timer: " + e.getMessage());
        }

        if (preferenceClass != null) {
            durationSec = preferenceClass.getInt("loading_screen_timer_seconds", durationSec);
        }

        return Math.max(durationSec, 2);
    }

    private void startProgressAnimation() {
        int durationSec = getTimerDurationSeconds();
        long totalMillis = durationSec * 1000L;

        progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(totalMillis);
        progressAnimator.setInterpolator(new DecelerateInterpolator(1.1f));

        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            if (loadingProgressBar != null) {
                loadingProgressBar.setProgress(progress);
            }
            if (tvLoadingPercentage != null) {
                tvLoadingPercentage.setText(progress + "%");
            }
            updatePencilAndCanvas(progress);
            updateStatusText(progress);
        });

        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                navigateToHome();
            }
        });

        progressAnimator.start();
    }

    private void updatePencilAndCanvas(int progress) {
        if (isFinishing() || isDestroyed()) return;

        int stageW = stagePencilCanvas != null && stagePencilCanvas.getWidth() > 0 ? stagePencilCanvas.getWidth() : dpToPx(180);
        int stageH = stagePencilCanvas != null && stagePencilCanvas.getHeight() > 0 ? stagePencilCanvas.getHeight() : dpToPx(170);

        int cardW = cardPosterArtwork != null && cardPosterArtwork.getWidth() > 0 ? cardPosterArtwork.getWidth() : dpToPx(105);
        int cardH = cardPosterArtwork != null && cardPosterArtwork.getHeight() > 0 ? cardPosterArtwork.getHeight() : dpToPx(138);

        int pencilW = ivDrawingPencil != null && ivDrawingPencil.getWidth() > 0 ? ivDrawingPencil.getWidth() : dpToPx(38);
        int pencilH = ivDrawingPencil != null && ivDrawingPencil.getHeight() > 0 ? ivDrawingPencil.getHeight() : dpToPx(38);

        float cardCenterX = stageW / 2.0f;
        float cardCenterY = stageH / 2.0f;
        float cardLeft = cardCenterX - (cardW / 2.0f);
        float cardRight = cardCenterX + (cardW / 2.0f);
        float cardTop = cardCenterY - (cardH / 2.0f);
        float cardBottom = cardCenterY + (cardH / 2.0f);

        // Pencil tip coordinates inside pencil view (at bottom-left corner of vector)
        float tipRatioX = 0.12f;
        float tipRatioY = 0.95f;
        float defaultTipX = (stageW / 2.0f) - (pencilW / 2.0f) + (pencilW * tipRatioX);
        float defaultTipY = (stageH / 2.0f) - (pencilH / 2.0f) + (pencilH * tipRatioY);

        float targetX;
        float targetY;
        float tilt;

        if (progress < 35) {
            // PHASE 1: Drawing the canvas frame & sketch outline (0% - 35%)
            float fraction = progress / 35.0f;

            if (cardPosterArtwork != null) {
                cardPosterArtwork.setAlpha(0.0f);
            }
            if (viewCanvasSketchOutline != null) {
                viewCanvasSketchOutline.setAlpha(0.25f + 0.75f * fraction);
            }

            if (fraction < 0.25f) {
                // Top Edge (Left -> Right)
                float sf = fraction / 0.25f;
                targetX = cardLeft + (sf * cardW);
                targetY = cardTop;
                tilt = -5f + (float) Math.sin(sf * Math.PI * 4.0) * 6f;
            } else if (fraction < 0.50f) {
                // Right Edge (Top -> Bottom)
                float sf = (fraction - 0.25f) / 0.25f;
                targetX = cardRight;
                targetY = cardTop + (sf * cardH);
                tilt = 12f + (float) Math.sin(sf * Math.PI * 4.0) * 6f;
            } else if (fraction < 0.75f) {
                // Bottom Edge (Right -> Left)
                float sf = (fraction - 0.50f) / 0.25f;
                targetX = cardRight - (sf * cardW);
                targetY = cardBottom;
                tilt = 6f + (float) Math.sin(sf * Math.PI * 4.0) * 6f;
            } else {
                // Left Edge (Bottom -> Top)
                float sf = (fraction - 0.75f) / 0.25f;
                targetX = cardLeft;
                targetY = cardBottom - (sf * cardH);
                tilt = -10f + (float) Math.sin(sf * Math.PI * 4.0) * 6f;
            }

            if (ivDrawingPencil != null) {
                ivDrawingPencil.setScaleX(1.0f);
                ivDrawingPencil.setScaleY(1.0f);
            }

        } else if (progress < 70) {
            // PHASE 2: Infilling colors & Poster Artwork Reveal (35% - 70%)
            float fraction = (progress - 35.0f) / 35.0f;

            // Fade in poster artwork smoothly
            if (cardPosterArtwork != null) {
                cardPosterArtwork.setAlpha(fraction);
            }
            // Blend outline
            if (viewCanvasSketchOutline != null) {
                viewCanvasSketchOutline.setAlpha(Math.max(0.35f, 1.0f - (fraction * 0.65f)));
            }

            // Expanding glow
            if (ivCanvasGlow != null) {
                ivCanvasGlow.setAlpha(0.4f + (fraction * 0.4f));
            }

            // Beautiful sinusoidal curved sweeps across the canvas
            float sweepAngle = fraction * (float) Math.PI * 5.0f;
            float waveX = (float) Math.sin(sweepAngle) * (cardW * 0.36f);
            targetX = cardCenterX + waveX;
            targetY = cardTop + (cardH * 0.12f) + (fraction * (cardH * 0.76f));
            tilt = (float) Math.cos(sweepAngle) * 16f;

            if (ivDrawingPencil != null) {
                ivDrawingPencil.setScaleX(1.0f);
                ivDrawingPencil.setScaleY(1.0f);
            }

        } else if (progress < 90) {
            // PHASE 3: Magic Flourish Lift & Sparkles Burst (70% - 90%)
            float fraction = (progress - 70.0f) / 20.0f;

            if (cardPosterArtwork != null) {
                cardPosterArtwork.setAlpha(1.0f);
            }
            if (viewCanvasSketchOutline != null) {
                viewCanvasSketchOutline.setAlpha(0.2f);
            }

            // Pencil lifts up towards top-right
            float startX = cardCenterX;
            float startY = cardBottom - (cardH * 0.15f);
            float endX = cardRight + dpToPx(14);
            float endY = cardTop - dpToPx(18);

            targetX = startX + fraction * (endX - startX);
            targetY = startY + fraction * (endY - startY);

            // Pencil elevates closer to the camera
            float liftScale = 1.0f + 0.30f * (float) Math.sin(fraction * (Math.PI / 2.0));
            if (ivDrawingPencil != null) {
                ivDrawingPencil.setScaleX(liftScale);
                ivDrawingPencil.setScaleY(liftScale);
            }
            tilt = 10f + (fraction * 25f);

            // Trigger Sparkles Burst once at 70%+
            if (!sparklesTriggered) {
                sparklesTriggered = true;
                triggerSparklesBurst();
                if (tvAppTitle != null) {
                    tvAppTitle.setText("Studio Ready!");
                }
                if (tvAppSubtitle != null) {
                    tvAppSubtitle.setText("Your creative workspace is prepared");
                }
            }

        } else {
            // PHASE 4: Studio Ready & Victory Celebration (90% - 100%)
            float fraction = (progress - 90.0f) / 10.0f;

            if (cardPosterArtwork != null) {
                cardPosterArtwork.setAlpha(1.0f);
                // Subtle celebratory pop bounce
                float popScale = 1.0f + 0.05f * (float) Math.sin(fraction * Math.PI);
                cardPosterArtwork.setScaleX(popScale);
                cardPosterArtwork.setScaleY(popScale);
            }

            // Pencil rests in celebratory float at top right
            targetX = cardRight + dpToPx(14);
            targetY = cardTop - dpToPx(18) + (float) Math.sin(fraction * Math.PI * 2.0) * dpToPx(3);
            tilt = 35f;

            if (ivDrawingPencil != null) {
                ivDrawingPencil.setScaleX(1.30f);
                ivDrawingPencil.setScaleY(1.30f);
            }
        }

        // Apply updated position and tilt to pencil
        if (ivDrawingPencil != null) {
            ivDrawingPencil.setTranslationX(targetX - defaultTipX);
            ivDrawingPencil.setTranslationY(targetY - defaultTipY);
            ivDrawingPencil.setRotation(tilt);
        }
    }

    private void triggerSparklesBurst() {
        ImageView[] sparkles = new ImageView[]{ivSparkle1, ivSparkle2, ivSparkle3, ivSparkle4};
        long[] delays = new long[]{0L, 80L, 160L, 240L};

        for (int i = 0; i < sparkles.length; i++) {
            final ImageView sparkle = sparkles[i];
            if (sparkle == null) continue;

            sparkle.setVisibility(View.VISIBLE);
            sparkle.setAlpha(0f);
            sparkle.setScaleX(0f);
            sparkle.setScaleY(0f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(sparkle, View.SCALE_X, 0f, 1.25f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(sparkle, View.SCALE_Y, 0f, 1.25f, 1.0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(sparkle, View.ALPHA, 0f, 1.0f);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(sparkle, View.ROTATION, 0f, 360f);

            AnimatorSet burstSet = new AnimatorSet();
            burstSet.playTogether(scaleX, scaleY, alpha, rotate);
            burstSet.setDuration(450);
            burstSet.setStartDelay(delays[i]);
            burstSet.setInterpolator(new OvershootInterpolator(2.2f));

            final int index = i;
            burstSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isFinishing() || isDestroyed()) return;
                    // Continuous subtle twinkling pulse
                    ObjectAnimator twinkle = ObjectAnimator.ofPropertyValuesHolder(
                            sparkle,
                            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.85f, 1.15f),
                            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.85f, 1.15f),
                            PropertyValuesHolder.ofFloat(View.ALPHA, 0.70f, 1.0f)
                    );
                    twinkle.setDuration(700L + (index * 150L));
                    twinkle.setRepeatCount(ValueAnimator.INFINITE);
                    twinkle.setRepeatMode(ValueAnimator.REVERSE);
                    twinkle.start();
                    activeAnimators.add(twinkle);
                }
            });

            burstSet.start();
            activeAnimators.add(burstSet);
        }
    }

    private void updateStatusText(int progress) {
        if (tvLoadingStatus == null || statusMessages.length == 0) return;

        int targetIndex;
        if (progress < 22) {
            targetIndex = 0;
        } else if (progress < 45) {
            targetIndex = 1;
        } else if (progress < 68) {
            targetIndex = 2;
        } else if (progress < 88) {
            targetIndex = 3;
        } else {
            targetIndex = 4;
        }

        if (targetIndex != currentMessageIndex && targetIndex < statusMessages.length) {
            currentMessageIndex = targetIndex;
            final String nextText = statusMessages[targetIndex];

            // Smooth crossfade transition
            tvLoadingStatus.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        if (tvLoadingStatus != null) {
                            tvLoadingStatus.setText(nextText);
                            tvLoadingStatus.animate().alpha(1f).setDuration(150).start();
                        }
                    })
                    .start();
        }
    }

    private synchronized void navigateToHome() {
        if (isNavigated || isFinishing() || isDestroyed()) return;
        isNavigated = true;

        if (nativeShimmerContainer != null) {
            nativeShimmerContainer.stopShimmer();
        }

        Intent intent = new Intent(MailER_AppLoadingActivity.this, MailER_PosterMainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Prevent accidental cancellation while setting up workspace
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressAnimator != null) {
            progressAnimator.removeAllUpdateListeners();
            progressAnimator.removeAllListeners();
            progressAnimator.cancel();
        }
        if (glowPulseAnimator != null) {
            glowPulseAnimator.cancel();
        }
        for (Animator anim : activeAnimators) {
            if (anim != null) {
                anim.cancel();
            }
        }
        activeAnimators.clear();
        if (nativeShimmerContainer != null) {
            nativeShimmerContainer.stopShimmer();
        }
    }
}
