package com.festival.flyer.postermaker.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.festival.flyer.postermaker.adManager.MailER_InterstitialAdManager;
import com.festival.flyer.postermaker.adManager.MailER_LoadAds;
import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager;
import com.festival.flyer.postermaker.components.MailER_AutofitTextRel;
import com.festival.flyer.postermaker.components.MailER_ResizableStickerView;
import com.festival.flyer.postermaker.components.MailER_StickerInfo;
import com.festival.flyer.postermaker.components.MailER_TemplateInfo;
import com.festival.flyer.postermaker.components.MailER_TextInfo;
import com.festival.flyer.postermaker.controller.MailER_EffectController;
import com.festival.flyer.postermaker.controller.MailER_StickerController;
import com.festival.flyer.postermaker.controller.MailER_TextController;
import com.festival.flyer.postermaker.fragment.MailER_ListFragment;
import com.festival.flyer.postermaker.fragment.MailER_StickerViewPagerAdapter;
import com.festival.flyer.postermaker.fragment.MailER_StickersFragment;
import com.festival.flyer.postermaker.model.MailER_StickerModel;
import com.festival.flyer.postermaker.model.MailER_TemplateModel;
import com.festival.flyer.postermaker.model.MailER_TextModel;
import com.festival.flyer.postermaker.utils.MailER_BitmapUtils;
import com.festival.flyer.postermaker.utils.MailER_DatabaseHandler;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_NetworkUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.utils.MailER_SizeUtils;
import com.festival.flyer.postermaker.utils.MailER_TimeUtils;
import com.festival.flyer.postermaker.view.MailER_AspectRatioFrameLayout;
import com.festival.flyer.postermaker.view.MailER_AutoResizeEditText;
import com.festival.flyer.postermaker.view.MailER_GridLine;
import com.festival.flyer.postermaker.view.MailER_PagerSlidingTabStrip;
import com.github.florent37.viewanimator.ViewAnimator;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MailER_PosterEditActivity extends AppCompatActivity implements View.OnClickListener, MailER_StickersFragment.GetSnapListener, MailER_AutofitTextRel.TouchEventListener, MailER_ResizableStickerView.TouchEventListener {

    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout txt_stkr_rel;
    private final int INTENT_FOR_BG = 0x2020;
    private final int SELECT_PICTURE_FROM_CAMERA = 905;
    private final int SELECT_PICTURE_FROM_GALLERY = 907;
    private final float wr = 1.0f;
    private final float hr = 1.0f;
    private MailER_AspectRatioFrameLayout center_rel;
    private RelativeLayout main_rel, save_ly;
    private RelativeLayout btn_layControls;
    private RelativeLayout lay_sticker, lay_TextMain, lay_StkrMain, lay_effects, temp_ll;
    private FrameLayout lay_container;
    private ImageView background_img, trans_img, img_sticker, img_effect, btn_watermark, btn_watermark_remove;
    RelativeLayout rl_watermark;
    private TextView txtHeader;
    private MailER_GridLine guideline;
    private ArrayList<MailER_TemplateModel> posterCos = new ArrayList<>();
    private ArrayList<MailER_StickerModel> sticker_info = new ArrayList<>();
    private ArrayList<MailER_TextModel> text_info = new ArrayList<>();
    private HashMap<Integer, Object> txtShapeList;
    private HashMap<Integer, Object> temp_txtShapeList = new HashMap<>();
    private int screenWidth, screenHeight;
    private MailER_TextController textController;
    private MailER_StickerController stickerController;
    private File camera_file = null;
    private MailER_ListFragment listFragment;
    private MailER_EffectController effectController;
    private Bitmap bg_bitmap;
    private int template_id = 0;
    private String path = MailER_TimeUtils.getTimeStamp();
    private MaterialDialog mMaterialDialog;
    private ImageView btn_undo;
    private ImageView btn_redo;
    private ProgressBar progress_undo_redo;
    private int undo_progress = 0;
    private int redo_progress = 0;
    private String bg_path;
    private int view_width, view_height;
    private float view_x, view_y, view_rotation;
    private int mainWidth, mainHeight;
    private String template_type;
    private boolean first_click = false;
    private boolean notifyRedoData = false;
    private boolean failedNotifyRedoData = false;
    private ScrollView lay_scroll;
    private float parentY;
    private int distanceScroll;
    private String permission_type = "";
    private String savePath = null;
    private String savePathWithWaterMark = null;

    private boolean isRewarded = false;
    private com.afollestad.materialdialogs.MaterialDialog progressDialog;
    private MailER_PreferenceClass prefManager;
    private String effect_name = "", temp_effect_name;
    private boolean premiumPoster = false;
    private String rewardType = "";
    private float old_x, old_y;
    private float new_x, new_y;
    private int child_position;
    private boolean local_permission = false;
    //    private RelativeLayout  rl_ad;
    public static Uri imguri;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.spawner_activity_poster_edit);

        // Preload reward ad when user enters edit screen (for watermark removal)


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (lay_sticker.getVisibility() == View.VISIBLE) {
                    hideStickerControl();
                    return;
                }
                leaveDialog();
            }
        });

        findById();

        main_rel.setOnTouchListener((v, event) -> {
            hideEffectControl();
            hideAllControls();
            hideListControl();
            removeScroll();
            return false;
        });

        lay_scroll.setOnTouchListener((v, event) -> true);

        boolean loadUserFrame = getIntent().getBooleanExtra("loadUserFrame", true);

        assert template_type != null;
        if (template_type.equals("MY_TEMP")) {
            String bg_image = getIntent().getStringExtra("bg_path");
            if (loadUserFrame) {
                new setPosterAsync(true, Uri.parse(bg_image)).execute();
            } else {
                new setPosterAsync(false, null).execute();
            }
        } else {
            int position = getIntent().getIntExtra("position", 0);
            new LoadUserPosterAsync(position).execute();
        }
        //  assignPermission();

    }

    /*private void assignPermission() {
     *//*if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
        }*//*
     *//*if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(PosterEditActivity.this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(PosterEditActivity.this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }*//*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PERMISSION_GRANTED ||
                    checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    local_permission = true;

                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
                } else {
                    //MaterialDialogUtils.getInstance().PermissionDialog(this);
                }
            }
        }
    }*/




