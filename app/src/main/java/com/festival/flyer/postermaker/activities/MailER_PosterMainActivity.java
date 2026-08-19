package com.festival.flyer.postermaker.activities;

import static com.festival.flyer.postermaker.adManager.MailER_LoadAds.loadAdmobBannerAd;
import static com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil.loadNativeAd;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import com.festival.flyer.postermaker.utils.MailER_BottomNavHelper;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.utils.MailER_ShareUtils;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.Continue;
import androidx.core.view.WindowCompat;
import androidx.activity.OnBackPressedCallback;

import java.util.UUID;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import com.festival.flyer.postermaker.model.HeroSliderModel;
import com.festival.flyer.postermaker.adapter.HeroSliderAdapter;
import java.util.ArrayList;
import java.util.List;

import android.widget.HorizontalScrollView;
public class MailER_PosterMainActivity extends AppCompatActivity {

    private HorizontalScrollView hsvTrending;
    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private boolean isScrollingForward = true;

    private ViewPager2 heroViewPager;
    private LinearLayout heroIndicatorLayout;
    private HeroSliderAdapter heroSliderAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    @RequiresApi(api = Build.VERSION_CODES.Q)

    public NavigationView navigationView;

    //    private CarouselView carouselView;
    private MailER_PreferenceClass preferenceClass;
    private int activityIndex = 0;


    final private int REQUEST_CAMERA_AND_STORAGE_PERMISSION = 100;

    private ConsentInformation consentInformation;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.spawner_activity_mainposter);

        hsvTrending = findViewById(R.id.hsv_trending);
        setupTrendingAutoScroll();

        MailER_BottomNavHelper.setupBottomNav(this, R.id.tab_home);


        // Set light status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.home_bg));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // OneSignal Permission Request
        OneSignal.getNotifications().requestPermission(true, Continue.none());

        // OneSignal Token Logging
        if (OneSignal.getUser().getPushSubscription() != null) {
            String pushToken = OneSignal.getUser().getPushSubscription().getToken();
            String subscriptionId = OneSignal.getUser().getPushSubscription().getId();
            android.util.Log.d("FCM_TOKEN", "OneSignal Push Token: " + pushToken);
            android.util.Log.d("FCM_TOKEN", "OneSignal Subscription ID: " + subscriptionId);
        }

        // Direct FCM Token Logging
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                android.util.Log.d("FCM_TOKEN", "Direct FCM Token: " + fcmToken);
            } else {
                android.util.Log.e("FCM_TOKEN", "Token Error: ", task.getException());
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MailER_MaterialDialogUtils.getInstance().exitAlertDialog(MailER_PosterMainActivity.this, "Exit Alert", "Do you really want to exit?", materialDialog -> {
                    materialDialog.dismiss();
                    MailER_PosterMainActivity.this.finish();
                    System.exit(0);
                });
            }
        });

        checkAccess();

