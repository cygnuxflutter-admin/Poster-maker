package com.festival.flyer.postermaker.makeityours.auto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.graphics.Typeface;
import android.widget.SeekBar;
import android.widget.HorizontalScrollView;
import android.view.MotionEvent;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_ShareActivity;
import com.festival.flyer.postermaker.utils.MailER_BitmapUtils;
import com.festival.flyer.postermaker.view.MailER_AspectRatioFrameLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class MailER_AutoPosterEditActivity extends AppCompatActivity {
    private ImageView btn_undo, btn_redo;
    
    private class ViewState {
        View view;
        float x, y, scaleX, scaleY;
        int color;
        Typeface font;
        float textSize;
        int width, height;
        int visibility;
    }

    private class PosterState {
        List<ViewState> states = new ArrayList<>();
    }

    private Stack<PosterState> undoStack = new Stack<>();
    private Stack<PosterState> redoStack = new Stack<>();

    private void saveState(Stack<PosterState> stack) {
        PosterState ps = new PosterState();
        for (int i = 0; i < txt_stkr_rel.getChildCount(); i++) {
            View child = txt_stkr_rel.getChildAt(i);
            ViewState vs = new ViewState();
            vs.view = child;
            vs.x = child.getX();
            vs.y = child.getY();
            vs.scaleX = child.getScaleX();
            vs.scaleY = child.getScaleY();
            vs.width = child.getLayoutParams().width;
            vs.height = child.getLayoutParams().height;
            vs.visibility = child.getVisibility();
            
            if (child instanceof TextView) {
                vs.color = ((TextView) child).getCurrentTextColor();
                vs.font = ((TextView) child).getTypeface();
                vs.textSize = ((TextView) child).getTextSize();
            } else if (child instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) child).getChildCount(); j++) {
                    View sc = ((LinearLayout) child).getChildAt(j);
                    if (sc instanceof TextView) {
                        vs.color = ((TextView) sc).getCurrentTextColor();
                        vs.font = ((TextView) sc).getTypeface();
                        vs.textSize = ((TextView) sc).getTextSize();
                    }
                }
            }
            ps.states.add(vs);
        }
        stack.push(ps);
    }
    
    private void restoreState(PosterState ps) {
        for (ViewState vs : ps.states) {
            View child = vs.view;
            child.setX(vs.x);
            child.setY(vs.y);
            child.setScaleX(vs.scaleX);
            child.setScaleY(vs.scaleY);
            child.setVisibility(vs.visibility);
            android.view.ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (lp != null) {
                lp.width = vs.width;
                lp.height = vs.height;
                child.setLayoutParams(lp);
            }
            
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(vs.color);
                ((TextView) child).setTypeface(vs.font);
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, vs.textSize);
            } else if (child instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) child).getChildCount(); j++) {
                    View sc = ((LinearLayout) child).getChildAt(j);
                    if (sc instanceof TextView) {
                        ((TextView) sc).setTextColor(vs.color);
                        ((TextView) sc).setTypeface(vs.font);
                        ((TextView) sc).setTextSize(TypedValue.COMPLEX_UNIT_PX, vs.textSize);
                    } else if (sc instanceof ImageView) {
                        ((ImageView) sc).setColorFilter(vs.color);
                    }
                }
            }
        }
    }


    private MailER_AspectRatioFrameLayout center_rel;
    private RelativeLayout main_rel, txt_stkr_rel;
    private ImageView background_img, btn_save_poster;
    private Button btn_size_square, btn_size_portrait;
    private LinearLayout ll_size_toggle;

    private JSONObject squareObj = null;
    private JSONObject portraitObj = null;
    private LinearLayout ll_mini_editor, ll_color_container, ll_font_container;
    private TextView tab_color, tab_font, tab_size;
    private View btn_delete_element;
    private HorizontalScrollView hsv_colors, hsv_fonts;
    private SeekBar sb_size;
    
    
    private View activeView = null;
    
    private String[] standardColors = {
        "#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF", 
        "#FFFF00", "#00FFFF", "#FF00FF", "#808080", "#FFA500", 
        "#800080", "#008000", "#800000", "#000080", "#008080"
    };


    private MaterialDialog materialDialog;

    public void startMaterialDialog() {
        if (isFinishing() || isDestroyed()) return;
        if (materialDialog != null && materialDialog.isShowing()) {
            return;
        }
        materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(this);
        if (materialDialog.getWindow() != null) {
            materialDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        materialDialog.setCancelable(false);
        materialDialog.show();
    }

    public void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_auto_poster_edit);

        center_rel = findViewById(R.id.center_rel);
        main_rel = findViewById(R.id.main_rel);
        background_img = findViewById(R.id.background_img);
        txt_stkr_rel = findViewById(R.id.txt_stkr_rel);
                btn_save_poster = findViewById(R.id.btn_save_poster);
        btn_undo = findViewById(R.id.btn_undo);
        btn_redo = findViewById(R.id.btn_redo);
        
        btn_undo.setOnClickListener(v -> {
            if (!undoStack.isEmpty()) {
                saveState(redoStack);
                restoreState(undoStack.pop());
                Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
            }
        });
        
        btn_redo.setOnClickListener(v -> {
            if (!redoStack.isEmpty()) {
                saveState(undoStack);
                restoreState(redoStack.pop());
                Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
            }
        });

        ll_mini_editor = findViewById(R.id.ll_mini_editor);
        ll_color_container = findViewById(R.id.ll_color_container);
        ll_font_container = findViewById(R.id.ll_font_container);
        tab_color = findViewById(R.id.tab_color);
        tab_font = findViewById(R.id.tab_font);
        tab_size = findViewById(R.id.tab_size);
        btn_delete_element = findViewById(R.id.btn_delete_element);
        hsv_colors = findViewById(R.id.hsv_colors);
        hsv_fonts = findViewById(R.id.hsv_fonts);
        sb_size = findViewById(R.id.sb_size);
                
        
        
        setupMiniEditor();

        btn_size_square = findViewById(R.id.btn_size_square);
        btn_size_portrait = findViewById(R.id.btn_size_portrait);
        ll_size_toggle = findViewById(R.id.ll_size_toggle);

        findViewById(R.id.btn_backPress).setOnClickListener(v -> finish());
        
        // Hide watermarks for pro version/auto posters
        try {
            findViewById(R.id.rl_watermark).setVisibility(View.GONE);
        } catch (Exception e) {}

        // Hide unnecessary stuff from original layout
        try {
            findViewById(R.id.fl_bottom_popups).setVisibility(View.GONE);
            findViewById(R.id.lay_container).setVisibility(View.GONE);
            
            
            findViewById(R.id.btn_layControls).setVisibility(View.GONE);
        } catch (Exception e) {}

        // Restore ll_size_toggle visibility since we want it
        if (ll_size_toggle != null) {
            ll_size_toggle.setVisibility(View.VISIBLE);
        }

        try {
            String designId = getIntent().getStringExtra("design_id");
            String allPostersStr = getIntent().getStringExtra("full_raw_data");

            if (allPostersStr != null) {
                JSONArray allPosters = new JSONArray(allPostersStr);
                for (int i = 0; i < allPosters.length(); i++) {
                    JSONObject obj = allPosters.getJSONObject(i);
                    String dId = obj.optString("design_id", obj.optString("id", ""));
                    if (dId.equals(designId)) {
                        if (obj.optString("ratio", "1:1").equals("1:1")) {
                            squareObj = obj;
                        } else if (obj.optString("ratio", "").equals("9:16")) {
                            portraitObj = obj;
                        }
                    }
                }
            }

            if (btn_size_square != null && btn_size_portrait != null) {
                if (squareObj == null || portraitObj == null) {
                    if (ll_size_toggle != null) ll_size_toggle.setVisibility(View.GONE);
                } else {
                    btn_size_square.setOnClickListener(v -> renderAutoPoster(squareObj, btn_size_square, btn_size_portrait));
                    btn_size_portrait.setOnClickListener(v -> renderAutoPoster(portraitObj, btn_size_portrait, btn_size_square));
                }

                if (squareObj != null) {
                    renderAutoPoster(squareObj, btn_size_square, btn_size_portrait);
                } else if (portraitObj != null) {
                    renderAutoPoster(portraitObj, btn_size_portrait, btn_size_square);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_save_poster.setOnClickListener(v -> handleSaveOrDownload(false));
        ImageView btn_download_poster = findViewById(R.id.btn_download_poster);
        if (btn_download_poster != null) {
            btn_download_poster.setOnClickListener(v -> handleSaveOrDownload(true));
        }
        
        main_rel.setOnClickListener(v -> {
            if (ll_mini_editor != null) ll_mini_editor.setVisibility(View.GONE);
            if (activeView != null) {
                          saveState(undoStack); redoStack.clear();
                activeView.setBackground(null);
                activeView.setPadding(0,0,0,0);
                activeView = null;
            }
        });
    }

    private void renderAutoPoster(JSONObject posterObj, Button activeBtn, Button inactiveBtn) {
        startMaterialDialog();
        if (activeBtn != null && inactiveBtn != null) {
            activeBtn.setBackgroundResource(R.drawable.spawner_tab_pill_selector);
            activeBtn.setSelected(true);
            activeBtn.setTextColor(Color.WHITE);

            inactiveBtn.setBackgroundResource(R.drawable.spawner_tab_pill_selector);
            inactiveBtn.setSelected(false);
            inactiveBtn.setTextColor(Color.BLACK);
        }

        txt_stkr_rel.removeAllViews();

        String posterUrl = posterObj.optString("poster_image", posterObj.optString("image_url"));
        JSONObject personalization = posterObj.optJSONObject("personalization");

        Glide.with(this).asBitmap().load(posterUrl).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                float ratio = (float) resource.getWidth() / resource.getHeight();
                center_rel.setAspectRatio(ratio);
                
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                main_rel.getLayoutParams().width = screenWidth;
                main_rel.getLayoutParams().height = (int) (screenWidth / ratio);
                
                background_img.getLayoutParams().width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                background_img.getLayoutParams().height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                background_img.setScaleType(ImageView.ScaleType.FIT_XY);
                main_rel.postInvalidate();
                main_rel.requestLayout();
                background_img.setImageBitmap(resource);

                if (personalization != null) {
                    float baseWidth = personalization.optInt("canvas_width", resource.getWidth() > 0 ? resource.getWidth() : 1080);
                    float scale = (float) screenWidth / baseWidth;
                    applyPersonalizationSlots(personalization.optJSONObject("slots"), personalization.optString("theme", "dark"), scale, resource);
                }
                main_rel.post(() -> dismissMaterialDialog());
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                dismissMaterialDialog();
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                dismissMaterialDialog();
            }
        });
    }

    private void applyPersonalizationSlots(JSONObject slots, String theme, float scale, android.graphics.Bitmap resource) {
        if (slots == null) return;

        android.content.SharedPreferences prefs = getSharedPreferences("BusinessProfile", MODE_PRIVATE);
        String userName = prefs.getString("business_name", "My Business");
        String userPhone = prefs.getString("phone", "1234567890");
        String userLogo = prefs.getString("logo_uri", "");
        String userAddress = prefs.getString("address", "");

        int defaultColor = theme.equals("dark") ? android.graphics.Color.WHITE : android.graphics.Color.BLACK;

        try {
            if (resource != null) {
                androidx.palette.graphics.Palette palette = androidx.palette.graphics.Palette.from(resource).generate();
                androidx.palette.graphics.Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch == null) swatch = palette.getLightVibrantSwatch();
                if (swatch == null) swatch = palette.getDarkVibrantSwatch();
                
                if (swatch != null) {
                    defaultColor = swatch.getRgb();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (slots.has("logo") && !userLogo.isEmpty()) {
            JSONObject logoSlot = slots.optJSONObject("logo");
            if (logoSlot != null) {
                ImageView ivLogo = new ImageView(this);
                ivLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
                
                int lw = logoSlot.optInt("w", 150);
                int lh = logoSlot.optInt("h", 150);
                if (lw <= 0) lw = 150;
                if (lh <= 0) lh = 150;
                
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)(lw * scale), (int)(lh * scale));
                lp.leftMargin = (int)(logoSlot.optInt("x", 50) * scale);
                lp.topMargin = (int)(logoSlot.optInt("y", 50) * scale);
                ivLogo.setLayoutParams(lp);
                if (userLogo.startsWith("http") || userLogo.startsWith("content")) { Glide.with(this).load(userLogo).into(ivLogo); } else { Glide.with(this).load(new java.io.File(userLogo)).into(ivLogo); }
                txt_stkr_rel.addView(ivLogo);
                makeDraggable(ivLogo);
            }
        }

        applyTextSlot("business_name", slots.optJSONObject("business_name"), userName, defaultColor, true, scale);
        applyTextSlot("phone", slots.optJSONObject("phone"), userPhone, defaultColor, false, scale);
        applyTextSlot("address", slots.optJSONObject("address"), userAddress, defaultColor, false, scale);
    }
    
    private void applyTextSlot(String slotKey, JSONObject slotData, String textValue, int defaultColor, boolean isBold, float scale) {
        if (slotData == null || textValue == null || textValue.isEmpty()) return;
        
        android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText(textValue);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setIncludeFontPadding(false);
        
        if (isBold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        String colorHex = slotData.optString("color", "");
        if (!colorHex.isEmpty()) {
            try { textView.setTextColor(android.graphics.Color.parseColor(colorHex)); } catch (Exception e) { textView.setTextColor(defaultColor); }
        } else { 
            textView.setTextColor(defaultColor); 
        }
        
        textView.setMaxLines(2);
        
        int w = slotData.optInt("w", -2);
        int h = slotData.optInt("h", -2);
        if (w > 0) w = (int)(w * scale);
        if (h > 0) h = (int)(h * scale);
        
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
        lp.leftMargin = (int)(slotData.optInt("x") * scale);
        lp.topMargin = (int)(slotData.optInt("y") * scale);
        textView.setLayoutParams(lp);
        
        if (w > 0 && h > 0) {
            int maxFontSize = slotData.optInt("size", slotData.optInt("fontSize", 100));
            if (maxFontSize <= 0) maxFontSize = 100;
            if (maxFontSize < 35) maxFontSize = (int)(maxFontSize * 2.5f);
            androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, 6, maxFontSize, 1, android.util.TypedValue.COMPLEX_UNIT_PX);
        } else {
            int fallbackSize = (int) slotData.optDouble("fontSize", 30.0);
            textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fallbackSize);
        }
        
        txt_stkr_rel.addView(textView);
        makeDraggable(textView);
        
        if (slotData.has("icon")) {
            JSONObject iconSlot = slotData.optJSONObject("icon");
            if (iconSlot != null) {
                ImageView ivIcon = new ImageView(this);
                
                String iconType = iconSlot.optString("type", "");
                int iconRes = 0;
                if (iconType.equals("location") || slotKey.equals("address")) iconRes = com.festival.flyer.postermaker.R.drawable.spawner_ic_location;
                else if (iconType.equals("phone") || slotKey.equals("phone")) iconRes = com.festival.flyer.postermaker.R.drawable.spawner_ic_phone;
                else if (iconType.equals("email") || slotKey.equals("email")) iconRes = com.festival.flyer.postermaker.R.drawable.spawner_ic_email;
                
                if (iconRes != 0) {
                    ivIcon.setImageResource(iconRes);
                    String iconColorHex = iconSlot.optString("color", "");
                    int iconColor = defaultColor;
                    if (!iconColorHex.isEmpty()) {
                        try { iconColor = android.graphics.Color.parseColor(iconColorHex); } catch (Exception e) {}
                    }
                    ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconColor));

                    int iw = iconSlot.optInt("w", 50);
                    int ih = iconSlot.optInt("h", 50);
                    if (iw <= 0) iw = 50;
                    if (ih <= 0) ih = 50;

                    RelativeLayout.LayoutParams iconLp = new RelativeLayout.LayoutParams((int)(iw * scale), (int)(ih * scale));
                    iconLp.leftMargin = (int)(iconSlot.optInt("x") * scale);
                    iconLp.topMargin = (int)(iconSlot.optInt("y") * scale);
                    ivIcon.setLayoutParams(iconLp);
                    ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    
                    txt_stkr_rel.addView(ivIcon);
                    makeDraggable(ivIcon);

                    textView.setTag(ivIcon);
                    ivIcon.setTag(textView);
                }
            }
        }
    }

    private void setupMiniEditor() {
        if (tab_color == null) return;

        if (btn_delete_element != null) {
            btn_delete_element.setOnClickListener(v -> {
                if (activeView != null) {
                    saveState(undoStack);
                    redoStack.clear();

                    View associated = null;
                    if (activeView.getTag() instanceof View) {
                        associated = (View) activeView.getTag();
                    }

                    activeView.setVisibility(View.GONE);
                    activeView.setBackground(null);
                    activeView.setPadding(0, 0, 0, 0);

                    if (associated != null) {
                        associated.setVisibility(View.GONE);
                        associated.setBackground(null);
                        associated.setPadding(0, 0, 0, 0);
                    }

                    activeView = null;
                    if (ll_mini_editor != null) {
                        ll_mini_editor.setVisibility(View.GONE);
                    }
                    txt_stkr_rel.invalidate();
                    main_rel.invalidate();
                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Select an item to delete", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (ll_mini_editor != null) {
            ll_mini_editor.setOnClickListener(v -> {
                // Prevent click from propagating to poster background
            });
        }

        // Tab UI Helper
        Runnable resetTabs = () -> {
            tab_color.setSelected(false);
            tab_color.setTextColor(Color.BLACK);
            tab_font.setSelected(false);
            tab_font.setTextColor(Color.BLACK);
            tab_size.setSelected(false);
            tab_size.setTextColor(Color.BLACK);
        };

        tab_color.setOnClickListener(v -> {
            hsv_colors.setVisibility(View.VISIBLE);
            hsv_fonts.setVisibility(View.GONE);
            sb_size.setVisibility(View.GONE);
            resetTabs.run();
            tab_color.setSelected(true);
            tab_color.setTextColor(Color.WHITE);
        });
        tab_font.setOnClickListener(v -> {
            hsv_colors.setVisibility(View.GONE);
            hsv_fonts.setVisibility(View.VISIBLE);
            sb_size.setVisibility(View.GONE);
            resetTabs.run();
            tab_font.setSelected(true);
            tab_font.setTextColor(Color.WHITE);
        });
        tab_size.setOnClickListener(v -> {
            hsv_colors.setVisibility(View.GONE);
            hsv_fonts.setVisibility(View.GONE);
            sb_size.setVisibility(View.VISIBLE);
            resetTabs.run();
            tab_size.setSelected(true);
            tab_size.setTextColor(Color.WHITE);
        });
        
        // set default selected
        tab_color.performClick();

        // Setup Colors
        if (ll_color_container != null) {
            ll_color_container.removeAllViews();
            int sizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
            int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
            for (String colorHex : standardColors) {
                View colorView = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
                lp.setMargins(marginPx, marginPx, marginPx, marginPx);
                colorView.setLayoutParams(lp);
                android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                gd.setColor(Color.parseColor(colorHex));
                gd.setStroke(2, Color.parseColor("#CCCCCC"));
                colorView.setBackground(gd);
                colorView.setOnClickListener(v -> {
                    if (activeView != null) {
                        saveState(undoStack); redoStack.clear();
                        int c = Color.parseColor(colorHex);
                        if (activeView instanceof TextView) {
                            ((TextView) activeView).setTextColor(c);
                            if (activeView.getTag() instanceof ImageView) {
                                ((ImageView) activeView.getTag()).setImageTintList(android.content.res.ColorStateList.valueOf(c));
                            }
                        } else if (activeView instanceof ImageView) {
                            ((ImageView) activeView).setColorFilter(c);
                            if (activeView.getTag() instanceof TextView) {
                                ((TextView) activeView.getTag()).setTextColor(c);
                            }
                        } else if (activeView instanceof LinearLayout) {
                            for (int i=0; i<((LinearLayout)activeView).getChildCount(); i++) {
                                View child = ((LinearLayout)activeView).getChildAt(i);
                                if (child instanceof TextView) ((TextView) child).setTextColor(c);
                                if (child instanceof ImageView) ((ImageView) child).setColorFilter(c);
                            }
                        }
                    }
                });
                ll_color_container.addView(colorView);
            }
        }

        // Setup Fonts
        if (ll_font_container != null) {
            ll_font_container.removeAllViews();
            try {
                String[] assetsFiles = getAssets().list("font");
                if (assetsFiles != null) {
                    for (String fontName : assetsFiles) {
                        if (fontName.endsWith(".ttf") || fontName.endsWith(".otf")) {
                            TextView fontView = new TextView(this);
                            fontView.setText("Abc");
                            fontView.setTextSize(20);
                            fontView.setPadding(20, 10, 20, 10);
                            try {
                                Typeface tf = Typeface.createFromAsset(getAssets(), "font/" + fontName);
                                fontView.setTypeface(tf);
                            } catch (Exception e) {}
                            
                            fontView.setOnClickListener(v -> {
                                if (activeView != null) {
                                    saveState(undoStack); redoStack.clear();
                                    try {
                                        Typeface tf = Typeface.createFromAsset(getAssets(), "font/" + fontName);
                              if (activeView instanceof TextView) {
                                            ((TextView) activeView).setTypeface(tf);
                                        } else if (activeView instanceof LinearLayout) {
                                            for (int i=0; i<((LinearLayout)activeView).getChildCount(); i++) {
                                                View child = ((LinearLayout)activeView).getChildAt(i);
                                                if (child instanceof TextView) ((TextView) child).setTypeface(tf);
                                            }
                                        }
                                    } catch (Exception e) {}
                                }
                            });
                            ll_font_container.addView(fontView);
                        }
                    }
                }
            } catch (Exception e) {}
        }

        // Setup Size
        if (sb_size != null) {
            sb_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (activeView != null && fromUser) {
                        float scale = 0.5f + (progress / 100f) * 2.5f;
                        activeView.setScaleX(scale);
                        activeView.setScaleY(scale);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (activeView != null) {
                        saveState(undoStack); redoStack.clear();
                    }
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private void makeDraggable(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            float assocDiffX, assocDiffY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        saveState(undoStack); redoStack.clear();
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        if (v.getTag() instanceof View) {
                            View assoc = (View) v.getTag();
                            assocDiffX = assoc.getX() - v.getX();
                            assocDiffY = assoc.getY() - v.getY();
                        }
                        if (activeView != null && activeView != v) {
                            activeView.setBackground(null);
                            activeView.setPadding(0,0,0,0);
                        }
                        activeView = v;
                        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                        border.setColor(android.graphics.Color.TRANSPARENT);
                        border.setStroke(3, android.graphics.Color.parseColor("#7E57C2"), 10, 10);
                        activeView.setBackground(border);
                        activeView.setPadding(4,4,4,4);
                        if (ll_mini_editor != null) ll_mini_editor.setVisibility(View.VISIBLE);
                        
                        // setup seekbar max and progress
                        if (sb_size != null) {
                            float currentScale = v.getScaleX();
                            int progress = (int) (((currentScale - 0.5f) / 2.5f) * 100f);
                            if (progress < 0) progress = 0;
                            if (progress > 100) progress = 100;
                            sb_size.setProgress(progress);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        v.setX(newX);
                        v.setY(newY);
                        if (v.getTag() instanceof View) {
                            View assoc = (View) v.getTag();
                            assoc.setX(newX + assocDiffX);
                            assoc.setY(newY + assocDiffY);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void handleSaveOrDownload(boolean isDownload) {
        com.festival.flyer.postermaker.utils.MailER_PreferenceClass prefManager = new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this);

        boolean isDownloadAdEnabled = true;
        int freeAllowed = 2;
        String adType = "reward";

        try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig rc = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
            if (rc != null && rc.getInfo().getLastFetchStatus() == com.google.firebase.remoteconfig.FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS) {
                isDownloadAdEnabled = rc.getBoolean("editor_download_ad_enabled");
                if (rc.getLong("editor_free_downloads_allowed") >= 0) {
                    freeAllowed = (int) rc.getLong("editor_free_downloads_allowed");
                }
                if (rc.getString("editor_download_ad_type") != null && !rc.getString("editor_download_ad_type").isEmpty()) {
                    adType = rc.getString("editor_download_ad_type");
                }
            } else {
                isDownloadAdEnabled = prefManager.getBoolean("editor_download_ad_enabled", true);
                freeAllowed = prefManager.getInt("editor_free_downloads_allowed", 2);
                adType = prefManager.getDataType("editor_download_ad_type", "reward");
            }
        } catch (Exception e) {
            isDownloadAdEnabled = prefManager.getBoolean("editor_download_ad_enabled", true);
            freeAllowed = prefManager.getInt("editor_free_downloads_allowed", 2);
            adType = prefManager.getDataType("editor_download_ad_type", "reward");
        }

        if (!isDownloadAdEnabled) {
            new SaveTask(isDownload).execute();
            return;
        }

        int totalDownloads = prefManager.getInt("TotalDownloadsCount", 0);
        if (totalDownloads < freeAllowed) {
            prefManager.setInt("TotalDownloadsCount", totalDownloads + 1);
            new SaveTask(isDownload).execute();
            return;
        }

        if ("interstitial".equalsIgnoreCase(adType)) {
            com.festival.flyer.postermaker.MyApplication.showInterstitialAd(this, () -> {
                new SaveTask(isDownload).execute();
            });
        } else {
            showRewardDownloadPromptDialog(isDownload);
        }
    }

    private void showRewardDownloadPromptDialog(boolean isDownload) {
        if (isFinishing() || isDestroyed()) return;

        android.app.Dialog dialog = new android.app.Dialog(this, 16974126);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.spawner_dialog_watermark_download);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView btnRemoveWatermark = dialog.findViewById(R.id.btn_remove_watermark);
        TextView btnCancelDialog = dialog.findViewById(R.id.btn_cancel_dialog);

        btnCancelDialog.setOnClickListener(v -> dialog.dismiss());

        btnRemoveWatermark.setOnClickListener(v -> {
            btnRemoveWatermark.setEnabled(false);
            com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager.showRewardVideoAd(this, new com.festival.flyer.postermaker.adManager.MailER_RewardVideoManager.OnRewardVideoResultListener() {
                @Override
                public void onRewardEarned() {
                    btnRemoveWatermark.setEnabled(true);
                    dialog.dismiss();
                    Toast.makeText(MailER_AutoPosterEditActivity.this, "Downloading HD Poster...", Toast.LENGTH_SHORT).show();
                    new SaveTask(isDownload).execute();
                }

                @Override
                public void onRewardSkipped() {
                    btnRemoveWatermark.setEnabled(true);
                    Toast.makeText(MailER_AutoPosterEditActivity.this, "Watch full ad to download poster!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private class SaveTask extends AsyncTask<Void, Void, String> {
        boolean isDownload;
        public SaveTask(boolean isDownload) {
            this.isDownload = isDownload;
        }

        @Override
        protected void onPreExecute() {
            if (activeView != null) {
                          saveState(undoStack); redoStack.clear();
                activeView.setBackground(null);
                activeView.setPadding(0,0,0,0);
                activeView = null;
            }
            if (ll_mini_editor != null) ll_mini_editor.setVisibility(View.GONE);
            Toast.makeText(MailER_AutoPosterEditActivity.this, "Saving Poster...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Bitmap bitmap = MailER_BitmapUtils.viewToBitmap(MailER_AutoPosterEditActivity.this, main_rel);
                if (bitmap != null) {
                    if (isDownload) {
                        return MailER_BitmapUtils.saveBitmap(MailER_AutoPosterEditActivity.this, bitmap);
                    } else {
                        // Save to internal app storage (My Poster)
                        String path = String.valueOf(System.currentTimeMillis());
                        Bitmap thumbBit = MailER_BitmapUtils.resizeBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                        String thumbPath = MailER_BitmapUtils.saveBitmapObject(MailER_AutoPosterEditActivity.this, path, thumbBit);
                        String bg_image = MailER_BitmapUtils.saveBitmapObject1(MailER_AutoPosterEditActivity.this, path, bitmap);
                        
                        com.festival.flyer.postermaker.utils.MailER_DatabaseHandler dh = com.festival.flyer.postermaker.utils.MailER_DatabaseHandler.getDbHandler(MailER_AutoPosterEditActivity.this);
                        com.festival.flyer.postermaker.components.MailER_TemplateInfo ti = new com.festival.flyer.postermaker.components.MailER_TemplateInfo();
                        ti.setTHUMB_URI(thumbPath);
                        ti.setFRAME_NAME(bg_image);
                        ti.setTYPE("USER");
                        ti.setTEMP_PATH(path);
                        ti.setOVERLAY_NAME("");
                        ti.setOVERLAY_OPACITY(0);
                        ti.setRATIO(main_rel.getWidth() + ":" + main_rel.getHeight());
                        dh.insertTemplateRow(ti);
                        
                        return "saved_to_db"; // dummy non-null string
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String path) {
            if (path != null) {
                if (isDownload) {
                    com.festival.flyer.postermaker.MyApplication.showInterstitialAd(MailER_AutoPosterEditActivity.this, () -> {
                        Intent intent = new Intent(MailER_AutoPosterEditActivity.this, MailER_ShareActivity.class);
                        intent.putExtra("imagePath", path);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    saveDialog();
                }
            } else {
                Toast.makeText(MailER_AutoPosterEditActivity.this, "Failed to save poster", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (ll_mini_editor != null && ll_mini_editor.getVisibility() == View.VISIBLE) {
            ll_mini_editor.setVisibility(View.GONE);
            if (activeView != null) {
                          saveState(undoStack); redoStack.clear();
                activeView.setBackground(null);
                activeView.setPadding(0,0,0,0);
                activeView = null;
            }
        } else {
            leaveDialog();
        }
    }

    private void saveDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this, 16974126);
        dialog.requestWindowFeature(1);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.spawner_save_dialog);
        TextView btn_ok = dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        RelativeLayout native_banner_ad_container = dialog.findViewById(R.id.native_banner_ad_containers);
        com.facebook.shimmer.ShimmerFrameLayout shimmer_view_container = dialog.findViewById(R.id.shimmer_view_container);
        if (new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this).getAdsStatus("ExitDialog_NativeAD") == 1) {
            com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil.loadDialogNativeAd(native_banner_ad_container, this, shimmer_view_container);
        } else {
            if (native_banner_ad_container != null) native_banner_ad_container.setVisibility(android.view.View.GONE);
            if (shimmer_view_container != null) shimmer_view_container.setVisibility(android.view.View.GONE);
        }
    }

    private void leaveDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this, 16974126);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.spawner_leave_dialog);
        dialog.setCancelable(true);

        RelativeLayout native_banner_ad_container = dialog.findViewById(R.id.native_banner_ad_containers);
        com.facebook.shimmer.ShimmerFrameLayout shimmer_view_container = dialog.findViewById(R.id.shimmer_view_container);
        if (new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(this).getAdsStatus("ExitDialog_NativeAD") == 1) {
            com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil.loadDialogNativeAd(native_banner_ad_container, this, shimmer_view_container);
        } else {
            if (native_banner_ad_container != null) native_banner_ad_container.setVisibility(android.view.View.GONE);
            if (shimmer_view_container != null) shimmer_view_container.setVisibility(android.view.View.GONE);
        }

        TextView btn_yes = dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        TextView btn_no = dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMaterialDialog();
    }
}
