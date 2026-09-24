package com.festival.flyer.postermaker.models;

public class MailER_NotificationModel {
    private String id;
    private String title;
    private String message;
    private String imageUrl;
    private long timestamp;
    private boolean isRead;

    public MailER_NotificationModel(String id, String title, String message, String imageUrl, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public MailER_NotificationModel(String id, String title, String message, long timestamp) {
        this(id, title, message, null, timestamp);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
