package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class MailER_PreferenceClass {

    private final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private final SharedPreferences prefs;

    // Fallback Ad IDs — used ONLY when Firebase has never loaded successfully
    private static final Map<String, String> FALLBACK_AD_IDS = new HashMap<>();
    static {
        FALLBACK_AD_IDS.put("AppOpenID", "ca-app-pub-5706123402805812/2933969658");
        FALLBACK_AD_IDS.put("BannerAdunitID", "ca-app-pub-5706123402805812/4821766396");
        FALLBACK_AD_IDS.put("CollapsibleBannerID", "ca-app-pub-5706123402805812/2657127464");
        FALLBACK_AD_IDS.put("interstitalAdunitID", "ca-app-pub-5706123402805812/1594372797");
        FALLBACK_AD_IDS.put("InterstitalAdunitID", "ca-app-pub-5706123402805812/1594372797");
        FALLBACK_AD_IDS.put("NativeUnitID", "ca-app-pub-5706123402805812/8186296336");
        FALLBACK_AD_IDS.put("RewardVideoUnitID", "ca-app-pub-5706123402805812/6490071289");
        FALLBACK_AD_IDS.put("google_Rw_ID", "ca-app-pub-5706123402805812/6490071289");
        FALLBACK_AD_IDS.put("editor_reward_ad_id", "ca-app-pub-5706123402805812/6490071289");
        FALLBACK_AD_IDS.put("editor_download_ad_id", "ca-app-pub-5706123402805812/6490071289");
        FALLBACK_AD_IDS.put("editor_undo_redo_ad_id", "ca-app-pub-5706123402805812/6490071289");
    }

    public static String getMasterConfigJson(com.google.firebase.remoteconfig.FirebaseRemoteConfig rc) {
        if (rc == null) return "";
        try {
            if (rc.getValue("postermaker").getSource() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                return rc.getString("postermaker");
            }
            if (rc.getValue("app_ad_config").getSource() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                return rc.getString("app_ad_config");
            }
            String pm = rc.getString("postermaker");
            if (pm != null && !pm.trim().isEmpty() && !pm.equals("{}")) {
                return pm;
            }
            return rc.getString("app_ad_config");
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isBannerShimmerOnFail(Context context) {
        if (context == null) return true;
        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            if (rc != null) {
                String masterJson = getMasterConfigJson(rc);
                if (masterJson != null && !masterJson.isEmpty()) {
                    org.json.JSONObject root = new org.json.JSONObject(masterJson);
                    if (root.has("banner_show_shimmer_on_fail")) {
                        return root.optBoolean("banner_show_shimmer_on_fail", true);
                    }
                    if (root.has("banner_shimmer_on_fail")) {
                        return root.optBoolean("banner_shimmer_on_fail", true);
                    }
                    if (root.has("show_banner_shimmer_on_fail")) {
                        return root.optBoolean("show_banner_shimmer_on_fail", true);
                    }
                    org.json.JSONObject bObj = root.optJSONObject("banner");
                    if (bObj == null) bObj = root.optJSONObject("banner_ad");
                    if (bObj != null) {
                        if (bObj.has("show_shimmer_on_fail")) {
                            return bObj.optBoolean("show_shimmer_on_fail", true);
                        }
                        if (bObj.has("shimmer_on_fail")) {
                            return bObj.optBoolean("shimmer_on_fail", true);
                        }
                        if (bObj.has("keep_loader_on_fail")) {
                            return bObj.optBoolean("keep_loader_on_fail", true);
                        }
                    }
                }
                if (rc.getKeysByPrefix("").contains("banner_show_shimmer_on_fail")) {
                    return rc.getBoolean("banner_show_shimmer_on_fail");
                }
                if (rc.getKeysByPrefix("").contains("banner_shimmer_on_fail")) {
                    return rc.getBoolean("banner_shimmer_on_fail");
                }
            }
        } catch (Exception ignored) {}
        MailER_PreferenceClass pref = new MailER_PreferenceClass(context);
        return pref.getBoolean("banner_show_shimmer_on_fail", true);
    }

    public static boolean isNativeShimmerOnFail(Context context) {
        if (context == null) return true;
        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            if (rc != null) {
                String masterJson = getMasterConfigJson(rc);
                if (masterJson != null && !masterJson.isEmpty()) {
                    org.json.JSONObject root = new org.json.JSONObject(masterJson);
                    if (root.has("native_show_shimmer_on_fail")) {
                        return root.optBoolean("native_show_shimmer_on_fail", true);
                    }
                    if (root.has("native_shimmer_on_fail")) {
                        return root.optBoolean("native_shimmer_on_fail", true);
                    }
                    if (root.has("show_native_shimmer_on_fail")) {
                        return root.optBoolean("show_native_shimmer_on_fail", true);
                    }
                    org.json.JSONObject nObj = root.optJSONObject("native");
                    if (nObj == null) nObj = root.optJSONObject("native_ad");
                    if (nObj != null) {
                        if (nObj.has("show_shimmer_on_fail")) {
                            return nObj.optBoolean("show_shimmer_on_fail", true);
                        }
                        if (nObj.has("shimmer_on_fail")) {
                            return nObj.optBoolean("shimmer_on_fail", true);
                        }
                        if (nObj.has("keep_loader_on_fail")) {
                            return nObj.optBoolean("keep_loader_on_fail", true);
                        }
                    }
                }
                if (rc.getKeysByPrefix("").contains("native_show_shimmer_on_fail")) {
                    return rc.getBoolean("native_show_shimmer_on_fail");
                }
                if (rc.getKeysByPrefix("").contains("native_shimmer_on_fail")) {
                    return rc.getBoolean("native_shimmer_on_fail");
                }
            }
        } catch (Exception ignored) {}
        MailER_PreferenceClass pref = new MailER_PreferenceClass(context);
        return pref.getBoolean("native_show_shimmer_on_fail", true);
    }

    public static String getSafeAdUnitId(String requestedId, String type) {
        if (com.festival.flyer.postermaker.BuildConfig.DEBUG) {
            if (type != null) {
                String t = type.toLowerCase();
                if (t.contains("appopen")) return "ca-app-pub-3940256099942544/9257395921";
                if (t.contains("inter")) return "ca-app-pub-3940256099942544/1033173712";
                if (t.contains("banner") || t.contains("collapsible")) return "ca-app-pub-3940256099942544/6300978111";
                if (t.contains("native")) return "ca-app-pub-3940256099942544/2247696110";
                if (t.contains("reward")) return "ca-app-pub-3940256099942544/5224354917";
            }
            return "ca-app-pub-3940256099942544/6300978111";
        }
        return (requestedId != null) ? requestedId.trim() : "";
    }

    public MailER_PreferenceClass(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isFirstTimeLaunch() {
        return prefs.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        prefs.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply();
    }

    public void setRateSubmited(String type, Boolean value) {
        prefs.edit().putBoolean(type, value).apply();
    }

    public Boolean getRateSubmited(String type) {
        if (prefs != null) {
            return prefs.getBoolean(type, false);
        }
        return false;
    }

    public void setBoolean(String key, boolean value) {
        if (prefs != null) {
            prefs.edit().putBoolean(key, value).apply();
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (prefs != null) {
            return prefs.getBoolean(key, defaultValue);
        }
        return defaultValue;
    }

    public void setDataType(String type, String value) {
        prefs.edit().putString(type, value).apply();
    }

    public String getDataType(String type) {
        return prefs.getString(type, "");
    }

    public String getDataType(String type, String defaultValue) {
        return prefs.getString(type, defaultValue);
    }

    public void setAdsId(String type, String value) {
        prefs.edit().putString(type, value).apply();
    }

    /**
     * Mark that Firebase ad data has been loaded at least once.
     * After this, fallback IDs will never be used — Firebase is in full control.
     */
    public void setFirebaseAdsLoaded() {
        prefs.edit().putBoolean("firebase_ads_loaded", true).apply();
    }

    public boolean isFirebaseAdsLoaded() {
        return prefs.getBoolean("firebase_ads_loaded", false);
    }

    /**
     * Get Ad ID with smart fallback:
     * - If Firebase has loaded → return Firebase value (even if empty = ad disabled)
     * - If Firebase has NEVER loaded → return fallback ID (so ads still work on first launch / no internet)
     */
    public String getAdsId(String type) {
        // Force Test Ads in Debug Mode
        if (com.festival.flyer.postermaker.BuildConfig.DEBUG) {
            if (type.equalsIgnoreCase("interstitalAdunitID") || type.equalsIgnoreCase("InterstitalAdunitID") || type.equalsIgnoreCase("OnboardingInterAdID")) return "ca-app-pub-3940256099942544/1033173712";
            if (type.equalsIgnoreCase("AppOpenID")) return "ca-app-pub-3940256099942544/9257395921";
            if (type.equalsIgnoreCase("BannerAdunitID") || type.equalsIgnoreCase("CollapsibleBannerID") || type.equalsIgnoreCase("BGSelectScreen_BannerAD") || type.equalsIgnoreCase("ReadymadePoste_BannerAD") || type.equalsIgnoreCase("EditScreenBannerAD")) return "ca-app-pub-3940256099942544/6300978111";
            if (type.equalsIgnoreCase("NativeUnitID") || type.equalsIgnoreCase("ExitDialog_NativeAD")) return "ca-app-pub-3940256099942544/2247696110";
            if (type.equalsIgnoreCase("RewardVideoUnitID") || type.equalsIgnoreCase("google_Rw_ID") || type.equalsIgnoreCase("editor_reward_ad_id") || type.equalsIgnoreCase("editor_download_ad_id") || type.equalsIgnoreCase("editor_undo_redo_ad_id")) return "ca-app-pub-3940256099942544/5224354917";
        }

        if (prefs != null) {
            String firebaseId = prefs.getString(type, "");
            if (firebaseId != null && !firebaseId.isEmpty()) {
                return firebaseId;
            }
            // Firebase ID is empty/null — check if Firebase ever loaded
            if (!isFirebaseAdsLoaded()) {
                // Firebase never loaded — use fallback
                String fallback = FALLBACK_AD_IDS.get(type);
                if (fallback != null) {
                    android.util.Log.d("[ADS_LOG]", "⚡ Using FALLBACK Ad ID for: " + type);
                    return fallback;
                }
            }
            // Firebase loaded but ID is empty — ad is disabled by Firebase
            return "";
        }
        return "";
    }

    public void setAdsStatus(String type, int value) {
        prefs.edit().putInt(type, value).apply();
    }

    public int getAdsStatus(String type) {
        return prefs.getInt(type, 1);
    }

    public void setInt(String type, int value) {
        prefs.edit().putInt(type, value).apply();
    }

    public int getInt(String type) {
        return prefs.getInt(type, 0);
    }

    public int getInt(String type, int def) {
        return prefs.getInt(type, def);
    }

    public void setFirstDate(String firstDate) {
        prefs.edit().putString("firstDate", firstDate).apply();
    }

    public String getFirstDate() {
        return prefs.getString("firstDate", null);
    }

    public void setDecryptionType(int type) {
        prefs.edit().putInt("decryptionType", type).apply();
    }

    public int getDecryptionType() {
        return prefs.getInt("decryptionType", 0);
    }
}
