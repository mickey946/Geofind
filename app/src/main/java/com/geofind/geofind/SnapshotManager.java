package com.geofind.geofind;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Ilia Marin on 14/11/2014.
 */
public class SnapshotManager {

    private static String TAG = "SnapshotManager";
    private GoogleApiClient mGoogleApiClient;
    private GameStatus gameStatus;
    private String currentSaveName = "snapshotTemp";
    private Context context;


    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Request code for listing saved games
    private static final int RC_LIST_SAVED_GAMES = 9002;

    // Request code for selecting a snapshot
    private static final int RC_SELECT_SNAPSHOT = 9003;

    // Request code for saving the game to a snapshot.
    private static final int RC_SAVE_SNAPSHOT = 9004;

    private static final int RC_LOAD_SNAPSHOT = 9005;

    public SnapshotManager(Context context, GoogleApiClient client){
        this.context = context;
        mGoogleApiClient = client;
        gameStatus  = ((GeoFindApp)context.getApplicationContext()).getGameStatus();

    }


    public void saveSnapshot(final SnapshotMetadata snapshotMetadata){
        AsyncTask<Void,Void,Snapshots.OpenSnapshotResult> task =
                new AsyncTask<Void, Void, Snapshots.OpenSnapshotResult>() {
                    @Override
                    protected Snapshots.OpenSnapshotResult doInBackground(Void... params) {
                        if (snapshotMetadata == null){
                            return Games.Snapshots.open(mGoogleApiClient,currentSaveName,true)
                                    .await();
                        }
                        else {
                            return Games.Snapshots.open(mGoogleApiClient,snapshotMetadata)
                                    .await();
                        }
                    }

                    @Override
                    protected void onPostExecute(Snapshots.OpenSnapshotResult openSnapshotResult) {
                        Snapshot toWrite = processSnapshotOpenResult(RC_SAVE_SNAPSHOT, openSnapshotResult, 0);

                        Log.i(TAG, writeSnapshot(toWrite));
                    }
                };
        task.execute();
    }

    /**
     * Conflict resolution for when Snapshots are opened.
     *
     * @param requestCode - the request currently being processed.  This is used to forward on the
     *                    information to another activity, or to send the result intent.
     * @param result The open snapshot result to resolve on open.
     * @param retryCount - the current iteration of the retry.  The first retry should be 0.
     * @return The opened Snapshot on success; otherwise, returns null.
     */
    Snapshot processSnapshotOpenResult(int requestCode, Snapshots.OpenSnapshotResult result,
                                       int retryCount) {

        retryCount++;
        int status = result.getStatus().getStatusCode();

        Log.i(TAG, "Save Result status: " + status);

        if (status == GamesStatusCodes.STATUS_OK) {
            return result.getSnapshot();
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
            return result.getSnapshot();
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {
            Log.e(TAG,"snapshot conflict");
            Toast.makeText(context,"snapshot conflict",Toast.LENGTH_LONG);
//            final Snapshot snapshot = result.getSnapshot();
//            final Snapshot conflictSnapshot = result.getConflictingSnapshot();
//
//            ArrayList<Snapshot> snapshotList = new ArrayList<Snapshot>(2);
//            snapshotList.add(snapshot);
//            snapshotList.add(conflictSnapshot);
//
//            selectSnapshotItem(requestCode, snapshotList, result.getConflictId(), retryCount);
            // display both to the user and allow them to select on
        }
        // Fail, return null.
        return null;
    }


    /**
     * Generates metadata, takes a screenshot, and performs the write operation for saving a
     * snapshot.
     */
    private String writeSnapshot(Snapshot snapshot) {
        // Set the data payload for the snapshot.


        snapshot.getSnapshotContents().writeBytes(gameStatus.toBytes());
        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                //.setCoverImage(getScreenShot())
                .setDescription("Modified data at: " + Calendar.getInstance().getTime())
                .build();
        Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange);
        return snapshot.getSnapshotContents().toString();
    }


//    private void selectSnapshotItem(int requestCode, ArrayList<Snapshot> items,
//                                    String conflictId, int retryCount) {
//
//        ArrayList<SnapshotMetadata> snapshotList = new ArrayList<SnapshotMetadata>(items.size());
//        for (Snapshot m : items) {
//            snapshotList.add(m.getMetadata().freeze());
//        }
////        Intent intent = new Intent(this, SelectSnapshotActivity.class);
////        intent.putParcelableArrayListExtra(SelectSnapshotActivity.SNAPSHOT_METADATA_LIST,
////                snapshotList);
////
////        intent.putExtra(SelectSnapshotActivity.CONFLICT_ID, conflictId);
////        intent.putExtra(SelectSnapshotActivity.RETRY_COUNT, retryCount);
////
////        startActivityForResult(intent, requestCode);
//    }
}
