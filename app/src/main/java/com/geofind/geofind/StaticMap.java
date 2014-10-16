package com.geofind.geofind;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ilia Merin on 11/10/2014.
 */
public class StaticMap extends AsyncTask<StaticMap.StaticMapDescriptor, Void, Bitmap> {


    private final static String base_address;
    private ImageView _view;

    static {
        base_address = "https://maps.googleapis.com/maps/api/staticmap?";
    }

    public StaticMap(ImageView view) {
        _view = view;

    }

    public static int getBoundsZoomLevel(LatLng northeast, LatLng southwest,
                                         int width, int height) {
        final int GLOBE_WIDTH = 256; // a constant in Google's map projection
        final int ZOOM_MAX = 21;
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

    private static List<LatLng> createCircle(LatLng center, float radius, int Details) {
        ArrayList<LatLng> perimeter = new ArrayList<LatLng>(360 / Details + 1);

        double lat = (center.latitude * Math.PI) / 180f;
        double lng = (center.longitude * Math.PI) / 180f;


        for (int i = 0; i <= 360; i += Details) {
            LatLng newPoint = calcPointOnArc(lat, lng, radius, i);

            perimeter.add(newPoint);

        }

        return perimeter;
    }

    private static LatLng calcPointOnArc(double lat, double lng, float radius, int angle) {
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

    @Override
    protected void onPreExecute() {
        if (_view.getWidth() == 0 || _view.getHeight() == 0) {
            Log.w("StaticMap", "image view size is 0");
        }
    }

    @Override
    protected Bitmap doInBackground(StaticMapDescriptor... staticMapDescriptors) {
        String address = composeAddress(staticMapDescriptors[0]);
        try {
            URL url = new URL(address);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        _view.setImageBitmap(bitmap);
    }

    protected String composeAddress(StaticMapDescriptor desc) {
        int Details = 25;


//        final float GLOBE_WIDTH = 256;

        double lat = desc.center.latitude * Math.PI / 180f,
                lng = desc.center.longitude * Math.PI / 180f;
//        double d = desc.radius/6371;


//        double resLat = Math.asin( Math.sin(lat)*Math.cos(d)  );
//        double resLon1 = lng + Math.atan2(Math.sin(d)*Math.cos(lat),
//                Math.cos(d)-Math.sin(lat)*Math.sin(resLat));
//        double resLon2 = lng + Math.atan2((-1)*Math.sin(d)*Math.cos(lat),
//                Math.cos(d)-Math.sin(lat)*Math.sin(resLat));


        LatLng ne = calcPointOnArc(lat, lng, desc.radius, 45);
        LatLng sw = calcPointOnArc(lat, lng, desc.radius, 225);

//        double angle = resLon1 - resLon2;
//        angle = angle ;// * 180 / Math.PI;

//        Log.d("StaticMap","angle = " + angle);
//        if (angle < 0) {
//            angle += 360;
//        }

//        int z = (int) Math.round(Math.log(_view.getWidth() *360 / angle / GLOBE_WIDTH ) / Math.log(2));
//        Log.d("StaticMap", "proposed zoom is " + z);

        int z = getBoundsZoomLevel(ne, sw, _view.getWidth(), _view.getHeight());
        Log.d("StaticMap", "proposed new zoom is " + z);

        String address;
        //do {
        address = base_address +
                "center=" + desc.center.latitude + "," + desc.center.longitude + "&" +
                "zoom=" + z + "&" + "size=" + _view.getWidth() + "x" + _view.getHeight();


        address += "&path=fillcolor:0xAA000033%7Ccolor:0xFFFFFF00%7C" +
                "enc:" + encode(createCircle(desc.center, desc.radius, 20));
        Details += 5;
        //} while (address.length() < 2048);

        return address;

    }

    public static class StaticMapDescriptor {
        private LatLng center;
        private float radius;

        public StaticMapDescriptor(LatLng center, float radius /*,int width, int height, int zoom*/) {
            this.center = center;
            this.radius = radius / 1000;
        }

    }

}
