package com.geofind.geofind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.util.App;


public class SettingsActivity extends ActionBarActivity {

    /**
     * General settings group.
     */
    public static final String GENERAL_SETTINGS =
            App.getContext().getString(R.string.pref_key_general_settings);

    /**
     * Sound effects settings entry.
     */
    public static final String SOUND_EFFECTS =
            App.getContext().getString(R.string.pref_key_sound);

    /**
     * Device settings group.
     */
    public static final String DEVICE_SETTINGS =
            App.getContext().getString(R.string.pref_key_device_settings);

    /**
     * Always awake settings entry.
     */
    public static final String ALWAYS_AWAKE =
            App.getContext().getString(R.string.pref_key_stay_awake);

    /**
     * Work in background settings entry.
     */
    public static final String WORK_IN_BACKGROUND =
            App.getContext().getString(R.string.pref_key_background);

    /**
     * Play offline settings entry.
     */
    public static final String PLAY_OFFLINE =
            App.getContext().getString(R.string.pref_key_offline);

    /**
     * Units settings group.
     */
    public static final String UNITS =
            App.getContext().getString(R.string.pref_key_units_settings);

    /**
     * Distance units settings enrty.
     */
    public static final String DISTANCE_UNITS =
            App.getContext().getString(R.string.pref_key_distance_units);

    /**
     * Account settings group.
     */
    public static final String ACCOUNT_SETTINGS =
            App.getContext().getString(R.string.pref_key_account_settings);

    /**
     * Account profile settings entry.
     */
    public static final String ACCOUNT_PROFILE =
            App.getContext().getString(R.string.pref_key_account_profile);

    /**
     * Account sign in/out settings entry.
     */
    public static final String SIGN_IN_OUT =
            App.getContext().getString(R.string.pref_key_account_sign_in_out);

    /**
     * Analytics settings group.
     */
    public static final String ANALYTICS_SETTINGS =
            App.getContext().getString(R.string.pref_key_analytics_settings);

    /**
     * Analytics settings entry.
     */
    public static final String ANALYTICS =
            App.getContext().getString(R.string.pref_key_analytics);

    /**
     * Crash logs settings entry.
     */
    public static final String CRASH_LOGS =
            App.getContext().getString(R.string.pref_key_crash_logs);

    /**
     * About settings group.
     */
    public static final String ABOUT_SETTINGS =
            App.getContext().getString(R.string.pref_key_about_settings);

    /**
     * Open source licences settings entry.
     */
    public static final String OPEN_SOURCE_LICENCES =
            App.getContext().getString(R.string.pref_key_licences);

    /**
     * About settings entry.
     */
    public static final String ABOUT =
            App.getContext().getString(R.string.pref_key_about);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // set and show the settings fragments
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        onBackPressed();
        return null;
    }

    @Override
    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onCreateSupportNavigateUpTaskStack(builder);
        onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setupSimplePreferencesScreen();
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        private void setupSimplePreferencesScreen() {
            // Add 'general' preferences.
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }
    }
}
