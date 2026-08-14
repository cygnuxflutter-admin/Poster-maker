package com.festival.flyer.postermaker.activities;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceClass = new MailER_PreferenceClass(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spawner_activity_splash_screen);
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

    private void getData() {
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            database = FirebaseDatabase.getInstance();
            DatabaseReference project_data = database.getReference("all_data").child("datas");
            project_data.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.e("---API_DATA---", "Firebase onDataChange called. Data: " + snapshot.toString());
                    preferenceClass.setDataType("field_0", Objects.requireNonNull(snapshot.child("field_0").getValue()).toString());
                    preferenceClass.setDataType("field_1", Objects.requireNonNull(snapshot.child("field_1").getValue()).toString());
                    preferenceClass.setDataType("field_2", Objects.requireNonNull(snapshot.child("field_2").getValue()).toString());
                    preferenceClass.setDataType("field_3", Objects.requireNonNull(snapshot.child("field_3").getValue()).toString());
                    preferenceClass.setDataType("field_4", Objects.requireNonNull(snapshot.child("field_4").getValue()).toString());
                    preferenceClass.setDataType("field_5", Objects.requireNonNull(snapshot.child("field_5").getValue()).toString());
                    preferenceClass.setDataType("field_6", Objects.requireNonNull(snapshot.child("field_6").getValue()).toString());
                    preferenceClass.setDataType("field_7", Objects.requireNonNull(snapshot.child("field_7").getValue()).toString());
                    preferenceClass.setDataType("field_8", Objects.requireNonNull(snapshot.child("field_8").getValue()).toString());
                    preferenceClass.setDataType("field_9", Objects.requireNonNull(snapshot.child("field_9").getValue()).toString());
                    preferenceClass.setDataType("field_10", Objects.requireNonNull(snapshot.child("field_10").getValue()).toString());
                    preferenceClass.setDataType("field_11", Objects.requireNonNull(snapshot.child("field_11").getValue()).toString());
                    preferenceClass.setDataType("field_12", Objects.requireNonNull(snapshot.child("field_12").getValue()).toString());
                    preferenceClass.setDataType("field_13", Objects.requireNonNull(snapshot.child("field_13").getValue()).toString());
                    preferenceClass.setDataType("field_14", Objects.requireNonNull(snapshot.child("field_14").getValue()).toString());
                    preferenceClass.setDataType("field_15", Objects.requireNonNull(snapshot.child("field_15").getValue()).toString());
                    preferenceClass.setDataType("field_16", Objects.requireNonNull(snapshot.child("field_16").getValue()).toString());
                    preferenceClass.setDataType("field_17", Objects.requireNonNull(snapshot.child("field_17").getValue()).toString());
                    preferenceClass.setDataType("field_18", Objects.requireNonNull(snapshot.child("field_18").getValue()).toString());
                    preferenceClass.setDataType("field_19", Objects.requireNonNull(snapshot.child("field_19").getValue()).toString());
                    preferenceClass.setDataType("field_20", Objects.requireNonNull(snapshot.child("field_20").getValue()).toString());
                    preferenceClass.setDataType("field_21", Objects.requireNonNull(snapshot.child("field_21").getValue()).toString());
                    preferenceClass.setDataType("field_22", Objects.requireNonNull(snapshot.child("field_22").getValue()).toString());
                    preferenceClass.setDataType("field_23", Objects.requireNonNull(snapshot.child("field_23").getValue()).toString());
                    preferenceClass.setDataType("field_24", Objects.requireNonNull(snapshot.child("field_24").getValue()).toString());
                    preferenceClass.setDataType("field_25", Objects.requireNonNull(snapshot.child("field_25").getValue()).toString());
                    preferenceClass.setDataType("field_26", Objects.requireNonNull(snapshot.child("field_26").getValue()).toString());
                    preferenceClass.setDataType("field_27", Objects.requireNonNull(snapshot.child("field_27").getValue()).toString());
                    preferenceClass.setDataType("field_28", Objects.requireNonNull(snapshot.child("field_28").getValue()).toString());
                    preferenceClass.setDataType("field_29", Objects.requireNonNull(snapshot.child("field_29").getValue()).toString());
                    preferenceClass.setDataType("field_30", Objects.requireNonNull(snapshot.child("field_30").getValue()).toString());
                    preferenceClass.setDataType("field_31", Objects.requireNonNull(snapshot.child("field_31").getValue()).toString());
                    preferenceClass.setDataType("field_32", Objects.requireNonNull(snapshot.child("field_32").getValue()).toString());
                    preferenceClass.setDataType("field_33", Objects.requireNonNull(snapshot.child("field_33").getValue()).toString());
                    preferenceClass.setDataType("field_34", Objects.requireNonNull(snapshot.child("field_34").getValue()).toString());
                    preferenceClass.setDataType("field_35", Objects.requireNonNull(snapshot.child("field_35").getValue()).toString());
                    preferenceClass.setDataType("field_36", Objects.requireNonNull(snapshot.child("field_36").getValue()).toString());
                    preferenceClass.setDataType("field_37", Objects.requireNonNull(snapshot.child("field_37").getValue()).toString());
                    preferenceClass.setDataType("field_38", Objects.requireNonNull(snapshot.child("field_38").getValue()).toString());
                    preferenceClass.setDataType("field_39", Objects.requireNonNull(snapshot.child("field_39").getValue()).toString());
                    preferenceClass.setDataType("field_40", Objects.requireNonNull(snapshot.child("field_40").getValue()).toString());
                    preferenceClass.setDataType("field_41", Objects.requireNonNull(snapshot.child("field_41").getValue()).toString());
                    preferenceClass.setDataType("field_42", Objects.requireNonNull(snapshot.child("field_42").getValue()).toString());
                    preferenceClass.setDataType("field_43", Objects.requireNonNull(snapshot.child("field_43").getValue()).toString());
                    preferenceClass.setDataType("field_44", Objects.requireNonNull(snapshot.child("field_44").getValue()).toString());
                    preferenceClass.setDataType("field_45", Objects.requireNonNull(snapshot.child("field_45").getValue()).toString());
                    preferenceClass.setDataType("field_46", Objects.requireNonNull(snapshot.child("field_46").getValue()).toString());
                    preferenceClass.setDataType("field_47", Objects.requireNonNull(snapshot.child("field_47").getValue()).toString());
                    preferenceClass.setDataType("field_48", Objects.requireNonNull(snapshot.child("field_48").getValue()).toString());

                    Log.e("---API_DATA---", "Base URL (field_1): " + preferenceClass.getDataType("field_1"));
                    Log.e("---API_DATA---", "API Path (field_34): " + preferenceClass.getDataType("field_34"));

                    project_data2 = database.getReference("all_data").child("ad_data");
                    project_data2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            Log.e("snapshot", String.valueOf(snapshot));

                            // ----------------------------------------------- All Ads from Firebase
                                preferenceClass.setDataType("BannerAdunitID", Objects.requireNonNull(snapshot.child("BannerAdunitID").getValue()).toString());
                                Log.e("AD_CONFIG", "Banner Ad ID from Firebase: " + preferenceClass.getDataType("BannerAdunitID"));
                                preferenceClass.setDataType("CollapsibleBannerID", Objects.requireNonNull(snapshot.child("CollapsibleBannerID").getValue()).toString());
                                Log.e("AD_CONFIG", "Collapsible Banner ID from Firebase: " + preferenceClass.getDataType("CollapsibleBannerID"));
                                preferenceClass.setDataType("InterstitalAdunitID", Objects.requireNonNull(snapshot.child("InterstitalAdunitID").getValue()).toString());
                                Log.e("AD_CONFIG", "Interstitial Ad ID from Firebase: " + preferenceClass.getDataType("InterstitalAdunitID"));
                                preferenceClass.setDataType("RewardVideoUnitID", Objects.requireNonNull(snapshot.child("RewardVideoUnitID").getValue()).toString());
                                Log.e("AD_CONFIG", "Reward Video ID from Firebase: " + preferenceClass.getDataType("RewardVideoUnitID"));
                                preferenceClass.setDataType("NativeUnitID", Objects.requireNonNull(snapshot.child("NativeUnitID").getValue()).toString());
                                Log.e("AD_CONFIG", "Native Ad ID from Firebase: " + preferenceClass.getDataType("NativeUnitID"));
                                preferenceClass.setDataType("AppOpenID", Objects.requireNonNull(snapshot.child("AppOpenID").getValue()).toString());
                                Log.e("AD_CONFIG", "App Open Ad ID from Firebase: " + preferenceClass.getDataType("AppOpenID"));

                                // AdX IDs
                                if (snapshot.hasChild("AdxAppOpenID")) preferenceClass.setAdsId("AdxAppOpenID", snapshot.child("AdxAppOpenID").getValue().toString());
                                if (snapshot.hasChild("AdxBannerAdunitID")) preferenceClass.setAdsId("AdxBannerAdunitID", snapshot.child("AdxBannerAdunitID").getValue().toString());
                                if (snapshot.hasChild("AdxInterstitalAdunitID")) preferenceClass.setAdsId("AdxInterstitalAdunitID", snapshot.child("AdxInterstitalAdunitID").getValue().toString());
                                if (snapshot.hasChild("AdxNativeUnitID")) preferenceClass.setAdsId("AdxNativeUnitID", snapshot.child("AdxNativeUnitID").getValue().toString());
                                if (snapshot.hasChild("AdxRewardVideoUnitID")) preferenceClass.setAdsId("AdxRewardVideoUnitID", snapshot.child("AdxRewardVideoUnitID").getValue().toString());
                                
                                // Facebook IDs
                                if (snapshot.hasChild("fbBannerAdunitID")) preferenceClass.setAdsId("fbBannerAdunitID", snapshot.child("fbBannerAdunitID").getValue().toString());
                                if (snapshot.hasChild("fbInterstitalAdunitID")) preferenceClass.setAdsId("fbInterstitalAdunitID", snapshot.child("fbInterstitalAdunitID").getValue().toString());
                                if (snapshot.hasChild("fbNativeUnitID")) preferenceClass.setAdsId("fbNativeUnitID", snapshot.child("fbNativeUnitID").getValue().toString());

                                preferenceClass.setAdsStatus("bannerAdStatus", Integer.parseInt(Objects.requireNonNull(snapshot.child("bannerAdStatus").getValue()).toString()));
                                preferenceClass.setAdsStatus("interstitalAdStatus", Integer.parseInt(Objects.requireNonNull(snapshot.child("interstitalAdStatus").getValue()).toString()));
                                preferenceClass.setAdsStatus("EditScreenAdCount", Integer.parseInt(Objects.requireNonNull(snapshot.child("EditScreenAdCount").getValue()).toString()));
                                preferenceClass.setAdsStatus("BGSelectScreen_BannerAD", Integer.parseInt(Objects.requireNonNull(snapshot.child("BGSelectScreen_BannerAD").getValue()).toString()));
                                preferenceClass.setAdsStatus("ReadymadePoste_BannerAD", Integer.parseInt(Objects.requireNonNull(snapshot.child("ReadymadePoste_BannerAD").getValue()).toString()));
                                preferenceClass.setInt("MainScreen_Native", Integer.parseInt(Objects.requireNonNull(snapshot.child("MainScreen_Native").getValue()).toString()));

                                preferenceClass.setAdsId("google_Rw_ID", Objects.requireNonNull(snapshot.child("google_Rw_ID").getValue()).toString());
                                preferenceClass.setAdsId("PremiumAdType", Objects.requireNonNull(snapshot.child("PremiumAdType").getValue()).toString());

                                // DEBUG MODE PROTECTION: Override with AdMob Test IDs when running from Android Studio
                                // This ensures the developer's live AdMob account is never blocked due to self-testing.
                                if (com.festival.flyer.postermaker.BuildConfig.DEBUG) {
                                    preferenceClass.setDataType("AppOpenID", "ca-app-pub-3940256099942544/9257395921");
                                    preferenceClass.setDataType("BannerAdunitID", "ca-app-pub-3940256099942544/6300978111");
                                    preferenceClass.setDataType("CollapsibleBannerID", "ca-app-pub-3940256099942544/6300978111");
                                    preferenceClass.setDataType("InterstitalAdunitID", "ca-app-pub-3940256099942544/1033173712");
                                    preferenceClass.setDataType("NativeUnitID", "ca-app-pub-3940256099942544/2247696110");
                                    preferenceClass.setDataType("RewardVideoUnitID", "ca-app-pub-3940256099942544/5354046379");
                                    preferenceClass.setAdsId("google_Rw_ID", "ca-app-pub-3940256099942544/5354046379");
                                    Log.e("AD_CONFIG", "DEBUG MODE ACTIVE: Automatically using AdMob Test IDs to protect your account.");
                                }

//                            -------------------------------------------------------
                            MailER_AppOpenManager.AppOpenAdShow = Integer.parseInt(snapshot.child("AppOpenAdShow").getValue().toString());
                            MailER_InterstitialAdManager.InterAdTimer = Integer.parseInt(snapshot.child("InterAdTimer").getValue().toString());

                            preferenceClass.setInt("UpdateAvailable", Integer.parseInt(snapshot.child("UpdateAvailable").getValue().toString()));
                            preferenceClass.setDataType("UpdateVersionName", Objects.requireNonNull(snapshot.child("UpdateVersionName").getValue()).toString());

                            preferenceClass.setInt("download", Integer.parseInt(Objects.requireNonNull(snapshot.child("download").getValue()).toString()));
                            preferenceClass.setInt("splashscreen", Integer.parseInt(Objects.requireNonNull(snapshot.child("splashscreen").getValue()).toString()));

                            preferenceClass.setInt("IsEditScreenBannerAD", Integer.parseInt(Objects.requireNonNull(snapshot.child("IsEditScreenBannerAD").getValue()).toString()));
                            preferenceClass.setInt("rv_count", Integer.parseInt(Objects.requireNonNull(snapshot.child("rv_count").getValue()).toString()));
                            preferenceClass.setInt("First_rv_count", Integer.parseInt(Objects.requireNonNull(snapshot.child("First_rv_count").getValue()).toString()));
                            preferenceClass.setInt("PremiumPostCount", Integer.parseInt(Objects.requireNonNull(snapshot.child("PremiumPostCount").getValue()).toString()));

                            preferenceClass.setDataType("main_key", Objects.requireNonNull(snapshot.child("main_key").getValue()).toString());
                            preferenceClass.setDecryptionType(Integer.parseInt(Objects.requireNonNull(snapshot.child("decryptionType").getValue()).toString()));

                            Log.e("TAG", "onDataChange:MainScreen_Native "+ preferenceClass.getInt("MainScreen_Native", 0));
//                            AppOpenManager.loadGoogleRewardVideoAd(SplashScreen.this);
                            save_token(true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_SplashScreen.this, getResources().getString(R.string.something_went_wrong));
                        }
                    });
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
        if (preferenceClass.getInt("UpdateAvailable") == 1 && !preferenceClass.getAdsId("UpdateVersionName").equals(BuildConfig.VERSION_NAME)) {

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