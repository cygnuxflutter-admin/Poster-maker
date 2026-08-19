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
import com.festival.flyer.postermaker.model.MailER_PosterModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class MailER_TemplateCategoryBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.spawner_bottom_sheet_categories, container, false);

        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        
        // Remove the dummy "More" from the end
        ArrayList<MailER_PosterModel> realCategories = new ArrayList<>();
        if (MailER_TemplateSelectionController.allCategoriesData != null) {
            for (MailER_PosterModel model : MailER_TemplateSelectionController.allCategoriesData) {
                if (!"More".equalsIgnoreCase(model.getCat_name())) {
                    realCategories.add(model);
                }
            }
        }

        com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter adapter = new com.festival.flyer.postermaker.adapter.MailER_CategoryTabAdapter(getContext(), realCategories, true, (position, model) -> {
            if (getActivity() != null) {
                ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
                if (viewPager != null) {
                    // Find actual index from allCategoriesData
                    int actualIndex = -1;
                    if (MailER_TemplateSelectionController.allCategoriesData != null) {
                        actualIndex = MailER_TemplateSelectionController.allCategoriesData.indexOf(model);
                    }
                    if (actualIndex != -1) {
                        viewPager.setCurrentItem(actualIndex);
                    } else {
                        viewPager.setCurrentItem(position);
                    }
                }
            }
            dismiss();
        });
        
        // Find which position corresponds to the current ViewPager item so we can highlight it
        if (getActivity() != null) {
            ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
            if (viewPager != null) {
                int currentVPIndex = viewPager.getCurrentItem();
                if (MailER_TemplateSelectionController.allCategoriesData != null && currentVPIndex >= 0 && currentVPIndex < MailER_TemplateSelectionController.allCategoriesData.size()) {
                    MailER_PosterModel selectedModel = MailER_TemplateSelectionController.allCategoriesData.get(currentVPIndex);
                    int mappedIndex = realCategories.indexOf(selectedModel);
                    if (mappedIndex != -1) {
                        adapter.setSelectedPosition(mappedIndex);
                    }
                }
            }
        }

        rvCategories.setAdapter(adapter);

        return view;
    }
}
