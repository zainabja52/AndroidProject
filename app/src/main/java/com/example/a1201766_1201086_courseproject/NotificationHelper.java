package com.example.a1201766_1201086_courseproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "TASK_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "Task Reminder Notifications";
    public static final String ACTION_SNOOZE = "com.example.a1201766_1201086_courseproject.ACTION_SNOOZE";

    public static void createNotification(Context context, String title, String message, int snoozeDuration) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        int uniqueRequestCode = (int) System.currentTimeMillis();

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("message", message);
        snoozeIntent.putExtra("snoozeDuration", snoozeDuration);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueRequestCode,
                snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_revert, "Snooze", snoozePendingIntent);

        // Show the notification
        notificationManager.notify(uniqueRequestCode, builder.build());
    }
}
