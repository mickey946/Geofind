package com.geofind.geofind;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Ilia Marin on 08/10/2014.
 */


//// Tzafrir you can add here what you need for Parse too.

public class Point implements Serializable { // TODO extend ParseGeoPoint

    private  double _latitude;
    private  double _longitude;

    Point(double latitude, double longitude){
        _latitude = latitude;
        _longitude = longitude;
    }

    Point(LatLng latLng){
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
    }

    Point(Location location){
        _latitude = location.getLatitude();
        _longitude = location.getLongitude();
    }

    public double get_latitude() {
        return _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }

    public LatLng toLatLng(){
        return  new LatLng(_latitude,_longitude);
    }

    // Not sure if we need it, it is obsolete
    public Location toLocation(){
        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(_latitude);
        l.setLongitude(_longitude);
        return  l;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(_latitude);
        out.writeDouble(_longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        _latitude = in.readDouble();
        _longitude = in.readDouble();
    }
}
