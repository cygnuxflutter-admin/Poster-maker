package com.festival.flyer.postermaker.makeityours.auto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;

import org.json.JSONObject;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import java.util.HashMap;
import java.util.Map;

public class MailER_BusinessProfileActivity extends AppCompatActivity {

    private ImageView iv_business_logo;
    private EditText et_business_name, et_phone, et_address;
    
    private Button btn_save_profile, btn_next_poster;

    private static final int PICK_IMAGE_REQUEST = 101;
    private String selectedLogoUri = "";
    private String base64Logo = "";

    private RequestQueue requestQueue;
    private final String BASE_URL = "https://cygnux.in/postermaker/index.php/API/V1/MakeItYoursAPI/";
    private final String SECRET_KEY = "drytdsry6ne48ndr8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_business_profile);

        requestQueue = Volley.newRequestQueue(this);

        iv_business_logo = findViewById(R.id.iv_business_logo);
        btn_save_profile = findViewById(R.id.btn_save_profile);
        btn_next_poster = findViewById(R.id.btn_next_poster);

        setupField(R.id.card_name, "Business Name *", "Enter your business name");
        setupField(R.id.card_phone, "Phone Number *", "Enter your phone number");
        setupField(R.id.card_address, "Address", "Enter your business address");
        
        
        
        // Setup new fields if they exist in XML
        
        
        
        

        View cName = findViewById(R.id.card_name);
        et_business_name = cName.findViewById(R.id.field_input);
        
        View cPhone = findViewById(R.id.card_phone);
        et_phone = cPhone.findViewById(R.id.field_input);
        
        View cAddress = findViewById(R.id.card_address);
        if (cAddress != null) et_address = cAddress.findViewById(R.id.field_input);
        
        
        
        

        // Fetch Live Data from Backend instead of Local Storage
        fetchBusinessProfileAPI();

        iv_business_logo.setOnClickListener(v -> openImagePicker());

        android.widget.ImageView ivClearLogo = findViewById(R.id.iv_clear_logo);
        ivClearLogo.setOnClickListener(v -> {
            selectedLogoUri = "";
            base64Logo = "";
            iv_business_logo.setImageResource(android.R.drawable.ic_menu_camera);
            iv_business_logo.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B5CF6")));
            iv_business_logo.setPadding(60, 60, 60, 60); // Roughly 20dp in pixels
            ivClearLogo.setVisibility(android.view.View.GONE);
        });

        android.widget.TextView tvUseDefault = findViewById(R.id.tv_use_default_logo);
        tvUseDefault.setOnClickListener(v -> {
            showDefaultIconsDialog();
        });

        

        btn_save_profile.setOnClickListener(v -> saveBusinessProfileAPI());
        
        btn_next_poster.setVisibility(android.view.View.GONE); // Hidden since we now auto-redirect on save
        btn_next_poster.setOnClickListener(v -> {
            startActivity(new Intent(MailER_BusinessProfileActivity.this, com.festival.flyer.postermaker.activities.MailER_CustomCategoryActivity.class));
        });
        
