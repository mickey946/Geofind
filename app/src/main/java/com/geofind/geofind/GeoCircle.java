package com.geofind.geofind;

/**
* Created by Ilia Marin on 23/10/2014.
*/
public class GeoCircle {
    private Point center;
    private float radius;

    public GeoCircle(Point center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    public float getRadius() {
        return radius;
    }
}
