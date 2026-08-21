package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.google.android.gms.ads.nativead.NativeAdView;

public class MailER_ColorPelleteAdapter extends RecyclerView.Adapter<MailER_ColorPelleteAdapter.MyViewHolder> {

    private final String[] colorList;
    private final int cellWidth;
    private final int cellHeight;
    private final ColorPelleteListener colorPelleteListener;

    public interface ColorPelleteListener {
        void onClick(String colorCode);
    }

    public MailER_ColorPelleteAdapter(Context context, int cellWidth, int cellHeight, ColorPelleteListener colorPelleteListener) {
        this.colorList = context.getResources().getStringArray(R.array.color_list);
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.colorPelleteListener = colorPelleteListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_poster_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.adLoadLayout.setVisibility(View.GONE);
        if (holder.progress_bar != null) {
            holder.progress_bar.setVisibility(View.GONE);
        }
        if (holder.iv_like != null) {
            holder.iv_like.setVisibility(View.GONE);
        }
        holder.iv_image.getLayoutParams().width = cellWidth;
        holder.iv_image.getLayoutParams().height = cellHeight;
        holder.iv_image.invalidate();

        holder.iv_image.setBackgroundColor(Color.parseColor(colorList[position]));

        holder.iv_image.setOnClickListener(v -> colorPelleteListener.onClick(colorList[position]));

    }

    @Override
    public int getItemCount() {
        return colorList.length;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image, iv_like;
        NativeAdView adLoadLayout;
        android.widget.ProgressBar progress_bar;
        public MyViewHolder(View itemView) {
            super(itemView);

            iv_image = itemView.findViewById(R.id.iv_image);
//            titanicTextView = itemView.findViewById(R.id.titanicTextView);
            adLoadLayout = itemView.findViewById(R.id.ad_load_layout);
            iv_like = itemView.findViewById(R.id.iv_like);
            progress_bar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
