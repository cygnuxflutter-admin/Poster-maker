package com.festival.flyer.postermaker.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.adManager.MailER_NativeAdUtil;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.utils.MailER_TemplateLikeManager;

import java.util.ArrayList;

public class MailER_PosterGroupChildAdapter extends RecyclerView.Adapter<MailER_PosterGroupChildAdapter.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<MailER_PosterImage> posterThumbLists;
    private final int cellWidth, cellHeight, cat_id;
    private final OnPosterClickListener onPosterClickListener;
    private final MailER_NativeAdUtil nativeAdUtil;
    private MailER_PreferenceClass preferenceClass;
    private MailER_TemplateLikeManager likeManager;

    public MailER_PosterGroupChildAdapter(Activity activity, ArrayList<MailER_PosterImage> posterThumbLists, int cat_id, int cellWidth, int cellHeight, OnPosterClickListener onPosterClickListener) {
        this.activity = activity;
        this.posterThumbLists = posterThumbLists;
        this.cat_id = cat_id;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.onPosterClickListener = onPosterClickListener;
        this.nativeAdUtil = new MailER_NativeAdUtil(activity, cellWidth, cellHeight);
        preferenceClass = new MailER_PreferenceClass(activity);
        likeManager = new MailER_TemplateLikeManager(activity);
    }

    private static final int TYPE_POSTER = 0;
    private static final int TYPE_AD = 1;

    @Override
    public int getItemViewType(int position) {
        if (position < posterThumbLists.size() && posterThumbLists.get(position) == null) {
            return TYPE_AD;
        }
        return TYPE_POSTER;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_poster_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        if (posterThumbLists.get(position) != null) {
            holder.itemView.setVisibility(View.VISIBLE);
            if (holder.rlMain != null) {
                holder.rlMain.setVisibility(View.VISIBLE);
            }
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null) {
                lp.width = cellWidth;
                lp.height = cellHeight;
                holder.itemView.setLayoutParams(lp);
            }
            if (holder.iv_image.getLayoutParams() != null) {
                holder.iv_image.getLayoutParams().width = cellWidth;
                holder.iv_image.getLayoutParams().height = cellHeight;
                holder.iv_image.invalidate();
            }

            if (posterThumbLists.get(position).getPremium()) {
                holder.iv_lock.setVisibility(View.VISIBLE);
            } else {
                holder.iv_lock.setVisibility(View.GONE);
            }

            holder.content_layout.setVisibility(View.VISIBLE);
            holder.ad_layout.setVisibility(View.GONE);
            holder.progress_bar.setVisibility(View.VISIBLE);
            Glide.with(activity)
                    .load(posterThumbLists.get(position).getPost_thumb())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            holder.progress_bar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            holder.progress_bar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .centerCrop()
                    .into(holder.iv_image);

            if (holder.iv_like != null) {
                holder.iv_like.setVisibility(View.VISIBLE);
                holder.iv_like.setImageResource(likeManager.isLiked(posterThumbLists.get(position).getPost_id()) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
                holder.iv_like.setOnClickListener(v -> {
                    posterThumbLists.get(position).setCat_id(cat_id);
                    likeManager.toggleLike(posterThumbLists.get(position));
                    holder.iv_like.setImageResource(likeManager.isLiked(posterThumbLists.get(position).getPost_id()) ? R.drawable.spawner_ic_heart_filled : R.drawable.spawner_ic_heart_outline);
                });
            }

            holder.iv_image.setOnClickListener(v -> {
                if (holder.iv_image.getDrawable() == null) {
                    return;
                }
                int actualCatId = cat_id != 0 ? cat_id : posterThumbLists.get(position).getCat_id();
                onPosterClickListener.onPosterClick(actualCatId, posterThumbLists.get(position).getPost_id(), holder.iv_lock.getVisibility() == View.VISIBLE);
            });
        } else {
            // Full Width Native Ad Item (covering height of 2 posters in the row)
            holder.itemView.setVisibility(View.VISIBLE);
            if (holder.rlMain != null) {
                holder.rlMain.setVisibility(View.VISIBLE);
            }
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = cellHeight;
                holder.itemView.setLayoutParams(lp);
            }
            if (holder.native_banner_ad_container.getLayoutParams() != null) {
                holder.native_banner_ad_container.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                holder.native_banner_ad_container.getLayoutParams().height = cellHeight;
                holder.native_banner_ad_container.invalidate();
            }
            if (holder.ad_layout.getLayoutParams() != null) {
                holder.ad_layout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                holder.ad_layout.getLayoutParams().height = cellHeight;
                holder.ad_layout.invalidate();
            }

            holder.content_layout.setVisibility(View.GONE);
            holder.ad_layout.setVisibility(View.VISIBLE);
            holder.iv_lock.setVisibility(View.GONE);
            nativeAdUtil.fillAdmobNativeAd(holder.native_banner_ad_container, position, null, new MailER_NativeAdUtil.OnAdLoadListener() {
                @Override
                public void onAdLoaded() {
                    if (holder.itemView != null) {
                        holder.itemView.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                        if (lp != null) {
                            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            lp.height = cellHeight;
                            holder.itemView.setLayoutParams(lp);
                        }
                    }
                    if (holder.rlMain != null) holder.rlMain.setVisibility(View.VISIBLE);
                    if (holder.ad_layout != null) holder.ad_layout.setVisibility(View.VISIBLE);
                    if (holder.native_banner_ad_container != null) holder.native_banner_ad_container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailed() {
                    boolean showShimmerOnFail = MailER_PreferenceClass.isNativeShimmerOnFail(activity);
                    if (!showShimmerOnFail) {
                        if (holder.itemView != null) {
                            holder.itemView.setVisibility(View.GONE);
                            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                            if (lp != null) {
                                lp.width = 0;
                                lp.height = 0;
                                holder.itemView.setLayoutParams(lp);
                            }
                        }
                        if (holder.rlMain != null) holder.rlMain.setVisibility(View.GONE);
                        if (holder.ad_layout != null) holder.ad_layout.setVisibility(View.GONE);
                        if (holder.native_banner_ad_container != null) holder.native_banner_ad_container.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return posterThumbLists.size();
    }

    public interface OnPosterClickListener {
        void onPosterClick(int cat_id, int post_id, boolean premium);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout content_layout, ad_layout, native_banner_ad_container, rlMain;
        private final ImageView iv_image, iv_like;
        private final TextView iv_lock;
        private final ProgressBar progress_bar;

        MyViewHolder(View itemView) {
            super(itemView);

            rlMain = itemView.findViewById(R.id.rlMain);
            iv_image = itemView.findViewById(R.id.iv_image);
            progress_bar = itemView.findViewById(R.id.progress_bar);
            iv_like = itemView.findViewById(R.id.iv_like);
            iv_lock = itemView.findViewById(R.id.iv_lock);
            content_layout = itemView.findViewById(R.id.content_layout);
            ad_layout = itemView.findViewById(R.id.ad_layout);
            native_banner_ad_container = itemView.findViewById(R.id.native_banner_ad_container);
            //TitanicTextView titanicTextView = itemView.findViewById(R.id.titanicTextView);
//            ImageView titanicTextView = itemView.findViewById(R.id.titanicTextView);
          //  new Titanic().start(titanicTextView);
        }
    }
}
