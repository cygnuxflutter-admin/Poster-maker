package com.festival.flyer.postermaker.activities;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class MailER_SplashScreen extends AppCompatActivity {

    private MailER_PreferenceClass preferenceClass;
    private FirebaseDatabase database;
    private DatabaseReference project_data2;
//    private AppUpdateManager appUpdateManager;
    private Dialog dialog;

    private long startTime;
    private static final long MIN_SPLASH_TIME = 3000; // 3.0 seconds

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

//        if (!preferenceClass.isFirstTimeLaunch()) {
//            getData();
//        } else {
//            @SuppressLint("SimpleDateFormat")
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
//            Calendar calender = Calendar.getInstance();
//            String start_date = preferenceClass.getFirstDate();
//            String end_date = simpleDateFormat.format(calender.getTime());
//
//            if (start_date != null) {
//                long findDiff = findDifference(start_date, end_date);
//                if (findDiff >= 2) {
//                    getData();
//                } else {
//                    save_token(false);
//                }
//            } else {
//                getData();
//            }
//        }
        if (MailER_NetworkUtils.isNetworkAvailable(MailER_SplashScreen.this)) {
            getData();
        } else {
            Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        }

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
                return Integer.parseInt(String.valueOf(snapshot.child(key).getValue()).trim());
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
            project_data.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Log.e("---API_DATA---", "Firebase onDataChange called. Data: " + snapshot.toString());
                        for (int i = 0; i <= 48; i++) {
                            String key = "field_" + i;
                            preferenceClass.setDataType(key, getSnapshotString(snapshot, key, ""));
                        }

                        Log.e("---API_DATA---", "Base URL (field_1): " + preferenceClass.getDataType("field_1"));
                        Log.e("---API_DATA---", "API Path (field_34): " + preferenceClass.getDataType("field_34"));

                        project_data2 = database.getReference("all_data").child("ad_data");
                        project_data2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    Log.e("snapshot", String.valueOf(snapshot));

                                    // ----------------------------------------------- All Ads from Firebase
                                    preferenceClass.setDataType("BannerAdunitID", getSnapshotString(snapshot, "BannerAdunitID", ""));
                                    preferenceClass.setDataType("CollapsibleBannerID", getSnapshotString(snapshot, "CollapsibleBannerID", ""));
                                    preferenceClass.setDataType("InterstitalAdunitID", getSnapshotString(snapshot, "InterstitalAdunitID", ""));
                                    preferenceClass.setDataType("RewardVideoUnitID", getSnapshotString(snapshot, "RewardVideoUnitID", ""));
                                    preferenceClass.setDataType("NativeUnitID", getSnapshotString(snapshot, "NativeUnitID", ""));
                                    preferenceClass.setDataType("AppOpenID", getSnapshotString(snapshot, "AppOpenID", ""));

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

                                    preferenceClass.setAdsStatus("bannerAdStatus", getSnapshotInt(snapshot, "bannerAdStatus", 0));
                                    preferenceClass.setAdsStatus("interstitalAdStatus", getSnapshotInt(snapshot, "interstitalAdStatus", 0));
                                    preferenceClass.setAdsStatus("EditScreenAdCount", getSnapshotInt(snapshot, "EditScreenAdCount", 0));
                                    preferenceClass.setAdsStatus("BGSelectScreen_BannerAD", getSnapshotInt(snapshot, "BGSelectScreen_BannerAD", 0));
                                    preferenceClass.setAdsStatus("ReadymadePoste_BannerAD", getSnapshotInt(snapshot, "ReadymadePoste_BannerAD", 0));
                                    preferenceClass.setInt("MainScreen_Native", getSnapshotInt(snapshot, "MainScreen_Native", 0));

                                    preferenceClass.setAdsId("google_Rw_ID", getSnapshotString(snapshot, "google_Rw_ID", ""));
                                    preferenceClass.setAdsId("PremiumAdType", getSnapshotString(snapshot, "PremiumAdType", ""));

                                    // DEBUG MODE PROTECTION: Override with AdMob Test IDs when running from Android Studio
                                    if (com.festival.flyer.postermaker.BuildConfig.DEBUG) {
                                        preferenceClass.setDataType("AppOpenID", "ca-app-pub-3940256099942544/9257395921");
                                        preferenceClass.setDataType("BannerAdunitID", "ca-app-pub-3940256099942544/6300978111");
                                        preferenceClass.setDataType("CollapsibleBannerID", "ca-app-pub-3940256099942544/6300978111");
                                        preferenceClass.setDataType("InterstitalAdunitID", "ca-app-pub-3940256099942544/1033173712");
                                        preferenceClass.setDataType("NativeUnitID", "ca-app-pub-3940256099942544/2247696110");
                                        preferenceClass.setDataType("RewardVideoUnitID", "ca-app-pub-3940256099942544/5354046379");
                                        preferenceClass.setAdsId("google_Rw_ID", "ca-app-pub-3940256099942544/5354046379");
                                    }

                                    MailER_AppOpenManager.AppOpenAdShow = getSnapshotInt(snapshot, "AppOpenAdShow", 0);
                                    MailER_InterstitialAdManager.InterAdTimer = getSnapshotInt(snapshot, "InterAdTimer", 0);

                                    preferenceClass.setInt("UpdateAvailable", getSnapshotInt(snapshot, "UpdateAvailable", 0));
                                    preferenceClass.setDataType("UpdateVersionName", getSnapshotString(snapshot, "UpdateVersionName", "1.0"));

                                    preferenceClass.setInt("download", getSnapshotInt(snapshot, "download", 0));
                                    preferenceClass.setInt("splashscreen", getSnapshotInt(snapshot, "splashscreen", 0));

                                    preferenceClass.setInt("IsEditScreenBannerAD", getSnapshotInt(snapshot, "IsEditScreenBannerAD", 0));
                                    preferenceClass.setInt("rv_count", getSnapshotInt(snapshot, "rv_count", 0));
                                    preferenceClass.setInt("First_rv_count", getSnapshotInt(snapshot, "First_rv_count", 0));
                                    preferenceClass.setInt("PremiumPostCount", getSnapshotInt(snapshot, "PremiumPostCount", 0));

                                    preferenceClass.setDataType("main_key", getSnapshotString(snapshot, "main_key", ""));
                                    preferenceClass.setDecryptionType(getSnapshotInt(snapshot, "decryptionType", 0));

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
                    MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_SplashScreen.this, getResources().getString(R.string.something_went_wrong));
                }
            });
        } else {
            MailER_MaterialDialogUtils.getInstance().errorDialog(this, getResources().getString(R.string.internet_error));
        }
    }

    private void save_token(boolean update) {
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
        boolean isUpdateRequired = false;
        try {
            String firebaseVerStr = preferenceClass.getDataType("UpdateVersionName", "0");
            double firebaseVer = Double.parseDouble(firebaseVerStr);
            double currentAppVer = Double.parseDouble(BuildConfig.VERSION_NAME);
            if (firebaseVer > currentAppVer) {
                isUpdateRequired = true;
            }
        } catch (Exception e) {
            isUpdateRequired = !preferenceClass.getDataType("UpdateVersionName").equals(BuildConfig.VERSION_NAME);
        }

        if (preferenceClass.getInt("UpdateAvailable") == 1 && isUpdateRequired) {

            dialog = new Dialog(MailER_SplashScreen.this);
            dialog.setContentView(R.layout.spawner_dialog_app_info);
            TextView descriptionTextView = dialog.findViewById(R.id.descriptionTextView);
            Button cancelBtn = dialog.findViewById(R.id.dialogCancelButton);
            TextView msgTextView = dialog.findViewById(R.id.titleTextView);
            Button okBtn = dialog.findViewById(R.id.dialogOkButton);
            descriptionTextView.setTextColor(getResources().getColor(R.color.black));
            descriptionTextView.setText("There is a newer version of app available please update it now.");
            msgTextView.setTextColor(getResources().getColor(R.color.black));
            msgTextView.setText("Update Available");
            okBtn.setText("Update");
            cancelBtn.setText("Cancel");

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    startIntent();
                }
            });
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    } catch (Exception e) {
                        e.getMessage();
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
        ((MyApplication) getApplicationContext()).sendRequest();
        ((MyApplication) getApplicationContext()).loadInterstitialAd();

        Intent intent = new Intent(getApplicationContext(), MailER_PosterMainActivity.class);
        startActivity(intent);
        finish();
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
        /*if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 0x11);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            });
            appUpdateManager.getAppUpdateInfo().addOnFailureListener(e -> startIntent());
        }*/
    }

}