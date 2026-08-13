package com.festival.flyer.postermaker.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_PosterEditActivity;
import com.festival.flyer.postermaker.adapter.MailER_FontAdapter;
import com.festival.flyer.postermaker.adapter.MailER_TxtBgAdapter;
import com.festival.flyer.postermaker.color_palette.MailER_LineColorPicker;
import com.festival.flyer.postermaker.color_palette.MailER_OnColorChangedListener;
import com.festival.flyer.postermaker.components.MailER_AutofitTextRel;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;
import com.festival.flyer.postermaker.utils.MailER_RepeatListener;
import com.festival.flyer.postermaker.view.MailER_GridLine;
import com.festival.flyer.postermaker.view.MailER_HorizontalProgressWheelView;

import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

import static com.festival.flyer.postermaker.activities.MailER_PosterEditActivity.txt_stkr_rel;

public class MailER_TextController {

    private final LinearLayout ly_txt_controls, ly_txt_rotation, ly_fonts, ly_txt_color, ly_txt_shadow, ly_txt_bg, ly_txt_style;
    private final RecyclerView fonts_rv, txtBg_recycler;
    private final RelativeLayout txt_btn_up, txt_btnLeft, txt_btnRight, txt_btnDown, lay_dupliText, lay_edit, lay_txt_delete;
    private final SeekBar txt_alpha_seekbar, seekBar_txt_shadow, txt_bg_seekbar;
    private final ImageView btn_pickColor, btn_txt_shadow_picker, btn_shadow_minus, btn_shadow_plus, btn_bg_none, btn_pick_bg_color;
    private final ImageView boldIV, italicIV, bold_italic_iv, normalIV;
    private final TextView text_view_rotate;
    private final MailER_LineColorPicker txt_color_LineColorPicker, color_picker_txtShadow, colorPicker_Bg;
    private final Activity activity;
    private final float[] mMatrixValues = new float[9];
    protected Matrix mCurrentImageMatrix = new Matrix();
    private MailER_AutofitTextRel autofitTextRel;
    private MailER_GridLine gridLine;

    public MailER_TextController(Activity activity, MailER_GridLine gridLine) {
        this.activity = activity;
        this.gridLine = gridLine;
        RelativeLayout btn_controls = activity.findViewById(R.id.btn_controls);
        RelativeLayout btn_rotation = activity.findViewById(R.id.btn_rotation);
        RelativeLayout btn_fonts = activity.findViewById(R.id.btn_fonts);
        RelativeLayout btn_colors = activity.findViewById(R.id.btn_colors);
        RelativeLayout btn_shadow = activity.findViewById(R.id.btn_shadow);
        RelativeLayout btn_backgnd = activity.findViewById(R.id.btn_backgnd);
        RelativeLayout btn_style = activity.findViewById(R.id.btn_style);

        ly_txt_controls = activity.findViewById(R.id.ly_txt_controls);
        ly_txt_rotation = activity.findViewById(R.id.ly_txt_rotation);
        ly_fonts = activity.findViewById(R.id.ly_fonts);
        ly_txt_color = activity.findViewById(R.id.ly_txt_color);
        ly_txt_shadow = activity.findViewById(R.id.ly_txt_shadow);
        ly_txt_bg = activity.findViewById(R.id.ly_txt_bg);
        ly_txt_style = activity.findViewById(R.id.ly_txt_style);

        txt_btn_up = activity.findViewById(R.id.txt_btn_up);
        txt_btnLeft = activity.findViewById(R.id.txt_btnLeft);
        txt_btnRight = activity.findViewById(R.id.txt_btnRight);
        txt_btnDown = activity.findViewById(R.id.txt_btnDown);
        lay_dupliText = activity.findViewById(R.id.lay_dupliText);
        lay_edit = activity.findViewById(R.id.lay_edit);
        lay_txt_delete = activity.findViewById(R.id.lay_txt_delete);

        fonts_rv = activity.findViewById(R.id.fonts_rv);
        txtBg_recycler = activity.findViewById(R.id.txtBg_recycler);

        txt_alpha_seekbar = activity.findViewById(R.id.txt_alpha_seekbar);
        seekBar_txt_shadow = activity.findViewById(R.id.seekBar_txt_shadow);
        txt_bg_seekbar = activity.findViewById(R.id.txt_bg_seekbar);

        btn_pickColor = activity.findViewById(R.id.btn_pickColor);
        btn_txt_shadow_picker = activity.findViewById(R.id.btn_txt_shadow_picker);
        btn_shadow_minus = activity.findViewById(R.id.btn_shadow_minus);
        btn_shadow_plus = activity.findViewById(R.id.btn_shadow_plus);
        btn_bg_none = activity.findViewById(R.id.btn_bg_none);
        btn_pick_bg_color = activity.findViewById(R.id.btn_pick_bg_color);
        boldIV = activity.findViewById(R.id.boldIV);
        italicIV = activity.findViewById(R.id.italicIV);
        bold_italic_iv = activity.findViewById(R.id.bold_italic_iv);
        normalIV = activity.findViewById(R.id.normalIV);

        txt_color_LineColorPicker = activity.findViewById(R.id.txt_color_LineColorPicker);
        color_picker_txtShadow = activity.findViewById(R.id.color_picker_txtShadow);
        colorPicker_Bg = activity.findViewById(R.id.colorPicker_Bg);

        text_view_rotate = activity.findViewById(R.id.text_view_rotate);
        MailER_HorizontalProgressWheelView rotate_scroll_wheel = activity.findViewById(R.id.rotate_scroll_wheel);
        FrameLayout wrapper_reset_rotate = activity.findViewById(R.id.wrapper_reset_rotate);
        FrameLayout wrapper_rotate_by_angle = activity.findViewById(R.id.wrapper_rotate_by_angle);

        btn_controls.setOnClickListener(v -> showLayout(ly_txt_controls));
        btn_rotation.setOnClickListener(v -> showLayout(ly_txt_rotation));
        btn_fonts.setOnClickListener(v -> showLayout(ly_fonts));
        btn_colors.setOnClickListener(v -> showLayout(ly_txt_color));
        btn_shadow.setOnClickListener(v -> showLayout(ly_txt_shadow));
        btn_backgnd.setOnClickListener(v -> showLayout(ly_txt_bg));
        btn_style.setOnClickListener(v -> showLayout(ly_txt_style));

        setControlLy();
        setFontAdapter();
        setColorLy();
        setShadowLy();
        setBgLy();
        setStyleLy();

        rotate_scroll_wheel.setScrollingListener(new MailER_HorizontalProgressWheelView.ScrollingListener() {
            @Override
            public void onScrollStart() {

            }

            @Override
            public void onScroll(float delta, float totalDistance) {
                float deltaAngle = delta / 42;
                postRotate(deltaAngle, false);
            }

            @Override
            public void onScrollEnd() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });

        wrapper_reset_rotate.setOnClickListener(v -> {
            if (autofitTextRel.getRotation() != 0.0f || autofitTextRel.getRotation() != -0.0f) {
                postRotate(0f, true);
            }
        });

        wrapper_rotate_by_angle.setOnClickListener(v -> postRotate(90f, true));
    }

