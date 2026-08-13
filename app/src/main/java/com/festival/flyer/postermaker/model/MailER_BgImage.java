package com.festival.flyer.postermaker.model;

import java.io.Serializable;

public class MailER_BgImage implements Serializable {

    private int id;
    private String thumb_url;
    private String image_url;

    public MailER_BgImage(int id, String thumb_url, String image_url) {
        this.id = id;
        this.thumb_url = thumb_url;
        this.image_url = image_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}