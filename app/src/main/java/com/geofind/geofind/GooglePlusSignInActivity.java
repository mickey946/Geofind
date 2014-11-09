package com.geofind.geofind;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotEntity;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;


public class GooglePlusSignInActivity extends BaseGameActivity {

    private GoogleApiClient mGoogleApiClient;

    TextView id;
    private boolean mIntentInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedClients(CLIENT_GAMES | CLIENT_PLUS);
        Log.d("onCreate", "start app");
        setContentView(R.layout.activity_splash_screen);

//        // Progress bar to be displayed if the connection failure is not resolved.
//        mConnectionProgressDialog = new ProgressDialog(this);
//        mConnectionProgressDialog.setMessage("Signing in...");


    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "before connect");
        beginUserInitiatedSignIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onSignInFailed() {
        Toast.makeText(this, "Connection failed / cancel pressed", Toast.LENGTH_LONG).show();

        Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
        startActivity(i);

        // close this activity
        //finish();
    }

    @Override
    public void onSignInSucceeded() {
        mGoogleApiClient = getApiClient();
        Log.d("mGoogleapi = ", mGoogleApiClient.toString());
        UserData.init(mGoogleApiClient);
        Toast.makeText(this, Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getDisplayName() + " is connected", Toast.LENGTH_LONG).show();

        Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }
}