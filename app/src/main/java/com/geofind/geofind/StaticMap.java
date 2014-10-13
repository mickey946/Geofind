package com.geofind.geofind;

import android.util.Log;

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

//        ArrayList<LatLng> t = new ArrayList<LatLng>();
//        t.add(new LatLng (31.793555207271424,35.22045135498047));
//        t.add(new LatLng (31.77867180905657,35.229034423828125));
//        t.add(new LatLng (31.756780107186728,35.2166748046875));
//        t.add(new LatLng (31.781006618184914,35.19676208496094));
//        //createCircle(center,radius,90)

        List<LatLng> ee = createCircle(center,radius/1000,10);
        for (LatLng e : ee){
            Log.d("static", "Lat, Lng " + e.latitude + "," + e.longitude);

        }

        address += "&path=fillcolor:0xAA000033%7Ccolor:0xFFFFFF00%7C" +
                "enc:"+encode(ee);


        return address;

    }

    private static List<LatLng> createCircle(LatLng center, float radius, int Details){
        ArrayList<LatLng> perimeter = new ArrayList<LatLng>(360/Details + 1);
        final int Ratio = 6371;
        double lat = (center.latitude * Math.PI) /180f;
        double lng = (center.longitude * Math.PI) /180f;
        double d = radius/Ratio;

        double phi1 = lat;
        double labmda1  = lng;

        for (int i=0; i<=360; i+=Details){
            double bRng = i * Math.PI / 180f;
//            double pLat = Math.asin(Math.sin(lat)*Math.cos(d) + Math.cos(lat) * Math.sin(d)*Math.cos(bRng));
//            double pLng = ((lng + Math.atan2(Math.sin(bRng)*Math.sin(d)*Math.cos(lat),
//                    Math.cos(d)-Math.sin(lat)*Math.sin(pLat)))*180)/Math.PI;


            double phi2 = Math.asin( Math.sin(phi1)*Math.cos(d) +
                                     Math.cos(phi1)*Math.sin(d)*Math.cos(bRng) );
            double lambda2 = labmda1 + Math.atan2(Math.sin(bRng)*Math.sin(d)*Math.cos(phi1),
                    Math.cos(d)-Math.sin(phi1)*Math.sin(phi2));

            //perimeter.add(new LatLng(pLat, pLng));
            perimeter.add(new LatLng(phi2 * 180/Math.PI,lambda2 * 180/ Math.PI));

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
