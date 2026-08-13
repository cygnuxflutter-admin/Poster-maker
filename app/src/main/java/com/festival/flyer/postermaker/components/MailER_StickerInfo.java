package com.festival.flyer.postermaker.components;

import android.graphics.Bitmap;
import android.net.Uri;

public class MailER_StickerInfo {
    private Bitmap BITMAP;
    private String COLORTYPE;
    private int COMP_ID;
    private String FIELD_FOUR = "";
    int FIELD_ONE = 0;
    private String FIELD_THREE = "";
    private String FIELD_TWO = "";
    private int HEIGHT;
    private int ORDER;
    private float POS_X;
    private float POS_Y;
    private String RES_ID;
    private Uri RES_URI;
    private float ROTATION;
    private int STC_COLOR;
    private int STC_HUE;
    private int STC_OPACITY;
    private String STKR_PATH = "";
    int ScaleProg;
    private int TEMPLATE_ID;
    private String TYPE = "";
    private int WIDTH;
    int XRotateProg;
    int YRotateProg;
    private float Y_ROTATION;
    int ZRotateProg;

    public MailER_StickerInfo(int TEMPLATE_ID, float POS_X, float POS_Y, int WIDTH, int HEIGHT, float ROTATION, float Y_ROTATION, String RES_ID, String TYPE, int ORDER, int STC_COLOR, int STC_OPACITY, int XRotateProg, int YRotateProg, int ZRotateProg, int ScaleProg, String stkr_path, String COLORTYPE, int stc_hue, int field_one, String field_two, String field_three, String field_four, Uri res_uri, Bitmap BITMAP) {
        this.TEMPLATE_ID = TEMPLATE_ID;
        this.POS_X = POS_X;
        this.POS_Y = POS_Y;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.ROTATION = ROTATION;
        this.Y_ROTATION = Y_ROTATION;
        this.RES_ID = RES_ID;
        this.RES_URI = res_uri;
        this.BITMAP = BITMAP;
        this.TYPE = TYPE;
        this.ORDER = ORDER;
        this.STC_COLOR = STC_COLOR;
        this.COLORTYPE = COLORTYPE;
        this.STC_OPACITY = STC_OPACITY;
        this.XRotateProg = XRotateProg;
        this.YRotateProg = YRotateProg;
        this.ZRotateProg = ZRotateProg;
        this.ScaleProg = ScaleProg;
        this.STKR_PATH = stkr_path;
        this.STC_HUE = stc_hue;
        this.FIELD_ONE = field_one;
        this.FIELD_TWO = field_two;
        this.FIELD_THREE = field_three;
        this.FIELD_FOUR = field_four;
    }

    public MailER_StickerInfo() {

    }

    public int getCOMP_ID() {
        return this.COMP_ID;
    }

    public void setCOMP_ID(int COMP_ID) {
        this.COMP_ID = COMP_ID;
    }

    public int getTEMPLATE_ID() {
        return this.TEMPLATE_ID;
    }

    public void setTEMPLATE_ID(int TEMPLATE_ID) {
        this.TEMPLATE_ID = TEMPLATE_ID;
    }

    public float getPOS_X() {
        return this.POS_X;
    }

    public void setPOS_X(float POS_X) {
        this.POS_X = POS_X;
    }

    public float getPOS_Y() {
        return this.POS_Y;
    }

    public void setPOS_Y(float POS_Y) {
        this.POS_Y = POS_Y;
    }

    public String getRES_ID() {
        return this.RES_ID;
    }

    public void setRES_ID(String RES_ID) {
        this.RES_ID = RES_ID;
    }

    public String getTYPE() {
        return this.TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public int getORDER() {
        return this.ORDER;
    }

    public void setORDER(int ORDER) {
        this.ORDER = ORDER;
    }

    public float getROTATION() {
        return this.ROTATION;
    }

    public void setROTATION(float ROTATION) {
        this.ROTATION = ROTATION;
    }

    public int getWIDTH() {
        return this.WIDTH;
    }

    public void setWIDTH(int WIDTH) {
        this.WIDTH = WIDTH;
    }

    public int getHEIGHT() {
        return this.HEIGHT;
    }

    public void setHEIGHT(int HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    public float getY_ROTATION() {
        return this.Y_ROTATION;
    }

    public void setY_ROTATION(float y_ROTATION) {
        this.Y_ROTATION = y_ROTATION;
    }

    public Uri getRES_URI() {
        return this.RES_URI;
    }

    public void setRES_URI(Uri RES_URI) {
        this.RES_URI = RES_URI;
    }

    public Bitmap getBITMAP() {
        return this.BITMAP;
    }

    public void setBITMAP(Bitmap BITMAP) {
        this.BITMAP = BITMAP;
    }

    public int getSTC_COLOR() {
        return this.STC_COLOR;
    }

    public void setSTC_COLOR(int STC_COLOR) {
        this.STC_COLOR = STC_COLOR;
    }

    public String getCOLORTYPE() {
        return this.COLORTYPE;
    }

    public void setCOLORTYPE(String COLORTYPE) {
        this.COLORTYPE = COLORTYPE;
    }

    public int getSTC_OPACITY() {
        return this.STC_OPACITY;
    }

    public void setSTC_OPACITY(int STC_OPACITY) {
        this.STC_OPACITY = STC_OPACITY;
    }

    public int getXRotateProg() {
        return this.XRotateProg;
    }

    public void setXRotateProg(int XRotateProg) {
        this.XRotateProg = XRotateProg;
    }

    public int getYRotateProg() {
        return this.YRotateProg;
    }

    public void setYRotateProg(int YRotateProg) {
        this.YRotateProg = YRotateProg;
    }

    public int getZRotateProg() {
        return this.ZRotateProg;
    }

    public void setZRotateProg(int ZRotateProg) {
        this.ZRotateProg = ZRotateProg;
    }

    public int getScaleProg() {
        return this.ScaleProg;
    }

    public void setScaleProg(int ScaleProg) {
        this.ScaleProg = ScaleProg;
    }

    public int getFIELD_ONE() {
        return this.FIELD_ONE;
    }

    public void setFIELD_ONE(int FIELD_ONE) {
        this.FIELD_ONE = FIELD_ONE;
    }

    public String getFIELD_TWO() {
        return this.FIELD_TWO;
    }

    public void setFIELD_TWO(String FIELD_TWO) {
        this.FIELD_TWO = FIELD_TWO;
    }

    public String getFIELD_THREE() {
        return this.FIELD_THREE;
    }

    public void setFIELD_THREE(String FIELD_THREE) {
        this.FIELD_THREE = FIELD_THREE;
    }

    public String getFIELD_FOUR() {
        return this.FIELD_FOUR;
    }

    public void setFIELD_FOUR(String FIELD_FOUR) {
        this.FIELD_FOUR = FIELD_FOUR;
    }

    public String getSTKR_PATH() {
        return this.STKR_PATH;
    }

    public void setSTKR_PATH(String STKR_PATH) {
        this.STKR_PATH = STKR_PATH;
    }

    public int getSTC_HUE() {
        return this.STC_HUE;
    }

    public void setSTC_HUE(int STC_HUE) {
        this.STC_HUE = STC_HUE;
    }
}
