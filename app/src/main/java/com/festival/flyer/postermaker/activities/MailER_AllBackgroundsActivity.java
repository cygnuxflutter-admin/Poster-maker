package com.festival.flyer.postermaker.activities;

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

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_BackgroundChildAdapter;
import com.festival.flyer.postermaker.controller.MailER_BGSelectionController;
import com.festival.flyer.postermaker.model.MailER_BgImage;
import com.festival.flyer.postermaker.model.MailER_BgModel;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;

import java.util.ArrayList;

public class MailER_AllBackgroundsActivity extends AppCompatActivity {

    private RecyclerView rvAllBg;
    private TextView tvNoData;
    private MailER_BackgroundChildAdapter adapter;
    private ArrayList<MailER_BgImage> allImages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_all_backgrounds);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.ic_back).setOnClickListener(v -> onBackPressed());

        rvAllBg = findViewById(R.id.rv_all_bg);
        tvNoData = findViewById(R.id.tv_no_data);

        setupRecyclerView();
        loadAllBackgrounds();
    }

    private void setupRecyclerView() {
        rvAllBg.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvAllBg.setLayoutManager(layoutManager);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = displayMetrics.widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 2;
        int cellHeight = (700 * cellWidth) / 507;

        adapter = new MailER_BackgroundChildAdapter(this, allImages, cellWidth, cellHeight, (path, premium) -> {
            if (MailER_NetworkUtils.isNetworkAvailable(this)) {
                int pos = 0;
                for (int i = 0; i < allImages.size(); i++) {
                    if (allImages.get(i) != null && path.equals(allImages.get(i).getImage_url())) {
                        pos = i;
                        break;
                    }
                }
                Intent intent = new Intent(this, MailER_BackgroundPreviewActivity.class);
                intent.putExtra("cat_name", "All Backgrounds");
                intent.putExtra("selected_position", pos);
                intent.putExtra("selected_path", path);
                MailER_BackgroundPreviewActivity.tempBgList = allImages;
                intent.putExtra("bg_list", allImages);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
            }
        });
        rvAllBg.setAdapter(adapter);
    }

    private void loadAllBackgrounds() {
        allImages.clear();
        if (MailER_BGSelectionController.allCategoriesData != null) {
            for (MailER_BgModel model : MailER_BGSelectionController.allCategoriesData) {
                if (!"More".equalsIgnoreCase(model.getCategory_name()) && model.getCategory_list() != null) {
                    allImages.addAll(model.getCategory_list());
                }
            }
        }

        if (allImages.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            tvNoData.setText("No backgrounds found.");
            rvAllBg.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvAllBg.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}
