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

    @Override
    protected void onPreExecute() {
        // Nothing to do here
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
        int zoom = GeoUtils.getBoundsZoomLevel(desc.center, desc.radius, desc.width,desc.height);
        Log.d("StaticMap", "proposed new zoom is " + zoom);

        String address = base_address +
                "center=" + desc.center.latitude + "," + desc.center.longitude + "&" +
                "zoom=" + zoom + "&" + "size=" + desc.width + "x" + desc.height;

        address += "&path=fillcolor:0xAA000033%7Ccolor:0xFFFFFF00%7C" +
                "enc:" + encode(GeoUtils.createCircle(desc.center, desc.radius, 20));
        return address;

    }

    public static class StaticMapDescriptor {
        private LatLng center;
        private float radius;
        private int width, height;

        public StaticMapDescriptor(LatLng center, float radius, int width, int height) {
            this.center = center;
            this.width = width;
            this.height = height;
            this.radius = radius / 1000; // Meters to KiloMeters
        }

    }

    /**
     * Taken from https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    private static String encode(final List<LatLng> path) {
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
