package com.example.a1201766_1201086_courseproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (NotificationHelper.ACTION_SNOOZE.equals(action)) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            int snoozeDuration = intent.getIntExtra("snoozeDuration", -1);

            if (snoozeDuration <= 0) {
                Log.e("ReminderReceiver", "Invalid snooze duration retrieved. Please check the source.");
                return;
            }

            Log.d("ReminderReceiver", "Snoozing reminder: " + title + " for " + snoozeDuration + " minutes.");
            scheduleSnoozedNotification(context, title, message, snoozeDuration);
        } else {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            Log.d("ReminderReceiver", "Received reminder: " + title + " - " + message);

            int snoozeDuration = intent.getIntExtra("snoozeDuration", -1);
            if (snoozeDuration <= 0) {
                Log.w("ReminderReceiver", "Invalid snoozeDuration. Using default.");
            }
            NotificationHelper.createNotification(context, title, message, snoozeDuration);
        }
    }

    private void scheduleSnoozedNotification(Context context, String title, String message, int snoozeDuration) {
        long snoozeTimeMillis = System.currentTimeMillis() + snoozeDuration * 60 * 1000;

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("message", message);
        snoozeIntent.putExtra("snoozeDuration", snoozeDuration);
        snoozeIntent.setAction(NotificationHelper.ACTION_SNOOZE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);
            Log.d("ReminderReceiver", "Snoozed notification scheduled for: " + snoozeDuration + " minutes later.");
        } else {
            Log.e("ReminderReceiver", "AlarmManager is null. Cannot schedule snoozed notification.");
        }
    }

    public static long parseDateTimeToMillis(String dueDate, String dueTime) {
        try {
            String dateTime = dueDate + " " + dueTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date parsedDate = sdf.parse(dateTime);
            if (parsedDate != null) {
                return parsedDate.getTime();
            } else {
                Log.e("ReminderReceiver", "Parsed date is null.");
                return -1;
            }
        } catch (Exception e) {
            Log.e("ReminderReceiver", "Error parsing date and time: " + e.getMessage());
            return -1;
        }
    }

    public static void cancelNotification(Context context, int taskId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d("ReminderReceiver", "Notification canceled for task ID: " + taskId);
            } else {
                Log.e("ReminderReceiver", "AlarmManager is null. Cannot cancel notification.");
            }
        } else {
            Log.w("ReminderReceiver", "No existing notification found to cancel for task ID: " + taskId);
        }
    }
}
