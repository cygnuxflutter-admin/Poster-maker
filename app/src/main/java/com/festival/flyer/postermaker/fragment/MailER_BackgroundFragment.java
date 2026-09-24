package com.festival.flyer.postermaker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_BackgroundChildAdapter;
import com.festival.flyer.postermaker.model.MailER_BgImage;
import com.festival.flyer.postermaker.threadTask.MailER_GetBgData;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import java.util.ArrayList;
import java.util.Objects;

public class MailER_BackgroundFragment extends Fragment {

    private RecyclerView template_rv;
    private GetPosterListener getPosterListener;
    private ArrayList<MailER_BgImage> bgImages = new ArrayList<>();
    private ArrayList<MailER_BgImage> tempbgImages = new ArrayList<MailER_BgImage>();
    private MailER_PreferenceClass preferenceClass;
    public interface GetPosterListener {
        void onPosterClick(String path, boolean premium);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.spawner_template_fragment, container, false);
        assert getArguments() != null;
        //noinspection unchecked
        preferenceClass = new MailER_PreferenceClass(getActivity());
        bgImages = (ArrayList<MailER_BgImage>) getArguments().getSerializable("posterDataLists");
        this.getPosterListener = (GetPosterListener) getActivity();
        template_rv = view.findViewById(R.id.template_rv);
        setPosterAdapter();
        return view;
    }

    public static Boolean TempisFirstShow = false;

    public void setPosterAdapter() {
        template_rv.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false);
        template_rv.setLayoutManager(layoutManager);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float screenDensity = getResources().getDisplayMetrics().density;
        int screenWidth = (displayMetrics).widthPixels;
        int cellWidth = (int) (screenWidth - (42 * screenDensity)) / 2;
        int cellHeight = (700 * cellWidth) / 507;

        int firstAdCount = preferenceClass.getInt("First_rv_count", 3);
        int repeatAdCount = preferenceClass.getInt("rv_count", 10);
        int itemsSinceLastAd = 0;

        for (int j = 0; j < bgImages.size(); j++) {
            if (j != 0) {
                itemsSinceLastAd++;
                if (!TempisFirstShow) {
                    if (itemsSinceLastAd >= firstAdCount) {
                        tempbgImages.add(null);
                        TempisFirstShow = true;
                        itemsSinceLastAd = 0;
                    }
                } else {
                    if (itemsSinceLastAd >= repeatAdCount) {
                        tempbgImages.add(null);
                        itemsSinceLastAd = 0;
                    }
                }
            }
            tempbgImages.add(bgImages.get(j));
        }
        MailER_BackgroundChildAdapter backgroundChildAdapter = new MailER_BackgroundChildAdapter(getActivity(), tempbgImages, cellWidth, cellHeight, (path, premium) -> getPosterListener.onPosterClick(path, premium));
        template_rv.setAdapter(backgroundChildAdapter);
        template_rv.invalidate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
