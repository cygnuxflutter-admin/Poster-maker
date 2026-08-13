package com.festival.flyer.postermaker.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MailER_StickerViewPagerAdapter extends FragmentPagerAdapter {
    String[] cateName = new String[]{"Love", "Birthday", "Business", "Number", "Education", "Food", "Vehicle", "Icons", "Sales"};

    public MailER_StickerViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    public Fragment getItem(int position) {
        String categoryName = this.cateName[position];
        MailER_StickersFragment stickersFragment = new MailER_StickersFragment();
        Bundle bundle = new Bundle();
        bundle.putString("categoryName", categoryName);
        stickersFragment.setArguments(bundle);
        return stickersFragment;
    }

    public CharSequence getPageTitle(int position) {
        return this.cateName[position];
    }

    public int getCount() {
        return this.cateName.length;
    }
}
