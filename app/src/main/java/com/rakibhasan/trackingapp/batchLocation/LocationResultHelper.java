package com.rakibhasan.trackingapp.batchLocation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.rakibhasan.trackingapp.MainActivity;
import com.rakibhasan.trackingapp.R;
import com.rakibhasan.trackingapp.constants.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LocationResultHelper {

    Context mContext;
    List<Location> mLocationList;

    public LocationResultHelper(Context mContext, List<Location> mLocationList) {
        this.mContext = mContext;
        this.mLocationList = mLocationList;
    }

    public static boolean getLocationRequestStatus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Utils.KEY_LOCATION_REQUEST, false);
    }

    public static void setLocationRequestStatus(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Utils.KEY_LOCATION_REQUEST, value)
                .apply();
    }

    //format and return the batch locations
    public String getLocationResultText(){
        if (mLocationList.isEmpty()){
            return "Location not received!";
        }else {
            StringBuilder builder = new StringBuilder();
            for (Location location : mLocationList){
                builder.append("(");
                builder.append(location.getLatitude());
                builder.append(",");
                builder.append(location.getLongitude());
                builder.append(")");
                builder.append("\n");
            }
            return builder.toString();
        }

    }

    private CharSequence getLocationResultTitle() {
        String result = mContext.getResources().getQuantityString(
                R.plurals.num_location_reported,
                mLocationList.size(),
                mLocationList.size());
        return result + " : " + DateFormat.getDateTimeInstance().format(new Date());
    }

    public void showNotification(){
        Intent notificationIntent = new Intent(mContext, BatchLocationActivity.class);

        //Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);

        //Push the content intent into the stack
        stackBuilder.addNextIntent(notificationIntent);

        //Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = null;
        notificationBuilder = new NotificationCompat.Builder(mContext,
                Utils.CHANNEL_ID)
                .setContentTitle(getLocationResultTitle())
                .setContentText(getLocationResultText())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(notificationPendingIntent);
        getNotificationManager().notify(0, notificationBuilder.build());

    }

    private NotificationManager getNotificationManager() {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    public void saveLocationResults(){
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(Utils.KEY_LOCATION_RESULTS, getLocationResultTitle()+ "\n"+
                        getLocationResultText())
                .apply();
    }

    public static String getSavedLocationResults(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Utils.KEY_LOCATION_RESULTS, "Default Value");
    }
}