        ImageView back = findViewById(R.id.ic_back);
        if(back != null) back.setOnClickListener(v -> onBackPressed());
    }

    private void setupField(int cardId, String title, String hint) {
        View card = findViewById(cardId);
        if (card != null) {
            TextView tvTitle = card.findViewById(R.id.field_title);
            EditText etInput = card.findViewById(R.id.field_input);
            if (tvTitle != null) tvTitle.setText(title);
            if (etInput != null) etInput.setHint(hint);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Logo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                
                // Copy to internal storage so we have permanent access
                java.io.File internalFile = new java.io.File(getFilesDir(), "profile_logo.png");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(internalFile);
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                
                selectedLogoUri = internalFile.getAbsolutePath();
                
                // Convert to Base64 for Server Upload
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                base64Logo = "data:image/png;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                
                Glide.with(this).load(internalFile).into(iv_business_logo);
                iv_business_logo.setImageTintList(null);
                iv_business_logo.setPadding(0,0,0,0);
                
                android.widget.ImageView ivClearLogo = findViewById(R.id.iv_clear_logo);
                if(ivClearLogo != null) ivClearLogo.setVisibility(android.view.View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private String getOrCreateDeviceUID() {
        SharedPreferences prefs = getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE);
        String uid = prefs.getString("user_uid", "");
        if (uid.isEmpty()) {
            uid = java.util.UUID.randomUUID().toString();
            prefs.edit().putString("user_uid", uid).apply();
        }
        return uid;
    }

    // ==========================================
    // API CALLS (Volley POST)

    // ==========================================

    private void fetchBusinessProfileAPI() {
        String url = BASE_URL + "get_business_profile";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.has("data")) {
                            JSONObject data = res.getJSONObject("data");
                            
                            // Map JSON keys to your UI. Change keys if your backend uses different names.
                            et_business_name.setText(data.optString("business_name", ""));
                            et_phone.setText(data.optString("phone", ""));
                            if (et_address != null) et_address.setText(data.optString("address", ""));
                            
                            
                            
                            String logoUrl = data.optString("logo_url", data.optString("logo", ""));
                            if (!logoUrl.isEmpty()) {
                                selectedLogoUri = logoUrl;
                                Glide.with(this).load(logoUrl).into(iv_business_logo);
                                iv_business_logo.setImageTintList(null);
                                iv_business_logo.setPadding(0,0,0,0);
                                android.widget.ImageView ivClearLogo = findViewById(R.id.iv_clear_logo);
                                if(ivClearLogo != null) ivClearLogo.setVisibility(android.view.View.VISIBLE);
                            }
                            
                            // Save locally for the adapter to read during poster generation
                            saveToLocalStorage(selectedLogoUri, et_business_name.getText().toString(), et_phone.getText().toString(), et_address != null ? et_address.getText().toString() : "" , "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("API_ERROR", "Error fetching profile: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                params.put("key", new MailER_PreferenceClass(getApplicationContext()).getDataType("field_0"));
                params.put("uid", getOrCreateDeviceUID());
                params.put("user_uid", getOrCreateDeviceUID());
                params.put("user_id", getOrCreateDeviceUID());
                // Optionally pass user_id or device_id if needed by backend to fetch specific profile
                return params;
            }
        };

        requestQueue.add(request);
    }

        private void showDefaultIconsDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.spawner_dialog_default_icons, null);
        dialog.setContentView(view);
        
        android.widget.GridLayout grid = view.findViewById(R.id.grid_icons);
        
        int[] icons = {
            R.drawable.ic_default_logo, // Briefcase
            R.drawable.ic_default_shop,
            R.drawable.ic_default_home,
            R.drawable.ic_default_tech,
            R.drawable.ic_default_food,
            R.drawable.ic_default_travel,
            R.drawable.ic_default_edu,
            R.drawable.ic_default_medical,
            R.drawable.ic_default_finance,
            R.drawable.ic_default_person
        };
        
        String[] titles = {
            "Business", "Shop", "Real Estate", "Tech / IT", "Food", "Travel", "Education", "Medical", "Finance", "Profile"
        };
        
        for (int i = 0; i < icons.length; i++) {
            final int iconRes = icons[i];
            final String titleText = titles[i];
            
            android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
            ll.setOrientation(android.widget.LinearLayout.VERTICAL);
            ll.setGravity(android.view.Gravity.CENTER);
            ll.setPadding(20, 20, 20, 20);
            
            // Layout params for grid
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = 0;
            params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            ll.setLayoutParams(params);
            
            android.widget.ImageView iv = new android.widget.ImageView(this);
            iv.setImageResource(iconRes);
            iv.setPadding(30, 30, 30, 30);
            iv.setBackgroundResource(R.drawable.bg_circle_purple);
            android.widget.LinearLayout.LayoutParams ivParams = new android.widget.LinearLayout.LayoutParams(120, 120);
            iv.setLayoutParams(ivParams);
            
            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(titleText);
            tv.setTextSize(10f);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextColor(android.graphics.Color.parseColor("#6B7280"));
            android.widget.LinearLayout.LayoutParams tvParams = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.topMargin = 10;
            tv.setLayoutParams(tvParams);
            
            ll.addView(iv);
            ll.addView(tv);
            
            ll.setOnClickListener(v -> {
                dialog.dismiss();
                iv_business_logo.setImageResource(iconRes);
                iv_business_logo.setImageTintList(null);
                iv_business_logo.setPadding(0, 0, 0, 0);
                
                android.widget.ImageView ivClearLogo = findViewById(R.id.iv_clear_logo);
                if(ivClearLogo != null) ivClearLogo.setVisibility(android.view.View.VISIBLE);

                try {
                    android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(this, iconRes);
                    android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    
                    // Draw a white background circle for the final PNG so it looks like a clean badge
                    android.graphics.Paint paint = new android.graphics.Paint();
                    paint.setColor(android.graphics.Color.WHITE);
                    paint.setAntiAlias(true);
                    canvas.drawCircle(256, 256, 256, paint);

                    // Draw the vector icon slightly smaller inside
                    drawable.setBounds(100, 100, 412, 412);
                    drawable.draw(canvas);
                    
                    java.io.File cachePath = new java.io.File(getCacheDir(), "images");
                    cachePath.mkdirs(); 
                    java.io.File file = new java.io.File(cachePath, "default_logo.png");
                    java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    android.net.Uri imageUri = androidx.core.content.FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".provider",
                            file);
                    
                    selectedLogoUri = imageUri.toString();
                    
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    base64Logo = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                    
                    android.widget.Toast.makeText(this, titleText + " Icon Selected", android.widget.Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            grid.addView(ll);
        }
        
        dialog.show();
    }


    private void saveBusinessProfileAPI() {
        String bName = et_business_name.getText().toString().trim();
        String bPhone = et_phone.getText().toString().trim();
        String bAddress = et_address != null ? et_address.getText().toString().trim() : "";
        
        if (selectedLogoUri == null || selectedLogoUri.isEmpty()) {
            Toast.makeText(this, "Please select your Business Logo!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bName.isEmpty()) {
            Toast.makeText(this, "Please enter your Business Name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bPhone.isEmpty()) {
            Toast.makeText(this, "Please enter your Phone Number!", Toast.LENGTH_SHORT).show();
            return;
        }

        btn_save_profile.setText("SAVING...");
        btn_save_profile.setEnabled(false);

        String url = BASE_URL + "save_business_profile";

        com.festival.flyer.postermaker.utils.VolleyMultipartRequest request = new com.festival.flyer.postermaker.utils.VolleyMultipartRequest(
                Request.Method.POST, url,
                response -> {
                    btn_save_profile.setText("SAVE PROFILE ✨");
                    btn_save_profile.setEnabled(true);
                    try {
                        String resStr = new String(response.data, "UTF-8");
                        JSONObject res = new JSONObject(resStr);
                        Toast.makeText(this, res.optString("message", "Profile Saved Successfully!"), Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("business_name", bName);
                        editor.putString("phone", bPhone);
                        editor.putString("address", bAddress);
                        editor.putString("logo_uri", selectedLogoUri);
                        editor.apply();
                        
                        android.util.Log.e("MIY_DEBUG", "Saved Profile Locally! Name: " + bName + ", Phone: " + bPhone + ", Logo: " + selectedLogoUri);
                        
                        // Handle logo if needed locally, but API dictates it's uploaded
                        // Start custom category activity
                        startActivity(new Intent(MailER_BusinessProfileActivity.this, com.festival.flyer.postermaker.activities.MailER_CustomCategoryActivity.class));
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parse Error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    btn_save_profile.setText("SAVE PROFILE ✨");
                    btn_save_profile.setEnabled(true);
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                params.put("key", new MailER_PreferenceClass(getApplicationContext()).getDataType("field_0"));
                params.put("user_uid", getOrCreateDeviceUID());
                params.put("business_name", bName);
                params.put("phone", bPhone);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (!base64Logo.isEmpty()) {
                    // Convert base64 back to byte array for upload
                    byte[] decodedString = android.util.Base64.decode(base64Logo, android.util.Base64.DEFAULT);
                    params.put("logo", new DataPart("logo.png", decodedString, "image/png"));
                } else if (selectedLogoUri != null && !selectedLogoUri.isEmpty() && !selectedLogoUri.startsWith("http")) {
                    try {
                        java.io.InputStream iStream = getContentResolver().openInputStream(android.net.Uri.parse(selectedLogoUri));
                        byte[] inputData = getBytes(iStream);
                        params.put("logo", new DataPart("logo.jpg", inputData, "image/jpeg"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return params;
            }
        };

        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                10000,
                com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private byte[] getBytes(java.io.InputStream inputStream) throws java.io.IOException {
        java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void saveToLocalStorage(String logo, String name, String phone, String address, String email) {
        SharedPreferences prefs = getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logo_uri", logo);
        editor.putString("business_name", name);
        editor.putString("phone", phone);
        editor.putString("address", address);
        editor.putString("email", email);
        editor.apply();
    }
}
