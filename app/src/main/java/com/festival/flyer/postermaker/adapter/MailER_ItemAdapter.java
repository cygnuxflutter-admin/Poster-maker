package com.festival.flyer.postermaker.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_PosterEditActivity;
import com.festival.flyer.postermaker.components.MailER_AutoResizeTextView;
import com.festival.flyer.postermaker.components.MailER_AutofitTextRel;
import com.festival.flyer.postermaker.components.MailER_ResizableStickerView;
import com.festival.flyer.postermaker.utils.MailER_BitmapUtils;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class MailER_ItemAdapter extends DragItemAdapter<Pair<Long, View>, MailER_ItemAdapter.ViewHolder> {
    private final Activity activity;
    private final boolean mDragOnLongPress;
    private final int mGrabHandleId;
    private final int mLayoutId;

    public MailER_ItemAdapter(Activity activity, ArrayList<Pair<Long, View>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        this.mLayoutId = layoutId;
        this.mGrabHandleId = grabHandleId;
        this.activity = activity;
        this.mDragOnLongPress = dragOnLongPress;
        setItemList(list);
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(this.mLayoutId, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        holder.item_layout.getLayoutParams().width = (int) (displayMetrics.widthPixels / 1.7f);
        holder.item_layout.invalidate();

        holder.underline_view.getLayoutParams().width = (int) (displayMetrics.widthPixels / 1.7f);
        holder.underline_view.invalidate();

        if(activity instanceof MailER_PosterEditActivity){
            holder.itemView.setOnTouchListener((v, event) -> {
                ((MailER_PosterEditActivity) activity).hideEverything();
                return false;
            });
        }

        View v = this.mItemList.get(position).second;
        try {
            if (v instanceof MailER_ResizableStickerView) {
                View cChild = ((MailER_ResizableStickerView) v).getChildAt(1);
                Bitmap thumbBit = Bitmap.createBitmap(cChild.getWidth(), cChild.getHeight(), Config.ARGB_8888);
                cChild.draw(new Canvas(thumbBit));
                float[] f = new float[9];
                ((ImageView) cChild).getImageMatrix().getValues(f);
                float scaleX = f[0];
                float scaleY = f[4];
                Drawable d = ((ImageView) cChild).getDrawable();
                int origW = d.getIntrinsicWidth();
                int origH = d.getIntrinsicHeight();
                int width = Math.round(((float) origW) * scaleX);
                int height = Math.round(((float) origH) * scaleY);
                holder.mImage.setImageBitmap(Bitmap.createBitmap(thumbBit, (thumbBit.getWidth() - width) / 2, (thumbBit.getHeight() - height) / 2, width, height));
                holder.mImage.setRotationY(cChild.getRotationY());
                holder.mImage.setTag(this.mItemList.get(position));
                holder.mImage.setAlpha(1.0f);
                holder.textView.setText(" ");
            }
            if (v instanceof MailER_AutofitTextRel) {
                holder.textView.setText(((MailER_AutoResizeTextView) ((MailER_AutofitTextRel) v).getChildAt(2)).getText());
                holder.textView.setTypeface(((MailER_AutoResizeTextView) ((MailER_AutofitTextRel) v).getChildAt(2)).getTypeface());
                holder.textView.setTextSize(400.0f);
                holder.textView.setGravity(17);
                holder.textView.setMinTextSize(10.0f);
                if (((MailER_AutofitTextRel) v).getTextInfo().getBG_COLOR() != 0) {
                    Bitmap bmp = Bitmap.createBitmap(150, 150, Config.ARGB_8888);
                    new Canvas(bmp).drawColor(((MailER_AutofitTextRel) v).getTextInfo().getBG_COLOR());
                    holder.mImage.setImageBitmap(bmp);
                    holder.mImage.setAlpha(((float) ((MailER_AutofitTextRel) v).getTextInfo().getBG_ALPHA()) / 255.0f);
                } else if (((MailER_AutofitTextRel) v).getTextInfo().getBG_DRAWABLE().equals("0")) {
                    holder.mImage.setAlpha(1.0f);
                    holder.mImage.setImageResource(R.drawable.spawner_trans);
                } else {
                    holder.mImage.setImageBitmap(MailER_BitmapUtils.getTiledBitmap(this.activity, this.activity.getResources().getIdentifier(((MailER_AutofitTextRel) v).getTextInfo().getBG_DRAWABLE(), "drawable", this.activity.getPackageName()), 150, 150));
                    holder.mImage.setAlpha(((float) ((MailER_AutofitTextRel) v).getTextInfo().getBG_ALPHA()) / 255.0f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (v instanceof MailER_ResizableStickerView) {
            if (((MailER_ResizableStickerView) v).isMultiTouchEnabled) {
                holder.img_lock.setImageResource(R.drawable.spawner_ic_unlock);
            } else {
                holder.img_lock.setImageResource(R.drawable.spawner_ic_lock);
            }
        }
        if (v instanceof MailER_AutofitTextRel) {
            if (((MailER_AutofitTextRel) v).isMultiTouchEnabled) {
                holder.img_lock.setImageResource(R.drawable.spawner_ic_unlock);
            } else {
                holder.img_lock.setImageResource(R.drawable.spawner_ic_lock);
            }
        }
        final View view = v;
        final ViewHolder viewHolder = holder;
        holder.img_lock.setOnClickListener(v1 -> {
            if (view instanceof MailER_ResizableStickerView) {
                if (((MailER_ResizableStickerView) view).isMultiTouchEnabled) {
                    ((MailER_ResizableStickerView) view).isMultiTouchEnabled = ((MailER_ResizableStickerView) view).setDefaultTouchListener(false);
                    viewHolder.img_lock.setImageResource(R.drawable.spawner_ic_lock);
                } else {
                    ((MailER_ResizableStickerView) view).isMultiTouchEnabled = ((MailER_ResizableStickerView) view).setDefaultTouchListener(true);
                    viewHolder.img_lock.setImageResource(R.drawable.spawner_ic_unlock);
                }
            }
            if (!(view instanceof MailER_AutofitTextRel)) {
                return;
            }
            if (((MailER_AutofitTextRel) view).isMultiTouchEnabled) {
                ((MailER_AutofitTextRel) view).isMultiTouchEnabled = ((MailER_AutofitTextRel) view).setDefaultTouchListener(false);
                viewHolder.img_lock.setImageResource(R.drawable.spawner_ic_lock);
                return;
            }
            ((MailER_AutofitTextRel) view).isMultiTouchEnabled = ((MailER_AutofitTextRel) view).setDefaultTouchListener(true);
            viewHolder.img_lock.setImageResource(R.drawable.spawner_ic_unlock);
        });
    }

    public long getUniqueItemId(int position) {
        return this.mItemList.get(position).first;
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        ImageView img_lock;
        ImageView mImage;
//        TextView mText;
        MailER_AutoResizeTextView textView;
        LinearLayout item_layout;
        View underline_view;

        ViewHolder(View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
//            this.mText = itemView.findViewById(R.id.text);
            this.mImage = itemView.findViewById(R.id.image1);
            this.img_lock = itemView.findViewById(R.id.img_lock);
            this.textView = itemView.findViewById(R.id.auto_fit_edit_text);
            this.item_layout = itemView.findViewById(R.id.item_layout);
            this.underline_view = itemView.findViewById(R.id.underline_view);
        }

        public void onItemClicked(View view) {
        }

        public boolean onItemLongClicked(View view) {
            return true;
        }
    }
}
