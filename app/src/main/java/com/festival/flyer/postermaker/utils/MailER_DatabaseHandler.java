package com.festival.flyer.postermaker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

import com.festival.flyer.postermaker.components.MailER_StickerInfo;
import com.festival.flyer.postermaker.components.MailER_TemplateInfo;
import com.festival.flyer.postermaker.components.MailER_TextInfo;

import java.util.ArrayList;

import yuku.ambilwarna.BuildConfig;

public class MailER_DatabaseHandler extends SQLiteOpenHelper {
    private static final String BG_ALPHA = "BG_ALPHA";
    private static final String BG_COLOR = "BG_COLOR";
    private static final String BG_DRAWABLE = "BG_DRAWABLE";
    private static final String COLORTYPE = "COLORTYPE";
    private static final String COMPONENT_INFO = "COMPONENT_INFO";
    private static final String COMPONENT_INFO1 = "COMPONENT_INFO1";
    private static final String COMPONENT_INFO2 = "COMPONENT_INFO2";
    private static final String COMP_ID = "COMP_ID";
    private static final String CREATE_TABLE_TEMPLATES = "CREATE TABLE TEMPLATES(TEMPLATE_ID INTEGER PRIMARY KEY,THUMB_URI TEXT,FRAME_NAME TEXT,RATIO TEXT,PROFILE_TYPE TEXT,SEEK_VALUE TEXT,TYPE TEXT,TEMP_PATH TEXT,TEMP_COLOR TEXT,OVERLAY_NAME TEXT,OVERLAY_OPACITY TEXT,OVERLAY_BLUR TEXT,OVERLAY_POS INTEGER)";
    private static final String CREATE_TABLE_TEMPLATES1 = "CREATE TABLE TEMPLATES1(TEMPLATE_ID INTEGER PRIMARY KEY,THUMB_URI TEXT,FRAME_NAME TEXT,RATIO TEXT,PROFILE_TYPE TEXT,SEEK_VALUE TEXT,TYPE TEXT,TEMP_PATH TEXT,TEMP_COLOR TEXT,OVERLAY_NAME TEXT,OVERLAY_OPACITY TEXT,OVERLAY_BLUR TEXT,OVERLAY_POS INTEGER, OPERATION TEXT, CHILD_POSITION INTEGER, CHILD_TYPE TEXT, OLD_X INTEGER, OLD_Y INTEGER)";
    private static final String CREATE_TABLE_TEMPLATES2 = "CREATE TABLE TEMPLATES2(TEMPLATE_ID INTEGER PRIMARY KEY,THUMB_URI TEXT,FRAME_NAME TEXT,RATIO TEXT,PROFILE_TYPE TEXT,SEEK_VALUE TEXT,TYPE TEXT,TEMP_PATH TEXT,TEMP_COLOR TEXT,OVERLAY_NAME TEXT,OVERLAY_OPACITY TEXT,OVERLAY_BLUR TEXT,OVERLAY_POS INTEGER, OPERATION TEXT, CHILD_POSITION INTEGER, CHILD_TYPE TEXT, OLD_X INTEGER, OLD_Y INTEGER)";
    private static final String CREATE_TABLE_COMPONENT_INFO = "CREATE TABLE COMPONENT_INFO(COMP_ID INTEGER PRIMARY KEY,TEMPLATE_ID TEXT,POS_X TEXT,POS_Y TEXT,WIDHT TEXT,HEIGHT TEXT,ROTATION TEXT,Y_ROTATION TEXT,RES_ID TEXT,TYPE TEXT,ORDER_ TEXT,STC_COLOR TEXT,STC_OPACITY TEXT,XROTATEPROG TEXT,YROTATEPROG TEXT,ZROTATEPROG TEXT,STC_SCALE TEXT,STKR_PATH TEXT,COLORTYPE TEXT,STC_HUE TEXT,FIELD_ONE TEXT,FIELD_TWO TEXT,FIELD_THREE TEXT,FIELD_FOUR TEXT)";
    private static final String CREATE_TABLE_COMPONENT_INFO1 = "CREATE TABLE COMPONENT_INFO1(COMP_ID INTEGER PRIMARY KEY,TEMPLATE_ID TEXT,POS_X TEXT,POS_Y TEXT,WIDHT TEXT,HEIGHT TEXT,ROTATION TEXT,Y_ROTATION TEXT,RES_ID TEXT,TYPE TEXT,ORDER_ TEXT,STC_COLOR TEXT,STC_OPACITY TEXT,XROTATEPROG TEXT,YROTATEPROG TEXT,ZROTATEPROG TEXT,STC_SCALE TEXT,STKR_PATH TEXT,COLORTYPE TEXT,STC_HUE TEXT,FIELD_ONE TEXT,FIELD_TWO TEXT,FIELD_THREE TEXT,FIELD_FOUR TEXT)";
    private static final String CREATE_TABLE_COMPONENT_INFO2 = "CREATE TABLE COMPONENT_INFO2(COMP_ID INTEGER PRIMARY KEY,TEMPLATE_ID TEXT,POS_X TEXT,POS_Y TEXT,WIDHT TEXT,HEIGHT TEXT,ROTATION TEXT,Y_ROTATION TEXT,RES_ID TEXT,TYPE TEXT,ORDER_ TEXT,STC_COLOR TEXT,STC_OPACITY TEXT,XROTATEPROG TEXT,YROTATEPROG TEXT,ZROTATEPROG TEXT,STC_SCALE TEXT,STKR_PATH TEXT,COLORTYPE TEXT,STC_HUE TEXT,FIELD_ONE TEXT,FIELD_TWO TEXT,FIELD_THREE TEXT,FIELD_FOUR TEXT)";
    private static final String CREATE_TABLE_TEXT_INFO = "CREATE TABLE TEXT_INFO(TEXT_ID INTEGER PRIMARY KEY,TEMPLATE_ID TEXT,TEXT TEXT,FONT_NAME TEXT,TEXT_COLOR TEXT,TEXT_ALPHA TEXT,SHADOW_COLOR TEXT,SHADOW_PROG TEXT,BG_DRAWABLE TEXT,BG_COLOR TEXT,BG_ALPHA TEXT,POS_X TEXT,POS_Y TEXT,WIDHT TEXT,HEIGHT TEXT,ROTATION TEXT,TYPE TEXT,ORDER_ TEXT,XROTATEPROG TEXT,YROTATEPROG TEXT,ZROTATEPROG TEXT,CURVEPROG TEXT,FIELD_ONE TEXT,FIELD_TWO TEXT,FIELD_THREE TEXT,FIELD_FOUR TEXT)";
    private static final String CREATE_TABLE_TEXT_INFO1 = "CREATE TABLE TEXT_INFO1(TEXT_ID INTEGER PRIMARY KEY,TEMPLATE_ID TEXT,TEXT TEXT,FONT_NAME TEXT,TEXT_COLOR TEXT,TEXT_ALPHA TEXT,SHADOW_COLOR TEXT,SHADOW_PROG TEXT,BG_DRAWABLE TEXT,BG_COLOR TEXT,BG_ALPHA TEXT,POS_X TEXT,POS_Y TEXT,WIDHT TEXT,HEIGHT TEXT,ROTATION TEXT,TYPE TEXT,ORDER_ TEXT,XROTATEPROG TEXT,YROTATEPROG TEXT,ZROTATEPROG TEXT,CURVEPROG TEXT,FIELD_ONE TEXT,FIELD_TWO TEXT,FIELD_THREE TEXT,FIELD_FOUR TEXT)";
    private static final String CREATE_TABLE_TEXT_INFO2 = "CREATE TABLE TEXT_INFO2(TEXT_ID INTEGER PRIMARY KEY,TEMPLATE_ID TEXT,TEXT TEXT,FONT_NAME TEXT,TEXT_COLOR TEXT,TEXT_ALPHA TEXT,SHADOW_COLOR TEXT,SHADOW_PROG TEXT,BG_DRAWABLE TEXT,BG_COLOR TEXT,BG_ALPHA TEXT,POS_X TEXT,POS_Y TEXT,WIDHT TEXT,HEIGHT TEXT,ROTATION TEXT,TYPE TEXT,ORDER_ TEXT,XROTATEPROG TEXT,YROTATEPROG TEXT,ZROTATEPROG TEXT,CURVEPROG TEXT,FIELD_ONE TEXT,FIELD_TWO TEXT,FIELD_THREE TEXT,FIELD_FOUR TEXT)";
    private static final String CURVEPROG = "CURVEPROG";
    private static final String TEXT_INFO1 = "TEXT_INFO1";
    private static final String TEXT_INFO2 = "TEXT_INFO2";
    private static final String DATABASE_NAME = "POSTERMAKER_DB";
    private static final int DATABASE_VERSION = 1;
    private static final String FIELD_FOUR = "FIELD_FOUR";
    private static final String FIELD_ONE = "FIELD_ONE";
    private static final String FIELD_THREE = "FIELD_THREE";
    private static final String FIELD_TWO = "FIELD_TWO";
    private static final String FONT_NAME = "FONT_NAME";
    private static final String FRAME_NAME = "FRAME_NAME";
    private static final String HEIGHT = "HEIGHT";
    private static final String ORDER = "ORDER_";
    private static final String OVERLAY_BLUR = "OVERLAY_BLUR";
    private static final String OVERLAY_NAME = "OVERLAY_NAME";
    private static final String OVERLAY_POS = "OVERLAY_POS";
    private static final String OPERATION = "OPERATION";
    private static final String CHILD_POSITION = "CHILD_POSITION";
    private static final String CHILD_TYPE = "CHILD_TYPE";
    private static final String OLD_X = "OLD_X";
    private static final String OLD_Y = "OLD_Y";
    private static final String OVERLAY_OPACITY = "OVERLAY_OPACITY";
    private static final String POS_X = "POS_X";
    private static final String POS_Y = "POS_Y";
    private static final String PROFILE_TYPE = "PROFILE_TYPE";
    private static final String RATIO = "RATIO";
    private static final String RES_ID = "RES_ID";
    private static final String ROTATION = "ROTATION";
    private static final String SEEK_VALUE = "SEEK_VALUE";
    private static final String SHADOW_COLOR = "SHADOW_COLOR";
    private static final String SHADOW_PROG = "SHADOW_PROG";
    private static final String STC_COLOR = "STC_COLOR";
    private static final String STC_HUE = "STC_HUE";
    private static final String STC_OPACITY = "STC_OPACITY";
    private static final String STC_SCALE = "STC_SCALE";
    private static final String STKR_PATH = "STKR_PATH";
    private static final String TEMPLATES = "TEMPLATES";
    private static final String TEMPLATES1 = "TEMPLATES1";
    private static final String TEMPLATES2 = "TEMPLATES2";
    private static final String TEMPLATE_ID = "TEMPLATE_ID";
    private static final String TEMP_COLOR = "TEMP_COLOR";
    private static final String TEMP_PATH = "TEMP_PATH";
    private static final String TEXT = "TEXT";
    private static final String TEXT_ALPHA = "TEXT_ALPHA";
    private static final String TEXT_COLOR = "TEXT_COLOR";
    private static final String TEXT_ID = "TEXT_ID";
    private static final String TEXT_INFO = "TEXT_INFO";
    private static final String THUMB_URI = "THUMB_URI";
    private static final String TYPE = "TYPE";
    private static final String WIDHT = "WIDHT";
    private static final String XROTATEPROG = "XROTATEPROG";
    private static final String YROTATEPROG = "YROTATEPROG";
    private static final String Y_ROTATION = "Y_ROTATION";
    private static final String ZROTATEPROG = "ZROTATEPROG";

