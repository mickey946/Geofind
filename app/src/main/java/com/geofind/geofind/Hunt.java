package com.geofind.geofind;

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

    //TODO Add clues, maps etc.


    public Hunt(String title, float rating, float totalDistance, String description) {
        this.title = title;
        this.rating = rating;
        this.totalDistance = totalDistance;
        this.description = description;
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
}
