package com.festival.flyer.postermaker;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.facebook.ads.AudienceNetworkAds;
import com.festival.flyer.postermaker.adManager.MailER_AppOpenManager;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
//import com.google.android.play.core.review.ReviewInfo;
//import com.google.android.play.core.review.ReviewManager;
//import com.google.android.play.core.review.ReviewManagerFactory;
//import com.google.android.play.core.tasks.Task;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MyApplication extends android.app.Application {


    public static Context context;

    public static MyApplication myApplication;

/*
    public static void showInterstitialAdWithOutCount(Activity activity, InterstitialAdManager.OnAdLoadInterface onAdLoadInterface) {
        ((MyApplication) activity.getApplication()).getInterstitialAdManager().showInterstitialAd(activity, onAdLoadInterface);
    }
*/


    public static boolean isShowingAppOpen = true, isAdsSplash = true;
    public MailER_AppOpenManager appOpenManager;
    private MailER_InterstitialAdManager interstitialAdManager;
    public static MyApplication mInstance;

    public static void showInterstitialAd(Activity activity, MailER_InterstitialAdManager.OnAdLoadInterface onAdLoadInterface) {
        ((MyApplication) activity.getApplication()).getInterstitialAdManager().showAdIfAvailable(activity, onAdLoadInterface);
    }

    public static void showInterstitialAdWithOutCount(Activity activity, MailER_InterstitialAdManager.OnAdLoadInterface onAdLoadInterface) {
        ((MyApplication) activity.getApplication()).getInterstitialAdManager().showInterstitialAd(activity, onAdLoadInterface);
    }

    public static void showEditInterstitialAd(Activity activity, MailER_InterstitialAdManager.OnAdLoadInterface onAdLoadInterface) {
        ((MyApplication) activity.getApplication()).getInterstitialAdManager().showEDitAdIfAvailable(activity, onAdLoadInterface);
    }

    public MailER_InterstitialAdManager getInterstitialAdManager() {
        if (interstitialAdManager == null) {
            interstitialAdManager = new MailER_InterstitialAdManager(this);
        }
        return interstitialAdManager;
    }

    public void loadInterstitialAd() {
        if (interstitialAdManager == null)
            interstitialAdManager = new MailER_InterstitialAdManager(MyApplication.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;


        myApplication = this;

//        connectivity = new MailER_Connectivity(this);
//        prefManager = new MailER_PrefManager(this);

        context = this;


 /*       MobileAds.initialize(this, new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });*/
        /*       AudienceNetworkAds.initialize(this);*/
        MobileAds.initialize(this, initializationStatus -> Log.d(" AD", " poster open ad"));
//        appOpenAdManager = new AppOpenManager(this);

        appOpenManager = new MailER_AppOpenManager(this);


        AudienceNetworkAds.initialize(this);

        // Debug ma tamara phone ne Test Device banave → Account Safe
        // Release (Play Store) ma aa code chalse j nahi → Real Ads aavse
        if (BuildConfig.DEBUG) {
            List<String> testDeviceIds = Collections.singletonList("9EB1C89D5458256B2C93F844BAAC93F5");
            RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }

        MobileAds.initialize(this, initializationStatus -> Log.d(" AD", " poster open ad"));

    }

    public static synchronized MyApplication getInstance() {
        MyApplication myApp;
        synchronized (MyApplication.class) {
            myApp = mInstance;
        }
        return myApp;
    }


    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    public void showAdIfAvailable(@NonNull Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        appOpenManager.showAdIfSplashAvailable(activity, onShowAdCompleteListener);
    }

    public void showAdIfHomeAvailable(@NonNull Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        appOpenManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    public void sendRequest() {
        appOpenManager.sendRequest();
    }

    public boolean isAdAvailable() {
        return appOpenManager.isAdAvailable();
    }

//    public void appReview(Activity activity) {
//        ReviewManager reviewManager = ReviewManagerFactory.create(activity);
//        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
//        request.addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                ReviewInfo reviewInfo = task.getResult();
//                Task<Void> flow = reviewManager.launchReviewFlow(activity, reviewInfo);
//                flow.addOnCompleteListener(task1 -> {
//
//                });
//            }
//        });
//    }

    public String GetMainPath() {
        String folderName = getString(R.string.app_name);
        String sPath = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            sPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            File dir = new File(sPath);
            if (dir.exists()) {
                sPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + folderName;
                dir = new File(sPath);
                dir.mkdirs();
            } else {
                sPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + folderName;
                dir = new File(sPath);
                dir.mkdirs();
            }
        } else {
            sPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + folderName;
            File dir = new File(sPath);
            if (!dir.exists()) {
                dir.mkdirs();
                if (!dir.exists()) {
                    sPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                    dir = new File(sPath);
                    if (dir.exists()) {
                        sPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + folderName;
                        dir = new File(sPath);
                        dir.mkdirs();
                    } else {
                        sPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + folderName;
                        dir = new File(sPath);
                        dir.mkdirs();
                    }
                }
            }
        }
        return sPath;
    }

    public static Display getDefaultDisplay() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display;
    }


}