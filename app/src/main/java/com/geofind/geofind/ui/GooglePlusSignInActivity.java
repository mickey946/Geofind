package com.geofind.geofind.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.geofind.geofind.R;
import com.geofind.geofind.playutils.BaseGameActivity;

/**
 * An {@link android.app.Activity} that is used as a splash screen and signs in the user.
 */
public class GooglePlusSignInActivity extends BaseGameActivity {

    /**
     * Maximum timeout for the splash screen to appear.
     */
    private static final long TIMEOUT = 3000;

    /**
     * The system time of which the app was launched.
     */
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedClients(CLIENT_GAMES | CLIENT_PLUS | CLIENT_SNAPSHOT);

        super.onCreate(savedInstanceState);

        getGameHelper().setMaxAutoSignInAttempts(
                getResources().getInteger(R.integer.google_play_max_auto_sing_in));

        setContentView(R.layout.activity_splash_screen);

        startTime = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "before connect");
    }

    /**
     * Open the {@link com.geofind.geofind.ui.MainScreenActivity}
     */
    private void openMainScreen() {
        long timeLeft = Math.max(TIMEOUT - (SystemClock.elapsedRealtime() - startTime), 0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, timeLeft);
    }

    @Override
    public void onSignInFailed() {
        Log.d("Failed to connect at GooglePlusSignInActivity", "");

        openMainScreen();
    }

    @Override
    public void onSignInSucceeded() {
        Log.d("Connected at GooglePlusSignInActivity", getApiClient().toString());

        openMainScreen();
    }
}