/*    private boolean checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }*/

    @SuppressLint("ClickableViewAccessibility")
    private void findById() {
     /*   MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });*/


        lay_scroll = findViewById(R.id.lay_scroll);
        txtHeader = findViewById(R.id.txtHeader);
        save_ly = findViewById(R.id.save_ly);

        center_rel = findViewById(R.id.center_rel);
        main_rel = findViewById(R.id.main_rel);
        txt_stkr_rel = findViewById(R.id.txt_stkr_rel);
        guideline = findViewById(R.id.guidelines);
        background_img = findViewById(R.id.background_img);
        trans_img = findViewById(R.id.trans_img);

        lay_sticker = findViewById(R.id.lay_sticker);
        img_sticker = findViewById(R.id.img_sticker);

        lay_TextMain = findViewById(R.id.lay_textEdit);
        lay_StkrMain = findViewById(R.id.lay_StkrMain);
        temp_ll = findViewById(R.id.temp_ly);

        lay_effects = findViewById(R.id.lay_effects);
        img_effect = findViewById(R.id.img_effect);

        btn_watermark = findViewById(R.id.btn_watermark);
        btn_watermark_remove = findViewById(R.id.btn_watermark_remove);
        rl_watermark = findViewById(R.id.rl_watermark);

        lay_container = findViewById(R.id.lay_container);

        ImageView btn_reset = findViewById(R.id.btn_reset);
        btn_undo = findViewById(R.id.btn_undo);
        btn_redo = findViewById(R.id.btn_redo);
        progress_undo_redo = findViewById(R.id.progress_undo_redo);

        ImageView btn_save_template = findViewById(R.id.btn_save_template);
        ImageView btn_save_poster = findViewById(R.id.btn_save_poster);

        LinearLayout add_text = findViewById(R.id.add_text);
        LinearLayout add_sticker = findViewById(R.id.add_sticker);
        LinearLayout add_effect = findViewById(R.id.add_effect);
        LinearLayout add_user_image = findViewById(R.id.add_user_image);
        LinearLayout add_bg = findViewById(R.id.add_bg);

        trans_img.setImageAlpha(100);

        Intent intent = getIntent();
        template_type = intent.getStringExtra("Temp_Type");
        posterCos = intent.getParcelableArrayListExtra("template");
        sticker_info = intent.getParcelableArrayListExtra("sticker");
        text_info = intent.getParcelableArrayListExtra("text");
        template_id = intent.getIntExtra("template_id", 0);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        this.screenWidth = getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = (getResources().getDisplayMetrics().heightPixels - MailER_SizeUtils.dpToPx(this, 110.0f));//(float) (dimension.heightPixels - ImageUtils.dpToPx(this, R.styleable.AppCompatTheme_checkedTextViewStyle));

        prefManager = new MailER_PreferenceClass(this);

//        RelativeLayout rl_ad = this.findViewById(R.id.rl_ad);
//        RelativeLayout btm = this.findViewById(R.id.btm);
//        ShimmerFrameLayout shimmer_view_container = this.findViewById(R.id.shimmer_view_container);
//        if (MailER_NetworkUtils.isNetworkAvailable(this)) {
//            if (prefManager.getInt("IsEditScreenBannerAD",0) == 1) {
//                btm.setVisibility(View.VISIBLE);
//                MailER_LoadAds.loadAdmobBannerAd(this, rl_ad, shimmer_view_container);
//            }else {
//                btm.setVisibility(View.GONE);
//            }
//
//        }


//        rl_ad = findViewById(R.id.rl_ad);
//        rl_ad.setVisibility(View.VISIBLE);
//        if (NetworkUtils.isNetworkAvailable(this)) {
//            if (prefManager.getAdsId("BannerAdunitID") != null) {
//                LoadAds.loadAdmobBannerAd(this, rl_ad);
//            }
//        }

        textController = new MailER_TextController(this, guideline);
        stickerController = new MailER_StickerController(this, guideline);
        effectController = new MailER_EffectController(this);


        final float[] widgetDX = {0f};
        final float[] widgetDY = {0f};


        rl_watermark.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(MailER_PosterEditActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
//                    MaterialDialogUtils.getInstance().rewardDialog(PosterEditActivity.this,
//                            "Remove Watermark(One Time)", "Remove Watermark by watching Ads", materialDialog -> {
//                        rewardType = "remWatermark";
//                                AppOpenManager.loadRewardVideoAd(PosterEditActivity.this,() -> btn_watermark.setVisibility(View.GONE));
////                                MyApplication.showInterstitialAd(PosterEditActivity.this,() -> btn_watermark.setVisibility(View.GONE));
//                        if (materialDialog != null && materialDialog.isShowing())
//                            materialDialog.dismiss();
//                    }, materialDialog -> {
//                        if (materialDialog != null && materialDialog.isShowing())
//                            materialDialog.dismiss();
//                    });
                    return super.onDoubleTap(e);
                }
            });

            private static final long CLICK_TIME_THRESHOLD = 200; // Adjust as needed
            private float startX, startY;
            private long touchStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                float parentHeight = main_rel.getHeight();
                float parentWidth = main_rel.getWidth();
                float xMax = parentWidth - v.getWidth();
                float yMax = parentHeight - v.getHeight();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        touchStartTime = System.currentTimeMillis();
                        widgetDX[0] = v.getX() - event.getRawX();
                        widgetDY[0] = v.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + widgetDX[0];
                        newX = Math.max(0F, newX);
                        newX = Math.min(xMax, newX);
                        v.setX(newX);

                        float newY = event.getRawY() + widgetDY[0];
                        newY = Math.max(0F, newY);
                        newY = Math.min(yMax, newY);
                        v.setY(newY);
                        hideAllControls();
                        removeScroll();
                        break;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        long touchDuration = System.currentTimeMillis() - touchStartTime;

                        // Check for click event
                        if (Math.abs(endX - startX) < 10 && Math.abs(endY - startY) < 10 && touchDuration < CLICK_TIME_THRESHOLD) {

                            // Handle click event here
                            // Add your desired code logic for handling the click event
                            MailER_MaterialDialogUtils.getInstance().rewardDialog(MailER_PosterEditActivity.this,
                                    "Remove Watermark(One Time)", "Remove Watermark by watching Ads", materialDialog -> {
                                        rewardType = "remWatermark";
                                        MailER_PreferenceClass preferenceClass = new MailER_PreferenceClass(MailER_PosterEditActivity.this);
                                        if (preferenceClass.getDataType("PremiumAdType") != null && preferenceClass.getDataType("PremiumAdType").equals("Reward")) {
                                            MailER_RewardVideoManager.showRewardVideoAd(MailER_PosterEditActivity.this, new MailER_InterstitialAdManager.OnRewardAdLoadInterface() {
                                                @Override
                                                public void onAdClose() {
                                                    rl_watermark.setVisibility(View.GONE);
                                                }
                                            });
                                        } else {
                                            MyApplication.showInterstitialAdWithOutCount(MailER_PosterEditActivity.this, () -> rl_watermark.setVisibility(View.GONE));
                                        }
//                                MyApplication.showInterstitialAd(PosterEditActivity.this,() -> btn_watermark.setVisibility(View.GONE));
                                        if (materialDialog != null && materialDialog.isShowing())
                                            materialDialog.dismiss();
                                    }, materialDialog -> {
                                        if (materialDialog != null && materialDialog.isShowing())
                                            materialDialog.dismiss();
                                    });
                        }
                        break;

                    default:
                        break;
                }

                return true;
            }
        });

        this.listFragment = new MailER_ListFragment();
        showFragment(this.listFragment);

        initViewPager();

        add_text.setOnClickListener(this);
        add_sticker.setOnClickListener(this);
        add_effect.setOnClickListener(this);
        add_user_image.setOnClickListener(this);
        add_bg.setOnClickListener(this);

        btn_reset.setOnClickListener(this);
        btn_redo.setOnClickListener(this);
        btn_undo.setOnClickListener(this);

        btn_save_template.setOnClickListener(this);
        btn_save_poster.setOnClickListener(this);

        btn_layControls = findViewById(R.id.btn_layControls);
        btn_layControls.setOnClickListener(this);

        RelativeLayout btn_backPress = findViewById(R.id.btn_backPress);
        btn_backPress.setOnClickListener(v -> onBackPressed());
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.lay_container, fragment, "fragment").commit();
    }

    private void initViewPager() {
        MailER_PagerSlidingTabStrip tabs = findViewById(R.id.pagerSlidingTabStrip);
        ViewPager _mViewPager = findViewById(R.id.stickerViewPager);
        _mViewPager.setAdapter(new MailER_StickerViewPagerAdapter(getSupportFragmentManager()));
        _mViewPager.setCurrentItem(0);
        tabs.setViewPager(_mViewPager);
    }

    public void notifyUndoRedo(String type, String operation, int position) {
        new saveTempUndoRedoTaskAsync(type, operation, position).execute();
    }

    @Override
    public void onDelete(View view) {
        child_position = txt_stkr_rel.indexOfChild(view);
        hideAllControls();
        hideEffectControl();
        guideline.setVisibility(View.GONE);
        removeScroll();
        String child_type;
        if (view instanceof MailER_AutofitTextRel) {
            child_type = "text";
        } else {
            child_type = "sticker";
        }

        new createUndoRedoAsync(true, true, "delete", child_type).execute();
    }

    @Override
    public void onDoubleTap(View view) {
        if (view instanceof MailER_AutofitTextRel) {
            String text = ((MailER_AutofitTextRel) view).getText();
            showTextDialog(text, (MailER_AutofitTextRel) view);
        }
    }

    @Override
    public void onEdit(View view, Uri uri) {

    }

    @Override
    public void onRotateDown(View view) {
        touchDown(view, "viewBorder");
    }

    @Override
    public void onRotateMove(View view) {
        touchMove(view);
    }

    @Override
    public void onRotateUp(View view) {
        touchUp(view);
    }

    @Override
    public void onScaleDown(View view) {
        touchDown(view, "viewBorder");
    }

    @Override
    public void onScaleMove(View view) {
        touchMove(view);
    }

    @Override
    public void onScaleUp(View view) {
        touchUp(view);
    }

    @Override
    public void onTouchDown(View view) {
        touchDown(view, "hideBorder");
    }

    @Override
    public void onTouchMove(View view) {
        touchMove(view);
    }

    @Override
    public void onTouchUp(View view) {
        touchUp(view);
    }

    @Override
    public void onMidX(View view) {
        guideline.setGuildLine(true, false);
    }

    @Override
    public void onMidXY(View view) {
        guideline.setGuildLine(true, true);
    }

    @Override
    public void onMidY(View view) {
        guideline.setGuildLine(false, true);
    }

    @Override
    public void onXY(View view) {
        this.guideline.setGuildLine(false, false);
    }

    private void touchDown(View view, String visible) {
        if (visible.equals("hideBorder")) {
            removeImageViewControl();
        }

        if (view instanceof MailER_ResizableStickerView) {
            ((MailER_ResizableStickerView) view).setBorderVisibility(true);
        }
        if (view instanceof MailER_AutofitTextRel) {
            ((MailER_AutofitTextRel) view).setBorderVisibility(true);
        }

        child_position = txt_stkr_rel.indexOfChild(view);

        view_width = view.getWidth();
        view_height = view.getHeight();
        view_x = view.getX();
        view_y = view.getY();
        view_rotation = view.getRotation();

        lay_effects.setVisibility(View.GONE);
        lay_StkrMain.setVisibility(View.GONE);
        lay_TextMain.setVisibility(View.GONE);
        hideListControl();

        if (guideline.getVisibility() == View.GONE) {
            guideline.setVisibility(View.VISIBLE);
        }
    }

    private void touchMove(View view) {
        if (!first_click) {
            if (view_width != view.getWidth() || view_height != view.getHeight() || view_x != view.getX() || view_y != view.getY() || view_rotation != view.getRotation()) {
                old_x = view_x;
                old_y = view_y;
                child_position = txt_stkr_rel.indexOfChild(view);

                String child_type;
                if (view instanceof MailER_AutofitTextRel) {
                    child_type = "text";
                } else {
                    child_type = "sticker";
                }
                new createUndoRedoAsync(true, true, "move", child_type).execute();
                first_click = true;
            }
        }

        this.lay_effects.setVisibility(View.GONE);
//        img_effect.setImageTintList(getResources().getColorStateList(R.color.white));
        this.lay_TextMain.setVisibility(View.GONE);
        this.lay_StkrMain.setVisibility(View.GONE);
        hideListControl();
    }

    private void touchUp(final View v) {

        child_position = txt_stkr_rel.indexOfChild(v);
        textStickerScrollView(v);
        if (notifyRedoData) {
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
            int pos = dh.getUndoLstTemplateId();
            saveUndoData(pos + 1);
        } else {
            failedNotifyRedoData = true;
        }
        notifyRedoData = false;


        if (v instanceof MailER_AutofitTextRel) {
            if (this.lay_TextMain.getVisibility() == View.GONE) {
                this.lay_TextMain.setVisibility(View.VISIBLE);
                textController.setAutofitTextRel((MailER_AutofitTextRel) v, guideline);
//                this.lay_TextMain.startAnimation(AnimClass.getAnimUp(this));
            }
        }
        if (v instanceof MailER_ResizableStickerView) {
            if (this.lay_StkrMain.getVisibility() == View.GONE) {
                this.lay_StkrMain.setVisibility(View.VISIBLE);
                stickerController.setResizableStickerView((MailER_ResizableStickerView) v, guideline);
//                this.lay_StkrMain.startAnimation(AnimClass.getAnimUp(this));
            }
        }
        if (this.guideline.getVisibility() == View.VISIBLE) {
            this.guideline.setVisibility(View.GONE);
        }

        first_click = false;
    }

    public void textStickerScrollView(View v) {
//        if (v != null) {
//            int vH = v.getHeight();
//            int[] los1 = new int[2];
//            this.lay_scroll.getLocationOnScreen(los1);
//            this.parentY = (float) los1[1];
//            float distance = this.parentY - ((float) SizeUtils.dpToPx(this, 55.0f));
//            int[] los = new int[2];
//            v.getLocationOnScreen(los);
//            float bY = (float) (los[1] + vH);
//            int[] los_edittext_lay = new int[2];
//            this.temp_ll.getLocationOnScreen(los_edittext_lay);
//            float pY = (float) los_edittext_lay[1];
//            if (this.parentY + ((float) this.lay_scroll.getHeight()) < bY) {
//                bY = this.parentY + ((float) this.lay_scroll.getHeight());
//            }
//            if (bY > pY) {
//                this.distanceScroll = (int) (bY - pY);
//                if (((float) this.distanceScroll) < distance) {
//                    this.lay_scroll.setY((this.parentY - ((float) SizeUtils.dpToPx(this, 55.0f))) - ((float) this.distanceScroll));
//                } else {
//                    int currentDistanceToScroll = this.lay_scroll.getScrollY();
//                    this.lay_scroll.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, -RelativeLayout.LayoutParams.WRAP_CONTENT));
//                    this.lay_scroll.postInvalidate();
//                    this.lay_scroll.requestLayout();
//                    int scrollBy = (int) ((bY - distance) - pY);
//                    int nH = this.lay_scroll.getHeight() - scrollBy;
//                    this.distanceScroll = currentDistanceToScroll + scrollBy;
//                    this.lay_scroll.getLayoutParams().height = nH;
//                    this.lay_scroll.postInvalidate();
//                    this.lay_scroll.requestLayout();
//                }
//                this.lay_scroll.post(() -> {
//                    if (lay_scroll.getY() < 0.0f) {
//                        lay_scroll.setY(0.0f);
//                    }
//                    lay_scroll.smoothScrollTo(0, distanceScroll);
//                });
//            }
//        }
    }

    private void removeScroll() {
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        this.lay_scroll.setLayoutParams(layoutParams);
//        this.lay_scroll.postInvalidate();
//        this.lay_scroll.requestLayout();
    }

    public void removeImageViewControl() {
        this.guideline.setVisibility(View.GONE);
        int childCount = txt_stkr_rel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = txt_stkr_rel.getChildAt(i);
            if (view instanceof MailER_AutofitTextRel) {
                ((MailER_AutofitTextRel) view).setBorderVisibility(false);
            }
            if (view instanceof MailER_ResizableStickerView) {
                ((MailER_ResizableStickerView) view).setBorderVisibility(false);
            }
        }
    }

    public void addNewAutofitTextRel(MailER_AutofitTextRel autofitTextRel, MailER_TextInfo textInfo) {
        new saveTempUndoRedoTaskAsync("text", "nothing", -1).execute();
        txt_stkr_rel.addView(autofitTextRel);
        removeImageViewControl();
        autofitTextRel.setTextInfo(textInfo, true);
        autofitTextRel.setId(View.generateViewId());
        autofitTextRel.setOnTouchCallbackListener(this);
        autofitTextRel.setBorderVisibility(true);
    }

    public void addNewResizableStickerView(MailER_ResizableStickerView resizableStickerView, MailER_StickerInfo stickerInfo, boolean show) {

        new saveTempUndoRedoTaskAsync("sticker", "nothing", -1).execute();

        txt_stkr_rel.addView(resizableStickerView);
        removeImageViewControl();
        resizableStickerView.setComponentInfo(stickerInfo, true);
        resizableStickerView.setMainLayoutWH((float) this.main_rel.getWidth(), (float) this.main_rel.getHeight());
        resizableStickerView.setId(View.generateViewId());
        resizableStickerView.setOnTouchCallbackListener(this);
        resizableStickerView.setBorderVisibility(true);
        if (show) {
            this.lay_StkrMain.setVisibility(View.VISIBLE);
            stickerController.setResizableStickerView(resizableStickerView, guideline);
//            this.lay_StkrMain.startAnimation(AnimClass.getAnimUp(this));
        }
    }

    private void hideEffectControl() {
        if (lay_effects.getVisibility() == View.VISIBLE) {
//            lay_effects.startAnimation(AnimClass.getAnimDown(this));
            lay_effects.setVisibility(View.GONE);
//            img_effect.setImageTintList(getResources().getColorStateList(R.color.white));
        }
    }

    private void hideStickerControl() {
        if (lay_sticker.getVisibility() == View.VISIBLE) {
            lay_sticker.setVisibility(View.GONE);
            save_ly.setVisibility(View.VISIBLE);
            txtHeader.setText("");
//            img_sticker.setImageTintList(getResources().getColorStateList(R.color.white));
        }
    }

    private void hideAllControls() {

        removeImageViewControl();

        if (this.lay_TextMain.getVisibility() == View.VISIBLE) {
//            this.lay_TextMain.startAnimation(AnimClass.getAnimDown(this));
            this.lay_TextMain.setVisibility(View.GONE);
        }
        if (this.lay_StkrMain.getVisibility() == View.VISIBLE) {
//            this.lay_StkrMain.startAnimation(AnimClass.getAnimDown(this));
            this.lay_StkrMain.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        removeScroll();
        int id = v.getId();

        if (id == R.id.add_text) {
            MyApplication.showEditInterstitialAd(this, () -> {
                hideAllControls();
                hideStickerControl();
                hideEffectControl();
                hideListControl();
                showTextDialog("", null);
            });

        } else if (id == R.id.add_sticker) {
            MyApplication.showEditInterstitialAd(this, () -> {
                hideAllControls();
                hideEffectControl();
                hideListControl();
                if (lay_sticker.getVisibility() == View.GONE) {
                    lay_sticker.setVisibility(View.VISIBLE);
                    save_ly.setVisibility(View.GONE);
//                    img_sticker.setImageTintList(getResources().getColorStateList(R.color.blue));
                    txtHeader.setText("Choose Sticker");
                    return;
                }
                hideStickerControl();
            });

        } else if (id == R.id.add_effect) {
            MyApplication.showEditInterstitialAd(this, () -> {
                hideAllControls();
                hideStickerControl();
                hideListControl();
                if (lay_effects.getVisibility() == View.GONE) {
//                lay_effects.startAnimation(AnimClass.getAnimUp(this));
                    lay_effects.setVisibility(View.VISIBLE);
//                    img_effect.setImageTintList(getResources().getColorStateList(R.color.blue));
                    return;
                }
                hideEffectControl();
            });
        } else if (id == R.id.add_user_image) {
            MyApplication.showEditInterstitialAd(this, () -> {
                hideAllControls();
                hideStickerControl();
                hideEffectControl();
                hideListControl();
                showUserDialog();
            });
        } else if (id == R.id.add_bg) {
            MyApplication.showEditInterstitialAd(this, () -> {
                hideAllControls();
                hideStickerControl();
                hideEffectControl();
                hideListControl();
                if (MailER_NetworkUtils.isNetworkAvailable(this)) {
                    Intent intent = new Intent(this, MailER_BackgroundSelectionActivity.class);
                    intent.putExtra("mode", "user");
                    startActivityForResult(intent, INTENT_FOR_BG);
                } else {
                    Toast.makeText(this, "Make sure you are connected to internet!!", Toast.LENGTH_SHORT).show();
                }
            });

        } else if (id == R.id.btn_layControls) {
            hideAllControls();
            hideStickerControl();
            hideEffectControl();
            if (lay_container.getVisibility() == View.GONE) {
                this.listFragment.getLayoutChild();
                lay_container.setVisibility(View.VISIBLE);
                ViewAnimator.animate(lay_container, btn_layControls)
                        .translationX(-500, 0)
                        .duration(200)
                        .alpha(0, 1)
                        .start();
                return;
            }
            hideListControl();
        } else if (id == R.id.btn_save_template) {
            hideAllControls();
            hideStickerControl();
            hideEffectControl();
            hideListControl();
            btn_watermark_remove.setVisibility(View.GONE);
            checkDownloadAdAndSave(false);
        } else if (id == R.id.btn_save_poster) {
            hideAllControls();
            hideStickerControl();
            hideEffectControl();
            hideListControl();
            btn_watermark_remove.setVisibility(View.GONE);
            checkDownloadAdAndSave(true);
        } else if (id == R.id.btn_reset) {
            MyApplication.showEditInterstitialAd(this, () -> {
                MailER_MaterialDialogUtils.getInstance().resetDialog(this, materialDialog -> {
                    MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(this);
                    dh.deleteUndoTemplateInfo();
                    dh.deleteRedoTemplateInfo();
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
                    recreate();
                }, materialDialog -> {
                    if (materialDialog != null && materialDialog.isShowing())
                        materialDialog.dismiss();
                });
            });
        } else if (id == R.id.btn_undo) {
            if (undo_progress == 0) {
                return;
            }
            hideAllControls();
            hideStickerControl();
            hideEffectControl();
            hideListControl();
            btn_undo.setVisibility(View.GONE);
            btn_redo.setVisibility(View.GONE);
            progress_undo_redo.setVisibility(View.VISIBLE);
            new saveRedoAsync().execute();
        } else if (id == R.id.btn_redo) {
            if (redo_progress == 0) {
                return;
            }
            hideAllControls();
            hideStickerControl();
            hideEffectControl();
            hideListControl();
            btn_undo.setVisibility(View.GONE);
            btn_redo.setVisibility(View.GONE);
            progress_undo_redo.setVisibility(View.VISIBLE);
            new saveUndoAsync().execute();
        }

    }

    private void hideListControl() {
        if (lay_container.getVisibility() == View.VISIBLE) {
            btn_layControls.setVisibility(View.GONE);
            lay_container.animate().translationX((float) (-lay_container.getRight())).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();
            new Handler().postDelayed(() -> {
                lay_container.setVisibility(View.GONE);
                btn_layControls.setVisibility(View.VISIBLE);
            }, 200);
        }
    }

    public void hideEverything() {
        hideAllControls();
        hideStickerControl();
        hideEffectControl();
    }

    private void checkDownloadAdAndSave(boolean isPoster) {
        com.festival.flyer.postermaker.utils.MailER_PreferenceClass prefManager = new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this);
        int freeAllowed = prefManager.getInt("FreeDownloadsAllowed", 2);
        int totalDownloads = prefManager.getInt("TotalDownloadsCount", 0);

        if (totalDownloads < freeAllowed) {
            prefManager.setInt("TotalDownloadsCount", totalDownloads + 1);
            new saveTemplateAsync(isPoster).execute();
        } else {
            // Show custom pop-up dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Premium Quality Download")
                    .setMessage("Watch a short video to download your poster in High Quality!")
                    .setCancelable(false)
                    .setPositiveButton("Watch Ad", (dialog, which) -> {
                        com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager.showRewardVideoAd(this, () -> {
                            new saveTemplateAsync(isPoster).execute();
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    private void showUserDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.spawner_dialog_select_image);
        dialog.setCancelable(true);
        LinearLayout img_camera = dialog.findViewById(R.id.img_camera);
        LinearLayout img_gallery = dialog.findViewById(R.id.img_gallery);

        RelativeLayout native_banner_ad_container = dialog.findViewById(R.id.native_banner_ad_container);
        ShimmerFrameLayout shimmer_view_container = dialog.findViewById(R.id.shimmer_view_container);

        MailER_NativeAdUtil.loadNativeAd(native_banner_ad_container, this, shimmer_view_container);

        img_camera.setOnClickListener(v -> {

//            if (SDK_INT >= Build.VERSION_CODES.M) {
//                if (checkSelfPermission("android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
//                        || checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
//                        || checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(MailER_PosterEditActivity.this, WRITE_EXTERNAL_STORAGE)
//                            || ActivityCompat.shouldShowRequestPermissionRationale(MailER_PosterEditActivity.this, READ_EXTERNAL_STORAGE)) {
//                        requestPermissions(new String[]{"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                    } else {
//                        MailER_MaterialDialogUtils.getInstance().PermissionDialog(this);
//                    }
//                    return;
//                }
//            }

            if (!checkPermission()) {
                try {
                    requestPermission();

//                    if (Build.MANUFACTURER.equals("TECNO MOBILE LIMITED") && Build.BRAND.equals("TECNO") ||
//                            Build.BRAND.equals("samsung") || Build.BRAND.equals("OPPO") ||
//                            Build.BRAND.equals("vivo")) {

                    MailER_MaterialDialogUtils.getInstance().PermissionDialog(MailER_PosterEditActivity.this);
//                        Log.e("#brand", Build.BRAND);
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                        intent.setData(uri);
//                        startActivity(intent);
//                    }

                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                camera_file = new File(Environment.getExternalStorageDirectory(), ".temp.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", camera_file));
                startActivityForResult(intent, SELECT_PICTURE_FROM_CAMERA);
                dialog.dismiss();
            }


        });

        img_gallery.setOnClickListener(v -> {

//            if (SDK_INT >= Build.VERSION_CODES.M) {
//                if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
//                        || checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(MailER_PosterEditActivity.this, WRITE_EXTERNAL_STORAGE)
//                            || ActivityCompat.shouldShowRequestPermissionRationale(MailER_PosterEditActivity.this, READ_EXTERNAL_STORAGE)) {
//                        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_GRANTED);
//                    } else {
//                        MailER_MaterialDialogUtils.getInstance().PermissionDialog(this);
//                    }
//                    return;
//                }
//            }

            dialog.dismiss();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction("android.intent.action.PICK");
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), SELECT_PICTURE_FROM_GALLERY);
        });

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    public void showTextDialog(String text, MailER_AutofitTextRel autofitTextRel) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.spawner_add_text_popup);
        dialog.setCancelable(false);
        MailER_AutoResizeEditText fitEditText = dialog.findViewById(R.id.auto_fit_edit_text);
        RelativeLayout cancel = dialog.findViewById(R.id.btnCancelDialog);
        RelativeLayout add = dialog.findViewById(R.id.btnAddTextSDialog);
        fitEditText.setText(text);
        cancel.setOnClickListener(v -> dialog.dismiss());
        add.setOnClickListener(v -> {
            MyApplication.showEditInterstitialAd(this, () -> {
                String string = fitEditText.getText().toString();
                if (string.length() == 0) {
                    Toast.makeText(MailER_PosterEditActivity.this, "Please enter text here.", Toast.LENGTH_SHORT).show();
                    return;
                }

                addOrChangeText(string, autofitTextRel);

                dialog.dismiss();
            });
        });

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA

            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA

            }, 1);
        }

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (result1 == 0 && result2 == 0) {
                return true;
            }
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (result == 0 && result1 == 0 && result2 == 0) {
                return true;
            }
        }

        return false;
    }

    private void addOrChangeText(String text, MailER_AutofitTextRel autofitTextRel) {
        notifyUndoRedo("text", "nothing", -1);
        MailER_TextInfo textInfo = new MailER_TextInfo();
        textInfo.setPOS_X((float) ((txt_stkr_rel.getWidth() / 2) - MailER_SizeUtils.dpToPx(this, 100)));
        textInfo.setPOS_Y((float) ((txt_stkr_rel.getHeight() / 2) - MailER_SizeUtils.dpToPx(this, 100)));
        textInfo.setWIDTH(MailER_SizeUtils.dpToPx(this, 200));
        textInfo.setHEIGHT(MailER_SizeUtils.dpToPx(this, 200));
        textInfo.setTEXT(text);
        textInfo.setFONT_NAME("Default.ttf");
        textInfo.setTEXT_COLOR(getResources().getColor(R.color.black));
        textInfo.setTEXT_ALPHA(100);
        textInfo.setSHADOW_COLOR(Color.parseColor("#7641b6"));
        textInfo.setSHADOW_PROG(0);
        textInfo.setBG_COLOR(0);
        textInfo.setBG_DRAWABLE("0");
        textInfo.setBG_ALPHA(0);
        textInfo.setROTATION(0.0f);
        textInfo.setFIELD_TWO("");

        if (autofitTextRel != null) {
            autofitTextRel.setText(text);
            autofitTextRel.setBorderVisibility(true);
        } else {
            MailER_AutofitTextRel rl = new MailER_AutofitTextRel(this);
            txt_stkr_rel.addView(rl);
            rl.setTextInfo(textInfo, true);
            rl.setId(View.generateViewId());
            rl.setMainLayoutWH((float) this.main_rel.getWidth(), (float) this.main_rel.getHeight());
            rl.setOnTouchCallbackListener(this);
            rl.setBorderVisibility(true);
            this.lay_TextMain.setVisibility(View.VISIBLE);
            textController.setAutofitTextRel(rl, guideline);
//            this.lay_TextMain.startAnimation(AnimClass.getAnimUp(this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (permission_type.equals("template")) {
                new saveTemplateAsync(false).execute();
                return;
            }
            if (permission_type.equals("poster")) {
                new saveTemplateAsync(true).execute();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int CROP_REQUEST_CODE = 0x2019;
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE_FROM_CAMERA) {
            Uri selectedImage = Uri.fromFile(this.camera_file);
            imguri = selectedImage;
            Intent intent = new Intent(this, MailER_PhotoCropActivity.class);
            intent.putExtra("selectedImage", selectedImage.toString());
            startActivityForResult(intent, CROP_REQUEST_CODE);
        }
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE_FROM_GALLERY && data != null) {
            Uri selectedImage = data.getData();
            imguri = selectedImage;
            Log.e("#selectedImage", String.valueOf(selectedImage));
            Intent intent = new Intent(this, MailER_PhotoCropActivity.class);
            assert selectedImage != null;
            intent.putExtra("selectedImage", selectedImage.toString());
            startActivityForResult(intent, CROP_REQUEST_CODE);
        }
        if (resultCode == RESULT_OK && requestCode == CROP_REQUEST_CODE && data != null) {
            String selectedImage = data.getStringExtra("sticker");
            addSticker(selectedImage, "user");
        }
        if (resultCode == RESULT_OK && requestCode == INTENT_FOR_BG && data != null) {
            String path = data.getStringExtra("bg_image");
            boolean local = data.getBooleanExtra("local", true);

            if (local) {
                startCrop(Uri.parse(path));
                return;
            }

            if (path != null) {
//                Uri uri = MailER_FileUtils.getUriFromPath(this, path);
                    startCrop(Uri.fromFile(new File(path)));
            } else {
                Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
            }

        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            final Uri resultUri = UCrop.getOutput(data);
            try {
                Bitmap bitmap = MailER_BitmapUtils.getCompressedBitmap(this, resultUri, screenWidth, screenHeight);
                bitmap = MailER_BitmapUtils.bitmapRatio("", bitmap, screenWidth, screenHeight);
                background_img.setImageBitmap(bitmap);
                this.bg_bitmap = bitmap;
            } catch (IOException e) {
                Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileName() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        return "SampleCropImage" + timeStamp + "_.png";
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "SampleCropImage.png";
//        File file=new File(getCacheDir(), destinationFileName);
//        if (file.exists()){
//            file.delete();
//        }
//        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), destinationFileName)));
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop = advancedConfig(uCrop);
        uCrop.start(this);
    }

    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        int gcd = MailER_SizeUtils.getAspectRatio(main_rel.getWidth(), main_rel.getHeight());
        int ratioX = main_rel.getWidth() / gcd;
        int ratioY = main_rel.getHeight() / gcd;
        options.withAspectRatio(ratioX, ratioY);
        options.withMaxResultSize(screenWidth, screenHeight);
        
        options.setToolbarColor(Color.parseColor("#7B2FF7")); // Primary Purple
        options.setStatusBarColor(Color.parseColor("#5A18C9")); // Darker Purple
        options.setToolbarWidgetColor(Color.WHITE);
        options.setActiveControlsWidgetColor(Color.parseColor("#7B2FF7")); // Purple controls
        options.setRootViewBackgroundColor(Color.WHITE); // Cleaner background
        options.setCropFrameColor(Color.WHITE); // White crop frame
        options.setCropGridColor(Color.WHITE); // White grid
        
        return uCrop.withOptions(options);
    }

    private void leaveDialog() {
        @SuppressLint("ResourceType")
        Dialog dialog = new Dialog(this, 16974126);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.spawner_leave_dialog);
        dialog.setCancelable(true);

        RelativeLayout native_banner_ad_container = dialog.findViewById(R.id.native_banner_ad_containers);
        ShimmerFrameLayout shimmer_view_container = dialog.findViewById(R.id.shimmer_view_container);

        MailER_NativeAdUtil.loadNativeAd(native_banner_ad_container, this, shimmer_view_container);

        TextView btn_yes = dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        TextView btn_no = dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    @Override
    public void onSnapFilter(String str) {
        hideStickerControl();
        addSticker(str, "sticker");
    }

    private void addSticker(String str, String type) {
//        new createUndoRedoAsync(true, false, "nothing", "sticker").execute();
        Bitmap bitmap;
        if (type.equals("sticker")) {
            bitmap = MailER_BitmapUtils.getBitmapFromAsset(this, str);
            str = MailER_BitmapUtils.saveBitmapObject1(MailER_PosterEditActivity.this, "temp", bitmap);
            bitmap.recycle();
        }
        MailER_StickerInfo stickerInfo = new MailER_StickerInfo();

        main_rel.post(() -> {
            stickerInfo.setPOS_X((float) ((this.main_rel.getWidth() / 2) - MailER_SizeUtils.dpToPx(this, 70)));
            stickerInfo.setPOS_Y((float) ((this.main_rel.getHeight() / 2) - MailER_SizeUtils.dpToPx(this, 70)));
        });

        stickerInfo.setWIDTH(MailER_SizeUtils.dpToPx(this, 140));
        stickerInfo.setHEIGHT(MailER_SizeUtils.dpToPx(this, 140));

        stickerInfo.setROTATION(0.0f);
        stickerInfo.setRES_ID("");
        stickerInfo.setBITMAP(null);
        stickerInfo.setCOLORTYPE("colored");
        stickerInfo.setTYPE("STICKER");
        stickerInfo.setSTC_OPACITY(100);
        stickerInfo.setSTC_COLOR(0);
        stickerInfo.setSTKR_PATH(str);
        stickerInfo.setSTC_HUE(1);
        stickerInfo.setFIELD_TWO("0,0");
        MailER_ResizableStickerView riv = new MailER_ResizableStickerView(this);
        addNewResizableStickerView(riv, stickerInfo, true);
    }

    private void saveComponent(long templateId, MailER_DatabaseHandler dh, String path) {
        template_id = (int) templateId;
        int childCount = txt_stkr_rel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = txt_stkr_rel.getChildAt(i);
            if (child instanceof MailER_AutofitTextRel) {
                MailER_TextInfo textInfo = ((MailER_AutofitTextRel) child).getTextInfo();
                textInfo.setTEMPLATE_ID((int) templateId);
                textInfo.setORDER(i);
                textInfo.setTYPE("TEXT");
                dh.insertTextRow(textInfo);
            } else {
                saveShapeAndSticker(templateId, i, dh, path);
            }
        }
    }

    private void saveUndoData(long templateId, MailER_DatabaseHandler dh) {
        int childCount = txt_stkr_rel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = txt_stkr_rel.getChildAt(i);
            if (child instanceof MailER_AutofitTextRel) {
                MailER_TextInfo textInfo = ((MailER_AutofitTextRel) child).getTextInfo();
                textInfo.setTEMPLATE_ID((int) templateId);
                textInfo.setORDER(i);
                textInfo.setTYPE("TEXT");
                dh.insertUndoTextRow(textInfo);
            } else {
                saveUndoSticker(templateId, i, dh);
            }
        }
    }

    private void saveUndoData(int templateId) {
        temp_txtShapeList.clear();
        int childCount = txt_stkr_rel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = txt_stkr_rel.getChildAt(i);
            if (child instanceof MailER_AutofitTextRel) {
                MailER_TextInfo textInfo = ((MailER_AutofitTextRel) child).getTextInfo();
                textInfo.setTEMPLATE_ID(templateId);
                textInfo.setORDER(i);
                textInfo.setTYPE("TEXT");
                temp_txtShapeList.put(textInfo.getORDER(), textInfo);
            } else {
                saveUndoSticker(templateId, i);
            }
        }
    }

    private void saveRedoData(long templateId, MailER_DatabaseHandler dh) {
        int childCount = txt_stkr_rel.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = txt_stkr_rel.getChildAt(i);
            if (child instanceof MailER_AutofitTextRel) {
                MailER_TextInfo textInfo = ((MailER_AutofitTextRel) child).getTextInfo();
                textInfo.setTEMPLATE_ID((int) templateId);
                textInfo.setORDER(i);
                textInfo.setTYPE("TEXT");
                dh.insertRedoTextRow(textInfo);
            } else {
                saveRedoSticker(templateId, i, dh);
            }
        }
    }

    public void saveShapeAndSticker(long templateId, int i, MailER_DatabaseHandler dh, String path) {
        String sticker_image = MailER_BitmapUtils.saveBitmapObject1(MailER_PosterEditActivity.this, path, ((MailER_ResizableStickerView) txt_stkr_rel.getChildAt(i)).getMainImageBitmap());
        MailER_StickerInfo ci = ((MailER_ResizableStickerView) txt_stkr_rel.getChildAt(i)).getComponentInfo();
        ci.setTEMPLATE_ID((int) templateId);
        ci.setTYPE("STICKER");
        ci.setORDER(i);
        ci.setSTKR_PATH(sticker_image);
        dh.insertComponentInfoRow(ci);
    }

    public void saveUndoSticker(long templateId, int i, MailER_DatabaseHandler dh) {
        MailER_StickerInfo ci = ((MailER_ResizableStickerView) txt_stkr_rel.getChildAt(i)).getComponentInfo();
        ci.setTEMPLATE_ID((int) templateId);
        ci.setTYPE("STICKER");
        ci.setORDER(i);
        dh.insertUndoComponentInfoRow(ci);
    }

    public void saveUndoSticker(int templateId, int i) {
        MailER_StickerInfo ci = ((MailER_ResizableStickerView) txt_stkr_rel.getChildAt(i)).getComponentInfo();
        ci.setTEMPLATE_ID(templateId);
        ci.setTYPE("STICKER");
        ci.setORDER(i);
        temp_txtShapeList.put(ci.getORDER(), ci);
    }

    public void saveRedoSticker(long templateId, int i, MailER_DatabaseHandler dh) {
        MailER_StickerInfo ci = ((MailER_ResizableStickerView) txt_stkr_rel.getChildAt(i)).getComponentInfo();
        ci.setTEMPLATE_ID((int) templateId);
        ci.setTYPE("STICKER");
        ci.setORDER(i);
        dh.insertRedoComponentInfoRow(ci);
    }

    private void saveDialog() {
        @SuppressLint("ResourceType")
        Dialog dialog = new Dialog(this, 16974126);
        dialog.requestWindowFeature(1);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.spawner_save_dialog);
        TextView btn_ok = dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        RelativeLayout native_banner_ad_container = dialog.findViewById(R.id.native_banner_ad_containers);
        ShimmerFrameLayout shimmer_view_container = dialog.findViewById(R.id.shimmer_view_container);

        MailER_NativeAdUtil.loadNativeAd(native_banner_ad_container, this, shimmer_view_container);
    }

    public int colorParse(String color_string) {
        int color;
        try {
            color = Color.parseColor(color_string);
        } catch (Throwable e) {
            color = Color.BLACK;
        }
        return color;
    }

    private void startIntent() {
//        Constant.imgPath = savePath;
//        Constant.bitmap = BitmapFactory.decodeFile(savePath);
        Intent intent = new Intent(this, MailER_ShareActivity.class);
        intent.putExtra("imagePath", savePath);
        intent.putExtra("imagePathWithWaterMark", savePathWithWaterMark);
        intent.putExtra("premiumPoster", premiumPoster);
        startActivity(intent);
    }

    private void startLoader() {
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Loading...");
//        progressDialog.show();
    }

    private void DismissLoader() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        if (listFragment == null) {
            this.listFragment = new MailER_ListFragment();
            showFragment(this.listFragment);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setOverlayImg(String effect_name) {
        try {
            this.effect_name = effect_name;
            Drawable drawable = Drawable.createFromResourceStream(getResources(), new TypedValue(), getAssets().open(effect_name), null);
            trans_img.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOverlayOpacity(int progress) {
        trans_img.setImageAlpha(progress);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadUserPosterAsync extends AsyncTask<String, String, String> {

        Bitmap bg_bitmap;
        Drawable drawable = null;
        int overlay_opacity = 0;
        List<MailER_TemplateInfo> templateList = new ArrayList<>();
        int position;
        ArrayList<MailER_TextInfo> textInfoList = new ArrayList<>();
        ArrayList<MailER_StickerInfo> stickerInfoList = new ArrayList<>();

        public LoadUserPosterAsync(int position) {
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMaterialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PosterEditActivity.this);
            Objects.requireNonNull(mMaterialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            mMaterialDialog.setCancelable(false);
            mMaterialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
            this.templateList = dh.getTemplateListDes("USER");
            MailER_TemplateInfo templateInfo = (MailER_TemplateInfo) this.templateList.get(position);

            textInfoList = dh.getTextInfoList(templateList.get(position).getTEMPLATE_ID());
            stickerInfoList = dh.getComponentInfoList(templateList.get(position).getTEMPLATE_ID(), "STICKER");

            String bg_image = templateInfo.getFRAME_NAME();
            String overlay_Name = templateInfo.getOVERLAY_NAME();
            overlay_opacity = templateInfo.getOVERLAY_OPACITY();
            path = templateInfo.getTEMP_PATH();

            effect_name = overlay_Name;
            effectController.effect_alpha_seekbar.setProgress(overlay_opacity);

            bg_path = bg_image;
            bg_bitmap = BitmapFactory.decodeFile(bg_image);
            bg_bitmap = MailER_BitmapUtils.bitmapRatio("", bg_bitmap, screenWidth, screenHeight);

            String[] concat = templateInfo.getRATIO().split(":");
            mainWidth = Integer.parseInt(concat[0]);
            mainHeight = Integer.parseInt(concat[1]);

            if (!overlay_Name.equals("")) {
                try {
                    drawable = Drawable.createFromResourceStream(getResources(), new TypedValue(), getAssets().open(overlay_Name), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            MailER_PosterEditActivity.this.txtShapeList = new HashMap<>();
            for (MailER_StickerInfo ci : stickerInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ci.getORDER(), ci);
            }
            for (MailER_TextInfo ti : textInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ti.getORDER(), ti);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                center_rel.setAspectRatio((float) bg_bitmap.getWidth() / bg_bitmap.getHeight());
                main_rel.getLayoutParams().width = bg_bitmap.getWidth();
                main_rel.getLayoutParams().height = bg_bitmap.getHeight();

                main_rel.postInvalidate();
                main_rel.requestLayout();
                background_img.setImageBitmap(bg_bitmap);
                MailER_PosterEditActivity.this.bg_bitmap = bg_bitmap;
                trans_img.setImageDrawable(drawable);
                effectController.effect_alpha_seekbar.setProgress(overlay_opacity);

                int[] los1 = new int[2];
                MailER_PosterEditActivity.this.lay_scroll.getLocationOnScreen(los1);
                MailER_PosterEditActivity.this.parentY = (float) los1[1];

                List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.txtShapeList.keySet());
                Collections.sort(sortedKeys);
                int len = sortedKeys.size();
                for (int i = 0; i < len; i++) {
                    Object obj = MailER_PosterEditActivity.this.txtShapeList.get(sortedKeys.get(i));
                    if (obj instanceof MailER_StickerInfo) {
                        MailER_ResizableStickerView riv;
                        riv = new MailER_ResizableStickerView(MailER_PosterEditActivity.this);
                        txt_stkr_rel.addView(riv);
                        riv.optimizeScreen(MailER_PosterEditActivity.this.screenWidth, MailER_PosterEditActivity.this.screenHeight);
                        riv.setMainLayoutWH(mainWidth, mainHeight);
                        riv.setComponentInfo((MailER_StickerInfo) obj, false);
                        riv.setId(View.generateViewId());
                        riv.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                        riv.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                        riv.setBorderVisibility(false);
                    } else if (obj instanceof MailER_TextInfo) {
                        MailER_AutofitTextRel rl = new MailER_AutofitTextRel(MailER_PosterEditActivity.this);
                        txt_stkr_rel.addView(rl);
                        rl.setTextInfo((MailER_TextInfo) obj, false);
                        rl.setId(View.generateViewId());
                        rl.setMainLayoutWH(mainWidth, mainHeight);
                        rl.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                        rl.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                        rl.setBorderVisibility(false);
                    }
                }
                new createUndoRedoAsync(false, false, "nothing", "both").execute();
                if (mMaterialDialog != null && mMaterialDialog.isShowing())
                    mMaterialDialog.dismiss();

                rl_watermark.setVisibility(View.VISIBLE); // visible

                main_rel.startAnimation(AnimationUtils.loadAnimation(MailER_PosterEditActivity.this, R.anim.spawner_bounce));

            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class setPosterAsync extends AsyncTask<String, String, String> {
        Bitmap bitmap;
        boolean loadUserFrame;
        Uri bg_image;

        public setPosterAsync(boolean loadUserFrame, Uri bg_image) {
            this.loadUserFrame = loadUserFrame;
            this.bg_image = bg_image;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMaterialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PosterEditActivity.this);
            Objects.requireNonNull(mMaterialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            mMaterialDialog.setCancelable(false);
            mMaterialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (loadUserFrame) {
                bg_path = bg_image.getPath();
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(bg_path, bmOptions);

//                bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
//                bitmap = BitmapFactory.decodeFile(bg_path);
//                bitmap = BitmapUtils.getCompressedBitmap(PosterEditActivity.this, bg_image, screenWidth, screenHeight);
                bitmap = MailER_BitmapUtils.bitmapRatio("", bitmap, screenWidth, screenHeight);
            } else {

                bg_path = MailER_FileUtils.getFile(MailER_PosterEditActivity.this, posterCos.get(0).getBack_image());
                bitmap = BitmapFactory.decodeFile(MailER_FileUtils.getFile(MailER_PosterEditActivity.this, posterCos.get(0).getBack_image()));
                bitmap = MailER_BitmapUtils.bitmapRatio("", bitmap, screenWidth, screenHeight);
            }

///storage/emulated/0/DCIM/BM Infotech/Festival Adbanao/.poster_data/.temp/.framedata/5b77b6e63f71a_18-08-2018__1534572262.jpg+
///storage/emulated/0/DCIM/BM Infotech/Festival Adbanao/.poster_data/.temp/.framedata/5b77baae8aa9f_18-08-2018__1534573230.jpg
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(bitmap!=null) {
                center_rel.setAspectRatio((float) bitmap.getWidth() / bitmap.getHeight());
                main_rel.getLayoutParams().width = bitmap.getWidth();
                main_rel.getLayoutParams().height = bitmap.getHeight();

                main_rel.postInvalidate();
                main_rel.requestLayout();
                background_img.setImageBitmap(bitmap);
                MailER_PosterEditActivity.this.bg_bitmap = bitmap;

                int[] los1 = new int[2];
                MailER_PosterEditActivity.this.lay_scroll.getLocationOnScreen(los1);
                MailER_PosterEditActivity.this.parentY = (float) los1[1];

                mainWidth = bitmap.getWidth();
                mainHeight = bitmap.getHeight();

                rl_watermark.setVisibility(View.VISIBLE); // visible

                if (!loadUserFrame) {
                    new SetPosterDataAsync().execute();
                } else {
                    new createUndoRedoAsync(false, false, "nothing", "both").execute();
                    if (mMaterialDialog != null && mMaterialDialog.isShowing())
                        mMaterialDialog.dismiss();
                }
            } else {
                if (mMaterialDialog != null && mMaterialDialog.isShowing())
                    mMaterialDialog.dismiss();
                Toast.makeText(MailER_PosterEditActivity.this, "Failed to load image. If this is a remote background, it needs to be downloaded first.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SetPosterDataAsync extends AsyncTask<String, String, String> {

        ArrayList<MailER_StickerInfo> stickerInfoList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < sticker_info.size(); i++) {
                if (!sticker_info.get(i).getSt_image().equals("")) {
                    Bitmap icon = null;
                    try {

                        icon = StringToBitMap(sticker_info.get(i).getSt_image());

//                        icon = Glide.with(PosterEditActivity.this)
//                                .load(FileUtils.getFile(PosterEditActivity.this, sticker_info.get(i).getSt_image()))
//                                .asBitmap()
//                                .into(screenWidth, screenHeight)
//                                .get();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int w = MailER_SizeUtils.getNewWidth(main_rel, Float.parseFloat(sticker_info.get(i).getSt_x_pos()), Float.parseFloat(sticker_info.get(i).getSt_width()));
                    int h = MailER_SizeUtils.getNewHeight(main_rel, Float.parseFloat(sticker_info.get(i).getSt_y_pos()), Float.parseFloat(sticker_info.get(i).getSt_height()));

                    if (w < 10) {
                        w = 20;
                    } else if (w > 10 && w <= 20) {
                        w = 35;
                    }
                    if (h < 10) {
                        h = 20;
                    } else if (h > 10 && h <= 20) {
                        h = 35;
                    }

                    stickerInfoList.add(new MailER_StickerInfo(Integer.parseInt(posterCos.get(0).getPost_id()),
                            MailER_SizeUtils.getXPosition(main_rel, Float.parseFloat(sticker_info.get(i).getSt_x_pos())),
                            MailER_SizeUtils.getYPosition(main_rel, Float.parseFloat(sticker_info.get(i).getSt_y_pos())),
                            w,
                            h,
                            0.0f, 0.0f, "", "STICKER",
                            Integer.parseInt(sticker_info.get(i).getSt_order()), 0, 100, 0, 0, 0,
                            0, MailER_FileUtils.getFile(MailER_PosterEditActivity.this, sticker_info.get(i).getSt_image()), "colored", 1, 0, "", "",
                            "", null, icon));
                }
            }

            MailER_PosterEditActivity.this.txtShapeList = new HashMap<>();
            for (MailER_StickerInfo ci : stickerInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ci.getORDER(), ci);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.txtShapeList.keySet());
            Collections.sort(sortedKeys);
            int len = sortedKeys.size();
            for (int i = 0; i < len; i++) {
                Object obj = MailER_PosterEditActivity.this.txtShapeList.get(sortedKeys.get(i));
                if (obj instanceof MailER_StickerInfo) {
                    MailER_ResizableStickerView riv;
                    riv = new MailER_ResizableStickerView(MailER_PosterEditActivity.this);
                    txt_stkr_rel.addView(riv);
                    riv.optimizeScreen(MailER_PosterEditActivity.this.screenWidth, MailER_PosterEditActivity.this.screenHeight);
                    riv.setMainLayoutWH((float) MailER_PosterEditActivity.this.main_rel.getWidth(), (float) MailER_PosterEditActivity.this.main_rel.getHeight());
                    riv.setComponentInfo((MailER_StickerInfo) obj, false);
                    riv.setId(View.generateViewId());
                    riv.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                    riv.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                    riv.setBorderVisibility(false);
                }
            }

            new setTextData().execute();
        }
    }

    public Bitmap StringToBitMap(String image) {
        try {
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);

            InputStream inputStream = new ByteArrayInputStream(encodeByte);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class setTextData extends AsyncTask<String, String, String> {

        ArrayList<MailER_TextInfo> textInfoList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < text_info.size(); i++) {

                textInfoList.add(new MailER_TextInfo(Integer.parseInt(posterCos.get(0).getPost_id()),
                        text_info.get(i).getText(),
                        text_info.get(i).getFont_family(),
                        colorParse(text_info.get(i).getTxt_color()),
                        100, -16777216, 0, "0", -16777216, 0,
                        MailER_SizeUtils.getXPosition(main_rel, Float.parseFloat(text_info.get(i).getTxt_x_pos())),
                        MailER_SizeUtils.getYPosition(main_rel, Float.parseFloat(text_info.get(i).getTxt_y_pos())),
                        MailER_SizeUtils.getNewWidth(main_rel, Float.parseFloat(text_info.get(i).getTxt_x_pos()), Float.parseFloat(text_info.get(i).getTxt_width())),
                        MailER_SizeUtils.getNewHeightText(main_rel, Float.parseFloat(text_info.get(i).getTxt_y_pos()), Float.parseFloat(text_info.get(i).getTxt_height())),
                        Float.parseFloat(text_info.get(i).getTxt_rotation()),
                        "TEXT",
                        Integer.parseInt(text_info.get(i).getTxt_order()),
                        0, 0, 0, 0, 0, "", "", ""));
            }
            MailER_PosterEditActivity.this.txtShapeList = new HashMap<>();
            for (MailER_TextInfo ti : textInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ti.getORDER(), ti);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.txtShapeList.keySet());
            Collections.sort(sortedKeys);
            int len = sortedKeys.size();
            for (int i = 0; i < len; i++) {
                Object obj = MailER_PosterEditActivity.this.txtShapeList.get(sortedKeys.get(i));
                if (obj instanceof MailER_TextInfo) {
                    MailER_AutofitTextRel rl = new MailER_AutofitTextRel(MailER_PosterEditActivity.this);
                    txt_stkr_rel.addView(rl);
                    rl.setTextInfo((MailER_TextInfo) obj, false);
                    rl.setId(View.generateViewId());
                    rl.setMainLayoutWH((float) MailER_PosterEditActivity.this.main_rel.getWidth(), (float) MailER_PosterEditActivity.this.main_rel.getHeight());
                    rl.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                    rl.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                    rl.setBorderVisibility(false);
                }
            }

            new createUndoRedoAsync(false, false, "nothing", "both").execute();
            if (mMaterialDialog != null && mMaterialDialog.isShowing())
                mMaterialDialog.dismiss();

            rl_watermark.setVisibility(View.VISIBLE); // visible

            main_rel.startAnimation(AnimationUtils.loadAnimation(MailER_PosterEditActivity.this, R.anim.spawner_bounce));
        }
    }

/*    @SuppressLint("StaticFieldLeak")
    private class saveTemplateAsync extends AsyncTask<String, String, String> {

        Bitmap thumbBit;
        boolean save;

        public saveTemplateAsync(boolean save) {
            this.save = save;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMaterialDialog = MaterialDialogUtils.getInstance().createAnimationDialog(PosterEditActivity.this);
            Objects.requireNonNull(mMaterialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            mMaterialDialog.setCancelable(false);
            mMaterialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            if (save) {
                if (rl_watermark.getVisibility() == View.GONE) {
                    savePath = BitmapUtils.saveBitmap(PosterEditActivity.this, BitmapUtils.viewToBitmap(PosterEditActivity.this, main_rel));//18042023
                    premiumPoster = true;
                } else {
                    savePath = BitmapUtils.saveBitmap(PosterEditActivity.this, BitmapUtils.viewToBitmap(PosterEditActivity.this, center_rel));//18042023
                    premiumPoster = false;
                }
                savePathWithWaterMark = BitmapUtils.saveBitmap(PosterEditActivity.this, BitmapUtils.viewToBitmap(PosterEditActivity.this, main_rel));//Add for next screen watermark show hide
                savePath = BitmapUtils.saveBitmap(PosterEditActivity.this, BitmapUtils.viewToBitmap(PosterEditActivity.this, center_rel));//Add for next screen watermark show hide

            }

            if (template_id != 0) {
                DatabaseHandler dh = DatabaseHandler.getDbHandler(PosterEditActivity.this);
                dh.deleteTemplateInfo(template_id);
                File folder = FileUtils.getSaveFileLocation(PosterEditActivity.this, "MyDesigns/" + path);
                File folder2 = FileUtils.getSaveFileLocation(PosterEditActivity.this, "category1/" + path);
                if (folder.exists()) {
                    FileUtils.deleteRecursive(folder);
                }
                if (folder2.exists()) {
                    FileUtils.deleteRecursive(folder2);
                }
            }

            center_rel.setDrawingCacheEnabled(true);
            thumbBit = Bitmap.createBitmap(center_rel.getDrawingCache());
            center_rel.setDrawingCacheEnabled(false);
            DatabaseHandler dh = null;

            String thumbPath = BitmapUtils.saveBitmapObject(PosterEditActivity.this, path, BitmapUtils.resizeBitmap(thumbBit, ((int) PosterEditActivity.this.screenWidth) / 2, ((int) PosterEditActivity.this.screenHeight) / 2));
            String bg_image = BitmapUtils.saveBitmapObject1(PosterEditActivity.this, path, bg_bitmap);

            TemplateInfo ti = new TemplateInfo();
            ti.setTHUMB_URI(thumbPath);
            ti.setFRAME_NAME(bg_image);
            ti.setTYPE("USER");
            ti.setTEMP_PATH(path);
            ti.setOVERLAY_NAME(effect_name);
            ti.setOVERLAY_OPACITY(effectController.effect_alpha_seekbar.getProgress());
            ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
            dh = DatabaseHandler.getDbHandler(PosterEditActivity.this);
            PosterEditActivity.this.saveComponent(dh.insertTemplateRow(ti), dh, path);
            return savePath;
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (mMaterialDialog != null && mMaterialDialog.isShowing())
                mMaterialDialog.dismiss();

            if (save) {
                if (path != null) {
                    MyApplication.showInterstitialAd(PosterEditActivity.this, PosterEditActivity.this::startIntent);
                } else {
                    Toast.makeText(PosterEditActivity.this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            saveDialog();
        }
    }*/
    @SuppressLint("StaticFieldLeak")
    private class saveTemplateAsync extends AsyncTask<String, String, String> {

        Bitmap thumbBit;
        boolean save;

        public saveTemplateAsync(boolean save) {
            this.save = save;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMaterialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PosterEditActivity.this);
            Objects.requireNonNull(mMaterialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            mMaterialDialog.setCancelable(false);
            mMaterialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            if (save) {
                if (rl_watermark.getVisibility() == View.GONE) {
                    savePath = MailER_BitmapUtils.saveBitmap(MailER_PosterEditActivity.this, MailER_BitmapUtils.viewToBitmap(MailER_PosterEditActivity.this, main_rel));//18042023
                    premiumPoster = true;
                } else {
                    savePath = MailER_BitmapUtils.saveBitmap(MailER_PosterEditActivity.this, MailER_BitmapUtils.viewToBitmap(MailER_PosterEditActivity.this, center_rel));//18042023
                    premiumPoster = false;
                }
//                savePathWithWaterMark = BitmapUtils.saveBitmap(PosterEditActivity.this, BitmapUtils.viewToBitmap(PosterEditActivity.this, main_rel));//Add for next screen watermark show hide
//                savePath = BitmapUtils.saveBitmap(PosterEditActivity.this, BitmapUtils.viewToBitmap(PosterEditActivity.this, center_rel));//Add for next screen watermark show hide

            }

            if (template_id != 0) {
                MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(MailER_PosterEditActivity.this);
                dh.deleteTemplateInfo(template_id);
                File folder = MailER_FileUtils.getSaveFileLocation(MailER_PosterEditActivity.this, "MyDesigns/" + path);
                File folder2 = MailER_FileUtils.getSaveFileLocation(MailER_PosterEditActivity.this, "category1/" + path);
                if (folder.exists()) {
                    MailER_FileUtils.deleteRecursive(folder);
                }
                if (folder2.exists()) {
                    MailER_FileUtils.deleteRecursive(folder2);
                }
            }

            center_rel.setDrawingCacheEnabled(true);
            thumbBit = Bitmap.createBitmap(center_rel.getDrawingCache());
            center_rel.setDrawingCacheEnabled(false);
            MailER_DatabaseHandler dh = null;

            String thumbPath = MailER_BitmapUtils.saveBitmapObject(MailER_PosterEditActivity.this, MailER_FileUtils.save_path, MailER_BitmapUtils.resizeBitmap(thumbBit, ((int) MailER_PosterEditActivity.this.screenWidth) / 2, ((int) MailER_PosterEditActivity.this.screenHeight) / 2));
            String bg_image = MailER_BitmapUtils.saveBitmapObject1(MailER_PosterEditActivity.this, MailER_FileUtils.save_path, bg_bitmap);

            MailER_TemplateInfo ti = new MailER_TemplateInfo();
            ti.setTHUMB_URI(thumbPath);
            ti.setFRAME_NAME(bg_image);
            ti.setTYPE("USER");
            ti.setTEMP_PATH(path);
            ti.setOVERLAY_NAME(effect_name);
            ti.setOVERLAY_OPACITY(effectController.effect_alpha_seekbar.getProgress());
            ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
            dh = MailER_DatabaseHandler.getDbHandler(MailER_PosterEditActivity.this);
            MailER_PosterEditActivity.this.saveComponent(dh.insertTemplateRow(ti), dh, path);
            return savePath;
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (mMaterialDialog != null && mMaterialDialog.isShowing())
                mMaterialDialog.dismiss();

            if (save) {
                if (path != null) {
                    MyApplication.showInterstitialAd(MailER_PosterEditActivity.this, MailER_PosterEditActivity.this::startIntent);
                } else {
                    Toast.makeText(MailER_PosterEditActivity.this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            saveDialog();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class createUndoRedoAsync extends AsyncTask<String, String, String> {

        boolean increment, notify;
        String operation, child_type;
        ArrayList<MailER_TextInfo> textInfoList = new ArrayList<>();
        ArrayList<MailER_StickerInfo> stickerInfoList = new ArrayList<>();

        public createUndoRedoAsync(boolean increment, boolean notify, String operation, String child_type) {
            this.increment = increment;
            this.notify = notify;
            this.operation = operation;
            this.child_type = child_type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            MailER_DatabaseHandler dh;
            MailER_TemplateInfo ti = new MailER_TemplateInfo();
            ti.setTHUMB_URI("");
            ti.setFRAME_NAME(bg_path);
            ti.setTYPE("USER");
            ti.setTEMP_PATH(path);
            ti.setOVERLAY_NAME(effect_name);
            ti.setOVERLAY_OPACITY(effectController.effect_alpha_seekbar.getProgress());
            ti.setOPERATION(operation);
            ti.setCHILD_TYPE(child_type);
            ti.setCHILD_POSITION(child_position);
            ti.setOLD_X((int) old_x);
            ti.setOLD_Y((int) old_y);

            if (template_type.equals("USER")) {
                ti.setRATIO(mainWidth + ":" + mainHeight);
            } else {
                ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
            }

            dh = MailER_DatabaseHandler.getDbHandler(MailER_PosterEditActivity.this);

            if (!notify) {
                int id = (int) dh.insertUndoTemplateRow(ti);
                MailER_PosterEditActivity.this.saveUndoData(id, dh);
                saveUndoData(id + 1);
            } else {
                dh.insertUndoTemplateRow(ti);
                List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.temp_txtShapeList.keySet());
                Collections.sort(sortedKeys);
                int len = sortedKeys.size();
                for (int i = 0; i < len; i++) {
                    Object obj = MailER_PosterEditActivity.this.temp_txtShapeList.get(sortedKeys.get(i));
                    if (obj instanceof MailER_StickerInfo) {
                        dh.insertUndoComponentInfoRow((MailER_StickerInfo) obj);
                    } else if (obj instanceof MailER_TextInfo) {
                        dh.insertUndoTextRow((MailER_TextInfo) obj);
                    }
                }
            }
            if (increment) {
                dh.deleteRedoTemplateInfo();
                undo_progress += 1;
                redo_progress = 0;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (undo_progress > 0) {
                btn_undo.setImageResource(R.drawable.spawner_ic_n_left_rotate);
            }
            if (redo_progress == 0) {
                btn_redo.setImageResource(R.drawable.spawner_ic_n_right_rotate);
            }
            if (notify) {
                notifyRedoData = true;
                MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
                int pos = dh.getUndoLstTemplateId();
                saveUndoData(pos + 1);
            }

            if (failedNotifyRedoData) {
                MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
                int pos = dh.getUndoLstTemplateId();
                saveUndoData(pos + 1);
                notifyRedoData = false;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class saveRedoAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMaterialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PosterEditActivity.this);
            Objects.requireNonNull(mMaterialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            mMaterialDialog.setCancelable(false);
            mMaterialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            MailER_DatabaseHandler dh;
            MailER_TemplateInfo ti = new MailER_TemplateInfo();

            ti.setTHUMB_URI("");
            ti.setFRAME_NAME(bg_path);
            ti.setTYPE("USER");
            ti.setTEMP_PATH(path);
            ti.setOVERLAY_NAME(effect_name);
            ti.setOVERLAY_OPACITY(effectController.effect_alpha_seekbar.getProgress());

            if (template_type.equals("USER")) {
                ti.setRATIO(mainWidth + ":" + mainHeight);
            } else {
                ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
            }

            dh = MailER_DatabaseHandler.getDbHandler(MailER_PosterEditActivity.this);

            int pos = dh.getUndoLstTemplateId();
            ti.setOPERATION(dh.getUndoOperation(pos));
            child_position = dh.getUndoChildPosition(pos);
            ti.setCHILD_POSITION(child_position);

            Point xyPos = dh.getUndoXYPosition(pos);

            ti.setOLD_X(xyPos.x);
            ti.setOLD_Y(xyPos.y);

            if (child_position != -1) {
                View view = txt_stkr_rel.getChildAt(child_position);
                if (view != null) {
                    old_x = view.getX();
                    old_y = view.getY();
                }
            }

            MailER_PosterEditActivity.this.saveRedoData(dh.insertRedoTemplateRow(ti), dh);
            return null;
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            new loadUndoTaskAsync().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class saveUndoAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMaterialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PosterEditActivity.this);
            Objects.requireNonNull(mMaterialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            mMaterialDialog.setCancelable(false);
            mMaterialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            MailER_DatabaseHandler dh;
            MailER_TemplateInfo ti = new MailER_TemplateInfo();
            ti.setTHUMB_URI("");
            ti.setFRAME_NAME(bg_path);
            ti.setTYPE("USER");
            ti.setTEMP_PATH(path);
            ti.setOVERLAY_NAME(effect_name);
            ti.setOVERLAY_OPACITY(effectController.effect_alpha_seekbar.getProgress());

            if (template_type.equals("USER")) {
                ti.setRATIO(mainWidth + ":" + mainHeight);
            } else {
                ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
            }

            dh = MailER_DatabaseHandler.getDbHandler(MailER_PosterEditActivity.this);

            int pos = dh.getRedoLstTemplateId();
            ti.setOPERATION(dh.getRedoOperation(pos));
            child_position = dh.getRedoChildPosition(pos);
            ti.setCHILD_POSITION(child_position);

            Point xyPos = dh.getRedoXYPosition(pos);

            ti.setOLD_X(xyPos.x);
            ti.setOLD_Y(xyPos.y);

            String child_type = dh.getRedoChildType(pos);
            if (child_type != null) {
                if (child_type.equals("text") || child_type.equals("sticker")) {
                    if (child_position != -1) {
                        old_x = xyPos.x;
                        old_y = xyPos.y;
                    }
                }
            } else {
                if (child_position != -1) {
                    View view = txt_stkr_rel.getChildAt(child_position);
                    if (view != null) {
                        old_x = view.getX();
                        old_y = view.getY();
                    }
                }
            }


            MailER_PosterEditActivity.this.saveUndoData(dh.insertUndoTemplateRow(ti), dh);

            return null;
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            new loadRedoTaskAsync().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadUndoTaskAsync extends AsyncTask<String, String, String> {

        Bitmap bg_bitmap;
        Drawable drawable = null;
        int overlay_opacity = 0;
        String operation;
        List<MailER_TemplateInfo> templateList = new ArrayList<>();
        ArrayList<MailER_TextInfo> textInfoList = new ArrayList<>();
        ArrayList<MailER_StickerInfo> stickerInfoList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
            int pos = dh.getUndoLstTemplateId();
            operation = dh.getUndoOperation(pos);

            textInfoList = dh.getUndoTextInfoList(pos);
            stickerInfoList = dh.getUndoComponentInfoList(pos, "STICKER");
            dh.deleteUndoTemplateInfo(pos);

            undo_progress -= 1;
            redo_progress += 1;
            MailER_PosterEditActivity.this.txtShapeList = new HashMap<>();
            for (MailER_StickerInfo ci : stickerInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ci.getORDER(), ci);
            }
            for (MailER_TextInfo ti : textInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ti.getORDER(), ti);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mMaterialDialog != null && mMaterialDialog.isShowing())
                mMaterialDialog.dismiss();
            if (undo_progress == 0) {
                btn_undo.setImageResource(R.drawable.spawner_ic_n_left_rotate);
            }
            if (redo_progress > 0) {
                btn_redo.setImageResource(R.drawable.spawner_ic_n_right_rotate);
            }
            txt_stkr_rel.removeAllViews();

            ViewAnimator.animate(main_rel)
                    .flash()
                    .duration(500)
                    .alpha(0.0f, 1)
                    .start();

            List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.txtShapeList.keySet());
            Collections.sort(sortedKeys);

            int len = sortedKeys.size();
            for (int i = 0; i < len; i++) {
                Object obj = MailER_PosterEditActivity.this.txtShapeList.get(sortedKeys.get(i));
                if (obj instanceof MailER_StickerInfo) {
                    MailER_ResizableStickerView riv;
                    riv = new MailER_ResizableStickerView(MailER_PosterEditActivity.this);
                    txt_stkr_rel.addView(riv);
                    riv.optimizeScreen(MailER_PosterEditActivity.this.screenWidth, MailER_PosterEditActivity.this.screenHeight);
                    riv.setMainLayoutWH(mainWidth, mainHeight);
                    riv.setComponentInfo((MailER_StickerInfo) obj, false);
                    riv.setId(View.generateViewId());
                    riv.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                    riv.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                    riv.setBorderVisibility(false);
                } else if (obj instanceof MailER_TextInfo) {
                    MailER_AutofitTextRel rl = new MailER_AutofitTextRel(MailER_PosterEditActivity.this);
                    txt_stkr_rel.addView(rl);
                    rl.setTextInfo((MailER_TextInfo) obj, false);
                    rl.setId(View.generateViewId());
                    rl.setMainLayoutWH(mainWidth, mainHeight);
                    rl.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                    rl.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                    rl.setBorderVisibility(false);
                }
            }

            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
            int t_pos = dh.getUndoLstTemplateId();
            saveUndoData(t_pos + 1);

            if (operation != null && operation.equals("move")) {
                View view = txt_stkr_rel.getChildAt(child_position);
                if (view != null) {
                    new_x = view.getX();
                    new_y = view.getY();

                    view.setX(old_x);
                    view.setY(old_y);

                    ViewAnimator.animate(view)
                            .translationX(new_x)
                            .translationY(new_y)
                            .duration(500)
                            .start();
                }
            }
            new Handler().postDelayed(() -> {
                btn_undo.setVisibility(View.VISIBLE);
                btn_redo.setVisibility(View.VISIBLE);
                progress_undo_redo.setVisibility(View.GONE);
            }, 500);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadRedoTaskAsync extends AsyncTask<String, String, String> {

        Bitmap bg_bitmap;
        Drawable drawable = null;
        int overlay_opacity = 0;
        ArrayList<MailER_TextInfo> textInfoList = new ArrayList<>();
        ArrayList<MailER_StickerInfo> stickerInfoList = new ArrayList<>();
        String operation;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
            int pos = dh.getRedoLstTemplateId();
            operation = dh.getRedoOperation(pos);
            MailER_TemplateInfo templateInfo = dh.getRedoTemplateDes(pos);
            textInfoList = dh.getRedoTextInfoList(pos);
            stickerInfoList = dh.getRedoComponentInfoList(pos, "STICKER");
            dh.deleteRedoTemplateInfo(pos);

            undo_progress += 1;
            redo_progress -= 1;
            MailER_PosterEditActivity.this.txtShapeList = new HashMap<>();
            for (MailER_StickerInfo ci : stickerInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ci.getORDER(), ci);
            }
            for (MailER_TextInfo ti : textInfoList) {
                MailER_PosterEditActivity.this.txtShapeList.put(ti.getORDER(), ti);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mMaterialDialog != null && mMaterialDialog.isShowing())
                mMaterialDialog.dismiss();
            if (undo_progress > 0) {
                btn_undo.setImageResource(R.drawable.spawner_ic_n_left_rotate);
            }
            if (redo_progress == 0) {
                btn_redo.setImageResource(R.drawable.spawner_ic_n_right_rotate);
            }
            txt_stkr_rel.removeAllViews();

            ViewAnimator.animate(main_rel)
                    .flash()
                    .duration(500)
                    .alpha(0.0f, 1)
                    .start();

            List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.txtShapeList.keySet());
            Collections.sort(sortedKeys);
            int len = sortedKeys.size();
            for (int i = 0; i < len; i++) {
                Object obj = MailER_PosterEditActivity.this.txtShapeList.get(sortedKeys.get(i));
                if (obj instanceof MailER_StickerInfo) {
                    MailER_ResizableStickerView riv;
                    riv = new MailER_ResizableStickerView(MailER_PosterEditActivity.this);
                    txt_stkr_rel.addView(riv);
                    riv.optimizeScreen(MailER_PosterEditActivity.this.screenWidth, MailER_PosterEditActivity.this.screenHeight);
                    riv.setMainLayoutWH(mainWidth, mainHeight);
                    riv.setComponentInfo((MailER_StickerInfo) obj, false);
                    riv.setId(View.generateViewId());
                    riv.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                    riv.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                    riv.setBorderVisibility(false);
                } else if (obj instanceof MailER_TextInfo) {
                    MailER_AutofitTextRel rl = new MailER_AutofitTextRel(MailER_PosterEditActivity.this);
                    txt_stkr_rel.addView(rl);
                    rl.setTextInfo((MailER_TextInfo) obj, false);
                    rl.setId(View.generateViewId());
                    rl.setMainLayoutWH(mainWidth, mainHeight);
                    rl.optimize(MailER_PosterEditActivity.this.wr, MailER_PosterEditActivity.this.hr);
                    rl.setOnTouchCallbackListener(MailER_PosterEditActivity.this);
                    rl.setBorderVisibility(false);
                }
            }

            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());
            int pos = dh.getUndoLstTemplateId();
            saveUndoData(pos + 1);

            if (operation != null && operation.equals("move")) {
                View view = txt_stkr_rel.getChildAt(child_position);
                if (view != null) {
                    new_x = view.getX();
                    new_y = view.getY();

                    view.setX(old_x);
                    view.setY(old_y);

                    ViewAnimator.animate(view)
                            .translationX(new_x)
                            .translationY(new_y)
                            .duration(500)
                            .start();
                }
            }


            new Handler().postDelayed(() -> {
                btn_undo.setVisibility(View.VISIBLE);
                btn_redo.setVisibility(View.VISIBLE);
                progress_undo_redo.setVisibility(View.GONE);
            }, 500);

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class saveTempUndoRedoTaskAsync extends AsyncTask<String, String, String> {
        String child_type, operation;
        int position;

        public saveTempUndoRedoTaskAsync(String child_type, String operation, int position) {
            this.child_type = child_type;
            this.operation = operation;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(getApplicationContext());

            MailER_TemplateInfo ti = new MailER_TemplateInfo();
            ti.setTHUMB_URI("");
            ti.setFRAME_NAME(bg_path);
            ti.setTYPE("USER");
            ti.setTEMP_PATH(path);
            ti.setOVERLAY_NAME(effect_name);
            ti.setOVERLAY_OPACITY(effectController.effect_alpha_seekbar.getProgress());
            ti.setOPERATION(operation);
            ti.setCHILD_TYPE(child_type);
            ti.setCHILD_POSITION(position);
            ti.setOLD_X((int) old_x);
            ti.setOLD_Y((int) old_y);

            if (template_type.equals("USER")) {
                ti.setRATIO(mainWidth + ":" + mainHeight);
            } else {
                ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
            }

            int id = (int) dh.insertUndoTemplateRow(ti);
            List<Integer> sortedKeys = new ArrayList<>(MailER_PosterEditActivity.this.temp_txtShapeList.keySet());
            Collections.sort(sortedKeys);
            int len = sortedKeys.size();
            for (int i = 0; i < len; i++) {
                Object obj = MailER_PosterEditActivity.this.temp_txtShapeList.get(sortedKeys.get(i));
                if (obj instanceof MailER_StickerInfo) {
                    ((MailER_StickerInfo) obj).setTEMPLATE_ID(id);
                    dh.insertUndoComponentInfoRow((MailER_StickerInfo) obj);
                } else if (obj instanceof MailER_TextInfo) {
                    ((MailER_TextInfo) obj).setTEMPLATE_ID(id);
                    dh.insertUndoTextRow((MailER_TextInfo) obj);
                }
            }

            saveUndoData(id + 1);

            undo_progress += 1;
            dh.deleteRedoTemplateInfo();
            redo_progress = 0;

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (undo_progress > 0) {
                btn_undo.setImageResource(R.drawable.spawner_ic_n_left_rotate);
            }
            if (redo_progress == 0) {
                btn_redo.setImageResource(R.drawable.spawner_ic_n_right_rotate);
            }
        }
    }

}