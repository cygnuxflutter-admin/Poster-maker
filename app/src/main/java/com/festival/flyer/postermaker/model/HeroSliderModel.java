package com.festival.flyer.postermaker.model;

public class HeroSliderModel {
    private String title;
    private String description;
    private int backgroundDrawable;
    private int illustrationDrawable;
    private String buttonText;

    public HeroSliderModel(String title, String description, String buttonText, int backgroundDrawable, int illustrationDrawable) {
        this.title = title;
        this.description = description;
        this.buttonText = buttonText;
        this.backgroundDrawable = backgroundDrawable;
        this.illustrationDrawable = illustrationDrawable;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getButtonText() {
        return buttonText;
    }

    public int getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public int getIllustrationDrawable() {
        return illustrationDrawable;
    }
}
