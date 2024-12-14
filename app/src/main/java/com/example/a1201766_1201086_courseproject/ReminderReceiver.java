package com.example.a1201766_1201086_courseproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        Log.d("ReminderReceiver", "Received reminder: " + title);


        // Show the notification
        NotificationHelper.createNotification(context, title, message);
    }
}
