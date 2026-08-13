package com.festival.flyer.postermaker.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.festival.flyer.postermaker.MyApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MailER_SignatureFileUtils {

    private final Context context;
    private final List<String> FILE_EXTN = new ArrayList<>();

    public MailER_SignatureFileUtils(Context context) {
        this.context = context;
    }

    public static Uri getContentUriForImageFromMediaStore(Context context, String path) {
        ContentResolver resolver = context.getContentResolver();
        //Uri photoUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // to handle hidden images
        Uri photoUri = MediaStore.Files.getContentUri("external");
        Cursor cursor = resolver.query(photoUri, new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?", new String[]{path}, null);
        if (cursor == null) {
            return Uri.parse(path);
        }
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            cursor.close();
            // insert system media db
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, path);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
            return context.getContentResolver().insert(photoUri, values);
        } else {
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            Uri uri = ContentUris.withAppendedId(photoUri, id);
            cursor.close();
            return uri;
        }
    }

    public ArrayList<String> getFilePaths() {

        FILE_EXTN.add("png");
        FILE_EXTN.add("PNG");
        FILE_EXTN.add("jpg");
        FILE_EXTN.add("JPG");
        FILE_EXTN.add("jpeg");
        FILE_EXTN.add("JPEG");

        ArrayList<String> filePaths = new ArrayList<>();

        //File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //File directory = new File(file, context.getResources().getString(R.string.company_name) + "/" + context.getResources().getString(R.string.app_name));
        File directory = new File(MyApplication.getInstance().GetMainPath());

        if (directory.isDirectory()) {
            File[] listFiles = directory.listFiles();

            Arrays.sort(listFiles, (file1, file2) -> {
                long k = file1.lastModified() - file2.lastModified();
                if (k > 0) {
                    return 1;
                } else if (k == 0) {
                    return 0;
                } else {
                    return -1;
                }
            });

            if (listFiles.length > 0) {

                for (File listFile : listFiles) {

                    String filePath = listFile.getAbsolutePath();

                    if (IsSupportedFile(filePath)) {
                        filePaths.add(filePath);
                    }
                }
            } else {
                Log.e("NullDirectory", "NullDirectory");
            }
        }

        return filePaths;
    }

    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1)
        );

        return FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault()));

    }

}