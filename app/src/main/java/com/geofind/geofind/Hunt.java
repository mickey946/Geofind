package com.geofind.geofind;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by mickey on 01/10/14.
 */
public class Hunt implements Serializable {

    /**
     * Title of the hunt.
     */
    private String title;

    /**
     * Rating of the hunt. Must be between 0 and 5.
     */
    private Float rating;

    /**
     * Total distance of the hunt. Calculated by summing the aerial distance between each two
     * successive points.
     */
    private Float totalDistance;

    /**
     * Description of the hunt.
     */
    private String description;

    /**
     * The center point of the hunt.
     */
    private SerializableLatLng centerPossition;

    /**
     * The radius in meters of the hunt.
     */
    private Float radius;

    //TODO Add clues, maps etc.


    public Hunt(String title, float rating, float totalDistance, String description, LatLng centerPossition, float radius) {
        this.title = title;
        this.rating = rating;
        this.totalDistance = totalDistance;
        this.description = description;
        this.centerPossition = new SerializableLatLng( centerPossition);
        this.radius = radius;
    }

    public String getTitle() {
        return title;
    }

    public Float getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public Float getTotalDistance() {
        return totalDistance;
    }

    public LatLng getCenterPosition() {
        return centerPossition.getLocation();
    }

    public Float getRadius() {
        return radius;
    }
    public class SerializableLatLng implements Serializable {
        // mark it transient so defaultReadObject()/defaultWriteObject() ignore it
        private transient com.google.android.gms.maps.model.LatLng mLocation;

        public SerializableLatLng(LatLng location){
            mLocation = location;
        }

        public  LatLng getLocation(){
            return  mLocation;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeDouble(mLocation.latitude);
            out.writeDouble(mLocation.longitude);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            mLocation = new LatLng(in.readDouble(), in.readDouble());
        }
    }
}
