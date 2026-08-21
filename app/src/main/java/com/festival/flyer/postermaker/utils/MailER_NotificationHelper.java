package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.festival.flyer.postermaker.models.MailER_NotificationModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MailER_NotificationHelper {
    private static final String PREF_NAME = "PosterMakerNotifications";
    private static final String KEY_LIST = "notif_list";

    public static void saveNotification(Context context, MailER_NotificationModel notification) {
        List<MailER_NotificationModel> currentList = getNotifications(context);
        currentList.add(0, notification); // Add to top

        // Keep only last 50
        if (currentList.size() > 50) {
            currentList = currentList.subList(0, 50);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, new Gson().toJson(currentList)).apply();
    }

    public static List<MailER_NotificationModel> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<MailER_NotificationModel>>() {}.getType();
        return new Gson().fromJson(json, type);
    }
}
