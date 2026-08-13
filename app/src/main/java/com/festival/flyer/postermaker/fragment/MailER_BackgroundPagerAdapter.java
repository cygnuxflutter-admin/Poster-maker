package com.festival.flyer.postermaker.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.festival.flyer.postermaker.model.MailER_BgModel;

import java.util.ArrayList;

public class MailER_BackgroundPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<MailER_BgModel> posterDataLists;

    public MailER_BackgroundPagerAdapter(FragmentManager fm, ArrayList<MailER_BgModel> posterDataLists) {
        super(fm);
        this.posterDataLists = posterDataLists;
    }

    @NonNull
    public Fragment getItem(int position) {
        MailER_BackgroundFragment.TempisFirstShow = false;
        MailER_BackgroundFragment backgroundFragment = new MailER_BackgroundFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("posterDataLists", posterDataLists.get(position).getCategory_list());
        backgroundFragment.setArguments(bundle);
        return backgroundFragment;
    }

    public CharSequence getPageTitle(int position) {
        return this.posterDataLists.get(position).getCategory_name();
    }

    public int getCount() {
        return this.posterDataLists.size();
    }
}
