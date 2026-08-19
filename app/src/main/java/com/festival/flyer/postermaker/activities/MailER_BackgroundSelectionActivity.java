package com.festival.flyer.postermaker.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager;
import com.festival.flyer.postermaker.controller.MailER_BGSelectionController;
import com.festival.flyer.postermaker.fragment.MailER_BackgroundFragment;
import com.festival.flyer.postermaker.threadTask.MailER_DlBgAsync;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

public class MailER_BackgroundSelectionActivity extends AppCompatActivity implements MailER_BackgroundFragment.GetPosterListener {

    private final ArrayList<String> strings = new ArrayList<>();
    private MailER_BGSelectionController bgSelectionController;
    private String mode, path;
    private boolean local = false;
    private boolean isRewarded = false;
    private boolean local_permission = false;
    private boolean isProModeActive = false;
    private MailER_PreferenceClass preferenceClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_background_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        com.festival.flyer.postermaker.utils.MailER_BottomNavHelper.setupBottomNav(this, R.id.tab_home);

        View statusBarSpacer = findViewById(R.id.status_bar_spacer);
        // WindowInsets are handled by fitsSystemWindows on root now
        if (statusBarSpacer != null) {
            statusBarSpacer.setVisibility(View.GONE);
        }

        View bottomLy = findViewById(R.id.bottom_ly);
        View cameraContainer = findViewById(R.id.camera_container);

        findByID();

        preferenceClass = new MailER_PreferenceClass(this);
        ShimmerFrameLayout shimmer_view_container = this.findViewById(R.id.shimmer_view_container);
        RelativeLayout rl_ad = this.findViewById(R.id.rl_ad);
        RelativeLayout btm = this.findViewById(R.id.btm);


