package com.festival.flyer.postermaker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import com.festival.flyer.postermaker.utils.MailER_BottomNavHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.utils.MailER_ShareUtils;

public class MailER_SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.spawner_activity_settings);
        MailER_BottomNavHelper.setupBottomNav(this, R.id.tab_settings);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.home_bg));
        }

        setupSettingsOptions();

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }

    private void setupSettingsOptions() {
        findViewById(R.id.ll_liked_backgrounds).setOnClickListener(v -> {
            startActivity(new Intent(this, MailER_LikedBackgroundsActivity.class));
        });

        findViewById(R.id.ll_liked_templates).setOnClickListener(v -> {
            startActivity(new Intent(this, MailER_LikedTemplatesActivity.class));
        });

        findViewById(R.id.ll_share_app).setOnClickListener(v -> {
            MailER_ShareUtils.onShare(this);
        });

        findViewById(R.id.ll_rate_app).setOnClickListener(v -> {
            MailER_ShareUtils.rateUs(this);
        });

        findViewById(R.id.ll_instagram).setOnClickListener(v -> {
            MailER_ShareUtils.onInstagram(this);
        });

        findViewById(R.id.ll_privacy_policy).setOnClickListener(v -> {
            startActivity(new Intent(this, MailER_PrivacyPolicyActivity.class));
        });

        

        findViewById(R.id.ll_help).setOnClickListener(v -> {
            // Placeholder for Help
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@cygnux.in", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support - Poster Maker App");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });
    }

    private void nextActivity(Class<? extends Activity> activity) {
        MyApplication.showInterstitialAd(this, () -> {
            Intent intent = new Intent(this, activity);
            if (activity == MailER_BackgroundSelectionActivity.class) {
                intent.putExtra("mode", "bg");
            }
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }
}
