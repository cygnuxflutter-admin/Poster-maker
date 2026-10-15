package com.festival.flyer.postermaker.makeityours;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_PosterGroupChildAdapter;
import com.festival.flyer.postermaker.model.MailER_PosterImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MailER_MakeItYoursPosterActivity extends AppCompatActivity {
    
    private RecyclerView rvPosters;
    private ArrayList<MailER_PosterImage> posterList;
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_make_it_yours_poster);
        
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        
        categoryId = getIntent().getStringExtra("category_id");
        String categoryName = getIntent().getStringExtra("category_name");
        
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null && categoryName != null) {
            tvTitle.setText(categoryName);
        }
        
        rvPosters = findViewById(R.id.recycler_view);
        rvPosters.setLayoutManager(new GridLayoutManager(this, 3));
        
        posterList = new ArrayList<>();
        
        loadPostersFromAPI();
    }
    
    private void loadPostersFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://cygnux.in/postermaker/API/V1/MakeItYoursAPI/posters";
        
        Log.e("MAKE_IT_YOURS_API", "Calling Poster API: " + url + " for Category ID: " + categoryId);
        
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.e("MAKE_IT_YOURS_API", "Poster Success Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("data")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject postObj = dataArray.getJSONObject(i);
                                String id = postObj.optString("id");
                                String poster_image = postObj.optString("poster_image");
                                String thumbnail_image = postObj.optString("thumbnail_image");
                                
                                // We store thumbnail in thumb and full image somewhere?
                                // MailER_PosterImage usually takes (id, thumb, ratio)
                                // Let's use poster_image as thumb for now, or just pass it in thumb
                                MailER_PosterImage model = new MailER_PosterImage(id, thumbnail_image, "1:1");
                                // We can store the full poster image URL in ratio or another field if needed.
                                // Actually, let's just pass the full image URL instead of the thumb so the click gets the full image.
                                MailER_PosterImage modelFull = new MailER_PosterImage(id, poster_image, "1:1");
                                posterList.add(modelFull);
                            }
                            
                            int w = getResources().getDisplayMetrics().widthPixels / 3;
                            int h = w;
                            
                            MailER_PosterGroupChildAdapter adapter = new MailER_PosterGroupChildAdapter(this, posterList, w, h, Integer.parseInt(categoryId != null ? categoryId : "1"), new MailER_PosterGroupChildAdapter.OnPosterClickListener() {
                                @Override
                                public void onPosterClick(int position, int clickType, boolean b) {
                                    Intent intent = new Intent(MailER_MakeItYoursPosterActivity.this, MailER_MakeItYoursEditorActivity.class);
                                    // Because we stored the full image URL in post_thumb:
                                    intent.putExtra("poster_image", posterList.get(position).getPost_thumb());
                                    startActivity(intent);
                                }
                            });
                            
                            rvPosters.setAdapter(adapter);
                        } else {
                            Toast.makeText(this, "No posters found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("MAKE_IT_YOURS_API", "Poster JSON Parse Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("MAKE_IT_YOURS_API", "Poster Volley Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Toast.makeText(this, "Poster API Error: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                com.festival.flyer.postermaker.utils.MailER_PreferenceClass preferenceClass = new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(MailER_MakeItYoursPosterActivity.this);
                params.put("key", preferenceClass.getDataType("field_0")); 
                params.put("category_id", categoryId != null ? categoryId : "1");
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
