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
                ArrayList<String> strings = new ArrayList<>();
                strings.add(path);
                
                com.afollestad.materialdialogs.MaterialDialog pd = com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_AllBackgroundsActivity.this);
                pd.setCancelable(false);
                pd.show();
                
                new com.festival.flyer.postermaker.threadTask.MailER_DlBgAsync(this, strings, new com.festival.flyer.postermaker.threadTask.MailER_DlBgAsync.OnDownloadTemplateListener() {
                    @Override
                    public void onDownloadComplete() {
                        pd.dismiss();
                        String localPath = com.festival.flyer.postermaker.utils.MailER_FileUtils.getFile(MailER_AllBackgroundsActivity.this, path);
                        Intent intent = new Intent(MailER_AllBackgroundsActivity.this, MailER_PosterEditActivity.class);
                        intent.putExtra("bg_path", localPath);
                        intent.putExtra("loadUserFrame", true);
                        intent.putExtra("Temp_Type", "MY_TEMP");
                        com.festival.flyer.postermaker.MyApplication.showInterstitialAd(MailER_AllBackgroundsActivity.this, () -> startActivity(intent));
                    }

                    @Override
                    public void onError() {
                        pd.dismiss();
                        com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_AllBackgroundsActivity.this, "Something went wrong!!!");
                    }
                }).execute();
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
