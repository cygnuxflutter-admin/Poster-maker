package com.festival.flyer.postermaker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MailER_BitmapUtils {

    public static Bitmap getBitmapFromString(String imagePath, Bitmap.CompressFormat compressFormat, int width, int height) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = width / height;

        if (actualHeight > height || actualWidth > width) {
            if (imgRatio < maxRatio) {
                imgRatio = height / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) height;
            } else if (imgRatio > maxRatio) {
                imgRatio = width / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) width;
            } else {
                actualHeight = (int) height;
                actualWidth = (int) width;
            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            scaledBitmap = transform(scaledBitmap, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(compressFormat, 85, out);

        byte[] byteArray = out.toByteArray();

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    public static Bitmap transform(Bitmap source, float bit_width, float bit_height) {
        float sWidth = source.getWidth();
        float sHeight = source.getHeight();

        float xScale;
        float yScale;
        if (sWidth < sHeight) {
            yScale = bit_height / sHeight;
            xScale = yScale;
        } else {
            xScale = bit_width / sWidth;
            yScale = xScale;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);
        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, (int) sWidth, (int) sHeight, matrix, true);

        scaledBitmap.getWidth();
        scaledBitmap.getHeight();
        if (scaledBitmap != source) {
            source.recycle();
        }

        return scaledBitmap;
    }


    public static Bitmap resampleImage(String path, int maxDim) {
        try {
            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bfo);
            BitmapFactory.Options optsDownSample = new BitmapFactory.Options();
            optsDownSample.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
            Bitmap bmpt = BitmapFactory.decodeFile(path, optsDownSample);
            if (bmpt == null) {
                return null;
            }
            Matrix matrix = new Matrix();
            if (bmpt.getWidth() > maxDim || bmpt.getHeight() > maxDim) {
                BitmapFactory.Options optsScale = getResampling(bmpt.getWidth(), bmpt.getHeight(), maxDim);
                matrix.postScale(((float) optsScale.outWidth) / ((float) bmpt.getWidth()), ((float) optsScale.outHeight) / ((float) bmpt.getHeight()));
            }
            return Bitmap.createBitmap(bmpt, 0, 0, bmpt.getWidth(), bmpt.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getExifRotation(String imgPath) {
        try {
            String rotationAmount = new ExifInterface(imgPath).getAttribute("Orientation");
            if (TextUtils.isEmpty(rotationAmount)) {
                return 0;
            }
            switch (Integer.parseInt(rotationAmount)) {
                case 3:
                    return 180;
                case 6:
                    return 90;
                case 8:
                    return 270;
                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }


    public static BitmapFactory.Options getResampling(int cx, int cy, int max) {
        float scaleVal;
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        if (cx > cy) {
            scaleVal = ((float) max) / ((float) cx);
        } else if (cy > cx) {
            scaleVal = ((float) max) / ((float) cy);
        } else {
            scaleVal = ((float) max) / ((float) cx);
        }
        bfo.outWidth = (int) ((((float) cx) * scaleVal) + 0.5f);
        bfo.outHeight = (int) ((((float) cy) * scaleVal) + 0.5f);
        return bfo;
    }


    public static int getClosestResampleSize(int cx, int cy, int maxDim) {
        int max = Math.max(cx, cy);
        int resample = 1;
        while (resample < Integer.MAX_VALUE) {
            if (resample * maxDim > max) {
                resample--;
                break;
            }
            resample++;
        }
        return resample > 0 ? resample : 1;
    }

    public static Bitmap bitmapRatio(String ratio, Bitmap btm, float bit_width, float bit_height) {
        Bitmap bit = null;
        if (ratio.equals("")) {
            bit = btm;
        }
        bit = resizeBitmap(bit, (int) bit_width, (int) bit_height);

        return bit;
    }

    public static Bitmap resizeBitmap(Bitmap bit, int width, int height) {
        if (bit == null) {
            return null;
        }
        float wr = (float) width;
        float hr = (float) height;
        float wd = (float) bit.getWidth();
        float he = (float) bit.getHeight();
        float rat1 = wd / he;
        float rat2 = he / wd;
        if (wd > wr) {
            if (wr * rat2 > hr) {
                he = hr;
                wd = he * rat1;
            } else {
                wd = wr;
                he = wd * rat2;
            }
        } else if (he > hr) {
            he = hr;
            wd = he * rat1;
            if (wd > wr) {
                wd = wr;
                he = wd * rat2;
            }
        } else if (rat1 > 0.75f) {
            if (wr * rat2 > hr) {
                he = hr;
                wd = he * rat1;
            } else {
                wd = wr;
                he = wd * rat2;
            }
        } else if (rat2 > 1.5f) {
            he = hr;
            wd = he * rat1;
            if (wd > wr) {
                wd = wr;
                he = wd * rat2;
            }
        } else if (wr * rat2 > hr) {
            he = hr;
            wd = he * rat1;
        } else {
            wd = wr;
            he = wd * rat2;
        }
        return Bitmap.createScaledBitmap(bit, (int) wd, (int) he, false);
    }


    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    public static String saveBitmap(File file, Bitmap bmp) {
        if (bmp == null) {
            return "";
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            bmp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }

    public static Bitmap getCompressedBitmap(Context context, Uri uri, int width, int height) throws IOException, IllegalArgumentException {
        Bitmap scaledBitmap = null;
        ParcelFileDescriptor openFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = openFileDescriptor.getFileDescriptor();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        Log.d("aaaaaa", actualWidth + " " + actualHeight);
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = (float) height / (float) width;

        if (actualHeight > (float) width || actualWidth > (float) height) {
            if (imgRatio < maxRatio) {
                imgRatio = (float) width / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) (float) width;
            } else if (imgRatio > maxRatio) {
                imgRatio = (float) height / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) (float) height;
            } else {
                actualHeight = (int) (float) width;
                actualWidth = (int) (float) height;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            // bmp = BitmapFactory.decodeFile(imagePath, options);
            bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2.0f, middleY - bmp.getHeight() / 2.0f, new Paint(Paint.FILTER_BITMAP_FLAG));

        Matrix matrix = new Matrix();
        try {
            ExifInterface exif = new ExifInterface(getRealPathFromURI(uri, context));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        byte[] byteArray = out.toByteArray();

        Bitmap updatedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        openFileDescriptor.close();

        return updatedBitmap;
    }

    public static String getRealPathFromURI(Uri uri, Context context) {
        String str;
        try {
            Cursor query = context.getContentResolver().query(uri, null, null, null, null);
            if (query == null) {
                str = uri.getPath();
            } else {
                query.moveToFirst();
                String string = query.getString(query.getColumnIndex("_data"));
                query.close();
                str = string;
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return uri.toString();
        }
    }

    public static Bitmap getTiledBitmap(Context ctx, int resId, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        Paint paint = new Paint();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        paint.setShader(new BitmapShader(BitmapFactory.decodeResource(ctx.getResources(), resId, options), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(b).drawRect(rect, paint);
        return b;
    }

    public static String saveBitmapObject(Activity activity, String path, Bitmap bitmap) {
        File myDir = MailER_FileUtils.getSaveFileLocation(activity, "MyDesigns/" + path);
        myDir.mkdirs();
        File file1 = new File(myDir, "thumb-" + System.currentTimeMillis() + ".png");
        if (file1.exists()) {
            file1.delete();
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file1));
            return file1.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, activity.getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static String saveBitmapObject1(Activity activity, String path, Bitmap bitmap) {
        String temp_path = "";
        File myDir = MailER_FileUtils.getSaveFileLocation(activity, "category1/" + path);
        myDir.mkdirs();
        File file1 = new File(myDir, "raw1-" + System.currentTimeMillis() + ".png");
        temp_path = file1.getAbsolutePath();
        if (file1.exists()) {
            file1.delete();
        }
        try {
            FileOutputStream ostream = new FileOutputStream(file1);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.close();
            return temp_path;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Bitmap viewToBitmap(Activity activity, View view) {
        Bitmap b = null;
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(b));
            b = resizeBitmap(b, metrics.widthPixels, metrics.heightPixels);
            return b;
        } finally {
            view.destroyDrawingCache();
        }
    }

    public static String saveBitmap(Activity activity, Bitmap bitmap) {
        String filename = null;

        //File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/" + activity.getResources().getString(R.string.company_name) + "/" + activity.getResources().getString(R.string.app_name));
        File pictureFileDir = new File(MyApplication.getInstance().GetMainPath());
        if (pictureFileDir.exists() || pictureFileDir.mkdirs()) {
            String photoFile = "PosterMaker_" + System.currentTimeMillis();

            photoFile = photoFile + ".png";

            filename = pictureFileDir.getPath() + File.separator + photoFile;
            File pictureFile = new File(filename);
            try {
                if (!pictureFile.exists()) {
                    pictureFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);

                DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                Canvas canvas = new Canvas(newBitmap);
                canvas.drawColor(-1);
                canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                newBitmap.recycle();

                fileOutputStream.flush();
                fileOutputStream.close();
                activity.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(pictureFile)));
            } catch (Exception e) {
                filename = null;
                e.printStackTrace();
            }

        }
        return filename;
    }

    public static void savePremiumPath(Activity activity, Bitmap bitmap) {
        ContextWrapper cw = new ContextWrapper(activity);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File file = new File(directory, "premium" + ".png");
        Log.d("path", file.toString());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
