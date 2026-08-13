package com.festival.flyer.postermaker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.adapter.MailER_StickerAdapter;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;

public class MailER_StickersFragment extends Fragment {

    private GetSnapListener onGetSnap;

    public interface GetSnapListener {
        void onSnapFilter(String str);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.spawner_template_fragment, container, false);
        assert getArguments() != null;
        String catName = getArguments().getString("categoryName");
        this.onGetSnap = (GetSnapListener) getActivity();

        RecyclerView sticker_rv = view.findViewById(R.id.template_rv);

        sticker_rv.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
        sticker_rv.setLayoutManager(layoutManager);

        MailER_StickerAdapter stickerAdapter = new MailER_StickerAdapter(getActivity(), MailER_FileUtils.listAssetFiles(getActivity(), "stickers/" + catName), (path) -> onGetSnap.onSnapFilter(path));
        sticker_rv.setAdapter(stickerAdapter);
        return view;
    }
}
