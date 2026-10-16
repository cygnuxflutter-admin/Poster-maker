package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
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
        
        if (holder.shimmerItem != null) {
            holder.shimmerItem.setVisibility(View.VISIBLE);
            holder.shimmerItem.startShimmer();
        }

        Glide.with(context)
                .load(model.getBanner_image())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object modelObj, Target<Drawable> target, boolean isFirstResource) {
                        if (holder.shimmerItem != null) {
                            holder.shimmerItem.stopShimmer();
                            holder.shimmerItem.setVisibility(View.GONE);
                        }
                        holder.ivBanner.setImageResource(R.drawable.spawner_bg_placeholder_top_rounded);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object modelObj, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (holder.shimmerItem != null) {
                            holder.shimmerItem.stopShimmer();
                            holder.shimmerItem.setVisibility(View.GONE);
                        }
                        return false;
                    }
                })
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
        ShimmerFrameLayout shimmerItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_trending_banner);
            tvTitle = itemView.findViewById(R.id.tv_trending_title);
            shimmerItem = itemView.findViewById(R.id.shimmer_trending_item);
        }
    }
}
