package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MailER_AutoPosterLikeManager {
    private static final String PREF_NAME = "liked_autoposters_pref";
    private static final String KEY_LIKED_TEMPLATES = "liked_autoposters";
    private SharedPreferences sharedPreferences;

    public MailER_AutoPosterLikeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<String> getLikedTemplates() {
        String json = sharedPreferences.getString(KEY_LIKED_TEMPLATES, null);
        List<String> list = new ArrayList<>();
        if (json == null) {
            return list;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void toggleLike(String posterObjStr) {
        List<String> likedTemplates = getLikedTemplates();
        try {
            JSONObject newObj = new JSONObject(posterObjStr);
            String newId = newObj.optString("id", "");

            boolean found = false;
            for (int i = 0; i < likedTemplates.size(); i++) {
                JSONObject obj = new JSONObject(likedTemplates.get(i));
                if (obj.optString("id", "").equals(newId)) {
                    likedTemplates.remove(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                likedTemplates.add(0, posterObjStr); // Add to beginning
            }

            saveLikedTemplates(likedTemplates);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isLiked(String postId) {
        List<String> likedTemplates = getLikedTemplates();
        for (String str : likedTemplates) {
            try {
                JSONObject obj = new JSONObject(str);
                if (obj.optString("id", "").equals(postId)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void saveLikedTemplates(List<String> likedTemplates) {
        JSONArray array = new JSONArray();
        for (String str : likedTemplates) {
            array.put(str);
        }
        sharedPreferences.edit().putString(KEY_LIKED_TEMPLATES, array.toString()).apply();
    }
}
