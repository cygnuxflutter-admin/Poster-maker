package com.festival.flyer.postermaker.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter;
import com.festival.flyer.postermaker.controller.MailER_TemplateSelectionController;
import com.festival.flyer.postermaker.model.MailER_PosterModel;

import java.util.ArrayList;

public class MailER_AllCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private EditText etSearch;
    private MailER_CategoryTabAdapter adapter;
    private ArrayList<MailER_PosterModel> allCategoriesList;
    private ArrayList<MailER_PosterModel> filteredList;

    private LinearLayout llTitleView, llSearchView;
    private ImageView icSearchToggle, icCloseSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_all_categories_full);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView icBack = findViewById(R.id.ic_back);
        icBack.setOnClickListener(v -> finish());

        llTitleView = findViewById(R.id.ll_title_view);
        llSearchView = findViewById(R.id.ll_search_view);
        icSearchToggle = findViewById(R.id.ic_search_toggle);
        icCloseSearch = findViewById(R.id.ic_close_search);
        etSearch = findViewById(R.id.et_search);
        
        icSearchToggle.setOnClickListener(v -> {
            llTitleView.setVisibility(View.GONE);
            icSearchToggle.setVisibility(View.GONE);
            llSearchView.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            // Show keyboard if needed
        });
        
        icCloseSearch.setOnClickListener(v -> {
            etSearch.setText("");
            llSearchView.setVisibility(View.GONE);
            llTitleView.setVisibility(View.VISIBLE);
            icSearchToggle.setVisibility(View.VISIBLE);
        });

        rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));

        allCategoriesList = new ArrayList<>();
        filteredList = new ArrayList<>();

        if (MailER_TemplateSelectionController.allCategoriesData != null && !MailER_TemplateSelectionController.allCategoriesData.isEmpty()) {
            for (MailER_PosterModel model : MailER_TemplateSelectionController.allCategoriesData) {
                if (!"More".equalsIgnoreCase(model.getCat_name())) {
                    allCategoriesList.add(model);
                    filteredList.add(model);
                }
            }
        } else {
            String cached = com.festival.flyer.postermaker.utils.MailER_AppCacheManager.getTemplates(this);
            if (cached != null && !cached.isEmpty()) {
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(cached);
                    org.json.JSONArray arr = obj.getJSONArray(new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this).getDataType("field_4"));
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject c = arr.getJSONObject(i);
                        String id = c.getString(new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this).getDataType("field_46"));
                        String name = c.getString(new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this).getDataType("field_5"));
                        String thumb = c.optString(new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this).getDataType("field_6"), "");
                        MailER_PosterModel model = new MailER_PosterModel(id, thumb, name, new ArrayList<>());
                        allCategoriesList.add(model);
                        filteredList.add(model);
                    }
                } catch (Exception ignored) {}
            }
        }

        adapter = new MailER_CategoryTabAdapter(this, filteredList, true, (position, model) -> {
            int actualIndex = -1;
            if (MailER_TemplateSelectionController.allCategoriesData != null) {
                for (int i = 0; i < MailER_TemplateSelectionController.allCategoriesData.size(); i++) {
                    MailER_PosterModel pm = MailER_TemplateSelectionController.allCategoriesData.get(i);
                    if (pm.getCat_id() == model.getCat_id() ||
                        (pm.getCat_name() != null && pm.getCat_name().equalsIgnoreCase(model.getCat_name()))) {
                        actualIndex = i;
                        break;
                    }
                }
            }
            if (MailER_TemplateSelectionController.globalSelectionListener != null && actualIndex != -1) {
                MailER_TemplateSelectionController.globalSelectionListener.onCategorySelected(actualIndex);
            }
            finish();
        });

        int selectedIndex = getIntent().getIntExtra("selected_index", -1);
        if (selectedIndex != -1 && MailER_TemplateSelectionController.allCategoriesData != null) {
            if (selectedIndex >= 0 && selectedIndex < MailER_TemplateSelectionController.allCategoriesData.size()) {
                MailER_PosterModel selectedModel = MailER_TemplateSelectionController.allCategoriesData.get(selectedIndex);
                for (int i = 0; i < filteredList.size(); i++) {
                    if (filteredList.get(i).getCat_id() == selectedModel.getCat_id()) {
                        adapter.setSelectedPosition(i);
                        break;
                    }
                }
            }
        }

        rvCategories.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(allCategoriesList);
        } else {
            text = text.toLowerCase();
            for (MailER_PosterModel item : allCategoriesList) {
                String catName = item.getCat_name();
                if (catName != null && catName.toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        }
        
        int selectedIndex = getIntent().getIntExtra("selected_index", -1);
        if (selectedIndex != -1 && MailER_TemplateSelectionController.allCategoriesData != null && selectedIndex < MailER_TemplateSelectionController.allCategoriesData.size()) {
            MailER_PosterModel selectedModel = MailER_TemplateSelectionController.allCategoriesData.get(selectedIndex);
            int mappedIndex = -1;
            for (int i = 0; i < filteredList.size(); i++) {
                if (filteredList.get(i).getCat_id() == selectedModel.getCat_id()) {
                    mappedIndex = i;
                    break;
                }
            }
            adapter.setSelectedPosition(mappedIndex);
        }
        adapter.notifyDataSetChanged();
    }
}

