package com.geofind.geofind;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.geofind.geofind.playutils.GameStatus;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;

import java.util.HashMap;

/**
 * Created by Ilia Marin on 31/10/2014.
 */
public class GeofindApp extends Application {

    public static final String BROWSER_API_KEY = "AIzaSyAtwXqO2w5kV9a8iE-AcbcoI9DWlK0Q8Yk";
    public static final String SERVER_API_KEY = "AIzaSyDzjQIXk15RyvuI-kwZnw6KeQsZP6c-eR0";
    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-56204480-2";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    private GameStatus gameStatus;

    public GeofindApp() {
        super();
        gameStatus = new GameStatus();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Tracker tracker = getTracker(TrackerName.APP_TRACKER);
        tracker.enableAutoActivityTracking(sharedPreferences.getBoolean(
                getString(R.string.pref_analytics), true));
        tracker.enableExceptionReporting(sharedPreferences.getBoolean(
                getString(R.string.pref_crash_logs), true));
        tracker.setSessionTimeout(300);

        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker(PROPERTY_ID);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     */
    public enum TrackerName {
        APP_TRACKER // Tracker used only in this app.
    }

    public synchronized GameStatus getGameStatus(){
        return gameStatus;
    }

}
