package com.festival.flyer.postermaker.f;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.components.MailER_StickerInfo;
import com.festival.flyer.postermaker.f.MailER_listener.MailER_MultiTouchListener;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;

public class MailER_ResizableStickerView extends RelativeLayout implements MailER_MultiTouchListener.TouchCallbackListener {
    public static final String TAG = "ResizableStickerView";
    private static final int SELF_SIZE_DP = 30;
    public static String zdgdfaezd = "nia";
    public boolean isMultiTouchEnabled = true;
    public ImageView main_iv;
    double angle = 0.0d;
    int baseh;
    int basew;
    int basex;
    int basey;
    float cX = 0.0f;
    float cY = 0.0f;
    double dAngle = 0.0d;
    int margl;
    int margt;
    Animation scale;
    int screenHeight = 300;
    int screenWidth = 300;
    double tAngle = 0.0d;
    double vAngle = 0.0d;
    Animation zoomInScale;
    Animation zoomOutScale;
    private float heightMain = 0.0f;
    private float widthMain = 0.0f;
    private int alphaProg = 0;
    private ImageView border_iv;
    private Bitmap btmp = null;
    private double centerX;
    private double centerY;
    private String colorType = "colored";
    private Context context;
    private ImageView delete_iv;
    private String drawableId;
    private String field_four = "";
    private int field_one = 0;
    private String field_three = "";
    private String field_two = "0,0";
    private ImageView flip_iv;
    private int he;
    private int hueProg = 1;
    private int imgAlpha = 100;
    private int imgColor = 0;
    private boolean isBorderVisible = false;
    private boolean isColorFilterEnable = false;
    private boolean isSticker = true;
    private boolean isStrickerEditEnable = false;
    private int leftMargin = 0;
    private TouchEventListener listener = null;
    private OnTouchListener rTouchListener = new OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            MailER_ResizableStickerView rl = (MailER_ResizableStickerView) view.getParent();
            switch (event.getAction()) {
                case 0:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onRotateDown(MailER_ResizableStickerView.this);
                    }
                    Rect rect = new Rect();
                    ((View) view.getParent()).getGlobalVisibleRect(rect);
                    MailER_ResizableStickerView.this.cX = rect.exactCenterX();
                    MailER_ResizableStickerView.this.cY = rect.exactCenterY();
                    MailER_ResizableStickerView.this.vAngle = (double) ((View) view.getParent()).getRotation();
                    MailER_ResizableStickerView.this.tAngle = (Math.atan2((double) (MailER_ResizableStickerView.this.cY - event.getRawY()), (double) (MailER_ResizableStickerView.this.cX - event.getRawX())) * 180.0d) / 3.141592653589793d;
                    MailER_ResizableStickerView.this.dAngle = MailER_ResizableStickerView.this.vAngle - MailER_ResizableStickerView.this.tAngle;
                    break;
                case 1:
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onRotateUp(MailER_ResizableStickerView.this);
                        break;
                    }
                    break;
                case 2:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onRotateMove(MailER_ResizableStickerView.this);
                    }
                    MailER_ResizableStickerView.this.angle = (Math.atan2((double) (MailER_ResizableStickerView.this.cY - event.getRawY()), (double) (MailER_ResizableStickerView.this.cX - event.getRawX())) * 180.0d) / 3.141592653589793d;
                    ((View) view.getParent()).setRotation((float) (MailER_ResizableStickerView.this.angle + MailER_ResizableStickerView.this.dAngle));
                    ((View) view.getParent()).invalidate();
                    ((View) view.getParent()).requestLayout();
                    break;
            }
            return true;
        }
    };
    private Uri resUri = null;
    private ImageView rotate_iv;
    private float rotation;
    private int s;
    private int scaleRotateProg = 0;
    private ImageView scale_iv;
    private double scale_orgHeight = -1.0d;
    private double scale_orgWidth = -1.0d;
    private float scale_orgX = -1.0f;
    private float scale_orgY = -1.0f;
    private String stkr_path = "";
    private float this_orgX = -1.0f;
    private float this_orgY = -1.0f;
    private int topMargin = 0;
    private int wi;
    private OnTouchListener mTouchListener = new OnTouchListener() {
        @SuppressLint({"NewApi"})
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    MailER_ResizableStickerView.this.this_orgX = MailER_ResizableStickerView.this.getX();
                    MailER_ResizableStickerView.this.this_orgY = MailER_ResizableStickerView.this.getY();
                    MailER_ResizableStickerView.this.scale_orgX = event.getRawX();
                    MailER_ResizableStickerView.this.scale_orgY = event.getRawY();
                    MailER_ResizableStickerView.this.scale_orgWidth = (double) MailER_ResizableStickerView.this.getLayoutParams().width;
                    MailER_ResizableStickerView.this.scale_orgHeight = (double) MailER_ResizableStickerView.this.getLayoutParams().height;
                    MailER_ResizableStickerView.this.centerX = (double) ((((View) MailER_ResizableStickerView.this.getParent()).getX() + MailER_ResizableStickerView.this.getX()) + (((float) MailER_ResizableStickerView.this.getWidth()) / 2.0f));
                    int result = 0;
                    int resourceId = MailER_ResizableStickerView.this.getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (resourceId > 0) {
                        result = MailER_ResizableStickerView.this.getResources().getDimensionPixelSize(resourceId);
                    }
                    double statusBarHeight = (double) result;
                    MailER_ResizableStickerView.this.centerY = (((double) (((View) MailER_ResizableStickerView.this.getParent()).getY() + MailER_ResizableStickerView.this.getY())) + statusBarHeight) + ((double) (((float) MailER_ResizableStickerView.this.getHeight()) / 2.0f));
                    break;
                case 1:
                    MailER_ResizableStickerView.this.wi = MailER_ResizableStickerView.this.getLayoutParams().width;
                    MailER_ResizableStickerView.this.he = MailER_ResizableStickerView.this.getLayoutParams().height;
                    break;
                case 2:
                    double angle_diff = (Math.abs(Math.atan2((double) (event.getRawY() - MailER_ResizableStickerView.this.scale_orgY), (double) (event.getRawX() - MailER_ResizableStickerView.this.scale_orgX)) - Math.atan2(((double) MailER_ResizableStickerView.this.scale_orgY) - MailER_ResizableStickerView.this.centerY, ((double) MailER_ResizableStickerView.this.scale_orgX) - MailER_ResizableStickerView.this.centerX)) * 180.0d) / 3.141592653589793d;
                    Log.v(MailER_ResizableStickerView.TAG, "angle_diff: " + angle_diff);
                    double length1 = MailER_ResizableStickerView.this.getLength(MailER_ResizableStickerView.this.centerX, MailER_ResizableStickerView.this.centerY, (double) MailER_ResizableStickerView.this.scale_orgX, (double) MailER_ResizableStickerView.this.scale_orgY);
                    double length2 = MailER_ResizableStickerView.this.getLength(MailER_ResizableStickerView.this.centerX, MailER_ResizableStickerView.this.centerY, (double) event.getRawX(), (double) event.getRawY());
                    int size = MailER_ResizableStickerView.this.dpToPx(MailER_ResizableStickerView.this.getContext(), MailER_ResizableStickerView.SELF_SIZE_DP);
                    double offset;
                    LayoutParams layoutParams;
                    if (length2 > length1 && (angle_diff < 25.0d || Math.abs(angle_diff - 180.0d) < 25.0d)) {
                        offset = (double) Math.round(Math.max((double) Math.abs(event.getRawX() - MailER_ResizableStickerView.this.scale_orgX), (double) Math.abs(event.getRawY() - MailER_ResizableStickerView.this.scale_orgY)));
                        layoutParams = (LayoutParams) MailER_ResizableStickerView.this.getLayoutParams();
                        layoutParams.width = (int) (((double) layoutParams.width) + offset);
                        layoutParams = (LayoutParams) MailER_ResizableStickerView.this.getLayoutParams();
                        layoutParams.height = (int) (((double) layoutParams.height) + offset);
                    } else if (length2 < length1 && ((angle_diff < 25.0d || Math.abs(angle_diff - 180.0d) < 25.0d) && MailER_ResizableStickerView.this.getLayoutParams().width > size / 2 && MailER_ResizableStickerView.this.getLayoutParams().height > size / 2)) {
                        offset = (double) Math.round(Math.max((double) Math.abs(event.getRawX() - MailER_ResizableStickerView.this.scale_orgX), (double) Math.abs(event.getRawY() - MailER_ResizableStickerView.this.scale_orgY)));
                        layoutParams = (LayoutParams) MailER_ResizableStickerView.this.getLayoutParams();
                        layoutParams.width = (int) (((double) layoutParams.width) - offset);
                        layoutParams = (LayoutParams) MailER_ResizableStickerView.this.getLayoutParams();
                        layoutParams.height = (int) (((double) layoutParams.height) - offset);
                    }
                    MailER_ResizableStickerView.this.scale_orgX = event.getRawX();
                    MailER_ResizableStickerView.this.scale_orgY = event.getRawY();
                    MailER_ResizableStickerView.this.postInvalidate();
                    MailER_ResizableStickerView.this.requestLayout();
                    break;
            }
            return true;
        }
    };
    private OnTouchListener mTouchListener1 = new OnTouchListener() {
        @SuppressLint({"NewApi"})
        public boolean onTouch(View view, MotionEvent event) {
            MailER_ResizableStickerView rl = (MailER_ResizableStickerView) view.getParent();
            int j = (int) event.getRawX();
            int i = (int) event.getRawY();
            LayoutParams layoutParams = (LayoutParams) MailER_ResizableStickerView.this.getLayoutParams();
            switch (event.getAction()) {
                case 0:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onScaleDown(MailER_ResizableStickerView.this);
                    }
                    MailER_ResizableStickerView.this.invalidate();
                    MailER_ResizableStickerView.this.basex = j;
                    MailER_ResizableStickerView.this.basey = i;
                    MailER_ResizableStickerView.this.basew = MailER_ResizableStickerView.this.getWidth();
                    MailER_ResizableStickerView.this.baseh = MailER_ResizableStickerView.this.getHeight();
                    MailER_ResizableStickerView.this.getLocationOnScreen(new int[2]);
                    MailER_ResizableStickerView.this.margl = layoutParams.leftMargin;
                    MailER_ResizableStickerView.this.margt = layoutParams.topMargin;
                    break;
                case 1:
                    MailER_ResizableStickerView.this.wi = MailER_ResizableStickerView.this.getLayoutParams().width;
                    MailER_ResizableStickerView.this.he = MailER_ResizableStickerView.this.getLayoutParams().height;
                    MailER_ResizableStickerView.this.leftMargin = ((LayoutParams) MailER_ResizableStickerView.this.getLayoutParams()).leftMargin;
                    MailER_ResizableStickerView.this.topMargin = ((LayoutParams) MailER_ResizableStickerView.this.getLayoutParams()).topMargin;
                    MailER_ResizableStickerView.this.field_two = String.valueOf(MailER_ResizableStickerView.this.leftMargin) + "," + String.valueOf(MailER_ResizableStickerView.this.topMargin);
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onScaleUp(MailER_ResizableStickerView.this);
                        break;
                    }
                    break;
                case 2:
                    if (rl != null) {
                        rl.requestDisallowInterceptTouchEvent(true);
                    }
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onScaleMove(MailER_ResizableStickerView.this);
                    }
                    float f2 = (float) Math.toDegrees(Math.atan2((double) (i - MailER_ResizableStickerView.this.basey), (double) (j - MailER_ResizableStickerView.this.basex)));
                    float f1 = f2;
                    if (f2 < 0.0f) {
                        f1 = f2 + 360.0f;
                    }
                    j -= MailER_ResizableStickerView.this.basex;
                    int k = i - MailER_ResizableStickerView.this.basey;
                    i = (int) (Math.sqrt((double) ((j * j) + (k * k))) * Math.cos(Math.toRadians((double) (f1 - MailER_ResizableStickerView.this.getRotation()))));
                    j = (int) (Math.sqrt((double) ((i * i) + (k * k))) * Math.sin(Math.toRadians((double) (f1 - MailER_ResizableStickerView.this.getRotation()))));
                    k = (i * 2) + MailER_ResizableStickerView.this.basew;
                    int m = (j * 2) + MailER_ResizableStickerView.this.baseh;
                    if (k > MailER_ResizableStickerView.this.s) {
                        layoutParams.width = k;
                        layoutParams.leftMargin = MailER_ResizableStickerView.this.margl - i;
                    }
                    if (m > MailER_ResizableStickerView.this.s) {
                        layoutParams.height = m;
                        layoutParams.topMargin = MailER_ResizableStickerView.this.margt - j;
                    }
                    MailER_ResizableStickerView.this.setLayoutParams(layoutParams);
                    MailER_ResizableStickerView.this.performLongClick();
                    break;
            }
            return true;
        }
    };
    private int xRotateProg = 0;
    private int yRotateProg = 0;
    private float yRotation;
    private int zRotateProg = 0;

    public MailER_ResizableStickerView(Context context) {
        super(context);
        init(context);
    }

    public MailER_ResizableStickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MailER_ResizableStickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MailER_ResizableStickerView setOnTouchCallbackListener(TouchEventListener l) {
        this.listener = l;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(Context ctx) {
        this.context = ctx;
        this.main_iv = new ImageView(this.context);
        this.scale_iv = new ImageView(this.context);
        this.border_iv = new ImageView(this.context);
        this.flip_iv = new ImageView(this.context);
        this.rotate_iv = new ImageView(this.context);
        this.delete_iv = new ImageView(this.context);
        this.s = dpToPx(this.context, 25);
        this.wi = dpToPx(this.context, 200);
        this.he = dpToPx(this.context, 200);
        this.scale_iv.setImageResource(R.drawable.spawner_ic_sticker_scale);
        this.border_iv.setImageResource(R.drawable.spawner_sticker_border_gray);
        this.rotate_iv.setImageResource(R.drawable.spawner_ic_sticker_rotate);
        this.delete_iv.setImageResource(R.drawable.spawner_ic_sticker_delete);
        LayoutParams lp = new LayoutParams(this.wi, this.he);
        LayoutParams mlp = new LayoutParams(-1, -1);
        mlp.setMargins(5, 5, 5, 5);
        mlp.addRule(17);
        LayoutParams slp = new LayoutParams(this.s, this.s);
        slp.addRule(12);
        slp.addRule(11);
        slp.setMargins(5, 5, 5, 5);
        LayoutParams flp = new LayoutParams(this.s, this.s);
        flp.addRule(10);
        flp.addRule(11);
        flp.setMargins(5, 5, 5, 5);
        LayoutParams elp = new LayoutParams(this.s, this.s);
        elp.addRule(12);
        elp.addRule(9);
        elp.setMargins(5, 5, 5, 5);
        LayoutParams dlp = new LayoutParams(this.s, this.s);
        dlp.addRule(10);
        dlp.addRule(9);
        dlp.setMargins(5, 5, 5, 5);
        LayoutParams blp = new LayoutParams(-1, -1);
        setLayoutParams(lp);
        setBackgroundResource(R.drawable.spawner_sticker_border_gray);
        addView(this.border_iv);
        this.border_iv.setLayoutParams(blp);
        this.border_iv.setScaleType(ScaleType.FIT_XY);
        this.border_iv.setTag("border_iv");
        addView(this.main_iv);
        this.main_iv.setLayoutParams(mlp);
        addView(this.flip_iv);
        this.flip_iv.setLayoutParams(flp);
        this.flip_iv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                float f = -180.0f;
                ImageView imageView = MailER_ResizableStickerView.this.main_iv;
                if (MailER_ResizableStickerView.this.main_iv.getRotationY() == -180.0f) {
                    f = 0.0f;
                }
                imageView.setRotationY(f);
                MailER_ResizableStickerView.this.main_iv.invalidate();
                MailER_ResizableStickerView.this.requestLayout();
            }
        });
        addView(this.rotate_iv);
        this.rotate_iv.setLayoutParams(elp);
        this.rotate_iv.setOnTouchListener(this.rTouchListener);
        addView(this.delete_iv);
        this.delete_iv.setLayoutParams(dlp);

