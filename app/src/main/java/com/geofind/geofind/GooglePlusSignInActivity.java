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
import com.google.example.games.basegameutils.BaseGameUtils;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;


public class GooglePlusSignInActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Request code for listing saved games
    private static final int RC_LIST_SAVED_GAMES = 9002;

    // Request code for selecting a snapshot
    private static final int RC_SELECT_SNAPSHOT = 9003;

    // Request code for saving the game to a snapshot.
    private static final int RC_SAVE_SNAPSHOT = 9004;

    private static final int RC_LOAD_SNAPSHOT = 9005;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // current save game - serializable to and from the saved game
    //SaveGame mSaveGame = new SaveGame();

    // progress dialog we display while we're loading state from the cloud
    ProgressDialog mLoadingDialog = null;

    // whether we already loaded the state the first time (so we don't reload
    // every time the activity goes to the background and comes back to the foreground)
    boolean mAlreadyLoadedState = false;

    //snapshot's name
    private String currentSaveName = "snapshotTemp";

    private ProgressDialog mConnectionProgressDialog;
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;

    TextView name,url,id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("onCreate", "start app");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                //.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                        // add other APIs and scopes here as needed
                .build();

        setContentView(R.layout.activity_splash_screen);

        // Progress bar to be displayed if the connection failure is not resolved.
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");


    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "before connect");
        mGoogleApiClient.connect();
    }

//    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "beginning");
        if (!mGoogleApiClient.isConnected()) {
            Log.d("onResume", "if not connected");
            if (mConnectionResult == null) {
                mConnectionProgressDialog.show();
                mGoogleApiClient.connect();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnectionProgressDialog != null){
            mConnectionProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
//        if (mConnectionProgressDialog != null){
//            mConnectionProgressDialog.dismiss();
//        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("onConnectionFailed", "onConnectionFailed() called, result: " + result);

        if (mResolvingConnectionFailure) {
            Log.d("onConnectionFailed", "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }


        mResolvingConnectionFailure = BaseGameUtils
                .resolveConnectionFailure(this, mGoogleApiClient,
                        result, RC_SIGN_IN, "error signing in");
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            Log.d("onActivityResult", "before connect");
            mGoogleApiClient.connect();
        }
        else if (requestCode == REQUEST_CODE_RESOLVE_ERR) {
            Log.d("onActivityResult", "request code: " + requestCode + ", response code: " + responseCode);
            Toast.makeText(this, "Connection failed / cancel pressed", Toast.LENGTH_LONG).show();

            Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
            startActivity(i);

            // close this activity
            finish();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        UserData.init(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient)
                ,Plus.AccountApi.getAccountName(mGoogleApiClient));
        Toast.makeText(this, UserData.getEmail() + " is connected.", Toast.LENGTH_LONG).show();
        if (mGoogleApiClient.isConnected()){

            //saveSnapshot(null);
            Intent i = new Intent(GooglePlusSignInActivity.this, MainScreenActivity.class);
            startActivity(i);

            // close this activity
            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    /**
     * Prepares saving Snapshot to the user's synchronized storage, conditionally resolves errors,
     * and stores the Snapshot.
     */
    void saveSnapshot(final SnapshotMetadata snapshotMetadata) {
        AsyncTask<Void, Void, Snapshots.OpenSnapshotResult> task =
                new AsyncTask<Void, Void, Snapshots.OpenSnapshotResult>() {
                    @Override
                    protected Snapshots.OpenSnapshotResult doInBackground(Void... params) {
                        if (snapshotMetadata == null) {
                            return Games.Snapshots.open(mGoogleApiClient, currentSaveName, true)
                                    .await();
                        }
                        else {
                            return Games.Snapshots.open(mGoogleApiClient, snapshotMetadata)
                                    .await();
                        }
                    }

                    @Override
                    protected void onPostExecute(Snapshots.OpenSnapshotResult result) {
                        writeSnapshot(result.getSnapshot());
                    }
                };

        task.execute();
    }

    /**
     * Generates metadata, takes a screenshot, and performs the write operation for saving a
     * snapshot.
     */
    private void writeSnapshot(Snapshot snapshot) {
        // Set the data payload for the snapshot.
        snapshot.writeBytes(null);

        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(null)
                .setDescription("Modified data at: " + Calendar.getInstance().getTime())
                .build();
        Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange);
    }

}