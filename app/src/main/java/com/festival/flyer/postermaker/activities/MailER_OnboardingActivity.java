package com.festival.flyer.postermaker.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.res.ResourcesCompat;

import androidx.cardview.widget.CardView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONObject;

import java.util.ArrayList;

public class MailER_OnboardingActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private MailER_PreferenceClass preferenceClass;
    private FirebaseAnalytics mFirebaseAnalytics;

    private int currentStep = 0;
    private String selectedPersona = "";
    private ArrayList<String> selectedInterests = new ArrayList<>();
    private ArrayList<String> selectedBackgrounds = new ArrayList<>();

    private View prog1, prog2, prog3, prog4;
    
    private ImageView ivBack;
    private ObjectAnimator pulseAnimator;

    private RelativeLayout rlOnboardingNative;
    private RelativeLayout nativeAdContainer;
    private ShimmerFrameLayout nativeShimmerContainer;

    // Screen 1: 3D Animated Hero Showcase & Multi-Poster Swiper
    private RelativeLayout stageContainer;
    private CardView cardLeft, cardRight, cardCenter;
    private ImageView ivCardLeft, ivCardRight, ivCardCenter, ivAuraGlow;
    private View pillFestival, pillTrending, pillBusiness, pillCount;
    private TextView tvPillFestival, tvPillTrending, tvPillBusiness, tvPillCount;
    private TextView tvScreen1Title, tvScreen1Subtitle;
    private HorizontalScrollView hsvCategoryTags;
    private TextView tvTagFestivals, tvTagBusiness, tvTagFitness, tvTagRealEstate, tvTagBirthday;
    private View llScreen1Text;
    private boolean isScreen1ViewsBound = false;
    private boolean isScreen1EntranceDone = false;
    private int carouselIndex = 0;
    private final ArrayList<Animator> screen1Animators = new ArrayList<>();
    private final Handler screen1Handler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;

    private final int[] carouselDrawables = {
            R.drawable.onboarding_poster_valentine,
            R.drawable.onboarding_poster_business,
            R.drawable.onboarding_poster_fitness,
            R.drawable.onboarding_poster_realestate,
            R.drawable.onboarding_poster_birthday
    };

    private final String[] carouselSubtitles = {
            "“Romantic Valentine & Festival Posters”",
            "“High-Impact Business & Marketing Ads”",
            "“Inspiring Gym, Fitness & Workout Flyers”",
            "“Luxury Real Estate & Dream Home Banners”",
            "“Joyful Birthday & Celebration Posts”"
    };

    private final String[][] carouselBadges = {
            {"💖 Valentine Special", "🔥 Trending Romance", "✨ 50% Off Today", "⭐ 10,000+ Designs"},
            {"💼 Digital Marketing", "🚀 Boost Sales", "📈 High Converting", "⭐ Top Agency Ad"},
            {"💪 Gym & Fitness", "🔥 Body Transformation", "⚡ Daily Workout", "🏆 New Year Goal"},
            {"🏡 Dream Villa", "✨ Luxury Living", "🔑 Book Now", "🌟 Prime Location"},
            {"🎂 Happy Birthday", "🎈 Big Celebration", "🎉 Special Greetings", "🎁 Gift Voucher"}
    };

    // Screen 2: Studio Customization Showcase Views & Animators
    private CardView cardCanvas;
    private ImageView ivCanvasPoster, ivAuraGlow2;
    private View viewCanvasBgOverlay;
    private View canvasTagContainer, layoutTextBox, viewSelectionBorder;
    private View handleTL, handleTR, handleBL, handleBR;
    private TextView tvCanvasTag, tvCanvasHeadline, tvCanvasSub;
    private View layoutBadge3D;
    private TextView tvBadgeText;
    private ImageView ivSparkle1, ivSparkle2;
    private View pillToolBg, pillToolStickers, pillToolFonts, pillToolEffects;
    private View dockToolBg, dockToolFonts, dockToolStickers, dockToolEffects;
    private TextView tvDockBg, tvDockFonts, tvDockStickers, tvDockEffects;
    private View llScreen2BottomArea;

    // Screen 2: Hand Pointer & Mini Tray Simulation Views
    private View layoutScreen2;
    private ImageView ivHandPointer;
    private View viewTapRipple;
    private View layoutMiniTray;
    private TextView tvMiniTrayLabel, tvChipOpt1, tvChipOpt2, tvChipOpt3;
    private View chipOpt1, chipOpt2, chipOpt3;

    // Screen 2: Floating Sticker Choices (Aju Baju)
    private View stickerChoice1, stickerChoice2, stickerChoice3, stickerChoice4;
    private TextView tvStickerChoice1, tvStickerChoice2, tvStickerChoice3, tvStickerChoice4;

    private boolean isScreen2ViewsBound = false;
    private boolean isScreen2EntranceDone = false;
    private int studioMode = 0; // 0 = BG, 1 = Fonts, 2 = Stickers, 3 = Effects
    private int studioCycleIndex = 0;
    private final ArrayList<Animator> screen2Animators = new ArrayList<>();
    private final Handler screen2Handler = new Handler(Looper.getMainLooper());
    private Runnable studioCycleRunnable;

    private final int[] bgOverlayColors = {
            Color.parseColor("#38FFD700"), // Festive Warm Gold glow
            Color.parseColor("#387B2FF7"), // Royal Purple cyber glow
            Color.parseColor("#38FF1493")  // Festive Magenta glow
    };
    private int bgOverlayIndex = 0;

    private final String[][] fontTextPresets = {
            {"SHUBH DIWALI", "SPECIAL FESTIVAL OFFER"},
            {"DIWALI MEGA SALE", "FLAT 50% DISCOUNT"},
            {"HAPPY DIWALI", "WISHES & GREETINGS"}
    };
    private int fontPresetIndex = 0;

    private final String[] badgePresets = {
            "🔥 50% OFF",
            "🪔 SHUBH",
            "🎉 SPECIAL",
            "✨ DIWALI"
    };
    private int badgePresetIndex = 0;
    private int effectsPresetIndex = 0;

    // Screen 3: Viral Social Sharing Showcase Views & Animators
    private CardView cardSharePoster;
    private ImageView ivSharePoster, ivAuraGlow3;
    private View layoutBadgeSaved, layoutBadgeWatermark;
    private TextView tvBadgeSaved;
    private View cardShareWhatsapp, bubbleWhatsappSent;
    private View cardShareInstagram, bubbleInstagramLikes;
    private ImageView ivShareSparkle1, ivShareSparkle2;
    private View llQuickShareBar;
    private View btnQuickWa, btnQuickIg, btnQuickFb, btnQuickDl;
    private TextView tvScreen3Title, tvScreen3Subtitle;
    private View llScreen3BottomArea;

    private boolean isScreen3ViewsBound = false;
    private boolean isScreen3EntranceDone = false;
    private final ArrayList<Animator> screen3Animators = new ArrayList<>();
    private final Handler screen3Handler = new Handler(Looper.getMainLooper());
    private Runnable shareCycleRunnable;
    private int shareCycleMode = 0; // 0 = WhatsApp, 1 = Instagram

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_onboarding);

        preferenceClass = new MailER_PreferenceClass(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        viewFlipper = findViewById(R.id.viewFlipper);
        prog1 = findViewById(R.id.prog1);
        prog2 = findViewById(R.id.prog2);
        prog3 = findViewById(R.id.prog3);
        prog4 = findViewById(R.id.prog4);
        
        ivBack = findViewById(R.id.ivBack);

        rlOnboardingNative = findViewById(R.id.rl_onboarding_native);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        nativeShimmerContainer = findViewById(R.id.native_shimmer_container);

        initScreen1Views();
        initScreen2Views();
        initScreen3Views();

        findViewById(R.id.btnContinue).setOnClickListener(v -> nextStep());
        
        ivBack.setOnClickListener(v -> prevStep());

        // Preload Interstitial ad for Onboarding Continue buttons
        try {
            JSONObject configObj = getOnboardingConfigJson();
            if (configObj != null) {
                String onbInterId = configObj.optString("interstitial_ad_id", "");
                if (!onbInterId.trim().isEmpty()) {
                    preferenceClass.setAdsId("OnboardingInterAdID", onbInterId.trim());
                    preferenceClass.setAdsId("interstitial_ad_id", onbInterId.trim());
                    preferenceClass.setAdsId("InterstitalAdunitID", onbInterId.trim());
                }
                boolean onboardingInterEnabled = configObj.optBoolean("is_interstitial_enabled", configObj.optBoolean("interstitial_enabled", true));
                preferenceClass.setBoolean("is_onboarding_inter_enabled", onboardingInterEnabled);
            }
        } catch (Exception ignored) {}

        ((MyApplication) getApplication()).loadInterstitialAd();

        updateUI();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void nextStep() {
        if (currentStep == 0) {
            if (selectedPersona == null || selectedPersona.isEmpty()) {
                switch (carouselIndex) {
                    case 0: selectedPersona = "Valentine & Festivals"; break;
                    case 1: selectedPersona = "Business & Ads"; break;
                    case 2: selectedPersona = "Gym & Fitness"; break;
                    case 3: selectedPersona = "Real Estate"; break;
                    case 4: selectedPersona = "Birthday & Greetings"; break;
                    default: selectedPersona = "Festivals"; break;
                }
            }
        } else if (currentStep == 1) {
            if (selectedInterests.isEmpty()) {
                selectedInterests.add("Festival");
                selectedInterests.add("Fashion");
                selectedInterests.add("Food");
            }
        } else if (currentStep == 2) {
            if (selectedBackgrounds.isEmpty()) {
                selectedBackgrounds.add("Festive");
                selectedBackgrounds.add("Modern");
                selectedBackgrounds.add("Creative");
            }
        }

        String screenKey = "onboarding_" + (currentStep + 1);
        boolean shouldShowAd = (currentStep < 3); // Default true for steps 0, 1, 2

        try {
            JSONObject configObj = getOnboardingConfigJson();
            if (configObj != null) {
                String onbInterId = configObj.optString("interstitial_ad_id", "");
                if (!onbInterId.trim().isEmpty() && preferenceClass != null) {
                    preferenceClass.setAdsId("OnboardingInterAdID", onbInterId.trim());
                    preferenceClass.setAdsId("interstitial_ad_id", onbInterId.trim());
                    preferenceClass.setAdsId("InterstitalAdunitID", onbInterId.trim());
                }

                // Global onboarding interstitial toggle check
                boolean isGlobalInterEnabled = true;
                if (configObj.has("is_interstitial_enabled")) {
                    isGlobalInterEnabled = configObj.optBoolean("is_interstitial_enabled", true);
                } else if (configObj.has("interstitial_enabled")) {
                    isGlobalInterEnabled = configObj.optBoolean("interstitial_enabled", true);
                }
                if (preferenceClass != null) {
                    preferenceClass.setBoolean("is_onboarding_inter_enabled", isGlobalInterEnabled);
                }

                if (!isGlobalInterEnabled) {
                    shouldShowAd = false;
                } else if (configObj.has(screenKey)) {
                    JSONObject screenObj = configObj.getJSONObject(screenKey);
                    if (screenObj.has("next_interstitial")) {
                        shouldShowAd = screenObj.optBoolean("next_interstitial", true);
                    } else if (screenObj.has("show_interstitial")) {
                        shouldShowAd = screenObj.optBoolean("show_interstitial", true);
                    } else if (screenObj.has("interstitial_enabled")) {
                        shouldShowAd = screenObj.optBoolean("interstitial_enabled", true);
                    }

                    // Custom Ad ID check if present in screen config
                    String customInterId = screenObj.optString("interstitial_ad_id", screenObj.optString("ad_id", ""));
                    if (!customInterId.trim().isEmpty() && preferenceClass != null) {
                        preferenceClass.setAdsId("OnboardingInterAdID", customInterId.trim());
                        preferenceClass.setAdsId("interstitial_ad_id", customInterId.trim());
                        preferenceClass.setAdsId("InterstitalAdunitID", customInterId.trim());
                    }
                }
            } else {
                // Fallback check from Remote Config booleans / SharedPreferences
                boolean isInterEnabled = getOnboardingConfigBool("is_onboarding_inter_enabled", true);
                if (!isInterEnabled) {
                    shouldShowAd = false;
                } else {
                    if (currentStep == 0) shouldShowAd = getOnboardingConfigBool("onboarding_inter_step_1", true);
                    else if (currentStep == 1) shouldShowAd = getOnboardingConfigBool("onboarding_inter_step_2", true);
                    else if (currentStep == 2) shouldShowAd = getOnboardingConfigBool("onboarding_inter_step_3", true);
                    else if (currentStep == 3) shouldShowAd = getOnboardingConfigBool("onboarding_inter_step_4", false);
                }
            }
        } catch (Exception e) {
            shouldShowAd = (currentStep < 3);
        }

        if (shouldShowAd) {
            showOnboardingInterstitialThenProceed();
        } else {
            proceedToNext();
        }
    }

    private void showOnboardingInterstitialThenProceed() {
        MyApplication.showInterstitialAdWithOutCount(this, new MailER_InterstitialAdManager.OnAdLoadInterface() {
            @Override
            public void onAdClose() {
                proceedToNext();
            }
        });
    }

    private void proceedToNext() {
        if (currentStep < 2) {
            currentStep++;
            updateUI();
        } else {
            finishOnboarding();
        }
    }

    private JSONObject getOnboardingConfigJson() {
        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterJson = MailER_PreferenceClass.getMasterConfigJson(rc);
            if (masterJson != null && !masterJson.trim().isEmpty()) {
                JSONObject root = new JSONObject(masterJson);
                if (root.has("onboarding")) {
                    return root.getJSONObject("onboarding");
                }
            }
            String jsonStr = rc.getString("onboarding_config");
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                return new JSONObject(jsonStr);
            }
            String prefStr = preferenceClass != null ? preferenceClass.getDataType("onboarding_config") : "";
            if (prefStr != null && !prefStr.trim().isEmpty()) {
                return new JSONObject(prefStr);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean getOnboardingConfigBool(String key, boolean defaultValue) {
        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            if (rc.getKeysByPrefix("").contains(key)) {
                return rc.getBoolean(key);
            }
        } catch (Exception ignored) {
        }
        return preferenceClass != null ? preferenceClass.getBoolean(key, defaultValue) : defaultValue;
    }

    private void prevStep() {
        if (currentStep > 0) {
            currentStep--;
            updateUI();
        }
    }

    private void updateOnboardingNativeAd() {
        if (rlOnboardingNative == null || nativeAdContainer == null) return;

        // Native ads only on first 3 screens (steps 0, 1, 2)
        if (currentStep >= 3) {
            rlOnboardingNative.setVisibility(View.GONE);
            return;
        }

        String screenKey1 = "screen_" + (currentStep + 1);
        String screenKey2 = "onboarding_" + (currentStep + 1);
        boolean showNative = false;
        String customNativeId = "";
        String adType = (currentStep == 1) ? "compact" : "compact";
        int customHeightDp = -1;

        try {
            JSONObject configObj = getOnboardingConfigJson();
            if (configObj != null) {
                JSONObject screenObj = null;
                if (configObj.has(screenKey1)) {
                    screenObj = configObj.getJSONObject(screenKey1);
                } else if (configObj.has(screenKey2)) {
                    screenObj = configObj.getJSONObject(screenKey2);
                }

                if (screenObj != null) {
                    JSONObject inlineAd = screenObj.optJSONObject("inline_ad");
                    if (inlineAd != null) {
                        showNative = inlineAd.optBoolean("is_show", inlineAd.optBoolean("show_ad", true));
                        customNativeId = inlineAd.optString("ad_id", inlineAd.optString("native_ad_id", ""));
                        adType = inlineAd.optString("type", inlineAd.optString("ad_type", adType));
                        customHeightDp = inlineAd.optInt("height", -1);
                    } else {
                        showNative = screenObj.optBoolean("show_native_ad", screenObj.optBoolean("show_ad", screenObj.optBoolean("is_show", false)));
                        customNativeId = screenObj.optString("native_ad_id", screenObj.optString("ad_id", ""));
                        adType = screenObj.optString("ad_type", screenObj.optString("type", adType));
                        customHeightDp = screenObj.optInt("height", -1);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (showNative) {
            rlOnboardingNative.setVisibility(View.VISIBLE);
            if (nativeAdContainer != null) {
                nativeAdContainer.removeAllViews();
            }

            boolean isCompact = "compact".equalsIgnoreCase(adType) || "small".equalsIgnoreCase(adType) || (customHeightDp > 0 && customHeightDp <= 120 && !"medium".equalsIgnoreCase(adType));
            boolean isMedium = !isCompact && ("medium".equalsIgnoreCase(adType) || "native_medium".equalsIgnoreCase(adType) || (customHeightDp >= 150));

            // Shimmer while loading
            int initialHeight = (customHeightDp > 0) ? (int) dpToPx(customHeightDp) : (int) dpToPx(isMedium ? 185 : 90);
            if (nativeShimmerContainer != null) {
                nativeShimmerContainer.setVisibility(View.VISIBLE);
                nativeShimmerContainer.startShimmer();
                ViewGroup.LayoutParams sLp = nativeShimmerContainer.getLayoutParams();
                if (sLp != null) {
                    sLp.height = initialHeight;
                    nativeShimmerContainer.setLayoutParams(sLp);
                }
                View shimmerMedia = nativeShimmerContainer.findViewById(R.id.shimmer_media_box);
                if (shimmerMedia != null) {
                    shimmerMedia.setVisibility(isMedium ? View.VISIBLE : View.GONE);
                }
            }

            // Container height: Use WRAP_CONTENT so media content is NEVER cut off
            ViewGroup.LayoutParams lp = rlOnboardingNative.getLayoutParams();
            if (lp != null) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                rlOnboardingNative.setLayoutParams(lp);
            }
            ViewGroup.LayoutParams cLp = nativeAdContainer.getLayoutParams();
            if (cLp != null) {
                cLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                nativeAdContainer.setLayoutParams(cLp);
            }

            final int targetHeightDp = customHeightDp;
            MailER_NativeAdUtil.OnAdLoadListener adListener = new MailER_NativeAdUtil.OnAdLoadListener() {
                @Override
                public void onAdLoaded() {
                    runOnUiThread(() -> {
                        if (nativeShimmerContainer != null) {
                            nativeShimmerContainer.stopShimmer();
                            nativeShimmerContainer.setVisibility(View.GONE);
                            nativeShimmerContainer.hideShimmer();
                        }
                        // Ensure layout expands naturally to WRAP_CONTENT so nothing is cut off
                        if (rlOnboardingNative != null && rlOnboardingNative.getLayoutParams() != null) {
                            rlOnboardingNative.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            rlOnboardingNative.requestLayout();
                        }
                        if (nativeAdContainer != null) {
                            if (nativeAdContainer.getLayoutParams() != null) {
                                nativeAdContainer.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                nativeAdContainer.requestLayout();
                            }
                            if (targetHeightDp > 90 && nativeAdContainer.getChildCount() > 0) {
                                View child = nativeAdContainer.getChildAt(0);
                                View mediaContainer = child.findViewById(R.id.media_container);
                                if (mediaContainer != null) {
                                    int targetMediaHeight = (int) dpToPx(targetHeightDp - 65);
                                    if (targetMediaHeight >= (int) dpToPx(120)) {
                                        mediaContainer.getLayoutParams().height = targetMediaHeight;
                                        mediaContainer.requestLayout();
                                    }
                                }
                            }
                        }
                    });
                }

                @Override
                public void onAdFailed() {
                    runOnUiThread(() -> {
                        boolean showShimmerOnFail = MailER_PreferenceClass.isNativeShimmerOnFail(MailER_OnboardingActivity.this);
                        if (showShimmerOnFail) {
                            if (nativeShimmerContainer != null) {
                                nativeShimmerContainer.startShimmer();
                                nativeShimmerContainer.setVisibility(View.VISIBLE);
                            }
                            if (rlOnboardingNative != null) {
                                rlOnboardingNative.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (nativeShimmerContainer != null) {
                                nativeShimmerContainer.stopShimmer();
                                nativeShimmerContainer.setVisibility(View.GONE);
                                nativeShimmerContainer.hideShimmer();
                            }
                            if (rlOnboardingNative != null) {
                                rlOnboardingNative.setVisibility(View.GONE);
                            }
                            if (nativeAdContainer != null) {
                                nativeAdContainer.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            };

            if ("banner".equalsIgnoreCase(adType)) {
                MailER_LoadAds.loadAdmobBannerAd(this, nativeAdContainer, nativeShimmerContainer, customNativeId.isEmpty() ? null : customNativeId);
            } else if (isMedium) {
                MailER_NativeAdUtil.loadMediumNativeAd(nativeAdContainer, this, nativeShimmerContainer, customNativeId.isEmpty() ? null : customNativeId, adListener);
            } else {
                MailER_NativeAdUtil.loadCompactNativeAd(nativeAdContainer, this, nativeShimmerContainer, customNativeId.isEmpty() ? null : customNativeId, adListener);
            }
        } else {
            if (nativeShimmerContainer != null) {
                nativeShimmerContainer.stopShimmer();
                nativeShimmerContainer.setVisibility(View.GONE);
                nativeShimmerContainer.hideShimmer();
            }
            rlOnboardingNative.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        viewFlipper.setDisplayedChild(currentStep);

        prog1.setBackgroundColor(currentStep >= 0 ? Color.parseColor("#7B2FF7") : Color.parseColor("#E0E0E0"));
        prog2.setBackgroundColor(currentStep >= 1 ? Color.parseColor("#7B2FF7") : Color.parseColor("#E0E0E0"));
        prog3.setBackgroundColor(currentStep >= 2 ? Color.parseColor("#7B2FF7") : Color.parseColor("#E0E0E0"));
        if (prog4 != null) {
            prog4.setVisibility(View.GONE);
        }

        ivBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);

        updateOnboardingNativeAd();

        androidx.appcompat.widget.AppCompatButton btnContinue = findViewById(R.id.btnContinue);
        
        findViewById(R.id.rootLayout).setBackgroundColor(Color.parseColor("#F9FAFB"));
        findViewById(R.id.topBar).setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.VISIBLE);
        btnContinue.setAlpha(1f);
        btnContinue.setTranslationY(0f);

        if (currentStep == 2) {
            btnContinue.setText("Start Creating →");
            
            // Subtle pulse on last step button to encourage start
            if (pulseAnimator == null) {
                PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.04f);
                PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.04f);
                pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(btnContinue, scaleX, scaleY);
                pulseAnimator.setDuration(900);
                pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
                pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            }
            if (!pulseAnimator.isRunning()) {
                pulseAnimator.start();
            }
        } else {
            btnContinue.setText("Continue →");
            
            // Stop pulse animation
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
                btnContinue.setScaleX(1f);
                btnContinue.setScaleY(1f);
            }
        }

        if (currentStep == 0) {
            stopScreen2Animations();
            stopScreen3Animations();
            if (!isScreen1EntranceDone) {
                isScreen1EntranceDone = true;
                startScreen1Entrance();
            } else {
                startContinuousFloating();
                startAutoCarousel();
            }
        } else if (currentStep == 1) {
            stopScreen1Animations();
            stopScreen3Animations();
            if (!isScreen2EntranceDone) {
                isScreen2EntranceDone = true;
                startScreen2Entrance();
            } else {
                startScreen2ContinuousFloating();
                startStudioCycle();
            }
        } else if (currentStep == 2) {
            stopScreen1Animations();
            stopScreen2Animations();
            if (!isScreen3EntranceDone) {
                isScreen3EntranceDone = true;
                startScreen3Entrance();
            } else {
                startScreen3ContinuousFloating();
                startScreen3ShareCycle();
            }
        } else {
            stopScreen1Animations();
            stopScreen2Animations();
            stopScreen3Animations();
        }
    }

    private void initScreen1Views() {
        stageContainer = findViewById(R.id.stageContainer);
        cardLeft = findViewById(R.id.cardLeft);
        cardRight = findViewById(R.id.cardRight);
        cardCenter = findViewById(R.id.cardCenter);
        ivCardLeft = findViewById(R.id.ivCardLeft);
        ivCardRight = findViewById(R.id.ivCardRight);
        ivCardCenter = findViewById(R.id.ivCardCenter);
        ivAuraGlow = findViewById(R.id.ivAuraGlow);

        pillFestival = findViewById(R.id.pillFestival);
        pillTrending = findViewById(R.id.pillTrending);
        pillBusiness = findViewById(R.id.pillBusiness);
        pillCount = findViewById(R.id.pillCount);

        tvPillFestival = findViewById(R.id.tvPillFestival);
        tvPillTrending = findViewById(R.id.tvPillTrending);
        tvPillBusiness = findViewById(R.id.tvPillBusiness);
        tvPillCount = findViewById(R.id.tvPillCount);

        tvScreen1Title = findViewById(R.id.tvScreen1Title);
        tvScreen1Subtitle = findViewById(R.id.tvScreen1Subtitle);
        hsvCategoryTags = findViewById(R.id.hsvCategoryTags);
        tvTagFestivals = findViewById(R.id.tvTagFestivals);
        tvTagBusiness = findViewById(R.id.tvTagBusiness);
        tvTagFitness = findViewById(R.id.tvTagFitness);
        tvTagRealEstate = findViewById(R.id.tvTagRealEstate);
        tvTagBirthday = findViewById(R.id.tvTagBirthday);
        llScreen1Text = findViewById(R.id.llScreen1Text);

        isScreen1ViewsBound = (cardCenter != null);

        if (cardLeft != null) {
            cardLeft.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(carouselIndex - 1, -1);
            });
        }
        if (cardRight != null) {
            cardRight.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(carouselIndex + 1, 1);
            });
        }
        if (tvTagFestivals != null) {
            tvTagFestivals.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(0, 0 >= carouselIndex ? 1 : -1);
            });
        }
        if (tvTagBusiness != null) {
            tvTagBusiness.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(1, 1 >= carouselIndex ? 1 : -1);
            });
        }
        if (tvTagFitness != null) {
            tvTagFitness.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(2, 2 >= carouselIndex ? 1 : -1);
            });
        }
        if (tvTagRealEstate != null) {
            tvTagRealEstate.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(3, 3 >= carouselIndex ? 1 : -1);
            });
        }
        if (tvTagBirthday != null) {
            tvTagBirthday.setOnClickListener(v -> {
                resetCarouselTimer();
                shuffleCarousel(4, 4 >= carouselIndex ? 1 : -1);
            });
        }

        // Gesture swipe detector on stageContainer
        if (stageContainer != null) {
            GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                private static final int SWIPE_THRESHOLD = 50;
                private static final int SWIPE_VELOCITY_THRESHOLD = 50;

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (e1 == null || e2 == null) return false;
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            resetCarouselTimer();
                            if (diffX > 0) {
                                shuffleCarousel(carouselIndex - 1, -1);
                            } else {
                                shuffleCarousel(carouselIndex + 1, 1);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });

            stageContainer.setOnTouchListener((v, event) -> {
                boolean handled = gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP && !handled) {
                    v.performClick();
                }
                return handled;
            });
        }
    }

    private void startScreen1Entrance() {
        if (cardCenter == null) return;

        // Reset views for entrance
        cardCenter.setAlpha(0f);
        cardCenter.setScaleX(0.7f);
        cardCenter.setScaleY(0.7f);
        cardCenter.setTranslationY(140f);

        if (cardLeft != null) {
            cardLeft.setAlpha(0f);
            cardLeft.setScaleX(0.80f);
            cardLeft.setScaleY(0.80f);
            cardLeft.setTranslationX(-dpToPx(130));
            cardLeft.setTranslationY(90f);
        }

        if (cardRight != null) {
            cardRight.setAlpha(0f);
            cardRight.setScaleX(0.80f);
            cardRight.setScaleY(0.80f);
            cardRight.setTranslationX(dpToPx(130));
            cardRight.setTranslationY(90f);
        }

        if (ivAuraGlow != null) {
            ivAuraGlow.setAlpha(0f);
            ivAuraGlow.setScaleX(0.7f);
            ivAuraGlow.setScaleY(0.7f);
            ivAuraGlow.animate()
                    .alpha(0.85f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(900)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (pillFestival != null) { pillFestival.setScaleX(0f); pillFestival.setScaleY(0f); }
        if (pillTrending != null) { pillTrending.setScaleX(0f); pillTrending.setScaleY(0f); }
        if (pillBusiness != null) { pillBusiness.setScaleX(0f); pillBusiness.setScaleY(0f); }
        if (pillCount != null) { pillCount.setScaleX(0f); pillCount.setScaleY(0f); }

        if (llScreen1Text != null) {
            llScreen1Text.setAlpha(0f);
            llScreen1Text.setTranslationY(40f);
            llScreen1Text.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(250)
                    .setDuration(600)
                    .start();
        }

        // Drop Center Hero Card
        cardCenter.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0f)
                .setDuration(750)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withEndAction(this::startContinuousFloating)
                .start();

        // Slide Left Card
        if (cardLeft != null) {
            cardLeft.animate()
                    .alpha(1f)
                    .translationX(-dpToPx(75))
                    .translationY(0f)
                    .rotation(-12f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setStartDelay(100)
                    .setDuration(750)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
        }

        // Slide Right Card
        if (cardRight != null) {
            cardRight.animate()
                    .alpha(1f)
                    .translationX(dpToPx(75))
                    .translationY(0f)
                    .rotation(10f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setStartDelay(160)
                    .setDuration(750)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
        }

        // Pop in badges
        popInView(pillFestival, 350);
        popInView(pillTrending, 480);
        popInView(pillBusiness, 600);
        popInView(pillCount, 720);

        updateCategoryHighlight(0);
        startAutoCarousel();
    }

    private void popInView(View v, long delay) {
        if (v == null) return;
        v.postDelayed(() -> {
            v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator(1.8f))
                    .start();
        }, delay);
    }

    private void startContinuousFloating() {
        stopFloatingAnimators();

        if (cardCenter == null) return;

        float bobCenter = dpToPx(8);
        ObjectAnimator animCenterY = ObjectAnimator.ofFloat(cardCenter, View.TRANSLATION_Y, -bobCenter, bobCenter);
        animCenterY.setDuration(2700);
        animCenterY.setRepeatMode(ValueAnimator.REVERSE);
        animCenterY.setRepeatCount(ValueAnimator.INFINITE);
        animCenterY.setInterpolator(new AccelerateDecelerateInterpolator());
        animCenterY.start();
        screen1Animators.add(animCenterY);

        // Center 3D rotation breathing
        ObjectAnimator animCenterRot = ObjectAnimator.ofFloat(cardCenter, View.ROTATION, -1.2f, 1.2f);
        animCenterRot.setDuration(3600);
        animCenterRot.setRepeatMode(ValueAnimator.REVERSE);
        animCenterRot.setRepeatCount(ValueAnimator.INFINITE);
        animCenterRot.setInterpolator(new AccelerateDecelerateInterpolator());
        animCenterRot.start();
        screen1Animators.add(animCenterRot);

        if (cardLeft != null) {
            float bobLeft = dpToPx(6);
            ObjectAnimator animLeftY = ObjectAnimator.ofFloat(cardLeft, View.TRANSLATION_Y, bobLeft, -bobLeft);
            animLeftY.setDuration(3200);
            animLeftY.setRepeatMode(ValueAnimator.REVERSE);
            animLeftY.setRepeatCount(ValueAnimator.INFINITE);
            animLeftY.setInterpolator(new AccelerateDecelerateInterpolator());
            animLeftY.start();
            screen1Animators.add(animLeftY);

            // Left tilt breathing
            ObjectAnimator animLeftRot = ObjectAnimator.ofFloat(cardLeft, View.ROTATION, -13.5f, -10.5f);
            animLeftRot.setDuration(4000);
            animLeftRot.setRepeatMode(ValueAnimator.REVERSE);
            animLeftRot.setRepeatCount(ValueAnimator.INFINITE);
            animLeftRot.setInterpolator(new AccelerateDecelerateInterpolator());
            animLeftRot.start();
            screen1Animators.add(animLeftRot);
        }

        if (cardRight != null) {
            float bobRight = dpToPx(7);
            ObjectAnimator animRightY = ObjectAnimator.ofFloat(cardRight, View.TRANSLATION_Y, -bobRight, bobRight);
            animRightY.setDuration(2900);
            animRightY.setRepeatMode(ValueAnimator.REVERSE);
            animRightY.setRepeatCount(ValueAnimator.INFINITE);
            animRightY.setInterpolator(new AccelerateDecelerateInterpolator());
            animRightY.start();
            screen1Animators.add(animRightY);

            // Right tilt breathing
            ObjectAnimator animRightRot = ObjectAnimator.ofFloat(cardRight, View.ROTATION, 8.5f, 11.5f);
            animRightRot.setDuration(3500);
            animRightRot.setRepeatMode(ValueAnimator.REVERSE);
            animRightRot.setRepeatCount(ValueAnimator.INFINITE);
            animRightRot.setInterpolator(new AccelerateDecelerateInterpolator());
            animRightRot.start();
            screen1Animators.add(animRightRot);
        }

        if (ivAuraGlow != null) {
            PropertyValuesHolder glowX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.94f, 1.06f);
            PropertyValuesHolder glowY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.94f, 1.06f);
            PropertyValuesHolder glowA = PropertyValuesHolder.ofFloat(View.ALPHA, 0.65f, 0.92f);
            ObjectAnimator animGlow = ObjectAnimator.ofPropertyValuesHolder(ivAuraGlow, glowX, glowY, glowA);
            animGlow.setDuration(3800);
            animGlow.setRepeatMode(ValueAnimator.REVERSE);
            animGlow.setRepeatCount(ValueAnimator.INFINITE);
            animGlow.setInterpolator(new AccelerateDecelerateInterpolator());
            animGlow.start();
            screen1Animators.add(animGlow);
        }

        if (pillFestival != null) {
            ObjectAnimator animPill1 = ObjectAnimator.ofFloat(pillFestival, View.TRANSLATION_Y, -bobCenter * 0.75f, bobCenter * 0.75f);
            animPill1.setDuration(2400);
            animPill1.setRepeatMode(ValueAnimator.REVERSE);
            animPill1.setRepeatCount(ValueAnimator.INFINITE);
            animPill1.setInterpolator(new AccelerateDecelerateInterpolator());
            animPill1.start();
            screen1Animators.add(animPill1);
        }

        if (pillTrending != null) {
            float bobLeft = dpToPx(6);
            ObjectAnimator animPill2 = ObjectAnimator.ofFloat(pillTrending, View.TRANSLATION_Y, bobLeft * 0.8f, -bobLeft * 0.8f);
            animPill2.setDuration(3000);
            animPill2.setRepeatMode(ValueAnimator.REVERSE);
            animPill2.setRepeatCount(ValueAnimator.INFINITE);
            animPill2.setInterpolator(new AccelerateDecelerateInterpolator());
            animPill2.start();
            screen1Animators.add(animPill2);
        }

        if (pillBusiness != null) {
            ObjectAnimator animPill3 = ObjectAnimator.ofFloat(pillBusiness, View.TRANSLATION_Y, -bobCenter * 0.7f, bobCenter * 0.7f);
            animPill3.setDuration(2700);
            animPill3.setRepeatMode(ValueAnimator.REVERSE);
            animPill3.setRepeatCount(ValueAnimator.INFINITE);
            animPill3.setInterpolator(new AccelerateDecelerateInterpolator());
            animPill3.start();
            screen1Animators.add(animPill3);
        }

        if (pillCount != null) {
            float bobRight = dpToPx(7);
            ObjectAnimator animPill4 = ObjectAnimator.ofFloat(pillCount, View.TRANSLATION_Y, -bobRight * 0.7f, bobRight * 0.7f);
            animPill4.setDuration(3300);
            animPill4.setRepeatMode(ValueAnimator.REVERSE);
            animPill4.setRepeatCount(ValueAnimator.INFINITE);
            animPill4.setInterpolator(new AccelerateDecelerateInterpolator());
            animPill4.start();
            screen1Animators.add(animPill4);
        }
    }

    private void shuffleCarousel(int targetIndex) {
        shuffleCarousel(targetIndex, targetIndex >= carouselIndex ? 1 : -1);
    }

    private void shuffleCarousel(int targetIndex, int direction) {
        if (cardCenter == null || ivCardCenter == null) return;
        int count = carouselDrawables.length;
        carouselIndex = (targetIndex % count + count) % count;

        stopFloatingAnimators();

        final float flipAngle = (direction >= 0) ? 28f : -28f;
        final float slideOffset = (direction >= 0) ? -dpToPx(40) : dpToPx(40);

        // 1. Center Hero Card: Swoop out with 3D Y-axis flip
        cardCenter.animate()
                .rotationY(flipAngle)
                .translationX(slideOffset)
                .scaleX(0.88f)
                .scaleY(0.88f)
                .alpha(0.6f)
                .setDuration(220)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    int centerRes = carouselDrawables[carouselIndex];
                    int leftRes = carouselDrawables[(carouselIndex - 1 + count) % count];
                    int rightRes = carouselDrawables[(carouselIndex + 1) % count];

                    ivCardCenter.setImageResource(centerRes);
                    if (ivCardLeft != null) ivCardLeft.setImageResource(leftRes);
                    if (ivCardRight != null) ivCardRight.setImageResource(rightRes);

                    cardCenter.setRotationY(-flipAngle);
                    cardCenter.setTranslationX(-slideOffset);
                    cardCenter.animate()
                            .rotationY(0f)
                            .translationX(0f)
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1.0f)
                            .setDuration(320)
                            .setInterpolator(new OvershootInterpolator(1.4f))
                            .withEndAction(this::startContinuousFloating)
                            .start();
                }).start();

        // 2. Left Card: Multi-directional swoop
        if (cardLeft != null) {
            float startY = (direction >= 0) ? -dpToPx(35) : dpToPx(35);
            cardLeft.animate()
                    .translationX(-dpToPx(130))
                    .translationY(startY)
                    .rotation(-22f)
                    .scaleX(0.70f)
                    .scaleY(0.70f)
                    .alpha(0.4f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        cardLeft.animate()
                                .translationX(-dpToPx(75))
                                .translationY(0f)
                                .rotation(-12f)
                                .scaleX(0.85f)
                                .scaleY(0.85f)
                                .alpha(1.0f)
                                .setDuration(320)
                                .setInterpolator(new DecelerateInterpolator(1.5f))
                                .start();
                    }).start();
        }

        // 3. Right Card: Multi-directional swoop
        if (cardRight != null) {
            float startY = (direction >= 0) ? dpToPx(35) : -dpToPx(35);
            cardRight.animate()
                    .translationX(dpToPx(130))
                    .translationY(startY)
                    .rotation(20f)
                    .scaleX(0.70f)
                    .scaleY(0.70f)
                    .alpha(0.4f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        cardRight.animate()
                                .translationX(dpToPx(75))
                                .translationY(0f)
                                .rotation(10f)
                                .scaleX(0.85f)
                                .scaleY(0.85f)
                                .alpha(1.0f)
                                .setDuration(320)
                                .setInterpolator(new DecelerateInterpolator(1.5f))
                                .start();
                    }).start();
        }

        // 4. Update the 4 floating badges with pop burst animation
        View[] pills = {pillFestival, pillTrending, pillBusiness, pillCount};
        TextView[] pillTexts = {tvPillFestival, tvPillTrending, tvPillBusiness, tvPillCount};
        for (int i = 0; i < pills.length; i++) {
            final View pill = pills[i];
            final TextView tv = pillTexts[i];
            final String newText = carouselBadges[carouselIndex][i];
            final long delay = i * 45L;
            if (pill != null) {
                pill.animate()
                        .scaleX(0.65f)
                        .scaleY(0.65f)
                        .alpha(0.35f)
                        .setDuration(160)
                        .withEndAction(() -> {
                            if (tv != null) tv.setText(newText);
                            pill.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1.0f)
                                    .setStartDelay(delay)
                                    .setDuration(260)
                                    .setInterpolator(new OvershootInterpolator(1.8f))
                                    .start();
                        }).start();
            }
        }

        // 5. Subtitle transition
        if (tvScreen1Subtitle != null) {
            tvScreen1Subtitle.animate().alpha(0f).translationY(-8f).setDuration(140)
                    .withEndAction(() -> {
                        tvScreen1Subtitle.setText(carouselSubtitles[carouselIndex]);
                        tvScreen1Subtitle.setTranslationY(8f);
                        tvScreen1Subtitle.animate().alpha(1f).translationY(0f).setDuration(220).start();
                    }).start();
        }

        // 6. Category chips highlight and auto-scroll
        updateCategoryHighlight(carouselIndex);
    }

    private void updateCategoryHighlight(int index) {
        TextView[] chips = {tvTagFestivals, tvTagBusiness, tvTagFitness, tvTagRealEstate, tvTagBirthday};
        int activeBg = R.drawable.spawner_bg_chip_purple_gradient;
        int inactiveBg = R.drawable.spawner_bg_chip_grey;

        for (int i = 0; i < chips.length; i++) {
            TextView chip = chips[i];
            if (chip == null) continue;
            boolean isActive = (i == index);
            if (isActive) {
                chip.setBackgroundResource(activeBg);
                chip.setTextColor(Color.WHITE);
                chip.setTypeface(null, Typeface.BOLD);
                chip.animate().scaleX(1.08f).scaleY(1.08f).setDuration(180)
                        .withEndAction(() -> chip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(160).start())
                        .start();

                // Smooth scroll to center this chip
                if (hsvCategoryTags != null) {
                    final TextView activeView = chip;
                    hsvCategoryTags.post(() -> {
                        int scrollX = activeView.getLeft() - (hsvCategoryTags.getWidth() - activeView.getWidth()) / 2;
                        hsvCategoryTags.smoothScrollTo(Math.max(0, scrollX), 0);
                    });
                }
            } else {
                chip.setBackgroundResource(inactiveBg);
                chip.setTextColor(Color.parseColor("#6B7280"));
                chip.setTypeface(null, Typeface.NORMAL);
                chip.setScaleX(1.0f);
                chip.setScaleY(1.0f);
            }
        }
    }

    private void startAutoCarousel() {
        stopAutoCarousel();
        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentStep == 0) {
                    shuffleCarousel(carouselIndex + 1);
                    screen1Handler.postDelayed(this, 3500);
                }
            }
        };
        screen1Handler.postDelayed(carouselRunnable, 3500);
    }

    private void stopAutoCarousel() {
        if (carouselRunnable != null) {
            screen1Handler.removeCallbacks(carouselRunnable);
            carouselRunnable = null;
        }
    }

    private void resetCarouselTimer() {
        startAutoCarousel();
    }

    private void stopScreen1Animations() {
        stopAutoCarousel();
        stopFloatingAnimators();
    }

    private void stopFloatingAnimators() {
        for (Animator anim : screen1Animators) {
            if (anim != null && anim.isRunning()) {
                anim.cancel();
            }
        }
        screen1Animators.clear();
    }

    private void finishOnboarding() {
        preferenceClass.setRateSubmited("isOnboardingDone", true);
        preferenceClass.setDataType("user_persona", selectedPersona);
        preferenceClass.setDataType("user_interests", android.text.TextUtils.join(",", selectedInterests));
        preferenceClass.setDataType("user_backgrounds", android.text.TextUtils.join(",", selectedBackgrounds));

        // Send to Firebase Analytics
        String personaSafe = selectedPersona.length() > 24 ? selectedPersona.substring(0, 24) : selectedPersona;
        mFirebaseAnalytics.setUserProperty("user_persona", personaSafe);
        
        String interestsStr = android.text.TextUtils.join(",", selectedInterests);
        if (interestsStr.length() > 36) interestsStr = interestsStr.substring(0, 36);
        mFirebaseAnalytics.setUserProperty("user_interests", interestsStr);

        String bgStr = android.text.TextUtils.join(",", selectedBackgrounds);
        if (bgStr.length() > 36) bgStr = bgStr.substring(0, 36);
        mFirebaseAnalytics.setUserProperty("user_backgrounds", bgStr);

        Bundle bundle = new Bundle();
        bundle.putString("persona", personaSafe);
        bundle.putString("interests", interestsStr);
        bundle.putString("backgrounds", bgStr);
        mFirebaseAnalytics.logEvent("onboarding_complete", bundle);

        // Loading Screen Handling after Onboarding via Remote Config
        boolean isLoadingScreenEnabled = true;
        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterJson = MailER_PreferenceClass.getMasterConfigJson(rc);
            if (masterJson != null && !masterJson.trim().isEmpty()) {
                JSONObject root = new JSONObject(masterJson);
                if (root.has("loading_screen")) {
                    JSONObject loadObj = root.getJSONObject("loading_screen");
                    isLoadingScreenEnabled = loadObj.optBoolean("enabled", true) && loadObj.optBoolean("show_after_onboarding", true);
                }
            }
        } catch (Exception ignored) {
        }
        if (preferenceClass != null) {
            boolean enabled = preferenceClass.getBoolean("loading_screen_enabled", true);
            boolean afterOnboarding = preferenceClass.getBoolean("loading_screen_show_after_onboarding", true);
            if (!enabled || !afterOnboarding) {
                isLoadingScreenEnabled = false;
            }
        }

        if (isLoadingScreenEnabled) {
            Intent intent = new Intent(this, MailER_AppLoadingActivity.class);
            startActivity(intent);
            finish();
        } else {
            navigateToMain();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MailER_PosterMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initScreen2Views() {
        cardCanvas = findViewById(R.id.cardCanvas);
        ivCanvasPoster = findViewById(R.id.ivCanvasPoster);
        viewCanvasBgOverlay = findViewById(R.id.viewCanvasBgOverlay);
        ivAuraGlow2 = findViewById(R.id.ivAuraGlow2);
        canvasTagContainer = findViewById(R.id.canvasTagContainer);
        tvCanvasTag = findViewById(R.id.tvCanvasTag);
        layoutTextBox = findViewById(R.id.layoutTextBox);
        viewSelectionBorder = findViewById(R.id.viewSelectionBorder);
        handleTL = findViewById(R.id.handleTL);
        handleTR = findViewById(R.id.handleTR);
        handleBL = findViewById(R.id.handleBL);
        handleBR = findViewById(R.id.handleBR);
        tvCanvasHeadline = findViewById(R.id.tvCanvasHeadline);
        tvCanvasSub = findViewById(R.id.tvCanvasSub);
        layoutBadge3D = findViewById(R.id.layoutBadge3D);
        tvBadgeText = findViewById(R.id.tvBadgeText);
        ivSparkle1 = findViewById(R.id.ivSparkle1);
        ivSparkle2 = findViewById(R.id.ivSparkle2);

        pillToolBg = findViewById(R.id.pillToolBg);
        pillToolStickers = findViewById(R.id.pillToolStickers);
        pillToolFonts = findViewById(R.id.pillToolFonts);
        pillToolEffects = findViewById(R.id.pillToolEffects);

        dockToolBg = findViewById(R.id.dockToolBg);
        dockToolFonts = findViewById(R.id.dockToolFonts);
        dockToolStickers = findViewById(R.id.dockToolStickers);
        dockToolEffects = findViewById(R.id.dockToolEffects);

        tvDockBg = findViewById(R.id.tvDockBg);
        tvDockFonts = findViewById(R.id.tvDockFonts);
        tvDockStickers = findViewById(R.id.tvDockStickers);
        tvDockEffects = findViewById(R.id.tvDockEffects);

        llScreen2BottomArea = findViewById(R.id.llScreen2BottomArea);

        // Hand pointer and mini tray simulator
        layoutScreen2 = findViewById(R.id.layoutScreen2);
        ivHandPointer = findViewById(R.id.ivHandPointer);
        viewTapRipple = findViewById(R.id.viewTapRipple);
        layoutMiniTray = findViewById(R.id.layoutMiniTray);
        tvMiniTrayLabel = findViewById(R.id.tvMiniTrayLabel);
        chipOpt1 = findViewById(R.id.chipOpt1);
        chipOpt2 = findViewById(R.id.chipOpt2);
        chipOpt3 = findViewById(R.id.chipOpt3);
        tvChipOpt1 = findViewById(R.id.tvChipOpt1);
        tvChipOpt2 = findViewById(R.id.tvChipOpt2);
        tvChipOpt3 = findViewById(R.id.tvChipOpt3);

        stickerChoice1 = findViewById(R.id.stickerChoice1);
        stickerChoice2 = findViewById(R.id.stickerChoice2);
        stickerChoice3 = findViewById(R.id.stickerChoice3);
        stickerChoice4 = findViewById(R.id.stickerChoice4);
        tvStickerChoice1 = findViewById(R.id.tvStickerChoice1);
        tvStickerChoice2 = findViewById(R.id.tvStickerChoice2);
        tvStickerChoice3 = findViewById(R.id.tvStickerChoice3);
        tvStickerChoice4 = findViewById(R.id.tvStickerChoice4);

        if (stickerChoice1 != null) stickerChoice1.setOnClickListener(v -> selectFloatingSticker(0));
        if (stickerChoice2 != null) stickerChoice2.setOnClickListener(v -> selectFloatingSticker(1));
        if (stickerChoice3 != null) stickerChoice3.setOnClickListener(v -> selectFloatingSticker(2));
        if (stickerChoice4 != null) stickerChoice4.setOnClickListener(v -> selectFloatingSticker(3));

        isScreen2ViewsBound = (cardCanvas != null);

        // Click listeners for dock items and floating tool pills
        if (dockToolBg != null) dockToolBg.setOnClickListener(v -> onDockToolClicked(0));
        if (pillToolBg != null) pillToolBg.setOnClickListener(v -> onDockToolClicked(0));

        if (dockToolFonts != null) dockToolFonts.setOnClickListener(v -> onDockToolClicked(1));
        if (pillToolFonts != null) pillToolFonts.setOnClickListener(v -> onDockToolClicked(1));

        if (dockToolStickers != null) dockToolStickers.setOnClickListener(v -> onDockToolClicked(2));
        if (pillToolStickers != null) pillToolStickers.setOnClickListener(v -> onDockToolClicked(2));

        if (dockToolEffects != null) dockToolEffects.setOnClickListener(v -> onDockToolClicked(3));
        if (pillToolEffects != null) pillToolEffects.setOnClickListener(v -> onDockToolClicked(3));

        if (chipOpt1 != null) chipOpt1.setOnClickListener(v -> onChipOptionClicked(0));
        if (chipOpt2 != null) chipOpt2.setOnClickListener(v -> onChipOptionClicked(1));
        if (chipOpt3 != null) chipOpt3.setOnClickListener(v -> onChipOptionClicked(2));

        if (cardCanvas != null) cardCanvas.setOnClickListener(v -> onDockToolClicked((studioMode + 1) % 4));
    }

    private void onDockToolClicked(int mode) {
        resetStudioCycleTimer();
        studioMode = mode;
        studioCycleIndex = (mode + 1) % 4;
        setStudioDockActive(mode);
        updateMiniTrayForMode(mode, 0);
        if (mode == 2) {
            showFloatingStickerChoices();
        } else {
            hideFloatingStickerChoices();
        }
        applyOptionForMode(mode, 0);
        highlightFloatingPill(mode);
    }

    private void onChipOptionClicked(int chipIndex) {
        resetStudioCycleTimer();
        applyOptionForMode(studioMode, chipIndex);
    }

    private void triggerStudioAction(int mode) {
        onDockToolClicked(mode);
    }

    private void performTapAt(View targetView, Runnable onTapped) {
        if (targetView == null || layoutScreen2 == null || ivHandPointer == null || viewTapRipple == null) {
            if (onTapped != null) onTapped.run();
            return;
        }

        int[] rootLoc = new int[2];
        layoutScreen2.getLocationInWindow(rootLoc);

        int[] targetLoc = new int[2];
        targetView.getLocationInWindow(targetLoc);

        float targetCenterX = (targetLoc[0] - rootLoc[0]) + (targetView.getWidth() / 2f);
        float targetCenterY = (targetLoc[1] - rootLoc[1]) + (targetView.getHeight() / 2f);

        float ptrW = ivHandPointer.getWidth() > 0 ? ivHandPointer.getWidth() : dpToPx(38);
        float ptrH = ivHandPointer.getHeight() > 0 ? ivHandPointer.getHeight() : dpToPx(38);
        float ripW = viewTapRipple.getWidth() > 0 ? viewTapRipple.getWidth() : dpToPx(32);
        float ripH = viewTapRipple.getHeight() > 0 ? viewTapRipple.getHeight() : dpToPx(32);

        // Position fingertip on button center
        float pointerX = targetCenterX - (ptrW * 0.54f);
        float pointerY = targetCenterY - (ptrH * 0.06f);

        float rippleX = targetCenterX - (ripW / 2f);
        float rippleY = targetCenterY - (ripH / 2f);

        ivHandPointer.setVisibility(View.VISIBLE);
        ivHandPointer.animate()
                .x(pointerX)
                .y(pointerY)
                .alpha(1f)
                .setDuration(360)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    // Tap down
                    ivHandPointer.animate()
                            .scaleX(0.82f)
                            .scaleY(0.82f)
                            .setDuration(110)
                            .withEndAction(() -> {
                                // Trigger ripple
                                viewTapRipple.setX(rippleX);
                                viewTapRipple.setY(rippleY);
                                viewTapRipple.setScaleX(0.2f);
                                viewTapRipple.setScaleY(0.2f);
                                viewTapRipple.setAlpha(0.9f);
                                viewTapRipple.setVisibility(View.VISIBLE);
                                viewTapRipple.animate()
                                        .scaleX(1.4f)
                                        .scaleY(1.4f)
                                        .alpha(0f)
                                        .setDuration(320)
                                        .withEndAction(() -> viewTapRipple.setVisibility(View.INVISIBLE))
                                        .start();

                                // Subtle target bounce
                                targetView.animate()
                                        .scaleX(0.92f)
                                        .scaleY(0.92f)
                                        .setDuration(90)
                                        .withEndAction(() -> targetView.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                                        .start();

                                // Release tap
                                ivHandPointer.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(130)
                                        .start();

                                if (onTapped != null) {
                                    onTapped.run();
                                }
                            })
                            .start();
                })
                .start();
    }

    private void highlightFloatingPill(int mode) {
        View targetPill = null;
        switch (mode) {
            case 0: targetPill = pillToolBg; break;
            case 1: targetPill = pillToolFonts; break;
            case 2: targetPill = pillToolStickers; break;
            case 3: targetPill = pillToolEffects; break;
        }
        if (targetPill != null) {
            final View pill = targetPill;
            pill.animate()
                    .scaleX(1.22f).scaleY(1.22f)
                    .setDuration(180)
                    .withEndAction(() -> pill.animate().scaleX(1.0f).scaleY(1.0f).setDuration(180).start())
                    .start();
        }
    }

    private void runGuidedHandDemo(int mode) {
        if (layoutScreen2 == null || ivHandPointer == null || currentStep != 1) return;

        View targetDock = null;
        switch (mode) {
            case 0: targetDock = dockToolBg; break;
            case 1: targetDock = dockToolFonts; break;
            case 2: targetDock = dockToolStickers; break;
            case 3: targetDock = dockToolEffects; break;
        }

        if (targetDock == null) return;

        final View finalDock = targetDock;
        performTapAt(finalDock, () -> {
            if (currentStep != 1) return;
            setStudioDockActive(mode);

            // Select an option to showcase
            int optIndex;
            if (mode == 0) {
                bgOverlayIndex = (bgOverlayIndex + 1) % bgOverlayColors.length;
                optIndex = bgOverlayIndex;
            } else if (mode == 1) {
                fontPresetIndex = (fontPresetIndex + 1) % fontTextPresets.length;
                optIndex = fontPresetIndex;
            } else if (mode == 2) {
                badgePresetIndex = (badgePresetIndex + 1) % badgePresets.length;
                optIndex = badgePresetIndex;
            } else {
                effectsPresetIndex = (effectsPresetIndex + 1) % 3;
                optIndex = effectsPresetIndex;
            }

            if (layoutMiniTray != null) {
                updateMiniTrayForMode(mode, optIndex);
                layoutMiniTray.animate().scaleX(1.06f).scaleY(1.06f).setDuration(150)
                        .withEndAction(() -> layoutMiniTray.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                        .start();
            }

            highlightFloatingPill(mode);

            if (mode == 2) {
                // Show floating sticker options around the screen (Aju Baju)
                showFloatingStickerChoices();

                View[] choices = {stickerChoice1, stickerChoice2, stickerChoice3, stickerChoice4};
                View targetSticker = choices[optIndex % choices.length];
                if (targetSticker == null) targetSticker = chipOpt1;

                final View finalSticker = targetSticker;
                final int finalOptIndex = optIndex;
                screen2Handler.postDelayed(() -> {
                    if (currentStep != 1) return;
                    performTapAt(finalSticker, () -> {
                        if (currentStep != 1) return;
                        selectFloatingSticker(finalOptIndex);
                    });
                }, 380);
            } else {
                hideFloatingStickerChoices();
                View targetChip = (optIndex == 0) ? chipOpt1 : (optIndex == 1 ? chipOpt2 : chipOpt3);
                final int finalOptIndex = optIndex;

                screen2Handler.postDelayed(() -> {
                    if (currentStep != 1) return;
                    if (targetChip != null) {
                        performTapAt(targetChip, () -> {
                            if (currentStep != 1) return;
                            applyOptionForMode(mode, finalOptIndex);
                        });
                    }
                }, 300);
            }
        });
    }

    private void updateMiniTrayForMode(int mode, int selectedIndex) {
        if (layoutMiniTray == null) return;

        switch (mode) {
            case 0:
                if (tvMiniTrayLabel != null) tvMiniTrayLabel.setText("🎨 Colors:");
                if (tvChipOpt1 != null) tvChipOpt1.setText("Gold");
                if (tvChipOpt2 != null) tvChipOpt2.setText("Purple");
                if (tvChipOpt3 != null) tvChipOpt3.setText("Magenta");
                break;
            case 1:
                if (tvMiniTrayLabel != null) tvMiniTrayLabel.setText("🔤 Headline:");
                if (tvChipOpt1 != null) tvChipOpt1.setText("Festive");
                if (tvChipOpt2 != null) tvChipOpt2.setText("Mega Sale");
                if (tvChipOpt3 != null) tvChipOpt3.setText("Wishes");
                break;
            case 2:
                if (tvMiniTrayLabel != null) tvMiniTrayLabel.setText("🏷️ Stickers:");
                if (tvChipOpt1 != null) tvChipOpt1.setText("🔥 50% OFF");
                if (tvChipOpt2 != null) tvChipOpt2.setText("🪔 Shubh");
                if (tvChipOpt3 != null) tvChipOpt3.setText("🎉 Special");
                break;
            case 3:
                if (tvMiniTrayLabel != null) tvMiniTrayLabel.setText("🪄 Effects:");
                if (tvChipOpt1 != null) tvChipOpt1.setText("✨ Sparkles");
                if (tvChipOpt2 != null) tvChipOpt2.setText("🌟 Diya Glow");
                if (tvChipOpt3 != null) tvChipOpt3.setText("💫 Mega Burst");
                break;
        }

        setChipSelected(chipOpt1, tvChipOpt1, selectedIndex == 0);
        setChipSelected(chipOpt2, tvChipOpt2, selectedIndex == 1);
        setChipSelected(chipOpt3, tvChipOpt3, selectedIndex == 2);
    }

    private void setChipSelected(View chip, TextView label, boolean selected) {
        if (chip == null || label == null) return;
        if (selected) {
            chip.setBackgroundResource(R.drawable.spawner_bg_dock_item_active);
            label.setTextColor(Color.WHITE);
            chip.animate().scaleX(1.1f).scaleY(1.1f).setDuration(140)
                    .withEndAction(() -> chip.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                    .start();
        } else {
            chip.setBackgroundResource(R.drawable.spawner_bg_tag_pill);
            label.setTextColor(Color.parseColor("#7B2FF7"));
            chip.setScaleX(1.0f);
            chip.setScaleY(1.0f);
        }
    }

    private void applyOptionForMode(int mode, int optIndex) {
        updateMiniTrayForMode(mode, optIndex);

        switch (mode) {
            case 0:
                applyBackgroundOption(optIndex);
                break;
            case 1:
                applyFontOption(optIndex);
                break;
            case 2:
                applyStickerOption(optIndex);
                break;
            case 3:
                applyEffectsOption(optIndex);
                break;
        }
    }

    private void applyBackgroundOption(int index) {
        if (cardCanvas == null) return;

        bgOverlayIndex = index % bgOverlayColors.length;
        int color = bgOverlayColors[bgOverlayIndex];

        if (viewCanvasBgOverlay != null) {
            viewCanvasBgOverlay.animate().alpha(0f).setDuration(100).withEndAction(() -> {
                viewCanvasBgOverlay.setBackgroundColor(color);
                viewCanvasBgOverlay.animate().alpha(1f).setDuration(220).start();
            }).start();
        }

        if (ivAuraGlow2 != null) {
            ivAuraGlow2.animate().scaleX(1.15f).scaleY(1.15f).alpha(1f).setDuration(200)
                    .withEndAction(() -> ivAuraGlow2.animate().scaleX(1.0f).scaleY(1.0f).alpha(0.85f).setDuration(350).start())
                    .start();
        }

        if (pillToolBg != null) {
            pillToolBg.animate().scaleX(1.18f).scaleY(1.18f).setDuration(160)
                    .withEndAction(() -> pillToolBg.animate().scaleX(1.0f).scaleY(1.0f).setDuration(160).start())
                    .start();
        }

        if (tvCanvasTag != null) {
            tvCanvasTag.setText("🎨 BACKGROUND");
        }
    }

    private void applyFontOption(int index) {
        if (tvCanvasHeadline == null || viewSelectionBorder == null) return;

        fontPresetIndex = index % fontTextPresets.length;

        // Selection border bounce on Diwali poster
        viewSelectionBorder.animate().scaleX(1.15f).scaleY(1.15f).setDuration(160)
                .withEndAction(() -> viewSelectionBorder.animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(new OvershootInterpolator(1.8f)).start())
                .start();

        View[] handles = {handleTL, handleTR, handleBL, handleBR};
        for (View h : handles) {
            if (h != null) {
                h.animate().scaleX(1.5f).scaleY(1.5f).setDuration(160)
                        .withEndAction(() -> h.animate().scaleX(1.0f).scaleY(1.0f).setDuration(160).start())
                        .start();
            }
        }

        // Headline text change with flip pop
        tvCanvasHeadline.animate().scaleX(0.6f).scaleY(0.6f).alpha(0f).setDuration(130).withEndAction(() -> {
            tvCanvasHeadline.setText(fontTextPresets[fontPresetIndex][0]);
            if (tvCanvasSub != null) tvCanvasSub.setText(fontTextPresets[fontPresetIndex][1]);
            tvCanvasHeadline.animate().scaleX(1.0f).scaleY(1.0f).alpha(1f).setDuration(240).setInterpolator(new OvershootInterpolator(1.8f)).start();
        }).start();

        if (pillToolFonts != null) {
            pillToolFonts.animate().scaleX(1.18f).scaleY(1.18f).setDuration(160)
                    .withEndAction(() -> pillToolFonts.animate().scaleX(1f).scaleY(1f).setDuration(160).start())
                    .start();
        }

        if (tvCanvasTag != null) {
            tvCanvasTag.setText("🔤 TYPOGRAPHY");
        }
    }

    private void showFloatingStickerChoices() {
        View[] choices = {stickerChoice1, stickerChoice2, stickerChoice3, stickerChoice4};
        for (int i = 0; i < choices.length; i++) {
            final View choice = choices[i];
            if (choice == null) continue;
            choice.setVisibility(View.VISIBLE);
            choice.setScaleX(0f);
            choice.setScaleY(0f);
            choice.setAlpha(0f);
            choice.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setStartDelay(i * 65L)
                    .setDuration(280)
                    .setInterpolator(new OvershootInterpolator(1.8f))
                    .start();
        }
    }

    private void hideFloatingStickerChoices() {
        View[] choices = {stickerChoice1, stickerChoice2, stickerChoice3, stickerChoice4};
        for (View choice : choices) {
            if (choice == null) continue;
            if (choice.getVisibility() == View.VISIBLE) {
                choice.animate()
                        .scaleX(0.3f)
                        .scaleY(0.3f)
                        .alpha(0f)
                        .setDuration(180)
                        .withEndAction(() -> choice.setVisibility(View.GONE))
                        .start();
            }
        }
    }

    private void selectFloatingSticker(int index) {
        resetStudioCycleTimer();
        int safeIndex = index % badgePresets.length;
        badgePresetIndex = safeIndex;

        View[] choices = {stickerChoice1, stickerChoice2, stickerChoice3, stickerChoice4};
        View selectedView = choices[safeIndex];

        if (selectedView != null) {
            selectedView.animate()
                    .scaleX(1.28f)
                    .scaleY(1.28f)
                    .setDuration(160)
                    .withEndAction(() -> {
                        if (layoutBadge3D != null && tvBadgeText != null) {
                            tvBadgeText.setText(badgePresets[safeIndex]);

                            layoutBadge3D.setScaleX(0.2f);
                            layoutBadge3D.setScaleY(0.2f);
                            layoutBadge3D.setAlpha(0f);
                            layoutBadge3D.setRotation(-35f);

                            layoutBadge3D.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1f)
                                    .rotation(-6f)
                                    .setDuration(360)
                                    .setInterpolator(new OvershootInterpolator(2.2f))
                                    .start();

                            if (ivSparkle1 != null) {
                                ivSparkle1.animate().scaleX(1.8f).scaleY(1.8f).rotationBy(180f).setDuration(240)
                                        .withEndAction(() -> ivSparkle1.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                                        .start();
                            }
                        }

                        hideFloatingStickerChoices();
                    })
                    .start();
        } else {
            if (layoutBadge3D != null && tvBadgeText != null) {
                tvBadgeText.setText(badgePresets[safeIndex]);
                layoutBadge3D.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150)
                        .withEndAction(() -> layoutBadge3D.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                        .start();
            }
            hideFloatingStickerChoices();
        }

        if (pillToolStickers != null) {
            pillToolStickers.animate().scaleX(1.18f).scaleY(1.18f).setDuration(160)
                    .withEndAction(() -> pillToolStickers.animate().scaleX(1f).scaleY(1f).setDuration(160).start())
                    .start();
        }

        if (tvCanvasTag != null) {
            tvCanvasTag.setText("🏷️ 3D STICKERS");
        }

        updateMiniTrayForMode(2, safeIndex);
    }

    private void applyStickerOption(int index) {
        selectFloatingSticker(index);
    }

    private void applyEffectsOption(int index) {
        effectsPresetIndex = index % 3;

        // 1. Diya Sparkles Spin & Burst
        if (ivSparkle1 != null) {
            ivSparkle1.animate().scaleX(1.9f).scaleY(1.9f).rotationBy(360f).setDuration(350)
                    .withEndAction(() -> ivSparkle1.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start())
                    .start();
        }
        if (ivSparkle2 != null) {
            ivSparkle2.animate().scaleX(1.9f).scaleY(1.9f).rotationBy(-360f).setDuration(350)
                    .withEndAction(() -> ivSparkle2.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start())
                    .start();
        }

        // 2. Full Background Aura Glow Pulse
        if (ivAuraGlow2 != null) {
            ivAuraGlow2.animate().scaleX(1.22f).scaleY(1.22f).alpha(1f).setDuration(250).withEndAction(() -> {
                ivAuraGlow2.animate().scaleX(1.0f).scaleY(1.0f).alpha(0.85f).setDuration(450).start();
            }).start();
        }

        // 3. Poster Text Glow Pulse
        if (tvCanvasHeadline != null) {
            tvCanvasHeadline.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200)
                    .withEndAction(() -> tvCanvasHeadline.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start())
                    .start();
        }

        // 4. Sticker Badge Glow Bounce
        if (layoutBadge3D != null) {
            layoutBadge3D.animate().scaleX(1.18f).scaleY(1.18f).rotation(-2f).setDuration(200)
                    .withEndAction(() -> layoutBadge3D.animate().scaleX(1.0f).scaleY(1.0f).rotation(-6f).setDuration(200).start())
                    .start();
        }

        // 5. Glow across all 4 floating pills around the poster
        View[] allPills = {pillToolBg, pillToolFonts, pillToolStickers, pillToolEffects};
        for (View p : allPills) {
            if (p != null) {
                p.animate().scaleX(1.14f).scaleY(1.14f).setDuration(220)
                        .withEndAction(() -> p.animate().scaleX(1.0f).scaleY(1.0f).setDuration(220).start())
                        .start();
            }
        }

        if (tvCanvasTag != null) {
            tvCanvasTag.setText("✨ MAGIC GLOW");
        }
    }

    private void startScreen2Entrance() {
        if (cardCanvas == null) return;

        studioCycleIndex = 0;

        cardCanvas.setAlpha(0f);
        cardCanvas.setScaleX(0.75f);
        cardCanvas.setScaleY(0.75f);
        cardCanvas.setTranslationY(0f);

        if (ivHandPointer != null) {
            ivHandPointer.setAlpha(0f);
            ivHandPointer.setVisibility(View.INVISIBLE);
        }
        if (viewTapRipple != null) {
            viewTapRipple.setAlpha(0f);
            viewTapRipple.setVisibility(View.INVISIBLE);
        }

        if (layoutTextBox != null) {
            layoutTextBox.setScaleX(0.6f);
            layoutTextBox.setScaleY(0.6f);
            layoutTextBox.setAlpha(0f);
        }

        if (layoutBadge3D != null) {
            layoutBadge3D.setScaleX(0f);
            layoutBadge3D.setScaleY(0f);
        }

        if (pillToolBg != null) { pillToolBg.setScaleX(0f); pillToolBg.setScaleY(0f); }
        if (pillToolStickers != null) { pillToolStickers.setScaleX(0f); pillToolStickers.setScaleY(0f); }
        if (pillToolFonts != null) { pillToolFonts.setScaleX(0f); pillToolFonts.setScaleY(0f); }
        if (pillToolEffects != null) { pillToolEffects.setScaleX(0f); pillToolEffects.setScaleY(0f); }

        if (llScreen2BottomArea != null) {
            llScreen2BottomArea.setAlpha(0f);
            llScreen2BottomArea.setTranslationY(40f);
            llScreen2BottomArea.animate().alpha(1f).translationY(0f).setStartDelay(200).setDuration(600).start();
        }

        if (ivAuraGlow2 != null) {
            ivAuraGlow2.setAlpha(0f);
            ivAuraGlow2.setScaleX(0.7f);
            ivAuraGlow2.setScaleY(0.7f);
            ivAuraGlow2.animate().alpha(0.85f).scaleX(1.0f).scaleY(1.0f).setDuration(800).setInterpolator(new DecelerateInterpolator()).start();
        }

        // Animate Canvas
        cardCanvas.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(700)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withEndAction(this::startScreen2ContinuousFloating)
                .start();

        // Animate Text Box
        if (layoutTextBox != null) {
            layoutTextBox.postDelayed(() -> {
                layoutTextBox.animate().alpha(1f).scaleX(1.0f).scaleY(1.0f).setDuration(400).setInterpolator(new OvershootInterpolator(1.5f)).start();
            }, 300);
        }

        // Animate Badge
        if (layoutBadge3D != null) {
            layoutBadge3D.postDelayed(() -> {
                layoutBadge3D.animate().scaleX(1.0f).scaleY(1.0f).rotation(-6f).setDuration(400).setInterpolator(new OvershootInterpolator(2.0f)).start();
            }, 500);
        }

        // Pop in Floating tool pills
        popInView(pillToolBg, 350);
        popInView(pillToolStickers, 480);
        popInView(pillToolFonts, 600);
        popInView(pillToolEffects, 720);

        updateMiniTrayForMode(0, 0);

        startStudioCycle();
    }

    private void startScreen2ContinuousFloating() {
        stopScreen2FloatingAnimators();

        if (cardCanvas == null) return;

        // Poster remains 100% fixed in place without vertical bobbing/shaking
        cardCanvas.setTranslationY(0f);

        if (ivAuraGlow2 != null) {
            PropertyValuesHolder glowX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.94f, 1.06f);
            PropertyValuesHolder glowY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.94f, 1.06f);
            PropertyValuesHolder glowA = PropertyValuesHolder.ofFloat(View.ALPHA, 0.70f, 0.95f);
            ObjectAnimator animGlow = ObjectAnimator.ofPropertyValuesHolder(ivAuraGlow2, glowX, glowY, glowA);
            animGlow.setDuration(3600);
            animGlow.setRepeatMode(ValueAnimator.REVERSE);
            animGlow.setRepeatCount(ValueAnimator.INFINITE);
            animGlow.setInterpolator(new AccelerateDecelerateInterpolator());
            animGlow.start();
            screen2Animators.add(animGlow);
        }

        // Sparkle twinkles
        if (ivSparkle1 != null) {
            ObjectAnimator animSp1 = ObjectAnimator.ofFloat(ivSparkle1, View.ROTATION, 0f, 180f);
            animSp1.setDuration(4000);
            animSp1.setRepeatMode(ValueAnimator.REVERSE);
            animSp1.setRepeatCount(ValueAnimator.INFINITE);
            animSp1.start();
            screen2Animators.add(animSp1);
        }
    }

    private void setStudioDockActive(int mode) {
        studioMode = mode;
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#64748B");

        if (dockToolBg != null) {
            dockToolBg.setBackgroundResource(mode == 0 ? R.drawable.spawner_bg_dock_item_active : android.R.color.transparent);
            if (tvDockBg != null) tvDockBg.setTextColor(mode == 0 ? activeText : inactiveText);
            if (mode == 0) dockToolBg.animate().scaleX(1.08f).scaleY(1.08f).setDuration(150).withEndAction(() -> dockToolBg.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
        }
        if (dockToolFonts != null) {
            dockToolFonts.setBackgroundResource(mode == 1 ? R.drawable.spawner_bg_dock_item_active : android.R.color.transparent);
            if (tvDockFonts != null) tvDockFonts.setTextColor(mode == 1 ? activeText : inactiveText);
            if (mode == 1) dockToolFonts.animate().scaleX(1.08f).scaleY(1.08f).setDuration(150).withEndAction(() -> dockToolFonts.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
        }
        if (dockToolStickers != null) {
            dockToolStickers.setBackgroundResource(mode == 2 ? R.drawable.spawner_bg_dock_item_active : android.R.color.transparent);
            if (tvDockStickers != null) tvDockStickers.setTextColor(mode == 2 ? activeText : inactiveText);
            if (mode == 2) dockToolStickers.animate().scaleX(1.08f).scaleY(1.08f).setDuration(150).withEndAction(() -> dockToolStickers.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
        }
        if (dockToolEffects != null) {
            dockToolEffects.setBackgroundResource(mode == 3 ? R.drawable.spawner_bg_dock_item_active : android.R.color.transparent);
            if (tvDockEffects != null) tvDockEffects.setTextColor(mode == 3 ? activeText : inactiveText);
            if (mode == 3) dockToolEffects.animate().scaleX(1.08f).scaleY(1.08f).setDuration(150).withEndAction(() -> dockToolEffects.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
        }
    }

    private void startStudioCycle() {
        stopStudioCycle();
        studioCycleRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentStep == 1) {
                    int modeToRun = studioCycleIndex % 4;
                    studioCycleIndex++;
                    runGuidedHandDemo(modeToRun);
                    screen2Handler.postDelayed(this, 3600);
                }
            }
        };
        screen2Handler.postDelayed(studioCycleRunnable, 600);
    }

    private void stopStudioCycle() {
        if (studioCycleRunnable != null) {
            screen2Handler.removeCallbacks(studioCycleRunnable);
            studioCycleRunnable = null;
        }
        screen2Handler.removeCallbacksAndMessages(null);
    }

    private void resetStudioCycleTimer() {
        startStudioCycle();
    }

    private void stopScreen2Animations() {
        stopStudioCycle();
        stopScreen2FloatingAnimators();
        hideFloatingStickerChoices();
        if (ivHandPointer != null) {
            ivHandPointer.animate().cancel();
            ivHandPointer.setVisibility(View.INVISIBLE);
        }
        if (viewTapRipple != null) {
            viewTapRipple.animate().cancel();
            viewTapRipple.setVisibility(View.INVISIBLE);
        }
    }

    private void stopScreen2FloatingAnimators() {
        for (Animator anim : screen2Animators) {
            if (anim != null && anim.isRunning()) {
                anim.cancel();
            }
        }
        screen2Animators.clear();
    }

    // ==========================================
    // SCREEN 3: VIRAL SOCIAL SHARING SHOWCASE
    // ==========================================

    private void initScreen3Views() {
        cardSharePoster = findViewById(R.id.cardSharePoster);
        ivSharePoster = findViewById(R.id.ivSharePoster);
        ivAuraGlow3 = findViewById(R.id.ivAuraGlow3);
        layoutBadgeSaved = findViewById(R.id.layoutBadgeSaved);
        tvBadgeSaved = findViewById(R.id.tvBadgeSaved);
        layoutBadgeWatermark = findViewById(R.id.layoutBadgeWatermark);
        cardShareWhatsapp = findViewById(R.id.cardShareWhatsapp);
        bubbleWhatsappSent = findViewById(R.id.bubbleWhatsappSent);
        cardShareInstagram = findViewById(R.id.cardShareInstagram);
        bubbleInstagramLikes = findViewById(R.id.bubbleInstagramLikes);
        ivShareSparkle1 = findViewById(R.id.ivShareSparkle1);
        ivShareSparkle2 = findViewById(R.id.ivShareSparkle2);
        llQuickShareBar = findViewById(R.id.llQuickShareBar);
        btnQuickWa = findViewById(R.id.btnQuickWa);
        btnQuickIg = findViewById(R.id.btnQuickIg);
        btnQuickFb = findViewById(R.id.btnQuickFb);
        btnQuickDl = findViewById(R.id.btnQuickDl);
        tvScreen3Title = findViewById(R.id.tvScreen3Title);
        tvScreen3Subtitle = findViewById(R.id.tvScreen3Subtitle);
        llScreen3BottomArea = findViewById(R.id.llScreen3BottomArea);

        isScreen3ViewsBound = (cardSharePoster != null);

        if (cardShareWhatsapp != null) {
            cardShareWhatsapp.setOnClickListener(v -> bounceSocialNode(cardShareWhatsapp, bubbleWhatsappSent));
        }
        if (btnQuickWa != null) {
            btnQuickWa.setOnClickListener(v -> bounceSocialNode(cardShareWhatsapp, bubbleWhatsappSent));
        }
        if (cardShareInstagram != null) {
            cardShareInstagram.setOnClickListener(v -> bounceSocialNode(cardShareInstagram, bubbleInstagramLikes));
        }
        if (btnQuickIg != null) {
            btnQuickIg.setOnClickListener(v -> bounceSocialNode(cardShareInstagram, bubbleInstagramLikes));
        }
        if (btnQuickFb != null) {
            btnQuickFb.setOnClickListener(v -> animateButtonBounce(btnQuickFb));
        }
        if (btnQuickDl != null) {
            btnQuickDl.setOnClickListener(v -> {
                animateButtonBounce(btnQuickDl);
                if (layoutBadgeSaved != null) {
                    layoutBadgeSaved.animate()
                            .scaleX(1.2f).scaleY(1.2f)
                            .setDuration(180)
                            .withEndAction(() -> layoutBadgeSaved.animate().scaleX(1f).scaleY(1f).setDuration(180).start())
                            .start();
                }
            });
        }
    }

    private void startScreen3Entrance() {
        if (!isScreen3ViewsBound) initScreen3Views();
        if (cardSharePoster == null) return;

        // Reset starting states
        cardSharePoster.setScaleX(0.85f);
        cardSharePoster.setScaleY(0.85f);
        cardSharePoster.setAlpha(0f);

        if (layoutBadgeSaved != null) {
            layoutBadgeSaved.setScaleX(0f);
            layoutBadgeSaved.setScaleY(0f);
            layoutBadgeSaved.setAlpha(0f);
        }

        if (cardShareWhatsapp != null) {
            cardShareWhatsapp.setTranslationX(-dpToPx(70));
            cardShareWhatsapp.setAlpha(0f);
        }
        if (bubbleWhatsappSent != null) {
            bubbleWhatsappSent.setTranslationY(dpToPx(15));
            bubbleWhatsappSent.setScaleX(0f);
            bubbleWhatsappSent.setScaleY(0f);
            bubbleWhatsappSent.setAlpha(0f);
        }

        if (cardShareInstagram != null) {
            cardShareInstagram.setTranslationX(dpToPx(70));
            cardShareInstagram.setAlpha(0f);
        }
        if (bubbleInstagramLikes != null) {
            bubbleInstagramLikes.setTranslationY(dpToPx(15));
            bubbleInstagramLikes.setScaleX(0f);
            bubbleInstagramLikes.setScaleY(0f);
            bubbleInstagramLikes.setAlpha(0f);
        }

        if (llQuickShareBar != null) {
            llQuickShareBar.setTranslationY(dpToPx(30));
            llQuickShareBar.setAlpha(0f);
        }
        if (tvScreen3Title != null) {
            tvScreen3Title.setTranslationY(dpToPx(20));
            tvScreen3Title.setAlpha(0f);
        }
        if (tvScreen3Subtitle != null) {
            tvScreen3Subtitle.setTranslationY(dpToPx(20));
            tvScreen3Subtitle.setAlpha(0f);
        }

        // Animate poster in
        cardSharePoster.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .withEndAction(() -> {
                    // Checkmark badge pops in
                    if (layoutBadgeSaved != null) {
                        layoutBadgeSaved.animate()
                                .scaleX(1f).scaleY(1f).alpha(1f)
                                .setDuration(400)
                                .setInterpolator(new OvershootInterpolator(2.2f))
                                .start();
                    }
                    // WhatsApp node slides in
                    if (cardShareWhatsapp != null) {
                        cardShareWhatsapp.animate()
                                .translationX(0f).alpha(1f)
                                .setDuration(500)
                                .setInterpolator(new OvershootInterpolator(1.4f))
                                .withEndAction(() -> {
                                    if (bubbleWhatsappSent != null) {
                                        bubbleWhatsappSent.animate()
                                                .translationY(0f).scaleX(1f).scaleY(1f).alpha(1f)
                                                .setDuration(400)
                                                .setInterpolator(new OvershootInterpolator(2.0f))
                                                .start();
                                    }
                                }).start();
                    }
                    // Instagram node slides in
                    if (cardShareInstagram != null) {
                        cardShareInstagram.animate()
                                .translationX(0f).alpha(1f)
                                .setDuration(500)
                                .setStartDelay(120)
                                .setInterpolator(new OvershootInterpolator(1.4f))
                                .withEndAction(() -> {
                                    if (bubbleInstagramLikes != null) {
                                        bubbleInstagramLikes.animate()
                                                .translationY(0f).scaleX(1f).scaleY(1f).alpha(1f)
                                                .setDuration(400)
                                                .setInterpolator(new OvershootInterpolator(2.0f))
                                                .start();
                                    }
                                }).start();
                    }

                    // Quick Share strip and texts
                    if (llQuickShareBar != null) {
                        llQuickShareBar.animate()
                                .translationY(0f).alpha(1f)
                                .setDuration(450)
                                .setStartDelay(200)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    }
                    if (tvScreen3Title != null) {
                        tvScreen3Title.animate()
                                .translationY(0f).alpha(1f)
                                .setDuration(400)
                                .setStartDelay(260)
                                .start();
                    }
                    if (tvScreen3Subtitle != null) {
                        tvScreen3Subtitle.animate()
                                .translationY(0f).alpha(1f)
                                .setDuration(400)
                                .setStartDelay(320)
                                .withEndAction(() -> {
                                    startScreen3ContinuousFloating();
                                    startScreen3ShareCycle();
                                })
                                .start();
                    }
                }).start();
    }

    private void startScreen3ContinuousFloating() {
        stopScreen3FloatingAnimators();

        if (cardShareWhatsapp != null) {
            ObjectAnimator animWa = ObjectAnimator.ofFloat(cardShareWhatsapp, View.TRANSLATION_Y, 0f, -dpToPx(5), 0f);
            animWa.setDuration(2400);
            animWa.setRepeatCount(ValueAnimator.INFINITE);
            animWa.setInterpolator(new AccelerateDecelerateInterpolator());
            animWa.start();
            screen3Animators.add(animWa);
        }

        if (cardShareInstagram != null) {
            ObjectAnimator animIg = ObjectAnimator.ofFloat(cardShareInstagram, View.TRANSLATION_Y, 0f, dpToPx(5), 0f);
            animIg.setDuration(2600);
            animIg.setRepeatCount(ValueAnimator.INFINITE);
            animIg.setInterpolator(new AccelerateDecelerateInterpolator());
            animIg.start();
            screen3Animators.add(animIg);
        }

        if (ivShareSparkle1 != null) {
            ObjectAnimator rot1 = ObjectAnimator.ofFloat(ivShareSparkle1, View.ROTATION, 0f, 360f);
            rot1.setDuration(6000);
            rot1.setRepeatCount(ValueAnimator.INFINITE);
            rot1.setInterpolator(new LinearInterpolator());
            rot1.start();
            screen3Animators.add(rot1);

            ObjectAnimator scale1 = ObjectAnimator.ofPropertyValuesHolder(
                    ivShareSparkle1,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1.25f, 0.8f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1.25f, 0.8f),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0.5f, 1f, 0.5f)
            );
            scale1.setDuration(2200);
            scale1.setRepeatCount(ValueAnimator.INFINITE);
            scale1.start();
            screen3Animators.add(scale1);
        }

        if (ivShareSparkle2 != null) {
            ObjectAnimator scale2 = ObjectAnimator.ofPropertyValuesHolder(
                    ivShareSparkle2,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0.7f, 1.2f, 0.7f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.7f, 1.2f, 0.7f),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0.4f, 1f, 0.4f)
            );
            scale2.setDuration(1900);
            scale2.setRepeatCount(ValueAnimator.INFINITE);
            scale2.start();
            screen3Animators.add(scale2);
        }

        if (ivAuraGlow3 != null) {
            ObjectAnimator auraAnim = ObjectAnimator.ofPropertyValuesHolder(
                    ivAuraGlow3,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.12f, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.12f, 1f),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0.6f, 0.9f, 0.6f)
            );
            auraAnim.setDuration(3200);
            auraAnim.setRepeatCount(ValueAnimator.INFINITE);
            auraAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            auraAnim.start();
            screen3Animators.add(auraAnim);
        }
    }

    private void startScreen3ShareCycle() {
        stopScreen3ShareCycle();
        shareCycleRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentStep == 2 && isScreen3ViewsBound) {
                    if (shareCycleMode == 0) {
                        bounceSocialNode(cardShareWhatsapp, bubbleWhatsappSent);
                        animateButtonBounce(btnQuickWa);
                        shareCycleMode = 1;
                    } else {
                        bounceSocialNode(cardShareInstagram, bubbleInstagramLikes);
                        animateButtonBounce(btnQuickIg);
                        shareCycleMode = 0;
                    }
                    screen3Handler.postDelayed(this, 2400);
                }
            }
        };
        screen3Handler.postDelayed(shareCycleRunnable, 2400);
    }

    private void bounceSocialNode(View card, View bubble) {
        if (card != null) {
            card.animate()
                    .scaleX(1.08f).scaleY(1.08f)
                    .setDuration(220)
                    .setInterpolator(new OvershootInterpolator(1.8f))
                    .withEndAction(() -> card.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                    .start();
        }
        if (bubble != null) {
            bubble.animate()
                    .scaleX(1.15f).scaleY(1.15f)
                    .setDuration(220)
                    .setInterpolator(new OvershootInterpolator(2.2f))
                    .withEndAction(() -> bubble.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                    .start();
        }
    }

    private void animateButtonBounce(View btn) {
        if (btn == null) return;
        btn.animate()
                .scaleX(1.18f).scaleY(1.18f)
                .setDuration(180)
                .setInterpolator(new OvershootInterpolator(2f))
                .withEndAction(() -> btn.animate().scaleX(1f).scaleY(1f).setDuration(180).start())
                .start();
    }

    private void stopScreen3ShareCycle() {
        if (shareCycleRunnable != null) {
            screen3Handler.removeCallbacks(shareCycleRunnable);
            shareCycleRunnable = null;
        }
    }

    private void stopScreen3Animations() {
        stopScreen3ShareCycle();
        stopScreen3FloatingAnimators();
    }

    private void stopScreen3FloatingAnimators() {
        for (Animator anim : screen3Animators) {
            if (anim != null && anim.isRunning()) {
                anim.cancel();
            }
        }
        screen3Animators.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentStep == 0 && isScreen1ViewsBound) {
            startContinuousFloating();
            startAutoCarousel();
        } else if (currentStep == 1 && isScreen2ViewsBound) {
            startScreen2ContinuousFloating();
            startStudioCycle();
        } else if (currentStep == 2 && isScreen3ViewsBound) {
            startScreen3ContinuousFloating();
            startScreen3ShareCycle();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentStep == 0) {
            stopScreen1Animations();
        } else if (currentStep == 1) {
            stopScreen2Animations();
        } else if (currentStep == 2) {
            stopScreen3Animations();
        }
    }

    @Override
    protected void onDestroy() {
        stopScreen1Animations();
        stopScreen2Animations();
        stopScreen3Animations();
        super.onDestroy();
    }
}
