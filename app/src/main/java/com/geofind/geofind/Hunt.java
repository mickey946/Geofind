package com.geofind.geofind;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mickey on 01/10/14.
 */
@ParseClassName("Hunt")
public class Hunt extends ParseObject implements Serializable {

    /**
     * Zero arg constructor. required by Parse.
     */
    public Hunt() {
    }

    public void initialize(String title, String description, String creatorID, ArrayList<Hint> hints) {
        put("title", title);
        put("description", description);
        put("creatorID", creatorID);
        put("hints", hints);
        put("rating", 0);
        put("firstPoint", hints.get(0).getLocation());
        put("comments", new ArrayList<Comment>());
        //TODO radius, totalDistance

    }

    public String getTitle() {
        return getString("title");
    }

    public Float getRating() {
        return (Float) get("rating");
    }

    public String getDescription() {
        return getString("description");
    }

    public Float getTotalDistance() {
        return (Float) get("totalDistance");
    }

    public LatLng getCenterPosition() {
        return ((Point) get("firstPoint")).toLatLng();
    }

    public Float getRadius() {
        return (Float) get("radius");
    }

}
