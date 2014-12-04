package com.geofind.geofind.geoutils;

import android.content.Context;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.Log;

import com.geofind.geofind.GeofindApp;
import com.geofind.geofind.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ilia Marin on 09/10/2014.
 */
public class GeoAutoComplete {

    public static final String LOG_TAG = GeoAutoComplete.class.getSimpleName();

    /**
     * Definition of the column in the relieved data from google auto-complete
     */
    private static final int DESCRIPTION_COL = 1;
    private static final int REFERENCE_COL = 2;

    /**
     * The {@link android.support.v7.widget.SearchView} for text entering
     */
    private SearchView searchView;

    /**
     * The {@link android.content.Context} which holds the maps
     */
    private Context context;

    /**
     * The {@link com.geofind.geofind.geoutils.MapManager} that will display the searched location
     */
    private MapManager mapManager;


    public GeoAutoComplete(MapManager mapManager, Context context, SearchView searchView) {
        this.mapManager = mapManager;
        this.context = context;
        this.searchView = searchView;
        initAutoCompleteLocation();
    }

    /**
     * Initialize auto-complete object
     */
    private void initAutoCompleteLocation() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (text.length() > 0) {
                    DownloadTask downloadTask = new DownloadTask(DownloadTypes.PLACES);
                    String Url = getAutocompleteUrl(text);
                    downloadTask.execute(Url);
                }
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                android.support.v4.widget.SimpleCursorAdapter adapter =
                        (SimpleCursorAdapter) searchView.getSuggestionsAdapter();
                MatrixCursor row = (MatrixCursor) adapter.getItem(i);

                DownloadTask downloadTask = new DownloadTask(DownloadTypes.PLACES_DETAILS);

                // set the selected auto complete as the text, but do not submit
                String description = row.getString(DESCRIPTION_COL);
                searchView.setQuery(description, false);

                // download the point
                String Url = getPlaceDetailsUrl(row.getString(REFERENCE_COL));
                downloadTask.execute(Url);
                return true;
            }
        });
    }


    /**
     * Compose the URL to request to coordinates of the user-selected place
     *
     * @param place_id the address of the place
     * @return the url to download the coordinates
     */
    private String getPlaceDetailsUrl(String place_id) {
        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=" + GeofindApp.SERVER_API_KEY;

        // reference of place
        String reference = "placeid=" + place_id;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = reference + "&" + sensor + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service

        return "https://maps.googleapis.com/maps/api/place/details/" + output + "?" +
                parameters;
    }

    /**
     * Compose Aut-Complete query URL
     *
     * @param place the place string entered by the user
     * @return the url to send to Google servers
     */
    private String getAutocompleteUrl(String place) {

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=" + GeofindApp.SERVER_API_KEY;

        // place to be be searched
        String input = null; 
        try {
            input = "input=" + URLEncoder.encode(place, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // place type to be searched
        String types = "types=geocode";

        // Building the parameters to the web service
        String parameters = input + "&" + types + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service

        return "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" +
                parameters;
    }

    /**
     * AsyncTask job types
     */
    public enum DownloadTypes {
        /**
         * Download the list for display in auto complete box
         */
        PLACES,

        /**
         * Download the details of the selected place
         */
        PLACES_DETAILS
    }

    /**
     * Asynchronous download command
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        /**
         * The {@link com.geofind.geofind.geoutils.GeoAutoComplete.DownloadTask} of the current task
         */
        DownloadTypes type;

        public DownloadTask(DownloadTypes type) {
            this.type = type;
        }

        @Override
        protected String doInBackground(String... strings) {

            String strUrl = strings[0];

            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;

            try {

                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                // Add buffering of the downloaded data
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                // Compose the download buffer to a single string
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception while downloading url" + e.toString());
            } finally {
                // Close all the opened streams
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return data;

        }

        /**
         * Call to parse object
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.isEmpty()) {
                return;
            }
            switch (type) {
                case PLACES:
                    ParseTask placeParser = new ParseTask(DownloadTypes.PLACES);
                    placeParser.execute(s);
                    break;
                case PLACES_DETAILS:
                    ParseTask detailsParser = new ParseTask(DownloadTypes.PLACES_DETAILS);
                    detailsParser.execute(s);
            }
        }
    }

    /**
     * Asynchronous parsing the downloaded result
     */
    private class ParseTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        /**
         * The {@link com.geofind.geofind.geoutils.GeoAutoComplete.DownloadTypes} of the data to be
         * parsed.
         */
        DownloadTypes parseType;

        public ParseTask(DownloadTypes type) {
            parseType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try {
                jObject = new JSONObject(strings[0]);
                // Display error code
                if (!jObject.getString("status").equals("OK")) {
                    if (jObject.has("error_message"))
                        Log.e(LOG_TAG, "google maps error = " + jObject.getString("error_message"));
                    else
                        Log.e(LOG_TAG, "google maps status " + jObject.getString("status"));
                }

                // Parse received data
                switch (parseType) {
                    case PLACES:
                        PlaceJSONParser placeParser = new PlaceJSONParser();
                        list = placeParser.parse(jObject);
                        break;
                    case PLACES_DETAILS:
                        PlaceDetailsJSONParser placeDetailsJSONParser
                                = new PlaceDetailsJSONParser();
                        list = placeDetailsJSONParser.parse(jObject);
                        break;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> results) {
            switch (parseType) {
                case PLACES:
                    final String key = context.getString(R.string.auto_complete_json_description);
                    final String value = context.getString(R.string.auto_complete_json_place_id);
                    String[] from = new String[]{key};
                    int[] to = new int[]{android.R.id.text1};

                    // Creating a SimpleCursorAdapter for the SearchView
                    MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_id", key, value});
                    for (HashMap<String, String> result : results) {
                        matrixCursor.addRow(new Object[]
                                {result.hashCode(), result.get(key), result.get(value)});
                    }
                    SimpleCursorAdapter adapter =
                            new SimpleCursorAdapter(context, R.layout.simple_list_item_1_white,
                                    matrixCursor, from, to, 0);

                    // Setting the adapter
                    searchView.setSuggestionsAdapter(adapter);

                    // update the auto-complete list
                    adapter.notifyDataSetChanged();
                    break;

                case PLACES_DETAILS:
                    HashMap<String, String> hm = results.get(0);

                    // Getting latitude from the parsed data
                    double latitude = Double.parseDouble(hm.get("lat"));

                    // Getting longitude from the parsed data
                    double longitude = Double.parseDouble(hm.get("lng"));

                    // display the parsed location on the map
                    mapManager.displayFoundLocation(new LatLng(latitude, longitude));
                    break;
            }
        }
    }
}
