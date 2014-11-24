package com.geofind.geofind.geoutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.geofind.geofind.R;
import com.geofind.geofind.structures.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ilia Marin on 23/10/2014.
 */
public class GeofenceManager implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {

    /*
         * Define a request code to send to Google Play services
         * This code is returned in Activity.onActivityResult
         */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GeoFence";

    // Geofence list
    List<Geofence> mCurrentGeofence;

    // Host activity
    private Activity _activity;

    // Holds the location client
    private LocationClient mLocationClient;

    // Geofonce request type
    private REQUEST_TYPE mRequestType;

    // the intent that will be called when point approached
    private PendingIntent mTransitionPendingIntent;

    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    // Storage of geofence data
    private SimpleGeofenceStore simpleGeofenceStore;

    // Active point index
    private int _pointIndex;

    private String _activeID;

    private boolean _geofenceCreateFinished;

    private boolean _pointCanceled;

    // List of point IDs to be canceled
    private List<String> _deletePoint;

    //Call back to inform the UI of canceling
    private GeoUtils.IndexCallback _cancelCallback;

    private boolean _cleanUp;

    private float _lastAccuracy;


    /**
     * Contstructor and initializer
     *
     * @param activity the hosting activity
     */
    public GeofenceManager(Activity activity) {
        this._activity = activity;
        simpleGeofenceStore = new SimpleGeofenceStore(activity);
        mCurrentGeofence = new ArrayList<Geofence>();
        _pointIndex = -1;
        _lastAccuracy = -1;
        mInProgress = false;
        _cancelCallback = null;
        _pointCanceled = false;
        _geofenceCreateFinished = true;
        _cleanUp = false;
        ReceiveTransitionsIntentService.set_manager(this);
        addGeofences();
    }

    /**
     * Add new geofence
     *
     * @param point      the destination point
     * @param radius     the radius of accepted arrival in meters
     * @param pointIndex the index of the point in the route
     */
    public void createGeofence(Point point, float radius, int pointIndex) {

        addGeofences();
        final String ID = composeID(point);
        _activeID = ID;

//        if (getLocationClient().getLastLocation()!=null) {
//            if(!mInProgress) {
//                _lastAccuracy = getLocationClient().getLastLocation().getAccuracy();
//            }
//        }

        if (_lastAccuracy > 0) {

            radius = Math.max(radius, _lastAccuracy);
        }

        Log.d(TAG, "create geofence " + ID + "with radius " + radius + "at" + pointIndex);
        _pointIndex = pointIndex;
        _geofenceCreateFinished = false;
        SimpleGeofence simpleGeofence = new SimpleGeofence(
                ID,
                point.getLatitude(),
                point.getLongitude(),
                radius,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER);

        simpleGeofenceStore.setGeofence(ID, simpleGeofence);
        mCurrentGeofence.add(simpleGeofence.toGeofence());


    }

