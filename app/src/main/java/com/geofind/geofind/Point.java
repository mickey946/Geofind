package com.geofind.geofind;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Ilia Marin on 08/10/2014.
 */

@ParseClassName("Point")
public class Point extends ParseObject implements Serializable { // TODO extend ParseGeoPoint?

    /**
     * Zero arg constructor. required by Parse.
     */
    public Point() {
    }

    public void initialize(double latitude, double longitude) {
        put("latitude", latitude);
        put("longitude", longitude);
    }

    public LatLng toLatLng() {
        return new LatLng((Double) get("latitude"), (Double) get("longitude"));
    }

    public Double get_latitude() {
        return (Double) get("latitude");
    }

    public Double get_longitude() {
        return (Double) get("longitude");
    }

    // Not sure if we need it, it is obsolete
    public Location toLocation() {
        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude((Double) get("latitude"));
        l.setLongitude((Double) get("longitude"));
        return l;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble((Double) get("latitude"));
        out.writeDouble((Double) get("longitude"));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        put("latitude", in.readDouble());
        put("longitude", in.readDouble());
    }

}
