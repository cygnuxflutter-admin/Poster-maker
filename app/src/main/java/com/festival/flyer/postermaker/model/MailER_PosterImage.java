package com.festival.flyer.postermaker.model;

import java.io.Serializable;

public class MailER_PosterImage implements Serializable {
    int post_id;
    String post_thumb;
    String ratio;

    public MailER_PosterImage(String post_id, String post_thumb, String ratio) {
        this.post_id = Integer.parseInt(post_id);
        this.post_thumb = post_thumb;
        this.ratio = ratio;
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
