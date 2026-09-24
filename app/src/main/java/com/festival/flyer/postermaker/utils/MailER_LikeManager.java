package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class MailER_LikeManager {
    private static final String PREF_NAME = "liked_backgrounds_pref";
    private static final String KEY_LIKED_URLS = "liked_urls";
    private SharedPreferences sharedPreferences;

    public MailER_LikeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Set<String> getLikedUrls() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_LIKED_URLS, new HashSet<>()));
    }

    public boolean isLiked(String url) {
        return getLikedUrls().contains(url);
    }

    public void toggleLike(String url) {
        Set<String> likedUrls = getLikedUrls();
        if (likedUrls.contains(url)) {
            likedUrls.remove(url);
        } else {
            likedUrls.add(url);
        }
        sharedPreferences.edit().putStringSet(KEY_LIKED_URLS, likedUrls).apply();
    }
}
