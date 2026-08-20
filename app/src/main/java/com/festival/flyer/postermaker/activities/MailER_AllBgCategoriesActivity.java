package com.festival.flyer.postermaker.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_BgCategoryTabAdapter;
import com.festival.flyer.postermaker.controller.MailER_BGSelectionController;
import com.festival.flyer.postermaker.model.MailER_BgModel;

import java.util.ArrayList;

public class MailER_AllBgCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private EditText etSearch;
    private MailER_BgCategoryTabAdapter adapter;
    private ArrayList<MailER_BgModel> allCategoriesList;
    private ArrayList<MailER_BgModel> filteredList;

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
        
        // Note: Title says "All Categories" based on spawner_activity_all_categories_full.xml which is fine as per Image 3.


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

        if (MailER_BGSelectionController.allCategoriesData != null) {
            for (MailER_BgModel model : MailER_BGSelectionController.allCategoriesData) {
                if (!"More".equalsIgnoreCase(model.getCategory_name())) {
                    allCategoriesList.add(model);
                    filteredList.add(model);
                }
            }
        }

        adapter = new MailER_BgCategoryTabAdapter(this, filteredList, true, (position, model) -> {
            int actualIndex = -1;
            if (MailER_BGSelectionController.allCategoriesData != null) {
                actualIndex = MailER_BGSelectionController.allCategoriesData.indexOf(model);
            }
            if (MailER_BGSelectionController.globalSelectionListener != null && actualIndex != -1) {
                MailER_BGSelectionController.globalSelectionListener.onCategorySelected(actualIndex);
            }
            finish();
        });

        int selectedIndex = getIntent().getIntExtra("selected_index", -1);
        if (selectedIndex != -1 && MailER_BGSelectionController.allCategoriesData != null) {
            if (selectedIndex >= 0 && selectedIndex < MailER_BGSelectionController.allCategoriesData.size()) {
                MailER_BgModel selectedModel = MailER_BGSelectionController.allCategoriesData.get(selectedIndex);
                int mappedIndex = filteredList.indexOf(selectedModel);
                if (mappedIndex != -1) {
                    adapter.setSelectedPosition(mappedIndex);
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
            for (MailER_BgModel item : allCategoriesList) {
                String catName = item.getCategory_name();
                if (catName != null && catName.toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        }
        
        int selectedIndex = getIntent().getIntExtra("selected_index", -1);
        if (selectedIndex != -1 && MailER_BGSelectionController.allCategoriesData != null && selectedIndex < MailER_BGSelectionController.allCategoriesData.size()) {
            MailER_BgModel selectedModel = MailER_BGSelectionController.allCategoriesData.get(selectedIndex);
            int mappedIndex = filteredList.indexOf(selectedModel);
            if (mappedIndex != -1) {
                adapter.setSelectedPosition(mappedIndex);
            } else {
                adapter.setSelectedPosition(-1);
            }
        }
        adapter.notifyDataSetChanged();
    }
}

