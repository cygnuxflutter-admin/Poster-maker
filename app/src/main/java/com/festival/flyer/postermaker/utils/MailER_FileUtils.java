package com.festival.flyer.postermaker.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.festival.flyer.postermaker.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MailER_FileUtils {

    //activity.getResources().getString(R.string.company_name) + "/" + activity.getResources().getString(R.string.app_name) +

    public static String save_path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Poster Maker");

    public static File getRootPath(Activity activity) {
        File file0 = activity.getExternalFilesDir(activity.getString(R.string.app_name));
        return new File(file0, ".poster_data" + "/.temp/.framedata");
    }

    public static String getFile(Context activity, String path) {
        File file = activity.getExternalFilesDir(activity.getString(R.string.app_name));
        File dir = new File(file, ".poster_data" + "/.temp/.framedata/" + getFileName(path));
        return dir.getPath();
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static File GetFileDir(Context context) {
        return "mounted".equals(Environment.getExternalStorageState()) ? context.getExternalFilesDir(null) : context.getFilesDir();
    }

    public static File GetFontDir(Context context) {
        File GetFileDir = GetFileDir(context);
        String sb = GetFileDir.getPath() + File.separator + "font";
        File file = new File(sb);
        return (file.exists() || file.mkdirs()) ? file : GetFileDir;
    }

    public static boolean isAssetExists(String pathInAssetsDir, Context context) {
        AssetManager am = context.getAssets();
        try {
            List<String> mapList = Arrays.asList(am.list("font"));
            return mapList.contains(pathInAssetsDir);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> listAssetFiles(Context context, String rootPath) {
        ArrayList<String> files = new ArrayList<>();
        try {
            String[] paths = context.getAssets().list(rootPath);
            if (paths != null) {
                if (paths.length > 0) {
                    // This is a folder
                    for (String file : paths) {
                        String path = rootPath + "/" + file;
                        if (new File(path).isDirectory())
                            files.addAll(listAssetFiles(context, path));
                        else files.add(path);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public static Uri getUriFromPath(Context context, String path) {
        ContentResolver resolver = context.getContentResolver();
        //Uri photoUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // to handle hidden images
        Uri photoUri = MediaStore.Files.getContentUri("external");
        Cursor cursor = resolver.query(photoUri,
                new String[]{BaseColumns._ID},
                MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, null);
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

    public static File getSaveFileLocation(Activity activity, String folderName) {
        //File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = activity.getExternalFilesDir(activity.getString(R.string.app_name));
        return new File(file, ".Poster_Maker_Stickers/" + folderName);
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static boolean checkFileExistOrNot(ArrayList<File> fileArrayList) {
        for (int i = 0; i < fileArrayList.size(); i++) {
            if (!fileArrayList.get(i).exists()) {
                return false;
            }
        }
        return true;
    }

    public static String getPremiumPath(Activity activity) {
        ContextWrapper cw = new ContextWrapper(activity);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        return new File(directory, "premium" + ".png").getPath();
    }

    public static boolean copyFile(File sourceLocation, File targetLocation) {
        InputStream in;
        OutputStream out;
        try {
            in = new FileInputStream(sourceLocation);
            out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while (true) {
                try {
                    if (!((len = in.read(buf)) > 0)) break;
                    out.write(buf, 0, len);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
