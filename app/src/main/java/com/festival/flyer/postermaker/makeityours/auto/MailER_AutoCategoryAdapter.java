package com.festival.flyer.postermaker.makeityours.auto;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;
import org.json.JSONArray;
import org.json.JSONObject;

public class MailER_AutoCategoryAdapter extends RecyclerView.Adapter<MailER_AutoCategoryAdapter.ViewHolder> {
    private Context context;
    private JSONArray categoryList;
    private int selectedPosition = 0;
    private boolean isGrid = false;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryId);
    }

    public MailER_AutoCategoryAdapter(Context context, JSONArray categoryList, OnCategoryClickListener listener) {
        this(context, categoryList, false, listener);
    }

    public MailER_AutoCategoryAdapter(Context context, JSONArray categoryList, boolean isGrid, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.isGrid = isGrid;
        this.listener = listener;
    }

    public void updateData(JSONArray newCategoryList) {
        this.categoryList = newCategoryList;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int oldPos = selectedPosition;
        selectedPosition = position;
        if (oldPos >= 0 && oldPos < getItemCount()) {
            notifyItemChanged(oldPos);
        }
        if (selectedPosition >= 0 && selectedPosition < getItemCount()) {
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
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
        if (t.contains("more")) return "➕";
        if (t.contains("raksha") || t.contains("rakhi")) return "🪢";
        if (t.contains("thank")) return "🙏";
        if (t.contains("summer")) return "☀️";
        if (t.contains("education") || t.contains("webinar")) return "🎓";
        if (t.contains("sport") || t.contains("contest")) return "🏅";
        if (t.contains("advertising") || t.contains("campaign")) return "📢";
        if (t.contains("wedding") || t.contains("anniversary")) return "💍";
        if (t.contains("photography")) return "📸";
        if (t.contains("motivation")) return "💡";
        if (t.contains("food")) return "🍔";
        if (t.contains("christmas")) return "🎄";
        if (t.contains("days") || t.contains("event")) return "📅";
        if (t.contains("real estate")) return "🏠";
        if (t.contains("beauty") || t.contains("cosmetic") || t.contains("spa")) return "💅";
        if (t.contains("music")) return "🎵";
        if (t.contains("fitness")) return "💪";
        if (t.contains("car")) return "🚗";
        if (t.contains("coffee")) return "☕";
        if (t.contains("job") || t.contains("hiring")) return "💼";
        if (t.contains("medical")) return "⚕️";
        if (t.contains("sale") || t.contains("friday")) return "🛍️";
        if (t.contains("diwali")) return "🪔";
        if (t.contains("dussehra") || t.contains("dasara") || t.contains("dashahara")) return "🏹";
        if (t.contains("navratri") || t.contains("garba") || t.contains("dance")) return "💃";
        if (t.contains("ganesh") || t.contains("vinayak") || t.contains("chaturthi")) return "🐘";
        if (t.contains("krishna") || t.contains("janmashtami") || t.contains("kanha")) return "🪈";
        if (t.contains("ram") || t.contains("hanuman") || t.contains("shiva") || t.contains("mahadev")) return "🚩";
        if (t.contains("eid") || t.contains("ramadan") || t.contains("mubarak")) return "🌙";
        if (t.contains("birthday") || t.contains("bday")) return "🎂";
        if (t.contains("anniversary")) return "💐";
        if (t.contains("republic") || t.contains("independence") || t.contains("tiranga")) return "🇮🇳";
        if (t.contains("new year") || t.contains("year")) return "🎆";
        if (t.contains("art") || t.contains("design") || t.contains("holi")) return "🎨";
        if (t.contains("business") || t.contains("marketing")) return "📈";
        return "✨";
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject cat = categoryList.getJSONObject(position);
            String name = cat.optString("name", cat.optString("cat_name", "Category"));
            String id = cat.optString("id", cat.optString("cat_id", "0"));
            String thumbUrl = cat.optString("image", cat.optString("thumb_img", cat.optString("thumbnail", cat.optString("cat_thumb", ""))));

            boolean isMore = "More".equalsIgnoreCase(name) || "-1".equals(id);

            Log.d("CATEGORY_THUMB_DEBUG", "Category [" + position + "]: " + name + " (ID: " + id + ") | image/thumb_img raw: '" + thumbUrl + "'");

            String originalTitle = name.replace("\n", " ");
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
            holder.tvText.setText(titleCase.toString());

            if (holder.ivThumbnail != null) {
                holder.ivThumbnail.setVisibility(View.GONE);
            }
            if (holder.tvEmoji != null) {
                holder.tvEmoji.setText(getEmojiForCategory(name));
                holder.tvEmoji.setVisibility(View.VISIBLE);
            }

            boolean isSelected = !isMore && (selectedPosition == position);
            holder.llRoot.setSelected(isSelected);

            if (isSelected) {
                holder.tvText.setTextColor(Color.WHITE);
            } else {
                holder.tvText.setTextColor(Color.parseColor("#21005D"));
            }

            holder.itemView.setOnClickListener(v -> {
                if (isMore) {
                    if (listener != null) listener.onCategoryClick(id);
                    return;
                }
                int oldPos = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                if (listener != null) listener.onCategoryClick(id);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.length() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvEmoji;
        ImageView ivThumbnail;
        LinearLayout llRoot;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_tab_text);
            tvEmoji = itemView.findViewById(R.id.tv_tab_emoji);
            ivThumbnail = itemView.findViewById(R.id.iv_tab_thumbnail);
            llRoot = itemView.findViewById(R.id.ll_tab_root);
        }
    }
}