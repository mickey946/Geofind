package com.geofind.geofind;

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
import android.widget.Toast;

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

    public static final String LOG_TAG = "MapManager";

    private static final int DEFAULT_ZOOM = 17;

    // Markers
    protected MarkerOptions markerOptions;
    // Visualize objects
    private SearchView _atvLocation;
    private MapFragment _mapFragment;
    private Activity _activity;
    // display parameters
    private float _offsetX, _offsetY;
    private int _zoomLevel;
    private int _mapWidth, _mapHeight;
    // zoom handling object
    private Callable<Void> _zoomUpdate;
    private IndexCallback _indexCallback;
    private HashMap<Marker, Integer> _markerMap;

    // Google map interface object
    private GoogleMap _mMap;
    private Point _selectedPoint;

    //Current location provider
    private LocationFinder _locationFinder;

    /**
     * Map Manager constructor for usage with AutoComplete
     */
    public MapManager(Activity activity, MapFragment map, SearchView atvLocation) {
        _mapFragment = map;
        _activity = activity;
        _atvLocation = atvLocation;
        new GeoAutoComplete(this, activity, atvLocation);
        initMap(true);
    }

    /**
     * Default constructor .
     */
    public MapManager(Activity activity, MapFragment map, boolean focusOnCurrent) {
        _mapFragment = map;
        _activity = activity;
        initMap(focusOnCurrent);
    }


    /**
     * Common initialization
     * @param focusOnCurrent
     */
    private void initMap(final boolean focusOnCurrent) {

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(_activity.getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, _activity, requestCode);
            dialog.show();
        }
        if (_mMap == null) {
            _mMap = _mapFragment.getMap();
            if (_mMap == null) {
                Toast.makeText(_activity, "Error creating map", Toast.LENGTH_LONG).show();
            }
            _markerMap = new HashMap<Marker, Integer>();

            _mapHeight = 0;
            _mapWidth = 0;

            // update the zoom parameters when the view is created
            ViewTreeObserver vto = _mapFragment.getView().getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        _mapFragment.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        _mapWidth = _mapFragment.getView().getWidth();
                        _mapHeight = _mapFragment.getView().getHeight();
                        if (_zoomUpdate != null) {
                            try {
                                _zoomUpdate.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }
        }
        _locationFinder = new LocationFinder(_activity, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Log.d("MapManager","LocationFinder updated");
                if (focusOnCurrent)
                    onLocationChanged(_locationFinder.getCurrentLocation());
                return null;
            }
        });
        _locationFinder.startLocation();
        _mMap.setMyLocationEnabled(true);
        _offsetX = 0;
        _offsetY = 0;
        _zoomLevel = DEFAULT_ZOOM;
        if (focusOnCurrent)
        focusOnCurrentLocation();


    }

    /**
     * Set callback for on marker click
     *
     * @param indexCallback the callback method
     */
    public void setMarkerCallback(IndexCallback indexCallback) {
        _indexCallback = indexCallback;
        _mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // move camera to the current location
                _mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                _indexCallback.executeCallback(_markerMap.get(marker));

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
        _mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    onMapClick.call();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "OnMapClick exception" + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }


    //Single time focus
    public void focusOnCurrentLocation() {

        _locationFinder.updateLocation();
    }

    // focus and keep tracking
    public void focusOnCurrentLocation(long updateInterval, long fastestInterval) {
        _locationFinder.startPeriodicUpdates(updateInterval, fastestInterval);
    }

    public void stopTrackCurrentLocation() {
        _locationFinder.stopPeriodicUpdates();
    }

    // show or hide the location button
    public void showMyLocationButton(boolean show) {
        _mMap.getUiSettings().setMyLocationButtonEnabled(show);
    }

    // show or hide the zoom buttons
    public void showZoomButton(boolean show) {
        _mMap.getUiSettings().setZoomControlsEnabled(show);
    }

    // disable interactive features
    public void setStaticMapMode() {
        _mMap.getUiSettings().setMyLocationButtonEnabled(false);
        _mMap.getUiSettings().setAllGesturesEnabled(false);


    }

    /**
     * enable the adding of markers by user click to the map
     *
     * @param onlyOne enable only one marker at a time
     */
    public void enableMarkers(final boolean onlyOne) {

        _mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerOptions = new MarkerOptions();

                markerOptions.position(latLng);
                markerOptions.title("lat: " + latLng.latitude + " lng: " + latLng.longitude);
                _selectedPoint = new Point(latLng);

                if (onlyOne) {
                    _mMap.clear();
                }
                Marker marker = _mMap.addMarker(markerOptions);

                new ReverseGeocodingTask(_activity.getBaseContext(), marker).execute(latLng);
            }
        });
    }


    /**
     * Add auto generated marker to the map
     *
     * @param location the location of the marker
     */
    public void displayFoundLocation(LatLng location) {
        _mMap.clear(); // Only one marker can be set
        _mMap.addMarker(new MarkerOptions()
                .position(location));
        _mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        _mMap.animateCamera(CameraUpdateFactory.zoomTo(_zoomLevel));

        _selectedPoint = new Point(location);
    }


    /**
     * Add a marker to the map
     *
     * @param location the location of the marker
     * @param title    the title to be added to the marker
     * @param state    the state of the hint, influence the color of the marker
     */
    public void setMarker(LatLng location, String title, Hint.State state) {
        Marker marker = _mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.
                        defaultMarker(state == Hint.State.REVEALED ?
                                BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN)));

        _markerMap.put(marker, _markerMap.size());
    }

    /**
     * Add offset to the display point, used when occluded by other bars
     */
    public void setMapOffset(float offsetX, float offsetY) {
        _offsetX = offsetX;
        _offsetY = offsetY;
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

        _mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, _zoomLevel),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() { // wait for the animation to end so we won't break it
                        _mMap.animateCamera(CameraUpdateFactory.scrollBy(_offsetX, _offsetY));
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

        _mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, _zoomLevel));
        _mMap.animateCamera(CameraUpdateFactory.scrollBy(_offsetX, _offsetY));
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
        // 0x represents, this is an hexadecimal code
        // 55 represents percentage of transparency. For 100% transparency, specify 00.
        // For 0% transparency ( ie, opaque ) , specify ff
        // The remaining 6 characters(00ff00) specify the fill color
        circleOptions.fillColor(0x33aa0000);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        _mMap.addCircle(circleOptions);

        if (_mapWidth == 0 || _mapHeight == 0) {
            // the map size is not available
            _zoomUpdate = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Log.d(LOG_TAG, "OnCall W = " + _mapWidth + " H = " + _mapWidth);
                    _zoomLevel = GeoUtils.getBoundsZoomLevel(position, radius / 1000f,
                            _mapWidth, _mapHeight) - 2;
                    Log.d(LOG_TAG, "OnCall Zoom = " + _zoomLevel);

                    _mMap.animateCamera(CameraUpdateFactory.zoomTo(_zoomLevel));
                    return null;
                }
            };
            // temporary zoom level
            _zoomLevel = DEFAULT_ZOOM;
        } else {
            // calculate the zoom level
            _zoomLevel = GeoUtils.getBoundsZoomLevel(position, radius / 1000f,
                    _mapWidth, _mapHeight) - 2;
        }


        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(position.latitude);
        l.setLongitude(position.longitude);

        Log.d("MapManager","DrawCircle Update ");
        onLocationChangedAnchored(l);
    }

    public Point get_selectedPoint() {
        return _selectedPoint;
    }

    public void setLocationRequired(boolean locationRequired){
        _locationFinder.set_requireLocationEnabled(locationRequired);
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
         * Set the found address to the text box or marker
         */
        @Override
        protected void onPostExecute(String addressText) {
            mMarker.setTitle(addressText);
        }
    }


}
