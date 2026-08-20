package com.festival.flyer.postermaker.model;

import java.io.Serializable;

public class MailER_HeroBannerModel implements Serializable {
    private String id;
    private String banner_image;
    private String action_url;

    public MailER_HeroBannerModel(String id, String banner_image, String action_url) {
        this.id = id;
        this.banner_image = banner_image;
        this.action_url = action_url;
    }

    public String getId() {
        return id;
    }

    public String getBanner_image() {
        return banner_image;
    }

    public String getAction_url() {
        return action_url;
    }
}
