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
 * An object which represents a point in a Geofind hunt.
 */
public class Point implements Serializable {

    /**
     * The latitude of this point.
     */
    private  double _latitude;

    /**
     * The longitude of this point.
     */
    private  double _longitude;

    /**
     * Constructor.
     */
    public Point(double latitude, double longitude) {
        _latitude = latitude;
        _longitude = longitude;
    }

    /**
     * Constructor.
     */
    public Point(LatLng latLng) {
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
    }

    /**
     * Constructor.
     */
    public Point(Location location) {
        _latitude = location.getLatitude();
        _longitude = location.getLongitude();
    }

    /**
     * Constructor.
     */
    public Point(ParseGeoPoint parseGeoPoint) {
        _latitude = parseGeoPoint.getLatitude();
        _longitude = parseGeoPoint.getLongitude();
    }

    /**
     * Returns this point's latitude.
     *
     * @return - this point's latitude.
     */
    public double getLatitude() {
        return _latitude;
    }

    /**
     * Returns this point's longitude.
     *
     * @return - this point's longitude.
     */
    public double getLongitude() {
        return _longitude;
    }

    /**
     * returns a {@link com.google.android.gms.maps.model.LatLng} object representation of this point.
     * @return - a {@link com.google.android.gms.maps.model.LatLng} object representation of this point.
     */
    public LatLng toLatLng(){
        return  new LatLng(_latitude,_longitude);
    }

    /**
     * Returns a {@link android.location.Location} object representation of this point.
     * @return - a {@link android.location.Location} object representation of this point.
     */
    public Location toLocation(){
        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(_latitude);
        l.setLongitude(_longitude);
        return  l;
    }

    /**
     * Returns a {@link com.parse.ParseGeoPoint} representation of this point.
     * @return - a {@link com.parse.ParseGeoPoint} representation of this point.
     */
    public ParseGeoPoint toParseGeoPoint() {
        return new ParseGeoPoint(_latitude, _longitude);
    }

    /**
     * For implementing serializable.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(_latitude);
        out.writeDouble(_longitude);
    }

    /**
     * For implementing Serializable.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        _latitude = in.readDouble();
        _longitude = in.readDouble();
    }

}
