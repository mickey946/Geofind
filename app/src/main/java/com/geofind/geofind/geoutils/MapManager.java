package com.geofind.geofind.geoutils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.geofind.geofind.structures.Hint;
import com.geofind.geofind.structures.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Ilia Marin on 05/10/2014.
 */
public class MapManager {

    public static final String TAG = MapManager.class.getName();

    /**
     * The zooming of the map when nothing is drawn on it
     */
    private static final int DEFAULT_ZOOM = 17;

    /**
     * The {@link com.google.android.gms.maps.model.MarkerOptions } which used to add new marker
     */
    protected MarkerOptions markerOptions;

    /**
     * The {@link com.google.android.gms.maps.MapFragment} which displaying the current map
     */
    private MapFragment mapFragment;

    /**
     * The host activity
     */
    private Activity activity;

    /**
     * The offset in which to move the map center. used when the map is covered by the sliding panel
     */
    private float offsetX, offsetY;

    /**
     * The zoom level in which to display the map
     */
    private int zoomLevel;

    /**
     * Map dimensions
     */
    private int mapWidth, mapHeight;

    /**
     * The callback to be called when the zoom changed to update the drawn circle
     */
    private Callable<Void> zoomUpdate;

    /**
     * the {@link com.geofind.geofind.geoutils.GeoUtils.IndexCallback} to be called when pressed
     * on marker
     */
    private GeoUtils.IndexCallback indexCallback;

    /**
     * Held all the {@link com.google.android.gms.maps.model.Marker} that are drawn on the map, and
     * the corresponding point index.
     */
    private HashMap<Marker, Integer> markerMap;

    /**
     * Google map interface object
     */
    private GoogleMap map;

    /**
     * The current selected {@link com.geofind.geofind.structures.Point}
     */
    private Point selectedPoint;

    /**
     * Current location provider
     */
    private LocationFinder locationFinder;

    /**
     * Map Manager constructor for usage with AutoComplete
     */
    public MapManager(Activity activity, MapFragment map, SearchView atvLocation) {
        mapFragment = map;
        this.activity = activity;
        new GeoAutoComplete(this, activity, atvLocation);
        initMap(true);
    }

    /**
     * Default constructor .
     */
    public MapManager(Activity activity, MapFragment map, boolean focusOnCurrent) {
        mapFragment = map;
        this.activity = activity;
        initMap(focusOnCurrent);
    }


