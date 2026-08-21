package com.festival.flyer.postermaker.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;

public class MailER_PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_privacy_policy);

        ShimmerFrameLayout shimmer_view_container = findViewById(R.id.shimmer_view_container);
        RelativeLayout rl_ad = this.findViewById(R.id.rl_ad);
        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
            MailER_LoadAds.loadAdmobBannerAd(this, rl_ad, shimmer_view_container);

        }

        WebView webView = findViewById(R.id.webview_privacy);
        webView.loadUrl("https://cygnux.in/application-privacy-policy/poster-maker-policy.html");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });


    }


}