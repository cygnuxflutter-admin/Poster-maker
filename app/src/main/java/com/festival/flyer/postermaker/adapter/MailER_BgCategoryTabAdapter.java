package com.festival.flyer.postermaker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.MailER_BgModel;

import java.util.ArrayList;

public class MailER_BgCategoryTabAdapter extends RecyclerView.Adapter<MailER_BgCategoryTabAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MailER_BgModel> categories;
    private final OnTabClickListener listener;
    private boolean isGrid = false;
    private int selectedPosition = 0;

    public interface OnTabClickListener {
        void onTabClick(int position, MailER_BgModel model);
    }

    public MailER_BgCategoryTabAdapter(Context context, ArrayList<MailER_BgModel> categories, boolean isGrid, OnTabClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.isGrid = isGrid;
        this.listener = listener;
    }

    public void updateData(ArrayList<MailER_BgModel> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGrid ? R.layout.spawner_item_category_grid : R.layout.spawner_item_category_tab;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailER_BgModel model = categories.get(position);

        String originalTitle = model.getCategory_name().replace("\n", " ");
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : originalTitle.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
                titleCase.append(c);
            } else if (nextTitleCase) {
                titleCase.append(Character.toTitleCase(c));
                nextTitleCase = false;
            } else {
                titleCase.append(Character.toLowerCase(c));
            }
        }
        String multiLineTitle = titleCase.toString();
        holder.tvText.setText(multiLineTitle);

        int iconRes = R.drawable.spawner_ic_tab_generic;
        if (originalTitle.contains("Abstract")) {
            iconRes = R.drawable.spawner_ic_tab_abstract;
        } else if (originalTitle.contains("Art")) {
            iconRes = R.drawable.spawner_ic_tab_art;
        } else if (originalTitle.contains("Assets")) {
            iconRes = R.drawable.spawner_ic_tab_assets;
        } else if (originalTitle.contains("Christmas")) {
            iconRes = R.drawable.spawner_ic_tab_christmas;
        } else if (originalTitle.contains("Event") || originalTitle.contains("Wedding") || originalTitle.contains("Sale") || originalTitle.contains("Birthday")) {
            iconRes = R.drawable.spawner_ic_tab_event;
        } else if (originalTitle.contains("Love") || originalTitle.contains("Valentine") || originalTitle.contains("Anniversary")) {
            iconRes = R.drawable.spawner_ic_tab_heart;
        } else if (originalTitle.contains("Sports")) {
            iconRes = R.drawable.spawner_ic_tab_sports;
        } else if (originalTitle.contains("Morning") || originalTitle.contains("Day")) {
            iconRes = R.drawable.spawner_ic_tab_sun;
        } else if (originalTitle.contains("Education")) {
            iconRes = R.drawable.spawner_ic_tab_education;
        } else if (originalTitle.contains("Rakhi") || originalTitle.contains("Raksha")) {
            iconRes = R.drawable.spawner_ic_tab_rakhi_mono;
        } else if (originalTitle.contains("More")) {
            iconRes = R.drawable.spawner_ic_apps;
        }

        holder.ivIcon.setImageResource(iconRes);

        boolean isSelected = (position == selectedPosition);
        holder.llRoot.setSelected(isSelected);

        if (isSelected) {
            holder.tvText.setTextColor(Color.WHITE);
            holder.ivIcon.setColorFilter(Color.WHITE);
        } else {
            holder.tvText.setTextColor(Color.BLACK);
            holder.ivIcon.clearColorFilter();
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabClick(position, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llRoot;
        ImageView ivIcon;
        TextView tvText;

        ViewHolder(View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.ll_tab_root);
            ivIcon = itemView.findViewById(R.id.iv_tab_icon);
            tvText = itemView.findViewById(R.id.tv_tab_text);
        }
    }
}
