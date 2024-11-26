package com.example.firstapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "task_due_channel";
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    // Create a notification channel (required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Due Notifications";
            String description = "Channel for task due notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Cancel a notification
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);  // Cancel notification ID 1
        }
    }

    // Show a notification
    public void showNotification(String taskTitle) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)  // Use a custom icon if possible
                .setContentTitle(taskTitle + " is due today!")
                .setContentText("Reminder: Don't forget to complete your task.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);  // Notification ID 1
        }
    }
}
