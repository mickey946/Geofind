package com.geofind.geofind;

import java.io.Serializable;

/**
 * Created by mickey on 04/10/14.
 */
public class Hint implements Serializable {
    private String title;
    private String description;

    // TODO add picture, recording and video


    public Hint(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }
}
