package com.rakibhasan.trackingapp.services;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationResult;
import com.rakibhasan.trackingapp.R;
import com.rakibhasan.trackingapp.batchLocation.LocationResultHelper;
import com.rakibhasan.trackingapp.constants.Utils;

import java.util.List;
import java.util.logging.LogRecord;

public class BackgroundLocationIntentService extends IntentService {

    public BackgroundLocationIntentService() {
        super("BackgroundLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("TAG", "onHandleIntent : Called");
        startForeground(1001, getNotification());
        if (intent!=null){
            if (Utils.ACTION_PROCESS_UPDATES.equals(intent.getAction())){
                LocationResult locationResult = LocationResult.extractResult(intent);
                if (locationResult!=null){
                    List<Location> locations = locationResult.getLocations();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(getApplicationContext(), locations);
                    locationResultHelper.saveLocationResults();
                    locationResultHelper.showNotification();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Location Received : "+ locations.size() + "  "+"OK!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }

    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = null;
        builder = new NotificationCompat.Builder(getApplicationContext(), Utils.CHANNEL_ID)
                .setContentTitle("Location Notification")
                .setContentText("Location service is running in the background.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(false);
        Log.d("TAG", "onDestroy : Called");
    }
}