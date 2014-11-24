package com.geofind.geofind.geoutils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.HashSet;
import java.util.List;

/**
 * This intent service used when approaching geofence point
 * Created by Ilia Marin on 24/10/2014.
 */
public class ReceiveTransitionsIntentService extends IntentService {

     private static String TAG = ReceiveTransitionsIntentService.class.getName();

    /**
     *  The Ids of the points that already arrived
     */
    private static HashSet<String> seenHashes = new HashSet<String>();

    /**
     * The geofence that initialized this request
     */
    private static GeofenceManager manager;

    /**
     * The instance of this class for prevent recreation
     */
    private static int currentUse = 0;


    /**
     * Set service identifier
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Assign the geofene manager to the class
     */
    public static void setManager(GeofenceManager manager) {
        ReceiveTransitionsIntentService.manager = manager;
        currentUse++;
    }

    /**
     * Reset the seen points
     */
    public static void clearList() {
        seenHashes.clear();
        currentUse++;
    }

    /**
     * @return the current instance number
     */
    public static int getCurrentUse() {
        return currentUse;
    }

    /**
     * Called when arrived to point
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (LocationClient.hasError(intent)) {
            int errorCode = LocationClient.getErrorCode(intent);
            Log.e(TAG,"Location Services error: " + Integer.toString(errorCode));
        } else {


            int transientType =
                    LocationClient.getGeofenceTransition(intent);

            // Check the transient status
            if (transientType == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    transientType == Geofence.GEOFENCE_TRANSITION_EXIT) {

                List<Geofence> triggerList =
                        LocationClient.getTriggeringGeofences(intent); //Added LocationClient

                // debug id
                int serviceIdNum = intent.getIntExtra("UseID", -1);
                Log.d(TAG, "curId = " + currentUse + " rec = " + serviceIdNum);
                if (currentUse > 1) {
                    //  return;
                }

                String[] triggerIds = new String[triggerList.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    triggerIds[i] = triggerList.get(i).getRequestId();
                }

                // Skip seen points (for double calling)
                if (seenHashes.contains(triggerIds[0])) {
                    Log.d(TAG, "skipping " + transientType);
                    return;
                }

                seenHashes.add(triggerIds[0]);

                // remove the geofence for the current point
                Log.d(TAG, "calling manager for type" + transientType);
                if (manager != null) {
                    Log.d(TAG, "removing trigger: " + triggerIds[0]);
                    manager.removeGeofences(triggerIds[0]);
                }

            }
        }
    }
}
