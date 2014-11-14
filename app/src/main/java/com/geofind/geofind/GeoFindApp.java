package com.geofind.geofind;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by Ilia Marin on 31/10/2014.
 */
public class GeoFindApp extends Application {

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-56204480-2";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    GameStatus gameStatus;

    public GeoFindApp() {
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
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
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
