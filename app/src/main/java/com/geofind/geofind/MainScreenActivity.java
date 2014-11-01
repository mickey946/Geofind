package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.parse.Parse;


public class MainScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Parse.initialize(this, "s5kJQcPb2ipcKOylJ6bgRkB1ZPsoskxEzNYca1ME", "19BXDIKmyJTq5BXcd3mFyjth0prmkuak0ze686yp");
    }

    /**
     * Start {@link com.geofind.geofind.HuntListActivity} so the user can choose a hunt to play.
     *
     * @param view The current view.
     */
    public void openHuntList(View view) {
        Intent intent = new Intent(this, HuntListActivity.class);
        startActivity(intent);
    }

    /**
     * Start {@link com.geofind.geofind.CreateHuntActivity} so the user can create a hunt
     *
     * @param view The current view.
     */
    public void openHuntCreation(View view) {
        Intent intent = new Intent(this, CreateHuntActivity.class);
        startActivity(intent);
    }

    /**
     * Start {@link com.geofind.geofind.SettingsActivity} so the user can modify settings.
     *
     * @param view The current view.
     */
    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //TODO: All of Google Play Games buttons
}
