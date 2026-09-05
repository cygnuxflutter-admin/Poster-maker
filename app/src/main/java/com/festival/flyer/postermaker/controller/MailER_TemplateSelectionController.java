package com.festival.flyer.postermaker.controller;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_MainSecurity;
import com.festival.flyer.postermaker.fragment.MailER_TemplateFragment;
import com.festival.flyer.postermaker.fragment.MailER_TemplatePagerAdapter;
import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.festival.flyer.postermaker.model.MailER_PosterModel;
import com.festival.flyer.postermaker.threadTask.MailER_GetTemplateData;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.view.MailER_PagerSlidingTabStrip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MailER_TemplateSelectionController {

    private final Activity activity;
    private final FragmentManager supportFragmentManager;
    private final MailER_PreferenceClass preferenceClass;
    private MaterialDialog materialDialog;

    private String initialCategoryToSelect;

    public MailER_TemplateSelectionController(Activity activity, FragmentManager supportFragmentManager, MailER_PreferenceClass preferenceClass, String initialCategoryToSelect) {
        this.activity = activity;
        this.supportFragmentManager = supportFragmentManager;
        this.preferenceClass = preferenceClass;
        this.initialCategoryToSelect = initialCategoryToSelect;
        loadTemplates();
    }

    public void loadTemplates() {
        startMaterialDialog();
        getTemplateThumb(preferenceClass.getDataType("field_0"));
    }

    public void getTemplateThumb(final String key) {
        String requestUrl = preferenceClass.getDataType("field_1") + preferenceClass.getDataType("field_34") + preferenceClass.getDataType("field_35");
        android.util.Log.d("API_CALL_DEBUG", "REQUEST URL (Template): " + requestUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, response -> {
            android.util.Log.d("API_CALL_DEBUG", "RESPONSE FROM (Template): " + requestUrl + "\nDATA: " + response);
            try {
                Log.d("xgdgdg", "getTemplateThumb: " + response);
                JSONObject jsonObject = new JSONObject(response);
                int error = jsonObject.getInt(preferenceClass.getDataType("field_3"));
                if (error == 0 || error == 404) {
                    dismissMaterialDialog();
                    MailER_MaterialDialogUtils.getInstance().errorDialog2(activity, activity.getResources().getString(R.string.something_went_wrong));
                    return;
                }
                if (error == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray(preferenceClass.getDataType("field_4"));
                    if (jsonArray.length() == 0) {
                        MailER_MaterialDialogUtils.getInstance().errorDialog2(activity, activity.getResources().getString(R.string.something_went_wrong));
                    } else {
                        MailER_TemplateFragment.TempisFirstShow = false;
                        new MailER_GetTemplateData(preferenceClass, jsonArray, new MailER_GetTemplateData.OnGetCatDataListener() {
                            @Override
                            public void onGetDataComplete(ArrayList<MailER_PosterModel> posterDataLists) {
                                new android.os.Handler().postDelayed(() -> setPagerAdapter(posterDataLists), 1200);
                            }

                            @Override
                            public void onError() {
                                dismissMaterialDialog();
                                MailER_MaterialDialogUtils.getInstance().errorDialog(activity, activity.getResources().getString(R.string.something_went_wrong));
                            }
                        }).execute();
                    }
                }
            } catch (Exception e) {
                dismissMaterialDialog();

                try {

                    String text11 = null;
                    if (preferenceClass.getDecryptionType() == 0) {
                        byte[] octets = Base64.decode(response, Base64.URL_SAFE);
                        String text_base = new String(octets, StandardCharsets.UTF_8);
                        byte[] octets1 = Base64.decode(text_base, Base64.URL_SAFE);
                        text11 = new String(octets1, StandardCharsets.UTF_8);
                    } else if (preferenceClass.getDecryptionType() == 1) {
                        MailER_MainSecurity decrypted = MailER_MainSecurity.decrypt(preferenceClass.getDataType("main_key"), response);
                        text11 = decrypted.getData();
                    }

                    MailER_MaterialDialogUtils.getInstance().errorDialog(activity, text11);
                    e.printStackTrace();
                } catch (Exception e1) {
                    MailER_MaterialDialogUtils.getInstance().errorDialog(activity, e.getMessage());
                    e.printStackTrace();
                }
            }
        }, error -> {
            Log.e("---API_DATA---", "--- ERROR (Template Thumb) ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Error: " + error.getMessage());
            dismissMaterialDialog();
            MailER_MaterialDialogUtils.getInstance().errorDialog(activity, error.getMessage());
            error.printStackTrace();

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put(preferenceClass.getDataType("field_47"), key);
                postMap.put(preferenceClass.getDataType("field_48"), "1");
                postMap.put(preferenceClass.getDataType("field_46"), "0");
                postMap.put(preferenceClass.getDataType("field_8"), "0");
                Log.e("---API_DATA---", "Params: " + postMap);
                return postMap;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 300000;

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(activity).add(stringRequest);
    }

    public static ArrayList<MailER_PosterModel> allCategoriesData;

    public interface CategorySelectionListener {
        void onCategorySelected(int index);
    }
    public static CategorySelectionListener globalSelectionListener;

    public void applyProFilter(boolean isPro) {
        if (allCategoriesData == null) return;
        
        ArrayList<MailER_PosterModel> filteredCategories = new ArrayList<>();
        for (MailER_PosterModel category : allCategoriesData) {
            if ("More".equalsIgnoreCase(category.getCat_name())) continue;
            
            ArrayList<MailER_PosterImage> proPosters = new ArrayList<>();
            for (MailER_PosterImage poster : category.getPoster_list()) {
                if (!isPro || poster.getPremium()) {
                    proPosters.add(poster);
                }
            }
            // Even if empty, we can keep the category or hide it. Let's keep it so tabs don't disappear.
            MailER_PosterModel clonedCategory = new MailER_PosterModel(
                    String.valueOf(category.getCat_id()), 
                    category.getThumb_img(), 
                    category.getCat_name(), 
                    proPosters);
            filteredCategories.add(clonedCategory);
        }
        
        // Re-inject More tab
        MailER_PosterModel moreModel = new MailER_PosterModel("0", "", "More", new ArrayList<>());
        int insertIndex = Math.min(3, filteredCategories.size());
        filteredCategories.add(insertIndex, moreModel);
        
        ViewPager viewPager = activity.findViewById(R.id.viewPager);
        androidx.recyclerview.widget.RecyclerView rvCategories = activity.findViewById(R.id.rv_category_tabs);
        if (viewPager != null && rvCategories != null) {
            int currentItem = viewPager.getCurrentItem();
            viewPager.setAdapter(new MailER_TemplatePagerAdapter(supportFragmentManager, filteredCategories));
            setupTabsIcons(rvCategories, viewPager, filteredCategories);
            viewPager.setCurrentItem(currentItem, false);
        }
    }

    private void setPagerAdapter(ArrayList<MailER_PosterModel> posterModel) {
        allCategoriesData = new ArrayList<>(posterModel); // Save original for More bottom sheet

        MailER_TemplateFragment.TempisFirstShow = false;
        androidx.recyclerview.widget.RecyclerView rvCategories = activity.findViewById(R.id.rv_category_tabs);
        ViewPager viewPager = activity.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MailER_TemplatePagerAdapter(supportFragmentManager, posterModel));

        setupTabsIcons(rvCategories, viewPager, posterModel);

        if (initialCategoryToSelect != null && !initialCategoryToSelect.isEmpty()) {
            for (int i = 0; i < posterModel.size(); i++) {
                if (posterModel.get(i).getCat_name().toLowerCase().contains(initialCategoryToSelect.toLowerCase()) ||
                    String.valueOf(posterModel.get(i).getCat_id()).equals(initialCategoryToSelect)) {
                    viewPager.setCurrentItem(i, false);
                    break;
                }
            }
            initialCategoryToSelect = null; // Clear it so it only applies on first load
        }

        dismissMaterialDialog();
    }
    
    private void updateTabsData(com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter adapter, ArrayList<MailER_PosterModel> allCategories, int selectedIndex) {
        ArrayList<MailER_PosterModel> visibleTabs = new ArrayList<>();
        
        // Add up to first 3 categories
        for (int i = 0; i < Math.min(3, allCategories.size()); i++) {
            visibleTabs.add(allCategories.get(i));
        }
        
        // Handle hidden selected category and More tab
        if (allCategories.size() > 3) {
            if (selectedIndex > 2) {
                // User wants: 3 fixed + Selected Hidden + More (More is not removed)
                visibleTabs.add(allCategories.get(selectedIndex));
            }
            
            // Always add More tab at the end
            MailER_PosterModel moreModel = new MailER_PosterModel("0", "", "More", new ArrayList<>());
            visibleTabs.add(moreModel);
        }
        
        adapter.updateData(visibleTabs);
        
        // The selected index in the RecyclerView is either the actual index (0, 1, 2) or the 4th slot (3) if > 2
        int rvSelectedIndex = (selectedIndex > 2) ? 3 : selectedIndex;
        adapter.setSelectedPosition(rvSelectedIndex);
    }
    
    private void setupTabsIcons(androidx.recyclerview.widget.RecyclerView rvCategories, ViewPager viewPager, ArrayList<MailER_PosterModel> posterModel) {
        com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter adapter = new com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter(activity, new ArrayList<>(), false, null);
        
        adapter = new com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter(activity, new ArrayList<>(), false, (position, model) -> {
            if ("More".equalsIgnoreCase(model.getCat_name())) {
                globalSelectionListener = new CategorySelectionListener() {
                    @Override
                    public void onCategorySelected(int actualIndex) {
                        viewPager.setCurrentItem(actualIndex);
                    }
                };
                android.content.Intent intent = new android.content.Intent(activity, com.festival.flyer.postermaker.activities.MailER_AllCategoriesActivity.class);
                if (viewPager != null) {
                    intent.putExtra("selected_index", viewPager.getCurrentItem());
                }
                activity.startActivity(intent);
            } else {
                // Find the actual index of the clicked model in the original posterModel list
                int actualIndex = posterModel.indexOf(model);
                if (actualIndex != -1) {
                    viewPager.setCurrentItem(actualIndex);
                }
            }
        });
        rvCategories.setAdapter(adapter);

        // Initial setup
        updateTabsData(adapter, posterModel, viewPager.getCurrentItem());
        
        com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter finalAdapter = adapter;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                updateTabsData(finalAdapter, posterModel, position);
                int rvSelectedIndex = (position > 2) ? 3 : position;
                rvCategories.smoothScrollToPosition(rvSelectedIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void startMaterialDialog() {
        materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
        Objects.requireNonNull(materialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        materialDialog.setCancelable(false);
        materialDialog.show();
    }

    public void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing())
            materialDialog.dismiss();
    }

}
