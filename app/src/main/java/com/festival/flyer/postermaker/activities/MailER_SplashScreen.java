package com.festival.flyer.postermaker.activities;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_AppOpenManager;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkMonitor;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
//import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MailER_SplashScreen extends AppCompatActivity {

    private MailER_PreferenceClass preferenceClass;
    private FirebaseDatabase database;
    private DatabaseReference project_data2;
//    private AppUpdateManager appUpdateManager;
    private Dialog dialog;

    private long startTime;
    private static final long MIN_SPLASH_TIME = 4500; // 4.5 seconds
    private boolean isDataLoaded = false;
    private boolean isUpdateClicked = false;
    private MailER_NetworkMonitor.NetworkStateListener networkListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        preferenceClass = new MailER_PreferenceClass(this);
        
        setContentView(R.layout.spawner_activity_splash_screen);

        TextView tvAppName = findViewById(R.id.tv_splash_app_name);

        if (tvAppName != null) {
            tvAppName.post(() -> {
                float width = tvAppName.getPaint().measureText(tvAppName.getText().toString());
                Shader textShader = new LinearGradient(0, 0, width, 0,
                        new int[]{
                                getResources().getColor(R.color.hero_start),
                                getResources().getColor(R.color.hero_end)
                        }, null, Shader.TileMode.CLAMP);
                tvAppName.getPaint().setShader(textShader);
                tvAppName.invalidate();
            });
        }

        View logoContainer = findViewById(R.id.logo_container);
        if (logoContainer != null) {
            logoContainer.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setStartDelay(200)
                    .start();
        }
        
        // Use modern approach for fullscreen to avoid black flash on transition
        // This must be called AFTER setContentView so the DecorView exists
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars());
                getWindow().getInsetsController().setSystemBarsBehavior(
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        
        MyApplication.isAdsSplash = true;

        fetchRemoteConfig();

        networkListener = isConnected -> {
            if (isConnected && !isDataLoaded && !isFinishing() && !isDestroyed()) {
                Log.d("SplashScreen", "Network restored on Splash Screen. Triggering getData()...");
                fetchRemoteConfig();
                getData();
            }
        };
        MailER_NetworkMonitor.getInstance().addListener(networkListener);

        if (MailER_NetworkUtils.isNetworkAvailable(MailER_SplashScreen.this)) {
            getData();
        } else {
            startIntent();
        }

    }

    @Override
    protected void onDestroy() {
        if (networkListener != null) {
            MailER_NetworkMonitor.getInstance().removeListener(networkListener);
        }
        super.onDestroy();
    }

    private long findDifference(String start_date, String end_date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        try {
            Date d1 = simpleDateFormat.parse(start_date);
            Date d2 = simpleDateFormat.parse(end_date);

            long difference_In_Time = d2.getTime() - d1.getTime();

            return (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private String getSnapshotString(DataSnapshot snapshot, String key, String defaultValue) {
        if (snapshot != null && snapshot.hasChild(key) && snapshot.child(key).getValue() != null) {
            return String.valueOf(snapshot.child(key).getValue());
        }
        return defaultValue;
    }

    private int getSnapshotInt(DataSnapshot snapshot, String key, int defaultValue) {
        if (snapshot != null && snapshot.hasChild(key) && snapshot.child(key).getValue() != null) {
            try {
                String val = String.valueOf(snapshot.child(key).getValue()).trim();
                return (int) Double.parseDouble(val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private void getData() {
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            database = FirebaseDatabase.getInstance();
            DatabaseReference project_data = database.getReference("all_data").child("datas");
            project_data.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Log.e("---API_DATA---", "Firebase onDataChange called. Data: " + snapshot.toString());
                        for (int i = 0; i <= 60; i++) {
                            String key = "field_" + i;
                            preferenceClass.setDataType(key, getSnapshotString(snapshot, key, ""));
                        }

                        // Asynchronously prefetch & warm cache for Hero Banners and Trending Festivals
                        com.festival.flyer.postermaker.utils.MailER_AppCacheManager.prefetchHomeData(getApplicationContext());

                        Log.e("---API_DATA---", "Base URL (field_1): " + preferenceClass.getDataType("field_51"));
                        Log.e("---API_DATA---", "API Path (field_34): " + preferenceClass.getDataType("field_34"));

                        project_data2 = database.getReference("all_data").child("ad_data");
                        project_data2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    Log.e("snapshot", String.valueOf(snapshot));

                                    // ----------------------------------------------- All Ads from Firebase
                                    String bannerId = getSnapshotString(snapshot, "BannerAdunitID", "");
                                    String collapsibleId = getSnapshotString(snapshot, "CollapsibleBannerID", "");
                                    String interstitialId = getSnapshotString(snapshot, "InterstitalAdunitID", "");
                                    String rewardId = getSnapshotString(snapshot, "RewardVideoUnitID", "");
                                    String nativeId = getSnapshotString(snapshot, "NativeUnitID", "");
                                    String appOpenId = getSnapshotString(snapshot, "AppOpenID", "");

                                    preferenceClass.setDataType("BannerAdunitID", bannerId);
                                    preferenceClass.setAdsId("BannerAdunitID", bannerId);

                                    preferenceClass.setDataType("CollapsibleBannerID", collapsibleId);
                                    preferenceClass.setAdsId("CollapsibleBannerID", collapsibleId);

                                    preferenceClass.setDataType("InterstitalAdunitID", interstitialId);
                                    preferenceClass.setAdsId("InterstitalAdunitID", interstitialId);

                                    preferenceClass.setDataType("RewardVideoUnitID", rewardId);
                                    preferenceClass.setAdsId("RewardVideoUnitID", rewardId);

                                    preferenceClass.setDataType("NativeUnitID", nativeId);
                                    preferenceClass.setAdsId("NativeUnitID", nativeId);

                                    preferenceClass.setDataType("AppOpenID", appOpenId);
                                    preferenceClass.setAdsId("AppOpenID", appOpenId);

                                    // AdX IDs
                                    if (snapshot.hasChild("AdxAppOpenID")) preferenceClass.setAdsId("AdxAppOpenID", getSnapshotString(snapshot, "AdxAppOpenID", ""));
                                    if (snapshot.hasChild("AdxBannerAdunitID")) preferenceClass.setAdsId("AdxBannerAdunitID", getSnapshotString(snapshot, "AdxBannerAdunitID", ""));
                                    if (snapshot.hasChild("AdxInterstitalAdunitID")) preferenceClass.setAdsId("AdxInterstitalAdunitID", getSnapshotString(snapshot, "AdxInterstitalAdunitID", ""));
                                    if (snapshot.hasChild("AdxNativeUnitID")) preferenceClass.setAdsId("AdxNativeUnitID", getSnapshotString(snapshot, "AdxNativeUnitID", ""));
                                    if (snapshot.hasChild("AdxRewardVideoUnitID")) preferenceClass.setAdsId("AdxRewardVideoUnitID", getSnapshotString(snapshot, "AdxRewardVideoUnitID", ""));
                                    
                                    // Facebook IDs
                                    if (snapshot.hasChild("fbBannerAdunitID")) preferenceClass.setAdsId("fbBannerAdunitID", getSnapshotString(snapshot, "fbBannerAdunitID", ""));
                                    if (snapshot.hasChild("fbInterstitalAdunitID")) preferenceClass.setAdsId("fbInterstitalAdunitID", getSnapshotString(snapshot, "fbInterstitalAdunitID", ""));
                                    if (snapshot.hasChild("fbNativeUnitID")) preferenceClass.setAdsId("fbNativeUnitID", getSnapshotString(snapshot, "fbNativeUnitID", ""));

                                    preferenceClass.setAdsStatus("ExitDialog_NativeAD", getSnapshotInt(snapshot, "ExitDialog_NativeAD", 0));
                                    preferenceClass.setAdsStatus("bannerAdStatus", getSnapshotInt(snapshot, "bannerAdStatus", 0));
                                    preferenceClass.setAdsStatus("interstitalAdStatus", getSnapshotInt(snapshot, "interstitalAdStatus", 0));
                                    preferenceClass.setAdsStatus("EditScreenAdCount", getSnapshotInt(snapshot, "EditScreenAdCount", 0));
                                    preferenceClass.setAdsStatus("BGSelectScreen_BannerAD", getSnapshotInt(snapshot, "BGSelectScreen_BannerAD", 0));
                                    preferenceClass.setAdsStatus("ReadymadePoste_BannerAD", getSnapshotInt(snapshot, "ReadymadePoste_BannerAD", 0));
                                    preferenceClass.setAdsStatus("CreateScreen_BannerAD", getSnapshotInt(snapshot, "CreateScreen_BannerAD", 1));
                                    preferenceClass.setAdsStatus("BrandPoster_BannerAD", getSnapshotInt(snapshot, "BrandPoster_BannerAD", getSnapshotInt(snapshot, "BrandPost_BannerAD", 1)));
                                    preferenceClass.setInt("MainScreen_Native", getSnapshotInt(snapshot, "MainScreen_Native", 0));

                                    preferenceClass.setAdsId("google_Rw_ID", getSnapshotString(snapshot, "google_Rw_ID", ""));
                                    preferenceClass.setAdsId("PremiumAdType", getSnapshotString(snapshot, "PremiumAdType", ""));

                                    Log.d("AdTracker", "--- ORIGINAL FIREBASE AD IDs ---");
                                    Log.d("AdTracker", "Original AppOpenID: " + appOpenId);
                                    Log.d("AdTracker", "Original BannerID: " + bannerId);
                                    Log.d("AdTracker", "Original InterstitialID: " + interstitialId);
                                    Log.d("AdTracker", "Original NativeID: " + nativeId);
                                    Log.d("AdTracker", "Original RewardVideoID: " + rewardId);
                                    Log.d("AdTracker", "----------------------------------");

                                    // DEBUG MODE PROTECTION: Override with AdMob Test IDs when running from Android Studio
                                    if (com.festival.flyer.postermaker.BuildConfig.DEBUG) {
                                        Log.d("AdTracker", "⚠️ DEBUG MODE DETECTED: Using Google Test Ad IDs for Safety!");
                                        preferenceClass.setDataType("AppOpenID", "ca-app-pub-3940256099942544/9257395921");
                                        preferenceClass.setAdsId("AppOpenID", "ca-app-pub-3940256099942544/9257395921");
                                        preferenceClass.setDataType("BannerAdunitID", "ca-app-pub-3940256099942544/6300978111");
                                        preferenceClass.setAdsId("BannerAdunitID", "ca-app-pub-3940256099942544/6300978111");
                                        preferenceClass.setDataType("CollapsibleBannerID", "ca-app-pub-3940256099942544/6300978111");
                                        preferenceClass.setAdsId("CollapsibleBannerID", "ca-app-pub-3940256099942544/6300978111");
                                        preferenceClass.setDataType("InterstitalAdunitID", "ca-app-pub-3940256099942544/1033173712");
                                        preferenceClass.setAdsId("InterstitalAdunitID", "ca-app-pub-3940256099942544/1033173712");
                                        preferenceClass.setDataType("NativeUnitID", "ca-app-pub-3940256099942544/2247696110");
                                        preferenceClass.setAdsId("NativeUnitID", "ca-app-pub-3940256099942544/2247696110");
                                        preferenceClass.setDataType("RewardVideoUnitID", "ca-app-pub-3940256099942544/5354046379");
                                        preferenceClass.setAdsId("RewardVideoUnitID", "ca-app-pub-3940256099942544/5354046379");
                                        preferenceClass.setAdsId("google_Rw_ID", "ca-app-pub-3940256099942544/5354046379");
                                    }

                                    MailER_AppOpenManager.AppOpenAdShow = getSnapshotInt(snapshot, "AppOpenAdShow", 0);
                                    com.festival.flyer.postermaker.adManager.MailER_AppOpenManager.APPOPEN_COOLDOWN_MS = getSnapshotInt(snapshot, "splashAdTimer", 60000);
                                    MailER_InterstitialAdManager.InterAdTimer = getSnapshotInt(snapshot, "InterAdTimer", 0);
                                    com.festival.flyer.postermaker.adManager.MailER_AppOpenManager.INTERSTITIAL_CLASH_MS = getSnapshotInt(snapshot, "InterAdTimer", 30000);

                                    int rawUpdateVal = getSnapshotInt(snapshot, "UpdateAvailable", 0);
                                    int rawForceUpdateVal = getSnapshotInt(snapshot, "ForceUpdate", 0);
                                    String rawUpdateVer = getSnapshotString(snapshot, "UpdateVersionName", "1.0");

                                    Log.e("FirebaseUpdateVal", "==================================================");
                                    Log.e("FirebaseUpdateVal", ">>> FIREBASE RAW READ: UpdateAvailable = " + rawUpdateVal + " | ForceUpdate = " + rawForceUpdateVal + " | UpdateVersionName = " + rawUpdateVer + " <<<");
                                    Log.e("FirebaseUpdateVal", "==================================================");

                                    preferenceClass.setInt("UpdateAvailable", rawUpdateVal);
                                    preferenceClass.setInt("ForceUpdate", rawForceUpdateVal);
                                    preferenceClass.setDataType("UpdateVersionName", rawUpdateVer);

                                    preferenceClass.setInt("download", getSnapshotInt(snapshot, "download", 0));
                                    preferenceClass.setInt("splashscreen", getSnapshotInt(snapshot, "splashscreen", 0));
                                    preferenceClass.setInt("SplashAppOpen", getSnapshotInt(snapshot, "SplashAppOpen", 1));
                                    preferenceClass.setInt("SplashFallbackInter", getSnapshotInt(snapshot, "SplashFallbackInter", 1));

                                    preferenceClass.setInt("IsEditScreenBannerAD", getSnapshotInt(snapshot, "IsEditScreenBannerAD", 0));
                                    preferenceClass.setInt("rv_count", getSnapshotInt(snapshot, "rv_count", 0));
                                    preferenceClass.setInt("First_rv_count", getSnapshotInt(snapshot, "First_rv_count", 0));
                                    preferenceClass.setInt("PremiumPostCount", getSnapshotInt(snapshot, "PremiumPostCount", 0));

                                    preferenceClass.setDataType("main_key", getSnapshotString(snapshot, "main_key", ""));
                                    preferenceClass.setDecryptionType(getSnapshotInt(snapshot, "decryptionType", 0));

                                    // Onboarding Interstitial Ad configurations from Firebase Realtime DB (if present)
                                    if (snapshot.hasChild("is_onboarding_inter_enabled")) {
                                        preferenceClass.setBoolean("is_onboarding_inter_enabled", getSnapshotInt(snapshot, "is_onboarding_inter_enabled", 1) == 1);
                                    }
                                    if (snapshot.hasChild("onboarding_inter_step_1")) {
                                        preferenceClass.setBoolean("onboarding_inter_step_1", getSnapshotInt(snapshot, "onboarding_inter_step_1", 1) == 1);
                                    }
                                    if (snapshot.hasChild("onboarding_inter_step_2")) {
                                        preferenceClass.setBoolean("onboarding_inter_step_2", getSnapshotInt(snapshot, "onboarding_inter_step_2", 0) == 1);
                                    }
                                    if (snapshot.hasChild("onboarding_inter_step_3")) {
                                        preferenceClass.setBoolean("onboarding_inter_step_3", getSnapshotInt(snapshot, "onboarding_inter_step_3", 0) == 1);
                                    }
                                    if (snapshot.hasChild("onboarding_inter_ad_id")) {
                                        String obAdId = getSnapshotString(snapshot, "onboarding_inter_ad_id", "");
                                        if (!obAdId.isEmpty()) {
                                            preferenceClass.setAdsId("OnboardingInterAdID", obAdId);
                                        }
                                    }

                                    // Remote Config Master Override: ensure Remote Config has 100% precedence over Realtime DB
                                    try {
                                        com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
                                        applyAllRemoteConfig(rc);
                                    } catch (Exception e) {
                                        Log.e("RemoteConfig", "Override error: " + e.getMessage());
                                    }

                                    // Mark Firebase ads as loaded - fallback IDs will no longer be used
                                    preferenceClass.setFirebaseAdsLoaded();

                                    save_token(true);
                                } catch (Exception e) {
                                    Log.e("FIREBASE_PARSING_ERROR", "Error parsing ad_data: " + e.getMessage());
                                    save_token(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                save_token(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FIREBASE_PARSING_ERROR", "Error parsing datas: " + e.getMessage());
                        save_token(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    startIntent();
                }
            });
        } else {
            startIntent();
        }
    }

    private void save_token(boolean update) {
        boolean isSplashAppOpenEnabled = preferenceClass.getBoolean("splash_app_open_enabled", true)
                && (preferenceClass.getInt("SplashAppOpen", 1) == 1)
                && (MailER_AppOpenManager.AppOpenAdShow == 1);
        if (isSplashAppOpenEnabled) {
            try { ((MyApplication) getApplicationContext()).appOpenManager.fetchAd(); } catch (Exception e) {}
        }
        try { ((MyApplication) getApplicationContext()).getInterstitialAdManager().loadAd(); } catch (Exception ignored) {}

        isDataLoaded = true;
        if (update) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            Calendar calender = Calendar.getInstance();
            String start_date = simpleDateFormat.format(calender.getTime());
            preferenceClass.setFirstDate(start_date);
        }

        if (preferenceClass.isFirstTimeLaunch()) {
            preferenceClass.setFirstTimeLaunch(false);
        }

        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            applyAllRemoteConfig(rc);
        } catch (Exception ignored) {}

        String firebaseVerStr = preferenceClass.getDataType("UpdateVersionName", "0");
        boolean isUpdateRequired = isVersionHigher(firebaseVerStr, BuildConfig.VERSION_NAME);

        int updateAvailable = preferenceClass.getInt("UpdateAvailable");
        int forceUpdate = preferenceClass.getInt("ForceUpdate");

        boolean isUpdateEnabled = (updateAvailable == 1);
        boolean isForce = (forceUpdate == 1);
        boolean shouldShowUpdateDialog = isUpdateRequired && isUpdateEnabled;

        Log.e("FirebaseUpdateVal", "==================================================");
        Log.e("FirebaseUpdateVal", "READ PREF UpdateAvailable: " + updateAvailable + " | ForceUpdate: " + forceUpdate);
        Log.e("FirebaseUpdateVal", "Firebase UpdateVersionName: " + firebaseVerStr + " | App BuildConfig.VERSION_NAME: " + BuildConfig.VERSION_NAME);
        Log.e("FirebaseUpdateVal", "isUpdateRequired: " + isUpdateRequired + " | isForce: " + isForce);
        Log.e("FirebaseUpdateVal", "Will show update dialog: " + shouldShowUpdateDialog);
        Log.e("FirebaseUpdateVal", "==================================================");

        if (shouldShowUpdateDialog) {
            if (isFinishing() || isDestroyed() || (dialog != null && dialog.isShowing())) return;

            dialog = new Dialog(MailER_SplashScreen.this);
            dialog.setContentView(R.layout.spawner_dialog_app_info);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            TextView descriptionTextView = dialog.findViewById(R.id.descriptionTextView);
            Button cancelBtn = dialog.findViewById(R.id.dialogCancelButton);
            TextView msgTextView = dialog.findViewById(R.id.titleTextView);
            Button okBtn = dialog.findViewById(R.id.dialogOkButton);
            descriptionTextView.setTextColor(getResources().getColor(R.color.black));
            String customDesc = preferenceClass.getDataType("update_dialog_desc", "");
            String updateMsg;
            if (!customDesc.trim().isEmpty()) {
                updateMsg = customDesc.replace("{version}", firebaseVerStr);
            } else {
                updateMsg = "A huge new update (Version " + firebaseVerStr + ") is available!\n\nPlease update the app immediately to unlock New Posters, Backgrounds, and all latest features. The old version will no longer receive new data.";
            }
            descriptionTextView.setText(updateMsg);
            msgTextView.setTextColor(getResources().getColor(R.color.black));
            String customTitle = preferenceClass.getDataType("update_dialog_title", "Update Available");
            msgTextView.setText(customTitle.trim().isEmpty() ? "Update Available" : customTitle);
            okBtn.setText("Update");
            cancelBtn.setText("Cancel");

            if (isForce) {
                // Force Update: Hide Cancel button, disable touch outside, exit app on BACK press
                cancelBtn.setVisibility(View.GONE);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            finishAffinity();
                        }
                        return true;
                    }
                    return false;
                });
            } else {
                // Optional Update (1): Show Cancel button & handle BACK button / dismiss to proceed to Home Screen
                cancelBtn.setVisibility(View.VISIBLE);
                dialog.setCancelable(true);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        startIntent();
                    }
                });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        startIntent();
                    }
                });
            }

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isUpdateClicked = true;
                    final String appPackageName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException e) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        } catch (Exception ex) {
                            Log.e("SPLASH", "Error opening Play Store: " + ex.getMessage());
                        }
                    }
                    if (!isForce) {
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();

        } else {
            startIntent();
        }

    }

    private void startIntent() {
        callMainActivity();
    }

    public void callMainActivity() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        if (elapsedTime < MIN_SPLASH_TIME) {
            new android.os.Handler().postDelayed(this::performNavigation, MIN_SPLASH_TIME - elapsedTime);
        } else {
            performNavigation();
        }
    }

    private void performNavigation() {
        MyApplication.isAdsSplash = false;

        boolean isAppOpenEnabled = preferenceClass.getBoolean("splash_app_open_enabled", true)
                && (preferenceClass.getInt("SplashAppOpen", 1) == 1)
                && (MailER_AppOpenManager.AppOpenAdShow == 1);
        boolean isFallbackInterEnabled = preferenceClass.getBoolean("splash_fallback_interstitial_enabled", true)
                && (preferenceClass.getInt("SplashFallbackInter", 1) == 1);

        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterConfigStr = MailER_PreferenceClass.getMasterConfigJson(rc);
            if (masterConfigStr != null && !masterConfigStr.trim().isEmpty()) {
                org.json.JSONObject root = new org.json.JSONObject(masterConfigStr);
                if (root.has("splash")) {
                    org.json.JSONObject obj = root.getJSONObject("splash");
                    if (obj.has("app_open_ad")) {
                        isAppOpenEnabled = obj.getJSONObject("app_open_ad").optBoolean("enabled", true);
                    }
                    if (obj.has("fallback_interstitial_ad")) {
                        isFallbackInterEnabled = obj.getJSONObject("fallback_interstitial_ad").optBoolean("enabled", true);
                    }
                }
            } else {
                String splashConfigStr = rc.getString("splash_ad_config");
                if (splashConfigStr != null && !splashConfigStr.trim().isEmpty()) {
                    org.json.JSONObject obj = new org.json.JSONObject(splashConfigStr);
                    if (obj.has("splash_screen")) {
                        obj = obj.getJSONObject("splash_screen");
                    }
                    if (obj.has("app_open_ad")) {
                        isAppOpenEnabled = obj.getJSONObject("app_open_ad").optBoolean("enabled", true);
                    } else {
                        isAppOpenEnabled = obj.optBoolean("app_open_enabled", true);
                    }
                    if (obj.has("fallback_interstitial_ad")) {
                        isFallbackInterEnabled = obj.getJSONObject("fallback_interstitial_ad").optBoolean("enabled", true);
                    } else {
                        isFallbackInterEnabled = obj.optBoolean("fallback_interstitial_enabled", true);
                    }
                } else {
                    int isSplashAdEnabled = preferenceClass.getInt("splashscreen");
                    int isAppOpenAdEnabled = com.festival.flyer.postermaker.adManager.MailER_AppOpenManager.AppOpenAdShow;
                    int splashAppOpen = preferenceClass.getInt("SplashAppOpen", 1);
                    int splashFallbackInter = preferenceClass.getInt("SplashFallbackInter", 1);
                    isAppOpenEnabled = (isAppOpenAdEnabled == 1 && splashAppOpen == 1);
                    isFallbackInterEnabled = (splashFallbackInter == 1 && isSplashAdEnabled == 1);
                }
            }
        } catch (Exception e) {
            int isSplashAdEnabled = preferenceClass.getInt("splashscreen");
            int isAppOpenAdEnabled = com.festival.flyer.postermaker.adManager.MailER_AppOpenManager.AppOpenAdShow;
            int splashAppOpen = preferenceClass.getInt("SplashAppOpen", 1);
            int splashFallbackInter = preferenceClass.getInt("SplashFallbackInter", 1);
            isAppOpenEnabled = (isAppOpenAdEnabled == 1 && splashAppOpen == 1);
            isFallbackInterEnabled = (splashFallbackInter == 1 && isSplashAdEnabled == 1);
        }

        boolean isAppOpenLoaded = ((MyApplication) getApplicationContext()).appOpenManager.isAdAvailable();
        
        // Check if Onboarding is done
        boolean isOnboardingDone = preferenceClass.getRateSubmited("isOnboardingDone");
        Class<?> targetActivity;
        if (isOnboardingDone) {
            boolean isLoadingScreenEnabled = preferenceClass.getBoolean("loading_screen_enabled", true);
            boolean showAfterSplash = preferenceClass.getBoolean("loading_screen_show_after_splash", true);
            targetActivity = (isLoadingScreenEnabled && showAfterSplash) ? MailER_AppLoadingActivity.class : MailER_PosterMainActivity.class;
        } else {
            targetActivity = MailER_OnboardingActivity.class;
        }

        if (isAppOpenEnabled) {
            if (isAppOpenLoaded) {
                ((MyApplication) getApplicationContext()).appOpenManager.showAdIfSplashAvailable(this, () -> {
                    Intent intent = new Intent(getApplicationContext(), targetActivity);
                    startActivity(intent);
                    finish();
                });
            } else {
                // AppOpen Failed (Fallback to Interstitial)
                if (isFallbackInterEnabled) {
                    com.festival.flyer.postermaker.MyApplication.showInterstitialAdWithOutCount(this, () -> {
                        Intent intent = new Intent(getApplicationContext(), targetActivity);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Intent intent = new Intent(getApplicationContext(), targetActivity);
                    startActivity(intent);
                    finish();
                }
            }
        } else if (isFallbackInterEnabled) {
            com.festival.flyer.postermaker.MyApplication.showInterstitialAdWithOutCount(this, () -> {
                Intent intent = new Intent(getApplicationContext(), targetActivity);
                startActivity(intent);
                finish();
            });
        } else {
            Intent intent = new Intent(getApplicationContext(), targetActivity);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x11) {
            if (MailER_NetworkUtils.isNetworkAvailable(MailER_SplashScreen.this)) {
                startIntent();
//                    Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
//                    appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
//                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
//                            try {
//                                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, SplashScreen.this, 0x11);
//                            } catch (IntentSender.SendIntentException e) {
//                                MaterialDialogUtils.getInstance().errorDialog(SplashScreen.this, "Make sure you are connected to internet !!");
//                                e.printStackTrace();
//                            }
//                        } else {
//                            startIntent();
//                        }
//                    });
            } else {
                MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_SplashScreen.this, "Make sure you are connected to internet !!");
            }
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int forceUpdate = preferenceClass != null ? preferenceClass.getInt("ForceUpdate") : 0;
        if (isUpdateClicked && forceUpdate == 0) {
            isUpdateClicked = false;
            startIntent();
        }
    }

    private boolean isVersionHigher(String newVersion, String currentVersion) {
        if (newVersion == null || currentVersion == null) return false;
        String[] newParts = newVersion.trim().split("\\.");
        String[] currentParts = currentVersion.trim().split("\\.");
        int length = Math.max(newParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int newPart = i < newParts.length ? parseVersionPart(newParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
            if (newPart > currentPart) return true;
            if (newPart < currentPart) return false;
        }
        return false;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void fetchRemoteConfig() {
        try {
            FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 0 : 3600)
                    .build();
            remoteConfig.setConfigSettingsAsync(configSettings);

            String defaultSplashJson = "{\n" +
                    "  \"splash_screen\": {\n" +
                    "    \"app_open_ad\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"ad_id\": \"\"\n" +
                    "    },\n" +
                    "    \"fallback_interstitial_ad\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"ad_id\": \"\"\n" +
                    "    },\n" +
                    "    \"foreground_app_open\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"ad_id\": \"\",\n" +
                    "      \"cooldown_seconds\": 60\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            String defaultOnboardingJson = "{\n" +
                    "  \"onboarding_1\": {\n" +
                    "    \"show_native_ad\": true,\n" +
                    "    \"native_ad_id\": \"\",\n" +
                    "    \"ad_type\": \"compact\",\n" +
                    "    \"next_interstitial\": true\n" +
                    "  },\n" +
                    "  \"onboarding_2\": {\n" +
                    "    \"show_native_ad\": true,\n" +
                    "    \"native_ad_id\": \"\",\n" +
                    "    \"ad_type\": \"medium\",\n" +
                    "    \"next_interstitial\": true\n" +
                    "  },\n" +
                    "  \"onboarding_3\": {\n" +
                    "    \"show_native_ad\": true,\n" +
                    "    \"native_ad_id\": \"\",\n" +
                    "    \"ad_type\": \"medium\",\n" +
                    "    \"height\": 140,\n" +
                    "    \"next_interstitial\": true\n" +
                    "  }\n" +
                    "}";

            String defaultMasterJson = "{\n" +
                    "  \"splash\": {\n" +
                    "    \"app_open_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"fallback_interstitial_ad\": { \"enabled\": true, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"foreground_app_open\": {\n" +
                    "    \"enabled\": true,\n" +
                    "    \"ad_id\": \"\",\n" +
                    "    \"cooldown_seconds\": 60,\n" +
                    "    \"suppress_on_ad_click\": true,\n" +
                    "    \"suppress_on_intent\": true\n" +
                    "  },\n" +
                    "  \"onboarding\": {\n" +
                    "    \"is_interstitial_enabled\": true,\n" +
                    "    \"onboarding_1\": { \"show_native_ad\": true, \"ad_type\": \"compact\", \"height\": 90, \"native_ad_id\": \"\", \"next_interstitial\": true },\n" +
                    "    \"onboarding_2\": { \"show_native_ad\": true, \"ad_type\": \"medium\", \"height\": 140, \"native_ad_id\": \"\", \"next_interstitial\": true },\n" +
                    "    \"onboarding_3\": { \"show_native_ad\": true, \"ad_type\": \"medium\", \"height\": 140, \"native_ad_id\": \"\", \"next_interstitial\": true }\n" +
                    "  },\n" +
                    "  \"home\": {\n" +
                    "    \"enabled\": true,\n" +
                    "    \"ad_type\": \"banner\",\n" +
                    "    \"ad_id\": \"\"\n" +
                    "  },\n" +
                    "  \"templates\": {\n" +
                    "    \"banner_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"preview_banner_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"native_ad\": { \"enabled\": true, \"first_ad_after\": 4, \"repeat_ad_after\": 6, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"backgrounds\": {\n" +
                    "    \"banner_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"preview_banner_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"native_ad\": { \"enabled\": true, \"first_ad_after\": 4, \"repeat_ad_after\": 6, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"create\": {\n" +
                    "    \"enabled\": true,\n" +
                    "    \"ad_type\": \"banner\",\n" +
                    "    \"ad_id\": \"\",\n" +
                    "    \"banner_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"collapsible_banner\": { \"enabled\": false, \"ad_id\": \"\" },\n" +
                    "    \"interstitial\": { \"enabled\": true, \"click_interval\": 2, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"custom_category\": {\n" +
                    "    \"banner_ad\": { \"enabled\": true, \"ad_id\": \"\" },\n" +
                    "    \"native_ad\": { \"enabled\": true, \"first_ad_after\": 4, \"repeat_ad_after\": 6, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"my_creation\": {\n" +
                    "    \"banner_ad\": { \"enabled\": false, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"editor\": {\n" +
                    "    \"banner_ad\": { \"enabled\": false, \"ad_id\": \"\" },\n" +
                    "    \"exit_dialog_native\": { \"enabled\": true, \"ad_id\": \"\", \"ad_type\": \"medium\" },\n" +
                    "    \"reward_timer\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"timer_seconds\": 60,\n" +
                    "      \"ad_id\": \"\"\n" +
                    "    },\n" +
                    "    \"download\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"free_downloads_allowed\": 2,\n" +
                    "      \"ask_watermark_removal\": true,\n" +
                    "      \"ad_type\": \"reward\",\n" +
                    "      \"ad_id\": \"\"\n" +
                    "    },\n" +
                    "    \"undo_redo\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"free_actions_allowed\": 3,\n" +
                    "      \"ad_type\": \"reward\",\n" +
                    "      \"ad_id\": \"\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"interstitial\": {\n" +
                    "    \"enabled\": true,\n" +
                    "    \"count\": 4,\n" +
                    "    \"ad_type\": \"interstitial\",\n" +
                    "    \"ad_id\": \"\"\n" +
                    "  },\n" +
                    "  \"loading_screen\": {\n" +
                    "    \"enabled\": true,\n" +
                    "    \"show_after_splash\": true,\n" +
                    "    \"show_after_onboarding\": true,\n" +
                    "    \"timer_seconds\": 4,\n" +
                    "    \"native_ad\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"height\": 280,\n" +
                    "      \"type\": \"video\",\n" +
                    "      \"ad_id\": \"\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"banner\": {\n" +
                    "    \"global_enabled\": true,\n" +
                    "    \"ad_id\": \"\"\n" +
                    "  },\n" +
                    "  \"share\": {\n" +
                    "    \"banner_ad\": { \"enabled\": true, \"ad_id\": \"\" }\n" +
                    "  },\n" +
                    "  \"premium_config\": {\n" +
                    "    \"templates\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"interval\": 3,\n" +
                    "      \"ad_type\": \"reward\",\n" +
                    "      \"ad_enabled\": true,\n" +
                    "      \"reward_ad_id\": \"\",\n" +
                    "      \"interstitial_ad_id\": \"\"\n" +
                    "    },\n" +
                    "    \"background\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"interval\": 3,\n" +
                    "      \"ad_type\": \"reward\",\n" +
                    "      \"ad_enabled\": true,\n" +
                    "      \"reward_ad_id\": \"\",\n" +
                    "      \"interstitial_ad_id\": \"\"\n" +
                    "    },\n" +
                    "    \"branding_poster\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"interval\": 3,\n" +
                    "      \"ad_type\": \"reward\",\n" +
                    "      \"ad_enabled\": true,\n" +
                    "      \"reward_ad_id\": \"\",\n" +
                    "      \"interstitial_ad_id\": \"\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"app_exit_dialog\": {\n" +
                    "    \"enabled\": true,\n" +
                    "    \"native_ad\": {\n" +
                    "      \"enabled\": true,\n" +
                    "      \"ad_id\": \"\",\n" +
                    "      \"ad_type\": \"medium\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"suppress_app_open_on_ad_click\": true,\n" +
                    "  \"suppress_app_open_on_intent\": true,\n" +
                    "  \"banner_show_shimmer_on_fail\": true,\n" +
                    "  \"native_show_shimmer_on_fail\": true,\n" +
                    "  \"screenshot_enabled\": true,\n" +
                    "  \"app_update\": {\n" +
                    "    \"update_available\": false,\n" +
                    "    \"force_update\": false,\n" +
                    "    \"update_version_name\": \"1.0\",\n" +
                    "    \"update_title\": \"Update Available\",\n" +
                    "    \"update_description\": \"\"\n" +
                    "  }\n" +
                    "}";

            Map<String, Object> defaults = new HashMap<>();
            defaults.put("app_ad_config", defaultMasterJson);
            defaults.put("postermaker", defaultMasterJson);
            defaults.put("splash_ad_config", defaultSplashJson);
            defaults.put("onboarding_config", defaultOnboardingJson);
            defaults.put("app_exit_dialog_enabled", true);
            defaults.put("app_exit_native_enabled", true);
            defaults.put("app_exit_native_id", "");
            defaults.put("app_exit_native_type", "medium");
            defaults.put("interstitial_enabled", true);
            defaults.put("interstitial_count", 4);
            defaults.put("interstitial_click_interval", 4);
            defaults.put("interstitial_ad_type", "interstitial");
            defaults.put("interstitial_ad_id", "");
            defaults.put("templates_premium_enabled", true);
            defaults.put("templates_premium_interval", 3);
            defaults.put("templates_premium_ad_type", "reward");
            defaults.put("templates_premium_ad_enabled", true);
            defaults.put("templates_reward_ad_id", "");
            defaults.put("background_premium_enabled", true);
            defaults.put("background_premium_interval", 3);
            defaults.put("background_premium_ad_type", "reward");
            defaults.put("background_premium_ad_enabled", true);
            defaults.put("background_reward_ad_id", "");
            defaults.put("branding_premium_enabled", true);
            defaults.put("branding_premium_interval", 3);
            defaults.put("branding_premium_ad_type", "reward");
            defaults.put("branding_premium_ad_enabled", true);
            defaults.put("branding_reward_ad_id", "");
            defaults.put("premium_enabled", true);
            defaults.put("premium_post_interval", 3);
            defaults.put("PremiumPostCount", 3);
            defaults.put("premium_ad_type", "reward");
            defaults.put("PremiumAdType", "reward");
            defaults.put("premium_ad_enabled", true);
            defaults.put("premium_reward_ad_id", "");
            defaults.put("premium_interstitial_ad_id", "");
            defaults.put("premium_dialog_native_enabled", true);
            defaults.put("premium_dialog_native_id", "");
            defaults.put("share_banner_ad_enabled", true);
            defaults.put("share_banner_ad_id", "");
            defaults.put("preview_poster_banner_enabled", true);
            defaults.put("preview_poster_banner_ad_id", "");
            defaults.put("preview_poster_native_enabled", true);
            defaults.put("preview_poster_native_ad_id", "");
            defaults.put("preview_poster_native_type", "compact");
            defaults.put("preview_poster_native_height", 100L);
            defaults.put("preview_bg_banner_enabled", true);
            defaults.put("preview_bg_banner_ad_id", "");
            defaults.put("preview_bg_native_enabled", true);
            defaults.put("preview_bg_native_ad_id", "");
            defaults.put("preview_bg_native_type", "compact");
            defaults.put("preview_bg_native_height", 100L);
            defaults.put("is_onboarding_inter_enabled", true);
            defaults.put("onboarding_inter_step_1", true);
            defaults.put("onboarding_inter_step_2", true);
            defaults.put("onboarding_inter_step_3", true);
            defaults.put("onboarding_inter_ad_id", "");
            defaults.put("editor_reward_timer_enabled", true);
            defaults.put("editor_reward_timer_seconds", 60);
            defaults.put("editor_reward_ad_id", "");
            defaults.put("editor_download_ad_enabled", true);
            defaults.put("editor_free_downloads_allowed", 2);
            defaults.put("editor_ask_watermark_removal", true);
            defaults.put("editor_download_ad_type", "reward");
            defaults.put("editor_download_ad_id", "");
            defaults.put("editor_undo_redo_ad_enabled", true);
            defaults.put("editor_free_undo_redo_allowed", 3);
            defaults.put("editor_undo_redo_ad_type", "reward");
            defaults.put("editor_undo_redo_ad_id", "");
            defaults.put("create_banner_ad_enabled", true);
            defaults.put("create_banner_ad_type", "banner");
            defaults.put("create_banner_ad_id", "");
            defaults.put("create_interstitial_enabled", true);
            defaults.put("create_interstitial_interval", 2);
            defaults.put("create_interstitial_ad_id", "");
            defaults.put("editor_exit_dialog_native_enabled", true);
            defaults.put("editor_exit_dialog_native_id", "");
            defaults.put("editor_exit_dialog_native_type", "medium");
            defaults.put("my_creation_banner_ad_enabled", false);
            defaults.put("my_creation_banner_ad_id", "");
            defaults.put("custom_category_banner_ad_enabled", true);
            defaults.put("custom_category_banner_ad_id", "");
            defaults.put("custom_category_native_enabled", true);
            defaults.put("custom_category_native_ad_id", "");
            defaults.put("custom_category_first_rv_count", 4);
            defaults.put("custom_category_rv_count", 6);
            defaults.put("foreground_app_open_enabled", true);
            defaults.put("foreground_app_open_ad_id", "");
            defaults.put("foreground_app_open_cooldown", 60L);
            defaults.put("suppress_app_open_on_ad_click", true);
            defaults.put("suppress_app_open_on_intent", true);
            defaults.put("screenshot_enabled", true);
            defaults.put("banner_show_shimmer_on_fail", true);
            defaults.put("native_show_shimmer_on_fail", true);
            defaults.put("UpdateAvailable", 0);
            defaults.put("ForceUpdate", 0);
            defaults.put("UpdateVersionName", "1.0");
            defaults.put("update_available", false);
            defaults.put("force_update", false);
            defaults.put("update_version_name", "1.0");
            remoteConfig.setDefaultsAsync(defaults);

            // Apply defaults immediately so preference flags are initialized safely
            parseMasterConfig(defaultMasterJson);

            remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    applyAllRemoteConfig(remoteConfig);
                    Log.d("RemoteConfig", "Remote Config loaded successfully!");
                } else {
                    Log.d("RemoteConfig", "Remote Config fetch failed or unchanged");
                }
            });

            remoteConfig.addOnConfigUpdateListener(new com.google.firebase.remoteconfig.ConfigUpdateListener() {
                @Override
                public void onUpdate(@NonNull com.google.firebase.remoteconfig.ConfigUpdate configUpdate) {
                    Log.d("RemoteConfig", "Real-time config update received: " + configUpdate.getUpdatedKeys());
                    remoteConfig.activate().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            applyAllRemoteConfig(remoteConfig);
                            Log.d("RemoteConfig", "Real-time Remote Config activated and applied!");
                        }
                    });
                }

                @Override
                public void onError(@NonNull com.google.firebase.remoteconfig.FirebaseRemoteConfigException error) {
                    Log.e("RemoteConfig", "Real-time config update error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("RemoteConfig", "Remote Config exception: " + e.getMessage());
        }
    }

    private boolean isRemoteKey(FirebaseRemoteConfig rc, String key, boolean isStandaloneAllowed) {
        if (rc == null || key == null) return false;
        try {
            if (rc.getValue(key).getSource() == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                return true;
            }
        } catch (Exception ignored) {}
        return isStandaloneAllowed;
    }

    private void applyAllRemoteConfig(FirebaseRemoteConfig remoteConfig) {
        if (remoteConfig == null) return;
        try {
            String masterConfigStr = null;
            if (remoteConfig.getValue("postermaker").getSource() == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                masterConfigStr = remoteConfig.getString("postermaker");
            } else if (remoteConfig.getValue("app_ad_config").getSource() == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                masterConfigStr = remoteConfig.getString("app_ad_config");
            } else {
                masterConfigStr = remoteConfig.getString("postermaker");
                if (masterConfigStr == null || masterConfigStr.trim().isEmpty()) {
                    masterConfigStr = remoteConfig.getString("app_ad_config");
                }
            }
            boolean hasMasterConfig = (masterConfigStr != null && !masterConfigStr.trim().isEmpty());
            if (hasMasterConfig) {
                Log.d("RemoteConfig", "Using Master Config: " + masterConfigStr);
                parseMasterConfig(masterConfigStr);
            }

            // Standalone fallback/override: ONLY apply if key came from remote server (VALUE_SOURCE_REMOTE)
            // or if masterConfigStr was absent (fallback to legacy individual keys).
            boolean isStandaloneAllowed = !hasMasterConfig;

            if (isRemoteKey(remoteConfig, "screenshot_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("screenshot_enabled", remoteConfig.getBoolean("screenshot_enabled"));
                com.festival.flyer.postermaker.MyApplication.applyScreenshotSecurity(MailER_SplashScreen.this);
            } else if (isRemoteKey(remoteConfig, "allow_screenshot", isStandaloneAllowed)) {
                preferenceClass.setBoolean("screenshot_enabled", remoteConfig.getBoolean("allow_screenshot"));
                com.festival.flyer.postermaker.MyApplication.applyScreenshotSecurity(MailER_SplashScreen.this);
            } else if (isRemoteKey(remoteConfig, "disable_screenshots", isStandaloneAllowed)) {
                preferenceClass.setBoolean("screenshot_enabled", !remoteConfig.getBoolean("disable_screenshots"));
                com.festival.flyer.postermaker.MyApplication.applyScreenshotSecurity(MailER_SplashScreen.this);
            }

            if (!hasMasterConfig) {
                String splashConfigStr = remoteConfig.getString("splash_ad_config");
                if (splashConfigStr != null && !splashConfigStr.trim().isEmpty()) {
                    try {
                        org.json.JSONObject splashObj = new org.json.JSONObject(splashConfigStr);
                        if (splashObj.has("splash_screen")) {
                            splashObj = splashObj.getJSONObject("splash_screen");
                        }

                        boolean appOpenEnabled = true;
                        String appOpenId = "";
                        if (splashObj.has("app_open_ad")) {
                            org.json.JSONObject appOpenObj = splashObj.getJSONObject("app_open_ad");
                            appOpenEnabled = appOpenObj.optBoolean("enabled", true);
                            appOpenId = appOpenObj.optString("ad_id", "");
                        } else {
                            appOpenEnabled = splashObj.optBoolean("app_open_enabled", true);
                            appOpenId = splashObj.optString("app_open_ad_id", "");
                        }

                        boolean fallbackInterEnabled = true;
                        String interId = "";
                        if (splashObj.has("fallback_interstitial_ad")) {
                            org.json.JSONObject interObj = splashObj.getJSONObject("fallback_interstitial_ad");
                            fallbackInterEnabled = interObj.optBoolean("enabled", true);
                            interId = interObj.optString("ad_id", "");
                        } else {
                            fallbackInterEnabled = splashObj.optBoolean("fallback_interstitial_enabled", true);
                            interId = splashObj.optString("interstitial_ad_id", "");
                        }

                        if (splashObj.has("foreground_app_open")) {
                            org.json.JSONObject fgObj = splashObj.getJSONObject("foreground_app_open");
                            preferenceClass.setBoolean("foreground_app_open_enabled", fgObj.optBoolean("enabled", true));
                            preferenceClass.setInt("foreground_app_open_cooldown", fgObj.optInt("cooldown_seconds", 60));
                            if (fgObj.has("ad_id") && !fgObj.optString("ad_id").trim().isEmpty()) {
                                preferenceClass.setAdsId("ForegroundAppOpenID", fgObj.optString("ad_id").trim());
                            }
                        } else if (splashObj.has("app_resume_ad")) {
                            org.json.JSONObject fgObj = splashObj.getJSONObject("app_resume_ad");
                            preferenceClass.setBoolean("foreground_app_open_enabled", fgObj.optBoolean("enabled", true));
                            preferenceClass.setInt("foreground_app_open_cooldown", fgObj.optInt("cooldown_seconds", 60));
                            if (fgObj.has("ad_id") && !fgObj.optString("ad_id").trim().isEmpty()) {
                                preferenceClass.setAdsId("ForegroundAppOpenID", fgObj.optString("ad_id").trim());
                            }
                        }

                        preferenceClass.setBoolean("splash_app_open_enabled", appOpenEnabled);
                        preferenceClass.setBoolean("splash_fallback_interstitial_enabled", fallbackInterEnabled);
                        MailER_AppOpenManager.AppOpenAdShow = appOpenEnabled ? 1 : 0;
                        preferenceClass.setInt("SplashAppOpen", appOpenEnabled ? 1 : 0);
                        preferenceClass.setInt("SplashFallbackInter", fallbackInterEnabled ? 1 : 0);

                        if (!appOpenId.trim().isEmpty()) {
                            preferenceClass.setAdsId("AppOpenID", appOpenId.trim());
                        }
                        if (!interId.trim().isEmpty()) {
                            preferenceClass.setAdsId("InterstitalAdunitID", interId.trim());
                        }
                    } catch (Exception ex) {
                        Log.e("RemoteConfig", "Error parsing splash_ad_config: " + ex.getMessage());
                    }
                }
            }

            // Standalone Foreground / Background App Open keys
            if (isRemoteKey(remoteConfig, "foreground_app_open_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("foreground_app_open_enabled", remoteConfig.getBoolean("foreground_app_open_enabled"));
            } else if (isRemoteKey(remoteConfig, "app_resume_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("foreground_app_open_enabled", remoteConfig.getBoolean("app_resume_ad_enabled"));
            }

            if (isRemoteKey(remoteConfig, "foreground_app_open_ad_id", isStandaloneAllowed)) {
                String standaloneFgId = remoteConfig.getString("foreground_app_open_ad_id");
                if (standaloneFgId.isEmpty()) {
                    standaloneFgId = remoteConfig.getString("app_resume_ad_id");
                }
                if (!standaloneFgId.trim().isEmpty()) {
                    preferenceClass.setAdsId("ForegroundAppOpenID", standaloneFgId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "foreground_app_open_cooldown", isStandaloneAllowed)) {
                long standaloneCooldown = remoteConfig.getLong("foreground_app_open_cooldown");
                if (standaloneCooldown <= 0) {
                    standaloneCooldown = remoteConfig.getLong("app_resume_cooldown_seconds");
                }
                if (standaloneCooldown > 0) {
                    preferenceClass.setInt("foreground_app_open_cooldown", (int) standaloneCooldown);
                }
            }

            if (isRemoteKey(remoteConfig, "suppress_app_open_on_ad_click", isStandaloneAllowed)) {
                preferenceClass.setBoolean("suppress_app_open_on_ad_click", remoteConfig.getBoolean("suppress_app_open_on_ad_click"));
            }
            if (isRemoteKey(remoteConfig, "suppress_app_open_on_intent", isStandaloneAllowed)) {
                preferenceClass.setBoolean("suppress_app_open_on_intent", remoteConfig.getBoolean("suppress_app_open_on_intent"));
            }

            if (!hasMasterConfig) {
                String onboardingConfigStr = remoteConfig.getString("onboarding_config");
                if (onboardingConfigStr != null && !onboardingConfigStr.trim().isEmpty()) {
                    preferenceClass.setDataType("onboarding_config", onboardingConfigStr);
                }
            }

            if (isRemoteKey(remoteConfig, "is_onboarding_inter_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("is_onboarding_inter_enabled", remoteConfig.getBoolean("is_onboarding_inter_enabled"));
            }
            if (isRemoteKey(remoteConfig, "onboarding_inter_step_1", isStandaloneAllowed)) {
                preferenceClass.setBoolean("onboarding_inter_step_1", remoteConfig.getBoolean("onboarding_inter_step_1"));
            }
            if (isRemoteKey(remoteConfig, "onboarding_inter_step_2", isStandaloneAllowed)) {
                preferenceClass.setBoolean("onboarding_inter_step_2", remoteConfig.getBoolean("onboarding_inter_step_2"));
            }
            if (isRemoteKey(remoteConfig, "onboarding_inter_step_3", isStandaloneAllowed)) {
                preferenceClass.setBoolean("onboarding_inter_step_3", remoteConfig.getBoolean("onboarding_inter_step_3"));
            }
            if (isRemoteKey(remoteConfig, "onboarding_inter_ad_id", isStandaloneAllowed)) {
                String onboardingAdId = remoteConfig.getString("onboarding_inter_ad_id");
                if (onboardingAdId != null && !onboardingAdId.trim().isEmpty()) {
                    preferenceClass.setAdsId("OnboardingInterAdID", onboardingAdId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "editor_reward_timer_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("editor_reward_timer_enabled", remoteConfig.getBoolean("editor_reward_timer_enabled"));
            }
            if (isRemoteKey(remoteConfig, "editor_reward_timer_seconds", isStandaloneAllowed)) {
                long editorRewardSeconds = remoteConfig.getLong("editor_reward_timer_seconds");
                if (editorRewardSeconds > 0) {
                    preferenceClass.setInt("editor_reward_timer_seconds", (int) editorRewardSeconds);
                }
            }
            if (isRemoteKey(remoteConfig, "editor_reward_ad_id", isStandaloneAllowed)) {
                String editorRewardAdId = remoteConfig.getString("editor_reward_ad_id");
                if (editorRewardAdId != null && !editorRewardAdId.trim().isEmpty()) {
                    preferenceClass.setAdsId("editor_reward_ad_id", editorRewardAdId.trim());
                    preferenceClass.setAdsId("RewardVideoUnitID", editorRewardAdId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "editor_download_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("editor_download_ad_enabled", remoteConfig.getBoolean("editor_download_ad_enabled"));
            }
            if (isRemoteKey(remoteConfig, "editor_free_downloads_allowed", isStandaloneAllowed)) {
                long freeDownloads = remoteConfig.getLong("editor_free_downloads_allowed");
                if (freeDownloads >= 0) {
                    preferenceClass.setInt("editor_free_downloads_allowed", (int) freeDownloads);
                }
            }
            if (isRemoteKey(remoteConfig, "editor_ask_watermark_removal", isStandaloneAllowed)) {
                preferenceClass.setBoolean("editor_ask_watermark_removal", remoteConfig.getBoolean("editor_ask_watermark_removal"));
            }
            if (isRemoteKey(remoteConfig, "editor_download_ad_type", isStandaloneAllowed)) {
                String dlAdType = remoteConfig.getString("editor_download_ad_type");
                if (dlAdType != null && !dlAdType.trim().isEmpty()) {
                    preferenceClass.setDataType("editor_download_ad_type", dlAdType.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "editor_download_ad_id", isStandaloneAllowed)) {
                String dlAdId = remoteConfig.getString("editor_download_ad_id");
                if (dlAdId != null && !dlAdId.trim().isEmpty()) {
                    preferenceClass.setAdsId("editor_download_ad_id", dlAdId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "editor_undo_redo_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("editor_undo_redo_ad_enabled", remoteConfig.getBoolean("editor_undo_redo_ad_enabled"));
            }
            if (isRemoteKey(remoteConfig, "editor_free_undo_redo_allowed", isStandaloneAllowed)) {
                long freeUndoRedo = remoteConfig.getLong("editor_free_undo_redo_allowed");
                if (freeUndoRedo >= 0) {
                    preferenceClass.setInt("editor_free_undo_redo_allowed", (int) freeUndoRedo);
                }
            }
            if (isRemoteKey(remoteConfig, "editor_undo_redo_ad_type", isStandaloneAllowed)) {
                String urAdType = remoteConfig.getString("editor_undo_redo_ad_type");
                if (urAdType != null && !urAdType.trim().isEmpty()) {
                    preferenceClass.setDataType("editor_undo_redo_ad_type", urAdType.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "editor_undo_redo_ad_id", isStandaloneAllowed)) {
                String urAdId = remoteConfig.getString("editor_undo_redo_ad_id");
                if (urAdId != null && !urAdId.trim().isEmpty()) {
                    preferenceClass.setAdsId("editor_undo_redo_ad_id", urAdId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "create_banner_ad_enabled", isStandaloneAllowed)) {
                boolean createBannerEnabled = remoteConfig.getBoolean("create_banner_ad_enabled");
                preferenceClass.setBoolean("create_banner_ad_enabled", createBannerEnabled);
                preferenceClass.setAdsStatus("CreateScreen_BannerAD", createBannerEnabled ? 1 : 0);
            }
            if (isRemoteKey(remoteConfig, "create_banner_ad_type", isStandaloneAllowed)) {
                String createBannerType = remoteConfig.getString("create_banner_ad_type");
                if (createBannerType != null && !createBannerType.trim().isEmpty()) {
                    preferenceClass.setDataType("create_banner_ad_type", createBannerType.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "create_banner_ad_id", isStandaloneAllowed)) {
                String createBannerId = remoteConfig.getString("create_banner_ad_id");
                if (createBannerId != null && !createBannerId.trim().isEmpty()) {
                    preferenceClass.setAdsId("create_banner_ad_id", createBannerId.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "create_interstitial_enabled", isStandaloneAllowed)) {
                boolean createInterEnabled = remoteConfig.getBoolean("create_interstitial_enabled");
                preferenceClass.setBoolean("interstitial_create_enabled", createInterEnabled);
            }
            if (isRemoteKey(remoteConfig, "create_interstitial_interval", isStandaloneAllowed)) {
                long createInterInterval = remoteConfig.getLong("create_interstitial_interval");
                if (createInterInterval > 0) {
                    preferenceClass.setInt("interstitial_create_interval", (int) createInterInterval);
                }
            }
            if (isRemoteKey(remoteConfig, "create_interstitial_ad_id", isStandaloneAllowed)) {
                String createInterId = remoteConfig.getString("create_interstitial_ad_id");
                if (createInterId != null && !createInterId.trim().isEmpty()) {
                    preferenceClass.setAdsId("create_interstitial_ad_id", createInterId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "editor_exit_dialog_native_enabled", isStandaloneAllowed)) {
                boolean editorExitNativeEnabled = remoteConfig.getBoolean("editor_exit_dialog_native_enabled");
                preferenceClass.setBoolean("editor_exit_dialog_native_enabled", editorExitNativeEnabled);
                preferenceClass.setAdsStatus("ExitDialog_NativeAD", editorExitNativeEnabled ? 1 : 0);
            }
            if (isRemoteKey(remoteConfig, "editor_exit_dialog_native_id", isStandaloneAllowed)) {
                String editorExitNativeId = remoteConfig.getString("editor_exit_dialog_native_id");
                if (editorExitNativeId != null && !editorExitNativeId.trim().isEmpty()) {
                    preferenceClass.setAdsId("editor_exit_dialog_native_id", editorExitNativeId.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "editor_exit_dialog_native_type", isStandaloneAllowed)) {
                String editorExitNativeType = remoteConfig.getString("editor_exit_dialog_native_type");
                if (editorExitNativeType != null && !editorExitNativeType.trim().isEmpty()) {
                    preferenceClass.setDataType("editor_exit_dialog_native_type", editorExitNativeType.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "my_creation_banner_ad_enabled", isStandaloneAllowed)) {
                boolean myCreationBannerEnabled = remoteConfig.getBoolean("my_creation_banner_ad_enabled");
                preferenceClass.setBoolean("my_creation_banner_ad_enabled", myCreationBannerEnabled);
            }
            if (isRemoteKey(remoteConfig, "my_creation_banner_ad_id", isStandaloneAllowed)) {
                String myCreationBannerId = remoteConfig.getString("my_creation_banner_ad_id");
                if (myCreationBannerId != null && !myCreationBannerId.trim().isEmpty()) {
                    preferenceClass.setAdsId("my_creation_banner_ad_id", myCreationBannerId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "custom_category_banner_ad_enabled", isStandaloneAllowed)) {
                boolean customCatBannerEnabled = remoteConfig.getBoolean("custom_category_banner_ad_enabled");
                preferenceClass.setBoolean("custom_category_banner_ad_enabled", customCatBannerEnabled);
                preferenceClass.setAdsStatus("BrandPoster_BannerAD", customCatBannerEnabled ? 1 : 0);
            }
            if (isRemoteKey(remoteConfig, "custom_category_banner_ad_id", isStandaloneAllowed)) {
                String customCatBannerId = remoteConfig.getString("custom_category_banner_ad_id");
                if (customCatBannerId != null && !customCatBannerId.trim().isEmpty()) {
                    preferenceClass.setAdsId("custom_category_banner_ad_id", customCatBannerId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "custom_category_native_enabled", isStandaloneAllowed)) {
                boolean customCatNativeEnabled = remoteConfig.getBoolean("custom_category_native_enabled");
                preferenceClass.setBoolean("custom_category_native_enabled", customCatNativeEnabled);
            }
            if (isRemoteKey(remoteConfig, "custom_category_native_ad_id", isStandaloneAllowed)) {
                String customCatNativeId = remoteConfig.getString("custom_category_native_ad_id");
                if (customCatNativeId != null && !customCatNativeId.trim().isEmpty()) {
                    preferenceClass.setAdsId("custom_category_native_ad_id", customCatNativeId.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "custom_category_first_rv_count", isStandaloneAllowed)) {
                long customCatFirstRv = remoteConfig.getLong("custom_category_first_rv_count");
                if (customCatFirstRv > 0) {
                    preferenceClass.setInt("custom_category_first_rv_count", (int) customCatFirstRv);
                }
            }
            if (isRemoteKey(remoteConfig, "custom_category_rv_count", isStandaloneAllowed)) {
                long customCatRv = remoteConfig.getLong("custom_category_rv_count");
                if (customCatRv > 0) {
                    preferenceClass.setInt("custom_category_rv_count", (int) customCatRv);
                }
            }

            // Templates Standalone Keys
            if (isRemoteKey(remoteConfig, "templates_premium_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("templates_premium_enabled", remoteConfig.getBoolean("templates_premium_enabled"));
            }
            if (isRemoteKey(remoteConfig, "templates_premium_interval", isStandaloneAllowed)) {
                long tInterval = remoteConfig.getLong("templates_premium_interval");
                if (tInterval > 0) preferenceClass.setInt("templates_premium_interval", (int) tInterval);
            }
            if (isRemoteKey(remoteConfig, "templates_premium_ad_type", isStandaloneAllowed)) {
                String tAdType = remoteConfig.getString("templates_premium_ad_type");
                if (tAdType != null && !tAdType.trim().isEmpty()) preferenceClass.setDataType("templates_premium_ad_type", tAdType.trim());
            }
            if (isRemoteKey(remoteConfig, "templates_premium_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("templates_premium_ad_enabled", remoteConfig.getBoolean("templates_premium_ad_enabled"));
            }
            if (isRemoteKey(remoteConfig, "templates_reward_ad_id", isStandaloneAllowed)) {
                String tRwId = remoteConfig.getString("templates_reward_ad_id");
                if (tRwId != null && !tRwId.trim().isEmpty()) preferenceClass.setAdsId("templates_reward_ad_id", tRwId.trim());
            }

            // Background Standalone Keys
            if (isRemoteKey(remoteConfig, "background_premium_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("background_premium_enabled", remoteConfig.getBoolean("background_premium_enabled"));
            }
            if (isRemoteKey(remoteConfig, "background_premium_interval", isStandaloneAllowed)) {
                long bInterval = remoteConfig.getLong("background_premium_interval");
                if (bInterval > 0) preferenceClass.setInt("background_premium_interval", (int) bInterval);
            }
            if (isRemoteKey(remoteConfig, "background_premium_ad_type", isStandaloneAllowed)) {
                String bAdType = remoteConfig.getString("background_premium_ad_type");
                if (bAdType != null && !bAdType.trim().isEmpty()) preferenceClass.setDataType("background_premium_ad_type", bAdType.trim());
            }
            if (isRemoteKey(remoteConfig, "background_premium_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("background_premium_ad_enabled", remoteConfig.getBoolean("background_premium_ad_enabled"));
            }
            if (isRemoteKey(remoteConfig, "background_reward_ad_id", isStandaloneAllowed)) {
                String bRwId = remoteConfig.getString("background_reward_ad_id");
                if (bRwId != null && !bRwId.trim().isEmpty()) preferenceClass.setAdsId("background_reward_ad_id", bRwId.trim());
            }

            // Branding Poster Standalone Keys
            if (isRemoteKey(remoteConfig, "branding_premium_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("branding_premium_enabled", remoteConfig.getBoolean("branding_premium_enabled"));
            }
            if (isRemoteKey(remoteConfig, "branding_premium_interval", isStandaloneAllowed)) {
                long brInterval = remoteConfig.getLong("branding_premium_interval");
                if (brInterval > 0) preferenceClass.setInt("branding_premium_interval", (int) brInterval);
            }
            if (isRemoteKey(remoteConfig, "branding_premium_ad_type", isStandaloneAllowed)) {
                String brAdType = remoteConfig.getString("branding_premium_ad_type");
                if (brAdType != null && !brAdType.trim().isEmpty()) preferenceClass.setDataType("branding_premium_ad_type", brAdType.trim());
            }
            if (isRemoteKey(remoteConfig, "branding_premium_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("branding_premium_ad_enabled", remoteConfig.getBoolean("branding_premium_ad_enabled"));
            }
            if (isRemoteKey(remoteConfig, "branding_reward_ad_id", isStandaloneAllowed)) {
                String brRwId = remoteConfig.getString("branding_reward_ad_id");
                if (brRwId != null && !brRwId.trim().isEmpty()) preferenceClass.setAdsId("branding_reward_ad_id", brRwId.trim());
            }

            // Legacy Common Premium Standalone Keys
            if (isRemoteKey(remoteConfig, "premium_enabled", isStandaloneAllowed)) {
                boolean premiumEnabled = remoteConfig.getBoolean("premium_enabled");
                preferenceClass.setBoolean("premium_enabled", premiumEnabled);
            }
            if (isRemoteKey(remoteConfig, "premium_post_interval", isStandaloneAllowed) || isRemoteKey(remoteConfig, "PremiumPostCount", isStandaloneAllowed)) {
                long premiumInterval = remoteConfig.getLong("premium_post_interval");
                if (premiumInterval <= 0) {
                    premiumInterval = remoteConfig.getLong("PremiumPostCount");
                }
                boolean pEn = preferenceClass.getBoolean("premium_enabled", true);
                preferenceClass.setInt("PremiumPostCount", pEn ? (premiumInterval > 0 ? (int) premiumInterval : 3) : 0);
            }
            if (isRemoteKey(remoteConfig, "premium_ad_type", isStandaloneAllowed) || isRemoteKey(remoteConfig, "PremiumAdType", isStandaloneAllowed)) {
                String premiumAdType = remoteConfig.getString("premium_ad_type");
                if (premiumAdType == null || premiumAdType.isEmpty()) {
                    premiumAdType = remoteConfig.getString("PremiumAdType");
                }
                if (premiumAdType != null && !premiumAdType.trim().isEmpty()) {
                    preferenceClass.setDataType("PremiumAdType", premiumAdType.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "premium_ad_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("premium_ad_enabled", remoteConfig.getBoolean("premium_ad_enabled"));
            }
            if (isRemoteKey(remoteConfig, "premium_reward_ad_id", isStandaloneAllowed)) {
                String premiumRwId = remoteConfig.getString("premium_reward_ad_id");
                if (premiumRwId != null && !premiumRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("premium_reward_ad_id", premiumRwId.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "premium_interstitial_ad_id", isStandaloneAllowed)) {
                String premiumIntId = remoteConfig.getString("premium_interstitial_ad_id");
                if (premiumIntId != null && !premiumIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("premium_interstitial_ad_id", premiumIntId.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "premium_dialog_native_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("premium_dialog_native_enabled", remoteConfig.getBoolean("premium_dialog_native_enabled"));
            }
            if (isRemoteKey(remoteConfig, "premium_dialog_native_id", isStandaloneAllowed)) {
                String pDnId = remoteConfig.getString("premium_dialog_native_id");
                if (pDnId != null && !pDnId.trim().isEmpty()) {
                    preferenceClass.setAdsId("premium_dialog_native_id", pDnId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "app_exit_dialog_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("app_exit_dialog_enabled", remoteConfig.getBoolean("app_exit_dialog_enabled"));
            }
            if (isRemoteKey(remoteConfig, "app_exit_native_enabled", isStandaloneAllowed)) {
                preferenceClass.setBoolean("app_exit_native_enabled", remoteConfig.getBoolean("app_exit_native_enabled"));
            }
            if (isRemoteKey(remoteConfig, "app_exit_native_id", isStandaloneAllowed)) {
                String appExitId = remoteConfig.getString("app_exit_native_id");
                if (appExitId != null && !appExitId.trim().isEmpty()) {
                    preferenceClass.setAdsId("app_exit_native_id", appExitId.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "app_exit_native_type", isStandaloneAllowed)) {
                String appExitType = remoteConfig.getString("app_exit_native_type");
                if (appExitType != null && !appExitType.trim().isEmpty()) {
                    preferenceClass.setDataType("app_exit_native_type", appExitType.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "interstitial_enabled", isStandaloneAllowed)) {
                boolean interEnabled = remoteConfig.getBoolean("interstitial_enabled");
                preferenceClass.setBoolean("interstitial_enabled", interEnabled);
                preferenceClass.setBoolean("interstitial_global_enabled", interEnabled);
            }
            if (isRemoteKey(remoteConfig, "interstitial_count", isStandaloneAllowed) || isRemoteKey(remoteConfig, "interstitial_click_interval", isStandaloneAllowed)) {
                long interCount = remoteConfig.getLong("interstitial_count");
                if (interCount <= 0) {
                    interCount = remoteConfig.getLong("interstitial_click_interval");
                }
                if (interCount > 0) {
                    preferenceClass.setInt("interstitial_count", (int) interCount);
                    preferenceClass.setAdsStatus("interstitalAdStatus", (int) interCount);
                }
            }
            if (isRemoteKey(remoteConfig, "interstitial_ad_type", isStandaloneAllowed)) {
                String interAdType = remoteConfig.getString("interstitial_ad_type");
                if (interAdType != null && !interAdType.trim().isEmpty()) {
                    preferenceClass.setDataType("interstitial_ad_type", interAdType.trim());
                }
            }
            if (isRemoteKey(remoteConfig, "interstitial_ad_id", isStandaloneAllowed)) {
                String interAdId = remoteConfig.getString("interstitial_ad_id");
                if (interAdId != null && !interAdId.trim().isEmpty()) {
                    preferenceClass.setAdsId("interstitial_ad_id", interAdId.trim());
                    preferenceClass.setAdsId("InterstitalAdunitID", interAdId.trim());
                }
            }

            if (isRemoteKey(remoteConfig, "banner_show_shimmer_on_fail", isStandaloneAllowed)) {
                preferenceClass.setBoolean("banner_show_shimmer_on_fail", remoteConfig.getBoolean("banner_show_shimmer_on_fail"));
            }
            if (isRemoteKey(remoteConfig, "native_show_shimmer_on_fail", isStandaloneAllowed)) {
                preferenceClass.setBoolean("native_show_shimmer_on_fail", remoteConfig.getBoolean("native_show_shimmer_on_fail"));
            }

            if (isRemoteKey(remoteConfig, "UpdateAvailable", isStandaloneAllowed)) {
                preferenceClass.setInt("UpdateAvailable", (int) remoteConfig.getLong("UpdateAvailable"));
            } else if (isRemoteKey(remoteConfig, "update_available", isStandaloneAllowed)) {
                preferenceClass.setInt("UpdateAvailable", remoteConfig.getBoolean("update_available") ? 1 : 0);
            }
            if (isRemoteKey(remoteConfig, "ForceUpdate", isStandaloneAllowed)) {
                preferenceClass.setInt("ForceUpdate", (int) remoteConfig.getLong("ForceUpdate"));
            } else if (isRemoteKey(remoteConfig, "force_update", isStandaloneAllowed)) {
                preferenceClass.setInt("ForceUpdate", remoteConfig.getBoolean("force_update") ? 1 : 0);
            }
            if (isRemoteKey(remoteConfig, "UpdateVersionName", isStandaloneAllowed)) {
                String uvn = remoteConfig.getString("UpdateVersionName");
                if (uvn != null && !uvn.trim().isEmpty()) preferenceClass.setDataType("UpdateVersionName", uvn.trim());
            } else if (isRemoteKey(remoteConfig, "update_version_name", isStandaloneAllowed)) {
                String uvn = remoteConfig.getString("update_version_name");
                if (uvn != null && !uvn.trim().isEmpty()) preferenceClass.setDataType("UpdateVersionName", uvn.trim());
            }
        } catch (Exception e) {
            Log.e("RemoteConfig", "applyAllRemoteConfig error: " + e.getMessage());
        }
    }

    private void parseMasterConfig(String configJsonStr) {
        if (configJsonStr == null || configJsonStr.trim().isEmpty()) return;
        try {
            org.json.JSONObject root = new org.json.JSONObject(configJsonStr);

            // 1. Splash
            if (root.has("splash")) {
                org.json.JSONObject splashObj = root.getJSONObject("splash");
                boolean appOpenEnabled = true;
                String appOpenId = "";
                if (splashObj.has("app_open_ad")) {
                    org.json.JSONObject appOpenObj = splashObj.getJSONObject("app_open_ad");
                    appOpenEnabled = appOpenObj.optBoolean("enabled", true);
                    appOpenId = appOpenObj.optString("ad_id", "");
                }
                boolean fallbackInterEnabled = true;
                String interId = "";
                if (splashObj.has("fallback_interstitial_ad")) {
                    org.json.JSONObject interObj = splashObj.getJSONObject("fallback_interstitial_ad");
                    fallbackInterEnabled = interObj.optBoolean("enabled", true);
                    interId = interObj.optString("ad_id", "");
                }
                preferenceClass.setBoolean("splash_app_open_enabled", appOpenEnabled);
                preferenceClass.setBoolean("splash_fallback_interstitial_enabled", fallbackInterEnabled);
                MailER_AppOpenManager.AppOpenAdShow = appOpenEnabled ? 1 : 0;
                preferenceClass.setInt("SplashAppOpen", appOpenEnabled ? 1 : 0);
                preferenceClass.setInt("SplashFallbackInter", fallbackInterEnabled ? 1 : 0);
                if (!appOpenId.trim().isEmpty()) {
                    preferenceClass.setAdsId("AppOpenID", appOpenId.trim());
                }
                if (!interId.trim().isEmpty()) {
                    preferenceClass.setAdsId("InterstitalAdunitID", interId.trim());
                }
            }

            // 1b. Foreground / Background App Open Ad (App Resume)
            boolean foregroundAppOpenEnabled = true;
            String foregroundAppOpenId = "";
            int foregroundAppOpenCooldown = 60;

            org.json.JSONObject resumeObj = null;
            if (root.has("foreground_app_open")) {
                resumeObj = root.optJSONObject("foreground_app_open");
            } else if (root.has("background_app_open")) {
                resumeObj = root.optJSONObject("background_app_open");
            } else if (root.has("app_open_resume")) {
                resumeObj = root.optJSONObject("app_open_resume");
            } else if (root.has("app_resume_ad")) {
                resumeObj = root.optJSONObject("app_resume_ad");
            } else if (root.has("app_resume")) {
                resumeObj = root.optJSONObject("app_resume");
            } else if (root.has("splash") && root.getJSONObject("splash").has("foreground_app_open")) {
                resumeObj = root.getJSONObject("splash").optJSONObject("foreground_app_open");
            } else if (root.has("splash") && root.getJSONObject("splash").has("background_app_open")) {
                resumeObj = root.getJSONObject("splash").optJSONObject("background_app_open");
            } else if (root.has("splash") && root.getJSONObject("splash").has("app_resume_ad")) {
                resumeObj = root.getJSONObject("splash").optJSONObject("app_resume_ad");
            }

            boolean suppressOnAdClick = true;
            boolean suppressOnIntent = true;
            if (resumeObj != null) {
                foregroundAppOpenEnabled = resumeObj.optBoolean("enabled", true);
                foregroundAppOpenId = resumeObj.optString("ad_id", "");
                foregroundAppOpenCooldown = resumeObj.optInt("cooldown_seconds", 60);
                if (resumeObj.has("suppress_on_ad_click")) {
                    suppressOnAdClick = resumeObj.optBoolean("suppress_on_ad_click", true);
                } else if (resumeObj.has("suppress_app_open_on_ad_click")) {
                    suppressOnAdClick = resumeObj.optBoolean("suppress_app_open_on_ad_click", true);
                }
                if (resumeObj.has("suppress_on_intent")) {
                    suppressOnIntent = resumeObj.optBoolean("suppress_on_intent", true);
                } else if (resumeObj.has("suppress_app_open_on_intent")) {
                    suppressOnIntent = resumeObj.optBoolean("suppress_app_open_on_intent", true);
                }
            }
            if (root.has("suppress_app_open_on_ad_click")) {
                suppressOnAdClick = root.optBoolean("suppress_app_open_on_ad_click", true);
            }
            if (root.has("suppress_app_open_on_intent")) {
                suppressOnIntent = root.optBoolean("suppress_app_open_on_intent", true);
            }

            preferenceClass.setBoolean("foreground_app_open_enabled", foregroundAppOpenEnabled);
            preferenceClass.setInt("foreground_app_open_cooldown", foregroundAppOpenCooldown);
            preferenceClass.setBoolean("suppress_app_open_on_ad_click", suppressOnAdClick);
            preferenceClass.setBoolean("suppress_app_open_on_intent", suppressOnIntent);
            if (!foregroundAppOpenId.trim().isEmpty()) {
                preferenceClass.setAdsId("ForegroundAppOpenID", foregroundAppOpenId.trim());
            }

            // 2. Onboarding
            if (root.has("onboarding")) {
                org.json.JSONObject onboardingObj = root.getJSONObject("onboarding");
                preferenceClass.setDataType("onboarding_config", onboardingObj.toString());
                boolean onboardingInterEnabled = onboardingObj.optBoolean("is_interstitial_enabled", onboardingObj.optBoolean("interstitial_enabled", true));
                preferenceClass.setBoolean("is_onboarding_inter_enabled", onboardingInterEnabled);
                if (onboardingObj.has("onboarding_1")) {
                    org.json.JSONObject s1 = onboardingObj.getJSONObject("onboarding_1");
                    preferenceClass.setBoolean("onboarding_inter_step_1", s1.optBoolean("next_interstitial", true));
                }
                if (onboardingObj.has("onboarding_2")) {
                    org.json.JSONObject s2 = onboardingObj.getJSONObject("onboarding_2");
                    preferenceClass.setBoolean("onboarding_inter_step_2", s2.optBoolean("next_interstitial", true));
                }
                if (onboardingObj.has("onboarding_3")) {
                    org.json.JSONObject s3 = onboardingObj.getJSONObject("onboarding_3");
                    preferenceClass.setBoolean("onboarding_inter_step_3", s3.optBoolean("next_interstitial", true));
                }
                String onbInterId = onboardingObj.optString("interstitial_ad_id", "");
                if (!onbInterId.trim().isEmpty()) {
                    preferenceClass.setAdsId("OnboardingInterAdID", onbInterId.trim());
                    preferenceClass.setAdsId("interstitial_ad_id", onbInterId.trim());
                    preferenceClass.setAdsId("InterstitalAdunitID", onbInterId.trim());
                }
            }

            // 3. Home Screen (Banner / Collapsible / Native)
            if (root.has("home")) {
                org.json.JSONObject homeObj = root.getJSONObject("home");
                boolean homeEnabled = homeObj.optBoolean("enabled", true);
                String adType = homeObj.optString("ad_type", "banner");
                if (homeObj.has("banner_ad")) {
                    org.json.JSONObject bObj = homeObj.getJSONObject("banner_ad");
                    if (bObj.has("enabled") && !bObj.optBoolean("enabled", true)) {
                        homeEnabled = false;
                    }
                    if (bObj.has("ad_id") && !bObj.optString("ad_id").trim().isEmpty()) {
                        preferenceClass.setAdsId("BannerAdunitID", bObj.optString("ad_id").trim());
                    }
                }
                int mode = 2; // default: banner
                if (!homeEnabled || "none".equalsIgnoreCase(adType) || "off".equalsIgnoreCase(adType)) {
                    mode = 0;
                } else if ("collapsible".equalsIgnoreCase(adType)) {
                    mode = 1;
                } else if ("banner".equalsIgnoreCase(adType)) {
                    mode = 2;
                } else if ("native".equalsIgnoreCase(adType)) {
                    mode = 3;
                }
                preferenceClass.setInt("MainScreen_Native", mode);
                String homeAdId = homeObj.optString("ad_id", "");
                if (!homeAdId.trim().isEmpty()) {
                    if (mode == 1) {
                        preferenceClass.setAdsId("CollapsibleBannerID", homeAdId.trim());
                    } else if (mode == 2) {
                        preferenceClass.setAdsId("BannerAdunitID", homeAdId.trim());
                    } else if (mode == 3) {
                        preferenceClass.setAdsId("NativeUnitID", homeAdId.trim());
                    }
                }
            }

            // 4. Templates Screen (Banner & Native)
            if (root.has("templates")) {
                org.json.JSONObject tplObj = root.getJSONObject("templates");
                if (tplObj.has("banner_ad")) {
                    org.json.JSONObject bObj = tplObj.getJSONObject("banner_ad");
                    boolean bEnabled = bObj.optBoolean("enabled", true);
                    preferenceClass.setAdsStatus("ReadymadePoste_BannerAD", bEnabled ? 1 : 0);
                    String bId = bObj.optString("ad_id", "");
                    if (!bId.trim().isEmpty()) {
                        preferenceClass.setAdsId("BannerAdunitID", bId.trim());
                    }
                }
                if (tplObj.has("preview_banner_ad")) {
                    org.json.JSONObject pbObj = tplObj.getJSONObject("preview_banner_ad");
                    boolean pbEnabled = pbObj.optBoolean("enabled", true);
                    preferenceClass.setBoolean("preview_poster_banner_enabled", pbEnabled);
                    String pbId = pbObj.optString("ad_id", "");
                    if (!pbId.trim().isEmpty()) {
                        preferenceClass.setAdsId("preview_poster_banner_ad_id", pbId.trim());
                    }
                }
                if (tplObj.has("preview_native_ad")) {
                    org.json.JSONObject pnObj = tplObj.getJSONObject("preview_native_ad");
                    boolean pnEnabled = pnObj.optBoolean("enabled", pnObj.optBoolean("is_show", true));
                    preferenceClass.setBoolean("preview_poster_native_enabled", pnEnabled);
                    String pnId = pnObj.optString("ad_id", pnObj.optString("native_ad_id", ""));
                    if (!pnId.trim().isEmpty()) {
                        preferenceClass.setAdsId("preview_poster_native_ad_id", pnId.trim());
                    }
                    String pnType = pnObj.optString("type", pnObj.optString("ad_type", "compact"));
                    preferenceClass.setDataType("preview_poster_native_type", pnType);
                    int pnH = pnObj.optInt("height", 100);
                    preferenceClass.setInt("preview_poster_native_height", pnH);
                } else if (tplObj.has("preview_ad")) {
                    org.json.JSONObject pnObj = tplObj.getJSONObject("preview_ad");
                    boolean pnEnabled = pnObj.optBoolean("enabled", pnObj.optBoolean("is_show", true));
                    preferenceClass.setBoolean("preview_poster_native_enabled", pnEnabled);
                    String pnId = pnObj.optString("ad_id", pnObj.optString("native_ad_id", ""));
                    if (!pnId.trim().isEmpty()) {
                        preferenceClass.setAdsId("preview_poster_native_ad_id", pnId.trim());
                    }
                    String pnType = pnObj.optString("type", pnObj.optString("ad_type", "compact"));
                    preferenceClass.setDataType("preview_poster_native_type", pnType);
                    int pnH = pnObj.optInt("height", 100);
                    preferenceClass.setInt("preview_poster_native_height", pnH);
                }
                if (tplObj.has("native_ad")) {
                    org.json.JSONObject nObj = tplObj.getJSONObject("native_ad");
                    boolean nEnabled = nObj.optBoolean("enabled", true);
                    preferenceClass.setInt("templates_native_enabled", nEnabled ? 1 : 0);
                    int firstAfter = nObj.optInt("first_ad_after", 4);
                    int repeatAfter = nObj.optInt("repeat_ad_after", 6);
                    preferenceClass.setInt("First_rv_count", firstAfter);
                    preferenceClass.setInt("rv_count", repeatAfter);
                    String nId = nObj.optString("ad_id", "");
                    if (!nId.trim().isEmpty()) {
                        preferenceClass.setAdsId("NativeUnitID", nId.trim());
                    }
                }
            }

            // 5. Backgrounds Screen (Banner & Native)
            if (root.has("backgrounds")) {
                org.json.JSONObject bgObj = root.getJSONObject("backgrounds");
                if (bgObj.has("banner_ad")) {
                    org.json.JSONObject bObj = bgObj.getJSONObject("banner_ad");
                    boolean bEnabled = bObj.optBoolean("enabled", true);
                    preferenceClass.setAdsStatus("BGSelectScreen_BannerAD", bEnabled ? 1 : 0);
                    String bId = bObj.optString("ad_id", "");
                    if (!bId.trim().isEmpty()) {
                        preferenceClass.setAdsId("BannerAdunitID", bId.trim());
                    }
                }
                if (bgObj.has("preview_banner_ad")) {
                    org.json.JSONObject pbObj = bgObj.getJSONObject("preview_banner_ad");
                    boolean pbEnabled = pbObj.optBoolean("enabled", true);
                    preferenceClass.setBoolean("preview_bg_banner_enabled", pbEnabled);
                    String pbId = pbObj.optString("ad_id", "");
                    if (!pbId.trim().isEmpty()) {
                        preferenceClass.setAdsId("preview_bg_banner_ad_id", pbId.trim());
                    }
                }
                if (bgObj.has("preview_native_ad")) {
                    org.json.JSONObject pnObj = bgObj.getJSONObject("preview_native_ad");
                    boolean pnEnabled = pnObj.optBoolean("enabled", pnObj.optBoolean("is_show", true));
                    preferenceClass.setBoolean("preview_bg_native_enabled", pnEnabled);
                    String pnId = pnObj.optString("ad_id", pnObj.optString("native_ad_id", ""));
                    if (!pnId.trim().isEmpty()) {
                        preferenceClass.setAdsId("preview_bg_native_ad_id", pnId.trim());
                    }
                    String pnType = pnObj.optString("type", pnObj.optString("ad_type", "compact"));
                    preferenceClass.setDataType("preview_bg_native_type", pnType);
                    int pnH = pnObj.optInt("height", 100);
                    preferenceClass.setInt("preview_bg_native_height", pnH);
                } else if (bgObj.has("preview_ad")) {
                    org.json.JSONObject pnObj = bgObj.getJSONObject("preview_ad");
                    boolean pnEnabled = pnObj.optBoolean("enabled", pnObj.optBoolean("is_show", true));
                    preferenceClass.setBoolean("preview_bg_native_enabled", pnEnabled);
                    String pnId = pnObj.optString("ad_id", pnObj.optString("native_ad_id", ""));
                    if (!pnId.trim().isEmpty()) {
                        preferenceClass.setAdsId("preview_bg_native_ad_id", pnId.trim());
                    }
                    String pnType = pnObj.optString("type", pnObj.optString("ad_type", "compact"));
                    preferenceClass.setDataType("preview_bg_native_type", pnType);
                    int pnH = pnObj.optInt("height", 100);
                    preferenceClass.setInt("preview_bg_native_height", pnH);
                }
                if (bgObj.has("native_ad")) {
                    org.json.JSONObject nObj = bgObj.getJSONObject("native_ad");
                    boolean nEnabled = nObj.optBoolean("enabled", true);
                    preferenceClass.setInt("backgrounds_native_enabled", nEnabled ? 1 : 0);
                    int firstAfter = nObj.optInt("first_ad_after", 4);
                    int repeatAfter = nObj.optInt("repeat_ad_after", 6);
                    preferenceClass.setInt("bg_first_ad_count", firstAfter);
                    preferenceClass.setInt("bg_repeat_ad_count", repeatAfter);
                    String nId = nObj.optString("ad_id", "");
                    if (!nId.trim().isEmpty()) {
                        preferenceClass.setAdsId("NativeUnitID", nId.trim());
                    }
                }
            }

            // 6. Create Screen
            if (root.has("create")) {
                org.json.JSONObject crObj = root.getJSONObject("create");
                boolean crEnabled = crObj.optBoolean("enabled", true);
                String crAdType = crObj.optString("ad_type", "banner");
                String crAdId = crObj.optString("ad_id", "");

                boolean bannerEnabled = crEnabled;
                String bannerId = crAdId;
                if (crObj.has("banner_ad")) {
                    org.json.JSONObject bObj = crObj.getJSONObject("banner_ad");
                    bannerEnabled = bObj.optBoolean("enabled", crEnabled);
                    if (bObj.has("ad_id") && !bObj.getString("ad_id").trim().isEmpty()) {
                        bannerId = bObj.getString("ad_id");
                    }
                }

                if (crObj.has("collapsible_banner")) {
                    org.json.JSONObject colObj = crObj.getJSONObject("collapsible_banner");
                    if (colObj.optBoolean("enabled", false)) {
                        crAdType = "collapsible";
                        if (colObj.has("ad_id") && !colObj.getString("ad_id").trim().isEmpty()) {
                            bannerId = colObj.getString("ad_id");
                        }
                    }
                }

                preferenceClass.setBoolean("create_banner_ad_enabled", bannerEnabled);
                preferenceClass.setAdsStatus("CreateScreen_BannerAD", bannerEnabled ? 1 : 0);
                preferenceClass.setDataType("create_banner_ad_type", crAdType);
                if (!bannerId.trim().isEmpty()) {
                    preferenceClass.setAdsId("create_banner_ad_id", bannerId.trim());
                }

                if (crObj.has("interstitial")) {
                    org.json.JSONObject inObj = crObj.getJSONObject("interstitial");
                    boolean inEnabled = inObj.optBoolean("enabled", true);
                    int inInterval = inObj.optInt("click_interval", 2);
                    String inId = inObj.optString("ad_id", "");
                    preferenceClass.setBoolean("interstitial_create_enabled", inEnabled);
                    preferenceClass.setInt("interstitial_create_interval", inInterval);
                    if (!inId.trim().isEmpty()) {
                        preferenceClass.setAdsId("create_interstitial_ad_id", inId.trim());
                    }
                }
            }

            // 7. Custom Category / Brand Poster
            if (root.has("custom_category")) {
                org.json.JSONObject catObj = root.getJSONObject("custom_category");
                if (catObj.has("banner_ad")) {
                    org.json.JSONObject bObj = catObj.getJSONObject("banner_ad");
                    boolean bEnabled = bObj.optBoolean("enabled", true);
                    preferenceClass.setBoolean("custom_category_banner_ad_enabled", bEnabled);
                    preferenceClass.setAdsStatus("BrandPoster_BannerAD", bEnabled ? 1 : 0);
                    String bId = bObj.optString("ad_id", "");
                    if (!bId.trim().isEmpty()) {
                        preferenceClass.setAdsId("custom_category_banner_ad_id", bId.trim());
                    }
                }
                if (catObj.has("native_ad")) {
                    org.json.JSONObject nObj = catObj.getJSONObject("native_ad");
                    boolean nEnabled = nObj.optBoolean("enabled", true);
                    preferenceClass.setBoolean("custom_category_native_enabled", nEnabled);
                    String nId = nObj.optString("ad_id", "");
                    if (!nId.trim().isEmpty()) {
                        preferenceClass.setAdsId("custom_category_native_ad_id", nId.trim());
                    }
                    int firstCount = nObj.optInt("first_ad_after", 4);
                    int repeatCount = nObj.optInt("repeat_ad_after", 6);
                    preferenceClass.setInt("custom_category_first_rv_count", firstCount);
                    preferenceClass.setInt("custom_category_rv_count", repeatCount);
                }
            }

            // 7b. My Creation Screen
            if (root.has("my_creation")) {
                org.json.JSONObject mcObj = root.getJSONObject("my_creation");
                boolean mcEnabled = mcObj.optBoolean("enabled", false);
                if (mcObj.has("banner_ad")) {
                    org.json.JSONObject bObj = mcObj.getJSONObject("banner_ad");
                    boolean bEnabled = bObj.optBoolean("enabled", mcEnabled);
                    preferenceClass.setBoolean("my_creation_banner_ad_enabled", bEnabled);
                    String bId = bObj.optString("ad_id", "");
                    if (!bId.trim().isEmpty()) {
                        preferenceClass.setAdsId("my_creation_banner_ad_id", bId.trim());
                    }
                } else {
                    preferenceClass.setBoolean("my_creation_banner_ad_enabled", mcEnabled);
                }
            }

            // 7c. Share Screen
            if (root.has("share")) {
                org.json.JSONObject shObj = root.getJSONObject("share");
                boolean shEnabled = shObj.optBoolean("enabled", true);
                if (shObj.has("banner_ad")) {
                    org.json.JSONObject bObj = shObj.getJSONObject("banner_ad");
                    boolean bEnabled = bObj.optBoolean("enabled", shEnabled);
                    preferenceClass.setBoolean("share_banner_ad_enabled", bEnabled);
                    String bId = bObj.optString("ad_id", "");
                    if (!bId.trim().isEmpty()) {
                        preferenceClass.setAdsId("share_banner_ad_id", bId.trim());
                    }
                } else {
                    preferenceClass.setBoolean("share_banner_ad_enabled", shEnabled);
                }
            }

            // 8. Editor Screen (Banner, Exit Dialog Native, and 3-min Timer Interstitial)
            if (root.has("editor")) {
                org.json.JSONObject edObj = root.getJSONObject("editor");
                if (edObj.has("banner_ad")) {
                    org.json.JSONObject bObj = edObj.getJSONObject("banner_ad");
                    boolean bEnabled = bObj.optBoolean("enabled", false);
                    preferenceClass.setInt("IsEditScreenBannerAD", bEnabled ? 1 : 0);
                    String bId = bObj.optString("ad_id", "");
                    if (!bId.trim().isEmpty()) {
                        preferenceClass.setAdsId("BannerAdunitID", bId.trim());
                    }
                }
                if (edObj.has("exit_dialog_native")) {
                    org.json.JSONObject exitObj = edObj.getJSONObject("exit_dialog_native");
                    boolean exitEnabled = exitObj.optBoolean("enabled", true);
                    preferenceClass.setBoolean("editor_exit_dialog_native_enabled", exitEnabled);
                    preferenceClass.setAdsStatus("ExitDialog_NativeAD", exitEnabled ? 1 : 0);
                    String exitId = exitObj.optString("ad_id", "");
                    if (!exitId.trim().isEmpty()) {
                        preferenceClass.setAdsId("editor_exit_dialog_native_id", exitId.trim());
                    }
                    String exitType = exitObj.optString("ad_type", "medium");
                    preferenceClass.setDataType("editor_exit_dialog_native_type", exitType);
                }
                if (edObj.has("reward_timer")) {
                    org.json.JSONObject rObj = edObj.getJSONObject("reward_timer");
                    boolean rEnabled = rObj.optBoolean("enabled", true);
                    int rSeconds = rObj.optInt("timer_seconds", 60);
                    preferenceClass.setBoolean("editor_reward_timer_enabled", rEnabled);
                    preferenceClass.setInt("editor_reward_timer_seconds", rSeconds);
                    String rId = rObj.optString("ad_id", "");
                    if (!rId.trim().isEmpty()) {
                        preferenceClass.setAdsId("RewardVideoUnitID", rId.trim());
                        preferenceClass.setAdsId("editor_reward_ad_id", rId.trim());
                    }
                } else if (edObj.has("timer_interstitial")) {
                    org.json.JSONObject tObj = edObj.getJSONObject("timer_interstitial");
                    boolean tEnabled = tObj.optBoolean("enabled", true);
                    int tSeconds = tObj.optInt("timer_seconds", 60);
                    preferenceClass.setBoolean("editor_reward_timer_enabled", tEnabled);
                    preferenceClass.setInt("editor_reward_timer_seconds", tSeconds);
                    String tId = tObj.optString("ad_id", "");
                    if (!tId.trim().isEmpty()) {
                        preferenceClass.setAdsId("RewardVideoUnitID", tId.trim());
                        preferenceClass.setAdsId("editor_reward_ad_id", tId.trim());
                    }
                } else {
                    preferenceClass.setBoolean("editor_reward_timer_enabled", true);
                    preferenceClass.setInt("editor_reward_timer_seconds", 60);
                }

                if (edObj.has("download")) {
                    org.json.JSONObject dlObj = edObj.getJSONObject("download");
                    boolean dlEnabled = dlObj.optBoolean("enabled", true);
                    int freeDownloads = dlObj.optInt("free_downloads_allowed", 2);
                    boolean askWatermark = dlObj.optBoolean("ask_watermark_removal", true);
                    String adType = dlObj.optString("ad_type", "reward");
                    String adId = dlObj.optString("ad_id", "");

                    preferenceClass.setBoolean("editor_download_ad_enabled", dlEnabled);
                    preferenceClass.setInt("editor_free_downloads_allowed", freeDownloads);
                    preferenceClass.setBoolean("editor_ask_watermark_removal", askWatermark);
                    preferenceClass.setDataType("editor_download_ad_type", adType);
                    if (!adId.trim().isEmpty()) {
                        preferenceClass.setAdsId("editor_download_ad_id", adId.trim());
                    }
                }

                if (edObj.has("undo_redo")) {
                    org.json.JSONObject urObj = edObj.getJSONObject("undo_redo");
                    boolean urEnabled = urObj.optBoolean("enabled", true);
                    int freeActions = urObj.optInt("free_actions_allowed", 3);
                    String adType = urObj.optString("ad_type", "reward");
                    String adId = urObj.optString("ad_id", "");

                    preferenceClass.setBoolean("editor_undo_redo_ad_enabled", urEnabled);
                    preferenceClass.setInt("editor_free_undo_redo_allowed", freeActions);
                    preferenceClass.setDataType("editor_undo_redo_ad_type", adType);
                    if (!adId.trim().isEmpty()) {
                        preferenceClass.setAdsId("editor_undo_redo_ad_id", adId.trim());
                    }
                }
            }

            // 9. Interstitial (Global Counter, 100% Remote Config Managed)
            if (root.has("interstitial")) {
                org.json.JSONObject interObj = root.getJSONObject("interstitial");
                boolean interEnabled = true;
                if (interObj.has("enabled")) {
                    interEnabled = interObj.optBoolean("enabled", true);
                } else if (interObj.has("global_enabled")) {
                    interEnabled = interObj.optBoolean("global_enabled", true);
                }

                int interCount = 4;
                if (interObj.has("count")) {
                    interCount = interObj.optInt("count", 4);
                } else if (interObj.has("click_interval")) {
                    interCount = interObj.optInt("click_interval", 4);
                } else if (interObj.has("default_click_interval")) {
                    interCount = interObj.optInt("default_click_interval", 4);
                }

                String interAdType = interObj.optString("ad_type", "interstitial");
                String interId = interObj.optString("ad_id", "");

                preferenceClass.setBoolean("interstitial_enabled", interEnabled);
                preferenceClass.setBoolean("interstitial_global_enabled", interEnabled);
                preferenceClass.setInt("interstitial_count", interCount);
                preferenceClass.setAdsStatus("interstitalAdStatus", interEnabled ? interCount : 0);
                preferenceClass.setDataType("interstitial_ad_type", interAdType);
                if (!interId.trim().isEmpty()) {
                    preferenceClass.setAdsId("interstitial_ad_id", interId.trim());
                    preferenceClass.setAdsId("InterstitalAdunitID", interId.trim());
                }
            }

            // 10. Global Banner
            if (root.has("banner")) {
                org.json.JSONObject bObj = root.getJSONObject("banner");
                boolean bEnabled = bObj.optBoolean("global_enabled", true);
                preferenceClass.setAdsStatus("bannerAdStatus", bEnabled ? 1 : 0);
                String bId = bObj.optString("ad_id", "");
                if (!bId.trim().isEmpty()) {
                    preferenceClass.setAdsId("BannerAdunitID", bId.trim());
                }
            } else {
                preferenceClass.setAdsStatus("bannerAdStatus", 1);
            }

            boolean bannerShimmerOnFail = true;
            if (root.has("banner_show_shimmer_on_fail")) {
                bannerShimmerOnFail = root.optBoolean("banner_show_shimmer_on_fail", true);
            } else if (root.has("banner_shimmer_on_fail")) {
                bannerShimmerOnFail = root.optBoolean("banner_shimmer_on_fail", true);
            } else if (root.has("banner") && root.getJSONObject("banner").has("show_shimmer_on_fail")) {
                bannerShimmerOnFail = root.getJSONObject("banner").optBoolean("show_shimmer_on_fail", true);
            }
            preferenceClass.setBoolean("banner_show_shimmer_on_fail", bannerShimmerOnFail);

            boolean nativeShimmerOnFail = true;
            if (root.has("native_show_shimmer_on_fail")) {
                nativeShimmerOnFail = root.optBoolean("native_show_shimmer_on_fail", true);
            } else if (root.has("native_shimmer_on_fail")) {
                nativeShimmerOnFail = root.optBoolean("native_shimmer_on_fail", true);
            } else if (root.has("native") && root.getJSONObject("native").has("show_shimmer_on_fail")) {
                nativeShimmerOnFail = root.getJSONObject("native").optBoolean("show_shimmer_on_fail", true);
            }
            preferenceClass.setBoolean("native_show_shimmer_on_fail", nativeShimmerOnFail);

            // 11. Loading Screen (Progress Bar, Medium Native Ad, Timer Duration)
            if (root.has("loading_screen")) {
                org.json.JSONObject loadObj = root.getJSONObject("loading_screen");
                boolean loadEnabled = loadObj.optBoolean("enabled", true);
                boolean showAfterSplash = loadObj.optBoolean("show_after_splash", true);
                boolean showAfterOnboarding = loadObj.optBoolean("show_after_onboarding", true);
                int timerSec = loadObj.optInt("timer_seconds", 4);
                preferenceClass.setBoolean("loading_screen_enabled", loadEnabled);
                preferenceClass.setBoolean("loading_screen_show_after_splash", showAfterSplash);
                preferenceClass.setBoolean("loading_screen_show_after_onboarding", showAfterOnboarding);
                preferenceClass.setInt("loading_screen_timer_seconds", timerSec);

                if (loadObj.has("native_ad")) {
                    org.json.JSONObject nObj = loadObj.getJSONObject("native_ad");
                    boolean nEnabled = nObj.optBoolean("enabled", true);
                    preferenceClass.setBoolean("loading_screen_native_enabled", nEnabled);
                    int nHeight = nObj.optInt("height", 280);
                    preferenceClass.setInt("loading_screen_native_height", nHeight);
                    String nType = nObj.optString("type", "video");
                    preferenceClass.setDataType("loading_screen_native_type", nType);
                    String nId = nObj.optString("ad_id", "");
                    if (!nId.trim().isEmpty()) {
                        preferenceClass.setAdsId("LoadingScreenNativeAdID", nId.trim());
                    }
                }
            } else {
                preferenceClass.setBoolean("loading_screen_enabled", true);
                preferenceClass.setBoolean("loading_screen_show_after_splash", true);
                preferenceClass.setBoolean("loading_screen_show_after_onboarding", true);
                preferenceClass.setInt("loading_screen_timer_seconds", 4);
                preferenceClass.setBoolean("loading_screen_native_enabled", true);
            }

            // 12. Premium / Pro Templates, Background & Branding Poster Configuration
            org.json.JSONObject pObj = root.optJSONObject("premium_config");
            
            // Global / Fallback values
            boolean globalPEnabled = true;
            int globalPInterval = 3;
            String globalPAdType = "reward";
            boolean globalPAdEnabled = true;
            String globalPRwId = "";
            String globalPIntId = "";
            if (pObj != null) {
                globalPEnabled = pObj.optBoolean("enabled", true);
                preferenceClass.setBoolean("premium_enabled", globalPEnabled);
                globalPInterval = pObj.optInt("post_interval", pObj.optInt("interval", 3));
                preferenceClass.setInt("PremiumPostCount", globalPEnabled ? globalPInterval : 0);
                globalPAdType = pObj.optString("ad_type", "reward");
                preferenceClass.setDataType("PremiumAdType", globalPAdType);
                globalPAdEnabled = pObj.optBoolean("ad_enabled", true);
                preferenceClass.setBoolean("premium_ad_enabled", globalPAdEnabled);
                globalPRwId = pObj.optString("reward_ad_id", "");
                if (!globalPRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("premium_reward_ad_id", globalPRwId.trim());
                }
                globalPIntId = pObj.optString("interstitial_ad_id", "");
                if (!globalPIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("premium_interstitial_ad_id", globalPIntId.trim());
                }

                // Pro Dialog Native Ad
                boolean pDialogNativeEnabled = true;
                String pDialogNativeId = "";
                if (pObj.has("dialog_native_ad")) {
                    org.json.JSONObject dnObj = pObj.getJSONObject("dialog_native_ad");
                    pDialogNativeEnabled = dnObj.optBoolean("enabled", true);
                    pDialogNativeId = dnObj.optString("ad_id", "");
                } else if (pObj.has("native_ad")) {
                    org.json.JSONObject dnObj = pObj.getJSONObject("native_ad");
                    pDialogNativeEnabled = dnObj.optBoolean("enabled", true);
                    pDialogNativeId = dnObj.optString("ad_id", "");
                }
                preferenceClass.setBoolean("premium_dialog_native_enabled", pDialogNativeEnabled);
                if (!pDialogNativeId.trim().isEmpty()) {
                    preferenceClass.setAdsId("premium_dialog_native_id", pDialogNativeId.trim());
                }
            }

            if (root.has("premium_dialog_native_enabled")) {
                preferenceClass.setBoolean("premium_dialog_native_enabled", root.optBoolean("premium_dialog_native_enabled", true));
            }
            if (root.has("premium_dialog_native_id") && !root.optString("premium_dialog_native_id").trim().isEmpty()) {
                preferenceClass.setAdsId("premium_dialog_native_id", root.optString("premium_dialog_native_id").trim());
            }

            // 12a. Templates Premium
            org.json.JSONObject tplObj = root.optJSONObject("templates_premium");
            if (tplObj == null && pObj != null) {
                tplObj = pObj.optJSONObject("templates");
            }
            if (tplObj != null) {
                boolean tEnabled = tplObj.optBoolean("enabled", globalPEnabled);
                preferenceClass.setBoolean("templates_premium_enabled", tEnabled);
                int tInterval = tplObj.optInt("interval", tplObj.optInt("post_interval", globalPInterval));
                preferenceClass.setInt("templates_premium_interval", tEnabled ? tInterval : 0);
                preferenceClass.setDataType("templates_premium_ad_type", tplObj.optString("ad_type", globalPAdType));
                preferenceClass.setBoolean("templates_premium_ad_enabled", tplObj.optBoolean("ad_enabled", globalPAdEnabled));
                String tRwId = tplObj.optString("reward_ad_id", globalPRwId);
                if (!tRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("templates_reward_ad_id", tRwId.trim());
                }
                String tIntId = tplObj.optString("interstitial_ad_id", globalPIntId);
                if (!tIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("templates_interstitial_ad_id", tIntId.trim());
                }
            } else {
                preferenceClass.setBoolean("templates_premium_enabled", globalPEnabled);
                preferenceClass.setInt("templates_premium_interval", globalPEnabled ? globalPInterval : 0);
                preferenceClass.setDataType("templates_premium_ad_type", globalPAdType);
                preferenceClass.setBoolean("templates_premium_ad_enabled", globalPAdEnabled);
                if (!globalPRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("templates_reward_ad_id", globalPRwId.trim());
                }
                if (!globalPIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("templates_interstitial_ad_id", globalPIntId.trim());
                }
            }

            // 12b. Background Premium
            org.json.JSONObject bgObj = root.optJSONObject("background_premium");
            if (bgObj == null && pObj != null) {
                bgObj = pObj.optJSONObject("background");
            }
            if (bgObj != null) {
                boolean bEnabled = bgObj.optBoolean("enabled", globalPEnabled);
                preferenceClass.setBoolean("background_premium_enabled", bEnabled);
                int bInterval = bgObj.optInt("interval", bgObj.optInt("post_interval", globalPInterval));
                preferenceClass.setInt("background_premium_interval", bEnabled ? bInterval : 0);
                preferenceClass.setDataType("background_premium_ad_type", bgObj.optString("ad_type", globalPAdType));
                preferenceClass.setBoolean("background_premium_ad_enabled", bgObj.optBoolean("ad_enabled", globalPAdEnabled));
                String bRwId = bgObj.optString("reward_ad_id", globalPRwId);
                if (!bRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("background_reward_ad_id", bRwId.trim());
                }
                String bIntId = bgObj.optString("interstitial_ad_id", globalPIntId);
                if (!bIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("background_interstitial_ad_id", bIntId.trim());
                }
            } else {
                preferenceClass.setBoolean("background_premium_enabled", globalPEnabled);
                preferenceClass.setInt("background_premium_interval", globalPEnabled ? globalPInterval : 0);
                preferenceClass.setDataType("background_premium_ad_type", globalPAdType);
                preferenceClass.setBoolean("background_premium_ad_enabled", globalPAdEnabled);
                if (!globalPRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("background_reward_ad_id", globalPRwId.trim());
                }
                if (!globalPIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("background_interstitial_ad_id", globalPIntId.trim());
                }
            }

            // 12c. Branding Poster Premium
            org.json.JSONObject brObj = root.optJSONObject("branding_poster_premium");
            if (brObj == null && pObj != null) {
                brObj = pObj.optJSONObject("branding_poster");
            }
            if (brObj == null && pObj != null) {
                brObj = pObj.optJSONObject("branding");
            }
            if (brObj != null) {
                boolean brEnabled = brObj.optBoolean("enabled", globalPEnabled);
                preferenceClass.setBoolean("branding_premium_enabled", brEnabled);
                int brInterval = brObj.optInt("interval", brObj.optInt("post_interval", globalPInterval));
                preferenceClass.setInt("branding_premium_interval", brEnabled ? brInterval : 0);
                preferenceClass.setDataType("branding_premium_ad_type", brObj.optString("ad_type", globalPAdType));
                preferenceClass.setBoolean("branding_premium_ad_enabled", brObj.optBoolean("ad_enabled", globalPAdEnabled));
                String brRwId = brObj.optString("reward_ad_id", globalPRwId);
                if (!brRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("branding_reward_ad_id", brRwId.trim());
                }
                String brIntId = brObj.optString("interstitial_ad_id", globalPIntId);
                if (!brIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("branding_interstitial_ad_id", brIntId.trim());
                }
            } else {
                preferenceClass.setBoolean("branding_premium_enabled", globalPEnabled);
                preferenceClass.setInt("branding_premium_interval", globalPEnabled ? globalPInterval : 0);
                preferenceClass.setDataType("branding_premium_ad_type", globalPAdType);
                preferenceClass.setBoolean("branding_premium_ad_enabled", globalPAdEnabled);
                if (!globalPRwId.trim().isEmpty()) {
                    preferenceClass.setAdsId("branding_reward_ad_id", globalPRwId.trim());
                }
                if (!globalPIntId.trim().isEmpty()) {
                    preferenceClass.setAdsId("branding_interstitial_ad_id", globalPIntId.trim());
                }
            }

            // 13. Screenshots Configuration: true = chalu (allowed), false = banth (blocked)
            boolean scEnabled = true;
            if (root.has("screenshot_enabled")) {
                scEnabled = root.optBoolean("screenshot_enabled", true);
            } else if (root.has("screenshot")) {
                org.json.JSONObject scObj = root.optJSONObject("screenshot");
                if (scObj != null) {
                    scEnabled = scObj.optBoolean("enabled", true);
                }
            } else if (root.has("allow_screenshot")) {
                scEnabled = root.optBoolean("allow_screenshot", true);
            } else if (root.has("disable_screenshots")) {
                scEnabled = !root.optBoolean("disable_screenshots", false);
            }
            preferenceClass.setBoolean("screenshot_enabled", scEnabled);
            com.festival.flyer.postermaker.MyApplication.applyScreenshotSecurity(MailER_SplashScreen.this);

            // 14. App Exit Dialog (On pressing Back button on Main Screen)
            if (root.has("app_exit_dialog")) {
                org.json.JSONObject exitObj = root.getJSONObject("app_exit_dialog");
                boolean appExitDialogEnabled = exitObj.optBoolean("enabled", true);
                preferenceClass.setBoolean("app_exit_dialog_enabled", appExitDialogEnabled);

                boolean exitNativeEnabled = exitObj.optBoolean("native_ad_enabled", true);
                String exitNativeId = exitObj.optString("native_ad_id", "");
                String exitNativeType = exitObj.optString("ad_type", "medium");

                if (exitObj.has("native_ad")) {
                    org.json.JSONObject nObj = exitObj.getJSONObject("native_ad");
                    exitNativeEnabled = nObj.optBoolean("enabled", exitNativeEnabled);
                    String nid = nObj.optString("ad_id", "");
                    if (!nid.trim().isEmpty()) exitNativeId = nid;
                    exitNativeType = nObj.optString("ad_type", exitNativeType);
                }

                preferenceClass.setBoolean("app_exit_native_enabled", exitNativeEnabled);
                if (!exitNativeId.trim().isEmpty()) {
                    preferenceClass.setAdsId("app_exit_native_id", exitNativeId.trim());
                }
                preferenceClass.setDataType("app_exit_native_type", exitNativeType);
            }

            // 15. App Update / Force Update via Remote Config
            if (root.has("app_update")) {
                org.json.JSONObject updateObj = root.optJSONObject("app_update");
                if (updateObj != null) {
                    boolean uAv = updateObj.optBoolean("update_available", updateObj.optInt("UpdateAvailable", updateObj.optInt("update_available", 0)) == 1);
                    boolean fUp = updateObj.optBoolean("force_update", updateObj.optInt("ForceUpdate", updateObj.optInt("force_update", 0)) == 1);
                    String vName = updateObj.optString("update_version_name", updateObj.optString("UpdateVersionName", "1.0"));
                    preferenceClass.setInt("UpdateAvailable", uAv ? 1 : 0);
                    preferenceClass.setInt("ForceUpdate", fUp ? 1 : 0);
                    if (!vName.trim().isEmpty()) {
                        preferenceClass.setDataType("UpdateVersionName", vName.trim());
                    }
                    String uTitle = updateObj.optString("update_title", "");
                    if (!uTitle.trim().isEmpty()) {
                        preferenceClass.setDataType("update_dialog_title", uTitle.trim());
                    }
                    String uDesc = updateObj.optString("update_description", "");
                    if (!uDesc.trim().isEmpty()) {
                        preferenceClass.setDataType("update_dialog_desc", uDesc.trim());
                    }
                }
            } else {
                if (root.has("update_available")) {
                    boolean uAv = root.optBoolean("update_available", false);
                    preferenceClass.setInt("UpdateAvailable", uAv ? 1 : 0);
                } else if (root.has("UpdateAvailable")) {
                    preferenceClass.setInt("UpdateAvailable", root.optInt("UpdateAvailable", 0));
                }
                if (root.has("force_update")) {
                    boolean fUp = root.optBoolean("force_update", false);
                    preferenceClass.setInt("ForceUpdate", fUp ? 1 : 0);
                } else if (root.has("ForceUpdate")) {
                    preferenceClass.setInt("ForceUpdate", root.optInt("ForceUpdate", 0));
                }
                if (root.has("update_version_name")) {
                    preferenceClass.setDataType("UpdateVersionName", root.optString("update_version_name", "1.0").trim());
                } else if (root.has("UpdateVersionName")) {
                    preferenceClass.setDataType("UpdateVersionName", root.optString("UpdateVersionName", "1.0").trim());
                }
            }

            Log.d("RemoteConfig", "app_ad_config parsed and applied successfully!");
        } catch (Exception e) {
            Log.e("RemoteConfig", "Error parsing app_ad_config: " + e.getMessage());
        }
    }
}
