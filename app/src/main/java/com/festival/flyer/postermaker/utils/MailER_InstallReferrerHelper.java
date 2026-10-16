package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MailER_InstallReferrerHelper {

    private static final String TAG = "InstallReferrerHelper";

    public interface AttributionCallback {
        void onAttributionFinished(String acquisitionType);
    }

    public static void resolveAttribution(Context context, AttributionCallback callback) {
        if (context == null) {
            if (callback != null) callback.onAttributionFinished("unknown");
            return;
        }

        MailER_PreferenceClass pref = new MailER_PreferenceClass(context);

        // Test 13: Returning User - if already definitively resolved, bypass API and use cached classification
        if (pref.isAttributionResolved()) {
            String cachedType = pref.getAcquisitionType();
            Log.d(TAG, "Attribution already resolved: " + cachedType + ". Bypassing Play Referrer API.");
            Log.e("USER_TYPE_DEBUG", "=================================================");
            Log.e("USER_TYPE_DEBUG", ">>> [RETURNING USER ATTRIBUTION]");
            Log.e("USER_TYPE_DEBUG", ">>> USER TYPE (CACHED) : " + cachedType.toUpperCase());
            Log.e("USER_TYPE_DEBUG", "=================================================");
            MailER_AnalyticsUtils.setAcquisitionTypeUserProperty(context, cachedType);
            if (callback != null) {
                callback.onAttributionFinished(cachedType);
            }
            return;
        }

        final AtomicBoolean isDone = new AtomicBoolean(false);
        final Handler handler = new Handler(Looper.getMainLooper());

        // 2000ms Watchdog Timer to guarantee splash never blocks
        final Runnable watchdogRunnable = () -> {
            if (isDone.compareAndSet(false, true)) {
                Log.w(TAG, "Play Install Referrer connection timed out (>2000ms). Falling back to unknown.");
                Log.e("USER_TYPE_DEBUG", "=================================================");
                Log.e("USER_TYPE_DEBUG", ">>> [INSTALL REFERRER TIMEOUT (>2000ms)]");
                Log.e("USER_TYPE_DEBUG", ">>> USER TYPE : UNKNOWN (Treated as ORGANIC)");
                Log.e("USER_TYPE_DEBUG", "=================================================");
                pref.setAcquisitionType("unknown");
                pref.setAttributionResolved(false); // Unknown is retryable on future launch
                MailER_AnalyticsUtils.setAcquisitionTypeUserProperty(context, "unknown");
                if (callback != null) {
                    callback.onAttributionFinished("unknown");
                }
            }
        };
        handler.postDelayed(watchdogRunnable, 2000);

        try {
            final InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(context).build();
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    handler.removeCallbacks(watchdogRunnable);
                    if (isDone.get()) {
                        try { referrerClient.endConnection(); } catch (Exception ignored) {}
                        return;
                    }

                    String rawReferrer = "";
                    String type = "unknown";

                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            ReferrerDetails response = referrerClient.getInstallReferrer();
                            if (response != null) {
                                rawReferrer = response.getInstallReferrer();
                                type = classifyReferrer(rawReferrer);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error fetching install referrer: " + e.getMessage());
                            type = "unknown";
                        }
                    } else {
                        Log.w(TAG, "Install Referrer response code: " + responseCode);
                        type = "unknown";
                    }

                    try { referrerClient.endConnection(); } catch (Exception ignored) {}

                    if (isDone.compareAndSet(false, true)) {
                        pref.setAcquisitionType(type);
                        pref.setAttributionRawReferrer(rawReferrer != null ? rawReferrer : "");

                        boolean resolved = "paid".equals(type) || "organic".equals(type);
                        pref.setAttributionResolved(resolved);

                        MailER_AnalyticsUtils.setAcquisitionTypeUserProperty(context, type);
                        MailER_AnalyticsUtils.logAttributionEvent(context, type, rawReferrer);

                        Log.e("USER_TYPE_DEBUG", "=================================================");
                        Log.e("USER_TYPE_DEBUG", ">>> [PLAY INSTALL REFERRER DETECTED]");
                        Log.e("USER_TYPE_DEBUG", ">>> RAW REFERRER : " + (rawReferrer != null && !rawReferrer.isEmpty() ? rawReferrer : "(EMPTY)"));
                        Log.e("USER_TYPE_DEBUG", ">>> USER TYPE    : " + type.toUpperCase() + (type.equals("unknown") ? " (Treated as ORGANIC)" : ""));
                        Log.e("USER_TYPE_DEBUG", ">>> RESOLVED     : " + resolved);
                        Log.e("USER_TYPE_DEBUG", "=================================================");

                        Log.d(TAG, "Attribution classified as: " + type + " (resolved=" + resolved + ")");
                        if (callback != null) {
                            callback.onAttributionFinished(type);
                        }
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    handler.removeCallbacks(watchdogRunnable);
                    if (isDone.compareAndSet(false, true)) {
                        Log.w(TAG, "Install Referrer service disconnected.");
                        Log.e("USER_TYPE_DEBUG", "=================================================");
                        Log.e("USER_TYPE_DEBUG", ">>> [INSTALL REFERRER DISCONNECTED]");
                        Log.e("USER_TYPE_DEBUG", ">>> USER TYPE : UNKNOWN (Treated as ORGANIC)");
                        Log.e("USER_TYPE_DEBUG", "=================================================");
                        pref.setAcquisitionType("unknown");
                        pref.setAttributionResolved(false);
                        MailER_AnalyticsUtils.setAcquisitionTypeUserProperty(context, "unknown");
                        if (callback != null) {
                            callback.onAttributionFinished("unknown");
                        }
                    }
                }
            });
        } catch (Exception e) {
            handler.removeCallbacks(watchdogRunnable);
            if (isDone.compareAndSet(false, true)) {
                Log.e(TAG, "Exception initializing InstallReferrerClient: " + e.getMessage());
                pref.setAcquisitionType("unknown");
                pref.setAttributionResolved(false);
                MailER_AnalyticsUtils.setAcquisitionTypeUserProperty(context, "unknown");
                if (callback != null) {
                    callback.onAttributionFinished("unknown");
                }
            }
        }
    }

    public static String classifyReferrer(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "unknown";
        }

        String decoded = raw.trim();
        try {
            // Support URL-encoded strings (e.g. utm_source%3Dgoogle-play%26utm_medium%3Dorganic)
            decoded = URLDecoder.decode(decoded, "UTF-8");
            // If still contains url encoding, decode second pass
            if (decoded.contains("%3D") || decoded.contains("%26") || decoded.contains("%20")) {
                decoded = URLDecoder.decode(decoded, "UTF-8");
            }
        } catch (Exception ignored) {}

        Map<String, String> params = new HashMap<>();
        String[] pairs = decoded.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length()) {
                String key = pair.substring(0, idx).trim().toLowerCase();
                String val = pair.substring(idx + 1).trim();
                try {
                    val = URLDecoder.decode(val, "UTF-8");
                } catch (Exception ignored) {}
                params.put(key, val);
            } else if (idx > 0 && idx == pair.length() - 1) {
                String key = pair.substring(0, idx).trim().toLowerCase();
                params.put(key, "");
            }
        }

        // --- 1. PAID CLASSIFICATION ---
        // A. gclid parameter is present and non-empty
        if (params.containsKey("gclid")) {
            String gclid = params.get("gclid");
            if (gclid != null && !gclid.trim().isEmpty()) {
                return "paid";
            }
        }
        if (decoded.contains("gclid=")) {
            int gIdx = decoded.indexOf("gclid=");
            int end = decoded.indexOf("&", gIdx);
            String val = (end == -1) ? decoded.substring(gIdx + 6) : decoded.substring(gIdx + 6, end);
            if (!val.trim().isEmpty()) {
                return "paid";
            }
        }

        // B. gad_source=1
        if ("1".equals(params.get("gad_source"))) {
            return "paid";
        }

        // C. utm_source contains google, adwords, or gads AND utm_medium matches paid media
        String utmSource = params.get("utm_source");
        String utmMedium = params.get("utm_medium");
        if (utmSource != null) utmSource = utmSource.toLowerCase().trim();
        if (utmMedium != null) utmMedium = utmMedium.toLowerCase().trim();

        if (utmSource != null && (utmSource.contains("google") || utmSource.contains("adwords") || utmSource.contains("gads"))) {
            if (utmMedium != null && isPaidMedium(utmMedium)) {
                return "paid";
            }
        }

        // --- 2. ORGANIC CLASSIFICATION ---
        // utm_source equals google-play or play.google.com AND utm_medium equals organic
        // and no gclid or paid media flags present
        if (("google-play".equals(utmSource) || "play.google.com".equals(utmSource)) && "organic".equals(utmMedium)) {
            return "organic";
        }

        // --- 3. UNKNOWN CLASSIFICATION ---
        return "unknown";
    }

    private static boolean isPaidMedium(String medium) {
        if (medium == null) return false;
        String m = medium.toLowerCase().trim();
        return m.equals("cpc") || m.equals("cpm") || m.equals("paid") ||
                m.equals("search") || m.equals("display") || m.equals("banner") ||
                m.equals("cpa") || m.equals("cpv") || m.equals("ad");
    }
}
