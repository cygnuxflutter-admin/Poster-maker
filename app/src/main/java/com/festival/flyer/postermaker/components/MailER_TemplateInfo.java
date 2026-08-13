package com.festival.flyer.postermaker.components;

import android.os.Parcel;
import android.os.Parcelable;

public class MailER_TemplateInfo implements Parcelable {
    private String FRAME_NAME;
    int OVERLAY_BLUR = 0;
    private String OVERLAY_NAME = "";
    int OVERLAY_OPACITY = 0;
    private String PROFILE_TYPE;
    private String RATIO;
    private String SEEK_VALUE;
    private String TEMPCOLOR = "";
    private int TEMPLATE_ID;
    private String TEMP_PATH = "";
    private String THUMB_URI;
    private String TYPE;
    private String OPERATION;
    private String CHILD_TYPE;
    int OVERLAY_POS, OLD_X, OLD_Y;
    int CHILD_POSITION;

    public MailER_TemplateInfo(String THUMB_URI, String FRAME_NAME, String RATIO, String PROFILE_TYPE, String SEEK_VALUE, String TYPE, String temp_path, String temp_color, String overlay_name, int overlay_opacity, int overlay_blur, int overlay_pos) {
        this.THUMB_URI = THUMB_URI;
        this.FRAME_NAME = FRAME_NAME;
        this.RATIO = RATIO;
        this.PROFILE_TYPE = PROFILE_TYPE;
        this.SEEK_VALUE = SEEK_VALUE;
        this.TYPE = TYPE;
        this.TEMP_PATH = temp_path;
        this.OVERLAY_OPACITY = overlay_opacity;
        this.TEMPCOLOR = temp_color;
        this.OVERLAY_NAME = overlay_name;
        this.OVERLAY_BLUR = overlay_blur;
        this.OVERLAY_POS = overlay_pos;
    }

    public MailER_TemplateInfo() {

    }

    protected MailER_TemplateInfo(Parcel in) {
        FRAME_NAME = in.readString();
        OVERLAY_BLUR = in.readInt();
        OVERLAY_NAME = in.readString();
        OVERLAY_OPACITY = in.readInt();
        PROFILE_TYPE = in.readString();
        RATIO = in.readString();
        SEEK_VALUE = in.readString();
        TEMPCOLOR = in.readString();
        TEMPLATE_ID = in.readInt();
        TEMP_PATH = in.readString();
        THUMB_URI = in.readString();
        TYPE = in.readString();
        OVERLAY_POS = in.readInt();
        OPERATION = in.readString();
        CHILD_TYPE = in.readString();
        CHILD_POSITION = in.readInt();
        OLD_X = in.readInt();
        OLD_Y = in.readInt();
    }

    public static final Creator<MailER_TemplateInfo> CREATOR = new Creator<MailER_TemplateInfo>() {
        @Override
        public MailER_TemplateInfo createFromParcel(Parcel in) {
            return new MailER_TemplateInfo(in);
        }

        @Override
        public MailER_TemplateInfo[] newArray(int size) {
            return new MailER_TemplateInfo[size];
        }
    };

    public int getOVERLAY_POS() {
        return OVERLAY_POS;
    }

    public void setOVERLAY_POS(int OVERLAY_POS) {
        this.OVERLAY_POS = OVERLAY_POS;
    }

    public int getTEMPLATE_ID() {
        return this.TEMPLATE_ID;
    }

    public void setTEMPLATE_ID(int TEMPLATE_ID) {
        this.TEMPLATE_ID = TEMPLATE_ID;
    }

    public String getTHUMB_URI() {
        return this.THUMB_URI;
    }

    public void setTHUMB_URI(String THUMB_URI) {
        this.THUMB_URI = THUMB_URI;
    }

    public String getFRAME_NAME() {
        return this.FRAME_NAME;
    }

    public void setFRAME_NAME(String FRAME_NAME) {
        this.FRAME_NAME = FRAME_NAME;
    }

    public String getRATIO() {
        return this.RATIO;
    }

    public void setRATIO(String RATIO) {
        this.RATIO = RATIO;
    }

    public String getPROFILE_TYPE() {
        return this.PROFILE_TYPE;
    }

    public void setPROFILE_TYPE(String PROFILE_TYPE) {
        this.PROFILE_TYPE = PROFILE_TYPE;
    }

    public String getSEEK_VALUE() {
        return this.SEEK_VALUE;
    }

    public void setSEEK_VALUE(String SEEK_VALUE) {
        this.SEEK_VALUE = SEEK_VALUE;
    }

    public String getTYPE() {
        return this.TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public int getOVERLAY_OPACITY() {
        return this.OVERLAY_OPACITY;
    }

    public void setOVERLAY_OPACITY(int OVERLAY_OPACITY) {
        this.OVERLAY_OPACITY = OVERLAY_OPACITY;
    }

    public String getTEMPCOLOR() {
        return this.TEMPCOLOR;
    }

    public void setTEMPCOLOR(String TEMPCOLOR) {
        this.TEMPCOLOR = TEMPCOLOR;
    }

    public String getOVERLAY_NAME() {
        return this.OVERLAY_NAME;
    }

    public void setOVERLAY_NAME(String OVERLAY_NAME) {
        this.OVERLAY_NAME = OVERLAY_NAME;
    }

    public String getTEMP_PATH() {
        return this.TEMP_PATH;
    }

    public void setTEMP_PATH(String TEMP_PATH) {
        this.TEMP_PATH = TEMP_PATH;
    }

    public int getOVERLAY_BLUR() {
        return this.OVERLAY_BLUR;
    }

    public void setOVERLAY_BLUR(int OVERLAY_BLUR) {
        this.OVERLAY_BLUR = OVERLAY_BLUR;
    }

    public String getOPERATION() {
        return OPERATION;
    }

    public void setOPERATION(String OPERATION) {
        this.OPERATION = OPERATION;
    }

    public int getOLD_X() {
        return OLD_X;
    }

    public void setOLD_X(int OLD_X) {
        this.OLD_X = OLD_X;
    }

    public int getOLD_Y() {
        return OLD_Y;
    }

    public void setOLD_Y(int OLD_Y) {
        this.OLD_Y = OLD_Y;
    }

    public String getCHILD_TYPE() {
        return CHILD_TYPE;
    }

    public void setCHILD_TYPE(String CHILD_TYPE) {
        this.CHILD_TYPE = CHILD_TYPE;
    }

    public int getCHILD_POSITION() {
        return CHILD_POSITION;
    }

    public void setCHILD_POSITION(int CHILD_POSITION) {
        this.CHILD_POSITION = CHILD_POSITION;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FRAME_NAME);
        dest.writeInt(OVERLAY_BLUR);
        dest.writeString(OVERLAY_NAME);
        dest.writeInt(OVERLAY_OPACITY);
        dest.writeString(PROFILE_TYPE);
        dest.writeString(RATIO);
        dest.writeString(SEEK_VALUE);
        dest.writeString(TEMPCOLOR);
        dest.writeInt(TEMPLATE_ID);
        dest.writeString(TEMP_PATH);
        dest.writeString(THUMB_URI);
        dest.writeString(TYPE);
        dest.writeInt(OVERLAY_POS);
        dest.writeString(OPERATION);
        dest.writeString(CHILD_TYPE);
        dest.writeInt(CHILD_POSITION);
        dest.writeInt(OLD_X);
        dest.writeInt(OLD_Y);
    }
}
