package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.festival.flyer.postermaker.models.MailER_NotificationModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MailER_NotificationHelper {
    private static final String PREF_NAME = "PosterMakerNotifications";
    private static final String KEY_LIST = "notif_list";

    public static synchronized void saveNotification(Context context, MailER_NotificationModel notification) {
        if (notification == null) return;
        List<MailER_NotificationModel> currentList = getNotifications(context);

        // Remove existing item if same ID or same Title+Message exists and preserve read status + original timestamp
        removeDuplicate(currentList, notification);

        currentList.add(0, notification); // Add new notification to top

        // Filter out any other duplicate entries
        currentList = deduplicateList(currentList);

        // Keep only last 50
        if (currentList.size() > 50) {
            currentList = new ArrayList<>(currentList.subList(0, 50));
        }

        saveAllNotifications(context, currentList);
    }

    public static synchronized void saveAllNotifications(Context context, List<MailER_NotificationModel> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LIST, new Gson().toJson(list)).apply();
    }

    public static synchronized void markNotificationAsRead(Context context, String notificationId) {
        if (notificationId == null) return;
        List<MailER_NotificationModel> list = getNotifications(context);
        boolean updated = false;
        for (MailER_NotificationModel model : list) {
            if (notificationId.equals(model.getId())) {
                model.setRead(true);
                updated = true;
                break;
            }
        }
        if (updated) {
            saveAllNotifications(context, list);
        }
    }

    public static List<MailER_NotificationModel> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<MailER_NotificationModel>>() {}.getType();
        List<MailER_NotificationModel> rawList = new Gson().fromJson(json, type);
        if (rawList == null) {
            return new ArrayList<>();
        }

        List<MailER_NotificationModel> cleanList = deduplicateList(rawList);
        // If raw list had duplicates, persist the cleaned list back to storage
        if (cleanList.size() != rawList.size()) {
            saveAllNotifications(context, cleanList);
        }
        return cleanList;
    }

    public static boolean hasUnreadNotifications(Context context) {
        List<MailER_NotificationModel> list = getNotifications(context);
        for (MailER_NotificationModel model : list) {
            if (!model.isRead()) {
                return true;
            }
        }
        return false;
    }

    private static void removeDuplicate(List<MailER_NotificationModel> list, MailER_NotificationModel target) {
        if (target == null) return;
        for (int i = 0; i < list.size(); i++) {
            MailER_NotificationModel item = list.get(i);
            boolean isSameId = target.getId() != null && target.getId().equals(item.getId());
            boolean isSameContent = target.getTitle() != null && target.getMessage() != null
                    && target.getTitle().trim().equalsIgnoreCase(item.getTitle() != null ? item.getTitle().trim() : "")
                    && target.getMessage().trim().equalsIgnoreCase(item.getMessage() != null ? item.getMessage().trim() : "");

            if (isSameId || isSameContent) {
                // Preserve existing read status so it doesn't reset to unread!
                if (item.isRead()) {
                    target.setRead(true);
                }
                // Preserve original timestamp so it doesn't reset to "Just now" or "3 mins ago"!
                if (item.getTimestamp() > 0) {
                    target.setTimestamp(item.getTimestamp());
                }
                list.remove(i);
                break;
            }
        }
    }

    private static List<MailER_NotificationModel> deduplicateList(List<MailER_NotificationModel> list) {
        List<MailER_NotificationModel> uniqueList = new ArrayList<>();
        Set<String> seenContentKeys = new HashSet<>();

        for (MailER_NotificationModel model : list) {
            if (model == null) continue;
            String contentKey = (model.getTitle() != null ? model.getTitle().trim().toLowerCase() : "")
                    + "||"
                    + (model.getMessage() != null ? model.getMessage().trim().toLowerCase() : "");

            if (!seenContentKeys.contains(contentKey)) {
                seenContentKeys.add(contentKey);
                uniqueList.add(model);
            }
        }
        return uniqueList;
    }
}
