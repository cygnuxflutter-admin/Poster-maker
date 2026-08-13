package com.festival.flyer.postermaker.threadTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;

import com.festival.flyer.postermaker.utils.MailER_FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MailER_DlTemplateAsync extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private final Activity activity;
    private final ArrayList<String> stringArrayList;
    private final OnDownloadTemplateListener onDownloadTemplateListener;

    public interface OnDownloadTemplateListener {
        void onDownloadComplete();
        void onError();
    }

    public MailER_DlTemplateAsync(Activity activity, ArrayList<String> stringArrayList, OnDownloadTemplateListener onDownloadTemplateListener) {
        this.activity = activity;
        this.stringArrayList = stringArrayList;
        this.onDownloadTemplateListener = onDownloadTemplateListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            for (int i = 0; i < stringArrayList.size(); i++) {
                File dir = MailER_FileUtils.getRootPath(activity);
                dir.mkdirs();
                File file = new File(dir, MailER_FileUtils.getFileName(stringArrayList.get(i)));

                if(!file.exists()) {
                    String uri = stringArrayList.get(i);
                    URL url = new URL(uri);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();

//                    file.createNewFile();
//                    String PATH = file.getPath();
//                    Log.e("PATH", "==" + PATH);
//                    File folder = new File(PATH);
//                    if (!folder.exists()) {
//                        folder.mkdir();
//                    }
                    InputStream input;
                    input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(file);
                    byte[] data = new byte[1024];
                    while (true) {
                        int count = input.read(data);
                        if (count == -1) {
                            break;
                        }
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }///storage/emulated/0/DCIM/BM Infotech/Festival Adbanao/.poster_data/.temp/.framedata/5404_18-08-2018__1534570928.png: open failed: EISDIR (Is a directory)
        return "success";
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result.equals("success")) {
            onDownloadTemplateListener.onDownloadComplete();
        } else {
            onDownloadTemplateListener.onError();
        }
    }

}
