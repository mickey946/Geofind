package com.geofind.geofind;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.games.snapshot.SnapshotMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Ilia Marin on 14/11/2014.
 */
public class GameStatus {


    private class HuntStatus {

        private String huntTitle;
        private String huntID;
        private long huntTime;
        private int huntPosition;
        private ArrayList<Integer> revealedPoints;
        private boolean isFinished;

        public HuntStatus(String huntTitle, String huntID) {
            this.huntID = huntID;
            this.huntTitle = huntTitle;
            huntPosition = 0;
            huntTime = 0;
            revealedPoints = new ArrayList<Integer>();
            isFinished = false;
        }

        public HuntStatus(JSONObject jsonObject) throws JSONException {
            huntTitle = jsonObject.getString("huntTitle");
            huntID = jsonObject.getString("huntID");
            huntTime = jsonObject.getInt("huntTime");
            huntPosition = jsonObject.getInt("huntPosition");
            JSONArray rev = jsonObject.getJSONArray("revealedPoints");
            revealedPoints = new ArrayList<Integer>();
            for (int i = 0; i < rev.length(); i++) {
                revealedPoints.add(rev.getInt(i));
            }
            isFinished = false;
        }


        public JSONObject toJsonObject() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("huntTitle", huntTitle);
            jsonObject.put("huntID", huntID);
            jsonObject.put("huntTime", huntTime);
            jsonObject.put("huntPosition", huntPosition);
            jsonObject.put("revealedPoints", revealedPoints);
            return jsonObject;
        }

        public void updateStatus(long huntTime, boolean revealed) {
            this.huntTime = huntTime;
            if (revealed)
                revealedPoints.add(huntPosition);
            huntPosition++;
        }

        public void markFinish(){
            isFinished=true;
        }


    }

    private Map<String, HuntStatus> _activeHunts;
    private final String SERIAL_VERSION = "0.0";
    private Map<String, SnapshotMetadata> _savedHunts;

    public GameStatus() {
        _activeHunts = new HashMap<String, HuntStatus>();
        _savedHunts = new HashMap<String, SnapshotMetadata>();
    }

    public GameStatus(byte[] data) {
        if (data == null) return;
        loadJson(new String(data));
    }

    public GameStatus(String json) {
        if (json == null) return;
        loadJson(json);
    }

    public GameStatus(SharedPreferences sp, String key) {
        loadJson(sp.getString(key, ""));
    }

    public boolean startGame(String HuntTitle, String HuntID) {
        if (_activeHunts.containsKey(HuntID)) {
            return false;
        } else {
            _activeHunts.put(HuntID, new HuntStatus(HuntTitle, HuntID));
            return true;
        }


    }


    public boolean isFinished(String huntID) {
        if (_activeHunts.containsKey(huntID)){
            return  _activeHunts.get(huntID).isFinished;
        }
        return false;
    }

    public boolean upDateGame(String HuntId, long huntTime, boolean revealed, boolean isFinished) {
        if (_activeHunts.containsKey(HuntId)) {
            _activeHunts.get(HuntId).updateStatus(huntTime, revealed);
            if (isFinished)
                _activeHunts.get(HuntId).markFinish();
            return true;
        } else {
            return false;
        }
    }

    public void loadHunt(byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            String id = jsonObject.getString("huntID");
            _activeHunts.put(id, new HuntStatus(jsonObject));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Replaces this SaveGame's content with the content loaded from the given JSON string.
     */
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
                String levelName = (String) iter.next();
                _activeHunts.put(levelName, new HuntStatus(hunts.getJSONObject(levelName)));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            //Log.e(TAG, "Save data has a syntax error: " + json, ex);

            // Initializing with empty stars if the game file is corrupt.
            // NOTE: In your game, you want to try recovering from the snapshot payload.
            _activeHunts.clear();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Save data has an invalid number in it: " + json, ex);
        }
    }

    public byte[] toBytes() {
        return toString().getBytes();
    }

    public byte[] HuntToBytes(String HuntID) {
        JSONObject currentHunt = new JSONObject();
        try {
            if (_activeHunts.containsKey(HuntID))
                currentHunt.put("Hunt", _activeHunts.get(HuntID).toJsonObject());
            else {
                Log.e("GameStatus", "HuntID not found on save");
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error converting single hunt save data to JSON.", ex);
        }

        return currentHunt.toString().getBytes();
    }

    /**
     * Serializes this SaveGame to a JSON string.
     */
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
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error converting save data to JSON.", ex);
        }
    }

    public HuntStatus GetHuntStatus(String HuntID) {
        return _activeHunts.get(HuntID);
    }

//
//    public GameStatus unionWith(GameStatus gameStatus){
//        GameStatus current = clone();
//
//        for (String HuntId: gameStatus._activeHunts.keySet()){
//
//        }
//
//    }

    public GameStatus clone() {
        GameStatus gameStatus = new GameStatus();
        for (String id : _activeHunts.keySet()) {
            gameStatus._activeHunts.put(id, _activeHunts.get(id));
        }

        return gameStatus;
    }

    public void addToSaveHunts(SnapshotMetadata metadata){
        _savedHunts.put(metadata.getUniqueName().substring(8), metadata);
        Log.d("GameStatus","adding " + metadata.getUniqueName() + ":" + metadata.getDescription());
    }

    public Collection<String> getOnGoing(){
        ArrayList<String> hunts = new ArrayList<String>();
        for (Map.Entry<String,SnapshotMetadata> entry : _savedHunts.entrySet()){
            if (entry.getValue().getDescription().equalsIgnoreCase("OnGoing")){
                hunts.add(entry.getKey());
            }
        }
        return  hunts;
    }

    public Collection<String> getFinished(){
        ArrayList<String> hunts = new ArrayList<String>();
        for (Map.Entry<String,SnapshotMetadata> entry : _savedHunts.entrySet()){
            if (entry.getValue().getDescription().equalsIgnoreCase("Finished")){
                hunts.add(entry.getKey());
            }
        }
        return  hunts;
    }

    public Collection<String> getPlayed(){
        return _savedHunts.keySet();
    }

}
