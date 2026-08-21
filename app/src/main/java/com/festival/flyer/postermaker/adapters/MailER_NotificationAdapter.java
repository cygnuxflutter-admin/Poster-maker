package com.festival.flyer.postermaker.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.models.MailER_NotificationModel;

import java.util.List;

public class MailER_NotificationAdapter extends RecyclerView.Adapter<MailER_NotificationAdapter.ViewHolder> {

    private Context context;
    private List<MailER_NotificationModel> list;

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

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                model.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
        }
    }
}
