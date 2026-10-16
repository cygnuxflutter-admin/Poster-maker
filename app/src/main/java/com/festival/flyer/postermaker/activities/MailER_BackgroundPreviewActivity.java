package com.festival.flyer.postermaker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager;
import com.festival.flyer.postermaker.model.MailER_BgImage;
import com.festival.flyer.postermaker.threadTask.MailER_DlBgAsync;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;
import com.festival.flyer.postermaker.utils.MailER_LikeManager;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MailER_BackgroundPreviewActivity extends AppCompatActivity {

    public static ArrayList<MailER_BgImage> tempBgList;

    private ViewPager2 viewPager2;
    private TextView tvTitle, tvSubtitle, tvEditText;
    private ImageView ivEditIcon;
    private ProgressBar pbEditLoading;
    private LinearLayout btnEditBottom;

    private ArrayList<MailER_BgImage> bgList = new ArrayList<>();
    private String catName;
    private String selectedPath;
    private String mode = "";
    private int selectedPosition = 0;

    private MailER_PreferenceClass preferenceClass;
    private MailER_LikeManager likeManager;

    private boolean isRewarded = false;
    private boolean isLoadingBg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_background_preview);

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
        likeManager = new MailER_LikeManager(this);

        catName = getIntent().getStringExtra("cat_name");
        selectedPath = getIntent().getStringExtra("selected_path");
        selectedPosition = getIntent().getIntExtra("selected_position", 0);
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "";

        if (tempBgList != null && !tempBgList.isEmpty()) {
            bgList = new ArrayList<>(tempBgList);
        } else {
            //noinspection unchecked
            ArrayList<MailER_BgImage> fromIntent = (ArrayList<MailER_BgImage>) getIntent().getSerializableExtra("bg_list");
            if (fromIntent != null) {
                bgList = new ArrayList<>(fromIntent);
            }
        }

        // Clean out any null items (e.g. ad slots)
        ArrayList<MailER_BgImage> cleanList = new ArrayList<>();
        for (MailER_BgImage b : bgList) {
            if (b != null) {
                cleanList.add(b);
            }
        }
        bgList = cleanList;

        if (selectedPath != null && !selectedPath.isEmpty()) {
            for (int i = 0; i < bgList.size(); i++) {
                if (selectedPath.equals(bgList.get(i).getImage_url())) {
                    selectedPosition = i;
                    break;
                }
            }
        }

        if (selectedPosition < 0 || selectedPosition >= bgList.size()) {
            selectedPosition = 0;
        }

        initViews();
        setupPreviewNativeAd();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void initViews() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MyApplication.showInterstitialAd(MailER_BackgroundPreviewActivity.this, () -> finish());
            }
        });

        ImageView icBack = findViewById(R.id.ic_back);
        if (icBack != null) {
            icBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        btnEditBottom = findViewById(R.id.btn_edit_bottom);
        ivEditIcon = findViewById(R.id.iv_edit_icon);
        tvEditText = findViewById(R.id.tv_edit_text);
        pbEditLoading = findViewById(R.id.pb_edit_loading);

        if (tvTitle != null) {
            tvTitle.setText(catName != null && !catName.isEmpty() ? catName : "Backgrounds");
        }

        updateSubtitle(selectedPosition);

        viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        BackgroundPreviewAdapter adapter = new BackgroundPreviewAdapter();
        viewPager2.setAdapter(adapter);

        if (selectedPosition < bgList.size()) {
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
            if (bgList.size() <= 1) {
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

        if (btnEditBottom != null) {
            btnEditBottom.setOnClickListener(v -> {
                if (isLoadingBg) return;
                int currentPos = viewPager2.getCurrentItem();
                if (currentPos >= 0 && currentPos < bgList.size()) {
                    onBgSelectedForEdit(bgList.get(currentPos));
                }
            });
        }
    }

    private void updateSubtitle(int position) {
        if (tvSubtitle != null) {
            int total = bgList.size();
            if (total > 0) {
                tvSubtitle.setText("Background " + (position + 1) + " of " + total);
            } else {
                tvSubtitle.setText("Backgrounds");
            }
        }
    }

    private void setupPreviewNativeAd() {
        CardView cardNativeAd = findViewById(R.id.card_native_ad);
        RelativeLayout nativeAdContainer = findViewById(R.id.native_ad_container);
        ShimmerFrameLayout nativeShimmerContainer = findViewById(R.id.native_shimmer_container);

        if (cardNativeAd == null || nativeAdContainer == null) return;

        boolean isNativeEnabled = true;
        String customAdId = "";
        String adType = "compact"; // Default: Compact Native Ad (height 100dp)
        int customHeightDp = 100;

        try {
            FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
            String masterJson = MailER_PreferenceClass.getMasterConfigJson(rc);
            if (masterJson != null && !masterJson.trim().isEmpty()) {
                JSONObject root = new JSONObject(masterJson);
                if (root.has("backgrounds")) {
                    JSONObject bgObj = root.getJSONObject("backgrounds");
                    if (bgObj.has("preview_native_ad")) {
                        JSONObject nObj = bgObj.getJSONObject("preview_native_ad");
                        isNativeEnabled = nObj.optBoolean("enabled", nObj.optBoolean("is_show", true));
                        customAdId = nObj.optString("ad_id", nObj.optString("native_ad_id", ""));
                        adType = nObj.optString("type", nObj.optString("ad_type", adType));
                        int configuredHeight = nObj.optInt("height", -1);
                        if (configuredHeight > 0) {
                            customHeightDp = configuredHeight;
                        }
                    } else if (bgObj.has("preview_ad")) {
                        JSONObject nObj = bgObj.getJSONObject("preview_ad");
                        isNativeEnabled = nObj.optBoolean("enabled", nObj.optBoolean("is_show", true));
                        customAdId = nObj.optString("ad_id", nObj.optString("native_ad_id", ""));
                        adType = nObj.optString("type", nObj.optString("ad_type", adType));
                        int configuredHeight = nObj.optInt("height", -1);
                        if (configuredHeight > 0) {
                            customHeightDp = configuredHeight;
                        }
                    }
                }
            } else {
                if (rc.getKeysByPrefix("").contains("preview_bg_native_enabled")) {
                    isNativeEnabled = rc.getBoolean("preview_bg_native_enabled");
                }
                if (rc.getKeysByPrefix("").contains("preview_bg_native_ad_id")) {
                    customAdId = rc.getString("preview_bg_native_ad_id");
                }
                if (rc.getKeysByPrefix("").contains("preview_bg_native_type")) {
                    adType = rc.getString("preview_bg_native_type");
                }
                if (rc.getKeysByPrefix("").contains("preview_bg_native_height")) {
                    long h = rc.getLong("preview_bg_native_height");
                    if (h > 0) customHeightDp = (int) h;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("BgPreview", "Error reading remote config preview native ad: " + e.getMessage());
        }

        if (preferenceClass != null) {
            isNativeEnabled = preferenceClass.getBoolean("preview_bg_native_enabled", isNativeEnabled);
            String savedId = preferenceClass.getAdsId("preview_bg_native_ad_id");
            if (customAdId.isEmpty() && savedId != null && !savedId.isEmpty()) {
                customAdId = savedId;
            }
            String savedType = preferenceClass.getDataType("preview_bg_native_type", "");
            if (!savedType.isEmpty()) {
                adType = savedType;
            }
            int savedH = preferenceClass.getInt("preview_bg_native_height", -1);
            if (savedH > 0) {
                customHeightDp = savedH;
            }
        }

        if (MailER_NetworkUtils.isNetworkAvailable(this) && isNativeEnabled) {
            cardNativeAd.setVisibility(View.VISIBLE);

            if (customHeightDp > 0) {
                int hPx = dpToPx(customHeightDp);
                // Only set fixed height on shimmer skeleton placeholder;
                // ad container stays wrap_content so ad fills its natural height
                if (nativeShimmerContainer != null) {
                    ViewGroup.LayoutParams slp = nativeShimmerContainer.getLayoutParams();
                    if (slp != null) {
                        slp.height = hPx;
                        nativeShimmerContainer.setLayoutParams(slp);
                    }
                }
            }

            if (nativeShimmerContainer != null) {
                nativeShimmerContainer.setVisibility(View.VISIBLE);
                nativeShimmerContainer.startShimmer();
            }

            if ("compact".equalsIgnoreCase(adType)) {
                MailER_NativeAdUtil.loadCompactNativeAd(nativeAdContainer, this, nativeShimmerContainer, customAdId.isEmpty() ? null : customAdId);
            } else if ("medium".equalsIgnoreCase(adType)) {
                MailER_NativeAdUtil.loadMediumNativeAd(nativeAdContainer, this, nativeShimmerContainer, customAdId.isEmpty() ? null : customAdId);
            } else if ("banner".equalsIgnoreCase(adType)) {
                MailER_LoadAds.loadAdmobBannerAd(this, nativeAdContainer, nativeShimmerContainer, customAdId.isEmpty() ? null : customAdId);
            } else {
                MailER_NativeAdUtil.loadNativeAd(nativeAdContainer, this, nativeShimmerContainer, customAdId.isEmpty() ? null : customAdId);
            }
        } else {
            cardNativeAd.setVisibility(View.GONE);
            if (nativeShimmerContainer != null) {
                nativeShimmerContainer.stopShimmer();
                nativeShimmerContainer.setVisibility(View.GONE);
            }
        }
    }

    public void onBgSelectedForEdit(MailER_BgImage bg) {
        if (isLoadingBg) return;

        if (!MailER_NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bg.isPremium()) {
            boolean isPremiumAdEnabled = preferenceClass.getBoolean("background_premium_ad_enabled", preferenceClass.getBoolean("premium_ad_enabled", true));
            if (!isPremiumAdEnabled) {
                downloadAndOpen(bg.getImage_url());
                return;
            }
            MailER_MaterialDialogUtils.getInstance().rewardDialog(this, "Use Background(One Time)", "Use premium background by watching Ads", materialDialog -> {
                if (materialDialog != null && materialDialog.isShowing()) {
                    materialDialog.dismiss();
                }
                String pAdType = preferenceClass.getDataType("background_premium_ad_type", preferenceClass.getDataType("PremiumAdType", "reward"));
                String customRwId = preferenceClass.getAdsId("background_reward_ad_id");
                if (pAdType != null && pAdType.equalsIgnoreCase("Reward")) {
                    MailER_RewardVideoManager.showRewardVideoAd(MailER_BackgroundPreviewActivity.this, customRwId, new MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                        @Override
                        public void onAdClose() {
                            isRewarded = true;
                            downloadAndOpen(bg.getImage_url());
                        }
                    });
                } else {
                    MyApplication.showInterstitialAdWithOutCount(MailER_BackgroundPreviewActivity.this, () -> {
                        isRewarded = true;
                        downloadAndOpen(bg.getImage_url());
                    });
                }
            }, materialDialog -> {
                if (materialDialog != null && materialDialog.isShowing()) {
                    materialDialog.dismiss();
                }
            });
            return;
        } else {
            downloadAndOpen(bg.getImage_url());
        }
    }

    private void setLoadingState(boolean loading) {
        isLoadingBg = loading;
        if (pbEditLoading != null && ivEditIcon != null && tvEditText != null && btnEditBottom != null) {
            if (loading) {
                pbEditLoading.setVisibility(View.VISIBLE);
                ivEditIcon.setVisibility(View.GONE);
                tvEditText.setText("Opening...");
                btnEditBottom.setEnabled(false);
            } else {
                pbEditLoading.setVisibility(View.GONE);
                ivEditIcon.setVisibility(View.VISIBLE);
                tvEditText.setText("Use Background");
                btnEditBottom.setEnabled(true);
            }
        }
    }

    private void downloadAndOpen(String url) {
        setLoadingState(true);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(url);

        new MailER_DlBgAsync(this, strings, new MailER_DlBgAsync.OnDownloadTemplateListener() {
            @Override
            public void onDownloadComplete() {
                setLoadingState(false);
                String localPath = MailER_FileUtils.getFile(MailER_BackgroundPreviewActivity.this, url);

                if ("user".equals(mode)) {
                    if (isRewarded) {
                        setResultAndFinish(localPath);
                        isRewarded = false;
                    } else {
                        MyApplication.showInterstitialAd(MailER_BackgroundPreviewActivity.this, () -> setResultAndFinish(localPath));
                    }
                } else {
                    startCrop(Uri.fromFile(new File(localPath)));
                }
            }

            @Override
            public void onError() {
                setLoadingState(false);
                Toast.makeText(MailER_BackgroundPreviewActivity.this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void setResultAndFinish(String localPath) {
        Intent intent = new Intent();
        intent.putExtra("local", false);
        intent.putExtra("bg_image", localPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void startCrop(@NonNull Uri uri) {
        String destinationFileName = "SampleCropImage_" + System.currentTimeMillis() + ".png";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(Color.parseColor("#7B2FF7"));
        options.setStatusBarColor(Color.parseColor("#5A18C9"));
        options.setToolbarWidgetColor(Color.WHITE);
        options.setActiveControlsWidgetColor(Color.parseColor("#7B2FF7"));
        options.setRootViewBackgroundColor(Color.WHITE);
        options.setCropFrameColor(Color.WHITE);
        options.setCropGridColor(Color.WHITE);
        options.setAspectRatioOptions(1,
                new AspectRatio("1:1", 1, 1),
                new AspectRatio("3:2", 3, 2),
                new AspectRatio("2:3", 2, 3),
                new AspectRatio("4:3", 4, 3),
                new AspectRatio("3:4", 3, 4),
                new AspectRatio("16:9", 16, 9),
                new AspectRatio("5:4", 5, 4),
                new AspectRatio("4:5", 4, 5));

        uCrop = uCrop.withOptions(options);
        uCrop.start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                String croppedPath = resultUri.toString();

                if ("user".equals(mode)) {
                    setResultAndFinish(croppedPath);
                } else {
                    if (isRewarded) {
                        launchPosterEdit(croppedPath);
                        isRewarded = false;
                    } else {
                        MyApplication.showInterstitialAd(this, () -> launchPosterEdit(croppedPath));
                    }
                }
            }
        }
    }

    private void launchPosterEdit(String bgPath) {
        Intent intent = new Intent(this, MailER_PosterEditActivity.class);
        intent.putExtra("bg_path", bgPath);
        intent.putExtra("loadUserFrame", true);
        intent.putExtra("Temp_Type", "MY_TEMP");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tempBgList = null;
        ShimmerFrameLayout nativeShimmerContainer = findViewById(R.id.native_shimmer_container);
        if (nativeShimmerContainer != null) {
            nativeShimmerContainer.stopShimmer();
        }
    }

    // =========================================================================
    // Dynamic Ratio Calculation Helpers
    // =========================================================================
    private void applyCardDimensions(CardView cardView, float ratio, int parentW, int parentH) {
        if (cardView == null || parentW <= 0 || parentH <= 0) return;
        if (ratio <= 0.05f) ratio = 1.0f;

        // Zero horizontal margin so background spans 100% full width with NO side white space
        int marginH = 0;
        int marginV = cardView.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp);

        int maxW = parentW;
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
    private class BackgroundPreviewAdapter extends RecyclerView.Adapter<BackgroundPreviewAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_item_background_preview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MailER_BgImage item = bgList.get(position);
            if (item == null) return;

            // Default to 1:1 square initially so card starts with full width and proper height
            float initialRatio = 1.0f;

            int pW = viewPager2 != null && viewPager2.getWidth() > 0 ? viewPager2.getWidth() : holder.itemView.getWidth();
            int pH = viewPager2 != null && viewPager2.getHeight() > 0 ? viewPager2.getHeight() : holder.itemView.getHeight();

            if (pW > 0 && pH > 0) {
                applyCardDimensions(holder.cardBg, initialRatio, pW, pH);
            } else {
                holder.itemView.post(() -> {
                    int w = viewPager2 != null && viewPager2.getWidth() > 0 ? viewPager2.getWidth() : holder.itemView.getWidth();
                    int h = viewPager2 != null && viewPager2.getHeight() > 0 ? viewPager2.getHeight() : holder.itemView.getHeight();
                    applyCardDimensions(holder.cardBg, initialRatio, w, h);
                });
            }

            // Initially hide badges while background image is loading
            holder.ivLock.setVisibility(View.GONE);
            holder.ivLike.setVisibility(View.GONE);

            // Load Background Image
            holder.pbLoading.setVisibility(View.VISIBLE);
            String imageUrl = item.getImage_url() != null && !item.getImage_url().isEmpty() ? item.getImage_url() : item.getThumb_url();
            Glide.with(MailER_BackgroundPreviewActivity.this)
                    .load(imageUrl)
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
                            holder.ivLike.setVisibility(View.VISIBLE);
                            holder.ivLock.setVisibility(item.isPremium() ? View.VISIBLE : View.GONE);
                            if (resource != null && resource.getIntrinsicWidth() > 0 && resource.getIntrinsicHeight() > 0) {
                                float realRatio = (float) resource.getIntrinsicWidth() / (float) resource.getIntrinsicHeight();
                                int w = viewPager2 != null && viewPager2.getWidth() > 0 ? viewPager2.getWidth() : holder.itemView.getWidth();
                                int h = viewPager2 != null && viewPager2.getHeight() > 0 ? viewPager2.getHeight() : holder.itemView.getHeight();
                                applyCardDimensions(holder.cardBg, realRatio, w, h);
                            }
                            return false;
                        }
                    })
                    .into(holder.ivBg);

            // Like Heart Button setup
            String bgUrl = item.getImage_url();
            holder.ivLike.setImageResource(likeManager.isLiked(bgUrl) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
            holder.ivLike.setOnClickListener(v -> {
                likeManager.toggleLike(bgUrl, item.isPremium());
                holder.ivLike.setImageResource(likeManager.isLiked(bgUrl) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
            });

            // Edit is only triggered via bottom "Use Background" button
            holder.ivBg.setOnClickListener(null);
            holder.ivBg.setClickable(false);
            holder.cardBg.setOnClickListener(null);
            holder.cardBg.setClickable(false);
        }

        @Override
        public int getItemCount() {
            return bgList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivBg, ivLike;
            final TextView ivLock;
            final ProgressBar pbLoading;
            final CardView cardBg;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivBg = itemView.findViewById(R.id.iv_bg);
                ivLike = itemView.findViewById(R.id.iv_like);
                ivLock = itemView.findViewById(R.id.iv_lock);
                pbLoading = itemView.findViewById(R.id.pb_bg_loading);
                cardBg = itemView.findViewById(R.id.card_bg);
            }
        }
    }
}
