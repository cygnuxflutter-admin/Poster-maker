package com.festival.flyer.postermaker.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.festival.flyer.postermaker.utils.MailER_LikeManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.MailER_BgImage;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import java.util.ArrayList;

public class MailER_BackgroundChildAdapter extends RecyclerView.Adapter<MailER_BackgroundChildAdapter.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<MailER_BgImage> bgImages;
    private final int cellWidth;
    private final int cellHeight;
    private final OnBgClickListener onBgClickListener;
    private final MailER_NativeAdUtil nativeAdUtil;
    private MailER_PreferenceClass preferenceClass;
    private final MailER_LikeManager likeManager;

    public MailER_BackgroundChildAdapter(Activity activity, ArrayList<MailER_BgImage> bgImages, int cellWidth, int cellHeight, OnBgClickListener onBgClickListener) {
        this.activity = activity;
        this.bgImages = bgImages;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.onBgClickListener = onBgClickListener;
        this.nativeAdUtil = new MailER_NativeAdUtil(activity, cellWidth, cellHeight);
        preferenceClass = new MailER_PreferenceClass(activity);
        this.likeManager = new MailER_LikeManager(activity);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_poster_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.native_banner_ad_container.getLayoutParams().width = cellWidth;
        holder.native_banner_ad_container.getLayoutParams().height = cellHeight;
        holder.native_banner_ad_container.invalidate();

        holder.iv_image.getLayoutParams().width = cellWidth;
        holder.iv_image.getLayoutParams().height = cellHeight;
        holder.iv_image.invalidate();

        boolean isPremium = false;
        if (bgImages.get(position) != null) {
            isPremium = bgImages.get(position).isPremium();
        }

        if (isPremium) {
            holder.iv_lock.setVisibility(View.VISIBLE);
        } else {
            holder.iv_lock.setVisibility(View.GONE);
        }

        if (bgImages.get(position) != null) {
            holder.content_layout.setVisibility(View.VISIBLE);
            holder.ad_layout.setVisibility(View.GONE);
            Glide.with(activity)
                    .load(bgImages.get(position).getThumb_url())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .centerCrop()
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.iv_image.setImageDrawable(resource);
                            float aspectRatio = (float) resource.getIntrinsicWidth() / (float) resource.getIntrinsicHeight();
                            if (aspectRatio > 1) {
                                holder.iv_image.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT));
                            } else {
                                holder.iv_image.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT));
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });


            Log.e("TAG", "onBindViewHolder: " + bgImages.get(position).getThumb_url());

            String bgUrl = bgImages.get(position).getImage_url();
            holder.iv_like.setImageResource(likeManager.isLiked(bgUrl) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);

            holder.iv_like.setOnClickListener(v -> {
                likeManager.toggleLike(bgUrl);
                holder.iv_like.setImageResource(likeManager.isLiked(bgUrl) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
            });

            holder.iv_image.setOnClickListener(v -> {
                if (holder.iv_image.getDrawable() == null) {
                    return;
                }

                onBgClickListener.onClick(bgUrl, holder.iv_lock.getVisibility() == View.VISIBLE);
            });
        } else {
            holder.content_layout.setVisibility(View.GONE);
            holder.ad_layout.setVisibility(View.VISIBLE);
            holder.iv_lock.setVisibility(View.GONE);
            nativeAdUtil.fillAdmobNativeAd(holder.native_banner_ad_container);
        }
    }

    @Override
    public int getItemCount() {
        return bgImages.size();
    }

    public interface OnBgClickListener {
        void onClick(String path, boolean premium);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout content_layout, ad_layout, native_banner_ad_container;
        private final ImageView iv_image, iv_like;
        private final TextView iv_lock;

        MyViewHolder(View itemView) {
            super(itemView);

            iv_image = itemView.findViewById(R.id.iv_image);
            iv_like = itemView.findViewById(R.id.iv_like);
            iv_lock = itemView.findViewById(R.id.iv_lock);
            content_layout = itemView.findViewById(R.id.content_layout);
            ad_layout = itemView.findViewById(R.id.ad_layout);
            native_banner_ad_container = itemView.findViewById(R.id.native_banner_ad_container);
//            ImageView titanicTextView = itemView.findViewById(R.id.titanicTextView);
            // new Titanic().start(titanicTextView);
        }
    }
}
