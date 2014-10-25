package com.geofind.geofind;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * Created by Ilia Marin on 24/10/2014.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    private static String TAG= "ReceiveTransitionsIntentService - GeoFence";

    /**
     * Set service identifier
     */
    public ReceiveTransitionsIntentService(){
        super("ReceiveTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent");
        if (LocationClient.hasError(intent)){
            int errorCode = LocationClient.getErrorCode(intent);
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode));
            //TODO send some error to application


        } else {
            int transientType =
                    LocationClient.getGeofenceTransition(intent);

            if (transientType == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    transientType == Geofence.GEOFENCE_TRANSITION_EXIT){
                List<Geofence> triggerList =
                            LocationClient.getTriggeringGeofences(intent); //Added LocationClient
                String[] triggerIds = new String[triggerList.size()];


                String type ;

                if (transientType == Geofence.GEOFENCE_TRANSITION_EXIT){
                    type = "Exit";
                } else
                {
                    type = "Enter";
                }

                for (int i = 0; i < triggerIds.length; i++) {
                    triggerIds[i] = triggerList.get(i).getRequestId();
                    Log.d(TAG, "Type: " + type + " " + triggerIds[i]);
                }
                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */
                }
        }
    }
}
