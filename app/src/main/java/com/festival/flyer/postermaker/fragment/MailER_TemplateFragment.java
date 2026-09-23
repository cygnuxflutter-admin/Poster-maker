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

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_PosterGroupChildAdapter;
import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import java.util.ArrayList;
import java.util.Objects;

public class MailER_TemplateFragment extends Fragment {

    private RecyclerView template_rv;
    private int catId;
    private GetPosterListener getPosterListener;
    private ArrayList<MailER_PosterImage> posterThumbFulls;
    private ArrayList<MailER_PosterImage> tempposterThumbFulls = new ArrayList<MailER_PosterImage>();
    private MailER_PreferenceClass preferenceClass;
    public interface GetPosterListener {
        void onPosterClick(int cat_id, int post_id, boolean premium);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.spawner_template_fragment, container, false);
        assert getArguments() != null;
        this.catId = getArguments().getInt("catId");
        //noinspection unchecked
        preferenceClass = new MailER_PreferenceClass(getActivity());
        posterThumbFulls = (ArrayList<MailER_PosterImage>) getArguments().getSerializable("posterDataLists");
        this.getPosterListener = (GetPosterListener) getActivity();
        template_rv = view.findViewById(R.id.template_rv);
        template_rv.setAdapter(null);
        setPosterAdapter();
        return view;
    }
    public static Boolean TempisFirstShow = false;
    public void setPosterAdapter() {
        template_rv.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false);

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

        for (int j = 0; j < posterThumbFulls.size(); j++) {
            if (j != 0) {
                itemsSinceLastAd++;
                if (!TempisFirstShow) {
                    if (itemsSinceLastAd >= firstAdCount) {
                        tempposterThumbFulls.add(null);
                        TempisFirstShow = true;
                        itemsSinceLastAd = 0;
                    }
                } else {
                    if (itemsSinceLastAd >= repeatAdCount) {
                        tempposterThumbFulls.add(null);
                        itemsSinceLastAd = 0;
                    }
                }
            }
            tempposterThumbFulls.add(posterThumbFulls.get(j));
        }

        MailER_PosterGroupChildAdapter posterGroupChildAdapter = new MailER_PosterGroupChildAdapter(getActivity(), tempposterThumbFulls, catId, cellWidth, cellHeight, (cat_id, post_id, premium) -> getPosterListener.onPosterClick(cat_id, post_id, premium));
        template_rv.setAdapter(posterGroupChildAdapter);
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