    public MailER_DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MailER_DatabaseHandler getDbHandler(Context context) {
        return new MailER_DatabaseHandler(context);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TEMPLATES);
        db.execSQL(CREATE_TABLE_TEMPLATES1);
        db.execSQL(CREATE_TABLE_TEMPLATES2);
        db.execSQL(CREATE_TABLE_TEXT_INFO);
        db.execSQL(CREATE_TABLE_TEXT_INFO1);
        db.execSQL(CREATE_TABLE_TEXT_INFO2);
        db.execSQL(CREATE_TABLE_COMPONENT_INFO);
        db.execSQL(CREATE_TABLE_COMPONENT_INFO1);
        db.execSQL(CREATE_TABLE_COMPONENT_INFO2);
        Log.i("testing", "Database Created");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TEMPLATES1 + " ADD COLUMN " + OPERATION + " TEXT;");
            db.execSQL("ALTER TABLE " + TEMPLATES2 + " ADD COLUMN " + OPERATION + " TEXT;");

        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TEMPLATES1 + " ADD COLUMN " + CHILD_POSITION + " INTEGER;");
            db.execSQL("ALTER TABLE " + TEMPLATES1 + " ADD COLUMN " + CHILD_TYPE + " TEXT;");
            db.execSQL("ALTER TABLE " + TEMPLATES1 + " ADD COLUMN " + OLD_X + " INTEGER;");
            db.execSQL("ALTER TABLE " + TEMPLATES1 + " ADD COLUMN " + OLD_Y + " INTEGER;");

            db.execSQL("ALTER TABLE " + TEMPLATES2 + " ADD COLUMN " + CHILD_POSITION + " INTEGER;");
            db.execSQL("ALTER TABLE " + TEMPLATES2 + " ADD COLUMN " + CHILD_TYPE + " TEXT;");
            db.execSQL("ALTER TABLE " + TEMPLATES2 + " ADD COLUMN " + OLD_X + " INTEGER;");
            db.execSQL("ALTER TABLE " + TEMPLATES2 + " ADD COLUMN " + OLD_Y + " INTEGER;");
        }
    }


    public long insertTemplateRow(MailER_TemplateInfo tInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(THUMB_URI, tInfo.getTHUMB_URI());
        values.put(FRAME_NAME, tInfo.getFRAME_NAME());
        values.put(RATIO, tInfo.getRATIO());
        values.put(PROFILE_TYPE, tInfo.getPROFILE_TYPE());
        values.put(SEEK_VALUE, tInfo.getSEEK_VALUE());
        values.put(TYPE, tInfo.getTYPE());
        values.put(TEMP_PATH, tInfo.getTEMP_PATH());
        values.put(TEMP_COLOR, tInfo.getTEMPCOLOR());
        values.put(OVERLAY_NAME, tInfo.getOVERLAY_NAME());
        values.put(OVERLAY_OPACITY, tInfo.getOVERLAY_OPACITY());
        values.put(OVERLAY_BLUR, tInfo.getOVERLAY_BLUR());
        values.put(OVERLAY_POS, tInfo.getOVERLAY_POS());
        long insert = db.insert(TEMPLATES, null, values);
        db.close();
        return insert;
    }

    public void insertComponentInfoRow(MailER_StickerInfo cInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEMPLATE_ID, cInfo.getTEMPLATE_ID());
        values.put(POS_X, cInfo.getPOS_X());
        values.put(POS_Y, cInfo.getPOS_Y());
        values.put(WIDHT, cInfo.getWIDTH());
        values.put(HEIGHT, cInfo.getHEIGHT());
        values.put(ROTATION, cInfo.getROTATION());
        values.put(Y_ROTATION, cInfo.getY_ROTATION());
        values.put(RES_ID, cInfo.getRES_ID());
        values.put(TYPE, cInfo.getTYPE());
        values.put(ORDER, cInfo.getORDER());
        values.put(STC_COLOR, cInfo.getSTC_COLOR());
        values.put(STC_OPACITY, cInfo.getSTC_OPACITY());
        values.put(XROTATEPROG, cInfo.getXRotateProg());
        values.put(YROTATEPROG, cInfo.getYRotateProg());
        values.put(ZROTATEPROG, cInfo.getZRotateProg());
        values.put(STC_SCALE, cInfo.getScaleProg());
        values.put(STKR_PATH, cInfo.getSTKR_PATH());
        values.put(COLORTYPE, cInfo.getCOLORTYPE());
        values.put(STC_HUE, cInfo.getSTC_HUE());
        values.put(FIELD_ONE, cInfo.getFIELD_ONE());
        values.put(FIELD_TWO, cInfo.getFIELD_TWO());
        values.put(FIELD_THREE, cInfo.getFIELD_THREE());
        values.put(FIELD_FOUR, cInfo.getFIELD_FOUR());
        db.insert(COMPONENT_INFO, null, values);
        db.close();
    }

    public void insertTextRow(MailER_TextInfo tInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEMPLATE_ID, tInfo.getTEMPLATE_ID());
        values.put(TEXT, tInfo.getTEXT());
        values.put(FONT_NAME, tInfo.getFONT_NAME());
        values.put(TEXT_COLOR, tInfo.getTEXT_COLOR());
        values.put(TEXT_ALPHA, tInfo.getTEXT_ALPHA());
        values.put(SHADOW_COLOR, tInfo.getSHADOW_COLOR());
        values.put(SHADOW_PROG, tInfo.getSHADOW_PROG());
        values.put(BG_DRAWABLE, tInfo.getBG_DRAWABLE());
        values.put(BG_COLOR, tInfo.getBG_COLOR());
        values.put(BG_ALPHA, tInfo.getBG_ALPHA());
        values.put(POS_X, tInfo.getPOS_X());
        values.put(POS_Y, tInfo.getPOS_Y());
        values.put(WIDHT, tInfo.getWIDTH());
        values.put(HEIGHT, tInfo.getHEIGHT());
        values.put(ROTATION, tInfo.getROTATION());
        values.put(TYPE, tInfo.getTYPE());
        values.put(ORDER, tInfo.getORDER());
        values.put(XROTATEPROG, tInfo.getXRotateProg());
        values.put(YROTATEPROG, tInfo.getYRotateProg());
        values.put(ZROTATEPROG, tInfo.getZRotateProg());
        values.put(CURVEPROG, tInfo.getCurveRotateProg());
        values.put(FIELD_ONE, tInfo.getFIELD_ONE());
        values.put(FIELD_TWO, tInfo.getFIELD_TWO());
        values.put(FIELD_THREE, tInfo.getFIELD_THREE());
        values.put(FIELD_FOUR, tInfo.getFIELD_FOUR());
        db.insert(TEXT_INFO, null, values);
        db.close();
    }

    public ArrayList<MailER_TemplateInfo> getTemplateListDes(String type) {
        ArrayList<MailER_TemplateInfo> templateList = new ArrayList<>();
        String query = "SELECT  * FROM TEMPLATES WHERE TYPE='" + type + "' ORDER BY " + TEMPLATE_ID + " DESC;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            Log.e("templateList size is", BuildConfig.FLAVOR + templateList.size());
            return templateList;
        }
        do {
            MailER_TemplateInfo values = new MailER_TemplateInfo();
            values.setTEMPLATE_ID(cursor.getInt(0));
            values.setTHUMB_URI(cursor.getString(1));
            values.setFRAME_NAME(cursor.getString(2));
            values.setRATIO(cursor.getString(3));
            values.setPROFILE_TYPE(cursor.getString(4));
            values.setSEEK_VALUE(cursor.getString(5));
            values.setTYPE(cursor.getString(6));
            values.setTEMP_PATH(cursor.getString(7));
            values.setTEMPCOLOR(cursor.getString(8));
            values.setOVERLAY_NAME(cursor.getString(9));
            values.setOVERLAY_OPACITY(cursor.getInt(10));
            values.setOVERLAY_BLUR(cursor.getInt(11));
            values.setOVERLAY_POS(cursor.getInt(12));
            templateList.add(values);
        } while (cursor.moveToNext());
        db.close();
        Log.e("templateList size is", "" + templateList.toString());
        return templateList;
    }

    public ArrayList<MailER_StickerInfo> getComponentInfoList(int templateID, String shape) {
        ArrayList<MailER_StickerInfo> componentInfoList = new ArrayList<>();
        String query = "SELECT * FROM COMPONENT_INFO WHERE TEMPLATE_ID='" + templateID + "' AND " + TYPE + " = '" + shape + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return componentInfoList;
        }
        do {
            MailER_StickerInfo values = new MailER_StickerInfo();
            values.setCOMP_ID(cursor.getInt(0));
            values.setTEMPLATE_ID(cursor.getInt(1));
            values.setPOS_X(cursor.getFloat(2));
            values.setPOS_Y(cursor.getFloat(3));
            values.setWIDTH(cursor.getInt(4));
            values.setHEIGHT(cursor.getInt(5));
            values.setROTATION(cursor.getFloat(6));
            values.setY_ROTATION(cursor.getFloat(7));
            values.setRES_ID(cursor.getString(8));
            values.setTYPE(cursor.getString(9));
            values.setORDER(cursor.getInt(10));
            values.setSTC_COLOR(cursor.getInt(11));
            values.setSTC_OPACITY(cursor.getInt(12));
            values.setXRotateProg(cursor.getInt(13));
            values.setYRotateProg(cursor.getInt(14));
            values.setZRotateProg(cursor.getInt(15));
            values.setScaleProg(cursor.getInt(16));
            values.setSTKR_PATH(cursor.getString(17));
            values.setCOLORTYPE(cursor.getString(18));
            values.setSTC_HUE(cursor.getInt(19));
            values.setFIELD_ONE(cursor.getInt(20));
            values.setFIELD_TWO(cursor.getString(21));
            values.setFIELD_THREE(cursor.getString(22));
            values.setFIELD_FOUR(cursor.getString(23));
            componentInfoList.add(values);
        } while (cursor.moveToNext());
        db.close();
        return componentInfoList;
    }

    public ArrayList<MailER_TextInfo> getTextInfoList(int templateID) {
        ArrayList<MailER_TextInfo> textInfoArrayList = new ArrayList<>();
        String query = "SELECT * FROM TEXT_INFO WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return textInfoArrayList;
        }
        do {
            MailER_TextInfo values = new MailER_TextInfo();
            values.setTEXT_ID(cursor.getInt(0));
            values.setTEMPLATE_ID(cursor.getInt(1));
            values.setTEXT(cursor.getString(2));
            values.setFONT_NAME(cursor.getString(3));
            values.setTEXT_COLOR(cursor.getInt(4));
            values.setTEXT_ALPHA(cursor.getInt(5));
            values.setSHADOW_COLOR(cursor.getInt(6));
            values.setSHADOW_PROG(cursor.getInt(7));
            values.setBG_DRAWABLE(cursor.getString(8));
            values.setBG_COLOR(cursor.getInt(9));
            values.setBG_ALPHA(cursor.getInt(10));
            values.setPOS_X(cursor.getFloat(11));
            values.setPOS_Y(cursor.getFloat(12));
            values.setWIDTH(cursor.getInt(13));
            values.setHEIGHT(cursor.getInt(14));
            values.setROTATION(cursor.getFloat(15));
            values.setTYPE(cursor.getString(16));
            values.setORDER(cursor.getInt(17));
            values.setXRotateProg(cursor.getInt(18));
            values.setYRotateProg(cursor.getInt(19));
            values.setZRotateProg(cursor.getInt(20));
            values.setCurveRotateProg(cursor.getInt(21));
            values.setFIELD_ONE(cursor.getInt(22));
            values.setFIELD_TWO(cursor.getString(23));
            values.setFIELD_THREE(cursor.getString(24));
            values.setFIELD_FOUR(cursor.getString(25));
            textInfoArrayList.add(values);
        } while (cursor.moveToNext());
        db.close();
        return textInfoArrayList;
    }

    public boolean deleteTemplateInfo(int templateID) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM TEMPLATES WHERE TEMPLATE_ID='" + templateID + "'");
            db.execSQL("DELETE FROM COMPONENT_INFO WHERE TEMPLATE_ID='" + templateID + "'");
            db.execSQL("DELETE FROM TEXT_INFO WHERE TEMPLATE_ID='" + templateID + "'");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //====================================================================================================================================//
    public long insertUndoTemplateRow(MailER_TemplateInfo tInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(THUMB_URI, tInfo.getTHUMB_URI());
        values.put(FRAME_NAME, tInfo.getFRAME_NAME());
        values.put(RATIO, tInfo.getRATIO());
        values.put(PROFILE_TYPE, tInfo.getPROFILE_TYPE());
        values.put(SEEK_VALUE, tInfo.getSEEK_VALUE());
        values.put(TYPE, tInfo.getTYPE());
        values.put(TEMP_PATH, tInfo.getTEMP_PATH());
        values.put(TEMP_COLOR, tInfo.getTEMPCOLOR());
        values.put(OVERLAY_NAME, tInfo.getOVERLAY_NAME());
        values.put(OVERLAY_OPACITY, tInfo.getOVERLAY_OPACITY());
        values.put(OVERLAY_BLUR, tInfo.getOVERLAY_BLUR());
        values.put(OVERLAY_POS, tInfo.getOVERLAY_POS());
        values.put(OPERATION, tInfo.getOPERATION());
        values.put(CHILD_POSITION, tInfo.getCHILD_POSITION());
        values.put(CHILD_TYPE, tInfo.getCHILD_TYPE());
        values.put(OLD_X, tInfo.getOLD_X());
        values.put(OLD_Y, tInfo.getOLD_Y());
        long insert = db.insert(TEMPLATES1, null, values);
        db.close();
        return insert;
    }

    public void insertUndoComponentInfoRow(MailER_StickerInfo cInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEMPLATE_ID, cInfo.getTEMPLATE_ID());
        values.put(POS_X, cInfo.getPOS_X());
        values.put(POS_Y, cInfo.getPOS_Y());
        values.put(WIDHT, cInfo.getWIDTH());
        values.put(HEIGHT, cInfo.getHEIGHT());
        values.put(ROTATION, cInfo.getROTATION());
        values.put(Y_ROTATION, cInfo.getY_ROTATION());
        values.put(RES_ID, cInfo.getRES_ID());
        values.put(TYPE, cInfo.getTYPE());
        values.put(ORDER, cInfo.getORDER());
        values.put(STC_COLOR, cInfo.getSTC_COLOR());
        values.put(STC_OPACITY, cInfo.getSTC_OPACITY());
        values.put(XROTATEPROG, cInfo.getXRotateProg());
        values.put(YROTATEPROG, cInfo.getYRotateProg());
        values.put(ZROTATEPROG, cInfo.getZRotateProg());
        values.put(STC_SCALE, cInfo.getScaleProg());
        values.put(STKR_PATH, cInfo.getSTKR_PATH());
        values.put(COLORTYPE, cInfo.getCOLORTYPE());
        values.put(STC_HUE, cInfo.getSTC_HUE());
        values.put(FIELD_ONE, cInfo.getFIELD_ONE());
        values.put(FIELD_TWO, cInfo.getFIELD_TWO());
        values.put(FIELD_THREE, cInfo.getFIELD_THREE());
        values.put(FIELD_FOUR, cInfo.getFIELD_FOUR());
        db.insert(COMPONENT_INFO1, null, values);
        db.close();
    }

    public void insertUndoTextRow(MailER_TextInfo tInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEMPLATE_ID, tInfo.getTEMPLATE_ID());
        values.put(TEXT, tInfo.getTEXT());
        values.put(FONT_NAME, tInfo.getFONT_NAME());
        values.put(TEXT_COLOR, tInfo.getTEXT_COLOR());
        values.put(TEXT_ALPHA, tInfo.getTEXT_ALPHA());
        values.put(SHADOW_COLOR, tInfo.getSHADOW_COLOR());
        values.put(SHADOW_PROG, tInfo.getSHADOW_PROG());
        values.put(BG_DRAWABLE, tInfo.getBG_DRAWABLE());
        values.put(BG_COLOR, tInfo.getBG_COLOR());
        values.put(BG_ALPHA, tInfo.getBG_ALPHA());
        values.put(POS_X, tInfo.getPOS_X());
        values.put(POS_Y, tInfo.getPOS_Y());
        values.put(WIDHT, tInfo.getWIDTH());
        values.put(HEIGHT, tInfo.getHEIGHT());
        values.put(ROTATION, tInfo.getROTATION());
        values.put(TYPE, tInfo.getTYPE());
        values.put(ORDER, tInfo.getORDER());
        values.put(XROTATEPROG, tInfo.getXRotateProg());
        values.put(YROTATEPROG, tInfo.getYRotateProg());
        values.put(ZROTATEPROG, tInfo.getZRotateProg());
        values.put(CURVEPROG, tInfo.getCurveRotateProg());
        values.put(FIELD_ONE, tInfo.getFIELD_ONE());
        values.put(FIELD_TWO, tInfo.getFIELD_TWO());
        values.put(FIELD_THREE, tInfo.getFIELD_THREE());
        values.put(FIELD_FOUR, tInfo.getFIELD_FOUR());
        db.insert(TEXT_INFO1, null, values);
        db.close();
    }

    public int getUndoLstTemplateId() {
        Cursor cursor = null;
        int TEMPLATE_ID = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String query = "SELECT  * FROM TEMPLATES1  ORDER BY  TEMPLATE_ID  DESC;";
            cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                TEMPLATE_ID = cursor.getInt(cursor.getColumnIndex("TEMPLATE_ID"));
            }
            return TEMPLATE_ID;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getUndoLstTextId() {
        Cursor cursor = null;
        int TEMPLATE_ID = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String query = "SELECT  * FROM TEXT_INFO1  ORDER BY  TEMPLATE_ID  DESC;";
            cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                TEMPLATE_ID = cursor.getInt(cursor.getColumnIndex("TEMPLATE_ID"));
            }
            return TEMPLATE_ID;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getUndoLstStickerId() {
        Cursor cursor = null;
        int TEMPLATE_ID = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String query = "SELECT  * FROM COMPONENT_INFO1  ORDER BY  TEMPLATE_ID  DESC;";
            cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                TEMPLATE_ID = cursor.getInt(cursor.getColumnIndex("TEMPLATE_ID"));
            }
            return TEMPLATE_ID;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getUndoOperation(int templateID) {
        String operation;
        String query = "SELECT  * FROM TEMPLATES1 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation = cursor.getString(cursor.getColumnIndex(OPERATION));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public int getUndoChildPosition(int templateID) {
        int operation;
        String query = "SELECT  * FROM TEMPLATES1 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return -1;
        }
        do {
            operation = cursor.getInt(cursor.getColumnIndex(CHILD_POSITION));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public Point getUndoXYPosition(int templateID) {
        Point operation = new Point();
        String query = "SELECT  * FROM TEMPLATES1 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return operation;
        }
        do {
            operation.x = cursor.getInt(cursor.getColumnIndex(OLD_X));
            operation.y = cursor.getInt(cursor.getColumnIndex(OLD_Y));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }


    public String getUndoChildType(int templateID) {
        String operation;
        String query = "SELECT  * FROM TEMPLATES1 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation = cursor.getString(cursor.getColumnIndex(CHILD_TYPE));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }


    public Point getUndoTextXYPosition(int templateID, int pos, String type) {
        Point operation = new Point();
        String query;
        if (type.equals("text")) {
            query = "SELECT  * FROM TEXT_INFO1 WHERE TEMPLATE_ID='" + templateID + "' AND ORDER_ ='" + pos + "'";
        } else {
            query = "SELECT  * FROM COMPONENT_INFO1 WHERE TEMPLATE_ID='" + templateID + "' AND ORDER_ ='" + pos + "'";
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation.x = cursor.getInt(cursor.getColumnIndex(POS_X));
            operation.y = cursor.getInt(cursor.getColumnIndex(POS_Y));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }


    public ArrayList<MailER_TemplateInfo> getUndoTemplateListDes(String type) {
        ArrayList<MailER_TemplateInfo> templateList = new ArrayList<>();
        String query = "SELECT  * FROM TEMPLATES1 WHERE TYPE='" + type + "' ORDER BY " + TEMPLATE_ID + " DESC;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            Log.e("templateList size is", BuildConfig.FLAVOR + templateList.size());
            return templateList;
        }
        do {
            MailER_TemplateInfo values = new MailER_TemplateInfo();
            values.setTEMPLATE_ID(cursor.getInt(0));
            values.setTHUMB_URI(cursor.getString(1));
            values.setFRAME_NAME(cursor.getString(2));
            values.setRATIO(cursor.getString(3));
            values.setPROFILE_TYPE(cursor.getString(4));
            values.setSEEK_VALUE(cursor.getString(5));
            values.setTYPE(cursor.getString(6));
            values.setTEMP_PATH(cursor.getString(7));
            values.setTEMPCOLOR(cursor.getString(8));
            values.setOVERLAY_NAME(cursor.getString(9));
            values.setOVERLAY_OPACITY(cursor.getInt(10));
            values.setOVERLAY_BLUR(cursor.getInt(11));
            values.setOVERLAY_POS(cursor.getInt(12));
            templateList.add(values);
        } while (cursor.moveToNext());
        db.close();
        Log.e("templateList size is", "" + templateList.toString());
        return templateList;
    }

    public MailER_TemplateInfo getUndoTemplateDes(int templateID) {
        MailER_TemplateInfo templateList = new MailER_TemplateInfo();
        String query = "SELECT  * FROM TEMPLATES1 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return templateList;
        }
        do {
            templateList.setTEMPLATE_ID(cursor.getInt(0));
            templateList.setTHUMB_URI(cursor.getString(1));
            templateList.setFRAME_NAME(cursor.getString(2));
            templateList.setRATIO(cursor.getString(3));
            templateList.setPROFILE_TYPE(cursor.getString(4));
            templateList.setSEEK_VALUE(cursor.getString(5));
            templateList.setTYPE(cursor.getString(6));
            templateList.setTEMP_PATH(cursor.getString(7));
            templateList.setTEMPCOLOR(cursor.getString(8));
            templateList.setOVERLAY_NAME(cursor.getString(9));
            templateList.setOVERLAY_OPACITY(cursor.getInt(10));
            templateList.setOVERLAY_BLUR(cursor.getInt(11));
            templateList.setOVERLAY_POS(cursor.getInt(12));
        } while (cursor.moveToNext());
        db.close();
        Log.e("templateList size is", "" + templateList.toString());
        return templateList;
    }

    public ArrayList<MailER_StickerInfo> getUndoComponentInfoList(int templateID, String shape) {
        ArrayList<MailER_StickerInfo> componentInfoList = new ArrayList<>();
        String query = "SELECT * FROM COMPONENT_INFO1 WHERE TEMPLATE_ID='" + templateID + "' AND " + TYPE + " = '" + shape + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return componentInfoList;
        }
        do {
            MailER_StickerInfo values = new MailER_StickerInfo();
            values.setCOMP_ID(cursor.getInt(0));
            values.setTEMPLATE_ID(cursor.getInt(1));
            values.setPOS_X(cursor.getFloat(2));
            values.setPOS_Y(cursor.getFloat(3));
            values.setWIDTH(cursor.getInt(4));
            values.setHEIGHT(cursor.getInt(5));
            values.setROTATION(cursor.getFloat(6));
            values.setY_ROTATION(cursor.getFloat(7));
            values.setRES_ID(cursor.getString(8));
            values.setTYPE(cursor.getString(9));
            values.setORDER(cursor.getInt(10));
            values.setSTC_COLOR(cursor.getInt(11));
            values.setSTC_OPACITY(cursor.getInt(12));
            values.setXRotateProg(cursor.getInt(13));
            values.setYRotateProg(cursor.getInt(14));
            values.setZRotateProg(cursor.getInt(15));
            values.setScaleProg(cursor.getInt(16));
            values.setSTKR_PATH(cursor.getString(17));
            values.setCOLORTYPE(cursor.getString(18));
            values.setSTC_HUE(cursor.getInt(19));
            values.setFIELD_ONE(cursor.getInt(20));
            values.setFIELD_TWO(cursor.getString(21));
            values.setFIELD_THREE(cursor.getString(22));
            values.setFIELD_FOUR(cursor.getString(23));
            componentInfoList.add(values);
        } while (cursor.moveToNext());
        db.close();
        return componentInfoList;
    }

    public ArrayList<MailER_TextInfo> getUndoTextInfoList(int templateID) {
        ArrayList<MailER_TextInfo> textInfoArrayList = new ArrayList<>();
        String query = "SELECT * FROM TEXT_INFO1 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return textInfoArrayList;
        }
        do {
            MailER_TextInfo values = new MailER_TextInfo();
            values.setTEXT_ID(cursor.getInt(0));
            values.setTEMPLATE_ID(cursor.getInt(1));
            values.setTEXT(cursor.getString(2));
            values.setFONT_NAME(cursor.getString(3));
            values.setTEXT_COLOR(cursor.getInt(4));
            values.setTEXT_ALPHA(cursor.getInt(5));
            values.setSHADOW_COLOR(cursor.getInt(6));
            values.setSHADOW_PROG(cursor.getInt(7));
            values.setBG_DRAWABLE(cursor.getString(8));
            values.setBG_COLOR(cursor.getInt(9));
            values.setBG_ALPHA(cursor.getInt(10));
            values.setPOS_X(cursor.getFloat(11));
            values.setPOS_Y(cursor.getFloat(12));
            values.setWIDTH(cursor.getInt(13));
            values.setHEIGHT(cursor.getInt(14));
            values.setROTATION(cursor.getFloat(15));
            values.setTYPE(cursor.getString(16));
            values.setORDER(cursor.getInt(17));
            values.setXRotateProg(cursor.getInt(18));
            values.setYRotateProg(cursor.getInt(19));
            values.setZRotateProg(cursor.getInt(20));
            values.setCurveRotateProg(cursor.getInt(21));
            values.setFIELD_ONE(cursor.getInt(22));
            values.setFIELD_TWO(cursor.getString(23));
            values.setFIELD_THREE(cursor.getString(24));
            values.setFIELD_FOUR(cursor.getString(25));
            textInfoArrayList.add(values);
        } while (cursor.moveToNext());
        db.close();
        return textInfoArrayList;
    }

    public boolean deleteUndoTemplateInfo(int templateID) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM TEMPLATES1 WHERE TEMPLATE_ID='" + templateID + "'");
            db.execSQL("DELETE FROM TEXT_INFO1 WHERE TEMPLATE_ID='" + templateID + "'");
            db.execSQL("DELETE FROM COMPONENT_INFO1 WHERE TEMPLATE_ID='" + templateID + "'");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUndoTextInfo(int templateID) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM TEXT_INFO1 WHERE TEMPLATE_ID='" + templateID + "'");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUndoStickerInfo(int templateID) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM COMPONENT_INFO1 WHERE TEMPLATE_ID='" + templateID + "'");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUndoTemplateInfo() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM TEMPLATES1");
            db.execSQL("DELETE FROM COMPONENT_INFO1");
            db.execSQL("DELETE FROM TEXT_INFO1");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //====================================================================================================================================//
    public long insertRedoTemplateRow(MailER_TemplateInfo tInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(THUMB_URI, tInfo.getTHUMB_URI());
        values.put(FRAME_NAME, tInfo.getFRAME_NAME());
        values.put(RATIO, tInfo.getRATIO());
        values.put(PROFILE_TYPE, tInfo.getPROFILE_TYPE());
        values.put(SEEK_VALUE, tInfo.getSEEK_VALUE());
        values.put(TYPE, tInfo.getTYPE());
        values.put(TEMP_PATH, tInfo.getTEMP_PATH());
        values.put(TEMP_COLOR, tInfo.getTEMPCOLOR());
        values.put(OVERLAY_NAME, tInfo.getOVERLAY_NAME());
        values.put(OVERLAY_OPACITY, tInfo.getOVERLAY_OPACITY());
        values.put(OVERLAY_BLUR, tInfo.getOVERLAY_BLUR());
        values.put(OVERLAY_POS, tInfo.getOVERLAY_POS());
        values.put(OPERATION, tInfo.getOPERATION());
        values.put(CHILD_POSITION, tInfo.getCHILD_POSITION());
        values.put(CHILD_TYPE, tInfo.getCHILD_TYPE());
        values.put(OLD_X, tInfo.getOLD_X());
        values.put(OLD_Y, tInfo.getOLD_Y());
        long insert = db.insert(TEMPLATES2, null, values);
        db.close();
        return insert;
    }

    public void insertRedoComponentInfoRow(MailER_StickerInfo cInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEMPLATE_ID, cInfo.getTEMPLATE_ID());
        values.put(POS_X, cInfo.getPOS_X());
        values.put(POS_Y, cInfo.getPOS_Y());
        values.put(WIDHT, cInfo.getWIDTH());
        values.put(HEIGHT, cInfo.getHEIGHT());
        values.put(ROTATION, cInfo.getROTATION());
        values.put(Y_ROTATION, cInfo.getY_ROTATION());
        values.put(RES_ID, cInfo.getRES_ID());
        values.put(TYPE, cInfo.getTYPE());
        values.put(ORDER, cInfo.getORDER());
        values.put(STC_COLOR, cInfo.getSTC_COLOR());
        values.put(STC_OPACITY, cInfo.getSTC_OPACITY());
        values.put(XROTATEPROG, cInfo.getXRotateProg());
        values.put(YROTATEPROG, cInfo.getYRotateProg());
        values.put(ZROTATEPROG, cInfo.getZRotateProg());
        values.put(STC_SCALE, cInfo.getScaleProg());
        values.put(STKR_PATH, cInfo.getSTKR_PATH());
        values.put(COLORTYPE, cInfo.getCOLORTYPE());
        values.put(STC_HUE, cInfo.getSTC_HUE());
        values.put(FIELD_ONE, cInfo.getFIELD_ONE());
        values.put(FIELD_TWO, cInfo.getFIELD_TWO());
        values.put(FIELD_THREE, cInfo.getFIELD_THREE());
        values.put(FIELD_FOUR, cInfo.getFIELD_FOUR());
        db.insert(COMPONENT_INFO2, null, values);
        db.close();
    }

    public void insertRedoTextRow(MailER_TextInfo tInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEMPLATE_ID, tInfo.getTEMPLATE_ID());
        values.put(TEXT, tInfo.getTEXT());
        values.put(FONT_NAME, tInfo.getFONT_NAME());
        values.put(TEXT_COLOR, tInfo.getTEXT_COLOR());
        values.put(TEXT_ALPHA, tInfo.getTEXT_ALPHA());
        values.put(SHADOW_COLOR, tInfo.getSHADOW_COLOR());
        values.put(SHADOW_PROG, tInfo.getSHADOW_PROG());
        values.put(BG_DRAWABLE, tInfo.getBG_DRAWABLE());
        values.put(BG_COLOR, tInfo.getBG_COLOR());
        values.put(BG_ALPHA, tInfo.getBG_ALPHA());
        values.put(POS_X, tInfo.getPOS_X());
        values.put(POS_Y, tInfo.getPOS_Y());
        values.put(WIDHT, tInfo.getWIDTH());
        values.put(HEIGHT, tInfo.getHEIGHT());
        values.put(ROTATION, tInfo.getROTATION());
        values.put(TYPE, tInfo.getTYPE());
        values.put(ORDER, tInfo.getORDER());
        values.put(XROTATEPROG, tInfo.getXRotateProg());
        values.put(YROTATEPROG, tInfo.getYRotateProg());
        values.put(ZROTATEPROG, tInfo.getZRotateProg());
        values.put(CURVEPROG, tInfo.getCurveRotateProg());
        values.put(FIELD_ONE, tInfo.getFIELD_ONE());
        values.put(FIELD_TWO, tInfo.getFIELD_TWO());
        values.put(FIELD_THREE, tInfo.getFIELD_THREE());
        values.put(FIELD_FOUR, tInfo.getFIELD_FOUR());
        db.insert(TEXT_INFO2, null, values);
        db.close();
    }

    public int getRedoLstTemplateId() {
        Cursor cursor = null;
        int TEMPLATE_ID = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String query = "SELECT  * FROM TEMPLATES2  ORDER BY  TEMPLATE_ID  DESC;";
            cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                TEMPLATE_ID = cursor.getInt(cursor.getColumnIndex("TEMPLATE_ID"));
            }
            return TEMPLATE_ID;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getRedoOperation(int templateID) {
        String operation;
        String query = "SELECT  * FROM TEMPLATES2 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation = cursor.getString(cursor.getColumnIndex(OPERATION));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public int getRedoChildPosition(int templateID) {
        int operation;
        String query = "SELECT  * FROM TEMPLATES2 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return -1;
        }
        do {
            operation = cursor.getInt(cursor.getColumnIndex(CHILD_POSITION));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public Point getRedoXYPosition(int templateID) {
        Point operation = new Point();
        String query = "SELECT  * FROM TEMPLATES2 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return operation;
        }
        do {
            operation.x = cursor.getInt(cursor.getColumnIndex(OLD_X));
            operation.y = cursor.getInt(cursor.getColumnIndex(OLD_Y));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }


    public String getRedoChildType(int templateID) {
        String operation;
        String query = "SELECT  * FROM TEMPLATES2 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation = cursor.getString(cursor.getColumnIndex(CHILD_TYPE));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public Point getRedoTextXYPosition(int templateID, int pos, String type) {
        Point operation = new Point();
        String query;
        if (type.equals("text")) {
            query = "SELECT  * FROM TEXT_INFO2 WHERE TEMPLATE_ID='" + templateID + "' AND ORDER_ ='" + pos + "'";
        } else {
            query = "SELECT  * FROM COMPONENT_INFO2 WHERE TEMPLATE_ID='" + templateID + "' AND ORDER_ ='" + pos + "'";
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation.x = cursor.getInt(cursor.getColumnIndex(POS_X));
            operation.y = cursor.getInt(cursor.getColumnIndex(POS_Y));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public Point getRedoStiXYPosition(int templateID, int pos) {
        Point operation = new Point();
        String query = "SELECT  * FROM TEXT_INFO1 WHERE TEMPLATE_ID='" + templateID + "' AND ORDER_ ='" + pos + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return null;
        }
        do {
            operation.x = cursor.getInt(cursor.getColumnIndex(POS_X));
            operation.y = cursor.getInt(cursor.getColumnIndex(POS_Y));
        } while (cursor.moveToNext());
        db.close();
        return operation;
    }

    public MailER_TemplateInfo getRedoTemplateDes(int templateID) {
        MailER_TemplateInfo templateList = new MailER_TemplateInfo();
        String query = "SELECT  * FROM TEMPLATES2 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return templateList;
        }
        do {
            templateList.setTEMPLATE_ID(cursor.getInt(0));
            templateList.setTHUMB_URI(cursor.getString(1));
            templateList.setFRAME_NAME(cursor.getString(2));
            templateList.setRATIO(cursor.getString(3));
            templateList.setPROFILE_TYPE(cursor.getString(4));
            templateList.setSEEK_VALUE(cursor.getString(5));
            templateList.setTYPE(cursor.getString(6));
            templateList.setTEMP_PATH(cursor.getString(7));
            templateList.setTEMPCOLOR(cursor.getString(8));
            templateList.setOVERLAY_NAME(cursor.getString(9));
            templateList.setOVERLAY_OPACITY(cursor.getInt(10));
            templateList.setOVERLAY_BLUR(cursor.getInt(11));
            templateList.setOVERLAY_POS(cursor.getInt(12));
        } while (cursor.moveToNext());
        db.close();
        Log.e("templateList size is", "" + templateList.toString());
        return templateList;
    }

    public ArrayList<MailER_TemplateInfo> getRedoTemplateListDes(String type) {
        ArrayList<MailER_TemplateInfo> templateList = new ArrayList<>();
        String query = "SELECT  * FROM TEMPLATES2 WHERE TYPE='" + type + "' ORDER BY " + TEMPLATE_ID + " DESC;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            Log.e("templateList size is", BuildConfig.FLAVOR + templateList.size());
            return templateList;
        }
        do {
            MailER_TemplateInfo values = new MailER_TemplateInfo();
            values.setTEMPLATE_ID(cursor.getInt(0));
            values.setTHUMB_URI(cursor.getString(1));
            values.setFRAME_NAME(cursor.getString(2));
            values.setRATIO(cursor.getString(3));
            values.setPROFILE_TYPE(cursor.getString(4));
            values.setSEEK_VALUE(cursor.getString(5));
            values.setTYPE(cursor.getString(6));
            values.setTEMP_PATH(cursor.getString(7));
            values.setTEMPCOLOR(cursor.getString(8));
            values.setOVERLAY_NAME(cursor.getString(9));
            values.setOVERLAY_OPACITY(cursor.getInt(10));
            values.setOVERLAY_BLUR(cursor.getInt(11));
            values.setOVERLAY_POS(cursor.getInt(12));
            templateList.add(values);
        } while (cursor.moveToNext());
        db.close();
        Log.e("templateList size is", "" + templateList.toString());
        return templateList;
    }

    public ArrayList<MailER_StickerInfo> getRedoComponentInfoList(int templateID, String shape) {
        ArrayList<MailER_StickerInfo> componentInfoList = new ArrayList<>();
        String query = "SELECT * FROM COMPONENT_INFO2 WHERE TEMPLATE_ID='" + templateID + "' AND " + TYPE + " = '" + shape + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return componentInfoList;
        }
        do {
            MailER_StickerInfo values = new MailER_StickerInfo();
            values.setCOMP_ID(cursor.getInt(0));
            values.setTEMPLATE_ID(cursor.getInt(1));
            values.setPOS_X(cursor.getFloat(2));
            values.setPOS_Y(cursor.getFloat(3));
            values.setWIDTH(cursor.getInt(4));
            values.setHEIGHT(cursor.getInt(5));
            values.setROTATION(cursor.getFloat(6));
            values.setY_ROTATION(cursor.getFloat(7));
            values.setRES_ID(cursor.getString(8));
            values.setTYPE(cursor.getString(9));
            values.setORDER(cursor.getInt(10));
            values.setSTC_COLOR(cursor.getInt(11));
            values.setSTC_OPACITY(cursor.getInt(12));
            values.setXRotateProg(cursor.getInt(13));
            values.setYRotateProg(cursor.getInt(14));
            values.setZRotateProg(cursor.getInt(15));
            values.setScaleProg(cursor.getInt(16));
            values.setSTKR_PATH(cursor.getString(17));
            values.setCOLORTYPE(cursor.getString(18));
            values.setSTC_HUE(cursor.getInt(19));
            values.setFIELD_ONE(cursor.getInt(20));
            values.setFIELD_TWO(cursor.getString(21));
            values.setFIELD_THREE(cursor.getString(22));
            values.setFIELD_FOUR(cursor.getString(23));
            componentInfoList.add(values);
        } while (cursor.moveToNext());
        db.close();
        return componentInfoList;
    }

    public ArrayList<MailER_TextInfo> getRedoTextInfoList(int templateID) {
        ArrayList<MailER_TextInfo> textInfoArrayList = new ArrayList<>();
        String query = "SELECT * FROM TEXT_INFO2 WHERE TEMPLATE_ID='" + templateID + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            db.close();
            return textInfoArrayList;
        }
        do {
            MailER_TextInfo values = new MailER_TextInfo();
            values.setTEXT_ID(cursor.getInt(0));
            values.setTEMPLATE_ID(cursor.getInt(1));
            values.setTEXT(cursor.getString(2));
            values.setFONT_NAME(cursor.getString(3));
            values.setTEXT_COLOR(cursor.getInt(4));
            values.setTEXT_ALPHA(cursor.getInt(5));
            values.setSHADOW_COLOR(cursor.getInt(6));
            values.setSHADOW_PROG(cursor.getInt(7));
            values.setBG_DRAWABLE(cursor.getString(8));
            values.setBG_COLOR(cursor.getInt(9));
            values.setBG_ALPHA(cursor.getInt(10));
            values.setPOS_X(cursor.getFloat(11));
            values.setPOS_Y(cursor.getFloat(12));
            values.setWIDTH(cursor.getInt(13));
            values.setHEIGHT(cursor.getInt(14));
            values.setROTATION(cursor.getFloat(15));
            values.setTYPE(cursor.getString(16));
            values.setORDER(cursor.getInt(17));
            values.setXRotateProg(cursor.getInt(18));
            values.setYRotateProg(cursor.getInt(19));
            values.setZRotateProg(cursor.getInt(20));
            values.setCurveRotateProg(cursor.getInt(21));
            values.setFIELD_ONE(cursor.getInt(22));
            values.setFIELD_TWO(cursor.getString(23));
            values.setFIELD_THREE(cursor.getString(24));
            values.setFIELD_FOUR(cursor.getString(25));
            textInfoArrayList.add(values);
        } while (cursor.moveToNext());
        db.close();
        return textInfoArrayList;
    }

    public boolean deleteRedoTemplateInfo(int templateID) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM TEMPLATES2 WHERE TEMPLATE_ID='" + templateID + "'");
            db.execSQL("DELETE FROM COMPONENT_INFO2 WHERE TEMPLATE_ID='" + templateID + "'");
            db.execSQL("DELETE FROM TEXT_INFO2 WHERE TEMPLATE_ID='" + templateID + "'");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRedoTemplateInfo() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM TEMPLATES2");
            db.execSQL("DELETE FROM COMPONENT_INFO2");
            db.execSQL("DELETE FROM TEXT_INFO2");
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
