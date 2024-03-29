package com.geofind.geofind.playutils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.geofind.geofind.GeofindApp;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ilia Marin on 14/11/2014.
 */
public class SnapshotManager {


    private static String TAG = SnapshotManager.class.getSimpleName();

    /**
     * Request code for saving the game to a snapshot.
     */
    private static final int RC_SAVE_SNAPSHOT = 9004;

    /**
     * Request code for load the game from a snapshot.
     */
    private static final int RC_LOAD_SNAPSHOT = 9005;

    /**
     * The api client
     */
    private GoogleApiClient googleApiClient;

    /**
     * The container of the game status
     */
    private GameStatus gameStatus;

    /**
     * the name of the snapshot to save
     */
    private String currentSaveName = "snapshotTemp";

    /**
     * The context of the host activity
     */
    private Context context;

    /**
     * Loading snapshot task
     */
    private AsyncTask<Void, Void, Snapshots.LoadSnapshotsResult> loadSnapshotsTask;

    public SnapshotManager(final Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.googleApiClient = googleApiClient;
        gameStatus = ((GeofindApp) context.getApplicationContext()).getGameStatus();
    }

    /**
     * Loads the saved snapshots from google plus account
     */
    public void loadSnapshotList() {
        Log.d(TAG, "Assigning and executing AsyncTask for loading snapshots");
        loadSnapshotsTask = new AsyncTask<Void, Void, Snapshots.LoadSnapshotsResult>() {
            @Override
            protected Snapshots.LoadSnapshotsResult doInBackground(Void... params) {

                Log.v(TAG, "Waiting for googleApiClient to sign in");
                googleApiClient.blockingConnect();

                boolean isConnected = googleApiClient.isConnected();
                Log.v(TAG, "Is googleApiClient connected? " + isConnected);

                Log.v(TAG, "Loading snapshots");
                Snapshots.LoadSnapshotsResult snapshotResults = null;
                if (isConnected) {
                    snapshotResults = Games.Snapshots.load(googleApiClient, true).await();
                    Log.v(TAG, "Load snapshot result finished.");
                } else {
                    Log.v(TAG, "googleApiClient is disconnected, not loading snapshots.");
                }

                if (snapshotResults != null) {
                    if (snapshotResults.getStatus().isSuccess()) {
                        Log.v(TAG, "Loaded " + snapshotResults.getSnapshots().getCount() + " snapshots");

                        for (SnapshotMetadata snapshotMetadata : snapshotResults.getSnapshots()) {

                            /**
                             * For debug only, deletes all the snapshots of the current user:
                             * ### Games.Snapshots.delete(googleApiClient, snapshotMetadata); ###
                             *
                             * When in use, comment out the line below. When done, bring the line back.
                             */
                            gameStatus.addToSaveHunts(snapshotMetadata.freeze());
                        }

                        snapshotResults.getSnapshots().release();
                    }
                }

                return snapshotResults;
            }

            @Override
            protected void onPostExecute(Snapshots.LoadSnapshotsResult snapshotResults) {
                if (snapshotResults != null) {
                    int status = snapshotResults.getStatus().getStatusCode();

                    if (status == GamesStatusCodes.STATUS_SNAPSHOT_NOT_FOUND) {
                        Log.e(TAG, "Error: Snapshot not found");
                    } else if (status
                            == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
                        Log.e(TAG, "Error: Snapshot contents unavailable");

                    } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_FOLDER_UNAVAILABLE) {
                        Log.e(TAG, "Error: Snapshot folder unavailable");

                    }
                }
            }
        }.execute();
    }

    /**
     * block the thread until the snapshot loading finish
     */
    public void waitForFinish() {
        try {
            loadSnapshotsTask.get();
            Log.d(TAG, "Snapshot list loading is finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a Snapshot from the user's synchronized storage.
     */
    public void loadFromSnapshot(final SnapshotMetadata snapshotMetadata, final Callable onFinish) {

        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult result;
                if (snapshotMetadata != null && snapshotMetadata.getUniqueName() != null) {
                    Log.d(TAG, "Opening snapshot by metadata: " + snapshotMetadata.getTitle());
                    result = Games.Snapshots.open(googleApiClient, snapshotMetadata).await();
                } else {
                    Log.d(TAG, "Opening snapshot by name: " + currentSaveName);
                    result = Games.Snapshots.open(googleApiClient, currentSaveName, true).await();
                }

                int status = result.getStatus().getStatusCode();

                Snapshot snapshot = null;
                if (status == GamesStatusCodes.STATUS_OK) {
                    snapshot = result.getSnapshot();
                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {

                    // if there is a conflict  - then resolve it.
                    snapshot = processSnapshotOpenResult(RC_LOAD_SNAPSHOT, result, 0);

                    // if it resolved OK, change the status to Ok
                    if (snapshot != null) {
                        status = GamesStatusCodes.STATUS_OK;
                    } else {
                        Log.d(TAG, "Conflict was not resolved automatically");
                    }
                } else {
                    Log.e(TAG, "Error while loading: " + status);
                }

                if (snapshot != null) {
                    //readSavedGame(snapshot);
                    gameStatus.loadHunt(snapshot.readFully(), snapshot.getMetadata());
                }
                return status;
            }

            @Override
            protected void onPostExecute(Integer status) {
                Log.i(TAG, "Snapshot loaded: " + status);

                if (status == GamesStatusCodes.STATUS_SNAPSHOT_NOT_FOUND) {
                    Log.e(TAG, "Error: Snapshot not found");
                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
                    Log.e(TAG, "Error: Snapshot contents unavailable");
                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_FOLDER_UNAVAILABLE) {
                    Log.e(TAG, "Error: Snapshot folder unavailable");
                }

                try {
                    onFinish.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        task.execute();
    }

    /**
     * save the hunt to google play
     */
    public void saveSnapshot(final String HuntID) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult openResult;
                SnapshotMetadata snapshotMetadata = gameStatus.getSnapshotMetadataById(HuntID);

                if (snapshotMetadata == null) {
                    Log.d(TAG, "snapshot null");
                    currentSaveName = "GeoFind-" + HuntID;
                    openResult = Games.Snapshots.open(googleApiClient, currentSaveName, true)
                            .await();
                } else {
                    Log.d(TAG, "continue snapshot");
                    openResult = Games.Snapshots.open(googleApiClient, snapshotMetadata)
                            .await();
                }
                final Snapshot toWrite = processSnapshotOpenResult(RC_SAVE_SNAPSHOT, openResult, 0);
                if (toWrite != null) {
                    writeSnapshot(toWrite, HuntID);
                } else {
                    Log.d(TAG, "Snapshot to write is null");
                }
                return null;
            }
        }.execute();
    }

    /**
     * Conflict resolution for when Snapshots are opened.
     *
     * @param requestCode - the request currently being processed.  This is used to forward on the
     *                    information to another activity, or to send the result intent.
     * @param result      The open snapshot result to resolve on open.
     * @param retryCount  - the current iteration of the retry.  The first retry should be 0.
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
            Log.e(TAG, "snapshot conflict");

            Snapshot snapshot = result.getSnapshot();
            Snapshot conflictSnapshot = result.getConflictingSnapshot();

            // Resolve between conflicts by selecting the newest of the conflicting snapshots.
            Snapshot mResolvedSnapshot = snapshot;

            if (snapshot.getMetadata().getLastModifiedTimestamp() <
                    conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
                mResolvedSnapshot = conflictSnapshot;
            }

            Snapshots.OpenSnapshotResult resolveResult = Games.Snapshots.resolveConflict(
                    googleApiClient, result.getConflictId(), mResolvedSnapshot)
                    .await();

            if (retryCount < 10) {
                return processSnapshotOpenResult(requestCode, resolveResult, retryCount + 1);
            } else {
                String message = "Could not resolve snapshot conflicts";
                Log.e(TAG, message);
            }
        }
        // Fail, return null.
        return null;
    }

    /**
     * Generates metadata, and performs the write operation for saving a
     * snapshot.
     */
    private void writeSnapshot(final Snapshot snapshot, String HuntID) {
        // Set the data payload for the snapshot.
        snapshot.getSnapshotContents().writeBytes(gameStatus.HuntToBytes(HuntID));

        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setDescription(gameStatus.isFinished(HuntID) ? "Finished" : "OnGoing")
                .build();

        Games.Snapshots.commitAndClose(googleApiClient, snapshot, metadataChange)
                .setResultCallback(new ResultCallback<Snapshots.CommitSnapshotResult>() {
                    @Override
                    public void onResult(Snapshots.CommitSnapshotResult commitSnapshotResult) {
                        Log.d(TAG, "Wrote snapshot " + snapshot.getMetadata().getTitle()
                        + " with status " + commitSnapshotResult.getStatus());
                    }
                });
    }


    /**
     * Interface to callback that called when the process finished
     */
    public interface ExecFinished {
        /**
         * the method to be called when the execution finished
         */
        void onFinish();
    }
}