    /**
     * Common initialization
     *
     * @param focusOnCurrent
     */
    private void initMap(final boolean focusOnCurrent) {

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, activity, requestCode);
            dialog.show();
        }
        if (map == null) {
            map = mapFragment.getMap();
            if (map == null) {
                Log.e(TAG,"Creating map failure");
            }
            markerMap = new HashMap<Marker, Integer>();

            mapHeight = 0;
            mapWidth = 0;

            // update the zoom parameters when the view is created
            ViewTreeObserver vto = mapFragment.getView().getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mapFragment.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mapWidth = mapFragment.getView().getWidth();
                        mapHeight = mapFragment.getView().getHeight();
                        if (zoomUpdate != null) {
                            try {
                                zoomUpdate.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }
        }
        locationFinder = new LocationFinder(activity, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Log.d(TAG, "LocationFinder updated");
                if (focusOnCurrent)
                    onLocationChanged(locationFinder.getCurrentLocation());
                return null;
            }
        });
        locationFinder.startLocation();
        map.setMyLocationEnabled(true);
        offsetX = 0;
        offsetY = 0;
        zoomLevel = DEFAULT_ZOOM;
        if (focusOnCurrent)
            focusOnCurrentLocation();


    }

    /**
     * Set callback for on marker click
     *
     * @param indexCallback the callback method
     */
    public void setMarkerCallback(GeoUtils.IndexCallback indexCallback) {
        this.indexCallback = indexCallback;
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // move camera to the current location
                map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                MapManager.this.indexCallback.executeCallback(markerMap.get(marker));

                return true;
            }
        });
    }

    /**
     * Set general purpose on map click
     *
     * @param onMapClick the callback method
     */
    public void setOnMapClick(final Callable onMapClick) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    onMapClick.call();
                } catch (Exception e) {
                    Log.e(TAG, "OnMapClick exception" + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Single time focus
     * @see LocationFinder#updateLocation()
     */
    public void focusOnCurrentLocation() {

        locationFinder.updateLocation();
    }

    /**
     * focus and keep tracking
     *
     * @see com.geofind.geofind.geoutils.LocationFinder#startPeriodicUpdates(long, long)
     */
    public void focusOnCurrentLocation(long updateInterval, long fastestInterval) {
        locationFinder.startPeriodicUpdates(updateInterval, fastestInterval);
    }

    /**
     * Stop tracking current location
     */
    public void stopTrackCurrentLocation() {
        locationFinder.stopPeriodicUpdates();
    }

    /**
     * show or hide the location button
     */
    public void showMyLocationButton(boolean show) {
        map.getUiSettings().setMyLocationButtonEnabled(show);
    }

    /**
     * show or hide the zoom buttons
     */
    public void showZoomButton(boolean show) {
        map.getUiSettings().setZoomControlsEnabled(show);
    }

    /**
     * disable interactive features
     */
    public void setStaticMapMode() {
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);


    }

    /**
     * enable the adding of markers by user click to the map
     *
     * @param onlyOne enable only one marker at a time
     */
    public void enableMarkers(final boolean onlyOne) {

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerOptions = new MarkerOptions();

                markerOptions.position(latLng);
                markerOptions.title("lat: " + latLng.latitude + " lng: " + latLng.longitude);
                selectedPoint = new Point(latLng);

                if (onlyOne) {
                    map.clear();
                }
                Marker marker = map.addMarker(markerOptions);

                new ReverseGeocodingTask(activity.getBaseContext(), marker).execute(latLng);
            }
        });
    }


    /**
     * Add auto generated marker to the map
     *
     * @param location the location of the marker
     */
    public void displayFoundLocation(LatLng location) {
        map.clear(); // Only one marker can be set
        map.addMarker(new MarkerOptions()
                .position(location));
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));

        selectedPoint = new Point(location);
    }


    /**
     * Add a marker to the map
     *
     * @param location the location of the marker
     * @param title    the title to be added to the marker
     * @param state    the state of the hint, influence the color of the marker
     */
    public void setMarker(LatLng location, String title, Hint.State state) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.
                        defaultMarker(state == Hint.State.REVEALED ?
                                BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN)));

        markerMap.put(marker, markerMap.size());
    }

    /**
     * Add offset to the display point, used when occluded by other bars
     */
    public void setMapOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Update the map if the required location to display changed
     *
     * @param location the location to be focused on
     */
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() { // wait for the animation to end so we won't break it
                        map.animateCamera(CameraUpdateFactory.scrollBy(offsetX, offsetY));
                    }

                    @Override
                    public void onCancel() {
                        // do nothing
                    }
                });
    }

    /**
     * A clone of onLocationChanged, except it does not animate the movement to a new point.
     * The main purpose of this function is to overcome the bug of animateCamera when the target
     * point didn't change (in this case, it won't enter the onFinish callback and won't scroll the
     * map - happens when collapsing the panel from an anchor point and the map is already focused).
     *
     * @param location The location to be focused on.
     */
    public void onLocationChangedAnchored(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        map.animateCamera(CameraUpdateFactory.scrollBy(offsetX, offsetY));
    }


    /**
     * Draw a circle on the map
     *
     * @param position center of the circle
     * @param radius   the radius of circle in meters
     */
    public void drawCircle(final LatLng position, final float radius) {
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(position);

        // Radius of the circle
        circleOptions.radius(radius);

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x33aa0000);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        map.addCircle(circleOptions);

        if (mapWidth == 0 || mapHeight == 0) {
            // the map size is not available
            zoomUpdate = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Log.d(TAG, "OnCall W = " + mapWidth + " H = " + mapWidth);
                    zoomLevel = GeoUtils.getBoundsZoomLevel(position, radius / 1000f,
                            mapWidth, mapHeight) - 2;
                    Log.d(TAG, "OnCall Zoom = " + zoomLevel);

                    map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
                    return null;
                }
            };
            // temporary zoom level
            zoomLevel = DEFAULT_ZOOM;
        } else {
            // calculate the zoom level
            zoomLevel = GeoUtils.getBoundsZoomLevel(position, radius / 1000f,
                    mapWidth, mapHeight) - 2;
        }


        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(position.latitude);
        l.setLongitude(position.longitude);

        Log.d("MapManager", "DrawCircle Update ");
        onLocationChangedAnchored(l);
    }

    /**
     *
     * @return the currently selected point
     */
    public Point getSelectedPoint() {
        return selectedPoint;
    }

    /**
     * @see com.geofind.geofind.geoutils.LocationFinder#setRequireLocationEnabled(boolean)
     */
    public void setLocationRequired(boolean locationRequired) {
        locationFinder.setRequireLocationEnabled(locationRequired);
    }

    /**
     * Class for finding an address by location
     */
    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;
        Marker mMarker;

        public ReverseGeocodingTask(Context context, Marker marker) {
            super();
            mContext = context;
            mMarker = marker;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {

            Geocoder geocoder = new Geocoder(mContext);
            double latitude = latLngs[0].latitude;
            double longitude = latLngs[0].longitude;

            List<Address> addresses = null;
            String addressText = "";

            try {
                // find the address
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // compose the address string
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                boolean sbChanged = false;
                if (address.getMaxAddressLineIndex() > 0) {
                    sb.append(address.getAddressLine(0));
                    sbChanged = true;
                }

                if (address.getLocality() != null) {
                    if (sbChanged)
                        sb.append(", ");
                    sb.append(address.getLocality());
                    sbChanged = true;
                }

                if (address.getCountryName() != null) {
                    if (sbChanged)
                        sb.append(", ");
                    sb.append(address.getCountryName());
                }

                addressText = sb.toString();
            }

            return addressText;
        }

        /**
         * Set the found address in the marker title
         */
        @Override
        protected void onPostExecute(String addressText) {
            mMarker.setTitle(addressText);
        }
    }


}
