package com.festival.flyer.postermaker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

public class MailER_SizeUtils {

    public static int getNewWidth(View view, float x_pos, float width) {
        return (int) ((((float) view.getWidth()) * (width - x_pos)) / 100.0f);
    }

    public static int getNewHeight(View view, float y_pos, float height) {
        return (int) ((((float) view.getHeight()) * (height - y_pos)) / 100.0f);
    }

    public static float getXPosition(View view, float x_pos) {
        return (((float) view.getWidth()) * x_pos) / 100.0f;
    }

    public static float getYPosition(View view, float y_pos) {
        return (((float) view.getHeight()) * y_pos) / 100.0f;
    }

    public static int getNewHeightText(View view, float y_pos, float main_height) {
        float height = (((float) view.getHeight()) * (main_height - y_pos)) / 100.0f;
        return (int) (((float) ((int) height)) + (height / 2.0f));
    }

    public static int dpToPx(Context c, float dp) {
        c.getResources();
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

    public static int getAspectRatio(int width, int height) {
        if (height == 0) {
            return width;
        }
        return getAspectRatio(height, width % height);
    }
}
