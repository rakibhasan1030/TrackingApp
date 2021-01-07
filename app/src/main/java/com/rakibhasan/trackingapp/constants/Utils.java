package com.rakibhasan.trackingapp.constants;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;

public class Utils {

    public static final int RC_SIGN_IN = 200;
    public static final int PERMISSION_REQUEST_CODE = 9001;
    public static final int PLAY_SERVICES_ERROR_CODE = 9002;
    public static final int GPS_PERMISSION_REQUEST_CODE = 9003;
    public static final float GOOGLE_MAP_DEFAULT_ZOOM_MAIN_ACTIVITY = 15;
    public static final int GOOGLE_MAP_DEFAULT_MAP_TYPE_MAIN_ACTIVITY = GoogleMap.MAP_TYPE_NORMAL;
    public static final String GOOGLE_MAP_DIRECTION_URL = "http://maps.googleapis.com/maps/api/directions/";
    public static final String GOOGLE_MAP_API_KEY = "AIzaSyCtUVY_OcBJ2eSdCeJsJDg9pjhKEfcLqmI";

    //batch location - channel id
    public static final String CHANNEL_ID = "default-channel";


    //Location timing
    public static final int LOCATION_BASE_INTERVAL = 1000;
    public static final int LOCATION_SET_INTERVAL = 5;
    public static final int LOCATION_SET_FASTEST_INTERVAL = 1;
    public static final int BATCH_LOCATION_MAX_WAITING_TIME = 15;

    //Location save constant
    public static final String KEY_LOCATION_RESULTS = "key-location-results";
    public static final String KEY_LOCATION_REQUEST = "key-location-request";


    //Services constants (BackgroundLocationIntentService)
    public static final String ACTION_PROCESS_UPDATES = "com.rakibhasan.trackingapp"+".PROCESS_UPDATES";





    public static void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

}
