package com.festival.flyer.postermaker.threadTask;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MailER_SaveBitmapTask extends AsyncTask<Void, Void, String> {

    private final Bitmap bitmap;
    private final String srcPath;
    private final OnColorBitmapListener onColorBitmapListener;

    public interface OnColorBitmapListener {
        void onDownloadComplete();
        void onError();
    }


    public MailER_SaveBitmapTask(Bitmap bitmap, String srcPath, OnColorBitmapListener onColorBitmapListener) {
        this.bitmap = bitmap;
        this.srcPath = srcPath;
        this.onColorBitmapListener = onColorBitmapListener;
    }

    @Override
    protected String doInBackground(Void... params) {
        FileOutputStream fos = null;
        try {

            File file = new File(srcPath);
            fos = new FileOutputStream(file);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                bitmap.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "success";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result.equals("success")) {
            onColorBitmapListener.onDownloadComplete();
        } else {
            onColorBitmapListener.onError();
        }
    }
}
