package com.example.firstapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * A utility class to handle the creation and display of notifications in the application.
 * This class is responsible for managing notification channels, displaying notifications,
 * and canceling them when necessary.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "task_due_channel"; // Unique ID for the notification channel
    private Context context;

    /**
     * Constructs a NotificationHelper instance and creates the required notification channel.
     *
     * @param context The application context used for managing notifications.
     */
    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates a notification channel for task due notifications.
     * Notification channels are required for Android 8.0 (API level 26) and above.
     */
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

    /**
     * Cancels a notification with a fixed ID (1 in this implementation).
     * Useful for dismissing task reminders when they are no longer relevant.
     */
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
    }

    /**
     * Displays a notification to remind the user of a task that is due today.
     *
     * @param taskTitle The title of the task to be displayed in the notification.
     */
    public void showNotification(String taskTitle) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle(taskTitle + " is due today!")
                .setContentText("Reminder: Don't forget to complete your task.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }
}
