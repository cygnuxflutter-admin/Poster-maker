package com.festival.flyer.postermaker.makeityours.auto;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class MailER_AutoPersonalizationActivity extends AppCompatActivity {

    private FrameLayout canvas_frame;
    private ImageView iv_base_poster;
    private Button btn_save_poster, btn_share_poster, btn_edit_poster;

    private String logoUri, businessName, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_auto_personalization);

        canvas_frame = findViewById(R.id.canvas_frame);
        iv_base_poster = findViewById(R.id.iv_base_poster);
        btn_save_poster = findViewById(R.id.btn_save_poster);
        btn_edit_poster = findViewById(R.id.btn_edit_poster);
        btn_share_poster = findViewById(R.id.btn_share_poster);
        ImageView ic_back = findViewById(R.id.ic_back);
        if(ic_back != null) {
            ic_back.setOnClickListener(v -> onBackPressed());
        }

        loadProfileData();
        
        Button btnSwitch = findViewById(R.id.btn_switch_size);
        try {
            String posterData = getIntent().getStringExtra("poster_data");
            String allPostersStr = getIntent().getStringExtra("all_posters");
            if(posterData != null && allPostersStr != null) {
                JSONObject currentObj = new JSONObject(posterData);
                String currentId = currentObj.optString("design_id", "none");
                String currentRatio = currentObj.optString("ratio", "1:1");
                
                org.json.JSONArray allPosters = new org.json.JSONArray(allPostersStr);
                JSONObject otherObj = null;
                for(int i=0; i<allPosters.length(); i++){
                    JSONObject obj = allPosters.getJSONObject(i);
                    if(obj.optString("design_id", "missing").equals(currentId) && !obj.optString("ratio", "missing").equals(currentRatio)){
                        otherObj = obj;
                        break;
                    }
                }
                
                if(otherObj != null) {
                    btnSwitch.setVisibility(View.VISIBLE);
                    String targetRatio = otherObj.optString("ratio");
                    btnSwitch.setText("Switch to " + (targetRatio.equals("9:16") ? "Portrait (9:16)" : "Square (1:1)"));
                    
                    final JSONObject finalOtherObj = otherObj;
                    btnSwitch.setOnClickListener(v -> {
                        getIntent().putExtra("poster_data", finalOtherObj.toString());
                        recreate();
                    });
                } else {
                    btnSwitch.setVisibility(View.GONE);
                }
            }
        } catch(Exception e) { e.printStackTrace(); }

        // Measure width and pass to fetchApiData
        canvas_frame.post(() -> {
            int size = canvas_frame.getWidth();
            fetchApiData(size);
        });

        
        btn_edit_poster.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, MailER_AutoPosterEditActivity.class);
            intent.putExtra("poster_data", getIntent().getStringExtra("poster_data"));
            startActivity(intent);
        });
        
        btn_save_poster.setOnClickListener(v -> saveCanvasToImage());

    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE);
        logoUri = prefs.getString("logo_uri", "");
        businessName = prefs.getString("business_name", "My Business");
        phone = prefs.getString("phone", "1234567890");
        
        
    }

    private void fetchApiData(int canvasSize) {
        try {
            String posterData = getIntent().getStringExtra("poster_data");
            if (posterData == null || posterData.isEmpty()) return;
            
            JSONObject response = new JSONObject(posterData);
            String posterUrl = response.getString("poster_image");
            
            JSONObject personalization = response.getJSONObject("personalization");
            String theme = personalization.optString("theme", "dark");
            String ratio = personalization.optString("ratio", "1:1");
            
            // Set frame height based on ratio
            int height = canvasSize;
            if (ratio.equals("9:16") || ratio.equals("9: 16")) {
                height = (int) (canvasSize * 16f / 9f);
            } else if (ratio.equals("4:5") || ratio.equals("4: 5")) {
                height = (int) (canvasSize * 5f / 4f);
            }
            android.view.ViewGroup.LayoutParams frameParams = canvas_frame.getLayoutParams();
            frameParams.height = height;
            canvas_frame.setLayoutParams(frameParams);
            JSONObject slots = personalization.getJSONObject("slots");

            Glide.with(this).asBitmap().load(posterUrl).into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                @Override
                public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                    iv_base_poster.setImageBitmap(resource);
                    float nativeWidth = resource.getWidth();
                    float scale = (float) canvasSize / nativeWidth;
                    renderOverlays(slots, theme, scale, resource);
                }
                @Override
                public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderOverlays(JSONObject slots, String theme, float scale, android.graphics.Bitmap resource) {
        int textColor = theme.contains("dark") ? Color.WHITE : Color.BLACK;
        
        // Magic Color Extraction (Palette API)
        try {
            androidx.palette.graphics.Palette palette = androidx.palette.graphics.Palette.from(resource).generate();
            androidx.palette.graphics.Palette.Swatch swatch = palette.getVibrantSwatch();
            if (swatch == null) swatch = palette.getLightVibrantSwatch();
            if (swatch == null) swatch = palette.getDarkVibrantSwatch();
            
            if (swatch != null) {
                textColor = swatch.getRgb(); // Override basic B/W with magical theme color!
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (slots.has("logo")) {
                JSONObject logoObj = slots.getJSONObject("logo");
                addLogoOverlay(logoObj, scale);
            }
            if (slots.has("business_name")) {
                JSONObject bnObj = slots.getJSONObject("business_name");
                addTextOverlay(bnObj, businessName, textColor, scale, true);
            }
            if (slots.has("phone")) {
                JSONObject phoneObj = slots.getJSONObject("phone");
                addTextOverlay(phoneObj, phone, textColor, scale, false);
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLogoOverlay(JSONObject bounds, float scale) throws Exception {
        int x = (int) (bounds.getInt("x") * scale);
        int y = (int) (bounds.getInt("y") * scale);
        int w = (int) (bounds.getInt("w") * scale);
        int h = (int) (bounds.getInt("h") * scale);

        ImageView logoView = new ImageView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h);
        params.leftMargin = x;
        params.topMargin = y;
        logoView.setLayoutParams(params);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (!logoUri.isEmpty()) {
            Glide.with(this).load(logoUri).into(logoView);
        } else {
            logoView.setImageResource(android.R.drawable.ic_menu_camera); // fallback
        }
        canvas_frame.addView(logoView);
    }

    private void addTextOverlay(JSONObject bounds, String text, int defaultColor, float scale, boolean isBold) throws Exception {
        if (text == null || text.trim().isEmpty()) return;

        int x = (int) (bounds.getInt("x") * scale);
        int y = (int) (bounds.getInt("y") * scale);
        int w = (int) (bounds.getInt("w") * scale);
        int h = (int) (bounds.getInt("h") * scale);

        android.widget.TextView textView = new android.widget.TextView(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(w, h);
        params.leftMargin = x;
        params.topMargin = y;
        textView.setLayoutParams(params);
        textView.setText(text);
        
        // Custom Color Support
        int finalColor = defaultColor;
        if (bounds.has("color")) {
            try {
                finalColor = android.graphics.Color.parseColor(bounds.getString("color"));
            } catch (Exception e) {}
        }
        textView.setTextColor(finalColor);
        
        textView.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
        textView.setMaxLines(1);
        
        // Custom Font Size Support
        if (bounds.has("size")) {
            try {
                float customSize = (float) bounds.getDouble("size");
                // Scale the custom size appropriately, or just use it directly. Usually SP is fine.
                // But since coordinates are scaled, we should scale the text size too.
                textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, customSize * scale);
            } catch (Exception e) {}
        } else {
            // Auto-sizing text!
            androidx.core.widget.TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, 6, 200, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        }

        if (isBold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        // Optional shadow from API or fallback
        if (bounds.optBoolean("shadow", true)) {
            textView.setShadowLayer(3, 1, 1, android.graphics.Color.BLACK);
        }

        canvas_frame.addView(textView);
    }

    private void saveCanvasToImage() {
        Bitmap bitmap = Bitmap.createBitmap(canvas_frame.getWidth(), canvas_frame.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas_frame.draw(canvas);

        try {
            File dir = new File(getExternalFilesDir(null), "PersonalizedPosters");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "poster_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}
