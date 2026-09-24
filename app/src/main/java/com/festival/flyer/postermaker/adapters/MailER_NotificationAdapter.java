package com.festival.flyer.postermaker.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.models.MailER_NotificationModel;
import com.festival.flyer.postermaker.utils.MailER_NotificationHelper;

import java.util.List;

public class MailER_NotificationAdapter extends RecyclerView.Adapter<MailER_NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<MailER_NotificationModel> list;

    public MailER_NotificationAdapter(Context context, List<MailER_NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.spawner_item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailER_NotificationModel model = list.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvMessage.setText(model.getMessage());

        long now = System.currentTimeMillis();
        long diff = Math.abs(now - model.getTimestamp());

        String displayTime;
        if (diff < DateUtils.MINUTE_IN_MILLIS) {
            displayTime = "Just now";
        } else {
            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    model.getTimestamp(),
                    now,
                    DateUtils.MINUTE_IN_MILLIS);
            displayTime = relative.toString();
            if (displayTime.equalsIgnoreCase("0 minutes ago") || displayTime.equalsIgnoreCase("0 mins ago")) {
                displayTime = "Just now";
            }
        }
        holder.tvTime.setText(displayTime);

        // Unread dot indicator
        if (holder.viewUnreadDot != null) {
            holder.viewUnreadDot.setVisibility(model.isRead() ? View.GONE : View.VISIBLE);
        }

        // Notification Thumbnail Image Handling
        String imageUrl = model.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            holder.cardNotifImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.spawner_no_image)
                    .error(R.drawable.spawner_no_image)
                    .into(holder.ivNotifImage);
        } else {
            holder.cardNotifImage.setVisibility(View.GONE);
        }

        // On Item Click - Mark as read & save state
        holder.itemView.setOnClickListener(v -> {
            if (!model.isRead()) {
                model.setRead(true);
                MailER_NotificationHelper.saveAllNotifications(context, list);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivNotifImage;
        CardView cardNotifImage;
        View viewUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            ivNotifImage = itemView.findViewById(R.id.iv_notif_image);
            cardNotifImage = itemView.findViewById(R.id.card_notif_image);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}