//        this.delete_iv.setOnTouchListener((v, event) -> {
//            if(event.getAction() == MotionEvent.ACTION_DOWN){
//                if (ResizableStickerView.this.listener != null) {
//                    Log.d("gsdtseyesryg", "ACTION_DOWN: ");
//                    ResizableStickerView.this.listener.beforeDelete(ResizableStickerView.this);
//                }
//            }
//            if(event.getAction() == MotionEvent.ACTION_UP){
//                Log.d("gsdtseyesryg", "ACTION_UP: ");
//                final ViewGroup parent = (ViewGroup) ResizableStickerView.this.getParent();
//                ResizableStickerView.this.zoomInScale.setAnimationListener(new AnimationListener() {
//                    public void onAnimationStart(Animation animation) {
//                    }
//
//                    public void onAnimationEnd(Animation animation) {
//                        parent.removeView(ResizableStickerView.this);
//                    }
//
//                    public void onAnimationRepeat(Animation animation) {
//                    }
//                });
//                ResizableStickerView.this.main_iv.startAnimation(ResizableStickerView.this.zoomInScale);
//                ResizableStickerView.this.setBorderVisibility(false);
//                if (ResizableStickerView.this.listener != null) {
//                    Log.d("gsdtseyesryg", "ACTION_UP2: ");
//                    ResizableStickerView.this.listener.onDelete(ResizableStickerView.this);
//                }
//            }
//            return false;
//        });

        this.delete_iv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//               final PrefManager prefManager=new PrefManager(context);
