package com.geofind.geofind;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ilia Merin on 09/10/2014.
 */
public class GeoAutoComplete {

    private static final String API_KEY = "AIzaSyAtwXqO2w5kV9a8iE-AcbcoI9DWlK0Q8Yk";

    private AutoCompleteTextView _atvLocation;
    private Context _context;
    private MapManager _mapManager;


    public enum DownloadTypes {PLACES, PLACES_DETAILS};


    public GeoAutoComplete(MapManager mapManager,Context context, AutoCompleteTextView atvLocation) {
        _mapManager = mapManager;
        _context = context;
        _atvLocation =atvLocation;
        initAutoCompleteLocation();
    }

    private void initAutoCompleteLocation(){

        Log.d("AutoComplete","Init");

        _atvLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // Nothing in this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.d("AutoComplete","Text changed");
                DownloadTask downloadTask = new DownloadTask(DownloadTypes.PLACES);

                String Url = getAutocompleteUrl(charSequence.toString());

                downloadTask.execute(Url);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Nothing in this case
            }
        });

        _atvLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                ListView lv = (ListView)adapterView;
                SimpleAdapter adapter = (SimpleAdapter) adapterView.getAdapter();

                HashMap<String,String> hm = (HashMap<String,String>)adapter.getItem(i);

                DownloadTask downloadTask = new DownloadTask(DownloadTypes.PLACES_DETAILS);

                String Url = getPlaceDetailsUrl(hm.get("reference"));

                downloadTask.execute(Url);

            }
        });

    }

    private String getPlaceDetailsUrl(String ref) {
        // Obtain browser key from https://code.google.com/apis/console
        String key = "key="+API_KEY;

        // reference of place
        String reference = "reference="+ref;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = reference+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

        return url;
    }

    private static String escape(String s) {
        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = true;
        for( char c : s.toCharArray() ) {
            if( c == ' ' ) {
                Log.d("AutoEscape","c = " + c);
                Log.d("AutoEscape","previous space" + previousWasASpace);
               // if( previousWasASpace ) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                //}
                //previousWasASpace = true;
            }
            /*else {
                previousWasASpace = false;
            }*/
            switch(c) {
                case '<': builder.append("&lt;"); break;
                case '>': builder.append("&gt;"); break;
                case '&': builder.append("&amp;"); break;
                case '"': builder.append("&quot;"); break;
                case '\n': builder.append("<br>"); break;
                // We need Tab support here, because we print StackTraces as HTML
                case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
                default:
                    if( c < 128 ) {
                        builder.append(c);
                    } else {
                        builder.append("&#").append((int)c).append(";");
                    }
            }
        }
        Log.d("AutoComplete-HTML",builder.toString());
        return builder.toString();
    }

    private String getAutocompleteUrl(String place) {

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key="+API_KEY;

        // place to be be searched
        String input = "input="+escape(place);

        Log.d("AutoComplete",input);

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = input+"&"+types+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        return url;
    }


    private class DownloadTask extends AsyncTask<String,Void,String> {


        DownloadTypes _type;

        public DownloadTask (DownloadTypes type){
            _type = type;
        }



        @Override
        protected String doInBackground(String... strings) {

            String strUrl = strings[0];
            Log.d("AutoComplete","Sending to url " + strUrl );

            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;

            try{

                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }
                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
            }finally{
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                urlConnection.disconnect();
            }
            Log.d("AutoComplete","Recieved " + data);
            return data;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.isEmpty()) {
                return;
            }
            switch (_type){
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

    private class ParseTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        DownloadTypes _parseType;

        public ParseTask(DownloadTypes type){
            _parseType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JSONObject jObject;
            List<HashMap<String,String>> list = null;

            try {
                jObject = new JSONObject(strings[0]);
                switch (_parseType){
                    case  PLACES:
                        PlaceJSONParser placeParser = new PlaceJSONParser();
                        Log.d("AutoComplete","jObject = " + jObject.toString());
                        list = placeParser.parse(jObject);
                        break;
                    case PLACES_DETAILS:
                        PlaceDetailsJSONParser placeDetailsJSONParser = new PlaceDetailsJSONParser();
                        list = placeDetailsJSONParser.parse(jObject);
                        break;
                }
            } catch (JSONException e) {
                Log.e("AutoComplete",e.getMessage());
                e.printStackTrace();
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            switch (_parseType){
                case PLACES:
                    if (result == null){
                        Log.d("AutoComplete", "result is null");
                    }
                    else
                        Log.d("AutoComplete", "item num = " +  result.size());
                    String[] from = new String[] {"description"};
                    int[] to = new  int[] {android.R.id.text1};

                    // Creating a SimpleAdapter for the AutoCompleteTextView
                    SimpleAdapter adapter = new SimpleAdapter(_context, result, android.R.layout.simple_list_item_1, from, to);

                    // Setting the adapter
                    _atvLocation.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case PLACES_DETAILS:
                    HashMap<String, String> hm = result.get(0);

                    // Getting latitude from the parsed data
                    double latitude = Double.parseDouble(hm.get("lat"));

                    // Getting longitude from the parsed data
                    double longitude = Double.parseDouble(hm.get("lng"));

                    _mapManager.displayFoundLocation(new LatLng(latitude,longitude));
                    break;
            }
        }
    }
}
