package com.festival.flyer.postermaker.makeityours.auto;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;

import org.json.JSONArray;
import org.json.JSONObject;

import com.festival.flyer.postermaker.utils.MailER_AutoPosterLikeManager;
import com.festival.flyer.postermaker.model.MailER_PosterImage;

import android.content.Intent;
import android.app.Activity;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;


public class MailER_AutoPosterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_POSTER = 0;
    public static final int VIEW_TYPE_NATIVE_AD = 1;

    private Context context;
    private java.util.List<JSONObject> displayList;
    private JSONArray fullPostersList;

    // User's Local Data
    private String userLogo, userName, userPhone, userEmail, userAddress;

    private String fullRawData;
    private MailER_AutoPosterLikeManager likeManager;
    private com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil nativeAdUtil;
    private String customNativeAdId = "";

    public MailER_AutoPosterAdapter(Context context, JSONArray uniquePostersList, String fullRawData) {
        this.context = context;
        this.fullRawData = fullRawData;
        
        try {
            this.fullPostersList = new JSONArray(fullRawData);
        } catch(Exception e) {}
        
        loadUserData();
        likeManager = new MailER_AutoPosterLikeManager(context);
        nativeAdUtil = new com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil(context);

        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(context);
        boolean isNativeEnabled = true;
        int firstAdCount = 4;
        int repeatAdCount = 6;

        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            if (rc != null && rc.getInfo().getLastFetchStatus() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS) {
                isNativeEnabled = rc.getBoolean("custom_category_native_enabled");
                customNativeAdId = rc.getString("custom_category_native_ad_id");
                if (rc.getLong("custom_category_first_rv_count") > 0) {
                    firstAdCount = (int) rc.getLong("custom_category_first_rv_count");
                }
                if (rc.getLong("custom_category_rv_count") > 0) {
                    repeatAdCount = (int) rc.getLong("custom_category_rv_count");
                }
            } else {
                isNativeEnabled = preferenceClass.getBoolean("custom_category_native_enabled", true);
                customNativeAdId = preferenceClass.getAdsId("custom_category_native_ad_id");
                firstAdCount = preferenceClass.getInt("custom_category_first_rv_count", preferenceClass.getInt("First_rv_count", 4));
                repeatAdCount = preferenceClass.getInt("custom_category_rv_count", preferenceClass.getInt("rv_count", 6));
            }
        } catch (Exception e) {
            isNativeEnabled = preferenceClass.getBoolean("custom_category_native_enabled", true);
            customNativeAdId = preferenceClass.getAdsId("custom_category_native_ad_id");
            firstAdCount = preferenceClass.getInt("custom_category_first_rv_count", preferenceClass.getInt("First_rv_count", 4));
            repeatAdCount = preferenceClass.getInt("custom_category_rv_count", preferenceClass.getInt("rv_count", 6));
        }

        this.displayList = new java.util.ArrayList<>();
        int itemsSinceLastAd = 0;
        boolean isFirstAdShown = false;

        if (uniquePostersList != null) {
            for (int i = 0; i < uniquePostersList.length(); i++) {
                JSONObject posterObj = uniquePostersList.optJSONObject(i);
                if (posterObj == null) continue;

                if (isNativeEnabled && firstAdCount > 0 && repeatAdCount > 0 && i != 0) {
                    itemsSinceLastAd++;
                    if (!isFirstAdShown) {
                        if (itemsSinceLastAd >= firstAdCount) {
                            JSONObject adMarker = new JSONObject();
                            try { adMarker.put("is_native_ad", true); } catch (Exception ignored) {}
                            displayList.add(adMarker);
                            isFirstAdShown = true;
                            itemsSinceLastAd = 0;
                        }
                    } else {
                        if (itemsSinceLastAd >= repeatAdCount) {
                            JSONObject adMarker = new JSONObject();
                            try { adMarker.put("is_native_ad", true); } catch (Exception ignored) {}
                            displayList.add(adMarker);
                            itemsSinceLastAd = 0;
                        }
                    }
                }
                displayList.add(posterObj);
            }
        }
    }

    public boolean isNativeAdPosition(int position) {
        return getItemViewType(position) == VIEW_TYPE_NATIVE_AD;
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList != null && position >= 0 && position < displayList.size()) {
            JSONObject obj = displayList.get(position);
            if (obj != null && obj.optBoolean("is_native_ad", false)) {
                return VIEW_TYPE_NATIVE_AD;
            }
        }
        return VIEW_TYPE_POSTER;
    }

    private void loadUserData() {
        SharedPreferences prefs = context.getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE);
        userLogo = prefs.getString("logo_uri", "");
        userName = prefs.getString("business_name", "My Business");
        userPhone = prefs.getString("phone", "9876543210");
        userEmail = prefs.getString("email", "contact@domain.com");
        userAddress = prefs.getString("address", "123, Main Street, City");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NATIVE_AD) {
            View view = LayoutInflater.from(context).inflate(R.layout.spawner_poster_rv, parent, false);
            return new NativeAdViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.spawner_item_auto_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rawHolder, int position) {
        if (rawHolder instanceof NativeAdViewHolder) {
            NativeAdViewHolder adHolder = (NativeAdViewHolder) rawHolder;
            int adHeight = (int) (280 * context.getResources().getDisplayMetrics().density);
            ViewGroup.LayoutParams lp = adHolder.itemView.getLayoutParams();
            if (lp != null) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = adHeight;
                adHolder.itemView.setLayoutParams(lp);
            }
            if (adHolder.content_layout != null) adHolder.content_layout.setVisibility(View.GONE);
            if (adHolder.ad_layout != null) adHolder.ad_layout.setVisibility(View.VISIBLE);
            if (adHolder.native_banner_ad_container != null) {
                adHolder.native_banner_ad_container.setVisibility(View.VISIBLE);
                if (nativeAdUtil != null) {
                    nativeAdUtil.fillAdmobNativeAd(adHolder.native_banner_ad_container, position, customNativeAdId);
                }
            }
            return;
        }

        ViewHolder holder = (ViewHolder) rawHolder;
        try {
            JSONObject posterObj = displayList.get(position);
            
            String thumbUrl = posterObj.optString("thumbnail_image", posterObj.optString("poster_image", posterObj.optString("image_url", "")));
            String ratio = posterObj.optString("ratio", "1:1"); // e.g., "1:1" or "9:16"
            
            MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(context);
            int premiumCount = preferenceClass.getInt("PremiumPostCount", 3);
            boolean premiumEnabled = preferenceClass.getBoolean("premium_enabled", true);
            boolean isPremium = premiumEnabled && (premiumCount > 0) && (position != 0) && (position % premiumCount == 0);
            
            String idStr = posterObj.optString("id", "0");
            MailER_PosterImage posterImage = new MailER_PosterImage(idStr, thumbUrl, ratio, isPremium);
            posterImage.setCat_id(Integer.parseInt(posterObj.optString("category_id", "0")));
            try { posterObj.put("is_premium_local", isPremium); } catch (Exception e) {}
            
            if (holder.ivLike != null) {
                holder.ivLike.setImageResource(likeManager.isLiked(idStr) ? com.festival.flyer.postermaker.R.drawable.spawner_ic_heart_filled : com.festival.flyer.postermaker.R.drawable.spawner_ic_heart_outline);
                holder.ivLike.setOnClickListener(v -> {
                    likeManager.toggleLike(posterObj.toString());
                    holder.ivLike.setImageResource(likeManager.isLiked(idStr) ? com.festival.flyer.postermaker.R.drawable.spawner_ic_heart_filled : com.festival.flyer.postermaker.R.drawable.spawner_ic_heart_outline);
                });
            }

            // 1. Set the correct Aspect Ratio dynamically
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.posterCard);
            constraintSet.setDimensionRatio(R.id.ratio_container, ratio);
            constraintSet.applyTo(holder.posterCard);

            // 2. Load Thumbnail Background
            
            // Load Thumbnail Background and get Native Size for scaling
            Glide.with(context).asBitmap().load(thumbUrl).into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                @Override
                public void onResourceReady(@NonNull android.graphics.Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                    holder.ivThumbnail.setImageBitmap(resource);
                    float nativeWidth = resource.getWidth();
                    
                    holder.overlayContainer.post(() -> {
                        int actualWidth = holder.overlayContainer.getWidth();
                        if (actualWidth == 0) return;
                        
                        try {
                            JSONObject personalization = posterObj.getJSONObject("personalization");
                            float baseWidth = personalization.optInt("canvas_width", (int) nativeWidth);
                            if (baseWidth <= 0) baseWidth = nativeWidth;
                            float scale = actualWidth / baseWidth;

                            JSONObject slots = personalization.getJSONObject("slots");
                            String theme = personalization.optString("theme", "dark");
                            int defaultColor = theme.equals("dark") ? Color.WHITE : Color.BLACK;

                            // Magic Color Extraction (Palette API)
                            try {
                                androidx.palette.graphics.Palette palette = androidx.palette.graphics.Palette.from(resource).generate();
                                androidx.palette.graphics.Palette.Swatch swatch = palette.getVibrantSwatch();
                                if (swatch == null) swatch = palette.getLightVibrantSwatch();
                                if (swatch == null) swatch = palette.getDarkVibrantSwatch();
                                
                                if (swatch != null) {
                                    defaultColor = swatch.getRgb(); // Override basic B/W with magical theme color!
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Clear old dynamic icons from previous bind
                            for (int i = holder.overlayContainer.getChildCount() - 1; i >= 0; i--) {
                                View child = holder.overlayContainer.getChildAt(i);
                                if ("dynamic_icon".equals(child.getTag())) {
                                    holder.overlayContainer.removeViewAt(i);
                                }
                            }

                            if (slots.has("logo")) {
                                applySlot(holder.slotLogo, slots.getJSONObject("logo"), scale);
                                if (!userLogo.isEmpty()) {
                                    Glide.with(context).load(userLogo).into(holder.slotLogo);
                                }
                            } else {
                                holder.slotLogo.setVisibility(View.GONE);
                            }

                            applyTextSlot(holder.slotName, slots.optJSONObject("business_name"), userName, scale, defaultColor, true, holder.overlayContainer, "business_name");
                            applyTextSlot(holder.slotPhone, slots.optJSONObject("phone"), userPhone, scale, defaultColor, false, holder.overlayContainer, "phone");
                            applyTextSlot(holder.slotEmail, slots.optJSONObject("email"), userEmail, scale, defaultColor, false, holder.overlayContainer, "email");
                            applyTextSlot(holder.slotAddress, slots.optJSONObject("address"), userAddress, scale, defaultColor, false, holder.overlayContainer, "address");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                @Override
                public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
            });


            
            
            View ivLock = holder.itemView.findViewById(R.id.iv_lock);
            if (ivLock != null) {
                ivLock.setVisibility(isPremium ? View.VISIBLE : View.GONE);
            }

            holder.posterCard.setOnClickListener(v -> {
                try {
                    String designId = posterObj.optString("design_id", posterObj.optString("id", ""));
                    
                    Runnable openEditor = () -> {
                        Intent intent = new Intent(context, MailER_AutoPosterEditActivity.class);
                        intent.putExtra("design_id", designId);
                        intent.putExtra("full_raw_data", fullRawData);
                        context.startActivity(intent);
                    };

                    if (isPremium && context instanceof Activity) {
                        Activity activity = (Activity) context;
                        boolean isPremiumAdEnabled = preferenceClass.getBoolean("premium_ad_enabled", true);
                        if (!isPremiumAdEnabled) {
                            openEditor.run();
                            return;
                        }
                        MailER_MaterialDialogUtils.getInstance().rewardDialog(activity, "Use Template(One Time)", "Use premium template by watching Ads", materialDialog -> {
                            if (materialDialog != null && materialDialog.isShowing()) {
                                materialDialog.dismiss();
                            }
                            String pAdType = preferenceClass.getDataType("PremiumAdType", "reward");
                            if (pAdType != null && pAdType.equalsIgnoreCase("Reward")) {
                                MailER_RewardVideoManager.showRewardVideoAd(activity, new MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                                    @Override
                                    public void onAdClose() {
                                        openEditor.run();
                                    }
                                });
                            } else {
                                MyApplication.showInterstitialAdWithOutCount(activity, () -> {
                                    openEditor.run();
                                });
                            }
                        }, materialDialog -> {
                            if (materialDialog != null && materialDialog.isShowing()) {
                                materialDialog.dismiss();
                            }
                        });
                    } else {
                        openEditor.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applySlot(View view, JSONObject slotData, float scale) throws Exception {
        view.setVisibility(View.VISIBLE);
        int x = (int) (slotData.getInt("x") * scale);
        int y = (int) (slotData.getInt("y") * scale);
        int w = (int) (slotData.getInt("w") * scale);
        int h = (int) (slotData.getInt("h") * scale);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = w;
        params.height = h;
        params.leftMargin = x;
        params.topMargin = y;
        view.setLayoutParams(params);
    }

    private void applyTextSlot(TextView textView, JSONObject slotData, String textValue, float scale, int defaultColor, boolean isBold, FrameLayout overlayContainer, String slotKey) throws Exception {
        if (slotData == null || textValue == null || textValue.isEmpty()) {
            textView.setVisibility(View.GONE);
            return;
        }
        
        applySlot(textView, slotData, scale);
        textView.setText(textValue);

        // Check if API sends specific color, otherwise use theme color
        String colorHex = slotData.optString("color", "");
        if (!colorHex.isEmpty()) {
            try {
                textView.setTextColor(Color.parseColor(colorHex));
            } catch (Exception e) { textView.setTextColor(defaultColor); }
        } else {
            textView.setTextColor(defaultColor);
        }
        
        // Custom Font Size Limit Support (Keep AutoSize, but cap it at the backend's font size)
        int maxFontSize = 100;
        if (slotData.has("size")) {
            maxFontSize = slotData.optInt("size", 100);
        } else if (slotData.has("fontSize")) {
            maxFontSize = slotData.optInt("fontSize", 100);
        }
        if (maxFontSize <= 0) maxFontSize = 100;
        
        if (maxFontSize < 35) maxFontSize = (int)(maxFontSize * 2.5f);
        int scaledMaxFontSize = (int) (maxFontSize * scale);
        if (scaledMaxFontSize < 6) scaledMaxFontSize = 6;
        int minSize = (int)(6 * scale);
        if (minSize < 2) minSize = 2;
        
        androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textView, 
            minSize, 
            scaledMaxFontSize, 
            1, 
            android.util.TypedValue.COMPLEX_UNIT_PX
        );

        if (isBold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if (slotData.optBoolean("shadow", true)) {
            textView.setShadowLayer(3, 1, 1, Color.BLACK);
        }

        if (slotData.has("icon") && overlayContainer != null) {
            JSONObject iconSlot = slotData.optJSONObject("icon");
            if (iconSlot != null) {
                ImageView ivIcon = new ImageView(context);
                ivIcon.setTag("dynamic_icon");
                String iconType = iconSlot.optString("type", "");
                int iconRes = 0;
                if (iconType.equals("location") || "address".equals(slotKey)) iconRes = com.festival.flyer.postermaker.R.drawable.spawner_ic_location;
                else if (iconType.equals("phone") || "phone".equals(slotKey)) iconRes = com.festival.flyer.postermaker.R.drawable.spawner_ic_phone;
                else if (iconType.equals("email") || "email".equals(slotKey)) iconRes = com.festival.flyer.postermaker.R.drawable.spawner_ic_email;
                if (iconRes != 0) {
                    ivIcon.setImageResource(iconRes);
                    String iconColorHex = iconSlot.optString("color", "");
                    int iconColor = defaultColor;
                    if (!iconColorHex.isEmpty()) {
                        try { iconColor = Color.parseColor(iconColorHex); } catch (Exception e) {}
                    }
                    ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconColor));
                    int iw = iconSlot.optInt("w", 50);
                    int ih = iconSlot.optInt("h", 50);
                    if (iw <= 0) iw = 50;
                    if (ih <= 0) ih = 50;
                    FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams((int)(iw * scale), (int)(ih * scale));
                    iconLp.leftMargin = (int)(iconSlot.optInt("x") * scale);
                    iconLp.topMargin = (int)(iconSlot.optInt("y") * scale);
                    ivIcon.setLayoutParams(iconLp);
                    ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    overlayContainer.addView(ivIcon);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return displayList != null ? displayList.size() : 0;
    }

    public static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout content_layout, ad_layout, native_banner_ad_container;

        public NativeAdViewHolder(@NonNull View itemView) {
            super(itemView);
            content_layout = itemView.findViewById(R.id.content_layout);
            ad_layout = itemView.findViewById(R.id.ad_layout);
            native_banner_ad_container = itemView.findViewById(R.id.native_banner_ad_container);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout posterCard, ratioContainer;
        FrameLayout overlayContainer;
        ImageView ivThumbnail, slotLogo, ivLike;
        TextView slotName, slotPhone, slotEmail, slotAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            posterCard = itemView.findViewById(R.id.poster_card);
            ratioContainer = itemView.findViewById(R.id.ratio_container);
            overlayContainer = itemView.findViewById(R.id.overlay_container);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            ivLike = itemView.findViewById(R.id.iv_like);
            slotLogo = itemView.findViewById(R.id.slot_logo);
            slotName = itemView.findViewById(R.id.slot_business_name);
            slotPhone = itemView.findViewById(R.id.slot_phone);
            slotEmail = itemView.findViewById(R.id.slot_email);
            slotAddress = itemView.findViewById(R.id.slot_address);
        }
    }
}