//
////                Toast.makeText(context,PosterActivity.stkrno.size()+"",Toast.LENGTH_LONG).show();
//                for(int i=0;i<PosterActivity.stkrno.size();i++)
//                {
//                    if(PosterActivity.stkrno.get(i).matches(drawableId))
//                    {
//                        PosterActivity.stkrno.remove(i);
//                        if (PosterActivity.stkrno.size()==0) {
//                            if (!prefManager.getstkrlocked()) {
//                                prefManager.setstkrlocked(true);
//                                prefManager.setstkrlockedfinal(false);
//                            }
//                        }
//                    }
//                }
//
//                Handler h=new Handler();
//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (prefManager.getstkrlocked()) {
//                            PosterActivity.img_oK.setImageResource(R.drawable.ic_done);
//                            PosterActivity.img_oK.setBackgroundDrawable(null);
//                            PosterActivity.img_oK.setColorFilter(Color.BLACK);
//                        }
//                    }
//                },1000);

//                if (ResizableStickerView.this.listener != null) {
//                    Log.d("gsdtseyesryg", "ACTION_DOWN: ");
//                    ResizableStickerView.this.listener.beforeDelete(ResizableStickerView.this);
//                }

                new Handler().postDelayed(() -> {
                    final ViewGroup parent = (ViewGroup) MailER_ResizableStickerView.this.getParent();
                    MailER_ResizableStickerView.this.zoomInScale.setAnimationListener(new AnimationListener() {
                        public void onAnimationStart(Animation animation) {
                        }

                        public void onAnimationEnd(Animation animation) {
                            parent.removeView(MailER_ResizableStickerView.this);
//                            new Handler().postDelayed(() -> {
//                                if (ResizableStickerView.this.listener != null) {
//                                    Log.d("gsdtseyesryg", "ACTION_UP2: ");
//                                    ResizableStickerView.this.listener.beforeDelete(ResizableStickerView.this);
//                                }
//                            }, 200);

                            if (MailER_ResizableStickerView.this.listener != null) {
                                Log.d("gsdtseyesryg", "ACTION_UP2: ");
                                MailER_ResizableStickerView.this.listener.onDelete(MailER_ResizableStickerView.this);
                            }

                        }

                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    MailER_ResizableStickerView.this.main_iv.startAnimation(MailER_ResizableStickerView.this.zoomInScale);
                    MailER_ResizableStickerView.this.setBorderVisibility(false);

                }, 1);
            }
        });

        addView(this.scale_iv);
        this.scale_iv.setLayoutParams(slp);
        this.scale_iv.setOnTouchListener(this.mTouchListener1);
        this.scale_iv.setTag("scale_iv");
        this.rotation = getRotation();
        this.scale = AnimationUtils.loadAnimation(getContext(), R.anim.spawner_sticker_scale_anim);
        this.zoomOutScale = AnimationUtils.loadAnimation(getContext(), R.anim.spawner_sticker_scale_zoom_out);
        this.zoomInScale = AnimationUtils.loadAnimation(getContext(), R.anim.spawner_sticker_scale_zoom_in);
        this.isMultiTouchEnabled = setDefaultTouchListener(true);
    }

    public void deleteSticker() {
        new Handler().postDelayed(() -> {
            final ViewGroup parent = (ViewGroup) MailER_ResizableStickerView.this.getParent();
            MailER_ResizableStickerView.this.zoomInScale.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    parent.removeView(MailER_ResizableStickerView.this);
                    if (MailER_ResizableStickerView.this.listener != null) {
                        MailER_ResizableStickerView.this.listener.onDelete(MailER_ResizableStickerView.this);
                    }
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            MailER_ResizableStickerView.this.main_iv.startAnimation(MailER_ResizableStickerView.this.zoomInScale);
            MailER_ResizableStickerView.this.setBorderVisibility(false);
        }, 1);
    }

    public boolean setDefaultTouchListener(boolean enable) {
        if (enable) {
            setOnTouchListener(new MailER_MultiTouchListener().enableRotation(true).setOnTouchCallbackListener(this));
            return true;
        }
        setOnTouchListener(null);
        return false;
    }

    public void setBorderVisibility(boolean ch) {
        this.isBorderVisible = ch;
        if (!ch) {
            this.border_iv.setVisibility(GONE);
            this.scale_iv.setVisibility(GONE);
            this.flip_iv.setVisibility(GONE);
            this.rotate_iv.setVisibility(GONE);
            this.delete_iv.setVisibility(GONE);
            setBackgroundResource(0);
            if (this.isColorFilterEnable) {
                this.main_iv.setColorFilter(Color.parseColor("#303828"));
            }
        } else if (this.border_iv.getVisibility() != VISIBLE) {
            this.border_iv.setVisibility(VISIBLE);
            this.scale_iv.setVisibility(VISIBLE);
            this.flip_iv.setVisibility(VISIBLE);
            this.rotate_iv.setVisibility(VISIBLE);
            this.delete_iv.setVisibility(VISIBLE);
            setBackgroundResource(R.drawable.spawner_sticker_border_gray);
            this.main_iv.startAnimation(this.scale);
        }
    }

    public boolean getBorderVisbilty() {
        return this.isBorderVisible;
    }

    public void opecitySticker(int process) {
        try {
            this.main_iv.setAlpha(((float) process) / 100.0f);
            this.imgAlpha = process;
        } catch (Exception e) {
        }
    }

    public int getHueProg() {
        return this.hueProg;
    }

    public void setHueProg(int hueProg) {
        this.hueProg = hueProg;
        if (this.hueProg == 1) {
            return;
        }
        if (hueProg == getResources().getColor(R.color.transparent)) {
            main_iv.setColorFilter(null);
            return;
        }
        int red = Color.red(hueProg);
        int green = Color.green(hueProg);
        int blue = Color.blue(hueProg);
        final int semiTransparent = Color.argb(155, red, green, blue);
        main_iv.setColorFilter(semiTransparent, PorterDuff.Mode.SRC_ATOP);
    }

    public String getColorType() {
        return this.colorType;
    }

    public void setColorType(String colorType) {
        this.colorType = colorType;
    }

    public int getAlphaProg() {
        return this.imgAlpha;
    }

    public void setAlphaProg(int alphaProg) {
        this.alphaProg = alphaProg;
        opecitySticker(alphaProg);
    }

    public int getColor() {
        return this.imgColor;
    }

    public void setColor(int color) {
        try {
            this.main_iv.setColorFilter(color);
            this.imgColor = color;
        } catch (Exception e) {
        }
    }

    public void setBgDrawable(String redId, boolean animate) {
        Glide.with(this.context).load(Integer.valueOf(getResources().getIdentifier(redId, "drawable", this.context.getPackageName()))).dontAnimate().placeholder(R.drawable.spawner_no_image).error(R.drawable.spawner_no_image).into(this.main_iv);
        this.drawableId = redId;
        if (animate) {
            this.main_iv.startAnimation(this.zoomOutScale);
        }
    }

    public Uri getMainImageUri() {
        return this.resUri;
    }

    public void setMainImageUri(Uri uri) {
        this.resUri = uri;
        this.main_iv.setImageURI(this.resUri);
    }

    public void setStrPath(String stkr_path1, boolean animate) {
        btmp = BitmapFactory.decodeFile(stkr_path1);
//        btmp = BitmapUtils.resampleImage(stkr_path1, this.screenWidth > this.screenHeight ? this.screenWidth : this.screenHeight);
        this.main_iv.setImageBitmap(btmp);
        this.stkr_path = stkr_path1;
        if (animate) {
            this.main_iv.startAnimation(this.zoomOutScale);
        }
//        this.main_iv.startAnimation(this.zoomOutScale);
    }

    public Bitmap getMainImageBitmap() {
        return this.btmp;
    }

    public void setMainImageBitmap(Bitmap bit) {
        this.main_iv.setImageBitmap(bit);
    }

    public void optimize(float wr, float hr) {
        setX(getX() * wr);
        setY(getY() * hr);
        int w = (int) (((float) this.wi) * wr);
        int h = (int) (((float) this.he) * hr);
        getLayoutParams().width = w;
        getLayoutParams().height = h;
    }

    public void optimizeScreen(float wr, float hr) {
        this.screenHeight = (int) hr;
        this.screenWidth = (int) wr;
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

    public MailER_StickerInfo getComponentInfo() {
//        if (this.btmp != null) {
//            this.stkr_path = saveBitmapObject1(this.btmp);
//        }
        MailER_StickerInfo ci = new MailER_StickerInfo();
        ci.setPOS_X(getX());
        ci.setPOS_Y(getY());
        ci.setWIDTH(this.wi);
        ci.setHEIGHT(this.he);
        ci.setRES_ID(this.drawableId);
        ci.setSTC_COLOR(this.imgColor);
        ci.setRES_URI(this.resUri);
        ci.setSTC_OPACITY(this.imgAlpha);
        ci.setCOLORTYPE(this.colorType);
        ci.setBITMAP(this.btmp);
        ci.setROTATION(getRotation());
        ci.setY_ROTATION(this.main_iv.getRotationY());
        ci.setXRotateProg(this.xRotateProg);
        ci.setYRotateProg(this.yRotateProg);
        ci.setZRotateProg(this.zRotateProg);
        ci.setScaleProg(this.scaleRotateProg);
        ci.setSTKR_PATH(this.stkr_path);
        ci.setSTC_HUE(this.hueProg);
        ci.setFIELD_ONE(this.field_one);
        ci.setFIELD_TWO(this.field_two);
        ci.setFIELD_THREE(this.field_three);
        ci.setFIELD_FOUR(this.field_four);
        return ci;
    }

    public void setComponentInfo(MailER_StickerInfo ci, boolean animate) {
        this.wi = ci.getWIDTH();
        this.he = ci.getHEIGHT();
        this.drawableId = ci.getRES_ID();
        this.resUri = ci.getRES_URI();
        this.btmp = ci.getBITMAP();
        this.rotation = ci.getROTATION();
        this.imgColor = ci.getSTC_COLOR();
        this.yRotation = ci.getY_ROTATION();
        this.imgAlpha = ci.getSTC_OPACITY();
        this.stkr_path = ci.getSTKR_PATH();
        this.colorType = ci.getCOLORTYPE();
        this.hueProg = ci.getSTC_HUE();
        this.field_two = ci.getFIELD_TWO();
        if (!this.stkr_path.equals("")) {
            setStrPath(this.stkr_path, animate);
        } else if (this.drawableId.equals("")) {
            this.main_iv.setImageBitmap(this.btmp);
            if (animate) {
                this.main_iv.startAnimation(this.zoomOutScale);
            }
        } else {
            setBgDrawable(this.drawableId, animate);
        }
        if (this.colorType.equals("white")) {
            setColor(this.imgColor);
        } else {
            setHueProg(this.hueProg);
        }
        setRotation(this.rotation);
        opecitySticker(this.imgAlpha);
        if (this.field_two.equals("")) {
            getLayoutParams().width = this.wi;
            getLayoutParams().height = this.he;
            setX(ci.getPOS_X());
            setY(ci.getPOS_Y());
        } else {
            String[] parts = this.field_two.split(",");
            int leftMergin = Integer.parseInt(parts[0]);
            int topMergin = Integer.parseInt(parts[1]);
            ((LayoutParams) getLayoutParams()).leftMargin = leftMergin;
            ((LayoutParams) getLayoutParams()).topMargin = topMergin;
            getLayoutParams().width = this.wi;
            getLayoutParams().height = this.he;
            setX(ci.getPOS_X() + ((float) (leftMergin * -1)));
            setY(ci.getPOS_Y() + ((float) (topMergin * -1)));
        }
        if (ci.getTYPE().equals("SHAPE")) {
            this.flip_iv.setVisibility(GONE);
            this.isSticker = false;
        }
        if (ci.getTYPE().equals("STICKER")) {
            this.flip_iv.setVisibility(VISIBLE);
            this.isSticker = true;
        }
        this.main_iv.setRotationY(this.yRotation);
    }

    private String saveBitmapObject1(Bitmap bitmap) {
        String temp_path = "";
        File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), ".Thumbnail Maker Stickers/category1");
        myDir.mkdirs();
        File file1 = new File(myDir, "raw1-" + System.currentTimeMillis() + ".png");
        temp_path = file1.getAbsolutePath();
        if (file1.exists()) {
            file1.delete();
        }
        try {
            FileOutputStream ostream = new FileOutputStream(file1);
            bitmap.compress(CompressFormat.PNG, 100, ostream);
            return temp_path;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("testing", "Exception" + e.getMessage());
            return "";
        }
    }

    public int dpToPx(Context c, int dp) {
        float f = (float) dp;
        c.getResources();
        return (int) (f * Resources.getSystem().getDisplayMetrics().density);
    }

    private double getLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(y2 - y1, 2.0d) + Math.pow(x2 - x1, 2.0d));
    }

    public void enableColorFilter(boolean b) {
        this.isColorFilterEnable = b;
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

    public interface TouchEventListener {

        void onDelete(View view);

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
