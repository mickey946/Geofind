package com.geofind.geofind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.example.games.basegameutils.BaseGameActivity;

public class GooglePlusSignInActivity extends BaseGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedClients(CLIENT_GAMES | CLIENT_PLUS | CLIENT_SNAPSHOT);

        super.onCreate(savedInstanceState);

        getGameHelper().setMaxAutoSignInAttempts(
                getResources().getInteger(R.integer.google_play_max_auto_sing_in));

        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "before connect");
    }

    @Override
    public void onSignInFailed() {
        Log.d("Failed to connect at GooglePlusSignInActivity", "");

        Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }

    @Override
    public void onSignInSucceeded() {
        Log.d("Connected at GooglePlusSignInActivity", getApiClient().toString());

        Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }
}