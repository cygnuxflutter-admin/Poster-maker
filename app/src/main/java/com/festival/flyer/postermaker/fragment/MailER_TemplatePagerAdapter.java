package com.festival.flyer.postermaker.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.festival.flyer.postermaker.model.MailER_PosterModel;

import java.util.ArrayList;

public class MailER_TemplatePagerAdapter extends FragmentStatePagerAdapter {

    private final ArrayList<MailER_PosterModel> posterModels;

    public MailER_TemplatePagerAdapter(@NonNull FragmentManager fm, ArrayList<MailER_PosterModel> posterModels) {
        super(fm);
        this.posterModels = posterModels;
    }

    @NonNull
    public Fragment getItem(int position) {
        MailER_TemplateFragment.TempisFirstShow = false;
        int catId = posterModels.get(position).getCat_id();
        MailER_TemplateFragment templateFragment = new MailER_TemplateFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("catId", catId);
        bundle.putSerializable("posterDataLists", posterModels.get(position).getPoster_list());
        templateFragment.setArguments(bundle);
        return templateFragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public CharSequence getPageTitle(int position) {
        return this.posterModels.get(position).getCat_name();
    }

    public int getCount() {
        return this.posterModels.size();
    }

}
