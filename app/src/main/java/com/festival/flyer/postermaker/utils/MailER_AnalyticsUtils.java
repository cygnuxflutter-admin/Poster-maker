package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MailER_AnalyticsUtils {

    public static void logButtonClick(Context context, String buttonName, String screenName) {
        if (context == null) return;
        Bundle bundle = new Bundle();
        bundle.putString("button_name", buttonName);
        bundle.putString("screen_name", screenName);
        FirebaseAnalytics.getInstance(context).logEvent("button_click", bundle);
    }

    public static void logPosterJourney(Context context, String eventName, String templateId, String templateCategory, String screenName) {
        if (context == null) return;
        Bundle bundle = new Bundle();
        if (templateId != null && !templateId.isEmpty()) {
            bundle.putString("template_id", templateId);
        }
        if (templateCategory != null && !templateCategory.isEmpty()) {
            bundle.putString("template_category", templateCategory);
        }
        if (screenName != null && !screenName.isEmpty()) {
            bundle.putString("screen_name", screenName);
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
    }

    public static void logPosterShare(Context context, String shareType, String screenName) {
        if (context == null) return;
        Bundle bundle = new Bundle();
        bundle.putString("share_type", shareType);
        bundle.putString("screen_name", screenName);
        FirebaseAnalytics.getInstance(context).logEvent("poster_share", bundle);
    }

    public static void logAdEvent(Context context, String eventName, String adType, String adUnit, String screenName, String errorMessage) {
        if (context == null) return;
        Bundle bundle = new Bundle();
        bundle.putString("ad_type", adType);
        bundle.putString("ad_unit", adUnit != null ? adUnit : "unknown");
        if (screenName != null) {
            bundle.putString("screen_name", screenName);
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
            // Trim error message to fit Firebase limits if necessary
            if (errorMessage.length() > 100) {
                errorMessage = errorMessage.substring(0, 97) + "...";
            }
            bundle.putString("error_message", errorMessage);
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
    }

    public static void setAcquisitionTypeUserProperty(Context context, String acquisitionType) {
        if (context == null || acquisitionType == null) return;
        try {
            FirebaseAnalytics.getInstance(context).setUserProperty("acquisition_type", acquisitionType);
            FirebaseAnalytics.getInstance(context).setUserProperty("user_type", acquisitionType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logAttributionEvent(Context context, String acquisitionType, String rawReferrer) {
        if (context == null) return;
        try {
            Bundle bundle = new Bundle();
            bundle.putString("acquisition_type", acquisitionType != null ? acquisitionType : "unknown");
            bundle.putString("user_type", acquisitionType != null ? acquisitionType : "unknown");
            if (rawReferrer != null && !rawReferrer.isEmpty()) {
                if (rawReferrer.length() > 100) {
                    rawReferrer = rawReferrer.substring(0, 97) + "...";
                }
                bundle.putString("raw_referrer", rawReferrer);
            }
            FirebaseAnalytics.getInstance(context).logEvent("app_install_attribution", bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
