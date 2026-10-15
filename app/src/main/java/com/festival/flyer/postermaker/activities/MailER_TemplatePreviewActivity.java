package com.festival.flyer.postermaker.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MailER_TemplatePreviewActivity extends AppCompatActivity {

    public static ArrayList<MailER_PosterImage> tempPosterList;

    private ViewPager2 viewPager2;
    private TextView tvTitle, tvSubtitle, tvEditText;
    private ImageView ivEditIcon;
    private ProgressBar pbEditLoading;
    private LinearLayout btnEditTop;

    private ArrayList<MailER_PosterImage> posterList = new ArrayList<>();
    private int catId;
    private String catName;
    private int selectedPosition = 0;
    private int selectedPostId = 0;

    private MailER_PreferenceClass preferenceClass;
    private MailER_TemplateLikeManager likeManager;

    private ArrayList<MailER_TemplateModel> templateModels;
    private ArrayList<MailER_StickerModel> sticker_model;
    private ArrayList<MailER_TextModel> text_model;
    private final ArrayList<String> stringArrayList = new ArrayList<>();
    private boolean isRewarded = false;
    private boolean isLoadingPoster = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_template_preview);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        preferenceClass = new MailER_PreferenceClass(this);
        likeManager = new MailER_TemplateLikeManager(this);

        catId = getIntent().getIntExtra("cat_id", 0);
        catName = getIntent().getStringExtra("cat_name");
        selectedPosition = getIntent().getIntExtra("selected_position", 0);
        selectedPostId = getIntent().getIntExtra("selected_post_id", 0);

        if (tempPosterList != null && !tempPosterList.isEmpty()) {
            posterList = new ArrayList<>(tempPosterList);
        } else {
            //noinspection unchecked
            ArrayList<MailER_PosterImage> fromIntent = (ArrayList<MailER_PosterImage>) getIntent().getSerializableExtra("poster_list");
            if (fromIntent != null) {
                posterList = new ArrayList<>(fromIntent);
            }
        }

        // Clean out any null items (e.g. ad slots) if present
        ArrayList<MailER_PosterImage> cleanList = new ArrayList<>();
        for (MailER_PosterImage p : posterList) {
            if (p != null) {
                cleanList.add(p);
            }
        }
        posterList = cleanList;

        // If selectedPosition was based on post_id, ensure we find the right index
        if (selectedPostId != 0) {
            for (int i = 0; i < posterList.size(); i++) {
                if (posterList.get(i).getPost_id() == selectedPostId) {
                    selectedPosition = i;
                    break;
                }
            }
        }

        if (selectedPosition < 0 || selectedPosition >= posterList.size()) {
            selectedPosition = 0;
        }

        initViews();
        setupBannerAd();
        deleteFromExternalStorage();
    }

    private void initViews() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MyApplication.showInterstitialAd(MailER_TemplatePreviewActivity.this, () -> finish());
            }
        });

        ImageView icBack = findViewById(R.id.ic_back);
        if (icBack != null) {
            icBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        btnEditTop = findViewById(R.id.btn_edit_top);
        ivEditIcon = findViewById(R.id.iv_edit_icon);
        tvEditText = findViewById(R.id.tv_edit_text);
        pbEditLoading = findViewById(R.id.pb_edit_loading);

        if (tvTitle != null) {
            tvTitle.setText(catName != null && !catName.isEmpty() ? catName : "Templates");
        }

        updateSubtitle(selectedPosition);

        viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        TemplatePreviewAdapter adapter = new TemplatePreviewAdapter();
        viewPager2.setAdapter(adapter);

        if (selectedPosition < posterList.size()) {
            viewPager2.setCurrentItem(selectedPosition, false);
        }

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSubtitle(position);

                View lytSwipeHint = findViewById(R.id.lyt_swipe_hint);
                if (position > 0 && lytSwipeHint != null && lytSwipeHint.getVisibility() == View.VISIBLE) {
                    lytSwipeHint.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> lytSwipeHint.setVisibility(View.GONE));
                }
            }
        });

        View lytSwipeHint = findViewById(R.id.lyt_swipe_hint);
        if (lytSwipeHint != null) {
            if (posterList.size() <= 1) {
                lytSwipeHint.setVisibility(View.GONE);
            } else {
                ImageView ivSwipeArrow = findViewById(R.id.iv_swipe_arrow);
                if (ivSwipeArrow != null) {
                    android.view.animation.TranslateAnimation bounce = new android.view.animation.TranslateAnimation(0, 0, 0, -10);
                    bounce.setDuration(600);
                    bounce.setRepeatMode(android.view.animation.Animation.REVERSE);
                    bounce.setRepeatCount(android.view.animation.Animation.INFINITE);
                    ivSwipeArrow.startAnimation(bounce);
                }
            }
        }

        if (btnEditTop != null) {
            btnEditTop.setOnClickListener(v -> {
                if (isLoadingPoster) return;
                int currentPos = viewPager2.getCurrentItem();
                if (currentPos >= 0 && currentPos < posterList.size()) {
                    onPosterSelectedForEdit(posterList.get(currentPos));
                }
            });
        }
    }

    private void updateSubtitle(int position) {
        if (tvSubtitle != null) {
            int total = posterList.size();
            if (total > 0) {
                tvSubtitle.setText("Template " + (position + 1) + " of " + total);
            } else {
                tvSubtitle.setText("Templates");
            }
        }
    }

    private void setupBannerAd() {
        RelativeLayout btm = findViewById(R.id.btm);
        ShimmerFrameLayout shimmer_view_container = findViewById(R.id.shimmer_view_container);
        RelativeLayout rl_ad = findViewById(R.id.rl_ad);

        int BannerAdStatus = preferenceClass.getAdsStatus("ReadymadePoste_BannerAD");
        boolean isPreviewBannerEnabled = preferenceClass.getBoolean("preview_poster_banner_enabled", BannerAdStatus == 1);
        String customAdId = preferenceClass.getAdsId("preview_poster_banner_ad_id");
        if (MailER_NetworkUtils.isNetworkAvailable(this) && isPreviewBannerEnabled) {
            if (btm != null) btm.setVisibility(View.VISIBLE);
            MailER_LoadAds.loadAdmobBannerAd(this, rl_ad, shimmer_view_container, customAdId);
        } else {
            if (btm != null) btm.setVisibility(View.GONE);
            if (shimmer_view_container != null) {
                shimmer_view_container.stopShimmer();
                shimmer_view_container.setVisibility(View.GONE);
            }
        }
    }

    public void onPosterSelectedForEdit(MailER_PosterImage poster) {
        if (isLoadingPoster) return;

        if (!MailER_NetworkUtils.isNetworkAvailable(this)) {
            MailER_MaterialDialogUtils.getInstance().errorDialog(this, "Make sure you are connected to internet!!");
            return;
        }

        int targetCatId = poster.getCat_id() != 0 ? poster.getCat_id() : catId;
        int targetPostId = poster.getPost_id();

        if (poster.getPremium()) {
            boolean isPremiumAdEnabled = preferenceClass.getBoolean("premium_ad_enabled", true);
            if (!isPremiumAdEnabled) {
                loadPoster(preferenceClass.getDataType("field_0"), targetCatId, targetPostId);
                return;
            }
            MailER_MaterialDialogUtils.getInstance().rewardDialog(this, "Use Template(One Time)", "Use premium template by watching Ads", materialDialog -> {
                if (materialDialog != null && materialDialog.isShowing()) {
                    materialDialog.dismiss();
                }
                String pAdType = preferenceClass.getDataType("PremiumAdType", "reward");
                if (pAdType != null && pAdType.equalsIgnoreCase("Reward")) {
                    MailER_RewardVideoManager.showRewardVideoAd(MailER_TemplatePreviewActivity.this, new MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                        @Override
                        public void onAdClose() {
                            isRewarded = true;
                            loadPoster(preferenceClass.getDataType("field_0"), targetCatId, targetPostId);
                        }
                    });
                } else {
                    MyApplication.showInterstitialAdWithOutCount(MailER_TemplatePreviewActivity.this, () -> {
                        isRewarded = true;
                        loadPoster(preferenceClass.getDataType("field_0"), targetCatId, targetPostId);
                    });
                }
            }, materialDialog -> {
                if (materialDialog != null && materialDialog.isShowing()) {
                    materialDialog.dismiss();
                }
            });
        } else {
            loadPoster(preferenceClass.getDataType("field_0"), targetCatId, targetPostId);
        }
    }

    private void setLoadingState(boolean loading) {
        isLoadingPoster = loading;
        if (pbEditLoading != null && ivEditIcon != null && tvEditText != null && btnEditTop != null) {
            if (loading) {
                pbEditLoading.setVisibility(View.VISIBLE);
                ivEditIcon.setVisibility(View.GONE);
                tvEditText.setText("Opening...");
                btnEditTop.setEnabled(false);
            } else {
                pbEditLoading.setVisibility(View.GONE);
                ivEditIcon.setVisibility(View.VISIBLE);
                tvEditText.setText("Edit");
                btnEditTop.setEnabled(true);
            }
        }
    }

    public void loadPoster(String key, final int targetCatId, final int targetPosId) {
        setLoadingState(true);

        String requestUrl = preferenceClass.getDataType("field_51") + preferenceClass.getDataType("field_34") + preferenceClass.getDataType("field_36");
        Log.d("API_CALL_DEBUG", "REQUEST URL: " + requestUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, response -> {
            Log.d("API_CALL_DEBUG", "RESPONSE FROM: " + requestUrl + "\nDATA: " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                int error = jsonObject.getInt(preferenceClass.getDataType("field_3"));
                if (error == 0 || error == 404) {
                    setLoadingState(false);
                    MailER_MaterialDialogUtils.getInstance().errorDialog2(MailER_TemplatePreviewActivity.this, getResources().getString(R.string.something_went_wrong));
                    return;
                }

                if (error == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray(preferenceClass.getDataType("field_4"));
                    if (jsonArray.length() == 0) {
                        setLoadingState(false);
                        MailER_MaterialDialogUtils.getInstance().errorDialog2(MailER_TemplatePreviewActivity.this, getResources().getString(R.string.something_went_wrong));
                    } else {
                        new MailER_GetPosDetail(preferenceClass, jsonArray, new MailER_GetPosDetail.OnGetDataListener() {
                            @Override
                            public void onGetDataComplete(ArrayList<MailER_TemplateModel> posterCos) {
                                MailER_TemplatePreviewActivity.this.templateModels = posterCos;
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

                                new MailER_DlTemplateAsync(MailER_TemplatePreviewActivity.this, stringArrayList, new MailER_DlTemplateAsync.OnDownloadTemplateListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        setLoadingState(false);
                                        if (MailER_NetworkUtils.isNetworkAvailable(MailER_TemplatePreviewActivity.this)) {
                                            if (isRewarded) {
                                                startIntent();
                                                isRewarded = false;
                                                return;
                                            }
                                            MyApplication.showInterstitialAd(MailER_TemplatePreviewActivity.this, MailER_TemplatePreviewActivity.this::startIntent);
                                        } else {
                                            MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_TemplatePreviewActivity.this, "Make sure you are connected to internet!!");
                                        }
                                    }

                                    @Override
                                    public void onError() {
                                        setLoadingState(false);
                                        MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_TemplatePreviewActivity.this, getResources().getString(R.string.something_went_wrong));
                                    }
                                }).execute();
                            }

                            @Override
                            public void onError() {
                                setLoadingState(false);
                                MailER_MaterialDialogUtils.getInstance().errorDialog3(MailER_TemplatePreviewActivity.this, getResources().getString(R.string.something_went_wrong));
                            }
                        }).execute();
                    }
                }

            } catch (Exception e) {
                setLoadingState(false);
                e.printStackTrace();
            }

        }, error -> {
            setLoadingState(false);
            Log.e("---API_DATA---", "--- ERROR ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Error: " + error.getMessage());
            MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_TemplatePreviewActivity.this, getResources().getString(R.string.something_went_wrong));
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put(preferenceClass.getDataType("field_47"), key);
                postMap.put(preferenceClass.getDataType("field_48"), "1");
                postMap.put(preferenceClass.getDataType("field_46"), String.valueOf(targetCatId));
                postMap.put(preferenceClass.getDataType("field_10"), String.valueOf(targetPosId));
                Log.e("---API_DATA---", "Params: " + postMap);
                return postMap;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
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

    public void deleteFromExternalStorage() {
        String fullPath = "/DCIM/BM Infotech/Festival Adbanao/.poster_data/.temp/.framedata/";
        File dir = new File(Environment.getExternalStorageDirectory() + fullPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    new File(dir, child).delete();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tempPosterList = null;
    }

    // =========================================================================
    // Dynamic Ratio Calculation Helpers
    // =========================================================================
    private float parseRatio(String ratioStr) {
        if (ratioStr != null && !ratioStr.trim().isEmpty()) {
            try {
                String clean = ratioStr.trim();
                if (clean.contains(":")) {
                    String[] parts = clean.split(":");
                    if (parts.length == 2) {
                        float w = Float.parseFloat(parts[0].trim());
                        float h = Float.parseFloat(parts[1].trim());
                        if (w > 0 && h > 0) return w / h;
                    }
                } else if (clean.contains("/")) {
                    String[] parts = clean.split("/");
                    if (parts.length == 2) {
                        float w = Float.parseFloat(parts[0].trim());
                        float h = Float.parseFloat(parts[1].trim());
                        if (w > 0 && h > 0) return w / h;
                    }
                } else {
                    float val = Float.parseFloat(clean);
                    if (val > 0) return val;
                }
            } catch (Exception ignored) {
            }
        }
        return 1.0f; // Default 1:1 square
    }

    private void applyCardDimensions(CardView cardView, float ratio, int parentW, int parentH) {
        if (cardView == null || parentW <= 0 || parentH <= 0) return;
        if (ratio <= 0.05f) ratio = 1.0f;

        int marginH = cardView.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._28sdp);
        int marginV = cardView.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._32sdp);

        int maxW = Math.max(100, parentW - marginH);
        int maxH = Math.max(100, parentH - marginV);

        int finalW;
        int finalH;

        float containerRatio = (float) maxW / (float) maxH;
        if (containerRatio > ratio) {
            // Container is wider than target ratio -> height is limiting factor
            finalH = maxH;
            finalW = Math.round(maxH * ratio);
            if (finalW > maxW) {
                finalW = maxW;
                finalH = Math.round(maxW / ratio);
            }
        } else {
            // Container is taller than target ratio -> width is limiting factor
            finalW = maxW;
            finalH = Math.round(maxW / ratio);
            if (finalH > maxH) {
                finalH = maxH;
                finalW = Math.round(maxH * ratio);
            }
        }

        ViewGroup.LayoutParams lp = cardView.getLayoutParams();
        if (lp != null) {
            if (lp.width != finalW || lp.height != finalH) {
                lp.width = finalW;
                lp.height = finalH;
                cardView.setLayoutParams(lp);
            }
        }
    }

    // =========================================================================
    // ViewPager2 Adapter
    // =========================================================================
    private class TemplatePreviewAdapter extends RecyclerView.Adapter<TemplatePreviewAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_item_template_preview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MailER_PosterImage item = posterList.get(position);
            if (item == null) return;

            float initialRatio = parseRatio(item.getRatio());

            // Immediately apply dynamic aspect ratio if ViewPager2 size is known
            int pW = viewPager2 != null && viewPager2.getWidth() > 0 ? viewPager2.getWidth() : holder.itemView.getWidth();
            int pH = viewPager2 != null && viewPager2.getHeight() > 0 ? viewPager2.getHeight() : holder.itemView.getHeight();

            if (pW > 0 && pH > 0) {
                applyCardDimensions(holder.cardPoster, initialRatio, pW, pH);
            } else {
                holder.itemView.post(() -> {
                    int w = viewPager2 != null && viewPager2.getWidth() > 0 ? viewPager2.getWidth() : holder.itemView.getWidth();
                    int h = viewPager2 != null && viewPager2.getHeight() > 0 ? viewPager2.getHeight() : holder.itemView.getHeight();
                    applyCardDimensions(holder.cardPoster, initialRatio, w, h);
                });
            }

            // Load Poster Image
            holder.pbLoading.setVisibility(View.VISIBLE);
            Glide.with(MailER_TemplatePreviewActivity.this)
                    .load(item.getPost_thumb())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.pbLoading.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.pbLoading.setVisibility(View.GONE);
                            if (resource != null && resource.getIntrinsicWidth() > 0 && resource.getIntrinsicHeight() > 0) {
                                float realRatio = (float) resource.getIntrinsicWidth() / (float) resource.getIntrinsicHeight();
                                int w = viewPager2 != null && viewPager2.getWidth() > 0 ? viewPager2.getWidth() : holder.itemView.getWidth();
                                int h = viewPager2 != null && viewPager2.getHeight() > 0 ? viewPager2.getHeight() : holder.itemView.getHeight();
                                applyCardDimensions(holder.cardPoster, realRatio, w, h);
                            }
                            return false;
                        }
                    })
                    .into(holder.ivPoster);

            // PRO Badge
            holder.ivLock.setVisibility(item.getPremium() ? View.VISIBLE : View.GONE);

            // Like Heart Button
            holder.ivLike.setImageResource(likeManager.isLiked(item.getPost_id()) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
            holder.ivLike.setOnClickListener(v -> {
                int targetCat = item.getCat_id() != 0 ? item.getCat_id() : catId;
                item.setCat_id(targetCat);
                likeManager.toggleLike(item);
                holder.ivLike.setImageResource(likeManager.isLiked(item.getPost_id()) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
            });

            // Poster Click to Edit (both image and card container)
            holder.ivPoster.setOnClickListener(v -> onPosterSelectedForEdit(item));
            holder.cardPoster.setOnClickListener(v -> onPosterSelectedForEdit(item));
        }

        @Override
        public int getItemCount() {
            return posterList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivPoster, ivLike;
            final TextView ivLock;
            final ProgressBar pbLoading;
            final CardView cardPoster;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPoster = itemView.findViewById(R.id.iv_poster);
                ivLike = itemView.findViewById(R.id.iv_like);
                ivLock = itemView.findViewById(R.id.iv_lock);
                pbLoading = itemView.findViewById(R.id.pb_poster_loading);
                cardPoster = itemView.findViewById(R.id.card_poster);
            }
        }
    }
}
