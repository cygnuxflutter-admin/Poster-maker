package com.festival.flyer.postermaker.view;

import android.content.Context;
import android.util.AttributeSet;

public class MailER_CustomSquareImageView extends androidx.appcompat.widget.AppCompatImageView {
    public MailER_CustomSquareImageView(Context context) {
        super(context);
    }

    public MailER_CustomSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }
}
