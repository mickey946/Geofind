package com.geofind.geofind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;


public class GooglePlusSignInActivity extends BaseGameActivity {

    private GoogleApiClient mGoogleApiClient;

    TextView id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedClients(CLIENT_GAMES | CLIENT_PLUS | GameHelper.CLIENT_SNAPSHOT);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "before connect");
        beginUserInitiatedSignIn();
    }

    @Override
    public void onSignInFailed() {
        Toast.makeText(this, "Connection failed / cancel pressed", Toast.LENGTH_LONG).show();

        Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }

    @Override
    public void onSignInSucceeded() {
        mGoogleApiClient = getApiClient();
        Log.d("mGoogleapi = ", mGoogleApiClient.toString());
        UserData.init(mGoogleApiClient);

        Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }
}