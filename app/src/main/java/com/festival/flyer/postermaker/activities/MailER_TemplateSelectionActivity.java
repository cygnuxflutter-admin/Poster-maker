package com.festival.flyer.postermaker.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import com.festival.flyer.postermaker.utils.MailER_BottomNavHelper;
import android.os.Bundle;
import android.util.Log;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager;
import com.festival.flyer.postermaker.controller.MailER_TemplateSelectionController;
import com.festival.flyer.postermaker.fragment.MailER_TemplateFragment;
import com.festival.flyer.postermaker.model.MailER_StickerModel;
import com.festival.flyer.postermaker.model.MailER_TemplateModel;
import com.festival.flyer.postermaker.model.MailER_TextModel;
import com.festival.flyer.postermaker.threadTask.MailER_DlTemplateAsync;
import com.festival.flyer.postermaker.threadTask.MailER_GetPosDetail;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MailER_TemplateSelectionActivity extends AppCompatActivity implements MailER_TemplateFragment.GetPosterListener {

    private final ArrayList<String> stringArrayList = new ArrayList<>();
    private MailER_TemplateSelectionController templateSelectionController;
    private ArrayList<MailER_TemplateModel> templateModels;
    private ArrayList<MailER_StickerModel> sticker_model;
    private ArrayList<MailER_TextModel> text_model;
    private int cat_id, post_id;
    private boolean local_permission = false;
    private boolean isRewarded = false;
    private boolean isProModeActive = false;
    private MailER_PreferenceClass preferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_template_selection);
        MailER_BottomNavHelper.setupBottomNav(this, R.id.tab_explore);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View statusBarSpacer = findViewById(R.id.status_bar_spacer);
        if (statusBarSpacer != null) {
            statusBarSpacer.setVisibility(View.GONE); // Let fitsSystemWindows handle it
        }

        RelativeLayout btm = this.findViewById(R.id.btm);
        // WindowInsets are handled by fitsSystemWindows on root now

        findByID();

        preferenceClass = new MailER_PreferenceClass(this);
        ShimmerFrameLayout shimmer_view_container = this.findViewById(R.id.shimmer_view_container);
        RelativeLayout rl_ad = this.findViewById(R.id.rl_ad);


        int BannerAdStatus = preferenceClass.getAdsStatus("ReadymadePoste_BannerAD");
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            if (BannerAdStatus == 1) {
                btm.setVisibility(View.VISIBLE);
                MailER_LoadAds.loadAdmobBannerAd(this, rl_ad, shimmer_view_container);
            } else {
                btm.setVisibility(View.GONE);
            }
        }

        findViewById(R.id.ic_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        
        View proToggle = findViewById(R.id.ll_pro_toggle);
        if (proToggle != null) {
            proToggle.setOnClickListener(v -> {
                isProModeActive = !isProModeActive;
                if (isProModeActive) {
                    proToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFD700"))); // Gold color for active
                } else {
                    proToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF8C00"))); // Orange for inactive
                }
                if (templateSelectionController != null) {
                    templateSelectionController.applyProFilter(isProModeActive);
                }
            });
        }
        
        deleteFromExternalStorage();

    }

    public void deleteFromExternalStorage() {
        String fullPath = "/DCIM/BM Infotech/Festival Adbanao/.poster_data/.temp/.framedata/";


        File dir = new File(Environment.getExternalStorageDirectory() + fullPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }


    private void findByID() {
        preferenceClass = new MailER_PreferenceClass(this);
        String selectedCategory = getIntent().getStringExtra("selected_category");
        templateSelectionController = new MailER_TemplateSelectionController(this, getSupportFragmentManager(), preferenceClass, selectedCategory);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
            if (local_permission) {
                loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);
                local_permission = false;
            }
        }
    }



    @Override
    public void onPosterClick(int cat_id, int post_id, boolean premium) {
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PERMISSION_GRANTED || checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            local_permission = true;
            this.cat_id = cat_id;
            this.post_id = post_id;
//                        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                    } else {
//                        MailER_MaterialDialogUtils.getInstance().PermissionDialog(this);
//                    }
//                    return;
//                }
//            }
            if (premium) {
                this.cat_id = cat_id;
                this.post_id = post_id;

                MailER_MaterialDialogUtils.getInstance().rewardDialog(this, "Use Template(One Time)", "Use premium template by watching Ads", materialDialog -> {
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
                    MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(this);
                    if (preferenceClass.getDataType("PremiumAdType") != null && preferenceClass.getDataType("PremiumAdType").equals("Reward")) {
                        MailER_RewardVideoManager.showRewardVideoAd(MailER_TemplateSelectionActivity.this, new MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                            @Override
                            public void onAdClose() {
                                isRewarded = true;
                                templateSelectionController.startMaterialDialog();
                                loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);
                            }
                        });
                    } else {
                        MyApplication.showInterstitialAdWithOutCount(MailER_TemplateSelectionActivity.this, () ->
                        {
                            isRewarded = true;
                            templateSelectionController.startMaterialDialog();
                            loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);

                        });
                    }
                }, materialDialog -> {
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
                });

            } else {
                templateSelectionController.startMaterialDialog();
                loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);
            }
        } else {
            MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_TemplateSelectionActivity.this, "Make sure you are connected to internet!!");
        }
    }

    public void loadPoster(String key, final int cat_id, final int pos_id) {

        String requestUrl = preferenceClass.getDataType("field_1") + preferenceClass.getDataType("field_34") + preferenceClass.getDataType("field_36");
        android.util.Log.d("API_CALL_DEBUG", "REQUEST URL: " + requestUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, response -> {
            android.util.Log.d("API_CALL_DEBUG", "RESPONSE FROM: " + requestUrl + "\nDATA: " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                int error = jsonObject.getInt(preferenceClass.getDataType("field_3"));
                if (error == 0 || error == 404) {
                    templateSelectionController.dismissMaterialDialog();
                    MailER_MaterialDialogUtils.getInstance().errorDialog2(MailER_TemplateSelectionActivity.this, getResources().getString(R.string.something_went_wrong));
                    return;
                }

                if (error == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray(preferenceClass.getDataType("field_4"));
                    if (jsonArray.length() == 0) {
                        MailER_MaterialDialogUtils.getInstance().errorDialog2(MailER_TemplateSelectionActivity.this, getResources().getString(R.string.something_went_wrong));
                    } else {
                        new MailER_GetPosDetail(preferenceClass, jsonArray, new MailER_GetPosDetail.OnGetDataListener() {
                            @Override
                            public void onGetDataComplete(ArrayList<MailER_TemplateModel> posterCos) {
                                MailER_TemplateSelectionActivity.this.templateModels = posterCos;
                                sticker_model = posterCos.get(0).getSticker_model();
                                text_model = posterCos.get(0).getText_model();
                                stringArrayList.clear();
                                stringArrayList.add(posterCos.get(0).getBack_image());

                                for (int i = 0; i < sticker_model.size(); i++) {
                                    if (!sticker_model.get(i).getSt_image().equals("")) {
                                        stringArrayList.add(preferenceClass.getDataType("field_1") + "/" + sticker_model.get(i).getSt_image());
                                    }
                                }

                                if (preferenceClass.getInt("download") == 0) {
                                    for (int i = 0; i < text_model.size(); i++) {
                                        stringArrayList.add(preferenceClass.getDataType("field_37") + text_model.get(i).getFont_family());
                                    }
                                }

                                new MailER_DlTemplateAsync(MailER_TemplateSelectionActivity.this, stringArrayList, new MailER_DlTemplateAsync.OnDownloadTemplateListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        templateSelectionController.dismissMaterialDialog();
                                        if (MailER_NetworkUtils.isNetworkAvailable(MailER_TemplateSelectionActivity.this)) {
                                            if (isRewarded) {
                                                startIntent();
                                                isRewarded = false;
                                                return;
                                            }
                                            MyApplication.showInterstitialAd(MailER_TemplateSelectionActivity.this, MailER_TemplateSelectionActivity.this::startIntent);
                                        } else {
                                            MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_TemplateSelectionActivity.this, "Make sure you are connected to internet!!");
                                        }
                                    }

                                    @Override
                                    public void onError() {
                                        templateSelectionController.dismissMaterialDialog();
                                        MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_TemplateSelectionActivity.this, getResources().getString(R.string.something_went_wrong));
                                    }
                                }).execute();

                            }

                            @Override
                            public void onError() {
                                templateSelectionController.dismissMaterialDialog();
                                MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_TemplateSelectionActivity.this, getResources().getString(R.string.something_went_wrong));
                            }
                        }).execute();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, error -> {
            Log.e("---API_DATA---", "--- ERROR ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Error: " + error.getMessage());
            templateSelectionController.dismissMaterialDialog();
            MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_TemplateSelectionActivity.this, getResources().getString(R.string.something_went_wrong));
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put(preferenceClass.getDataType("field_47"), key);
                postMap.put(preferenceClass.getDataType("field_48"), "1");
                postMap.put(preferenceClass.getDataType("field_46"), String.valueOf(cat_id));
                postMap.put(preferenceClass.getDataType("field_10"), String.valueOf(pos_id));
                Log.e("---API_DATA---", "Params: " + postMap);
                return postMap;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
//        templateSelectionController.dismissMaterialDialog();
    }

    private void startIntent() {
        Intent intent = new Intent(this, MailER_PosterEditActivity.class);
        intent.putParcelableArrayListExtra("template", templateModels);
        intent.putParcelableArrayListExtra("sticker", sticker_model);
        intent.putParcelableArrayListExtra("text", text_model);
        intent.putExtra("loadUserFrame", false);
        intent.putExtra("Temp_Type", "MY_TEMP");
        startActivity(intent);
        templateSelectionController.dismissMaterialDialog();
    }
}