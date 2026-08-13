package com.festival.flyer.postermaker.controller;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_ColorPelleteAdapter;
import com.festival.flyer.postermaker.fragment.MailER_BackgroundFragment;
import com.festival.flyer.postermaker.fragment.MailER_BackgroundPagerAdapter;
import com.festival.flyer.postermaker.model.MailER_BgModel;
import com.festival.flyer.postermaker.threadTask.MailER_GetBgData;
import com.festival.flyer.postermaker.threadTask.MailER_SaveBitmapTask;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.view.MailER_PagerSlidingTabStrip;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MailER_BGSelectionController {

    public final int CAMERA_INTENT = 905;
    public final int GALLERY_INTENT = 907;
    public String mode;
    public File camera_file;

    private final Activity activity;
    private final FragmentManager supportFragmentManager;
    private final MailER_PreferenceClass preferenceClass;
    private MaterialDialog materialDialog;

    public MailER_BGSelectionController(Activity activity, FragmentManager supportFragmentManager, MailER_PreferenceClass preferenceClass, String mode) {
        this.activity = activity;
        this.supportFragmentManager = supportFragmentManager;
        this.preferenceClass = preferenceClass;
        this.mode = mode;
        loadBgImages();
    }

    public void loadBgImages() {
        startMaterialDialog();
        getBgThumb(preferenceClass.getDataType("field_0"));
    }

    private void getBgThumb(String key) {
        String requestUrl = preferenceClass.getDataType("field_1") + preferenceClass.getDataType("field_34") + preferenceClass.getDataType("field_38");
        Log.e("---API_DATA---", "--- REQUEST START (BG Thumb) ---");
        Log.e("---API_DATA---", "URL: " + requestUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, response -> {
            Log.e("---API_DATA---", "--- RESPONSE START (BG Thumb) ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Response: " + response);
            String text1 = null;

            Log.d("qwertyu", "getBgThumb: " + response);
//            if (preferenceClass.getDecryptionType() == 0) {
//                byte[] octets = Base64.decode(response, Base64.URL_SAFE);
//                String text_base = new String(octets, StandardCharsets.UTF_8);
//                byte[] octets1 = Base64.decode(text_base, Base64.URL_SAFE);
//                text1 = new String(octets1, StandardCharsets.UTF_8);
//            } else if (preferenceClass.getDecryptionType() == 1) {
//                MainSecurity decrypted = MainSecurity.decrypt(preferenceClass.getDataType("main_key"), response);
//                text1 = decrypted.getData();
//            }
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray(preferenceClass.getDataType("field_39"));
                if (jsonArray.length() == 0) {
                    MailER_MaterialDialogUtils.getInstance().errorDialog2(activity, activity.getResources().getString(R.string.something_went_wrong));
                } else {
                    new MailER_GetBgData(preferenceClass, jsonArray, new MailER_GetBgData.OnGetCatDataListener() {
                        @Override
                        public void onGetDataComplete(ArrayList<MailER_BgModel> posterDataLists) {
                            setPagerAdapter(posterDataLists);
                        }

                        @Override
                        public void onError() {
                            dismissMaterialDialog();
                            MailER_MaterialDialogUtils.getInstance().errorDialog(activity, activity.getResources().getString(R.string.something_went_wrong));
                        }
                    }).execute();
                }
            } catch (Exception e) {
                dismissMaterialDialog();
                MailER_MaterialDialogUtils.getInstance().errorDialog(activity, activity.getResources().getString(R.string.something_went_wrong));
                e.printStackTrace();
            }
        }, error -> {
            Log.e("---API_DATA---", "--- ERROR (BG Thumb) ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Error: " + error.getMessage());
            dismissMaterialDialog();
            MailER_MaterialDialogUtils.getInstance().errorDialog(activity, activity.getResources().getString(R.string.something_went_wrong));
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put(preferenceClass.getDataType("field_47"), key);
                postMap.put(preferenceClass.getDataType("field_48"), "1");
                Log.e("---API_DATA---", "Params: " + postMap);
                return postMap;
            }
        };
        Volley.newRequestQueue(activity).add(stringRequest);
    }

    private void setPagerAdapter(ArrayList<MailER_BgModel> posterDataLists) {
        MailER_BackgroundFragment.TempisFirstShow = false;
        MailER_PagerSlidingTabStrip tabs = activity.findViewById(R.id.pagerSlidingTabStrip);
        ViewPager viewPager = activity.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MailER_BackgroundPagerAdapter(supportFragmentManager, posterDataLists));
        viewPager.setCurrentItem(0);
        tabs.setViewPager(viewPager);
        dismissMaterialDialog();
    }

    public void openPhotoGallery() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (activity.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
//                    || activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    activity.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                } else {
//                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(activity);
//                }
//                return;
//            }
//        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.PICK");
        activity.startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(R.string.select_picture)), GALLERY_INTENT);
    }

    public void openCamera() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (activity.checkSelfPermission("android.permission.CAMERA") != PERMISSION_GRANTED
//                    || activity.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PERMISSION_GRANTED
//                    || activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    activity.requestPermissions(new String[]{"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                } else {
//                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(activity);
//                }
//                return;
//            }
//        }

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        camera_file = new File(Environment.getExternalStorageDirectory(), ".temp.jpg");
        camera_file = new File(activity.getCacheDir(), ".temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider",
                        camera_file));

        activity.startActivityForResult(intent, CAMERA_INTENT);


    }

    public void openColorDialog() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (activity.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
//                    || activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    activity.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                } else {
//                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(activity);
//                }
//                return;
//            }
//        }
        itemColorAdapter();
    }

    public void itemColorAdapter() {
        Dialog dialogColor = new Dialog(activity, R.style.DialogTheme);
        dialogColor.setContentView(R.layout.spawner_color_pallete_layout);

        Objects.requireNonNull(dialogColor.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation_2;

        dialogColor.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        ImageView tvBack = dialogColor.findViewById(R.id.tv_back);

        tvBack.setOnClickListener(v -> dialogColor.dismiss());

        ImageView tvCustom = dialogColor.findViewById(R.id.tv_custom);
        tvCustom.setOnClickListener(v -> {
            dialogColor.dismiss();
            openColorPicker();
        });

        RecyclerView rvSubList = dialogColor.findViewById(R.id.rv_sub_list);
        rvSubList.setLayoutManager(new GridLayoutManager(activity, 3));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = activity.getResources().getDisplayMetrics().density;
        int screenWidth = (displayMetrics).widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 3;
        int cellHeight = (700 * cellWidth) / 507;

        MailER_ColorPelleteAdapter colorListAdapter = new MailER_ColorPelleteAdapter(activity, cellWidth, cellHeight, (colorCode) -> {
            Bitmap bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.parseColor(colorCode));
            new MailER_SaveBitmapTask(bitmap, new File(/*Environment.getExternalStorageDirectory()*/activity.getCacheDir(), ".temp.jpg").getPath(), new MailER_SaveBitmapTask.OnColorBitmapListener() {
                @Override
                public void onDownloadComplete() {
                    if (mode.equals("user")) {
                        Intent intent = new Intent();
                        intent.putExtra("local", false);
                        intent.putExtra("bg_image", new File(activity.getCacheDir(), ".temp.jpg").getPath());
//                        intent.putExtra("bg_image", new File(Environment.getExternalStorageDirectory(), ".temp.jpg").getPath());
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();
                    } else {
                        startCrop(Uri.fromFile(new File(activity.getCacheDir(), ".temp.jpg")));
//                        startCrop(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), ".temp.jpg")));
                    }
                }

                @Override
                public void onError() {
                    //Toast.makeText(activity, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                }
            }).execute();
        });

        rvSubList.setAdapter(colorListAdapter);

        dialogColor.show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA

            }, 1);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA

            }, 1);
        }

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

            if (result1 == 0 && result2 == 0) {
                return true;
            }
        } else {
            int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

            if (result == 0 && result1 == 0 && result2 == 0) {
                return true;
            }
        }

        return false;
    }

    private void openColorPicker() {
        new AmbilWarnaDialog(activity, Color.CYAN, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            public void onOk(AmbilWarnaDialog dialog, int color) {
                Bitmap bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(color);
                new MailER_SaveBitmapTask(bitmap, new File(/*Environment.getExternalStorageDirectory()*/activity.getCacheDir(), ".temp.jpg").getPath(), new MailER_SaveBitmapTask.OnColorBitmapListener() {
                    @Override
                    public void onDownloadComplete() {
                        if (mode.equals("user")) {
                            Intent intent = new Intent();
                            intent.putExtra("local", false);
//                            intent.putExtra("bg_image", new File(Environment.getExternalStorageDirectory(), ".temp.jpg").getPath());
                            intent.putExtra("bg_image", new File(activity.getCacheDir(), ".temp.jpg").getPath());
                            activity.setResult(RESULT_OK, intent);
                            activity.finish();
                        } else {
//                            startCrop(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), ".temp.jpg")));
                            startCrop(Uri.fromFile(new File(activity.getCacheDir(), ".temp.jpg")));
                        }
                    }

                    @Override
                    public void onError() {
                        // Toast.makeText(activity, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                    }
                }).execute();
            }

            public void onCancel(AmbilWarnaDialog dialog) {
            }
        }).show();
    }

    public void startCrop(@NonNull Uri uri) {
        String destinationFileName = "SampleCropImage.png";
//        File file=new File(activity.getCacheDir(), destinationFileName);
//        if (file.exists()){
//            file.delete();
//        }
//        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), destinationFileName)));
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(activity.getCacheDir(), destinationFileName)));
        uCrop = advancedConfig(uCrop);
        uCrop.start(activity);
    }


    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(activity, R.color.purple_700));
        options.setStatusBarColor(ContextCompat.getColor(activity, R.color.purple_700));
        options.setToolbarWidgetColor(Color.WHITE);
        options.setRootViewBackgroundColor(ContextCompat.getColor(activity, R.color.purple_200));
        options.setAspectRatioOptions(1,
                new AspectRatio("1:1", 1, 1),
                new AspectRatio("3:2", 3, 2),
                new AspectRatio("2:3", 2, 3),
                new AspectRatio("4:3", 4, 3),
                new AspectRatio("3:4", 3, 4),
                new AspectRatio("16:9", 16, 9),
                new AspectRatio("5:4", 5, 4),
                new AspectRatio("4:5", 4, 5));
        return uCrop.withOptions(options);
    }

    public void startMaterialDialog() {
        materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
        Objects.requireNonNull(materialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        materialDialog.setCancelable(false);
        materialDialog.show();
    }

    public void dismissMaterialDialog() {
        try {
            if (activity instanceof Activity) {
                if (!((Activity) activity).isFinishing())
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
