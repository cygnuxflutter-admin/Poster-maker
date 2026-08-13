package com.festival.flyer.postermaker.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.festival.flyer.postermaker.R;

public class MailER_ShareUtils {

    public static void onShare(Context context) {

        String shareBody = "I'm using amazing Poster maker App, I just created a new poster within 2 min and it's completely free. I recommend you to try this app. \n\n" +
                "https://play.google.com/store/apps/details?id=" + context.getApplicationContext().getPackageName();

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public static void rateUs(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getApplicationContext().getPackageName())));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getApplicationContext().getPackageName())));
        }
    }

    public static void onInstagram(Context context) {

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/postersmakerapp?igshid=YmMyMTA2M2Y=" + context.getApplicationContext().getPackageName())));
        }

    public  static void onPrivacyPolicy(Context context){


            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://cygnux.in/application-privacy-policy/festival-adbanao.html" + context.getApplicationContext().getPackageName())));

    }
}
