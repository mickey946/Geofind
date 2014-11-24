package com.geofind.geofind.structures;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Ilia Marin on 08/10/2014.
 */
public class Point implements Serializable {

    private  double _latitude;
    private  double _longitude;

    public Point(double latitude, double longitude) {
        _latitude = latitude;
        _longitude = longitude;
    }

    public Point(LatLng latLng) {
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
    }

    public Point(Location location) {
        _latitude = location.getLatitude();
        _longitude = location.getLongitude();
    }

    public Point(ParseGeoPoint parseGeoPoint) {
        _latitude = parseGeoPoint.getLatitude();
        _longitude = parseGeoPoint.getLongitude();
    }

    public double getLatitude() {
        return _latitude;
    }

    public double getLongitude() {
        return _longitude;
    }

    public LatLng toLatLng(){
        return  new LatLng(_latitude,_longitude);
    }

    public Location toLocation(){
        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(_latitude);
        l.setLongitude(_longitude);
        return  l;
    }

    public ParseGeoPoint toParseGeoPoint() {
        return new ParseGeoPoint(_latitude, _longitude);
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
