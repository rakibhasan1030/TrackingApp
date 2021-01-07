package com.rakibhasan.trackingapp.batchLocation;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.rakibhasan.trackingapp.constants.Utils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Utils.CHANNEL_ID,
                    "LocationChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
