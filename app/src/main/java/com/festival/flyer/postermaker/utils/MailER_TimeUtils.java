package com.festival.flyer.postermaker.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MailER_TimeUtils {
    public static String getTimeStamp() {
        Date today = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-MM hh.mm.ss");
        return format.format(today);
    }
}
