package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;


public class MailER_FontAdapter extends RecyclerView.Adapter<MailER_FontAdapter.MyViewHolder> {

    private final String[] font_style;
    private final Context context;
    private final FontClickListener fontClickListener;

    public interface FontClickListener {
        void onClick(String fontName);
    }

    public MailER_FontAdapter(Context context, String[] font_style, FontClickListener fontClickListener) {
        this.context = context;
        this.font_style = font_style;
        this.fontClickListener = fontClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_font_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Typeface tf_regular1;
        AssetManager assets = this.context.getAssets();
        String sb = "font/" + font_style[position];
        tf_regular1 = Typeface.createFromAsset(assets, sb);
        holder.font_style.setTypeface(tf_regular1);
        holder.itemView.setOnClickListener(v -> fontClickListener.onClick(font_style[position]));
    }

    @Override
    public int getItemCount() {
        return font_style.length;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView font_style;

        public MyViewHolder(View v) {
            super(v);
            font_style = v.findViewById(R.id.font_style);
        }
    }
}
