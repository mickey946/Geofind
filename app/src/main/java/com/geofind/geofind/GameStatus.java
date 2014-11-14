package com.geofind.geofind;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ilia Marin on 14/11/2014.
 */
public class GameStatus {
    
    private class HuntStatus{
        
        private String huntTitle;
        private String huntID;
        private long huntTime;
        private int huntPosition;
        private ArrayList<Integer> revealedPoints;
        
        public HuntStatus(String huntTitle, String huntID){
            this.huntID = huntID;
            this.huntTitle = huntTitle;
            huntPosition=0;
            huntTime=0;
            revealedPoints = new ArrayList<Integer>();
        }

        public HuntStatus(JSONObject jsonObject) throws JSONException {
            huntTitle = jsonObject.getString("huntTitle");
            huntID = jsonObject.getString("huntID");
            huntTime = jsonObject.getInt("huntTime");
            huntPosition = jsonObject.getInt("huntPosition");
            JSONArray rev = jsonObject.getJSONArray("revealedPoints");
            revealedPoints = new ArrayList<Integer>();
            for (int i = 0 ; i < rev.length(); i++){
                revealedPoints.add(rev.getInt(i));
            }
        }


        public JSONObject toJsonObject() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("huntTitle",huntTitle);
            jsonObject.put("huntID",huntID);
            jsonObject.put("huntTime",huntTime);
            jsonObject.put("huntPosition",huntPosition);
            jsonObject.put("revealedPoints",revealedPoints);
            return  jsonObject;
        }


    }

    private Map<String, HuntStatus> _activeHunts;
    private final String SERIAL_VERSION = "0.0";

    public GameStatus(){}

    public GameStatus(byte[] data){
        if (data == null) return;
        loadJson(new String(data));
    }

    public GameStatus(String json){
        if (json == null) return;
        loadJson(json);
    }

    public GameStatus(SharedPreferences sp, String key){
        loadJson(sp.getString(key, ""));
    }


    /** Replaces this SaveGame's content with the content loaded from the given JSON string. */
    public void loadJson(String json) {
        _activeHunts.clear();
        if (json == null || json.trim().equals("")) return;

        try {
            JSONObject obj = new JSONObject(json);
            String format = obj.getString("version");
            if (!format.equals(SERIAL_VERSION)) {
                throw new RuntimeException("Unexpected loot format " + format);
            }



            JSONObject hunts = obj.getJSONObject("hunts");
            Iterator<?> iter = hunts.keys();

            while (iter.hasNext()) {
                String levelName = (String)iter.next();
                _activeHunts.put(levelName, new HuntStatus(hunts.getJSONObject(levelName)));
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            //Log.e(TAG, "Save data has a syntax error: " + json, ex);

            // Initializing with empty stars if the game file is corrupt.
            // NOTE: In your game, you want to try recovering from the snapshot payload.
            _activeHunts.clear();
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Save data has an invalid number in it: " + json, ex);
        }
    }


    /** Serializes this SaveGame to a JSON string. */
    @Override
    public String toString() {
        try {
            JSONObject hunts = new JSONObject();
            for (String levelName : _activeHunts.keySet()) {
                hunts.put(levelName, _activeHunts.get(levelName).toJsonObject());

            }

            JSONObject obj = new JSONObject();
            obj.put("version", SERIAL_VERSION);
            obj.put("hunts", hunts);
            return obj.toString();
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error converting save data to JSON.", ex);
        }
    }
}