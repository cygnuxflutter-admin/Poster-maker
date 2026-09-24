package com.festival.flyer.postermaker.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.MailER_BgModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class MailER_CategoryBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.spawner_bottom_sheet_categories, container, false);

        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        
        // Remove the dummy "More" from the end
        ArrayList<MailER_BgModel> realCategories = new ArrayList<>();
        if (MailER_BGSelectionController.allCategoriesData != null) {
            for (MailER_BgModel model : MailER_BGSelectionController.allCategoriesData) {
                if (!"More".equalsIgnoreCase(model.getCategory_name())) {
                    realCategories.add(model);
                }
            }
        }

        CategoryAdapter adapter = new CategoryAdapter(realCategories, position -> {
            if (getActivity() != null) {
                ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
                if (viewPager != null) {
                    viewPager.setCurrentItem(position);
                }
            }
            dismiss();
        });
        rvCategories.setAdapter(adapter);

        return view;
    }

    private static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private final ArrayList<MailER_BgModel> categories;
        private final OnCategoryClickListener listener;

        public CategoryAdapter(ArrayList<MailER_BgModel> categories, OnCategoryClickListener listener) {
            this.categories = categories;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvName.setText(categories.get(position).getCategory_name());
            holder.tvName.setTextColor(android.graphics.Color.parseColor("#1F1735"));
            holder.tvName.setTextSize(14);
            holder.tvName.setGravity(android.view.Gravity.CENTER);
            holder.tvName.setBackgroundResource(R.drawable.spawner_tab_pill_background);
            
            // Adjust margins to look like pills
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 8, 8, 8);
            holder.tvName.setLayoutParams(params);
            holder.tvName.setPadding(16, 24, 16, 24);

            holder.itemView.setOnClickListener(v -> listener.onClick(position));
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    private interface OnCategoryClickListener {
        void onClick(int position);
    }
}
