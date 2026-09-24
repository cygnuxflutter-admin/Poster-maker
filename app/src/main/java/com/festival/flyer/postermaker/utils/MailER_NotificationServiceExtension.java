package com.festival.flyer.postermaker.utils;

import android.content.Context;

import com.festival.flyer.postermaker.models.MailER_NotificationModel;
import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;
import com.onesignal.notifications.IDisplayableMutableNotification;

public class MailER_NotificationServiceExtension implements INotificationServiceExtension {

    @Override
    public void onNotificationReceived(INotificationReceivedEvent event) {
        IDisplayableMutableNotification notification = event.getNotification();
        Context context = event.getContext();

        if (notification != null) {
            String title = notification.getTitle() != null ? notification.getTitle() : "Poster Maker";
            String body = notification.getBody() != null ? notification.getBody() : "";
            String id = notification.getNotificationId();
            String imageUrl = notification.getBigPicture();

            if ((imageUrl == null || imageUrl.isEmpty()) && notification.getAdditionalData() != null) {
                if (notification.getAdditionalData().has("image_url")) {
                    imageUrl = notification.getAdditionalData().optString("image_url");
                } else if (notification.getAdditionalData().has("image")) {
                    imageUrl = notification.getAdditionalData().optString("image");
                } else if (notification.getAdditionalData().has("big_picture")) {
                    imageUrl = notification.getAdditionalData().optString("big_picture");
                }
            }

            MailER_NotificationModel model = new MailER_NotificationModel(
                    id,
                    title,
                    body,
                    imageUrl,
                    System.currentTimeMillis()
            );

            MailER_NotificationHelper.saveNotification(context, model);
        }
    }
}
