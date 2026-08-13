package com.festival.flyer.postermaker.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_PosterEditActivity;
import com.festival.flyer.postermaker.adapter.MailER_ItemAdapter;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.DragListView.DragListListenerAdapter;

import java.util.ArrayList;

public class MailER_ListFragment extends Fragment {
    private RelativeLayout lay_Notext;
    private DragListView mDragListView;
    private final ArrayList<Pair<Long, View>> mItemArray = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.spawner_list_layout, container, false);
        this.mDragListView = view.findViewById(R.id.drag_list_view);
        this.lay_Notext = view.findViewById(R.id.lay_text);
        this.mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        this.mDragListView.setDragListListener(new DragListListenerAdapter() {
            public void onItemDragStarted(int position) {
            }

            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    for (int i = mItemArray.size() - 1; i >= 0; i--) {
                        mItemArray.get(i).second.bringToFront();
                    }

                    MailER_PosterEditActivity.txt_stkr_rel.requestLayout();
                    MailER_PosterEditActivity.txt_stkr_rel.postInvalidate();

                }
            }
        });

        return view;
    }

    public void getLayoutChild() {
        this.mItemArray.clear();

        if (MailER_PosterEditActivity.txt_stkr_rel.getChildCount() != 0) {
            this.lay_Notext.setVisibility(View.GONE);
            for (int i = MailER_PosterEditActivity.txt_stkr_rel.getChildCount() - 1; i >= 0; i--) {
                this.mItemArray.add(new Pair<>((long) i, MailER_PosterEditActivity.txt_stkr_rel.getChildAt(i)));
            }
        } else {
            this.lay_Notext.setVisibility(View.VISIBLE);
        }


        setupListRecyclerView();
    }

    private void setupListRecyclerView() {
        this.mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mDragListView.setAdapter(new MailER_ItemAdapter(getActivity(), this.mItemArray, R.layout.spawner_list_item, R.id.layeradjust, false), true);
        this.mDragListView.setCanDragHorizontally(false);
        this.mDragListView.setCustomDragItem(new MyDragItem(getContext(), R.layout.spawner_list_item));
    }

    private static class MyDragItem extends DragItem {
        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        public void onBindDragView(View clickedView, View dragView) {
            Bitmap thumbBit1 = Bitmap.createBitmap(clickedView.getWidth(), clickedView.getHeight(), Config.ARGB_8888);
            clickedView.draw(new Canvas(thumbBit1));
            ((ImageView) dragView.findViewById(R.id.backimg)).setImageBitmap(thumbBit1);
        }
    }
}