//        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(this)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                .addTestDeviceHashedId("B3EEABB8EE11C2BE770B684D95219ECB")
//                .build();

        // Create a ConsentRequestParameters object.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                /*.setConsentDebugSettings(debugSettings)*/
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    Log.w("TAG52451", String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }
                            }
                    );
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w("TAG54697", String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                });


        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        preferenceClass = new MailER_PreferenceClass(this);

        findByID();
        setupHeroSlider();

        findViewById(R.id.lay_poster).setOnClickListener(v -> {
            activityIndex = 1;
            if (!checkPermission()) {
                try {
                    requestPermission();

//                    if (Build.MANUFACTURER.equals("TECNO MOBILE LIMITED") && Build.BRAND.equals("TECNO") ||
//                            Build.BRAND.equals("samsung") || Build.BRAND.equals("OPPO") ||
//                            Build.BRAND.equals("vivo")) {
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
//                        Log.e("#brand", Build.BRAND);
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                        intent.setData(uri);
//                        startActivity(intent);
//                    }

                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                nextActivity(MailER_BackgroundSelectionActivity.class);
            }
//            openActivity(MailER_BackgroundSelectionActivity.class);
        });
        findViewById(R.id.lay_template).setOnClickListener(v -> {
            activityIndex = 2;
            if (!checkPermission()) {
                try {
                    requestPermission();

//                    if (Build.MANUFACTURER.equals("TECNO MOBILE LIMITED") && Build.BRAND.equals("TECNO") ||
//                            Build.BRAND.equals("samsung") || Build.BRAND.equals("OPPO") ||
//                            Build.BRAND.equals("vivo")) {
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
//                        Log.e("#brand", Build.BRAND);
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                        intent.setData(uri);
//                        startActivity(intent);
//                    }

                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
//                nextActivity(MailER_HomeActivity.class);
                nextActivity(MailER_TemplateSelectionActivity.class);
            }
//            openActivity(MailER_HomeActivity.class);
        });

        findViewById(R.id.lay_design).setOnClickListener(v -> {
            MailER_ShareUtils.rateUs(MailER_PosterMainActivity.this);
        });

        findViewById(R.id.lay_creation).setOnClickListener(v -> {
            activityIndex = 4;
            if (!checkPermission()) {
                try {
                    requestPermission();

//                    if (Build.MANUFACTURER.equals("TECNO MOBILE LIMITED") && Build.BRAND.equals("TECNO") ||
//                            Build.BRAND.equals("samsung") || Build.BRAND.equals("OPPO") ||
//                            Build.BRAND.equals("vivo")) {
//                        Log.e("#brand", Build.BRAND);
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                        intent.setData(uri);
//                        startActivity(intent);
//                    }

                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                nextActivity(MailER_MyCreationActivity.class);
            }
        });




        findViewById(R.id.btn_view_all).setOnClickListener(v -> {
            findViewById(R.id.lay_template).performClick();
        });

        // Bottom Nav: Explore tab -> same as lay_template (Readymade Poster)
        findViewById(R.id.tab_explore).setOnClickListener(v -> {
            activityIndex = 2;
            if (!checkPermission()) {
                try {
                    requestPermission();
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                nextActivity(MailER_TemplateSelectionActivity.class);
            }
        });

        // Bottom Nav: Create FAB (+) -> open new Create screen
        findViewById(R.id.tab_create).setOnClickListener(v -> {
            activityIndex = 1;
            if (!checkPermission()) {
                try {
                    requestPermission();
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                nextActivity(MailER_CreateActivity.class);
            }
        });

        // Bottom Nav: My Creations tab -> same as lay_creation
        findViewById(R.id.tab_creations).setOnClickListener(v -> {
            activityIndex = 4;
            if (!checkPermission()) {
                try {
                    requestPermission();
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                nextActivity(MailER_MyCreationActivity.class);
            }
        });

        // Bottom Nav: Settings tab
        findViewById(R.id.tab_settings).setOnClickListener(v -> {
            nextActivity(MailER_SettingsActivity.class);
        });

//        MediationTestSuite.launch(MainActivity.this);

        findViewById(R.id.chip_festival).setOnClickListener(v -> openCategory("Festival"));
        findViewById(R.id.chip_wedding).setOnClickListener(v -> openCategory("Wedding"));
        findViewById(R.id.chip_sale).setOnClickListener(v -> openCategory("Sale"));
        findViewById(R.id.chip_birthday).setOnClickListener(v -> openCategory("Birthday"));
        findViewById(R.id.chip_business).setOnClickListener(v -> openCategory("Business"));
    }

    private void openCategory(String category) {
        if (!checkPermission()) {
            try {
                requestPermission();
                MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterMainActivity.this);
            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        } else {
            MyApplication.showInterstitialAd(this, () -> {
                Intent intent = new Intent(this, MailER_TemplateSelectionActivity.class);
                intent.putExtra("selected_category", category);
                startActivity(intent);
            });
        }
    }


    private FrameLayout bannerContainer;
    ShimmerFrameLayout shimmerFrameLayout;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void findByID() {
        RelativeLayout native_banner_ad_container = findViewById(R.id.native_banner_ad_container);
        ShimmerFrameLayout shimmer_view_container = findViewById(R.id.shimmer_view_container);
        ShimmerFrameLayout shimmer_view_container_banner = findViewById(R.id.shimmer_view_container_banner);

        RelativeLayout rl_banner_ad = this.findViewById(R.id.rl_banner_ad);
        RelativeLayout rl_native_ad = this.findViewById(R.id.rl_native_ad);
        RelativeLayout rl_ad_banner = this.findViewById(R.id.rl_ad);
        RelativeLayout rl_collapsible = this.findViewById(R.id.rl_collapsible);

        bannerContainer = findViewById(R.id.CollapsibleContainer);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_CollapsibleContainer);

        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            if (preferenceClass.getInt("MainScreen_Native", 0) == 1) {
                rl_collapsible.setVisibility(View.VISIBLE);
                rl_native_ad.setVisibility(View.GONE);
                rl_banner_ad.setVisibility(View.GONE);
                MailER_LoadAds.loadCollapsibleBanner(this, bannerContainer, findViewById(R.id.rl_ad), shimmerFrameLayout);
            } else if (preferenceClass.getInt("MainScreen_Native", 0) == 2) {
                rl_collapsible.setVisibility(View.GONE);
                rl_native_ad.setVisibility(View.GONE);
                rl_banner_ad.setVisibility(View.VISIBLE);
                MailER_LoadAds.loadAdmobBannerAd(this, rl_ad_banner, shimmer_view_container_banner);
            } else if (preferenceClass.getInt("MainScreen_Native", 0) == 3) {
                rl_collapsible.setVisibility(View.GONE);
                rl_native_ad.setVisibility(View.VISIBLE);
                rl_banner_ad.setVisibility(View.GONE);
                loadNativeAd(native_banner_ad_container, this, shimmer_view_container);
            } else {
                rl_collapsible.setVisibility(View.GONE);
                rl_native_ad.setVisibility(View.GONE);
                rl_banner_ad.setVisibility(View.GONE);
            }
        }
    }


    public void nextActivity(Class<? extends Activity> activity) {
        MyApplication.showInterstitialAd(this, () -> startIntent1(activity));
    }

    private void startIntent1(Class<? extends Activity> activity) {
        if (activityIndex == 1) {
            Intent intent = new Intent(MailER_PosterMainActivity.this, activity);
            intent.putExtra("mode", "bg");
            startActivity(intent);
            return;
        }
        startActivity(new Intent(MailER_PosterMainActivity.this, activity));
    }


    private void checkAccess() {

        if (Build.VERSION.SDK_INT > 21 && !checkPermission()) {
            requestPermission();
        } else {

        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA

            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA

            }, 1);
        }

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (result1 == 0 && result2 == 0) {
                return true;
            }
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (result == 0 && result1 == 0 && result2 == 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    private void setupHeroSlider() {
        heroViewPager = findViewById(R.id.hero_viewpager);
        heroIndicatorLayout = findViewById(R.id.hero_indicator_layout);

        List<HeroSliderModel> list = new ArrayList<>();
        list.add(new HeroSliderModel("Custom\nDesigns", "Create stunning custom posters in minutes", "Start Design", R.drawable.spawner_bg_hero_banner, R.drawable.picture));
        list.add(new HeroSliderModel("Festival\nPosts", "Explore thousands of readymade festival designs", "Explore Now", R.drawable.spawner_bg_hero_banner, R.drawable.picture));
        list.add(new HeroSliderModel("Business\nAds", "Grow your business with professional ad templates", "Create Ad", R.drawable.spawner_bg_hero_banner, R.drawable.picture));

        heroSliderAdapter = new HeroSliderAdapter(list, item -> {
            // No navigation on hero card for now as requested
        });

        heroViewPager.setAdapter(heroSliderAdapter);
        
        setupSliderIndicators(list.size());
        setCurrentIndicator(0);

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (heroViewPager.getAdapter() != null) {
                    int nextItem = heroViewPager.getCurrentItem() + 1;
                    if (nextItem >= heroViewPager.getAdapter().getItemCount()) {
                        nextItem = 0;
                    }
                    heroViewPager.setCurrentItem(nextItem, true);
                }
            }
        };

        heroViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void setupSliderIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.spawner_dot_unselected));
            indicators[i].setLayoutParams(layoutParams);
            heroIndicatorLayout.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = heroIndicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) heroIndicatorLayout.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.spawner_dot_selected));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.spawner_dot_unselected));
            }
        }
    }

    private void setupTrendingAutoScroll() {
        if (hsvTrending == null) return;
        
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (hsvTrending.getChildCount() > 0) {
                    android.view.ViewGroup innerContainer = (android.view.ViewGroup) hsvTrending.getChildAt(0);
                    if (innerContainer.getChildCount() > 0) {
                        int cardTotalWidth = innerContainer.getWidth() / innerContainer.getChildCount();
                        int maxScrollX = innerContainer.getWidth() - hsvTrending.getWidth();
                        
                        if (maxScrollX > 0) {
                            if (hsvTrending.getScrollX() < maxScrollX - 10) {
                                hsvTrending.smoothScrollBy(cardTotalWidth, 0);
                            } else {
                                hsvTrending.smoothScrollTo(0, 0);
                            }
                        }
                    }
                }
                autoScrollHandler.postDelayed(this, 3000);
            }
        };
        
        // Start scrolling
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }
}