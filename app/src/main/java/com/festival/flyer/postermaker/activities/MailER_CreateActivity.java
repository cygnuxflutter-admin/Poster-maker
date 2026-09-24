package com.festival.flyer.postermaker.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import com.festival.flyer.postermaker.utils.MailER_BottomNavHelper;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.adapter.MailER_ColorPelleteAdapter;
import com.festival.flyer.postermaker.threadTask.MailER_SaveBitmapTask;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MailER_CreateActivity extends AppCompatActivity {

    private String path;
    public File camera_file;
    public int CAMERA_INTENT = 1001;
    public int GALLERY_INTENT = 1002;
    
    // Bottom nav tabs
    private LinearLayout tabHome, tabExplore, tabCreate, tabCreations, tabSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_create);
        MailER_BottomNavHelper.setupBottomNav(this, R.id.tab_create);


        findViewById(R.id.ic_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        findViewById(R.id.btn_gallery).setOnClickListener(v -> openPhotoGallery());
        findViewById(R.id.btn_camera).setOnClickListener(v -> openCamera());
        findViewById(R.id.btn_colors).setOnClickListener(v -> openColorDialog());

        setupBottomNavigation();
    }

    public void openPhotoGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_INTENT);
    }

    public void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        camera_file = new File(getCacheDir(), ".temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", camera_file));
        startActivityForResult(intent, CAMERA_INTENT);
    }

    public void openColorDialog() {
        Dialog dialogColor = new Dialog(this, R.style.DialogTheme);
        dialogColor.setContentView(R.layout.spawner_color_pallete_layout);
        Objects.requireNonNull(dialogColor.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialogColor.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageView tvBack = dialogColor.findViewById(R.id.tv_back);
        tvBack.setOnClickListener(v -> dialogColor.dismiss());

        ImageView tvCustom = dialogColor.findViewById(R.id.tv_custom);
        tvCustom.setOnClickListener(v -> {
            dialogColor.dismiss();
            openColorPicker();
        });

        RecyclerView rvSubList = dialogColor.findViewById(R.id.rv_sub_list);
        rvSubList.setLayoutManager(new GridLayoutManager(this, 3));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = (displayMetrics).widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 3;
        int cellHeight = (700 * cellWidth) / 507;

        MailER_ColorPelleteAdapter colorListAdapter = new MailER_ColorPelleteAdapter(this, cellWidth, cellHeight, (colorCode) -> {
            Bitmap bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.parseColor(colorCode));
            new MailER_SaveBitmapTask(bitmap, new File(getCacheDir(), ".temp.jpg").getPath(), new MailER_SaveBitmapTask.OnColorBitmapListener() {
                @Override
                public void onDownloadComplete() {
                    startCrop(Uri.fromFile(new File(getCacheDir(), ".temp.jpg")));
                }
                @Override
                public void onError() { }
            }).execute();
        });

        rvSubList.setAdapter(colorListAdapter);
        dialogColor.show();
    }

    private void openColorPicker() {
        new AmbilWarnaDialog(this, Color.CYAN, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            public void onOk(AmbilWarnaDialog dialog, int color) {
                Bitmap bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(color);
                new MailER_SaveBitmapTask(bitmap, new File(getCacheDir(), ".temp.jpg").getPath(), new MailER_SaveBitmapTask.OnColorBitmapListener() {
                    @Override
                    public void onDownloadComplete() {
                        startCrop(Uri.fromFile(new File(getCacheDir(), ".temp.jpg")));
                    }
                    @Override
                    public void onError() { }
                }).execute();
            }
            public void onCancel(AmbilWarnaDialog dialog) { }
        }).show();
    }

    public void startCrop(@NonNull Uri uri) {
        String destinationFileName = "SampleCropImage.png";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(Color.parseColor("#7B2FF7")); // Primary Purple
        options.setStatusBarColor(Color.parseColor("#5A18C9")); // Darker Purple
        options.setToolbarWidgetColor(Color.WHITE);
        options.setActiveControlsWidgetColor(Color.parseColor("#7B2FF7")); // Replaces Orange with Purple
        options.setRootViewBackgroundColor(Color.WHITE); // Cleaner background
        options.setCropFrameColor(Color.WHITE); // White crop frame
        options.setCropGridColor(Color.WHITE); // White grid
        
        options.setAspectRatioOptions(1,
                new AspectRatio("1:1", 1, 1),
                new AspectRatio("3:2", 3, 2),
                new AspectRatio("2:3", 2, 3),
                new AspectRatio("4:3", 4, 3),
                new AspectRatio("3:4", 3, 4),
                new AspectRatio("16:9", 16, 9),
                new AspectRatio("5:4", 5, 4),
                new AspectRatio("4:5", 4, 5));
        uCrop.withOptions(options).start(this);
    }

    private void setupBottomNavigation() {
        tabHome = findViewById(R.id.tab_home);
        tabExplore = findViewById(R.id.tab_explore);
        tabCreate = findViewById(R.id.tab_create);
        tabCreations = findViewById(R.id.tab_creations);
        tabSettings = findViewById(R.id.tab_settings);

        tabHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MailER_PosterMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        tabExplore.setOnClickListener(v -> {
            Intent intent = new Intent(this, MailER_TemplateSelectionActivity.class);
            startActivity(intent);
            finish();
        });

        tabCreations.setOnClickListener(v -> {
            Intent intent = new Intent(this, MailER_MyCreationActivity.class);
            startActivity(intent);
            finish();
        });

        tabSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, MailER_SettingsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA_INTENT) {
            Uri selectedImage = Uri.fromFile(camera_file);
            startCrop(selectedImage);
        }
        if (resultCode == RESULT_OK && requestCode == GALLERY_INTENT && data != null) {
            Uri selectedImage = data.getData();
            assert selectedImage != null;
            startCrop(selectedImage);
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                path = resultUri.toString();
                if (MailER_NetworkUtils.isNetworkAvailable(this)) {
                    MyApplication.showInterstitialAd(this, this::startIntent);
                } else {
                    Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
            final Throwable resultUri = UCrop.getError(data);
            Toast.makeText(this, resultUri.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startIntent() {
        Intent intent = new Intent(this, MailER_PosterEditActivity.class);
        intent.putExtra("bg_path", path);
        intent.putExtra("loadUserFrame", true);
        intent.putExtra("Temp_Type", "MY_TEMP");
        startActivity(intent);
    }
}
