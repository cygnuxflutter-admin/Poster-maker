package com.festival.flyer.postermaker.activities;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.makeityours.auto.MailER_AutoCategoryAdapter;
import com.festival.flyer.postermaker.makeityours.auto.MailER_AutoPosterAdapter;
import com.festival.flyer.postermaker.utils.MailER_CustomSwipeRefreshLayout;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MailER_CustomCategoryActivity extends AppCompatActivity {
    public interface AutoCategorySelectionListener {
        void onCategorySelected(int actualIndex);
    }
    public static AutoCategorySelectionListener globalSelectionListener;
    public static org.json.JSONArray allCategoriesData;

    private boolean isProModeActive = false;
    private org.json.JSONArray allCategoriesRawList;
    private int selectedCategoryActualIndex = 0;

    private ImageView ic_back;
    private RecyclerView rv_category_tabs;
    private ViewPager viewPager;
    private MailER_CustomSwipeRefreshLayout swipe_refresh_layout;

    private MailER_AutoCategoryAdapter categoryAdapter;
    private CategoryPagerAdapter pagerAdapter;
    private MaterialDialog materialDialog;

    private final Map<Integer, JSONArray> categoryPostersMap = new HashMap<>();
    private final Map<Integer, String> categoryRawDataMap = new HashMap<>();
    private final Map<Integer, RecyclerView> pageRecyclerViewMap = new HashMap<>();
    private final Map<Integer, MailER_AutoPosterAdapter> pageAdapterMap = new HashMap<>();
    private final java.util.Set<Integer> pendingFetchCategories = new java.util.HashSet<>();

    private RequestQueue requestQueue;
    private final String BASE_URL = "https://cygnux.in/postermaker/API/V1/MakeItYoursAPI/";
    private final String SECRET_KEY = "drytdsry6ne48ndr8";
    private boolean initialScreenLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_custom_category);

        requestQueue = Volley.newRequestQueue(this);
        initViews();
        setupListeners();
        setupBottomNav();

        fetchCategoriesAPI();
        loadBannerAd();
    }

    private void initViews() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                com.festival.flyer.postermaker.MyApplication.showInterstitialAd(MailER_CustomCategoryActivity.this, () -> finish());
            }
        });

        ic_back = findViewById(R.id.ic_back);
        rv_category_tabs = findViewById(R.id.rv_category_tabs);
        viewPager = findViewById(R.id.viewPager);
        swipe_refresh_layout = findViewById(R.id.swipe_refresh_layout);

        rv_category_tabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupListeners() {
        if (ic_back != null) {
            ic_back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
        if (swipe_refresh_layout != null) {
            swipe_refresh_layout.setOnRefreshListener(() -> {
                categoryPostersMap.clear();
                categoryRawDataMap.clear();
                pageAdapterMap.clear();
                pendingFetchCategories.clear();
                fetchCategoriesAPI();
                loadBannerAd();
            });
        }

        View proToggle = findViewById(R.id.ll_pro_toggle);
        if (proToggle != null) {
            proToggle.setOnClickListener(v -> {
                isProModeActive = !isProModeActive;
                if (isProModeActive) {
                    proToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFD700")));
                } else {
                    proToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF8C00")));
                }
                filterAllVisiblePages();
            });
        }
    }

    public void startMaterialDialog() {
        if (isFinishing() || isDestroyed()) return;
        if (materialDialog != null && materialDialog.isShowing()) {
            return;
        }
        materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(this);
        if (materialDialog.getWindow() != null) {
            materialDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        materialDialog.setCancelable(false);
        materialDialog.show();
    }

    public void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allCategoriesRawList != null && allCategoriesRawList.length() > 0) {
            dismissMaterialDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
            materialDialog = null;
        }
        globalSelectionListener = null;
        allCategoriesData = null;
    }

    private void loadBannerAd() {
        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(this);
        boolean isBannerEnabled = preferenceClass.getBoolean("custom_category_banner_ad_enabled", preferenceClass.getAdsStatus("BrandPoster_BannerAD") == 1);
        String bannerAdId = preferenceClass.getAdsId("custom_category_banner_ad_id");

        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            if (rc != null) {
                if (rc.getValue("custom_category_banner_ad_enabled").getSource() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                    isBannerEnabled = rc.getBoolean("custom_category_banner_ad_enabled");
                }
                if (rc.getValue("custom_category_banner_ad_id").getSource() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                    String remoteId = rc.getString("custom_category_banner_ad_id");
                    if (!remoteId.trim().isEmpty()) bannerAdId = remoteId.trim();
                }
            }
        } catch (Exception ignored) {}

        android.widget.RelativeLayout btm = findViewById(R.id.btm);
        android.widget.RelativeLayout rl_ad = findViewById(R.id.rl_ad);
        com.facebook.shimmer.ShimmerFrameLayout shimmer_view_container = findViewById(R.id.shimmer_view_container);

        if (btm != null) btm.setVisibility(android.view.View.GONE);
        if (rl_ad != null) rl_ad.setVisibility(android.view.View.GONE);
        if (shimmer_view_container != null) {
            shimmer_view_container.stopShimmer();
            shimmer_view_container.setVisibility(android.view.View.GONE);
        }
        if (com.festival.flyer.postermaker.utils.MailER_NetworkUtils.isNetworkAvailable(this) && isBannerEnabled) {
            com.festival.flyer.postermaker.adManager.MailER_LoadAds.loadAdmobBannerAd(this, rl_ad, shimmer_view_container, bannerAdId);
        }
    }

    private void filterAllVisiblePages() {
        for (Map.Entry<Integer, RecyclerView> entry : pageRecyclerViewMap.entrySet()) {
            int pos = entry.getKey();
            RecyclerView rv = entry.getValue();
            displayPostersOnPage(pos, rv);
        }
    }

    private void displayPostersOnPage(int position, RecyclerView rv) {
        if (rv == null) return;
        JSONArray posters = categoryPostersMap.get(position);
        String rawData = categoryRawDataMap.get(position);
        if (posters == null || rawData == null) return;

        try {
            JSONArray filtered = new JSONArray();
            for (int i = 0; i < posters.length(); i++) {
                JSONObject poster = posters.getJSONObject(i);
                if (!isProModeActive || poster.optBoolean("is_premium_local", false)) {
                    filtered.put(poster);
                }
            }
            MailER_AutoPosterAdapter adapter = new MailER_AutoPosterAdapter(this, filtered, rawData);
            pageAdapterMap.put(position, adapter);
            rv.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray getVisibleTabs(int selectedActualIndex) {
        JSONArray visibleTabs = new JSONArray();
        try {
            if (allCategoriesRawList == null || allCategoriesRawList.length() == 0) {
                return visibleTabs;
            }

            if (allCategoriesRawList.length() <= 3) {
                for (int i = 0; i < allCategoriesRawList.length(); i++) {
                    visibleTabs.put(allCategoriesRawList.getJSONObject(i));
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    visibleTabs.put(allCategoriesRawList.getJSONObject(i));
                }

                if (selectedActualIndex > 2 && selectedActualIndex < allCategoriesRawList.length()) {
                    visibleTabs.put(allCategoriesRawList.getJSONObject(selectedActualIndex));
                }

                JSONObject moreObj = new JSONObject();
                moreObj.put("id", "-1");
                moreObj.put("name", "More");
                visibleTabs.put(moreObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return visibleTabs;
    }

    private void updateVisibleTabs(int selectedActualIndex) {
        if (allCategoriesRawList == null) return;
        JSONArray visibleTabs = getVisibleTabs(selectedActualIndex);
        int tabPositionToSelect = (selectedActualIndex > 2) ? 3 : selectedActualIndex;

        if (categoryAdapter == null) {
            categoryAdapter = new MailER_AutoCategoryAdapter(this, visibleTabs, false, this::onCategoryTabClicked);
            categoryAdapter.setSelectedPosition(tabPositionToSelect);
            rv_category_tabs.setAdapter(categoryAdapter);
        } else {
            categoryAdapter.updateData(visibleTabs);
            categoryAdapter.setSelectedPosition(tabPositionToSelect);
        }
        rv_category_tabs.smoothScrollToPosition(tabPositionToSelect);
    }

    private void onCategoryTabClicked(String categoryId) {
        if ("-1".equals(categoryId)) {
            globalSelectionListener = actualIndex -> {
                if (viewPager != null && allCategoriesRawList != null && actualIndex >= 0 && actualIndex < allCategoriesRawList.length()) {
                    if (viewPager.getCurrentItem() != actualIndex) {
                        viewPager.setCurrentItem(actualIndex, true);
                    } else {
                        selectedCategoryActualIndex = actualIndex;
                        updateVisibleTabs(actualIndex);
                        fetchPostersForCategory(actualIndex);
                    }
                }
            };
            android.content.Intent intent = new android.content.Intent(this, MailER_AllAutoCategoriesActivity.class);
            intent.putExtra("selected_index", selectedCategoryActualIndex);
            startActivity(intent);
            return;
        }

        if (allCategoriesRawList == null) return;
        for (int i = 0; i < allCategoriesRawList.length(); i++) {
            try {
                JSONObject obj = allCategoriesRawList.getJSONObject(i);
                String id = obj.optString("id", obj.optString("cat_id", ""));
                if (id.equals(categoryId)) {
                    if (viewPager != null) {
                        if (viewPager.getCurrentItem() != i) {
                            viewPager.setCurrentItem(i, true);
                        } else {
                            selectedCategoryActualIndex = i;
                            updateVisibleTabs(i);
                            fetchPostersForCategory(i);
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupBottomNav() {
        com.festival.flyer.postermaker.utils.MailER_BottomNavHelper.setupBottomNav(this, com.festival.flyer.postermaker.R.id.tab_creations);
    }

    private class CategoryPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return allCategoriesRawList != null ? allCategoriesRawList.length() : 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            RecyclerView rv = new RecyclerView(MailER_CustomCategoryActivity.this);
            rv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            GridLayoutManager glm = new GridLayoutManager(MailER_CustomCategoryActivity.this, 2);
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int pos) {
                    RecyclerView.Adapter adapter = rv.getAdapter();
                    if (adapter instanceof MailER_AutoPosterAdapter) {
                        if (((MailER_AutoPosterAdapter) adapter).isNativeAdPosition(pos)) {
                            return 2;
                        }
                    }
                    return 1;
                }
            });
            rv.setLayoutManager(glm);
            rv.setClipToPadding(false);
            float density = getResources().getDisplayMetrics().density;
            int pad = (int) (8 * density);
            int bottomPad = (int) (80 * density);
            rv.setPadding(pad, pad, pad, bottomPad);

            pageRecyclerViewMap.put(position, rv);

            if (categoryPostersMap.containsKey(position)) {
                displayPostersOnPage(position, rv);
            } else if (position == viewPager.getCurrentItem()) {
                fetchPostersForCategory(position);
            }

            container.addView(rv);
            return rv;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            pageRecyclerViewMap.remove(position);
            pageAdapterMap.remove(position);
            container.removeView((View) object);
        }
    }

    private void fetchCategoriesAPI() {
        startMaterialDialog();
        String url = BASE_URL + "categories";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (swipe_refresh_layout != null) swipe_refresh_layout.setRefreshing(false);
                    try {
                        Log.d("CATEGORY_THUMB_DEBUG", "=== CATEGORIES API RAW RESPONSE ===\n" + response);

                        JSONObject responseObj = new JSONObject(response);
                        if (responseObj.has("data")) {
                            JSONArray data = responseObj.getJSONArray("data");
                            allCategoriesRawList = data;
                            allCategoriesData = data;
                            selectedCategoryActualIndex = 0;
                            updateVisibleTabs(0);

                            pagerAdapter = new CategoryPagerAdapter();
                            viewPager.setAdapter(pagerAdapter);
                            viewPager.clearOnPageChangeListeners();
                            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                                @Override
                                public void onPageSelected(int position) {
                                    selectedCategoryActualIndex = position;
                                    updateVisibleTabs(position);
                                    fetchPostersForCategory(position);
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {}
                            });

                            if (data.length() > 0) {
                                fetchPostersForCategory(0);
                            } else {
                                dismissMaterialDialog();
                            }
                        } else {
                            dismissMaterialDialog();
                        }
                    } catch (Exception e) {
                        dismissMaterialDialog();
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing categories", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (swipe_refresh_layout != null) swipe_refresh_layout.setRefreshing(false);
                    dismissMaterialDialog();
                    Log.e("API_ERROR", "Error fetching categories: " + error.getMessage());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                params.put("key", new MailER_PreferenceClass(getApplicationContext()).getDataType("field_0"));
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void fetchPostersForCategory(int categoryIndex) {
        if (allCategoriesRawList == null || categoryIndex < 0 || categoryIndex >= allCategoriesRawList.length()) return;

        if (categoryPostersMap.containsKey(categoryIndex)) {
            RecyclerView rv = pageRecyclerViewMap.get(categoryIndex);
            if (rv != null) {
                displayPostersOnPage(categoryIndex, rv);
            }
            dismissMaterialDialog();
            return;
        }

        if (pendingFetchCategories.contains(categoryIndex)) return;
        pendingFetchCategories.add(categoryIndex);

        String categoryId;
        try {
            JSONObject catObj = allCategoriesRawList.getJSONObject(categoryIndex);
            categoryId = catObj.optString("id", catObj.optString("cat_id", ""));
        } catch (Exception e) {
            e.printStackTrace();
            pendingFetchCategories.remove(categoryIndex);
            dismissMaterialDialog();
            return;
        }

        startMaterialDialog();
        String url = BASE_URL + "posters";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    pendingFetchCategories.remove(categoryIndex);
                    try {
                        Log.d("API_DEBUG", "Posters Response: " + response);

                        JSONObject responseObj = new JSONObject(response);
                        if (responseObj.has("data")) {
                            JSONArray data = responseObj.getJSONArray("data");

                            // Group by design_id
                            JSONArray uniquePosters = new JSONArray();
                            java.util.HashSet<String> seenDesignIds = new java.util.HashSet<>();
                            MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(MailER_CustomCategoryActivity.this);
                            boolean premiumEnabled = preferenceClass.getBoolean("branding_premium_enabled", preferenceClass.getBoolean("premium_enabled", true));
                            int premiumCount = preferenceClass.getInt("branding_premium_interval", preferenceClass.getInt("PremiumPostCount", 3));
                            int uniqueIndex = 0;

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject poster = data.getJSONObject(i);
                                String designId = poster.optString("design_id", poster.optString("id", ""));
                                if (!seenDesignIds.contains(designId)) {
                                    seenDesignIds.add(designId);

                                    boolean isPremium = premiumEnabled && (premiumCount > 0) && (uniqueIndex != 0) && (uniqueIndex % premiumCount == 0);
                                    poster.put("is_premium_local", isPremium);
                                    uniquePosters.put(poster);
                                    uniqueIndex++;
                                }
                            }

                            categoryPostersMap.put(categoryIndex, uniquePosters);
                            categoryRawDataMap.put(categoryIndex, data.toString());

                            RecyclerView rv = pageRecyclerViewMap.get(categoryIndex);
                            if (rv != null) {
                                displayPostersOnPage(categoryIndex, rv);
                            }
                            if (categoryIndex == 0 && initialScreenLoad) {
                                initialScreenLoad = false;
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::dismissMaterialDialog, 800);
                            } else {
                                dismissMaterialDialog();
                            }
                        } else {
                            dismissMaterialDialog();
                        }
                    } catch (Exception e) {
                        dismissMaterialDialog();
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing posters", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    pendingFetchCategories.remove(categoryIndex);
                    dismissMaterialDialog();
                    Log.e("API_ERROR", "Error fetching posters: " + error.getMessage());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                params.put("key", new MailER_PreferenceClass(getApplicationContext()).getDataType("field_0"));
                params.put("category_id", categoryId);
                params.put("cat_id", categoryId); // fallback

                SharedPreferences prefs = getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE);
                String uid = prefs.getString("user_uid", "");
                if (!uid.isEmpty()) {
                    params.put("uid", uid);
                    params.put("user_uid", uid);
                    params.put("user_id", uid);
                }
                return params;
            }
        };

        requestQueue.add(request);
    }
}
