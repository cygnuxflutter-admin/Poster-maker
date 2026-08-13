package com.festival.flyer.postermaker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MailER_TemplateModel implements Parcelable {

    private String cat_id;
    private String ratio;
    private String back_image;
    private String post_id;
    private String post_thumb;
    private ArrayList<MailER_TextModel> text_model;
    private ArrayList<MailER_StickerModel> sticker_model;

    public MailER_TemplateModel(String cat_id, String ratio, String back_image, String post_id, String post_thumb, ArrayList<MailER_TextModel> text_model, ArrayList<MailER_StickerModel> sticker_model) {
        this.cat_id = cat_id;
        this.ratio = ratio;
        this.back_image = back_image;
        this.post_id = post_id;
        this.post_thumb = post_thumb;
        this.text_model = text_model;
        this.sticker_model = sticker_model;
    }



    protected MailER_TemplateModel(Parcel in) {
        cat_id = in.readString();
        ratio = in.readString();
        back_image = in.readString();
        post_id = in.readString();
        post_thumb = in.readString();
    }

    public static final Creator<MailER_TemplateModel> CREATOR = new Creator<MailER_TemplateModel>() {
        @Override
        public MailER_TemplateModel createFromParcel(Parcel in) {
            return new MailER_TemplateModel(in);
        }

        @Override
        public MailER_TemplateModel[] newArray(int size) {
            return new MailER_TemplateModel[size];
        }
    };

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public String getBack_image() {
        return back_image;
    }

    public void setBack_image(String back_image) {
        this.back_image = back_image;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_thumb() {
        return post_thumb;
    }

    public void setPost_thumb(String post_thumb) {
        this.post_thumb = post_thumb;
    }

    public ArrayList<MailER_TextModel> getText_model() {
        return text_model;
    }

    public void setText_model(ArrayList<MailER_TextModel> text_model) {
        this.text_model = text_model;
    }

    public ArrayList<MailER_StickerModel> getSticker_model() {
        return sticker_model;
    }

    public void setSticker_model(ArrayList<MailER_StickerModel> sticker_model) {
        this.sticker_model = sticker_model;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cat_id);
        dest.writeString(ratio);
        dest.writeString(back_image);
        dest.writeString(post_id);
        dest.writeString(post_thumb);
    }
}
