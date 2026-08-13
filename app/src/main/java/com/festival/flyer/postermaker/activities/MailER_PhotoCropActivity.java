package com.festival.flyer.postermaker.activities;

import static com.festival.flyer.postermaker.f.MailER_ResizableStickerView.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.cropper.MailER_CropImageView;
import com.festival.flyer.postermaker.utils.MailER_BitmapUtils;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

public class MailER_PhotoCropActivity extends AppCompatActivity implements MailER_CropImageView.OnCropImageCompleteListener {

    private MailER_CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.spawner_activity_photo_crop);

        cropImageView = findViewById(R.id.cropImageView);

        String imageLink = getIntent().getStringExtra("selectedImage");

        RelativeLayout freeRatio = findViewById(R.id.freeRatio);
        RelativeLayout ratio1 = findViewById(R.id.ratio1);
        RelativeLayout ratio2 = findViewById(R.id.ratio2);
        RelativeLayout ratio3 = findViewById(R.id.ratio3);
        RelativeLayout ratio4 = findViewById(R.id.ratio4);
        RelativeLayout ratio5 = findViewById(R.id.ratio5);

        cropImageView.setAutoZoomEnabled(false);
        cropImageView.setOnCropImageCompleteListener(this);

        findViewById(R.id.btn_backPress).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

//        new loadBitmapTask(imageLink).execute();
        cropImageView.setImageUriAsync(MailER_PosterEditActivity.imguri);
        freeRatio.setOnClickListener(v -> cropImageView.clearAspectRatio());
        ratio1.setOnClickListener(v -> cropImageView.setAspectRatio(1, 1));
        ratio2.setOnClickListener(v -> cropImageView.setAspectRatio(16, 9));
        ratio3.setOnClickListener(v -> cropImageView.setAspectRatio(9, 16));
        ratio4.setOnClickListener(v -> cropImageView.setAspectRatio(4, 3));
        ratio5.setOnClickListener(v -> cropImageView.setAspectRatio(3, 4));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        findViewById(R.id.done_ly).setOnClickListener(v -> cropImageView.getCroppedImageAsync(metrics.widthPixels, metrics.heightPixels));

    }



    @Override
    public void onCropImageComplete(MailER_CropImageView view, MailER_CropImageView.CropResult result) {
        new saveBitmapTask(result.getBitmap()).execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class loadBitmapTask extends AsyncTask<String, String, Bitmap> {

        private final String path;
        private MaterialDialog materialDialog;
        private int width, height;

        public loadBitmapTask(String path) {
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PhotoCropActivity.this);
            Objects.requireNonNull(materialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            materialDialog.setCancelable(false);
            materialDialog.show();
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;
            height = metrics.heightPixels;

        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {

                bitmap = StringToBitMap(path);

//                bitmap = BitmapUtils.saveBitmap(PhotoCropActivity.this, BitmapUtils.viewToBitmap(PhotoCropActivity.this, Uri.parse(path)));


//                bitmap = Glide.with(PhotoCropActivity.this)
//                        .load(Uri.parse(path))
//                        .asBitmap()
//                        .into(width, height)
//                        .get();
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: " + e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (materialDialog != null && materialDialog.isShowing())
                materialDialog.dismiss();
            cropImageView.setImageBitmap(bitmap);
        }
    }

    public Bitmap StringToBitMap(String image) {
        try {
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);

            InputStream inputStream = new ByteArrayInputStream(encodeByte);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "StringToBitMap: " + e.getMessage());
            e.getMessage();
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class saveBitmapTask extends AsyncTask<String, String, String> {

        private final Bitmap bitmap;
        private MaterialDialog materialDialog;

        public saveBitmapTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(MailER_PhotoCropActivity.this);
            Objects.requireNonNull(materialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            materialDialog.setCancelable(false);
            materialDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            return MailER_BitmapUtils.saveBitmapObject1(MailER_PhotoCropActivity.this, "temp", bitmap);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                if (materialDialog != null && materialDialog.isShowing())
                    materialDialog.dismiss();
            } catch (Exception e) {
                Log.e("#Dailog_catch", e.getMessage());
            }
            Intent intent = new Intent();
            intent.putExtra("sticker", result);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}