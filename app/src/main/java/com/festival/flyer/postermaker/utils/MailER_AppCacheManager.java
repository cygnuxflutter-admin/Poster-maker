package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * MailER_AppCacheManager
 * Handles offline / disk caching of API JSON responses (Hero, Trending, Templates, Backgrounds)
 * to achieve 0-second instant screen rendering (Cache-First / Stale-While-Revalidate).
 */
public class MailER_AppCacheManager {

    private static final String TAG = "AppCacheManager";

    public static final String KEY_HERO_BANNERS = "hero_banners";
    public static final String KEY_TRENDING_FESTIVALS = "trending_festivals";
    public static final String KEY_TEMPLATES = "templates_data";
    public static final String KEY_BACKGROUNDS = "backgrounds_data";

    public static synchronized void saveCache(Context context, String key, String jsonContent) {
        if (context == null || key == null || jsonContent == null || jsonContent.trim().isEmpty()) {
            return;
        }
        try {
            File cacheDir = new File(context.getFilesDir(), "api_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File cacheFile = new File(cacheDir, "cache_" + key + ".json");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            Log.d(TAG, "Saved cache for key: " + key + " (Size: " + jsonContent.length() + " chars)");
        } catch (Exception e) {
            Log.e(TAG, "Error saving cache for " + key + ": " + e.getMessage());
        }
    }

    public static synchronized String getCache(Context context, String key) {
        if (context == null || key == null) {
            return null;
        }
        try {
            File cacheDir = new File(context.getFilesDir(), "api_cache");
            File cacheFile = new File(cacheDir, "cache_" + key + ".json");
            if (!cacheFile.exists() || cacheFile.length() == 0) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (FileInputStream fis = new FileInputStream(cacheFile);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String result = sb.toString();
            if (!result.trim().isEmpty()) {
                Log.d(TAG, "Loaded cache for key: " + key + " (Size: " + result.length() + " chars)");
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading cache for " + key + ": " + e.getMessage());
        }
        return null;
    }

    // Convenient Helpers
    public static void saveHeroBanners(Context context, String json) {
        saveCache(context, KEY_HERO_BANNERS, json);
    }

    public static String getHeroBanners(Context context) {
        return getCache(context, KEY_HERO_BANNERS);
    }

    public static void saveTrendingFestivals(Context context, String json) {
        saveCache(context, KEY_TRENDING_FESTIVALS, json);
    }

    public static String getTrendingFestivals(Context context) {
        return getCache(context, KEY_TRENDING_FESTIVALS);
    }

    public static void saveTemplates(Context context, String json) {
        saveCache(context, KEY_TEMPLATES, json);
    }

    public static String getTemplates(Context context) {
        return getCache(context, KEY_TEMPLATES);
    }

    public static void saveBackgrounds(Context context, String json) {
        saveCache(context, KEY_BACKGROUNDS, json);
    }

    public static String getBackgrounds(Context context) {
        return getCache(context, KEY_BACKGROUNDS);
    }
}
