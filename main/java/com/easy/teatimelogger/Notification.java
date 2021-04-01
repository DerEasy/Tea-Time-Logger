package com.easy.teatimelogger;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobScheduler;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Notification extends Application {
    public static final String CHANNEL_REMINDER = "reminder";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminder = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminder.setDescription("Reminds the user to do a Tea Session");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(reminder);
        }
    }
}