package com.festival.flyer.postermaker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapters.MailER_NotificationAdapter;
import com.festival.flyer.postermaker.models.MailER_NotificationModel;
import com.festival.flyer.postermaker.utils.MailER_NotificationHelper;

import java.util.List;

public class MailER_NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private LinearLayout llNoNotifications;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spawner_activity_notification);

        rvNotifications = findViewById(R.id.rv_notifications);
        llNoNotifications = findViewById(R.id.ll_no_notifications);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> onBackPressed());

        loadNotifications();
    }

    private void loadNotifications() {
        List<MailER_NotificationModel> list = MailER_NotificationHelper.getNotifications(this);

        if (list == null || list.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            llNoNotifications.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            llNoNotifications.setVisibility(View.GONE);

            MailER_NotificationAdapter adapter = new MailER_NotificationAdapter(this, list);
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(adapter);
        }
    }
}