    public void postRotate(float deltaAngle, boolean update) {
        if (deltaAngle != 0) {
            mCurrentImageMatrix.postRotate(deltaAngle);
            setRotation(getMatrixAngle(mCurrentImageMatrix), update);
        } else {
            mCurrentImageMatrix.reset();
            setRotation(0.0f, update);
        }
    }

    public float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) -(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X), getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI));
    }

    protected float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 9) int valueIndex) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[valueIndex];
    }

    public void setRotation(float rotation, boolean update) {
        autofitTextRel.setRotation(rotation);
        if (rotation == -0.0f) {
            text_view_rotate.setText(String.format(Locale.getDefault(), "%.1f°", 0.0f));
        } else {
            text_view_rotate.setText(String.format(Locale.getDefault(), "%.1f°", rotation));
        }

        if (update) {
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setControlLy() {

        txt_btn_up.setOnTouchListener(new MailER_RepeatListener(200, 100, gridLine, new MailER_RepeatListener.RepeatInterface() {
            @Override
            public void onClick(View view) {
                autofitTextRel.decY();
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "move", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        }));

        txt_btnLeft.setOnTouchListener(new MailER_RepeatListener(200, 100, gridLine, new MailER_RepeatListener.RepeatInterface() {
            @Override
            public void onClick(View view) {
                autofitTextRel.decX();
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "move", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        }));

        txt_btnRight.setOnTouchListener(new MailER_RepeatListener(200, 100, gridLine, new MailER_RepeatListener.RepeatInterface() {
            @Override
            public void onClick(View view) {
                autofitTextRel.incrX();
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "move", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        }));

        txt_btnDown.setOnTouchListener(new MailER_RepeatListener(200, 100, gridLine, new MailER_RepeatListener.RepeatInterface() {
            @Override
            public void onClick(View view) {
                autofitTextRel.incrY();
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "move", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        }));

        lay_dupliText.setOnClickListener(v -> {
            if (activity instanceof MailER_PosterEditActivity) {
                MailER_AutofitTextRel autofitTextRel = new MailER_AutofitTextRel(activity);
                ((MailER_PosterEditActivity) activity).addNewAutofitTextRel(autofitTextRel, this.autofitTextRel.getTextInfo());
            }
        });

        lay_edit.setOnClickListener(v -> {
            if (activity instanceof MailER_PosterEditActivity) {
                ((MailER_PosterEditActivity) activity).showTextDialog(autofitTextRel.getText(), autofitTextRel);
            }
        });

        lay_txt_delete.setOnClickListener(v -> {
            if (activity instanceof MailER_PosterEditActivity && autofitTextRel != null) {
                autofitTextRel.deleteText();
            }
        });
    }

    private void setFontAdapter() {
        fonts_rv.setLayoutManager(new GridLayoutManager(activity, 1));
        fonts_rv.setItemAnimator(new DefaultItemAnimator());
        MailER_FontAdapter fontAdapter = new MailER_FontAdapter(activity, activity.getResources().getStringArray(R.array.fonts_array), (fontName) -> {
            autofitTextRel.setTextFont(fontName);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
        fonts_rv.setAdapter(fontAdapter);
    }

    private void setColorLy() {
        txt_alpha_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (autofitTextRel != null)
                    autofitTextRel.setTextAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });

        btn_pickColor.setOnClickListener(v -> new AmbilWarnaDialog(activity, autofitTextRel.getTextColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            public void onOk(AmbilWarnaDialog dialog, int color) {
                autofitTextRel.setTextColor(color);
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }

            public void onCancel(AmbilWarnaDialog dialog) {
            }
        }).show());

        txt_color_LineColorPicker.setColors(activity.getResources().getIntArray(R.array.palette));
        txt_color_LineColorPicker.setOnColorChangedListener(new MailER_OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                autofitTextRel.setTextColor(color);
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });

    }

    private void setShadowLy() {
        seekBar_txt_shadow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (autofitTextRel != null)
                    autofitTextRel.setTextShadowProg(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });
        btn_shadow_minus.setOnClickListener(v -> {
            if (seekBar_txt_shadow.getProgress() == 0) {
                return;
            }
            seekBar_txt_shadow.setProgress(seekBar_txt_shadow.getProgress() - 1);
        });
        btn_shadow_plus.setOnClickListener(v -> {
            if (seekBar_txt_shadow.getProgress() == 20) {
                return;
            }
            seekBar_txt_shadow.setProgress(seekBar_txt_shadow.getProgress() + 1);
        });
        btn_txt_shadow_picker.setOnClickListener(v -> new AmbilWarnaDialog(activity, autofitTextRel.getTextShadowColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            public void onOk(AmbilWarnaDialog dialog, int color) {
                autofitTextRel.setTextShadowColor(color);
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }

            public void onCancel(AmbilWarnaDialog dialog) {
            }
        }).show());
        color_picker_txtShadow.setColors(activity.getResources().getIntArray(R.array.palette));
        color_picker_txtShadow.setOnColorChangedListener(new MailER_OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                autofitTextRel.setTextShadowColor(color);
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });
    }

    private void setBgLy() {
        txt_bg_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (autofitTextRel != null)
                    autofitTextRel.setBgAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });
        btn_bg_none.setOnClickListener(v -> {
            txt_bg_seekbar.setProgress(0);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
        txtBg_recycler.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        txtBg_recycler.setHasFixedSize(true);
        MailER_TxtBgAdapter txtBgAdapter = new MailER_TxtBgAdapter(activity, MailER_FileUtils.listAssetFiles(activity, "background_img"), (path) -> {
            autofitTextRel.setBgDrawable(path);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
        txtBg_recycler.setAdapter(txtBgAdapter);
        btn_pick_bg_color.setOnClickListener(v -> new AmbilWarnaDialog(activity, autofitTextRel.getBgColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            public void onOk(AmbilWarnaDialog dialog, int color) {
                autofitTextRel.setBgColor(color);
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }

            public void onCancel(AmbilWarnaDialog dialog) {
            }
        }).show());
        colorPicker_Bg.setColors(activity.getResources().getIntArray(R.array.palette));
        colorPicker_Bg.setOnColorChangedListener(new MailER_OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                autofitTextRel.setBgColor(color);
            }

            @Override
            public void onFinish() {
                ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
            }
        });
    }

    private void setStyleLy() {
        boldIV.setOnClickListener(v -> {
            autofitTextRel.setTextStyle(Typeface.BOLD);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
        italicIV.setOnClickListener(v -> {
            autofitTextRel.setTextStyle(Typeface.ITALIC);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
        bold_italic_iv.setOnClickListener(v -> {
            autofitTextRel.setTextStyle(Typeface.BOLD_ITALIC);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
        normalIV.setOnClickListener(v -> {
            autofitTextRel.setTextStyle(Typeface.NORMAL);
            ((MailER_PosterEditActivity) activity).notifyUndoRedo("text", "nothing", txt_stkr_rel.indexOfChild(autofitTextRel));
        });
    }

    public void setAutofitTextRel(MailER_AutofitTextRel textRel, MailER_GridLine gridLine) {
        this.autofitTextRel = textRel;
        this.gridLine = gridLine;
        this.txt_alpha_seekbar.setProgress(textRel.getTextAlpha());
        this.seekBar_txt_shadow.setProgress(textRel.getTextShadowProg());
        this.txt_bg_seekbar.setProgress(textRel.getBgAlpha());
        mCurrentImageMatrix.postRotate(textRel.getRotation());
        text_view_rotate.setText(String.format(Locale.getDefault(), "%.1f°", textRel.getRotation()));
    }

    private void showLayout(View view) {
        ly_txt_controls.setVisibility(View.GONE);
        ly_txt_rotation.setVisibility(View.GONE);
        ly_fonts.setVisibility(View.GONE);
        ly_txt_color.setVisibility(View.GONE);
        ly_txt_shadow.setVisibility(View.GONE);
        ly_txt_bg.setVisibility(View.GONE);
        ly_txt_style.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }
}
