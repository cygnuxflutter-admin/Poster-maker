package com.festival.flyer.postermaker.activities;

import android.app.ProgressDialog;
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

import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_BackgroundChildAdapter;
import com.festival.flyer.postermaker.model.MailER_BgImage;
import com.festival.flyer.postermaker.threadTask.MailER_DlBgAsync;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;
import com.festival.flyer.postermaker.utils.MailER_LikeManager;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;

import java.util.ArrayList;
import java.util.Set;

public class MailER_LikedBackgroundsActivity extends AppCompatActivity {

    private RecyclerView rvLiked;
    private TextView tvNoData;
    private MailER_BackgroundChildAdapter adapter;
    private ArrayList<MailER_BgImage> likedImages = new ArrayList<>();
    private MailER_LikeManager likeManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_liked_backgrounds);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.ic_back).setOnClickListener(v -> onBackPressed());

        rvLiked = findViewById(R.id.rv_liked);
        tvNoData = findViewById(R.id.tv_no_data);
        likeManager = new MailER_LikeManager(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedBackgrounds();
    }

    private void setupRecyclerView() {
        rvLiked.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvLiked.setLayoutManager(layoutManager);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = displayMetrics.widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 2;
        int cellHeight = (700 * cellWidth) / 507;

        adapter = new MailER_BackgroundChildAdapter(this, likedImages, cellWidth, cellHeight, (path, premium) -> {
            if (MailER_NetworkUtils.isNetworkAvailable(this)) {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(path);
                progressDialog.show();
                new MailER_DlBgAsync(this, strings, new MailER_DlBgAsync.OnDownloadTemplateListener() {
                    @Override
                    public void onDownloadComplete() {
                        progressDialog.dismiss();
                        String localPath = MailER_FileUtils.getFile(MailER_LikedBackgroundsActivity.this, path);
                        Intent intent = new Intent(MailER_LikedBackgroundsActivity.this, MailER_PosterEditActivity.class);
                        intent.putExtra("bg_path", localPath);
                        intent.putExtra("loadUserFrame", true);
                        intent.putExtra("Temp_Type", "MY_TEMP");
                        MyApplication.showInterstitialAd(MailER_LikedBackgroundsActivity.this, () -> startActivity(intent));
                    }

                    @Override
                    public void onError() {
                        progressDialog.dismiss();
                        MailER_MaterialDialogUtils.getInstance().errorDialog(MailER_LikedBackgroundsActivity.this, "Something went wrong!!!");
                    }
                }).execute();
            } else {
                Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
            }
        });
        rvLiked.setAdapter(adapter);
    }

    private void loadLikedBackgrounds() {
        likedImages.clear();
        Set<String> urls = likeManager.getLikedUrls();
        int idCounter = 1;
        for (String url : urls) {
            likedImages.add(new MailER_BgImage(idCounter++, url, url, false));
        }

        if (likedImages.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvLiked.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvLiked.setVisibility(View.VISIBLE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
