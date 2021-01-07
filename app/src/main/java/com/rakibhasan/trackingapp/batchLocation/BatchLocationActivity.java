package com.rakibhasan.trackingapp.batchLocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.rakibhasan.trackingapp.R;
import com.rakibhasan.trackingapp.constants.Utils;
import com.rakibhasan.trackingapp.services.BackgroundLocationIntentService;

import java.util.List;

public class BatchLocationActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    TextView locationTV;
    Button locationRequestBtn, startLocationUpdateServiceBtn, stopLocationUpdateServiceBtn;
    private FusedLocationProviderClient mLocationProviderClient;
    LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_location);

        locationTV = findViewById(R.id.location_tv);
        locationRequestBtn = findViewById(R.id.location_request_btn);
        startLocationUpdateServiceBtn = findViewById(R.id.start_location_update_service_btn);
        stopLocationUpdateServiceBtn = findViewById(R.id.stop_location_update_service_btn);

        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //execute in background
                if (locationResult == null) {
                    Toast.makeText(BatchLocationActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Location> locations = locationResult.getLocations();
                LocationResultHelper helper = new LocationResultHelper(BatchLocationActivity.this, locations);
                helper.showNotification();
                helper.saveLocationResults();
                locationTV.setText(helper.getLocationResultText());
            }
        };
        locationRequestBtn.setOnClickListener(this::requestBatchLocationUpdates);
        startLocationUpdateServiceBtn.setOnClickListener(this::startLocationService);
        stopLocationUpdateServiceBtn.setOnClickListener(this::stopLocationService);

    }

    private void requestBatchLocationUpdates(View view) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Utils.LOCATION_SET_INTERVAL * Utils.LOCATION_BASE_INTERVAL);
        locationRequest.setFastestInterval(Utils.LOCATION_SET_FASTEST_INTERVAL * Utils.LOCATION_BASE_INTERVAL);
        locationRequest.setMaxWaitTime(Utils.BATCH_LOCATION_MAX_WAITING_TIME * Utils.LOCATION_BASE_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent(){
        Intent intent = new Intent(this, BackgroundLocationIntentService.class);
        intent.setAction(Utils.ACTION_PROCESS_UPDATES);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return  PendingIntent.getForegroundService(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }else {
                return  PendingIntent.getService(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

    }

    private void startLocationService(View view) {
        requestLocationUpdates();
        LocationResultHelper.setLocationRequestStatus(this, true);
    }

    private void stopLocationService(View view) {
        mLocationProviderClient.removeLocationUpdates(getPendingIntent());
        LocationResultHelper.setLocationRequestStatus(this, false);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //if don't want to receive location in background
        // mLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationTV.setText(LocationResultHelper.getSavedLocationResults(this));
        setButtonEnableState(LocationResultHelper.getLocationRequestStatus(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Utils.KEY_LOCATION_RESULTS)){
            locationTV.setText(LocationResultHelper.getSavedLocationResults(this));
        }else if (key.equals(Utils.KEY_LOCATION_REQUEST)){
            setButtonEnableState(LocationResultHelper.getLocationRequestStatus(this));
        }
    }

    private void setButtonEnableState(boolean value){
        if (value){
            startLocationUpdateServiceBtn.setEnabled(false);
            stopLocationUpdateServiceBtn.setEnabled(true);
        }else {
            startLocationUpdateServiceBtn.setEnabled(true);
            stopLocationUpdateServiceBtn.setEnabled(false);
        }
    }

}