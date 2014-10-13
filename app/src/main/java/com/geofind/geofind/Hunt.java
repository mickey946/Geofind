package com.geofind.geofind;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

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
    private SerializableLatLng firstPoint; // TODO discuss if we need to change to Point

    /**
     * The radius in meters of the hunt.
     */
    private Float radius;

    /**
     * The hints of this hunt.
     */
    private ArrayList<Hint> hints;

    /**
     * The creator Google user ID.
     */
    private String creatorID; // TODO change to the appropriate type

    /**
     * Constructor for HuntListActivity.
     */
    public Hunt(String title, float rating, float totalDistance, String description,
                LatLng firstPoint, float radius) {
        this.title = title;
        this.rating = rating;
        this.totalDistance = totalDistance;
        this.description = description;
        this.firstPoint = new SerializableLatLng(firstPoint);
        this.radius = radius;

        // TODO discuss if we need to add the creator name in the preview
    }

    /**
     * Constructor for CreateHuntActivity.
     */
    public Hunt(String title, String description, ArrayList<Hint> hints, String creatorID) {
        this.title = title;
        this.description = description;
        this.hints = hints;
        this.creatorID = creatorID;

        // TODO calculate total distance and radius
        // TODO assign first point from the Hints list
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
        return firstPoint.getLocation();
    }

    public Float getRadius() {
        return radius;
    }

    public class SerializableLatLng implements Serializable {
        // mark it transient so defaultReadObject()/defaultWriteObject() ignore it
        private transient com.google.android.gms.maps.model.LatLng mLocation;

        public SerializableLatLng(LatLng location) {
            mLocation = location;
        }

        public LatLng getLocation() {
            return mLocation;
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
