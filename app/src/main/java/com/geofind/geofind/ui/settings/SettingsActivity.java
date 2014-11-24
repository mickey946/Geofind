package com.geofind.geofind.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.BuildConfig;
import com.geofind.geofind.GeofindApp;
import com.geofind.geofind.R;
import com.google.android.gms.analytics.Tracker;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.playutils.GameHelper;

/**
 * An {@link android.app.Activity} used for settings.
 */
public class SettingsActivity extends BaseGameActivity {

    /**
     * The {@link com.geofind.geofind.ui.settings.SettingsActivity} of this
     * {@link android.app.Activity}.
     */
    SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // set and show the settings fragments
        if (savedInstanceState == null) {
            settingsFragment = new SettingsFragment();
            settingsFragment.setGameHelper(getGameHelper());
            getFragmentManager().beginTransaction()
                    .add(R.id.container, settingsFragment)
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

    @Override
    public void onSignInFailed() {
        settingsFragment.setButtonToSignIn();
    }

    @Override
    public void onSignInSucceeded() {
        settingsFragment.setButtonToSignOut();
    }

    /**
     * A {@link android.preference.PreferenceFragment} used to contain the settings.
     */
    public static class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        /**
         * Sign in/out preference button.
         */
        private Preference signInOut;

        /**
         * Two listeners for the sign in/out button.
         */
        private Preference.OnPreferenceClickListener signInClick, signOutClick;

        /**
         * The {@link com.geofind.geofind.playutils.GameHelper} of the activity.
         */
        private GameHelper gameHelper;

        public SettingsFragment() {
        }

        /**
         * Set the {@link com.geofind.geofind.playutils.GameHelper} for this
         * {@link com.geofind.geofind.ui.settings.SettingsActivity.SettingsFragment}.
         * @param gameHelper The {@link com.geofind.geofind.playutils.GameHelper} of the
         * {@link android.app.Activity}.
         */
        public void setGameHelper(GameHelper gameHelper) {
            this.gameHelper = gameHelper;
        }

        /**
         * Change the sign in\out button into a sign in button.
         */
        public void setButtonToSignIn() {
            signInOut.setOnPreferenceClickListener(signInClick);
            signInOut.setTitle(getString(R.string.preferences_account_sign_in_title));
        }

        /**
         * Change the sign in\out button into a sign out button.
         */
        public void setButtonToSignOut() {
            signInOut.setOnPreferenceClickListener(signOutClick);
            signInOut.setTitle(getString(R.string.preferences_account_sign_out_title));
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setupSimplePreferencesScreen();

            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            // Sign in/out button title and click listener managing
            signInOut = findPreference(getString(R.string.pref_key_account_sign_in_out));

            signOutClick =
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            gameHelper.signOut();
                            setButtonToSignIn();
                            return true;
                        }
                    };

            signInClick =
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            gameHelper.beginUserInitiatedSignIn();
                            return true;
                        }
                    };

            if (gameHelper.isSignedIn()) {
                setButtonToSignOut();
            } else { // user is disconnected
                setButtonToSignIn();
            }

            // Show the version number
            Preference version = findPreference(getString(R.string.pref_key_version));
            version.setSummary(BuildConfig.VERSION_NAME);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        /**
         * Setup the preference screen.
         */
        private void setupSimplePreferencesScreen() {
            // Add 'general' preferences.
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // this method is called after the key is set so it must exists.

            if (key.equals(getString(R.string.pref_analytics))) {
                Tracker tracker = ((GeofindApp) getActivity().getApplicationContext()).
                        getTracker(GeofindApp.TrackerName.APP_TRACKER);
                tracker.enableAutoActivityTracking(
                        sharedPreferences.getBoolean(key, false));
            } else if (key.equals(getString(R.string.pref_crash_logs))) {
                Tracker tracker = ((GeofindApp) getActivity().getApplicationContext()).
                        getTracker(GeofindApp.TrackerName.APP_TRACKER);
                tracker.enableExceptionReporting(
                        sharedPreferences.getBoolean(key, false));
            }
        }
    }
}
