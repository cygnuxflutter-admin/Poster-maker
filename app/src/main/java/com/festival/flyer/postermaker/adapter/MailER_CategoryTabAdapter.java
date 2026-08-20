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
import com.festival.flyer.postermaker.model.MailER_PosterModel;

import java.util.ArrayList;

public class MailER_CategoryTabAdapter extends RecyclerView.Adapter<MailER_CategoryTabAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MailER_PosterModel> categories;
    private final OnTabClickListener listener;
    private boolean isGrid = false;
    private int selectedPosition = 0;

    public interface OnTabClickListener {
        void onTabClick(int position, MailER_PosterModel model);
    }

    public MailER_CategoryTabAdapter(Context context, ArrayList<MailER_PosterModel> categories, boolean isGrid, OnTabClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.isGrid = isGrid;
        this.listener = listener;
    }

    public void updateData(ArrayList<MailER_PosterModel> newCategories) {
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

    private String getEmojiForCategory(String title) {
        if (title == null) return "✨";
        String t = title.toLowerCase();
        if (t.contains("raksha") || t.contains("rakhi")) return "🪢";
        if (t.contains("thank")) return "🙏";
        if (t.contains("summer")) return "☀️";
        if (t.contains("education") || t.contains("webinar") || t.contains("educationss")) return "📚";
        if (t.contains("sport") || t.contains("contest")) return "🏆";
        if (t.contains("advertising") || t.contains("campaign")) return "📢";
        if (t.contains("wedding") || t.contains("anniversary")) return "💍";
        if (t.contains("photography")) return "📷";
        if (t.contains("motivation")) return "💪";
        if (t.contains("food")) return "🍔";
        if (t.contains("christmas")) return "🎄";
        if (t.contains("days") || t.contains("event")) return "📅";
        if (t.contains("real estate")) return "🏠";
        if (t.contains("beauty") || t.contains("cosmetic") || t.contains("spa")) return "💄";
        if (t.contains("festa junina")) return "🎊";
        if (t.contains("music")) return "🎵";
        if (t.contains("fitness")) return "🏋️";
        if (t.contains("car")) return "🚗";
        if (t.contains("coffee")) return "☕";
        if (t.contains("easter")) return "🥚";
        if (t.contains("job") || t.contains("hiring")) return "💼";
        if (t.contains("cleaning")) return "🧹";
        if (t.contains("insurance")) return "🛡️";
        if (t.contains("social") || t.contains("mobile") || t.contains("phone")) return "📱";
        if (t.contains("ice cream")) return "🍦";
        if (t.contains("conference")) return "🎤";
        if (t.contains("oktober") || t.contains("festival")) return "🍻";
        if (t.contains("carpentry") || t.contains("repair")) return "🪚";
        if (t.contains("barber")) return "💈";
        if (t.contains("electronic") || t.contains("computer")) return "💻";
        if (t.contains("valentine") || t.contains("love")) return "❤️";
        if (t.contains("party")) return "🎉";
        if (t.contains("medical")) return "⚕️";
        if (t.contains("jewellery")) return "💎";
        if (t.contains("opening")) return "✂️";
        if (t.contains("wanted")) return "🕵️";
        if (t.contains("july") || t.contains("year")) return "🎆";
        if (t.contains("sale") || t.contains("friday") || t.contains("monday")) return "🛍️";
        if (t.contains("diwali") || t.contains("krathong")) return "🪔";
        if (t.contains("halloween")) return "🎃";
        if (t.contains("navratri") || t.contains("dance")) return "💃";
        if (t.contains("muharram") || t.contains("eid") || t.contains("night")) return "🌙";
        if (t.contains("brazilian")) return "🎭";
        if (t.contains("tour") || t.contains("travel")) return "✈️";
        if (t.contains("autumn")) return "🍁";
        if (t.contains("giving")) return "🦃";
        if (t.contains("sankranti")) return "🪁";
        if (t.contains("republic") || t.contains("independence")) return "🇮🇳";
        if (t.contains("church")) return "⛪";
        if (t.contains("certificate") || t.contains("notice")) return "📜";
        if (t.contains("black") || t.contains("plain")) return "⚫";
        if (t.contains("mix")) return "🔀";
        if (t.contains("baby")) return "👶";
        if (t.contains("monsoon")) return "🌧️";
        if (t.contains("art") || t.contains("design") || t.contains("holi")) return "🎨";
        if (t.contains("ganesh")) return "🐘";
        if (t.contains("volunteer") || t.contains("worker")) return "🤝";
        if (t.contains("law") || t.contains("firm")) return "⚖️";
        if (t.contains("business") || t.contains("marketing")) return "📈";
        if (t.contains("abstract")) return "🌀";
        if (t.contains("asset")) return "📁";
        if (t.contains("fashion")) return "👗";
        if (t.contains("gradient")) return "🌈";
        if (t.contains("morning")) return "🌅";
        if (t.contains("women")) return "👩";
        if (t.contains("more")) return "➕";
        return "✨";
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MailER_PosterModel model = categories.get(position);

        String catName = model.getCat_name();
        if (catName == null) catName = "";
        String originalTitle = catName.replace("\n", " ");
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

        holder.tvEmoji.setText(getEmojiForCategory(originalTitle));

        boolean isSelected = (position == selectedPosition);
        holder.llRoot.setSelected(isSelected);

        if (isSelected) {
            holder.tvText.setTextColor(Color.WHITE);
        } else {
            holder.tvText.setTextColor(Color.parseColor("#21005D"));
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
        TextView tvEmoji;
        TextView tvText;

        ViewHolder(View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.ll_tab_root);
            tvEmoji = itemView.findViewById(R.id.tv_tab_emoji);
            tvText = itemView.findViewById(R.id.tv_tab_text);
        }
    }
}
