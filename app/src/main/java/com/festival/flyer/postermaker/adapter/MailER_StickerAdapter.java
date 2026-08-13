package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.view.MailER_CustomSquareImageView;

import java.util.ArrayList;

public class MailER_StickerAdapter extends RecyclerView.Adapter<MailER_StickerAdapter.MyViewHolder> {

    private final ArrayList<String> stickerArrayList;
    private final Context context;
    private final StickerClickListener stickerClickListener;

    public interface StickerClickListener {
        void onClick(String path);
    }

    public MailER_StickerAdapter(Context context, ArrayList<String> stickerArrayList, StickerClickListener stickerClickListener) {
        this.context = context;
        this.stickerArrayList = stickerArrayList;
        this.stickerClickListener = stickerClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_sticker_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        String sb = stickerArrayList.get(position);
        String pathUri = "file:///android_asset/" + sb;
        Glide.with(context)
                .load(Uri.parse(pathUri))
                .into(holder.thumbnail_image);

        holder.itemView.setOnClickListener(v -> stickerClickListener.onClick(stickerArrayList.get(position)));

    }

    @Override
    public int getItemCount() {
        return stickerArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public MailER_CustomSquareImageView thumbnail_image;

        public MyViewHolder(View v) {
            super(v);
            thumbnail_image = v.findViewById(R.id.thumbnail_image);
        }
    }
}
