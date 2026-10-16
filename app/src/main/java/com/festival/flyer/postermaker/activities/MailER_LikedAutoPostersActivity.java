package com.festival.flyer.postermaker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.makeityours.auto.MailER_AutoPosterAdapter;
import com.festival.flyer.postermaker.utils.MailER_AutoPosterLikeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MailER_LikedAutoPostersActivity extends AppCompatActivity {

    private RecyclerView rvLiked;
    private TextView tvNoData;
    private MailER_AutoPosterAdapter adapter;
    private MailER_AutoPosterLikeManager likeManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_liked_auto_posters);

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                com.festival.flyer.postermaker.MyApplication.showInterstitialAd(MailER_LikedAutoPostersActivity.this, () -> finish());
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.ic_back).setOnClickListener(v -> onBackPressed());

        rvLiked = findViewById(R.id.rv_liked);
        tvNoData = findViewById(R.id.tv_no_data);
        likeManager = new MailER_AutoPosterLikeManager(this);

        rvLiked.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLikedTemplates();
    }

    private void loadLikedTemplates() {
        List<String> likedStrings = likeManager.getLikedTemplates();
        if (likedStrings.isEmpty()) {
            rvLiked.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            rvLiked.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);

            JSONArray array = new JSONArray();
            for (String str : likedStrings) {
                try {
                    array.put(new JSONObject(str));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            adapter = new MailER_AutoPosterAdapter(this, array, array.toString());
            rvLiked.setAdapter(adapter);
        }
    }
}
