package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MailER_TemplateLikeManager {
    private static final String PREF_NAME = "liked_templates_pref";
    private static final String KEY_LIKED_TEMPLATES = "liked_templates";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public MailER_TemplateLikeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<MailER_PosterImage> getLikedTemplates() {
        String json = sharedPreferences.getString(KEY_LIKED_TEMPLATES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<MailER_PosterImage>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isLiked(int postId) {
        List<MailER_PosterImage> list = getLikedTemplates();
        for (MailER_PosterImage p : list) {
            if (p.getPost_id() == postId) {
                return true;
            }
        }
        return false;
    }

    public void toggleLike(MailER_PosterImage posterImage) {
        List<MailER_PosterImage> list = getLikedTemplates();
        boolean found = false;
        Iterator<MailER_PosterImage> iterator = list.iterator();
        while (iterator.hasNext()) {
            MailER_PosterImage p = iterator.next();
            if (p.getPost_id() == posterImage.getPost_id()) {
                iterator.remove();
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(posterImage);
        }
        
        String json = gson.toJson(list);
        sharedPreferences.edit().putString(KEY_LIKED_TEMPLATES, json).apply();
    }
}
