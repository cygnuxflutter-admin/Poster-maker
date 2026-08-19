package com.festival.flyer.postermaker.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.BuildConfig;
import com.festival.flyer.postermaker.MyApplication;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

public class MailER_MyCreationAdapter extends RecyclerView.Adapter<MailER_MyCreationAdapter.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<String> itemList = new ArrayList<>();
    private final int cellWidth, cellHeight;
    private final MyCreationListener myCreationListener;

    public MailER_MyCreationAdapter(Activity activity, ArrayList<String> itemList, int cellWidth, int cellHeight, MyCreationListener myCreationListener) {
        super();
        for (String file : itemList) {
            add(file);
        }
        this.activity = activity;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.myCreationListener = myCreationListener;
    }

    private void add(String path) {
        itemList.add(path);
    }

    public int getSize() {
        return itemList.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spawner_design_rv, viewGroup, false);
        return new MyViewHolder(view);
    }

    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.imageView.getLayoutParams().width = cellWidth;
        holder.imageView.getLayoutParams().height = cellHeight;
        holder.imageView.invalidate();

        holder.btn_share.setVisibility(View.VISIBLE);

        loadImage(holder.imageView, itemList.get(position));

        Glide.with(this.activity)
                .load(itemList.get(position))
                .thumbnail(0.1f).dontAnimate()
                .placeholder(R.drawable.spawner_no_image)
                .error(R.drawable.spawner_no_image)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            MyApplication.showInterstitialAd(activity, () -> startActivity(position));
//            File file = new File(itemList.get(position));
//            if (file.exists()) {
//                fullscreenDialog(itemList.get(position));
//            } else {
//                Toast.makeText(activity, "Something went wrong!!", Toast.LENGTH_SHORT).show();
//            }
        });

        holder.btn_del.setOnClickListener(view -> MailER_MaterialDialogUtils.getInstance().DeleteDialog(activity, dialog -> {

            File file = new File(itemList.get(position));

            if (file.exists()) {
                boolean delete = file.delete();
                try {
                    activity.getContentResolver().delete(
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            android.provider.MediaStore.Images.Media.DATA + "=?",
                            new String[]{file.getAbsolutePath()}
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (delete || !file.exists()) {
                    itemList.remove(position);
                    notifyDataSetChanged();
                    if (itemList.size() == 0) {
                        myCreationListener.onEmptyAdapter();
                    }
                } else {
                    android.widget.Toast.makeText(activity, "Failed to delete file", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }));

        holder.btn_share.setOnClickListener(v -> {
            String shareBody = "I'm using amazing Poster maker App, I just created a new poster within 2 min and it's completely free. I recommend you to try this app. \n\n" +
                    "https://play.google.com/store/apps/details?id=" + activity.getApplicationContext().getPackageName();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", new File(itemList.get(position))));
            activity.startActivity(Intent.createChooser(shareIntent, "Share Image using"));
        });
    }
    public void startActivity(int position) {
        File file = new File(itemList.get(position));
        if (file.exists()) {
            fullscreenDialog(itemList.get(position));
        } else {
            Toast.makeText(activity, "Something went wrong!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadImage(final ImageView imageView, String path) {
        Glide.with(activity)
                .load(path)
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .animate(R.anim.abc_fade_in)
                .skipMemoryCache(false)
                .centerCrop()
                .into(imageView);
    }

    private void fullscreenDialog(String position) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.spawner_layout_fullscreen_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        ImageView imgAvatar = dialog.findViewById(R.id.imageView);
        Glide.with(activity).load(position).into(imgAvatar);
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public interface MyCreationListener {
        void onEmptyAdapter();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageView btn_del, btn_share;

        public MyViewHolder(View v) {
            super(v);
            imageView = itemView.findViewById(R.id.kenBurnsView);
            btn_del = itemView.findViewById(R.id.btn_del);
            btn_share = itemView.findViewById(R.id.btn_share);
        }
    }
}
