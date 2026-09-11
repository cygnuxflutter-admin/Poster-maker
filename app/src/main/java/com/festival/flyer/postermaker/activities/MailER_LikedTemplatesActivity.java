package com.festival.flyer.postermaker.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_PosterGroupChildAdapter;
import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.festival.flyer.postermaker.model.MailER_StickerModel;
import com.festival.flyer.postermaker.model.MailER_TemplateModel;
import com.festival.flyer.postermaker.model.MailER_TextModel;
import com.festival.flyer.postermaker.threadTask.MailER_DlTemplateAsync;
import com.festival.flyer.postermaker.threadTask.MailER_GetPosDetail;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.utils.MailER_TemplateLikeManager;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailER_LikedTemplatesActivity extends AppCompatActivity {

    private RecyclerView rvLiked;
    private TextView tvNoData;
    private MailER_PosterGroupChildAdapter adapter;
    private ArrayList<MailER_PosterImage> likedTemplates = new ArrayList<>();
    private MailER_TemplateLikeManager likeManager;
    private MailER_PreferenceClass preferenceClass;
    private com.afollestad.materialdialogs.MaterialDialog progressDialog;
    
    private ArrayList<MailER_TemplateModel> templateModels = new ArrayList<>();
    private ArrayList<MailER_StickerModel> sticker_model = new ArrayList<>();
    private ArrayList<MailER_TextModel> text_model = new ArrayList<>();
    private ArrayList<String> stringArrayList = new ArrayList<>();
    private boolean isRewarded = false;
    private AdView adView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_liked_templates);

        // Preload reward ad for premium template unlock


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.ic_back).setOnClickListener(v -> onBackPressed());

        rvLiked = findViewById(R.id.rv_liked);
        tvNoData = findViewById(R.id.tv_no_data);
        likeManager = new MailER_TemplateLikeManager(this);
        preferenceClass = new MailER_PreferenceClass(this);
        
        progressDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(this);
        progressDialog.setCancelable(false);

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedTemplates();
    }

    private void setupRecyclerView() {
        rvLiked.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvLiked.setLayoutManager(layoutManager);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = displayMetrics.widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 2;
        int cellHeight = (800 * cellWidth) / 507;

        adapter = new MailER_PosterGroupChildAdapter(this, likedTemplates, 0, cellWidth, cellHeight, (cat_id, post_id, premium) -> {
            if (cat_id == 0) {
                Toast.makeText(this, "This template is from an older version. Please unlike and like it again from the main categories.", Toast.LENGTH_LONG).show();
                return;
            }
            if (MailER_NetworkUtils.isNetworkAvailable(this)) {
                if (premium) {
                    MailER_MaterialDialogUtils.getInstance().rewardDialog(this, "Use Template(One Time)", "Use premium Template by watching Ads", materialDialog -> {
                        if (preferenceClass.getDataType("PremiumAdType") != null && preferenceClass.getDataType("PremiumAdType").equals("Reward")) {
                            com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager.showRewardVideoAd(MailER_LikedTemplatesActivity.this, new com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                                @Override
                                public void onAdClose() {
                                    isRewarded = true;
                                    progressDialog.show();
                                    loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);
                                }
                            });
                        } else {
                            MyApplication.showInterstitialAdWithOutCount(MailER_LikedTemplatesActivity.this, () -> {
                                isRewarded = true;
                                progressDialog.show();
                                loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);
                            });
                        }
                    }, materialDialog -> {
                        if (materialDialog != null && materialDialog.isShowing())
                            materialDialog.dismiss();
                    });
                } else {
                    progressDialog.show();
                    loadPoster(preferenceClass.getDataType("field_0"), cat_id, post_id);
                }
            } else {
                Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
            }
        });
        rvLiked.setAdapter(adapter);
    }

    private void loadLikedTemplates() {
        likedTemplates.clear();
        List<MailER_PosterImage> list = likeManager.getLikedTemplates();
        if (list != null) {
            likedTemplates.addAll(list);
        }

        if (likedTemplates.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvLiked.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvLiked.setVisibility(View.VISIBLE);
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadPoster(String key, final int cat_id, final int pos_id) {
        String requestUrl = preferenceClass.getDataType("field_51") + preferenceClass.getDataType("field_34") + preferenceClass.getDataType("field_36");
        android.util.Log.d("API_CALL_DEBUG", "REQUEST URL: " + requestUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, response -> {
            android.util.Log.d("API_CALL_DEBUG", "RESPONSE FROM: " + requestUrl + "\nDATA: " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                int error = jsonObject.getInt(preferenceClass.getDataType("field_3"));
                if (error == 0 || error == 404) {
                    progressDialog.dismiss();
                    MailER_MaterialDialogUtils.getInstance().errorDialog2(MailER_LikedTemplatesActivity.this, getResources().getString(R.string.something_went_wrong));
                    return;
                }

                if (error == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray(preferenceClass.getDataType("field_4"));
                    if (jsonArray.length() == 0) {
                        progressDialog.dismiss();
                        MailER_MaterialDialogUtils.getInstance().errorDialog2(MailER_LikedTemplatesActivity.this, getResources().getString(R.string.something_went_wrong));
                    } else {
                        new MailER_GetPosDetail(preferenceClass, jsonArray, new MailER_GetPosDetail.OnGetDataListener() {
                            @Override
                            public void onGetDataComplete(ArrayList<MailER_TemplateModel> posterCos) {
                                MailER_LikedTemplatesActivity.this.templateModels = posterCos;
                                sticker_model = posterCos.get(0).getSticker_model();
                                text_model = posterCos.get(0).getText_model();
                                stringArrayList.clear();
                                stringArrayList.add(posterCos.get(0).getBack_image());

                                for (int i = 0; i < sticker_model.size(); i++) {
                                    if (!sticker_model.get(i).getSt_image().equals("")) {
                                        stringArrayList.add(preferenceClass.getDataType("field_51") + "/" + sticker_model.get(i).getSt_image());
                                    }
                                }

                                if (preferenceClass.getInt("download") == 0) {
                                    for (int i = 0; i < text_model.size(); i++) {
                                        stringArrayList.add(preferenceClass.getDataType("field_52") + text_model.get(i).getFont_family());
                                    }
                                }

                                new MailER_DlTemplateAsync(MailER_LikedTemplatesActivity.this, stringArrayList, new MailER_DlTemplateAsync.OnDownloadTemplateListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        progressDialog.dismiss();
                                        if (MailER_NetworkUtils.isNetworkAvailable(MailER_LikedTemplatesActivity.this)) {
                                            if (isRewarded) {
                                                startIntent();
                                                isRewarded = false;
                                                return;
                                            }
                                            MyApplication.showInterstitialAd(MailER_LikedTemplatesActivity.this, MailER_LikedTemplatesActivity.this::startIntent);
                                        } else {
                                            Toast.makeText(MailER_LikedTemplatesActivity.this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onError() {
                                        progressDialog.dismiss();
                                        MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_LikedTemplatesActivity.this, getResources().getString(R.string.something_went_wrong));
                                    }
                                }).execute();
                            }

                            @Override
                            public void onError() {
                                progressDialog.dismiss();
                                MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_LikedTemplatesActivity.this, getResources().getString(R.string.something_went_wrong));
                            }
                        }).execute();
                    }
                }

            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }, error -> {
            progressDialog.dismiss();
            MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_LikedTemplatesActivity.this, getResources().getString(R.string.something_went_wrong));
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put(preferenceClass.getDataType("field_47"), key);
                postMap.put(preferenceClass.getDataType("field_48"), "1");
                postMap.put(preferenceClass.getDataType("field_46"), String.valueOf(cat_id));
                postMap.put(preferenceClass.getDataType("field_10"), String.valueOf(pos_id));
                return postMap;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(stringRequest);
    }

    private void startIntent() {
        Intent intent = new Intent(this, MailER_PosterEditActivity.class);
        intent.putParcelableArrayListExtra("template", templateModels);
        intent.putParcelableArrayListExtra("sticker", sticker_model);
        intent.putParcelableArrayListExtra("text", text_model);
        intent.putExtra("loadUserFrame", false);
        intent.putExtra("Temp_Type", "MY_TEMP");
        startActivity(intent);
    }
}
