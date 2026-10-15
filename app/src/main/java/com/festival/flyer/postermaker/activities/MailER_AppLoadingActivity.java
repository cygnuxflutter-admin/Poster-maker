package com.festival.flyer.postermaker.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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
import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONObject;

public class MailER_AppLoadingActivity extends AppCompatActivity {

    private static final String TAG = "AppLoadingActivity";

    private FrameLayout flIconContainer;
    private ImageView ivLoadingIcon;
    private TextView tvLoadingTitle;
    private TextView tvLoadingStatus;
    private ProgressBar loadingProgressBar;
    private CardView cardNativeAd;
    private RelativeLayout nativeAdContainer;
    private ShimmerFrameLayout nativeShimmerContainer;
    private TextView tvBottomHint;

    private MailER_PreferenceClass preferenceClass;
    private ValueAnimator progressAnimator;
    private ObjectAnimator pulseAnimator;

    private boolean isNavigated = false;
    private int currentMessageIndex = -1;

    private final String[] statusMessages = new String[]{
            "Finding the best templates for you...",
            "Curating top categories & styles...",
            "Preparing high-resolution designs...",
            "Setting up your creative workspace...",
            "Almost ready!..."
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_app_loading);

        preferenceClass = new MailER_PreferenceClass(this);

        initViews();
        setupPulseAnimation();
        loadNativeAdIfEnabled();
        startProgressAnimation();
    }

    private void initViews() {
        flIconContainer = findViewById(R.id.fl_icon_container);
        ivLoadingIcon = findViewById(R.id.iv_loading_icon);
        tvLoadingTitle = findViewById(R.id.tv_loading_title);
        tvLoadingStatus = findViewById(R.id.tv_loading_status);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        cardNativeAd = findViewById(R.id.card_native_ad);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        nativeShimmerContainer = findViewById(R.id.native_shimmer_container);
        tvBottomHint = findViewById(R.id.tv_bottom_hint);

        loadingProgressBar.setMax(100);
        loadingProgressBar.setProgress(0);

        if (statusMessages.length > 0) {
            tvLoadingStatus.setText(statusMessages[0]);
            currentMessageIndex = 0;
        }
    }

    private void setupPulseAnimation() {
        if (flIconContainer == null) return;
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.12f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.12f);
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(flIconContainer, scaleX, scaleY);
        pulseAnimator.setDuration(900);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.start();
    }

    private void loadNativeAdIfEnabled() {
        boolean isNativeEnabled = true;
        String customAdId = "";

        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterJson = rc.getString("app_ad_config");
            if (masterJson != null && !masterJson.trim().isEmpty()) {
                JSONObject root = new JSONObject(masterJson);
                if (root.has("loading_screen")) {
                    JSONObject loadObj = root.getJSONObject("loading_screen");
                    if (loadObj.has("native_ad")) {
                        JSONObject nObj = loadObj.getJSONObject("native_ad");
                        isNativeEnabled = nObj.optBoolean("enabled", true);
                        customAdId = nObj.optString("ad_id", "");
                    }
                }
            } else if (rc.getKeysByPrefix("").contains("loading_screen_native_enabled")) {
                isNativeEnabled = rc.getBoolean("loading_screen_native_enabled");
                if (rc.getKeysByPrefix("").contains("loading_screen_native_ad_id")) {
                    customAdId = rc.getString("loading_screen_native_ad_id");
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
        }

        if (isNativeEnabled) {
            if (cardNativeAd != null) cardNativeAd.setVisibility(View.VISIBLE);
            if (nativeShimmerContainer != null) {
                nativeShimmerContainer.setVisibility(View.VISIBLE);
                nativeShimmerContainer.startShimmer();
            }

            if (!customAdId.trim().isEmpty() && preferenceClass != null) {
                preferenceClass.setAdsId("NativeUnitID", customAdId.trim());
            }

            MailER_NativeAdUtil.loadMediumNativeAd(nativeAdContainer, this, nativeShimmerContainer);
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
            String masterJson = rc.getString("app_ad_config");
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
                    .setDuration(160)
                    .withEndAction(() -> {
                        if (tvLoadingStatus != null) {
                            tvLoadingStatus.setText(nextText);
                            tvLoadingStatus.animate().alpha(1f).setDuration(160).start();
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
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (nativeShimmerContainer != null) {
            nativeShimmerContainer.stopShimmer();
        }
    }
}