        int BannerAdStatus = preferenceClass.getAdsStatus("BGSelectScreen_BannerAD");
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            if (BannerAdStatus == 1) {
                btm.setVisibility(View.VISIBLE);
                MailER_LoadAds.loadAdmobBannerAd(this, rl_ad, shimmer_view_container);
            } else {
                btm.setVisibility(View.GONE);
            }
        }


        findViewById(R.id.ic_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        findViewById(R.id.ic_gallery).setOnClickListener(v -> bgSelectionController.openPhotoGallery());

        findViewById(R.id.ic_camera).setOnClickListener(v ->
        {
            if (!checkPermission()) {
                try {
                    requestPermission();

//                if (Build.MANUFACTURER.equals("TECNO MOBILE LIMITED") && Build.BRAND.equals("TECNO") ||
//                        Build.BRAND.equals("samsung") || Build.BRAND.equals("OPPO") ||
//                        Build.BRAND.equals("vivo")){
                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_BackgroundSelectionActivity.this);
//                    Log.e("#brand", Build.BRAND);
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
//                    intent.setData(uri);
//                    startActivity(intent);
//                }

                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                bgSelectionController.openCamera();
            }
        });

        findViewById(R.id.ic_color).setOnClickListener(v -> bgSelectionController.openColorDialog());

        View proToggle = findViewById(R.id.ll_pro_toggle);
        if (proToggle != null) {
            proToggle.setOnClickListener(v -> {
                isProModeActive = !isProModeActive;
                if (isProModeActive) {
                    proToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFD700"))); // Gold color for active
                } else {
                    proToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF8C00"))); // Orange for inactive
                }
                if (bgSelectionController != null) {
                    bgSelectionController.applyProFilter(isProModeActive);
                }
            });
        }
    }

    private void findByID() {
        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(this);
        mode = getIntent().getStringExtra("mode");
        bgSelectionController = new MailER_BGSelectionController(this, getSupportFragmentManager(), preferenceClass, mode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == bgSelectionController.CAMERA_INTENT) {
            Uri selectedImage = Uri.fromFile(bgSelectionController.camera_file);
            if (mode.equals("user")) {
                Intent intent = new Intent();
                intent.putExtra("local", true);
                intent.putExtra("bg_image", selectedImage.toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                bgSelectionController.startCrop(selectedImage);
            }
        }
        if (resultCode == RESULT_OK && requestCode == bgSelectionController.GALLERY_INTENT && data != null) {
            Uri selectedImage = data.getData();
            assert selectedImage != null;
            if (mode.equals("user")) {
                Intent intent = new Intent();
                intent.putExtra("local", true);
                intent.putExtra("bg_image", selectedImage.toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                bgSelectionController.startCrop(selectedImage);
            }
        }

        if (resultCode == RESULT_CANCELED && requestCode == UCrop.REQUEST_CROP) {
            isRewarded = false;
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;//file:///storage/emulated/0/SampleCropImage.png
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                path = resultUri.toString();
                if (MailER_NetworkUtils.isNetworkAvailable(this)) {
                    if (isRewarded) {
                        if (mode.equals("user")) {
                            setPosterIntent();
                        } else {
                            startIntent();
                        }
                        isRewarded = false;
                        return;
                    }

                    MyApplication.showInterstitialAd(this, this::startIntent);

                } else {
                    Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
            final Throwable resultUri = UCrop.getError(data);
            Toast.makeText(this, resultUri.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPosterClick(String path, boolean premium) {
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED || checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                    } else {
//                        MailER_MaterialDialogUtils.getInstance().PermissionDialog(this);
//                    }
//                    return;
//                }
//            }
            strings.clear();
            strings.add(path);
            if (premium) {
                MailER_MaterialDialogUtils.getInstance().rewardDialog(this, "Use Background(One Time)", "Use premium background by watching Ads", materialDialog -> {
//                    bgSelectionController.startMaterialDialog();
                    MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(this);
                    if (preferenceClass.getDataType("PremiumAdType") != null && preferenceClass.getDataType("PremiumAdType").equals("Reward")) {
                        MailER_RewardVideoManager.showRewardVideoAd(MailER_BackgroundSelectionActivity.this, new MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                            @Override
                            public void onAdClose() {
                                isRewarded = true;
                                downloadTask(strings);
                            }
                        });
                    } else {
                        MyApplication.showInterstitialAdWithOutCount(MailER_BackgroundSelectionActivity.this, () -> {
                            isRewarded = true;
                            downloadTask(strings);
                        });
                    }
//                    MyApplication.showInterstitialAd(this,() -> downloadTask(strings));
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
                }, materialDialog -> {
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
                });
            } else {
                downloadTask(strings);
            }
        } else {
            Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadTask(ArrayList<String> strings) {
        bgSelectionController.startMaterialDialog();
        new MailER_DlBgAsync(MailER_BackgroundSelectionActivity.this, strings, new MailER_DlBgAsync.OnDownloadTemplateListener() {
            @Override
            public void onDownloadComplete() {
                bgSelectionController.dismissMaterialDialog();
                if (mode.equals("user")) {
                    local = false;
                    path = MailER_FileUtils.getFile(MailER_BackgroundSelectionActivity.this, strings.get(0));

                    if (isRewarded) {
                        setPosterIntent();
                    } else {
                        MyApplication.showInterstitialAd(MailER_BackgroundSelectionActivity.this, () -> setPosterIntent());
                    }
                } else {
                    if (isRewarded) {
                        bgSelectionController.startCrop(Uri.fromFile(new File(MailER_FileUtils.getFile(MailER_BackgroundSelectionActivity.this, strings.get(0)))));
                    } else {
                        MyApplication.showInterstitialAd(MailER_BackgroundSelectionActivity.this, () -> {
                            bgSelectionController.startCrop(Uri.fromFile(new File(MailER_FileUtils.getFile(MailER_BackgroundSelectionActivity.this, strings.get(0)))));
                        });
                    }

                }
            }

            @Override
            public void onError() {
                Toast.makeText(MailER_BackgroundSelectionActivity.this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                bgSelectionController.dismissMaterialDialog();
            }
        }).execute();
    }

    private void startIntent() {
        Intent intent = new Intent(this, MailER_PosterEditActivity.class);
        intent.putExtra("bg_path", path);
        intent.putExtra("loadUserFrame", true);
        intent.putExtra("Temp_Type", "MY_TEMP");
        startActivity(intent);
    }

    private void setPosterIntent() {
        Intent intent = new Intent();
        intent.putExtra("local", local);
        intent.putExtra("bg_image", MailER_FileUtils.getFile(MailER_BackgroundSelectionActivity.this, path));
        setResult(RESULT_OK, intent);
        finish();
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(MailER_BackgroundSelectionActivity.this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA

            }, 1);
        } else {
            ActivityCompat.requestPermissions(MailER_BackgroundSelectionActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA

            }, 1);
        }

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int result1 = ContextCompat.checkSelfPermission(MailER_BackgroundSelectionActivity.this, Manifest.permission.READ_MEDIA_IMAGES);
            int result2 = ContextCompat.checkSelfPermission(MailER_BackgroundSelectionActivity.this, Manifest.permission.CAMERA);

            if (result1 == 0 && result2 == 0) {
                return true;
            }
        } else {
            int result = ContextCompat.checkSelfPermission(MailER_BackgroundSelectionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(MailER_BackgroundSelectionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(MailER_BackgroundSelectionActivity.this, Manifest.permission.CAMERA);

            if (result == 0 && result1 == 0 && result2 == 0) {
                return true;
            }
        }

        return false;
    }

}