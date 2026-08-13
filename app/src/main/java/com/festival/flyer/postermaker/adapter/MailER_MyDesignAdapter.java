package com.festival.flyer.postermaker.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.components.MailER_StickerInfo;
import com.festival.flyer.postermaker.components.MailER_TemplateInfo;
import com.festival.flyer.postermaker.utils.MailER_DatabaseHandler;
import com.festival.flyer.postermaker.utils.MailER_FileUtils;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;

import java.io.File;
import java.util.ArrayList;

import static com.festival.flyer.postermaker.utils.MailER_FileUtils.deleteRecursive;

public class MailER_MyDesignAdapter extends RecyclerView.Adapter<MailER_MyDesignAdapter.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<MailER_TemplateInfo> templateInfoArrayList;
    private final int cellWidth, cellHeight;
    private final MyDesignClickListener designClickListener;
    private final ArrayList<File> fileArrayList = new ArrayList<>();

    public MailER_MyDesignAdapter(Activity activity, ArrayList<MailER_TemplateInfo> templateInfoArrayList, int cellWidth, int cellHeight, MyDesignClickListener designClickListener) {
        this.activity = activity;
        this.templateInfoArrayList = templateInfoArrayList;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.designClickListener = designClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_design_rv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.imageView.getLayoutParams().width = cellWidth;
        holder.imageView.getLayoutParams().height = cellHeight;
        holder.imageView.invalidate();

        Glide.with(this.activity)
                .load(templateInfoArrayList.get(position).getTHUMB_URI())
                .thumbnail(0.1f).dontAnimate()
                .placeholder(R.drawable.spawner_no_image)
                .error(R.drawable.spawner_no_image)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(view -> {
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(activity);
            ArrayList<MailER_TemplateInfo> templateList = dh.getTemplateListDes("USER");
            ArrayList<MailER_StickerInfo> stickerInfoList = dh.getComponentInfoList(templateList.get(position).getTEMPLATE_ID(), "STICKER");
            fileArrayList.clear();
            fileArrayList.add(new File(templateInfoArrayList.get(position).getTHUMB_URI()));
            fileArrayList.add(new File(templateInfoArrayList.get(position).getFRAME_NAME()));
            for (int i = 0; i < stickerInfoList.size(); i++) {
                fileArrayList.add(new File(stickerInfoList.get(i).getSTKR_PATH()));
            }

            if (!MailER_FileUtils.checkFileExistOrNot(fileArrayList)) {
                Toast.makeText(activity, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
                return;
            }

            designClickListener.onPostClick(templateInfoArrayList.get(position).getTEMPLATE_ID(), position);

        });
        holder.btn_del.setOnClickListener(view -> MailER_MaterialDialogUtils.getInstance().DeleteDialog(activity, dialog -> {
            File folder = MailER_FileUtils.getSaveFileLocation(activity, "MyDesigns/" + templateInfoArrayList.get(position).getTEMP_PATH());
            File folder2 = MailER_FileUtils.getSaveFileLocation(activity, "category1/" + templateInfoArrayList.get(position).getTEMP_PATH());
            if (folder.exists()) {
                deleteRecursive(folder);
            }
            if (folder2.exists()) {
                deleteRecursive(folder2);
            }
            MailER_DatabaseHandler dh = MailER_DatabaseHandler.getDbHandler(activity);
            dh.deleteTemplateInfo(templateInfoArrayList.get(position).getTEMPLATE_ID());
            templateInfoArrayList.remove(position);
            notifyDataSetChanged();
            if (templateInfoArrayList.size() == 0) {
                designClickListener.onEmptyAdapter();
            }

            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        }));
    }

    @Override
    public int getItemCount() {
        return templateInfoArrayList.size();
    }

    public interface MyDesignClickListener {
        void onPostClick(int post_id, int position);

        void onEmptyAdapter();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageView btn_del;

        public MyViewHolder(View v) {
            super(v);
            imageView = itemView.findViewById(R.id.kenBurnsView);
            btn_del = itemView.findViewById(R.id.btn_del);
        }
    }
}
