package com.geofind.geofind;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ilia Merin on 16/10/2014.
 */
public abstract class GeoUtils {

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

    private static double latRad(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
    }

    private static double zoom(double mapPx, double worldPx, double fraction) {
        final double LN2 = .693147180559945309417;
        return (Math.log(mapPx / worldPx / fraction) / LN2);
    }

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

    public static List<LatLng> createCircle(LatLng center, float radius, int Details) {
        ArrayList<LatLng> perimeter = new ArrayList<LatLng>(360 / Details + 1);

        double lat = (center.latitude * Math.PI) / 180f;
        double lng = (center.longitude * Math.PI) / 180f;


        for (int i = 0; i <= 360; i += Details) {
            LatLng newPoint = calcPointOnArc(lat, lng, radius, i);

            perimeter.add(newPoint);

        }

        return perimeter;
    }


}
