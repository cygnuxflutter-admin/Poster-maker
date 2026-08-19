package com.festival.flyer.postermaker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.model.HeroSliderModel;

import java.util.List;

public class HeroSliderAdapter extends RecyclerView.Adapter<HeroSliderAdapter.SliderViewHolder> {

    public interface OnHeroClickListener {
        void onHeroClick(HeroSliderModel item);
    }

    private List<HeroSliderModel> sliderItems;
    private OnHeroClickListener listener;

    public HeroSliderAdapter(List<HeroSliderModel> sliderItems, OnHeroClickListener listener) {
        this.sliderItems = sliderItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spawner_item_hero_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        HeroSliderModel item = sliderItems.get(position);
        
        holder.heroTitle.setText(item.getTitle());
        holder.heroDesc.setText(item.getDescription());
        holder.heroIllustration.setImageResource(item.getIllustrationDrawable());
        holder.heroBanner.setBackgroundResource(item.getBackgroundDrawable());

        holder.heroBanner.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHeroClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderItems != null ? sliderItems.size() : 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout heroBanner;
        ImageView heroIllustration;
        TextView heroTitle, heroDesc;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            heroBanner = itemView.findViewById(R.id.hero_banner);
            heroIllustration = itemView.findViewById(R.id.hero_illustration);
            heroTitle = itemView.findViewById(R.id.hero_title);
            heroDesc = itemView.findViewById(R.id.hero_desc);
        }
    }
}
