package com.festival.flyer.postermaker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class MailER_CustomSquareFrameLayout extends FrameLayout {
    public MailER_CustomSquareFrameLayout(Context context) {
        super(context);
    }

    public MailER_CustomSquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, w, oldw, oldh);
    }
}
