package com.geofind.geofind.playutils;

import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.games.snapshot.SnapshotMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class hold the status of the game and synchronized with google play saved games
 *
 * Created by Ilia Marin on 14/11/2014.
 */
public class GameStatus {

    private static final String TAG = GameStatus.class.getName();

    /**
     * JSON fields constants
     */
    private final String SAVEGAME_TITLE = "huntTitle";
    private final String SAVEGAME_ID = "huntID";
    private final String SAVEGAME_START_TIME = "huntStartTime";
    private final String SAVEGAME_FINISH_TIME = "huntFinishTime";
    private final String SAVEGAME_HUNT_POSITION = "huntPosition";
    private final String SAVEGAME_REVEALED_POINTS = "revealedPoints";
    private final String SAVEGAME_IS_FINISHED = "isFinished";

    /**
     * The Json format version
     */
    private final String SERIAL_VERSION = "0.1";


    /**
     * The played hunt in current session
     */
    private Map<String, HuntStatus> activeHunts;

    /**
     * The loaded hunts from google plus
     */
    private Map<String, SnapshotMetadata> savedHunts;

    /**
     * This class holds the status of specific hunt
     */
    public class HuntStatus {

        /**
         * The title of the hunt
         */
        private String huntTitle;

        /**
         * The ID of the hunt
         */
        private String huntID;

        /**
         * The time frame of the hunt
         */
        private long huntStartTime, huntFinishTime;

        /**
         * The current position of the play (the next point to find)
         */
        private int huntPosition;

        /**
         * The list of the revealed point indices
         */
        private ArrayList<Integer> revealedPoints;

        /**
         * Specifies if the hunt is finished
         */
        private boolean isFinished;

        /**
         * The snapshot of the hunt
         */
        private SnapshotMetadata metadata;

        /**
         * Create new hunt game
         * @param huntTitle the title of the hunt
         * @param huntID the ParseID of the hunt
         */
        public HuntStatus(String huntTitle, String huntID) {
            this.huntID = huntID;
            this.huntTitle = huntTitle;
            huntPosition = 0;
            huntStartTime = SystemClock.elapsedRealtime();
            revealedPoints = new ArrayList<Integer>();
            isFinished = false;
            metadata = null;
        }

        /**
         * Loads a saved hunt from google plus
         * @param jsonObject the json the describes the hunt
         * @param snapshotMetadata the snapshot of the hunt
         * @throws JSONException
         */
        public HuntStatus(JSONObject jsonObject, SnapshotMetadata snapshotMetadata) throws JSONException {
            metadata = snapshotMetadata;
            Log.d(TAG,jsonObject.toString());
            huntTitle = jsonObject.getString(SAVEGAME_TITLE);
            huntID = jsonObject.getString(SAVEGAME_ID);
            huntStartTime = jsonObject.getLong(SAVEGAME_START_TIME);
            huntFinishTime = jsonObject.getLong(SAVEGAME_FINISH_TIME);
            huntPosition = jsonObject.getInt(SAVEGAME_HUNT_POSITION);

            JSONArray rev = new JSONArray(jsonObject.getString(SAVEGAME_REVEALED_POINTS)); // jsonObject.getJSONArray("revealedPoints");
            revealedPoints = new ArrayList<Integer>();
            for (int i = 0; i < rev.length(); i++) {
                revealedPoints.add(rev.getInt(i));
            }
            isFinished = jsonObject.getBoolean(SAVEGAME_IS_FINISHED);
        }

