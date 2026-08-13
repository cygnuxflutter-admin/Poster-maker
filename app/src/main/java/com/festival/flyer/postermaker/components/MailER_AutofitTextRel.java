package com.festival.flyer.postermaker.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.components.MailER_listener.MailER_MultiTouchListener;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;

import java.io.File;
import java.io.IOException;

public class MailER_AutofitTextRel extends RelativeLayout implements MailER_MultiTouchListener.TouchCallbackListener {
    public boolean isMultiTouchEnabled = true;
    double angle = 0.0d;
    int baseh;
    int basew;
    int basex;
    int basey;
    float cX = 0.0f;
    float cY = 0.0f;
    double dAngle = 0.0d;
    int height;
    int margl;
    int margt;
    float ratio;
    Animation scale;
    int sh = 1794;
    int sw = 1080;
    double tAngle = 0.0d;
    double vAngle = 0.0d;
    int width;
    Animation zoomInScale;
    Animation zoomOutScale;
    private ImageView background_iv;
    private int bgAlpha = 255;
    private int bgColor = 0;
    private String bgDrawable = "0";
    private ImageView border_iv;
    private Context context;
    private ImageView delete_iv;
    private String field_four = "";
    private int field_one = 0;
    private String field_three = "";
    private String field_two = "0,0";
    private String fontName = "";
    private GestureDetector gd = null;
    private int he;
    private boolean isBorderVisible = false;
    private int leftMargin = 0;
    private TouchEventListener listener = null;
    private int progress = 0;
    private float heightMain = 0.0f;
    private float widthMain = 0.0f;
    private OnTouchListener rTouchListener = new OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            MailER_AutofitTextRel rl = (MailER_AutofitTextRel) view.getParent();
            switch (event.getAction()) {
                case 0:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onRotateDown(MailER_AutofitTextRel.this);
                    }
                    Rect rect = new Rect();
                    ((View) view.getParent()).getGlobalVisibleRect(rect);
                    MailER_AutofitTextRel.this.cX = rect.exactCenterX();
                    MailER_AutofitTextRel.this.cY = rect.exactCenterY();
                    MailER_AutofitTextRel.this.vAngle = (double) ((View) view.getParent()).getRotation();
                    MailER_AutofitTextRel.this.tAngle = (Math.atan2((double) (MailER_AutofitTextRel.this.cY - event.getRawY()), (double) (MailER_AutofitTextRel.this.cX - event.getRawX())) * 180.0d) / 3.141592653589793d;
                    MailER_AutofitTextRel.this.dAngle = MailER_AutofitTextRel.this.vAngle - MailER_AutofitTextRel.this.tAngle;
                    break;
                case 1:
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onRotateUp(MailER_AutofitTextRel.this);
                        break;
                    }
                    break;
                case 2:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onRotateMove(MailER_AutofitTextRel.this);
                    }
                    MailER_AutofitTextRel.this.angle = (Math.atan2((double) (MailER_AutofitTextRel.this.cY - event.getRawY()), (double) (MailER_AutofitTextRel.this.cX - event.getRawX())) * 180.0d) / 3.141592653589793d;
                    ((View) view.getParent()).setRotation((float) (MailER_AutofitTextRel.this.angle + MailER_AutofitTextRel.this.dAngle));
                    ((View) view.getParent()).invalidate();
                    ((View) view.getParent()).requestLayout();
                    break;
            }
            return true;
        }
    };
    private ImageView rotate_iv;
    private float rotation;
    private int s;
    private ImageView scale_iv;
    private int shadowColor = 0;
    private int shadowProg = 0;
    private int tAlpha = 100;
    private int tColor = -16777216;
    private String text = "";
    private TextView resizeTextView;
    private int topMargin = 0;
    private int wi;
    private OnTouchListener mTouchListener1 = new OnTouchListener() {
        @SuppressLint({"NewApi"})
        public boolean onTouch(View view, MotionEvent event) {
            MailER_AutofitTextRel rl = (MailER_AutofitTextRel) view.getParent();
            int j = (int) event.getRawX();
            int i = (int) event.getRawY();
            LayoutParams layoutParams = (LayoutParams) MailER_AutofitTextRel.this.getLayoutParams();
            switch (event.getAction()) {
                case 0:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onScaleDown(MailER_AutofitTextRel.this);
                    }
                    MailER_AutofitTextRel.this.invalidate();
                    MailER_AutofitTextRel.this.basex = j;
                    MailER_AutofitTextRel.this.basey = i;
                    MailER_AutofitTextRel.this.basew = MailER_AutofitTextRel.this.getWidth();
                    MailER_AutofitTextRel.this.baseh = MailER_AutofitTextRel.this.getHeight();
                    MailER_AutofitTextRel.this.getLocationOnScreen(new int[2]);
                    MailER_AutofitTextRel.this.margl = layoutParams.leftMargin;
                    MailER_AutofitTextRel.this.margt = layoutParams.topMargin;
                    break;
                case 1:
                    MailER_AutofitTextRel.this.wi = MailER_AutofitTextRel.this.getLayoutParams().width;
                    MailER_AutofitTextRel.this.he = MailER_AutofitTextRel.this.getLayoutParams().height;
                    MailER_AutofitTextRel.this.leftMargin = ((LayoutParams) MailER_AutofitTextRel.this.getLayoutParams()).leftMargin;
                    MailER_AutofitTextRel.this.topMargin = ((LayoutParams) MailER_AutofitTextRel.this.getLayoutParams()).topMargin;
                    MailER_AutofitTextRel.this.field_two = String.valueOf(MailER_AutofitTextRel.this.leftMargin) + "," + String.valueOf(MailER_AutofitTextRel.this.topMargin);
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onScaleUp(MailER_AutofitTextRel.this);
                        break;
                    }
                    break;
                case 2:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onScaleMove(MailER_AutofitTextRel.this);
                    }
                    float f2 = (float) Math.toDegrees(Math.atan2((double) (i - MailER_AutofitTextRel.this.basey), (double) (j - MailER_AutofitTextRel.this.basex)));
                    float f1 = f2;
                    if (f2 < 0.0f) {
                        f1 = f2 + 360.0f;
                    }
                    j -= MailER_AutofitTextRel.this.basex;
                    int k = i - MailER_AutofitTextRel.this.basey;
                    i = (int) (Math.sqrt((double) ((j * j) + (k * k))) * Math.cos(Math.toRadians((double) (f1 - MailER_AutofitTextRel.this.getRotation()))));
                    j = (int) (Math.sqrt((double) ((i * i) + (k * k))) * Math.sin(Math.toRadians((double) (f1 - MailER_AutofitTextRel.this.getRotation()))));
                    k = (i * 2) + MailER_AutofitTextRel.this.basew;
                    int m = (j * 2) + MailER_AutofitTextRel.this.baseh;
                    if (k > MailER_AutofitTextRel.this.s) {
                        layoutParams.width = k;
                        layoutParams.leftMargin = MailER_AutofitTextRel.this.margl - i;
                    }
                    if (m > MailER_AutofitTextRel.this.s) {
                        layoutParams.height = m;
                        layoutParams.topMargin = MailER_AutofitTextRel.this.margt - j;
                    }
                    MailER_AutofitTextRel.this.setLayoutParams(layoutParams);
                    if (!MailER_AutofitTextRel.this.bgDrawable.equals("0")) {
                        MailER_AutofitTextRel.this.wi = MailER_AutofitTextRel.this.getLayoutParams().width;
                        MailER_AutofitTextRel.this.he = MailER_AutofitTextRel.this.getLayoutParams().height;
                        MailER_AutofitTextRel.this.setBgDrawable(MailER_AutofitTextRel.this.bgDrawable);
                        break;
                    }
                    break;
            }
            return true;
        }
    };
    private int xRotateProg = 0;
    private int yRotateProg = 0;
    private int zRotateProg = 0;

    public MailER_AutofitTextRel(Context context) {
        super(context);
        init(context);
    }

    public MailER_AutofitTextRel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MailER_AutofitTextRel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MailER_AutofitTextRel setOnTouchCallbackListener(TouchEventListener l) {
        this.listener = l;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(Context ctx) {
        this.context = ctx;
        Display display = ((Activity) this.context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.width = size.x;
        this.height = size.y;
        this.ratio = ((float) this.width) / ((float) this.height);
        this.resizeTextView = new MailER_AutoResizeTextView(this.context);
        this.scale_iv = new ImageView(this.context);
        this.border_iv = new ImageView(this.context);
        this.background_iv = new ImageView(this.context);
        this.delete_iv = new ImageView(this.context);
        this.rotate_iv = new ImageView(this.context);
        this.s = dpToPx(this.context, 30);
        this.wi = dpToPx(this.context, 200);
        this.he = dpToPx(this.context, 200);
        this.scale_iv.setImageResource(R.drawable.spawner_ic_sticker_scale);
        this.background_iv.setImageResource(0);
        this.rotate_iv.setImageResource(R.drawable.spawner_ic_sticker_rotate);
        this.delete_iv.setImageResource(R.drawable.spawner_ic_sticker_delete);
        LayoutParams lp = new LayoutParams(this.wi, this.he);
        LayoutParams slp = new LayoutParams(this.s, this.s);
        slp.addRule(12);
        slp.addRule(11);
        slp.setMargins(5, 5, 5, 5);
        LayoutParams elp = new LayoutParams(this.s, this.s);
        elp.addRule(12);
        elp.addRule(9);
        elp.setMargins(5, 5, 5, 5);
        LayoutParams tlp = new LayoutParams(-1, -1);
        tlp.setMargins(5, 5, 5, 5);
        tlp.addRule(17);
        LayoutParams dlp = new LayoutParams(this.s, this.s);
        dlp.addRule(10);
        dlp.addRule(9);
        dlp.setMargins(5, 5, 5, 5);
        LayoutParams blp = new LayoutParams(-1, -1);
        LayoutParams bglp = new LayoutParams(-1, -1);
        setLayoutParams(lp);
        setBackgroundResource(R.drawable.spawner_sticker_border_gray);
        addView(this.background_iv);
        this.background_iv.setLayoutParams(bglp);
        this.background_iv.setScaleType(ScaleType.FIT_XY);
        addView(this.border_iv);
        this.border_iv.setLayoutParams(blp);
        this.border_iv.setTag("border_iv");
        addView(this.resizeTextView);
        this.resizeTextView.setText(this.text);
        this.resizeTextView.setTextColor(this.tColor);
        this.resizeTextView.setTextSize(400.0f);
        this.resizeTextView.setLayoutParams(tlp);
        this.resizeTextView.setGravity(17);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(resizeTextView, 10, 1000, 1, TypedValue.COMPLEX_UNIT_DIP);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(resizeTextView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//        this.resizeTextView.setAutoSizeTextTypeUniformWithConfiguration(10,1000,1, TypedValue.COMPLEX_UNIT_PX);
        addView(this.delete_iv);
        this.delete_iv.setLayoutParams(dlp);

        this.delete_iv.setOnClickListener(v -> {
            final ViewGroup parent = (ViewGroup) MailER_AutofitTextRel.this.getParent();
            MailER_AutofitTextRel.this.zoomInScale.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    parent.removeView(MailER_AutofitTextRel.this);
                    if (MailER_AutofitTextRel.this.listener != null) {
                        MailER_AutofitTextRel.this.listener.onDelete(MailER_AutofitTextRel.this);
                    }
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            MailER_AutofitTextRel.this.resizeTextView.startAnimation(MailER_AutofitTextRel.this.zoomInScale);
            MailER_AutofitTextRel.this.background_iv.startAnimation(MailER_AutofitTextRel.this.zoomInScale);
            MailER_AutofitTextRel.this.setBorderVisibility(false);

        });

        addView(this.rotate_iv);
        this.rotate_iv.setLayoutParams(elp);
        this.rotate_iv.setOnTouchListener(this.rTouchListener);
        addView(this.scale_iv);
        this.scale_iv.setLayoutParams(slp);
        this.scale_iv.setTag("scale_iv");
        this.scale_iv.setOnTouchListener(this.mTouchListener1);
        this.rotation = getRotation();
        this.scale = AnimationUtils.loadAnimation(getContext(), R.anim.spawner_sticker_scale_anim);
        this.zoomOutScale = AnimationUtils.loadAnimation(getContext(), R.anim.spawner_sticker_scale_zoom_out);
        this.zoomInScale = AnimationUtils.loadAnimation(getContext(), R.anim.spawner_sticker_scale_zoom_in);
        initGD();
        this.isMultiTouchEnabled = setDefaultTouchListener(true);
    }

    public void deleteText() {
        final ViewGroup parent = (ViewGroup) MailER_AutofitTextRel.this.getParent();
        MailER_AutofitTextRel.this.zoomInScale.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                parent.removeView(MailER_AutofitTextRel.this);
                if (MailER_AutofitTextRel.this.listener != null) {
                    MailER_AutofitTextRel.this.listener.onDelete(MailER_AutofitTextRel.this);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        MailER_AutofitTextRel.this.resizeTextView.startAnimation(MailER_AutofitTextRel.this.zoomInScale);
        MailER_AutofitTextRel.this.background_iv.startAnimation(MailER_AutofitTextRel.this.zoomInScale);
        MailER_AutofitTextRel.this.setBorderVisibility(false);
    }

    public boolean setDefaultTouchListener(boolean enable) {
        if (enable) {
            setOnTouchListener(new MailER_MultiTouchListener().enableRotation(true).setOnTouchCallbackListener(this).setGestureListener(this.gd));
            return true;
        }
        setOnTouchListener(null);
        return false;
    }

    public boolean getBorderVisibility() {
        return this.isBorderVisible;
    }

    public void setBorderVisibility(boolean ch) {
        this.isBorderVisible = ch;
        if (!ch) {
            this.border_iv.setVisibility(GONE);
            this.scale_iv.setVisibility(GONE);
            this.delete_iv.setVisibility(GONE);
            this.rotate_iv.setVisibility(GONE);
            setBackgroundResource(0);
        } else if (this.border_iv.getVisibility() != VISIBLE) {
            this.border_iv.setVisibility(VISIBLE);
            this.scale_iv.setVisibility(VISIBLE);
            this.delete_iv.setVisibility(VISIBLE);
            this.rotate_iv.setVisibility(VISIBLE);
            setBackgroundResource(R.drawable.spawner_sticker_border_gray);
            this.resizeTextView.startAnimation(this.scale);
        }
    }

    private void setTextWithAnimation(String text, boolean animate) {
        this.resizeTextView.setText(text);
        this.text = text;
        if (animate) {
            this.resizeTextView.startAnimation(this.zoomOutScale);
        }
//        this.text_iv.startAnimation(this.zoomOutScale);
    }

    public String getText() {
        return this.resizeTextView.getText().toString();
    }

    public void setText(String text) {
        this.resizeTextView.setText(text);
        this.text = text;
    }

    public void setTextFont(String str) {
        try {

            AssetManager assets = this.context.getAssets();
            StringBuilder sb = new StringBuilder();
            sb.append("font/");
            sb.append(str);
            boolean b = MailER_FileUtils.isAssetExists(str, context);
            if (b) {
                this.resizeTextView.setTypeface(Typeface.createFromAsset(assets, sb.toString()));
                this.fontName = str;
                return;
            }

            if (new File(MailER_FileUtils.getFile(context, str)).exists()) {
                try {
                    this.resizeTextView.setTypeface(Typeface.createFromFile(new File(MailER_FileUtils.getFile(context, str))));
                    this.fontName = str;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return;
            }

            File file = new File(MailER_FileUtils.GetFontDir(this.context), str);
            if (file.exists()) {
                try {
                    this.resizeTextView.setTypeface(Typeface.createFromFile(file));
                    this.fontName = str;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }


        } catch (Exception unused) {
            Log.e("AutofitTextRel", "setTextFont: ");
        }
    }

    public void setTextStyle(int ii) {
//        if (ii == 0) {
//            text_iv.setTypeface(text_iv.getTypeface(), ii);
//        } else if (ii == 1) {
//            text_iv.setTypeface(text_iv.getTypeface(), Typeface.BOLD);
//        } else if (ii == 2) {
//            text_iv.setTypeface(text_iv.getTypeface(), Typeface.ITALIC);
//        } else if (ii == 3) {
//            text_iv.setTypeface(text_iv.getTypeface(), Typeface.BOLD_ITALIC);
//        }
        if (ii == 0) {
            setTextFont(fontName);
            return;
        }
        resizeTextView.setTypeface(resizeTextView.getTypeface(), ii);

    }

    public String getFontName() {
        return this.fontName;
    }

    public int getTextColor() {
        return this.tColor;
    }

    public void setTextColor(int color) {
        this.resizeTextView.setTextColor(color);
        this.tColor = color;
    }

    public int getTextAlpha() {
        return this.tAlpha;
    }

    public void setTextAlpha(int prog) {
        this.resizeTextView.setAlpha(((float) prog) / 100.0f);
        this.tAlpha = prog;
    }

    public int getTextShadowColor() {
        return this.shadowColor;
    }

    public void setTextShadowColor(int color) {
        this.shadowColor = color;
        this.resizeTextView.setShadowLayer((float) this.shadowProg, 0.0f, 0.0f, this.shadowColor);
    }

    public int getTextShadowProg() {
        return this.shadowProg;
    }

    public void setTextShadowProg(int prog) {
        this.shadowProg = prog;
        this.resizeTextView.setShadowLayer((float) this.shadowProg, 0.0f, 0.0f, this.shadowColor);
    }

    public String getBgDrawable() {
        return this.bgDrawable;
    }

    public void setBgDrawable(String path) {
        this.bgDrawable = path;
        this.bgColor = 0;
//        this.background_iv.setImageBitmap(getTiledBitmap(this.context, getResources().getIdentifier(did, "drawable", this.context.getPackageName()), this.wi, this.he));
        try {
            this.background_iv.setImageBitmap(getTiledBitmap(BitmapFactory.decodeStream(context.getAssets().open(path)), this.wi, this.he));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.background_iv.setBackgroundColor(this.bgColor);
    }

    public int getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(int c) {
        this.bgDrawable = "0";
        this.bgColor = c;
        this.background_iv.setImageBitmap(null);
        this.background_iv.setBackgroundColor(c);
    }

    public int getBgAlpha() {
        return this.bgAlpha;
    }

    public void setBgAlpha(int prog) {
        this.background_iv.setAlpha(((float) prog) / 255.0f);
        this.bgAlpha = prog;
    }

    public MailER_TextInfo getTextInfo() {
        MailER_TextInfo textInfo = new MailER_TextInfo();
        textInfo.setPOS_X(getX());
        textInfo.setPOS_Y(getY());
        textInfo.setWIDTH(this.wi);
        textInfo.setHEIGHT(this.he);
        textInfo.setTEXT(this.text);
        textInfo.setFONT_NAME(this.fontName);
        textInfo.setTEXT_COLOR(this.tColor);
        textInfo.setTEXT_ALPHA(this.tAlpha);
        textInfo.setSHADOW_COLOR(this.shadowColor);
        textInfo.setSHADOW_PROG(this.shadowProg);
        textInfo.setBG_COLOR(this.bgColor);
        textInfo.setBG_DRAWABLE(this.bgDrawable);
        textInfo.setBG_ALPHA(this.bgAlpha);
        textInfo.setROTATION(getRotation());
        textInfo.setXRotateProg(this.xRotateProg);
        textInfo.setYRotateProg(this.yRotateProg);
        textInfo.setZRotateProg(this.zRotateProg);
        textInfo.setCurveRotateProg(this.progress);
        textInfo.setFIELD_ONE(this.field_one);
        textInfo.setFIELD_TWO(this.field_two);
        textInfo.setFIELD_THREE(this.field_three);
        textInfo.setFIELD_FOUR(this.field_four);
        return textInfo;
    }

    public void setTextInfo(MailER_TextInfo textInfo, boolean animate) {
        Log.e("set Text value", "" + textInfo.getPOS_X() + " ," + textInfo.getPOS_Y() + " ," + textInfo.getWIDTH() + " ," + textInfo.getHEIGHT() + " ," + textInfo.getFIELD_TWO());
        this.wi = textInfo.getWIDTH();
        this.he = textInfo.getHEIGHT();
        this.text = textInfo.getTEXT();
        this.fontName = textInfo.getFONT_NAME();
        this.tColor = textInfo.getTEXT_COLOR();
        this.tAlpha = textInfo.getTEXT_ALPHA();
        this.shadowColor = textInfo.getSHADOW_COLOR();
        this.shadowProg = textInfo.getSHADOW_PROG();
        this.bgColor = textInfo.getBG_COLOR();
        this.bgDrawable = textInfo.getBG_DRAWABLE();
        this.bgAlpha = textInfo.getBG_ALPHA();
        this.rotation = textInfo.getROTATION();
        this.field_two = textInfo.getFIELD_TWO();
        setTextWithAnimation(this.text, animate);
        setTextFont(this.fontName);
        setTextColor(this.tColor);
        setTextAlpha(this.tAlpha);
        setTextShadowColor(this.shadowColor);
        setTextShadowProg(this.shadowProg);
        if (this.bgColor != 0) {
            setBgColor(this.bgColor);
        } else {
            this.background_iv.setBackgroundColor(0);
        }
        if (this.bgDrawable.equals("0")) {
            this.background_iv.setImageBitmap(null);
        } else {
            setBgDrawable(this.bgDrawable);
        }
        setBgAlpha(this.bgAlpha);
        setRotation(textInfo.getROTATION());
        if (this.field_two.equals("")) {
            getLayoutParams().width = this.wi;
            getLayoutParams().height = this.he;
            setX(textInfo.getPOS_X());
            setY(textInfo.getPOS_Y());
            return;
        }
        String[] parts = this.field_two.split(",");
        int leftMergin = Integer.parseInt(parts[0]);
        int topMergin = Integer.parseInt(parts[1]);
        ((LayoutParams) getLayoutParams()).leftMargin = leftMergin;
        ((LayoutParams) getLayoutParams()).topMargin = topMergin;
        getLayoutParams().width = this.wi;
        getLayoutParams().height = this.he;
        setX(textInfo.getPOS_X() + ((float) (leftMergin * -1)));
        setY(textInfo.getPOS_Y() + ((float) (topMergin * -1)));
    }

    public void optimize(float wr, float hr) {
        setX(getX() * wr);
        setY(getY() * hr);
        getLayoutParams().width = (int) (((float) this.wi) * wr);
        getLayoutParams().height = (int) (((float) this.he) * hr);
    }

    public void setMainLayoutWH(float wMLay, float hMLay) {
        this.widthMain = wMLay;
        this.heightMain = hMLay;
    }

    public float getMainWidth() {
        return this.widthMain;
    }

    public float getMainHeight() {
        return this.heightMain;
    }

    public void incrX() {
        setX(getX() + 1.0f);
    }

    public void decX() {
        setX(getX() - 1.0f);
    }

    public void incrY() {
        setY(getY() + 1.0f);
    }

    public void decY() {
        setY(getY() - 1.0f);
    }

    public int dpToPx(Context c, int dp) {
        float f = (float) dp;
        c.getResources();
        return (int) (f * Resources.getSystem().getDisplayMetrics().density);
    }

    private Bitmap getTiledBitmap(Context ctx, int resId, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(BitmapFactory.decodeResource(ctx.getResources(), resId, new Options()), TileMode.REPEAT, TileMode.REPEAT));
        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        new Canvas(b).drawRect(rect, paint);
        return b;
    }

    private Bitmap getTiledBitmap(Bitmap bitmap, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT));
        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        new Canvas(b).drawRect(rect, paint);
        return b;
    }

    private void initGD() {
        this.gd = new GestureDetector(this.context, new SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                if (MailER_AutofitTextRel.this.listener != null) {
                    MailER_AutofitTextRel.this.listener.onDoubleTap(MailER_AutofitTextRel.this);
                }
                return true;
            }

            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }

            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    public void onTouchCallback(View v) {
        if (this.listener != null) {
            this.listener.onTouchDown(v);
        }
    }

    public void onTouchUpCallback(View v) {
        if (this.listener != null) {
            this.listener.onTouchUp(v);
        }
    }

    public void onTouchMoveCallback(View v) {
        if (this.listener != null) {
            this.listener.onTouchMove(v);
        }
    }

    public void onMidX(View v) {
        if (this.listener != null) {
            this.listener.onMidX(v);
        }
    }

    public void onMidXY(View v) {
        if (this.listener != null) {
            this.listener.onMidXY(v);
        }
    }

    public void onMidY(View v) {
        if (this.listener != null) {
            this.listener.onMidY(v);
        }
    }

    public void onXY(View v) {
        if (this.listener != null) {
            this.listener.onXY(v);
        }
    }


    public float getNewX(float x) {
        return ((float) this.width) * (x / ((float) this.sw));
    }

    public float getNewY(float y) {
        return ((float) this.height) * (y / ((float) this.sh));
    }

    public interface TouchEventListener {

        void onDelete(View view);

        void onDoubleTap(View view);

        void onEdit(View view, Uri uri);

        void onRotateDown(View view);

        void onRotateMove(View view);

        void onRotateUp(View view);

        void onScaleDown(View view);

        void onScaleMove(View view);

        void onScaleUp(View view);

        void onTouchDown(View view);

        void onTouchMove(View view);

        void onTouchUp(View view);

        void onMidX(View view);

        void onMidXY(View view);

        void onMidY(View view);

        void onXY(View view);
    }
}
