package com.festival.flyer.postermaker.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_CreateActivity;
import com.festival.flyer.postermaker.activities.MailER_MyCreationActivity;
import com.festival.flyer.postermaker.activities.MailER_PosterMainActivity;
import com.festival.flyer.postermaker.activities.MailER_SettingsActivity;
import com.festival.flyer.postermaker.activities.MailER_TemplateSelectionActivity;

public class MailER_BottomNavHelper {

    public static void setupBottomNav(Activity activity, int activeTabId) {
        View tabHome = activity.findViewById(R.id.tab_home);
        View tabExplore = activity.findViewById(R.id.tab_explore);
        View tabCreate = activity.findViewById(R.id.tab_create);
        View tabCreations = activity.findViewById(R.id.tab_creations);
        View tabSettings = activity.findViewById(R.id.tab_settings);

        if (tabHome == null) return;

        // Reset all to inactive
        setTabState(activity, R.id.ic_tab_home, R.id.txt_tab_home, false);
        setTabState(activity, R.id.ic_tab_explore, R.id.txt_tab_explore, false);
        setTabState(activity, R.id.ic_tab_creations, R.id.txt_tab_creations, false);
        setTabState(activity, R.id.ic_tab_settings, R.id.txt_tab_settings, false);

        // Set active
        if (activeTabId == R.id.tab_home) {
            setTabState(activity, R.id.ic_tab_home, R.id.txt_tab_home, true);
        } else if (activeTabId == R.id.tab_explore) {
            setTabState(activity, R.id.ic_tab_explore, R.id.txt_tab_explore, true);
        } else if (activeTabId == R.id.tab_creations) {
            setTabState(activity, R.id.ic_tab_creations, R.id.txt_tab_creations, true);
        } else if (activeTabId == R.id.tab_settings) {
            setTabState(activity, R.id.ic_tab_settings, R.id.txt_tab_settings, true);
        }

        // Set listeners
        tabHome.setOnClickListener(v -> {
            if (activeTabId != R.id.tab_home || activity instanceof com.festival.flyer.postermaker.activities.MailER_BackgroundSelectionActivity) {
                Intent intent = new Intent(activity, MailER_PosterMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        tabExplore.setOnClickListener(v -> {
            if (activeTabId != R.id.tab_explore) {
                Intent intent = new Intent(activity, MailER_TemplateSelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                if (!(activity instanceof MailER_PosterMainActivity)) activity.finish();
            }
        });

        tabCreate.setOnClickListener(v -> {
            if (activeTabId != R.id.tab_create) {
                Intent intent = new Intent(activity, MailER_CreateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                if (!(activity instanceof MailER_PosterMainActivity)) activity.finish();
            }
        });

        tabCreations.setOnClickListener(v -> {
            if (activeTabId != R.id.tab_creations) {
                Intent intent = new Intent(activity, MailER_MyCreationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                if (!(activity instanceof MailER_PosterMainActivity)) activity.finish();
            }
        });

        tabSettings.setOnClickListener(v -> {
            if (activeTabId != R.id.tab_settings) {
                Intent intent = new Intent(activity, MailER_SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                if (!(activity instanceof MailER_PosterMainActivity)) activity.finish();
            }
        });
    }

    private static void setTabState(Activity activity, int iconId, int textId, boolean isActive) {
        ImageView icon = activity.findViewById(iconId);
        TextView text = activity.findViewById(textId);
        
        if (icon == null || text == null) return;

        int color = ContextCompat.getColor(activity, isActive ? R.color.bottom_nav_active : R.color.bottom_nav_inactive);
        
        icon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        text.setTextColor(color);
        // Note: setting font family programmatically is omitted for simplicity, colors are enough
    }
}
