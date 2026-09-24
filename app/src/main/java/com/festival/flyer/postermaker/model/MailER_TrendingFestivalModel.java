package com.festival.flyer.postermaker.model;

public class MailER_TrendingFestivalModel {
    private String cat_id;
    private String cat_name;
    private String banner_image;

    public MailER_TrendingFestivalModel(String cat_id, String cat_name, String banner_image) {
        this.cat_id = cat_id;
        this.cat_name = cat_name;
        this.banner_image = banner_image;
    }

    public String getCat_id() {
        return cat_id;
    }

    public String getCat_name() {
        return cat_name;
    }

    public String getBanner_image() {
        return banner_image;
    }
}
