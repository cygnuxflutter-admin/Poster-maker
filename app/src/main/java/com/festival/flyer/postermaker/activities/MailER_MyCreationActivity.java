package com.festival.flyer.postermaker.activities;

import com.festival.flyer.postermaker.utils.MailER_BottomNavHelper;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_MyCreationAdapter;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.utils.MailER_SignatureFileUtils;

import static com.festival.flyer.postermaker.adManager.MailER_LoadAds.loadAdmobBannerAd;

public class MailER_MyCreationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtEmptyMsg;
    private MailER_MyCreationAdapter myCreationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_my_creation);
        MailER_BottomNavHelper.setupBottomNav(this, R.id.tab_creations);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View statusBarSpacer = findViewById(R.id.status_bar_spacer);
        ViewCompat.setOnApplyWindowInsetsListener(statusBarSpacer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.getLayoutParams().height = systemBars.top;
            v.requestLayout();
            return insets;
        });

        View btm = findViewById(R.id.btm);
        ViewCompat.setOnApplyWindowInsetsListener(btm, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

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
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = (displayMetrics).widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 2;
        int cellHeight = (700 * cellWidth) / 507;

        myCreationAdapter = new MailER_MyCreationAdapter(this, new MailER_SignatureFileUtils(getApplicationContext()).getFilePaths(), cellWidth, cellHeight, () -> txtEmptyMsg.setVisibility(View.VISIBLE));
        recyclerView.setAdapter(myCreationAdapter);

        if (myCreationAdapter.getItemCount() > 0) {
            txtEmptyMsg.setVisibility(View.GONE);
        } else {
            txtEmptyMsg.setVisibility(View.VISIBLE);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (myCreationAdapter != null) {
            setTemplateAdapter();
        }
    }
}