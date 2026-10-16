package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * MailER_AppCacheManager
 * Multi-layer caching system (Memory -> SharedPreferences -> Disk File)
 * with prefetch capability to guarantee 0-second instant screen rendering
 * for Hero Banners, Trending Festivals, and templates.
 */
public class MailER_AppCacheManager {

    private static final String TAG = "AppCacheManager";
    private static final String PREF_NAME = "api_cache_prefs";

    public static final String KEY_HERO_BANNERS = "hero_banners";
    public static final String KEY_TRENDING_FESTIVALS = "trending_festivals";
    public static final String KEY_TEMPLATES = "templates_data";
    public static final String KEY_BACKGROUNDS = "backgrounds_data";

    private static final Map<String, String> memoryCache = new ConcurrentHashMap<>();

    public static synchronized void saveCache(Context context, String key, String jsonContent) {
        if (context == null || key == null || jsonContent == null || jsonContent.trim().isEmpty()) {
            return;
        }

        // 1. Fast in-memory cache
        memoryCache.put(key, jsonContent);

        // 2. Persistent SharedPreferences for instant synchronous retrieval on app open
        try {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString("cache_" + key, jsonContent).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving SharedPreferences cache for " + key + ": " + e.getMessage());
        }

        // 3. Disk file cache (asynchronous to avoid blocking UI)
        final Context appContext = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File cacheDir = new File(appContext.getFilesDir(), "api_cache");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                File cacheFile = new File(cacheDir, "cache_" + key + ".json");
                try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                    fos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
                    fos.flush();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving disk cache for " + key + ": " + e.getMessage());
            }
        });
    }

    public static synchronized String getCache(Context context, String key) {
        if (context == null || key == null) {
            return null;
        }

        // 1. Check in-memory RAM cache (0ms)
        String memData = memoryCache.get(key);
        if (memData != null && !memData.trim().isEmpty()) {
            return memData;
        }

        // 2. Check SharedPreferences (instant synchronous memory-backed storage)
        try {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String prefData = prefs.getString("cache_" + key, null);
            if (prefData != null && !prefData.trim().isEmpty()) {
                memoryCache.put(key, prefData);
                return prefData;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading SharedPreferences cache for " + key + ": " + e.getMessage());
        }

        // 3. Fallback to disk file cache
        try {
            File cacheDir = new File(context.getApplicationContext().getFilesDir(), "api_cache");
            File cacheFile = new File(cacheDir, "cache_" + key + ".json");
            if (cacheFile.exists() && cacheFile.length() > 0) {
                StringBuilder sb = new StringBuilder();
                try (FileInputStream fis = new FileInputStream(cacheFile);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                String diskData = sb.toString();
                if (!diskData.trim().isEmpty()) {
                    memoryCache.put(key, diskData);
                    return diskData;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading disk cache for " + key + ": " + e.getMessage());
        }

        return null;
    }

    // Convenient Helpers
    public static void saveHeroBanners(Context context, String json) {
        saveCache(context, KEY_HERO_BANNERS, json);
        preloadImagesFromJson(context, json, "banner_image");
    }

    public static String getHeroBanners(Context context) {
        return getCache(context, KEY_HERO_BANNERS);
    }

    public static void saveTrendingFestivals(Context context, String json) {
        saveCache(context, KEY_TRENDING_FESTIVALS, json);
        preloadImagesFromJson(context, json, "banner_image");
    }

    public static String getTrendingFestivals(Context context) {
        return getCache(context, KEY_TRENDING_FESTIVALS);
    }

    public static void saveTemplates(Context context, String json) {
        saveCache(context, KEY_TEMPLATES, json);
        preloadTemplateImages(context, json);
    }

    public static String getTemplates(Context context) {
        return getCache(context, KEY_TEMPLATES);
    }

    public static void saveBackgrounds(Context context, String json) {
        saveCache(context, KEY_BACKGROUNDS, json);
        preloadBackgroundImages(context, json);
    }

    public static String getBackgrounds(Context context) {
        return getCache(context, KEY_BACKGROUNDS);
    }

    /**
     * Prefetch Hero Banners, Trending Festivals, Templates and Backgrounds in background
     * Can be invoked from Splash or AppLoading screen so cache is warm before screens open!
     */
    public static void prefetchHomeData(Context context) {
        if (context == null || !MailER_NetworkUtils.isNetworkAvailable(context)) return;

        final Context appContext = context.getApplicationContext();
        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(appContext);
        final String apiKey = preferenceClass.getDataType("field_0");

        // 1. Prefetch Hero Banners
        String heroUrl = "https://cygnux.in/postermaker/api/v1/poster/hero";
        StringRequest heroReq = new StringRequest(Request.Method.POST, heroUrl, response -> {
            if (response != null && !response.trim().isEmpty()) {
                saveHeroBanners(appContext, response);
                Log.d(TAG, "Prefetched and saved Hero Banners cache successfully");
            }
        }, error -> Log.e(TAG, "Hero prefetch error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                params.put("key", apiKey != null ? apiKey : "");
                return params;
            }
        };

        // 2. Prefetch Trending Festivals
        String trendingUrl = "https://cygnux.in/postermaker/api/v1/poster/trending";
        StringRequest trendingReq = new StringRequest(Request.Method.POST, trendingUrl, response -> {
            if (response != null && !response.trim().isEmpty()) {
                saveTrendingFestivals(appContext, response);
                Log.d(TAG, "Prefetched and saved Trending Festivals cache successfully");
            }
        }, error -> Log.e(TAG, "Trending prefetch error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                params.put("key", apiKey != null ? apiKey : "");
                return params;
            }
        };

        // 3. Prefetch Templates & Backgrounds (if endpoints are configured)
        String baseUrl = preferenceClass.getDataType("field_51");
        String apiPath = preferenceClass.getDataType("field_34");
        String templateEndpoint = preferenceClass.getDataType("field_35");
        String bgEndpoint = preferenceClass.getDataType("field_38");

        try {
            Volley.newRequestQueue(appContext).add(heroReq);
            Volley.newRequestQueue(appContext).add(trendingReq);

            if (baseUrl != null && !baseUrl.isEmpty() && apiPath != null && !apiPath.isEmpty()) {
                if (templateEndpoint != null && !templateEndpoint.isEmpty()) {
                    String templateUrl = baseUrl + apiPath + templateEndpoint;
                    StringRequest templateReq = new StringRequest(Request.Method.POST, templateUrl, response -> {
                        if (response != null && !response.trim().isEmpty()) {
                            saveTemplates(appContext, response);
                            Log.d(TAG, "Prefetched and saved Templates cache successfully");
                        }
                    }, error -> Log.e(TAG, "Templates prefetch error: " + error.getMessage())) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> postMap = new HashMap<>();
                            postMap.put(preferenceClass.getDataType("field_47"), apiKey != null ? apiKey : "");
                            postMap.put(preferenceClass.getDataType("field_48"), "1");
                            postMap.put(preferenceClass.getDataType("field_46"), "0");
                            postMap.put(preferenceClass.getDataType("field_8"), "0");
                            return postMap;
                        }
                    };
                    Volley.newRequestQueue(appContext).add(templateReq);
                }

                if (bgEndpoint != null && !bgEndpoint.isEmpty()) {
                    String bgUrl = baseUrl + apiPath + bgEndpoint;
                    StringRequest bgReq = new StringRequest(Request.Method.POST, bgUrl, response -> {
                        if (response != null && !response.trim().isEmpty()) {
                            saveBackgrounds(appContext, response);
                            Log.d(TAG, "Prefetched and saved Backgrounds cache successfully");
                        }
                    }, error -> Log.e(TAG, "Backgrounds prefetch error: " + error.getMessage())) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> postMap = new HashMap<>();
                            postMap.put(preferenceClass.getDataType("field_47"), apiKey != null ? apiKey : "");
                            postMap.put(preferenceClass.getDataType("field_48"), "1");
                            return postMap;
                        }
                    };
                    Volley.newRequestQueue(appContext).add(bgReq);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error dispatching prefetch requests: " + e.getMessage());
        }
    }

    public static void preloadImagesFromJson(Context context, String jsonResponse, String imageKey) {
        if (context == null || jsonResponse == null || imageKey == null) return;
        final Context appContext = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                org.json.JSONObject obj = new org.json.JSONObject(jsonResponse);
                if (obj.optString("error", "").equals("1")) {
                    org.json.JSONArray data = obj.optJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            org.json.JSONObject item = data.getJSONObject(i);
                            String imgUrl = item.optString(imageKey, "");
                            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                                com.bumptech.glide.Glide.with(appContext)
                                        .load(imgUrl)
                                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                        .preload();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error preloading images: " + e.getMessage());
            }
        });
    }

    public static void preloadTemplateImages(Context context, String jsonResponse) {
        if (context == null || jsonResponse == null) return;
        final Context appContext = context.getApplicationContext();
        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(appContext);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);
                int error = jsonObject.optInt(preferenceClass.getDataType("field_3"), 0);
                if (error == 1) {
                    org.json.JSONArray categories = jsonObject.optJSONArray(preferenceClass.getDataType("field_4"));
                    if (categories != null) {
                        String field7Key = preferenceClass.getDataType("field_7");
                        String field11Key = preferenceClass.getDataType("field_11");
                        for (int i = 0; i < categories.length(); i++) {
                            org.json.JSONObject cat = categories.getJSONObject(i);
                            org.json.JSONArray posters = cat.optJSONArray(field7Key);
                            if (posters != null) {
                                int limit = Math.min(posters.length(), 6);
                                for (int j = 0; j < limit; j++) {
                                    org.json.JSONObject poster = posters.getJSONObject(j);
                                    String imgUrl = poster.optString("thumbnail_image", "");
                                    if (imgUrl.isEmpty()) imgUrl = poster.optString("thumbnail", "");
                                    if (imgUrl.isEmpty() && field11Key != null) imgUrl = poster.optString(field11Key, "");
                                    if (!imgUrl.trim().isEmpty()) {
                                        com.bumptech.glide.Glide.with(appContext)
                                                .load(imgUrl)
                                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                                .preload();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error preloading template images: " + e.getMessage());
            }
        });
    }

    public static void preloadBackgroundImages(Context context, String jsonResponse) {
        if (context == null || jsonResponse == null) return;
        final Context appContext = context.getApplicationContext();
        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(appContext);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);
                org.json.JSONArray categories = jsonObject.optJSONArray(preferenceClass.getDataType("field_39"));
                if (categories != null) {
                    String field11Key = preferenceClass.getDataType("field_11");
                    for (int i = 0; i < categories.length(); i++) {
                        org.json.JSONObject cat = categories.getJSONObject(i);
                        org.json.JSONArray bgs = cat.optJSONArray(preferenceClass.getDataType("field_7"));
                        if (bgs != null) {
                            int limit = Math.min(bgs.length(), 6);
                            for (int j = 0; j < limit; j++) {
                                org.json.JSONObject bg = bgs.getJSONObject(j);
                                String imgUrl = bg.optString("thumbnail_image", "");
                                if (imgUrl.isEmpty()) imgUrl = bg.optString("thumbnail", "");
                                if (imgUrl.isEmpty() && field11Key != null) imgUrl = bg.optString(field11Key, "");
                                if (!imgUrl.trim().isEmpty()) {
                                    com.bumptech.glide.Glide.with(appContext)
                                            .load(imgUrl)
                                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                            .preload();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error preloading background images: " + e.getMessage());
            }
        });
    }
}