        /**
         * Convert the hunt status to json
         */
        public JSONObject toJsonObject() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SAVEGAME_TITLE, huntTitle);
            jsonObject.put(SAVEGAME_ID, huntID);
            jsonObject.put(SAVEGAME_START_TIME, huntStartTime);
            jsonObject.put(SAVEGAME_FINISH_TIME,huntFinishTime);
            jsonObject.put(SAVEGAME_IS_FINISHED,isFinished);
            jsonObject.put(SAVEGAME_HUNT_POSITION, huntPosition);
            jsonObject.put(SAVEGAME_REVEALED_POINTS, revealedPoints);
            return jsonObject;
        }

        /**
         * update the status of the current hunt
         * @param revealed specified if the last point had been revealed or solved
         */
        public void updateStatus(boolean revealed) {
            if (revealed)
                revealedPoints.add(huntPosition);
            huntPosition++;
        }

        /**
         * Marks the hunt as finished
         */
        public void markFinish(){
            isFinished=true;
            huntFinishTime = SystemClock.elapsedRealtime();
        }

        /**
         *
         * @return the last played position
         */
        public int getHuntPosition() {
            return huntPosition;
        }

        /**
         *
         * @return the list of the revealed points indices.
         */
        public ArrayList<Integer> getRevealedPoints() {
            return revealedPoints;
        }

        public SnapshotMetadata getMetaData() {
            return metadata;
        }

        public long getHuntStartTime() {
            return huntStartTime;
        }
    }


    public GameStatus() {
        activeHunts = new HashMap<String, HuntStatus>();
        savedHunts = new HashMap<String, SnapshotMetadata>();
    }

    /**
     * add a new played hunt
     * @param HuntTitle the title of the hunt
     * @param HuntID the ID of the hunt
     * @return true if the new hunt was successfully registered
     */
    public boolean startGame(String HuntTitle, String HuntID) {
        if (activeHunts.containsKey(HuntID)) {
            return false;
        } else {
            activeHunts.put(HuntID, new HuntStatus(HuntTitle, HuntID));
            return true;
        }


    }

    /**
     * Returns true if the hunt with {@param huntID} is finished
     */
    public boolean isFinished(String huntID) {
        if (activeHunts.containsKey(huntID)){
            return  activeHunts.get(huntID).isFinished;
        }
        return false;
    }

    /**
     * Updates the status of the hunt
     * @param HuntId the id of the hunt to be updated
     * @param revealed marks if this point is revealed or solved
     * @param isFinished is this the last point of the hunt
     * @return true if hunt successfully updated
     */
    public boolean updateGame(String HuntId, boolean revealed, boolean isFinished) {
        if (activeHunts.containsKey(HuntId)) {
            activeHunts.get(HuntId).updateStatus(revealed);
            if (isFinished)
                activeHunts.get(HuntId).markFinish();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads saved hunt from json
     */
    public void loadHunt(byte[] data, SnapshotMetadata metadata) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data)).getJSONObject("Hunt");
            String id = jsonObject.getString("huntID");
            activeHunts.put(id, new HuntStatus(jsonObject, metadata));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Convert the hunt specified by {@param HuntID} to byte array for saving
     * @return the converted byte array
     */
    public byte[] HuntToBytes(String HuntID) {
        JSONObject currentHunt = new JSONObject();
        try {
            if (activeHunts.containsKey(HuntID))
                currentHunt.put("Hunt", activeHunts.get(HuntID).toJsonObject());
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
            for (String levelName : activeHunts.keySet()) {
                hunts.put(levelName, activeHunts.get(levelName).toJsonObject());

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

    /**
     * Creates a clone of the game status
     */
    @Override
    public GameStatus clone() throws CloneNotSupportedException {
        super.clone();
        GameStatus gameStatus = new GameStatus();
        for (String id : activeHunts.keySet()) {
            gameStatus.activeHunts.put(id, activeHunts.get(id));
        }

        return gameStatus;
    }

    /**
     * Register loaded hunt
     * @param metadata the metadata od the hunt
     */
    public void addToSaveHunts(SnapshotMetadata metadata){
        savedHunts.put(metadata.getUniqueName().substring(8), metadata);

        Log.d("GameStatus","adding " + metadata.getUniqueName() + ":" + metadata.getDescription());
    }

    /**
     * @return the list of the onGoing hunts
     */
    public Collection<String> getOnGoing(){
        ArrayList<String> hunts = new ArrayList<String>();
        for (Map.Entry<String,SnapshotMetadata> entry : savedHunts.entrySet()){
            if (entry.getValue().getDescription().equalsIgnoreCase("OnGoing")){
                hunts.add(entry.getKey());
            }
        }
        return  hunts;
    }

    /**
     * @return the lost of the finished hunts
     */
    public Collection<String> getFinished(){
        ArrayList<String> hunts = new ArrayList<String>();
        for (Map.Entry<String,SnapshotMetadata> entry : savedHunts.entrySet()){
            if (entry.getValue().getDescription().equalsIgnoreCase("Finished")){
                hunts.add(entry.getKey());
            }
        }
        return  hunts;
    }

    /**
     * @return the IDs of all the known to the player hunts
     */
    public Collection<String> getPlayed(){
        return savedHunts.keySet();
    }

    /**
     * @param huntId the Id of the hunt
     * @return the snapshot associated with this hunt.
     */
    public SnapshotMetadata getSnapshotMetadataById(String huntId) {
        return savedHunts.get(huntId);
    }

    /**
     * @param huntId the Id of the hunt
     * @return the list of indices of the revealed points .
     */
    public ArrayList<Integer> getHuntRevealedPoints(String huntId) {
        return activeHunts.get(huntId).getRevealedPoints();
    }

    /**
     * @param huntId the Id of the hunt
     * @return The current position of the play (the next point to find)
     */
    public int getHuntPosition(String huntId) {
        return activeHunts.get(huntId).getHuntPosition();
    }

    /**
     * If the hunt is finish then returns the played time for this hunt,
     * otherwise returns -1
     * @param huntId the Id of the hunt
     * @return the time in ms
     */
    public long getHuntPlayedTime(String huntId){
        if (activeHunts.get(huntId).isFinished)
        {
            return activeHunts.get(huntId).huntFinishTime - activeHunts.get(huntId).huntStartTime;
        }
        return -1;
    }
}
