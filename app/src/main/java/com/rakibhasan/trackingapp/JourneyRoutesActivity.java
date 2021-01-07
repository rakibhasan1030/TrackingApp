package com.rakibhasan.trackingapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rakibhasan.trackingapp.constants.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JourneyRoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_routes);

        initGoogleMap();

        LatLng latlngOne = new LatLng(23.760441, 90.360321); //
        LatLng latlngTwo = new LatLng(23.900839, 90.404810); //

        configureMap(mGoogleMap, latlngOne.latitude, latlngOne.latitude);


        //5.When you want to get the path of two point
        String url = getMapsApiDirectionsUrl(latlngOne, latlngTwo);
        ReadTask downloadTask = new ReadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    //1. Get direction URL require to call Google Maps API
    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;

    }

    //2.Do the URL call in background
    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);


            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }

    public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();


            } catch (Exception e) {
                Log.d("TAG", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;

        }
    }

    //3.Create Parser class to parse the data from JSON to List of pointer's
    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;

        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }

    }

    //4.Do the parse using another Thread to expand performance
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            ;
            PolylineOptions lineOptions = new PolylineOptions();
            ;
            lineOptions.width(2);
            lineOptions.color(Color.RED);
            MarkerOptions markerOptions = new MarkerOptions();
            // Traversing through all the routes
            for (int i = 0; i < routes.size(); i++) {
                // Fetching i-th route
                List<HashMap<String, String>> path = routes.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);

            }
            // Drawing polyline in the Google Map for the i-th route
            if (points.size() != 0) mGoogleMap.addPolyline(lineOptions);//to avoid crash

        }

    }


    //permissions

    public void initGoogleMap() {
        if (checkGoogleServices()) {
            if (isGPSEnabled()) {
                if (checkLocationPermission()) {
                    Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
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


    private void goToLocation(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Utils.GOOGLE_MAP_DEFAULT_ZOOM_MAIN_ACTIVITY);
        mGoogleMap.moveCamera(cameraUpdate);
        MarkerOptions markerOptions = new MarkerOptions()
                .title("Dhaka")
                .position(new LatLng(lat, lng));
        mGoogleMap.addMarker(markerOptions);
        mGoogleMap.setMapType(Utils.GOOGLE_MAP_DEFAULT_MAP_TYPE_MAIN_ACTIVITY);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }
/*        //setMyLocationEnabled use gps all the time and waste device battery charge
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);*/
    }

    private void configureMap(GoogleMap map, double lat, double lon) {
        if (map == null)
            return; // Google Maps not available
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            Log.e("TAG", "Have GoogleMap but then error", e);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
        LatLng latLng = new LatLng(lat, lon);
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, Utils.GOOGLE_MAP_DEFAULT_ZOOM_MAIN_ACTIVITY);
        map.moveCamera(camera);
    }



}
