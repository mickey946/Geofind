package com.geofind.geofind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    Location currentLocation;
    //Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    private Activity context;
    private LocationClient locationClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private boolean mConnected;
    private Callable<Void> locationFound;
    // is it a single request or continues
    private boolean _requireUpdates;

    public LocationFinder(Activity context, Callable<Void> locationFound) {
        Log.d("LocationFinder", "c-tor");
        locationClient = new LocationClient(context, this, this);
        this.context = context;
        mInProgress = false;
        mConnected = false;
        currentLocation = null;
        this.locationFound = locationFound;


        _requireUpdates = false;

    }

    /**
     * Start continues updates
     *
     * @param updateInterval  the interval between the requests [milisec]
     * @param fastestInterval the minimal interval between consecutive requests [milisec]
     */
    public void startPeriodicUpdates(long updateInterval, long fastestInterval) {
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(updateInterval);
        mLocationRequest.setFastestInterval(fastestInterval);

        _requireUpdates = true;
        startLocation();

    }

    /**
     * stop the update requests
     */
    public void stopPeriodicUpdates() {
        if (_requireUpdates) {
            _requireUpdates = false;
            if (locationClient.isConnected()) {
                locationClient.removeLocationUpdates(this);
            }
        }
        stopLocation();
    }

    /**
     * start location client
     */
    public void startLocation() {
        if (!locationClient.isConnected())
            locationClient.connect();
    }

    /**
     * stop location client
     */
    public void stopLocation() {
        if (locationClient.isConnected())
            locationClient.disconnect();
    }

    /**
     * get the current location
     *
     * @return the current location or null if the location unavailable
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {

        currentLocation = locationClient.getLastLocation();
        Log.d("LocationFinder", "onConnect");

        if (currentLocation == null) {
            updateLocation();
            Log.d("LocationFinder", "No location oncreate");


            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

            builder.setMessage(context.getString(R.string.LocationNotAvailable))
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    context.startActivity(new Intent(action));


                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            })
            ;
            builder.create().show();
        }

        if (_requireUpdates) {
            locationClient.requestLocationUpdates(mLocationRequest, this);
        }

        try {
            if (currentLocation != null)
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
            Log.e("LocationFinder", connectionResult.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationFinder", "Location changed");
        currentLocation = location;
        try {
            if (currentLocation != null)
                locationFound.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLocation() {
        if (locationClient.isConnected())
            locationClient.requestLocationUpdates(
                    LocationRequest.create().setNumUpdates(1)
                            .setInterval(5000).setFastestInterval(1000)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY), LocationFinder.this);
    }
}
