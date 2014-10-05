package com.geofind.geofind;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class SplashScreenActivity extends Activity implements LocationListener {

    private GoogleMap mMap;
    protected MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS){
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,this,requestCode);
            dialog.show();
        }
        if (mMap == null)
        {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap == null)
                Toast.makeText(this,"Error creating map",Toast.LENGTH_LONG);
        }
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria,true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null){
            onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(provider,20000,0,this);

        mMap.setOnMapClickListener( new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerOptions = new MarkerOptions();

                markerOptions.position(latLng);
                markerOptions.title("lat: " + latLng.latitude + " lng: " + latLng.longitude );

                new ReverseGeocodingTask(getBaseContext()).execute(latLng);
              //   mMap.clear();
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.addMarker(markerOptions);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap == null)
        {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap == null)
                Toast.makeText(this,"Error creating map",Toast.LENGTH_LONG);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView tvLocation = (TextView) findViewById(R.id.tv_location);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude,longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        tvLocation.setText("Lat: " + latitude + " Long:" + longitude);
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

    private class ReverseGeocodingTask extends AsyncTask<LatLng,Void,String> {
        Context mContext;

        public ReverseGeocodingTask(Context context){
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = latLngs[0].latitude;
            double longitude = latLngs[0].longitude;

            List<Address> addresses=null;
            String addressText="";

            try {
                addresses = geocoder.getFromLocation(latitude,longitude,1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses!=null && addresses.size()>0){
                Address address = addresses.get(0);
                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0):"",
                        address.getLocality(),
                        address.getCountryName());
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            markerOptions.title(addressText);
            mMap.addMarker(markerOptions);
            Toast.makeText(mContext,"Created " + addressText,Toast.LENGTH_LONG);
        }
    }

}
