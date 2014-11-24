package com.geofind.geofind.geoutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.geofind.geofind.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.Callable;


/**
 * This class connects to location client and retrieves the fused location
 * Created by Ilia Marin on 30/10/2014.
 */
public class LocationFinder implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final String TAG = LocationFinder.class.getName();

    /**
     * Define an object that holds accuracy and frequency parameters
     */
    LocationRequest locationRequest;

    /**
     * The found {@link android.location.Location}
     */
    private Location currentLocation;

    /**
     * The requested activity
     */
    private Activity context;

    /**
     * The {@link com.google.android.gms.location.LocationClient} is the interface to get the
     * location
     */
    private LocationClient locationClient;

    /**
     * The {@link java.util.concurrent.Callable} (callback) to be called when the location found
     */
    private Callable<Void> locationFound;

    /**
     * is it a single request or continues
     */
    private boolean requireUpdates;

    /**
     * The host activity requires (as opposed to optional) the location
     */
    private boolean requireLocationEnabled;

    public LocationFinder(Activity context, Callable<Void> locationFound) {
        locationClient = new LocationClient(context, this, this);
        this.context = context;
        currentLocation = null;
        this.locationFound = locationFound;

        requireLocationEnabled = true;
        requireUpdates = false;

    }

    /**
     * Sets if the host activity requires (as opposed to optional) the location
     */
    public void setRequireLocationEnabled(boolean requireLocationEnabled) {
        this.requireLocationEnabled = requireLocationEnabled;
    }

    /**
     * Start continues updates
     *
     * @param updateInterval  the interval between the requests [milisec]
     * @param fastestInterval the minimal interval between consecutive requests [milisec]
     */
    public void startPeriodicUpdates(long updateInterval, long fastestInterval) {
        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(fastestInterval);

        requireUpdates = true;
        startLocation();

    }

    /**
     * stop the update requests
     */
    public void stopPeriodicUpdates() {
        if (requireUpdates) {
            requireUpdates = false;
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
        Log.d(TAG, "onConnect");

        if (currentLocation == null) {
            updateLocation();
            Log.d(TAG, "No location oncreate");

            if (requireLocationEnabled) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

                builder.setMessage(context.getString(R.string.location_not_available))
                        .setTitle(context.getString(R.string.location_services_disabled))
                        .setPositiveButton(context.getString(R.string.location_settings),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        context.startActivity(new Intent(action));
                                        d.dismiss();
                                    }
                                })
                        .setNegativeButton(context.getString(R.string.dialog_dismiss),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        d.cancel();
                                    }
                                });
                builder.create().show();
            }
        }

        if (requireUpdates) {
            locationClient.requestLocationUpdates(locationRequest, this);
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
        locationClient = null;
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "connect failed");
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

    /**
     * Called when the location changed
     * @param location current location
     */
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

    /**
     * force the the android to sample a new location
     */
    public void updateLocation() {
        if (locationClient.isConnected())
            locationClient.requestLocationUpdates(
                    LocationRequest.create().setNumUpdates(1)
                            .setInterval(5000).setFastestInterval(1000)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY), LocationFinder.this);
    }
}
