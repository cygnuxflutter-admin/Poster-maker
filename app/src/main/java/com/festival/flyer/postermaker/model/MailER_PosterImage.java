package com.festival.flyer.postermaker.model;

import java.io.Serializable;

public class MailER_PosterImage implements Serializable {
    int post_id;
    int cat_id;
    String post_thumb;
    String ratio;
    boolean isPremium;

    public MailER_PosterImage(String post_id, String post_thumb, String ratio) {
        this.post_id = Integer.parseInt(post_id);
        this.post_thumb = post_thumb;
        this.ratio = ratio;
        this.isPremium = false;
    }

    public MailER_PosterImage(String post_id, String post_thumb, String ratio, boolean isPremium) {
        this.post_id = Integer.parseInt(post_id);
        this.post_thumb = post_thumb;
        this.ratio = ratio;
        this.isPremium = isPremium;
    }
    
    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public boolean getPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public String getPost_thumb() {
        return post_thumb;
    }

    public void setPost_thumb(String post_thumb) {
        this.post_thumb = post_thumb;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }
}
