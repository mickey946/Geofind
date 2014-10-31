package com.geofind.geofind;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.Callable;



/**
 * Created by Ilia Marin on 30/10/2014.
 */
public class LocationFinder implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener        {
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Activity context;
    private LocationClient locationClient;

    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private boolean mConnected;

    private Callable<Void> locationFound;

    Location currentLocation;


    public LocationFinder(Activity context, Callable<Void> locationFound){
        locationClient = new LocationClient(context,this,this);
        this.context = context;
        mInProgress = false;
        mConnected = false;
        currentLocation = null;
        this.locationFound = locationFound;

    }

    public void startLocation(){
        locationClient.connect();
    }

    public void stopLocation(){
        locationClient.disconnect();
    }

    public Location getCurrentLocation(){
        return currentLocation;
    }
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        Log.d("LocationFinder", "onConnected");
        // Display the connection status
        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();

        currentLocation = locationClient.getLastLocation();

        try {
            locationFound.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(context, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
        mInProgress = false;
        locationClient = null;
        mConnected = false;
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
        Log.d("LocationFinder", "connect failed");
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        context,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            //context.showErrorDialog(connectionResult.getErrorCode());
            Log.e("LocationFinder",connectionResult.toString());
        }
    }

}
