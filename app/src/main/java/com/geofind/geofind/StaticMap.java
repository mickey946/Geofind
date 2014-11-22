package com.geofind.geofind;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * This class loads a static google map to an ImageView object
 * Created by Ilia Marin on 11/10/2014.
 */
public class StaticMap extends AsyncTask<StaticMap.StaticMapDescriptor, Void, Bitmap> {


    /**
     * The google maps api address
     */
    private final static String base_address = "https://maps.googleapis.com/maps/api/staticmap?";
    private static final int RESOLUTION = 20;
    private static final String CIRCLE_FILL_COLOR = "0xAA000033";
    private static final String CIRCLE_BORDER_COLOR = "0xFFFFFF00";

    /**
     * The image view for output
     */
    private ImageView _view;

    /**
     * The progress bar that is visible till the image loads
     */
    private ProgressBar progressBar;

    public StaticMap(ImageView view, ProgressBar progressBar) {
        _view = view;
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        // Nothing to do here
    }

    /**
     * An asynchronous method that downloads the map
     *
     * @param staticMapDescriptors the parameters of the map
     * @return the image of the map
     */
    @Override
    protected Bitmap doInBackground(StaticMapDescriptor... staticMapDescriptors) {
        try {
            // generate the path to the map
            URL url = new URL(composeAddress(staticMapDescriptors[0]));
            // download the map
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set the downloaded image to ImageView
     *
     * @param bitmap the downloaded image
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        progressBar.setVisibility(View.GONE);
        _view.setVisibility(View.VISIBLE);
        _view.setImageBitmap(bitmap);
    }

    /**
     * compose the URL address from which to download the map
     */
    protected String composeAddress(StaticMapDescriptor desc) {


        // compose url of the map
        String address = base_address + "size=" + desc.width + "x" + desc.height;

        switch (desc.mapElement) {
            case None:
                break;
            case Circle:
                // Calculate zoom level
                int zoom = GeoUtils.getBoundsZoomLevel(desc.center, desc.radius, desc.width, desc.height);

                address += "&center=" + desc.center.latitude + "," + desc.center.longitude + "&" +
                        "zoom=" + zoom;


                // Append the circle encoding
                address += "&path=fillcolor:" + CIRCLE_FILL_COLOR +
                        "%7Ccolor:" + CIRCLE_BORDER_COLOR + "%7C" +
                        "enc:" + encode(GeoUtils.createCircle(desc.center, desc.radius, RESOLUTION));
                break;
            case Marker:
                address += "&markers=" + desc.center.latitude + "," + desc.center.longitude;
                break;
        }

        address += "&key=" + GeofindApp.BROWSER_API_KEY;

        return address;

    }

    /**
     * Description of the static map
     */
    public static class StaticMapDescriptor {
        public static enum MapElement {None, Circle, Marker}

        private LatLng center; // the center of the map
        private float radius; // the radius of the circle in meters
        private int width, height; // the resolution of the output image
        private MapElement mapElement;


        public StaticMapDescriptor(int width, int height) {
            this.mapElement = MapElement.None;
            this.width = width;
            this.height = height;
        }

        public StaticMapDescriptor(LatLng center, float radius, int width, int height) {
            this.mapElement = MapElement.Circle;
            this.center = center;
            this.width = width;
            this.height = height;
            this.radius = radius / 1000; // Meters to KiloMeters
        }

        public StaticMapDescriptor(LatLng point, int width, int height) {
            this.mapElement = MapElement.Marker;
            this.center = point;
            this.width = width;
            this.height = height;
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
