package com.festival.flyer.postermaker.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.makeityours.auto.MailER_AutoCategoryAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MailER_AllAutoCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private EditText etSearch;
    private MailER_AutoCategoryAdapter adapter;
    private ArrayList<JSONObject> allCategoriesList;
    private ArrayList<JSONObject> filteredList;

    private LinearLayout llTitleView, llSearchView;
    private ImageView icSearchToggle, icCloseSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_all_categories_full);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                com.festival.flyer.postermaker.MyApplication.showInterstitialAd(MailER_AllAutoCategoriesActivity.this, () -> finish());
            }
        });

        ImageView icBack = findViewById(R.id.ic_back);
        icBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

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
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        icCloseSearch.setOnClickListener(v -> {
            etSearch.setText("");
            llSearchView.setVisibility(View.GONE);
            llTitleView.setVisibility(View.VISIBLE);
            icSearchToggle.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        });

        rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));

        allCategoriesList = new ArrayList<>();
        filteredList = new ArrayList<>();

        if (MailER_CustomCategoryActivity.allCategoriesData != null) {
            for (int i = 0; i < MailER_CustomCategoryActivity.allCategoriesData.length(); i++) {
                try {
                    JSONObject obj = MailER_CustomCategoryActivity.allCategoriesData.getJSONObject(i);
                    String name = obj.optString("name", obj.optString("cat_name", ""));
                    String id = obj.optString("id", obj.optString("cat_id", ""));
                    if (!"More".equalsIgnoreCase(name) && !"-1".equals(id)) {
                        allCategoriesList.add(obj);
                        filteredList.add(obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        JSONArray initialArray = new JSONArray();
        for (JSONObject obj : filteredList) {
            initialArray.put(obj);
        }

        adapter = new MailER_AutoCategoryAdapter(this, initialArray, true, categoryId -> {
            int actualIndex = -1;
            if (MailER_CustomCategoryActivity.allCategoriesData != null) {
                for (int i = 0; i < MailER_CustomCategoryActivity.allCategoriesData.length(); i++) {
                    try {
                        JSONObject obj = MailER_CustomCategoryActivity.allCategoriesData.getJSONObject(i);
                        String id = obj.optString("id", obj.optString("cat_id", ""));
                        if (id.equals(categoryId)) {
                            actualIndex = i;
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (MailER_CustomCategoryActivity.globalSelectionListener != null && actualIndex != -1) {
                MailER_CustomCategoryActivity.globalSelectionListener.onCategorySelected(actualIndex);
            }
            finish();
        });

        int selectedIndex = getIntent().getIntExtra("selected_index", -1);
        if (selectedIndex != -1 && MailER_CustomCategoryActivity.allCategoriesData != null) {
            try {
                if (selectedIndex >= 0 && selectedIndex < MailER_CustomCategoryActivity.allCategoriesData.length()) {
                    JSONObject selectedModel = MailER_CustomCategoryActivity.allCategoriesData.getJSONObject(selectedIndex);
                    String selectedId = selectedModel.optString("id", selectedModel.optString("cat_id", ""));
                    int mappedIndex = -1;
                    for (int i = 0; i < filteredList.size(); i++) {
                        String id = filteredList.get(i).optString("id", filteredList.get(i).optString("cat_id", ""));
                        if (id.equals(selectedId)) {
                            mappedIndex = i;
                            break;
                        }
                    }
                    if (mappedIndex != -1) {
                        adapter.setSelectedPosition(mappedIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            String query = text.toLowerCase();
            for (JSONObject item : allCategoriesList) {
                String catName = item.optString("name", item.optString("cat_name", ""));
                if (catName != null && catName.toLowerCase().contains(query)) {
                    filteredList.add(item);
                }
            }
        }

        JSONArray filteredArray = new JSONArray();
        for (JSONObject obj : filteredList) {
            filteredArray.put(obj);
        }
        adapter.updateData(filteredArray);

        int selectedIndex = getIntent().getIntExtra("selected_index", -1);
        if (selectedIndex != -1 && MailER_CustomCategoryActivity.allCategoriesData != null && selectedIndex < MailER_CustomCategoryActivity.allCategoriesData.length()) {
            try {
                JSONObject selectedModel = MailER_CustomCategoryActivity.allCategoriesData.getJSONObject(selectedIndex);
                String selectedId = selectedModel.optString("id", selectedModel.optString("cat_id", ""));
                int mappedIndex = -1;
                for (int i = 0; i < filteredList.size(); i++) {
                    String id = filteredList.get(i).optString("id", filteredList.get(i).optString("cat_id", ""));
                    if (id.equals(selectedId)) {
                        mappedIndex = i;
                        break;
                    }
                }
                adapter.setSelectedPosition(mappedIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
