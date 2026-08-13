package com.festival.flyer.postermaker.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MailER_StickerModel implements Parcelable {

    private String st_order;
    private String st_image;
    private String st_rotation;
    private String st_height;
    private String st_y_pos;
    private String st_x_pos;
    private String st_res_uri;
    private String st_width;
    private String sticker_id;
    private String st_res_id;

    public MailER_StickerModel(String st_order, String st_image, String st_rotation, String st_height, String st_y_pos, String st_x_pos, String st_res_uri, String st_width, String sticker_id, String st_res_id) {
        this.st_order = st_order;
        this.st_image = st_image;
        this.st_rotation = st_rotation;
        this.st_height = st_height;
        this.st_y_pos = st_y_pos;
        this.st_x_pos = st_x_pos;
        this.st_res_uri = st_res_uri;
        this.st_width = st_width;
        this.sticker_id = sticker_id;
        this.st_res_id = st_res_id;
    }

    public String getSt_order() {
        return st_order;
    }

    public void setSt_order(String st_order) {
        this.st_order = st_order;
    }

    public String getSt_image() {
        return st_image;
    }

    public void setSt_image(String st_image) {
        this.st_image = st_image;
    }

    public String getSt_rotation() {
        return st_rotation;
    }

    public void setSt_rotation(String st_rotation) {
        this.st_rotation = st_rotation;
    }

    public String getSt_height() {
        return st_height;
    }

    public void setSt_height(String st_height) {
        this.st_height = st_height;
    }

    public String getSt_y_pos() {
        return st_y_pos;
    }

    public void setSt_y_pos(String st_y_pos) {
        this.st_y_pos = st_y_pos;
    }

    public String getSt_x_pos() {
        return st_x_pos;
    }

    public void setSt_x_pos(String st_x_pos) {
        this.st_x_pos = st_x_pos;
    }

    public String getSt_res_uri() {
        return st_res_uri;
    }

    public void setSt_res_uri(String st_res_uri) {
        this.st_res_uri = st_res_uri;
    }

    public String getSt_width() {
        return st_width;
    }

    public void setSt_width(String st_width) {
        this.st_width = st_width;
    }

    public String getSticker_id() {
        return sticker_id;
    }

    public void setSticker_id(String sticker_id) {
        this.sticker_id = sticker_id;
    }

    public String getSt_res_id() {
        return st_res_id;
    }

    public void setSt_res_id(String st_res_id) {
        this.st_res_id = st_res_id;
    }

    @Override
    public String toString() {
        return "ClassPojo [st_order = " + st_order + ", st_image = " + st_image + ", st_rotation = " + st_rotation + ", st_height = " + st_height + ", st_y_pos = " + st_y_pos + ", st_x_pos = " + st_x_pos + ", st_res_uri = " + st_res_uri + ", st_width = " + st_width + ", sticker_id = " + sticker_id + ", st_res_id = " + st_res_id + "]";
    }

    protected MailER_StickerModel(Parcel in) {
        st_order = in.readString();
        st_image = in.readString();
        st_rotation = in.readString();
        st_height = in.readString();
        st_y_pos = in.readString();
        st_x_pos = in.readString();
        st_res_uri = in.readString();
        st_width = in.readString();
        sticker_id = in.readString();
        st_res_id = in.readString();
    }

    public static final Creator<MailER_StickerModel> CREATOR = new Creator<MailER_StickerModel>() {
        @Override
        public MailER_StickerModel createFromParcel(Parcel in) {
            return new MailER_StickerModel(in);
        }

        @Override
        public MailER_StickerModel[] newArray(int size) {
            return new MailER_StickerModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(st_order);
        dest.writeString(st_image);
        dest.writeString(st_rotation);
        dest.writeString(st_height);
        dest.writeString(st_y_pos);
        dest.writeString(st_x_pos);
        dest.writeString(st_res_uri);
        dest.writeString(st_width);
        dest.writeString(sticker_id);
        dest.writeString(st_res_id);
    }
}

