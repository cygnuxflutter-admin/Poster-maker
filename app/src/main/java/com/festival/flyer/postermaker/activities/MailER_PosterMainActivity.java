package com.festival.flyer.postermaker.activities;

import static com.festival.flyer.postermaker.adManager.MailER_LoadAds.loadAdmobBannerAd;
import static com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil.loadNativeAd;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
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

public class MailER_PosterMainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.spawner_activity_mainposter);

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, "83d4adaf-4ae7-4f59-b91a-d0050698af6a");
        OneSignal.getNotifications().requestPermission(true, Continue.none());

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


        drawerLayout = findViewById(R.id.my_drawer_layout);
        navigationView = findViewById(R.id.nav_view_poster_maker);

        setupDrawerContent(navigationView);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        preferenceClass = new MailER_PreferenceClass(this);

        findByID();

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

//        MediationTestSuite.launch(MainActivity.this);
    }


    private FrameLayout bannerContainer;
    ShimmerFrameLayout shimmerFrameLayout;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setupDrawerContent(NavigationView nvDrawer) {
        nvDrawer.setNavigationItemSelectedListener(item -> {
                    selectDrawerItem(item);
                    return true;
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void selectDrawerItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_share_app) {
            MailER_ShareUtils.onShare(MailER_PosterMainActivity.this);
        } else if (id == R.id.nav_rate_app) {
            MailER_ShareUtils.rateUs(MailER_PosterMainActivity.this);
        } else if (id == R.id.iv_instagram_app) {
            MailER_ShareUtils.onInstagram(MailER_PosterMainActivity.this);
        } else if (id == R.id.iv_privacy_policy) {
            Intent intent = new Intent(MailER_PosterMainActivity.this, MailER_PrivacyPolicyActivity.class);
            startActivity(intent);
        }
        item.setChecked(true);
        setTitle(item.getTitle());
        drawerLayout.closeDrawers();
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

}