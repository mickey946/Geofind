package com.geofind.geofind;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This intent service used when approaching geofence point
 * Created by Ilia Marin on 24/10/2014.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    private static GeofenceManager _manager;
    static HashSet<String> _seenHashes = new HashSet<String>();

    /**
     * Set service identifier
     */
    public ReceiveTransitionsIntentService(){
        super("ReceiveTransitionsIntentService");
        //_seenHashes = new HashSet<String>();
    //    _manager = null;
    }

    public static void set_manager(GeofenceManager manager){
        _manager = manager;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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

                for (int i = 0; i < triggerIds.length; i++) {
                    triggerIds[i] = triggerList.get(i).getRequestId();
                }
                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */

                if (_seenHashes.contains(triggerIds[0]))
                {
                    Log.d("Geofence Service","skipping " + transientType);
                    return;
                }

                _seenHashes.add(triggerIds[0]);

                Log.d("Geofence Service","calling manager for type" + transientType);
                if (_manager != null){
                    Log.d("Geofence Service","removing trigger: " + triggerIds[0]);
                    _manager.removeGeofences(triggerIds[0]);
                }

//                Intent intent1 = new Intent(getString(R.string.GeofenceResultIntent));
//                intent1.putExtra(getString(R.string.PointIdIntentExtra), triggerIds[0]);
//                intent1.putExtra(getString(R.string.PointIndexExtra),
//                        intent.getIntExtra(getString(R.string.PointIndexExtra),-1));
//                LocalBroadcastManager.getInstance(this)
//                        .sendBroadcast(intent1);
            }
        }
    }
}
