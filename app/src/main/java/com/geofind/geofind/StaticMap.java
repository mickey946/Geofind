package com.geofind.geofind;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ilia Merin on 11/10/2014.
 */
public class StaticMap {



    private final static String base_address;

    static {
        base_address = "https://maps.googleapis.com/maps/api/staticmap?";
    }


    public static String composeAddress (LatLng center, float radius ,
                                         int width, int height,
                                         int zoom){
        String address =  base_address +
                "center=" + center.latitude + "," + center.longitude +"&"+
                "zoom=" + zoom + "&" + "size="+width+"x"+height;

        address += "&path=fillcolor:0xAA000033%7Ccolor:0xFFFFFF00%7C\n" +
                "enc:"+encode(createCircle(center,radius,8));


        return address;

    }

    private static List<LatLng> createCircle(LatLng center, float radius, int Details){
        ArrayList<LatLng> perimeter = new ArrayList<LatLng>(360/Details + 1);
        final int Ratio = 6371;
        double lat = (center.latitude * Math.PI) /180f;
        double lng = (center.longitude * Math.PI) /180f;
        double d = radius/Ratio;

        for (int i=0; i<=360; i+=Details){
            double bRng = i * Math.PI / 180f;
            double pLat = Math.asin(Math.sin(lat)*Math.cos(d) + Math.cos(lat) * Math.sin(d)*Math.cos(bRng));
            double pLng = ((lng + Math.atan2(Math.sin(bRng)*Math.sin(d)*Math.cos(lat),
                    Math.cos(d)-Math.sin(lat)*Math.sin(pLat)))*180)/Math.PI;
            perimeter.add(new LatLng(pLat, pLng));

        }

        return perimeter;
    }


    /**
     * Taken from https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    public static String encode(final List<LatLng> path) {
        long lastLat = 0;
        long lastLng = 0;
        final StringBuffer result = new StringBuffer();
        for (final LatLng point : path) {
            long lat = Math.round(point.latitude * 1e5);
            long lng = Math.round(point.longitude * 1e5);
            long dLat = lat - lastLat;
            long dLng = lng - lastLng;
            encode(dLat, result);
            encode(dLng, result);
            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }
    private static void encode(long v, StringBuffer result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }

}
