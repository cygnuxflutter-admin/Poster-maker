package com.festival.flyer.postermaker.makeityours;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter;
import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.festival.flyer.postermaker.model.MailER_PosterModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MailER_MakeItYoursCategoryActivity extends AppCompatActivity {
    
    private RecyclerView rvCategories;
    private MailER_CategoryTabAdapter adapter;
    private ArrayList<MailER_PosterModel> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_make_it_yours_category);
        
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        
        rvCategories = findViewById(R.id.recycler_view);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));
        
        categoryList = new ArrayList<>();
        
        loadCategoriesFromAPI();
    }
    
    private void loadCategoriesFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(this);
        // User provided API URL
        String url = "https://cygnux.in/postermaker/API/V1/MakeItYoursAPI/categories";
        
        Log.e("MAKE_IT_YOURS_API", "Calling API: " + url);
        
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.e("MAKE_IT_YOURS_API", "Success Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("data")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject catObj = dataArray.getJSONObject(i);
                                String id = catObj.optString("id");
                                String name = catObj.optString("name");
                                String image = catObj.optString("image");
                                
                                // Create a model. (We use MailER_PosterModel which holds categories)
                                MailER_PosterModel model = new MailER_PosterModel(id, image, name, new ArrayList<>());
                                categoryList.add(model);
                            }
                            
                            adapter = new MailER_CategoryTabAdapter(MailER_MakeItYoursCategoryActivity.this, categoryList, true, (position, model) -> {
                                Intent intent = new Intent(MailER_MakeItYoursCategoryActivity.this, MailER_MakeItYoursPosterActivity.class);
                                intent.putExtra("category_id", model.getCat_id());
                                intent.putExtra("category_name", model.getCat_name());
                                startActivity(intent);
                            });
                            rvCategories.setAdapter(adapter);
                        } else {
                            Toast.makeText(this, "No data found in response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("MAKE_IT_YOURS_API", "JSON Parse Error: " + e.getMessage());
                        Toast.makeText(this, "JSON Parse Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MAKE_IT_YOURS_API", "Volley Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("MAKE_IT_YOURS_API", "Status Code: " + error.networkResponse.statusCode);
                        Toast.makeText(this, "API Error: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Network Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device", "1");
                com.festival.flyer.postermaker.utils.MailER_PreferenceClass preferenceClass = new com.festival.flyer.postermaker.utils.MailER_PreferenceClass(MailER_MakeItYoursCategoryActivity.this);
                params.put("key", preferenceClass.getDataType("field_0")); // Or whatever key is required
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
