package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.MailER_TrendingFestivalModel;
import java.util.List;

public class MailER_TrendingFestivalAdapter extends RecyclerView.Adapter<MailER_TrendingFestivalAdapter.ViewHolder> {

    private Context context;
    private List<MailER_TrendingFestivalModel> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MailER_TrendingFestivalModel item);
    }

    public MailER_TrendingFestivalAdapter(Context context, List<MailER_TrendingFestivalModel> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.spawner_item_trending_festival, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailER_TrendingFestivalModel model = list.get(position);
        holder.tvTitle.setText(model.getCat_name() != null ? model.getCat_name() : "");
        
        Glide.with(context)
                .load(model.getBanner_image())
                .placeholder(R.drawable.spawner_no_image)
                .into(holder.ivBanner);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_trending_banner);
            tvTitle = itemView.findViewById(R.id.tv_trending_title);
        }
    }
}
