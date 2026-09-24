package com.festival.flyer.postermaker.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.MailER_BgModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class MailER_BgCategoryBottomSheet extends BottomSheetDialogFragment {

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

        com.festival.flyer.postermaker.adapter.MailER_BgCategoryTabAdapter adapter = new com.festival.flyer.postermaker.adapter.MailER_BgCategoryTabAdapter(getContext(), realCategories, true, (position, model) -> {
            if (getActivity() != null) {
                ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
                if (viewPager != null) {
                    // Find actual index from allCategoriesData
                    int actualIndex = -1;
                    if (MailER_BGSelectionController.allCategoriesData != null) {
                        actualIndex = MailER_BGSelectionController.allCategoriesData.indexOf(model);
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
                if (MailER_BGSelectionController.allCategoriesData != null && currentVPIndex >= 0 && currentVPIndex < MailER_BGSelectionController.allCategoriesData.size()) {
                    MailER_BgModel selectedModel = MailER_BGSelectionController.allCategoriesData.get(currentVPIndex);
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
