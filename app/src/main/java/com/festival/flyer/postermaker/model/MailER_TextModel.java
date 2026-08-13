package com.festival.flyer.postermaker.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MailER_TextModel implements Parcelable {

    private String txt_height;
    private String txt_width;
    private String text;
    private String txt_x_pos;
    private String font_family;
    private String txt_y_pos;
    private String txt_order;
    private String text_id;
    private String txt_rotation;
    private String txt_color;

    public MailER_TextModel(String txt_height, String txt_width, String text, String txt_x_pos, String font_family, String txt_y_pos, String txt_order, String text_id, String txt_rotation, String txt_color) {
        this.txt_height = txt_height;
        this.txt_width = txt_width;
        this.text = text;
        this.txt_x_pos = txt_x_pos;
        this.font_family = font_family;
        this.txt_y_pos = txt_y_pos;
        this.txt_order = txt_order;
        this.text_id = text_id;
        this.txt_rotation = txt_rotation;
        this.txt_color = txt_color;
    }

    public String getTxt_height() {
        return txt_height;
    }

    public void setTxt_height(String txt_height) {
        this.txt_height = txt_height;
    }

    public String getTxt_width() {
        return txt_width;
    }

    public void setTxt_width(String txt_width) {
        this.txt_width = txt_width;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTxt_x_pos() {
        return txt_x_pos;
    }

    public void setTxt_x_pos(String txt_x_pos) {
        this.txt_x_pos = txt_x_pos;
    }

    public String getFont_family() {
        return font_family;
    }

    public void setFont_family(String font_family) {
        this.font_family = font_family;
    }

    public String getTxt_y_pos() {
        return txt_y_pos;
    }

    public void setTxt_y_pos(String txt_y_pos) {
        this.txt_y_pos = txt_y_pos;
    }

    public String getTxt_order() {
        return txt_order;
    }

    public void setTxt_order(String txt_order) {
        this.txt_order = txt_order;
    }

    public String getText_id() {
        return text_id;
    }

    public void setText_id(String text_id) {
        this.text_id = text_id;
    }

    public String getTxt_rotation() {
        return txt_rotation;
    }

    public void setTxt_rotation(String txt_rotation) {
        this.txt_rotation = txt_rotation;
    }

    public String getTxt_color() {
        return txt_color;
    }

    public void setTxt_color(String txt_color) {
        this.txt_color = txt_color;
    }

    public static Creator<MailER_TextModel> getCREATOR() {
        return CREATOR;
    }

    protected MailER_TextModel(Parcel in) {
        txt_height = in.readString();
        txt_width = in.readString();
        text = in.readString();
        txt_x_pos = in.readString();
        font_family = in.readString();
        txt_y_pos = in.readString();
        txt_order = in.readString();
        text_id = in.readString();
        txt_rotation = in.readString();
        txt_color = in.readString();
    }

    public static final Creator<MailER_TextModel> CREATOR = new Creator<MailER_TextModel>() {
        @Override
        public MailER_TextModel createFromParcel(Parcel in) {
            return new MailER_TextModel(in);
        }

        @Override
        public MailER_TextModel[] newArray(int size) {
            return new MailER_TextModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(txt_height);
        dest.writeString(txt_width);
        dest.writeString(text);
        dest.writeString(txt_x_pos);
        dest.writeString(font_family);
        dest.writeString(txt_y_pos);
        dest.writeString(txt_order);
        dest.writeString(text_id);
        dest.writeString(txt_rotation);
        dest.writeString(txt_color);
    }
}