package com.festival.flyer.postermaker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_MyDesignAdapter;
import com.festival.flyer.postermaker.components.MailER_TemplateInfo;
import com.festival.flyer.postermaker.utils.MailER_DatabaseHandler;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import java.util.ArrayList;

import static com.festival.flyer.postermaker.adManager.MailER_LoadAds.loadAdmobBannerAd;

public class MailER_MyDesignActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmptyMsg;
    private int template_id, position;
    private MailER_MyDesignAdapter myDesignAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.spawner_activity_my_design);
        
        // Use modern approach for fullscreen to avoid black flash on transition
        // This must be called AFTER setContentView so the DecorView exists
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars());
                getWindow().getInsetsController().setSystemBarsBehavior(
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        findByID();

        findViewById(R.id.ic_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        setTemplateAdapter();

    }

    private void findByID() {
        recyclerView = findViewById(R.id.template_rv);
        txtEmptyMsg = findViewById(R.id.txtEmptyMsg);
        ShimmerFrameLayout shimmer_view_container = findViewById(R.id.shimmer_view_container);
        RelativeLayout rl_ad = findViewById(R.id.rl_ad);
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            if (new MailER_PreferenceClass(this).getAdsId("BannerAdunitID") != null) {
                loadAdmobBannerAd(this, rl_ad, shimmer_view_container);
            }
        }
    }

    private void setTemplateAdapter() {
        MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(this);
        ArrayList<MailER_TemplateInfo> templateList = dh.getTemplateListDes("USER");

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = (displayMetrics).widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 2;
        int cellHeight = (700 * cellWidth) / 507;

        myDesignAdapter = new MailER_MyDesignAdapter(this, templateList, cellWidth, cellHeight, new MailER_MyDesignAdapter.MyDesignClickListener() {
            @Override
            public void onPostClick(int template_id, int position) {
                MailER_MyDesignActivity.this.template_id = template_id;
                MailER_MyDesignActivity.this.position = position;
                if (MailER_NetworkUtils.isNetworkAvailable(MailER_MyDesignActivity.this)) {
                    startIntent();
                } else {
                    Toast.makeText(MailER_MyDesignActivity.this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEmptyAdapter() {
                txtEmptyMsg.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.setAdapter(myDesignAdapter);

        if (templateList.size() > 0) {
            txtEmptyMsg.setVisibility(View.GONE);
        } else {
            txtEmptyMsg.setVisibility(View.VISIBLE);
        }
    }


    private void startIntent() {
        Intent intent = new Intent(this, MailER_PosterEditActivity.class);
        intent.putExtra("loadUserFrame", false);
        intent.putExtra("Temp_Type", "USER");
        intent.putExtra("template_id", template_id);
        intent.putExtra("position", position);
        startActivity(intent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (myDesignAdapter != null) {
            setTemplateAdapter();
        }
    }
}