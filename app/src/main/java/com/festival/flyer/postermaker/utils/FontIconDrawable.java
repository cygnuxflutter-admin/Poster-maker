package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.festival.flyer.postermaker.R;

public class FontIconDrawable extends Drawable {
    private final Paint paint;
    private final String text;
    private final int size;
    private ColorStateList tintList;

    public FontIconDrawable(Context context, String text, int sizeDp) {
        this.text = text;
        this.size = (int) (sizeDp * context.getResources().getDisplayMetrics().density);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(0xFF333333); 
        paint.setTextSize(this.size);

        Typeface typeface = ResourcesCompat.getFont(context, R.font.fa_solid_900);
        paint.setTypeface(typeface);

        setBounds(0, 0, this.size, this.size);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        float x = bounds.width() / 2f;
        float y = bounds.height() / 2f - ((paint.descent() + paint.ascent()) / 2f);
        canvas.drawText(text, x, y, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public void setTintList(ColorStateList tint) {
        super.setTintList(tint);
        this.tintList = tint;
        updateTintColor(getState());
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean changed = super.onStateChange(state);
        if (tintList != null) {
            updateTintColor(state);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean isStateful() {
        return true; 
    }

    private void updateTintColor(int[] state) {
        if (tintList != null) {
            int color = tintList.getColorForState(state, tintList.getDefaultColor());
            paint.setColor(color);
            invalidateSelf();
        }
    }
}
