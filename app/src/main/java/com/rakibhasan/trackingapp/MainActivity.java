package com.rakibhasan.trackingapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.GeoPoint;
import com.rakibhasan.trackingapp.constants.Utils;
import com.rakibhasan.trackingapp.contact.ContactToAdminActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout mainDrawerLayout;
    private NavigationView mainNavigationView;
    private Toolbar mainToolbar;
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;
    HandlerThread mHandlerThread;

    private FirebaseFirestore db;
    FirebaseUser user;
    Location mLastLocation;
    Marker mCurrLocationMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainDrawerLayout = findViewById(R.id.main_drawer_layout);
        mainNavigationView = findViewById(R.id.main_nav_view);
        mainToolbar = findViewById(R.id.main_tool_bar);

        //init fire store
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        //Toolbar
        setSupportActionBar(mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);

        //Navigation drawer menu
        configureDrawerLayout();

        //check the location permission
        initGoogleMap();

        //initializing
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //execute in background
                if (locationResult == null) {
                    Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                mLastLocation = locationResult.getLastLocation();

                //execute in mainThread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goToLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    }
                });
                updateLocationDB(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
        };
        getLocationUpdate();
    }

    //inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_for_map, menu);
        return true;
    }

    //Menu selected items and its behave
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedItemID = item.getItemId();

        switch (selectedItemID) {
            case R.id.map_type_normal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_type_satellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_type_terrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.map_type_hybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void updateLocationDB(double latitude, double longitude) {

        Map<String, Object> currentLocationUpdateDB = new HashMap<>();
        currentLocationUpdateDB.put("currentLocation", new GeoPoint(latitude,longitude));
        currentLocationUpdateDB.put("lastUpdatedTime", FieldValue.serverTimestamp());
        currentLocationUpdateDB.put("userName", user.getDisplayName());

        db.collection("usersLocation").document(user.getUid())
                .set(currentLocationUpdateDB)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      //  Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });



    }

    public void initGoogleMap() {
        if (checkGoogleServices()) {
            if (isGPSEnabled()) {
                if (checkLocationPermission()) {
                    SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.map_fragment_container, supportMapFragment)
                            .commit();
                    supportMapFragment.getMapAsync(this);
                } else {
                    locationPermissionRequest();
                }
            }
        }
    }

    //return a boolean value that says permission true or false
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    //this method is for runtime permission
    private void locationPermissionRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Utils.PERMISSION_REQUEST_CODE);
            }
        }
    }

    //gps permission
    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnabled) {
            return true;
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.GPS_PERMISSION_TITLE)
                    .setMessage(R.string.GPS_PERMISSION_MESSAGE)
                    .setPositiveButton(R.string.GPS_PERMISSION_POSITIVE_BUTTON_STRING, ((dialog, which) -> {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), Utils.GPS_PERMISSION_REQUEST_CODE);
                    }))
                    .setCancelable(false)
                    .show();
        }
        return false;
    }

    private boolean checkGoogleServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            //if all is ok
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            // if device have some error but user can solve that problem
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, Utils.PLAY_SERVICES_ERROR_CODE,
                    task -> Toast.makeText(this, R.string.PLAY_SERVICE_DIALOG_CANCELLED, Toast.LENGTH_SHORT).show());
            dialog.show();

        } else {
            Toast.makeText(this, R.string.PLAY_SERVICE_DIALOG, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //configuring the drawerLayout and its components
    private void configureDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolbar, R.string.NAV_OPEN, R.string.NAV_CLOSE);
        mainDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mainNavigationView.bringToFront();
        mainNavigationView.setNavigationItemSelectedListener(this);

        //access the header components
        View mainHeader = mainNavigationView.getHeaderView(0);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null) {
            ((TextView) mainHeader.findViewById(R.id.main_header_user_name)).setText(signInAccount.getDisplayName());
            ((TextView) mainHeader.findViewById(R.id.main_header_user_email)).setText(signInAccount.getEmail());
            Glide.with(this).load(signInAccount.getPhotoUrl()).into(((CircleImageView) mainHeader.findViewById(R.id.main_header_user_image)));
        }
    }

    @Override
    public void onBackPressed() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Navigation selected items and its behave
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.nav_contact_with_admin:
                startActivity(new Intent(getApplicationContext(), ContactToAdminActivity.class));
                break;
            case R.id.nav_signOut:
                Utils.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
        }
        return false;
    }

    private void getMyJourneyFragment() {
        MyJourneyFragment myJourneyFragment = new MyJourneyFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_fragment_container, myJourneyFragment)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utils.PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Log.v("Tag", "PERMISSION_GRANTED");
            Toast.makeText(this, "PERMISSION_GRANTED", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "PERMISSION_NOT_GRANTED", Toast.LENGTH_SHORT).show();
            Log.v("Tag", "PERMISSION_NOT_GRANTED");

        }
    }

    //maps
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private void goToLocation(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Utils.GOOGLE_MAP_DEFAULT_ZOOM_MAIN_ACTIVITY);
        mGoogleMap.moveCamera(cameraUpdate);
        MarkerOptions markerOptions = new MarkerOptions()
                .title(user.getDisplayName())
                .position(new LatLng(lat, lng));

        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.setMapType(Utils.GOOGLE_MAP_DEFAULT_MAP_TYPE_MAIN_ACTIVITY);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }
        //setMyLocationEnabled use gps all the time and waste device battery charge
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.GPS_PERMISSION_REQUEST_CODE) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerEnabled) {
                Toast.makeText(this, "GPS is enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS is not enabled, unable to show location!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Utils.LOCATION_SET_INTERVAL * Utils.LOCATION_BASE_INTERVAL);
        locationRequest.setFastestInterval(Utils.LOCATION_SET_FASTEST_INTERVAL * Utils.LOCATION_BASE_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //manage the thread in background
        mHandlerThread = new HandlerThread("LocationCallBackThread");
        mHandlerThread.start();
        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, mHandlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationCallback != null) {
            //if user minimize the app
            mLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
    }

}



