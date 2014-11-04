package com.geofind.geofind;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ilia Marin on 16/10/2014.
 * <p/>
 * This file contains general usage geographical calculation function
 */
public abstract class GeoUtils {

    /**
     * Calculates the zoom level for static maps
     *
     * @param center the center of the map
     * @param radius the required radius around the center in KiloMeters
     * @param width  the width of the display rectangle
     * @param height the height of the display rectangle
     * @return the required zoom level
     */
    public static int getBoundsZoomLevel(LatLng center, float radius,
                                         int width, int height) {
        final int GLOBE_WIDTH = 256; // a constant in Google's map projection
        final int ZOOM_MAX = 21;


        double lat = center.latitude * Math.PI / 180f,
                lng = center.longitude * Math.PI / 180f;


        LatLng northeast = calcPointOnArc(lat, lng, radius, 45);
        LatLng southwest = calcPointOnArc(lat, lng, radius, 225);


        double latFraction = (latRad(northeast.latitude) - latRad(southwest.latitude)) / Math.PI;
        double lngDiff = northeast.longitude - southwest.longitude;
        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;
        double latZoom = zoom(height, GLOBE_WIDTH, latFraction);
        double lngZoom = zoom(width, GLOBE_WIDTH, lngFraction);
        double zoom = Math.min(Math.min(latZoom, lngZoom), ZOOM_MAX);
        return (int) (zoom);
    }

    /**
     * Converts latitude from degree to radians
     */
    private static double latRad(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
    }

    /**
     * Calculate the zoom level (formula from GoogleMap website)
     *
     * @param mapPx    map dimension in pixels
     * @param worldPx  the world dimension in pixels as given by Google
     * @param fraction the part of the world to display
     * @return the required zoom level
     */
    private static double zoom(double mapPx, double worldPx, double fraction) {
        final double LN2 = .693147180559945309417;
        return (Math.log(mapPx / worldPx / fraction) / LN2);
    }

    /**
     * Calculate the coordinates of a point the located at specific angle on a distance from
     * specified point.
     *
     * @param lat    the latitude of the center
     * @param lng    the longitude of the center
     * @param radius the distance in KM
     * @param angle  the required angle in degrees
     * @return the request point
     */
    public static LatLng calcPointOnArc(double lat, double lng, float radius, int angle) {
        final int Ratio = 6371;
        double d = radius / Ratio;
        double bRng = angle * Math.PI / 180f;

        double resLat = Math.asin(Math.sin(lat) * Math.cos(d) +
                Math.cos(lat) * Math.sin(d) * Math.cos(bRng));
        double resLng = lng + Math.atan2(Math.sin(bRng) * Math.sin(d) * Math.cos(lat),
                Math.cos(d) - Math.sin(lat) * Math.sin(resLat));

        return new LatLng(resLat * 180 / Math.PI, resLng * 180 / Math.PI);
    }

    /**
     * Creates a list of points which are located on a circle around the center
     *
     * @param center     the center of the circle
     * @param radius     the radius around the center in KiloMeters
     * @param resolution the maximal distance in degrees between two consecutive points
     * @return the list of the point on the circle
     */
    public static List<LatLng> createCircle(LatLng center, float radius, int resolution) {
        ArrayList<LatLng> perimeter = new ArrayList<LatLng>(360 / resolution + 1);

        double lat = (center.latitude * Math.PI) / 180f;
        double lng = (center.longitude * Math.PI) / 180f;


        for (int i = 0; i <= 360; i += resolution) {
            LatLng newPoint = calcPointOnArc(lat, lng, radius, i);

            perimeter.add(newPoint);

        }

        return perimeter;
    }

    /**
     * Returns the air-path distance in meters between the
     * point in the path.
     * Distance is defined using  the WGS84 ellipsoid.
     *
     * @param hints the hints that hold the points of the path
     * @return the approximate distance in meters
     */
    public static float calcPathLength(List<Hint> hints) {
        float len = 0;
        for (int i = 0; i < hints.size() - 1; i++) {
            len += hints.get(i).getLocation().toLocation().distanceTo(hints.get(i + 1).getLocation().toLocation());
        }

        return len;
    }


    /**
     * Calculate the maximal distance from the starting point in meters.
     *
     * @param hints the list of hints that holds the points of the path
     * @return the approximate distance in meters
     */
    public static float calcRadius(List<Hint> hints) {
        float len = 0;
        Location startPoint = hints.get(0).getLocation().toLocation();
        for (Hint h : hints) {
            len = Math.max(len, startPoint.distanceTo(h.getLocation().toLocation()));
        }
        return len;
    }

    public static float calcDistance(Location currentLocation, Point p) {
        return currentLocation.distanceTo(p.toLocation());
    }
}