    LocationClient getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(_activity, this, this);
        }
        return mLocationClient;
    }

    public void resumeGeofence() {
        if (!_geofenceCreateFinished) {
            Log.d(TAG, "resuming geofence");
            mCurrentGeofence.add(simpleGeofenceStore.getGeofence(_activeID).toGeofence());
            if (!getLocationClient().isConnected())
                getLocationClient().connect();
        }
    }


    /**
     * Set the callback to be initiated when the point be revealed
     *
     * @param cancelCallback
     */
    public void setCancelCallback(GeoUtils.IndexCallback cancelCallback) {
        this._cancelCallback = cancelCallback;
    }

    /*
     * Helping method for consistent id
     */
    private String composeID(Point p) {
        return "GeoFind_" + p.getLatitude() + "_" + p.getLongitude();
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(_activity,
                ReceiveTransitionsIntentService.class);

        intent.putExtra("UseID", ReceiveTransitionsIntentService.get_currentUse());

        intent.putExtra(_activity.
                getString(R.string.PointIndexExtra), _pointIndex);

        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                _activity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */

    public void removeGeofences(String geoID) {
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_POINT;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!GooglePlayUtils.servicesConnected(_activity)) {
            return;
        }
        // Store the PendingIntent
        _deletePoint = new ArrayList<String>();
        _deletePoint.add(geoID);
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        //mLocationClient = new LocationClient(_activity, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            Log.d(TAG, "remove request");
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            getLocationClient().connect();
        } else {
            Log.d(TAG, "Skip remove request in progress");
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    public void removeGeofences(Point point) {
        _pointCanceled = true;
        removeGeofences(composeID(point));
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Start with the request flag set to false
        mInProgress = false;
        _lastAccuracy = getLocationClient().getLastLocation().getAccuracy();
        switch (mRequestType) {
            case ADD:
                mTransitionPendingIntent = getTransitionPendingIntent();
                getLocationClient().addGeofences(mCurrentGeofence,
                        mTransitionPendingIntent, this);
                break;
            case REMOVE_POINT:
                getLocationClient().removeGeofences(
                        _deletePoint, this);
                break;
            case REMOVE_INTENT:
                getLocationClient().removeGeofences(mTransitionPendingIntent, new LocationClient.OnRemoveGeofencesResultListener() {
                    @Override
                    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {

                    }

                    @Override
                    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {

                    }
                });
        }

    }

    /**
     * initialize geofence capability
     */
    public void addGeofences() {

        mRequestType = REQUEST_TYPE.ADD;
        if (!GooglePlayUtils.servicesConnected(_activity))
            return;

        // mLocationClient = new LocationClient(_activity, this, this);

        if (!mInProgress) {
            Log.d(TAG, "Add geofence STARTING progress");
            mInProgress = true;
            getLocationClient().connect();
        } else {
            Log.d(TAG, "Add geofence in progress");
            // connection is already set
        }

    }

    @Override
    public void onDisconnected() {
        mInProgress = false;
        mLocationClient = null;
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (LocationStatusCodes.SUCCESS == statusCode) {
              /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
            _geofenceCreateFinished = true;
            Log.d(TAG, "Location geofence added succssfully");
        } else {
            // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */

            if (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == statusCode) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(_activity);

                builder.setMessage(_activity.getString(R.string.location_disabled_error))
                        .setTitle(_activity.getString(R.string.location_services_disabled))
                        .setPositiveButton(_activity.getString(R.string.location_settings),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        _activity.startActivity(new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        d.dismiss();
                                    }
                                })
                        .setNegativeButton(_activity.getString(R.string.location_missing_canceled),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        _activity.finish();
                                    }
                                })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                _activity.finish();
                            }
                        });
                builder.create().show();
            }

            Log.d(TAG, "Location geofence added with error: " + statusCode);
        }

        mInProgress = false;
        getLocationClient().disconnect();


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        _activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }

        } else {
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    _activity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        ErrorDialogFragment.newInstance(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        _activity.getFragmentManager(),
                        "Geofence Detection");
            }

        }
    }

    /**
     * Canceling geofence response by ID
     */
    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] strings) {

        mInProgress = false;
        getLocationClient().disconnect();
        if (statusCode == LocationStatusCodes.SUCCESS) {
            Log.d(TAG, "removed #" + _pointIndex + " by id (" + strings.length + ") " + strings[0]);
            simpleGeofenceStore.clearGeofence(strings[0]);
            if (_cancelCallback != null && _pointCanceled) {
                _pointCanceled = false; // reset for next time
                try {
                    _cancelCallback.executeCallback(_pointIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (!_pointCanceled && !_cleanUp) {
                Intent intent1 = new Intent(_activity.getString(R.string.GeofenceResultIntent));
                intent1.putExtra(_activity.getString(R.string.PointIdIntentExtra), strings[0]);
                intent1.putExtra(_activity.getString(R.string.PointIndexExtra),
                        _pointIndex);
                LocalBroadcastManager.getInstance(_activity)
                        .sendBroadcast(intent1);
            }
            /**
             * Succusful removal can handle UI
             */
        } else {
            Log.d(TAG, "location failed removed by id");
            /**
             * Report Error to UI
             */
        }

    }

    /**
     * Canceling geofence response by intent
     */
    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {

        mInProgress = false;
        getLocationClient().disconnect();
        if (statusCode == LocationStatusCodes.SUCCESS) {
            if (_cancelCallback != null && _pointCanceled) {
                try {
                    _cancelCallback.executeCallback(_pointIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /**
             * Succusful removal can handle UI
             */
        } else {
            Log.d(TAG, "location removed failed by intent");
            /**
             * Report Error to UI
             */
        }

    }

    public void destroy() {
        _cleanUp = true;
        ReceiveTransitionsIntentService.clearList();
//        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
//            /*
//         * Test for Google Play services after setting the request type.
//         * If Google Play services isn't present, the request can be
//         * restarted.
//         */
//        if (!GooglePlayUtils.servicesConnected(_activity)) {
//            return;
//        }
//        //mLocationClient = new LocationClient(_activity, this, this);
//        // If a request is not already underway
//        if (!mInProgress) {
//            Log.d(TAG,"remove intent");
//            // Indicate that a request is underway
//            mInProgress = true;
//            // Request a connection from the client to Location Services
//            getLocationClient().connect();
//        } else {
//            Log.d(TAG,"Skip remove request in progress");
//            /*
//             * A request is already underway. You can handle
//             * this situation by disconnecting the client,
//             * re-setting the flag, and then re-trying the
//             * request.
//             */
//        }
    }


    // Defines the allowable request types.
    public enum REQUEST_TYPE {
        ADD,
        REMOVE_POINT,
        REMOVE_INTENT
    }

    /**
     * A single Geofence object, defined by its center and radius.
     */
    public class SimpleGeofence {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private long mExpirationDuration;
        private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude   Latitude of the Geofence's center.
         * @param longitude  Longitude of the Geofence's center.
         * @param radius     Radius of the geofence circle.
         * @param expiration Geofence expiration duration
         * @param transition Type of Geofence transition.
         */
        public SimpleGeofence(
                String geofenceId,
                double latitude,
                double longitude,
                float radius,
                long expiration,
                int transition) {
            // Set the instance fields from the constructor
            this.mId = geofenceId;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            this.mExpirationDuration = expiration;
            this.mTransitionType = transition;
        }

        // Instance field getters
        public String getId() {
            return mId;
        }

        public double getLatitude() {
            return mLatitude;
        }

        public double getLongitude() {
            return mLongitude;
        }

        public float getRadius() {
            return mRadius;
        }

        public long getExpirationDuration() {
            return mExpirationDuration;
        }

        public int getTransitionType() {
            return mTransitionType;
        }

        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence() {
            // Build a new Geofence object
            return new Geofence.Builder()
                    .setRequestId(getId())
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(
                            getLatitude(), getLongitude(), getRadius())
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        }
    }

    /**
     * Storage for geofence values, implemented in SharedPreferences.
     */
    public class SimpleGeofenceStore {
        // Keys for flattened geofences stored in SharedPreferences
        public static final String KEY_LATITUDE =
                "com.geofind.geofind.geofence.KEY_LATITUDE";
        public static final String KEY_LONGITUDE =
                "com.geofind.geofind.geofence.KEY_LONGITUDE";
        public static final String KEY_RADIUS =
                "com.geofind.geofind.geofence.KEY_RADIUS";
        public static final String KEY_EXPIRATION_DURATION =
                "com.geofind.geofind.geofence.KEY_EXPIRATION_DURATION";
        public static final String KEY_TRANSITION_TYPE =
                "com.geofind.geofind.geofence.KEY_TRANSITION_TYPE";
        // The prefix for flattened geofence keys
        public static final String KEY_PREFIX =
                "com.geofind.geofind.geofence.KEY";
        /*
         * Invalid values, used to test geofence storage when
         * retrieving geofences
         */
        public static final long INVALID_LONG_VALUE = -999l;
        public static final float INVALID_FLOAT_VALUE = -999.0f;
        public static final int INVALID_INT_VALUE = -999;
        // The name of the SharedPreferences
        private static final String SHARED_PREFERENCES =
                "SharedPreferences";
        // The SharedPreferences object in which geofences are stored
        private final SharedPreferences mPrefs;

        // Create the SharedPreferences storage with private access only
        public SimpleGeofenceStore(Context context) {
            mPrefs =
                    context.getSharedPreferences(
                            SHARED_PREFERENCES,
                            Context.MODE_PRIVATE);
        }

        /**
         * Returns a stored geofence by its id, or returns null
         * if it's not found.
         *
         * @param id The ID of a stored geofence
         * @return A geofence defined by its center and radius. See
         */
        public SimpleGeofence getGeofence(String id) {
            /*
             * Get the latitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lat = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_LATITUDE),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the longitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lng = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_LONGITUDE),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the radius for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            float radius = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_RADIUS),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the expiration duration for the geofence identified
             * by id, or INVALID_LONG_VALUE if it doesn't exist
             */
            long expirationDuration = mPrefs.getLong(
                    getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    INVALID_LONG_VALUE);
            /*
             * Get the transition type for the geofence identified by
             * id, or INVALID_INT_VALUE if it doesn't exist
             */
            int transitionType = mPrefs.getInt(
                    getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    INVALID_INT_VALUE);
            // If none of the values is incorrect, return the object
            if (
                    lat != INVALID_FLOAT_VALUE &&
                            lng != INVALID_FLOAT_VALUE &&
                            radius != INVALID_FLOAT_VALUE &&
                            expirationDuration !=
                                    INVALID_LONG_VALUE &&
                            transitionType != INVALID_INT_VALUE) {

                // Return a true Geofence object
                return new SimpleGeofence(
                        id, lat, lng, radius, expirationDuration,
                        transitionType);
                // Otherwise, return null.
            } else {
                return null;
            }
        }

        /**
         * Save a geofence.
         *
         * @param geofence The SimpleGeofence containing the
         *                 values you want to save in SharedPreferences
         */
        public void setGeofence(String id, SimpleGeofence geofence) {
            /*
             * Get a SharedPreferences editor instance. Among other
             * things, SharedPreferences ensures that updates are atomic
             * and non-concurrent
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            // Write the Geofence values to SharedPreferences
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_LATITUDE),
                    (float) geofence.getLatitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_LONGITUDE),
                    (float) geofence.getLongitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_RADIUS),
                    geofence.getRadius());
            editor.putLong(
                    getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    geofence.getExpirationDuration());
            editor.putInt(
                    getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    geofence.getTransitionType());
            // Commit the changes
            editor.commit();
        }

        public void clearGeofence(String id) {
            /*
             * Remove a flattened geofence object from storage by
             * removing all of its keys
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
            editor.remove(getGeofenceFieldKey(id,
                    KEY_EXPIRATION_DURATION));
            editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
            editor.commit();
        }

        /**
         * Given a Geofence object's ID and the name of a field
         * (for example, KEY_LATITUDE), return the key name of the
         * object's values in SharedPreferences.
         *
         * @param id        The ID of a Geofence object
         * @param fieldName The field represented by the key
         * @return The full key name of a value in SharedPreferences
         */
        private String getGeofenceFieldKey(String id,
                                           String fieldName) {
            return KEY_PREFIX + "_" + id + "_" + fieldName;
        }
    }
}
