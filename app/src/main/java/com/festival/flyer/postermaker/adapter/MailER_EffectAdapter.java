package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;

import java.util.ArrayList;

public class MailER_EffectAdapter extends RecyclerView.Adapter<MailER_EffectAdapter.MyViewHolder> {

    private final Context context;
    private final EffectClickListener effectClickListener;
    private ArrayList<String> effectArrayList;

    public MailER_EffectAdapter(Context context, ArrayList<String> effectArrayList, EffectClickListener effectClickListener) {
        this.context = context;
        this.effectArrayList = effectArrayList;
        this.effectClickListener = effectClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_effect_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        String sb = effectArrayList.get(position);

        String pathUri = "file:///android_asset/" + sb;
        Glide.with(context)
                .load(Uri.parse(pathUri))
                .into(holder.item_image);

        holder.itemView.setOnClickListener(v -> effectClickListener.onClick(effectArrayList.get(position)));

    }

    @Override
    public int getItemCount() {
        return effectArrayList.size();
    }

    public void updateRv(ArrayList<String> list) {
        effectArrayList = list;
        notifyDataSetChanged();
    }

    public interface EffectClickListener {
        void onClick(String path);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView item_image;

        public MyViewHolder(View v) {
            super(v);
            item_image = v.findViewById(R.id.item_image);
        }
    }
}
