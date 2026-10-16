package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.festival.flyer.postermaker.model.MailER_HeroBannerModel;
import java.util.List;

public class MailER_DynamicHeroAdapter extends RecyclerView.Adapter<MailER_DynamicHeroAdapter.ViewHolder> {

    private Context context;
    private List<MailER_HeroBannerModel> bannerList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MailER_HeroBannerModel item);
    }

    public MailER_DynamicHeroAdapter(Context context, List<MailER_HeroBannerModel> bannerList, OnItemClickListener listener) {
        this.context = context;
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.spawner_item_dynamic_hero, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailER_HeroBannerModel model = bannerList.get(position);

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
        return bannerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        ShimmerFrameLayout shimmerItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_hero_banner);
            shimmerItem = itemView.findViewById(R.id.shimmer_hero_item);
        }
    }
}
