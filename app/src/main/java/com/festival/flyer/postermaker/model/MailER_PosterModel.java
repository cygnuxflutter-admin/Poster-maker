package com.festival.flyer.postermaker.model;

import java.io.Serializable;
import java.util.ArrayList;

public class MailER_PosterModel implements Serializable {
    private int cat_id;

    private String thumb_img;

    private String cat_name;

    private ArrayList<MailER_PosterImage> poster_list;


    public MailER_PosterModel(String cat_id, String thumb_img, String cat_name, ArrayList<MailER_PosterImage> poster_list) {
        this.cat_id = Integer.parseInt(cat_id);
        this.thumb_img = thumb_img;
        this.cat_name = cat_name;
        this.poster_list = poster_list;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public String getThumb_img() {
        return thumb_img;
    }

    public void setThumb_img(String thumb_img) {
        this.thumb_img = thumb_img;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

    public ArrayList<MailER_PosterImage> getPoster_list() {
        return poster_list;
    }

    public void setPoster_list(ArrayList<MailER_PosterImage> poster_list) {
        this.poster_list = poster_list;
    }

}
