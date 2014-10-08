package com.geofind.geofind;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by Ilia Merin on 05/10/2014.
 */
public class MapManager implements LocationListener {

    private TextView _tvLocation;
    private MapFragment _mapFragment;
    private GoogleMap _mMap;
    private Activity _activity;



    protected MarkerOptions markerOptions;

    public MapManager(Activity activity, MapFragment map, TextView tvLocation) {
        _activity = activity;
        _tvLocation = tvLocation;
        _mapFragment = map;
        initMap();
    }

    public MapManager(Activity activity, MapFragment map) {
        _tvLocation = null;
        _mapFragment = map;
        _activity = activity;
        initMap();
    }


    private void initMap() {

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(_activity.getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, _activity, requestCode);
            dialog.show();
        }
        if (_mMap == null) {
            _mMap = _mapFragment.getMap();
            if (_mMap == null)
                Toast.makeText(_activity, "Error creating map", Toast.LENGTH_LONG);
        }
        _mMap.setMyLocationEnabled(true);
        focusOnCurrentLocation();


    }

    //Single time focus
    public void focusOnCurrentLocation(){
        focusOnCurrentLocation(-1, -1);
    }

    // focus and keep tracking
    public void focusOnCurrentLocation(long minTime, float minDistance) {

        LocationManager locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria,true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null){
            onLocationChanged(location);
        }

        if (minTime>=0 && minDistance>=0)
            locationManager.requestLocationUpdates(provider,minTime,minDistance,this);

    }

    public void showMyLocationButton(boolean show){
        _mMap.getUiSettings().setMyLocationButtonEnabled(show);
    }

    public void showZoomButton(boolean show){
        _mMap.getUiSettings().setZoomControlsEnabled(show);
    }

    public void setStaticMapMode (){
        _mMap.getUiSettings().setMyLocationButtonEnabled(false);
        _mMap.getUiSettings().setAllGesturesEnabled(false);



    }

    public void enableMarkers(final boolean onlyOne){

        _mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerOptions = new MarkerOptions();

                markerOptions.position(latLng);
                markerOptions.title("lat: " + latLng.latitude + " lng: " + latLng.longitude);

                new ReverseGeocodingTask(_activity.getBaseContext(),onlyOne).execute(latLng);
                //   mMap.clear();
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.addMarker(markerOptions);
            }
        });
    }

    public void setMarker(Location location, String title,Hint.State state){


        _mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(),location.getLongitude()))
                .title(title)
                .icon(BitmapDescriptorFactory.
                        defaultMarker(state == Hint.State.REVEALED ?
                                BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        _mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        _mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if (_tvLocation != null)
            _tvLocation.setText("Lat: " + latitude + " Long:" + longitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void drawCircle (LatLng position, float radius){
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
        circleOptions.fillColor(0x5500ff00);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        _mMap.addCircle(circleOptions);

        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(position.latitude);
        l.setLongitude(position.longitude);

        onLocationChanged(l);
    }

    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;
        boolean mOnlyOne;

        public ReverseGeocodingTask(Context context, boolean onlyOne) {
            super();
            mContext = context;
            mOnlyOne = onlyOne;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = latLngs[0].latitude;
            double longitude = latLngs[0].longitude;

            List<Address> addresses = null;
            String addressText = "";

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            markerOptions.title(addressText);
            if (mOnlyOne){
                _mMap.clear();
            }
            _mMap.addMarker(markerOptions);
            Toast.makeText(mContext, "Created " + addressText, Toast.LENGTH_LONG);
        }
    }

}
