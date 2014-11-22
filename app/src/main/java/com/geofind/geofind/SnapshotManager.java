package com.geofind.geofind;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ilia Marin on 14/11/2014.
 */
public class SnapshotManager {

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    // Request code for listing saved games
    private static final int RC_LIST_SAVED_GAMES = 9002;
    // Request code for selecting a snapshot
    private static final int RC_SELECT_SNAPSHOT = 9003;
    // Request code for saving the game to a snapshot.
    private static final int RC_SAVE_SNAPSHOT = 9004;
    private static final int RC_LOAD_SNAPSHOT = 9005;
    private static String TAG = "SnapshotManager";
    private GoogleApiClient mGoogleApiClient;
    private GameStatus gameStatus;
    private String currentSaveName = "snapshotTemp";
    private Context context;
    private ProgressDialog mLoadingDialog = null;
    private AsyncTask<Void, Void, Snapshots.LoadSnapshotsResult> task;

    public SnapshotManager(Context context, GoogleApiClient client) {
        this.context = context;
        mGoogleApiClient = client;
        gameStatus = ((GeofindApp) context.getApplicationContext()).getGameStatus();
    }

    public void loadSnapshot(final ExecFinished callback) {
//        if (mLoadingDialog == null) {
//            mLoadingDialog = new ProgressDialog(context);
//            mLoadingDialog.setMessage("Loading");
//        }
//        mLoadingDialog.show();
        Log.d("Load","assign async task");
        //Start an asynchronous task to read this snapshot and load it.
        task = new AsyncTask<Void, Void, Snapshots.LoadSnapshotsResult>() {
            @Override
            protected Snapshots.LoadSnapshotsResult doInBackground(Void... params) {

                Log.i(TAG, "Listing snapshots");


                Snapshots.LoadSnapshotsResult snapshotResults = Games.Snapshots.load(mGoogleApiClient, true).await();

                if (snapshotResults.getStatus().isSuccess()) {
                    ArrayList<SnapshotMetadata> items = new ArrayList<SnapshotMetadata>();
                    Log.i(TAG, "loaded " + snapshotResults.getSnapshots().getCount() + " snapshots");
                    for (SnapshotMetadata m : snapshotResults.getSnapshots()) {
                        //loadFromSnapshot(m.freeze());
                        // This is a hack to clear saved games
                        //Games.Snapshots.delete(mGoogleApiClient,m);
                        gameStatus.addToSaveHunts(m.freeze());


                    }

                    snapshotResults.getSnapshots().release();
                }

                return  snapshotResults;
            }

            @Override
            protected void onPostExecute(Snapshots.LoadSnapshotsResult snapshotResults) {

//                        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
//                            mLoadingDialog.dismiss();
//                            mLoadingDialog = null;
//                        }
                int status = snapshotResults.getStatus().getStatusCode();

                // Note that showing a toast is done here for debugging. Your application should
                // resolve the error appropriately to your app.
                if (status == GamesStatusCodes.STATUS_SNAPSHOT_NOT_FOUND) {
                    Log.i(TAG, "Error: Snapshot not found");
                    Toast.makeText(context, "Error: Snapshot not found",
                            Toast.LENGTH_SHORT).show();
                } else if (status
                        == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
                    Log.i(TAG, "Error: Snapshot contents unavailable");
                    Toast.makeText(context, "Error: Snapshot contents unavailable",
                            Toast.LENGTH_SHORT).show();
                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_FOLDER_UNAVAILABLE) {
                    Log.i(TAG, "Error: Snapshot folder unavailable");
                    Toast.makeText(context, "Error: Snapshot folder unavail able.",
                            Toast.LENGTH_SHORT).show();
                }

                callback.onFinish();


            }
        };


        Log.d("Load","execute async task");
        task.execute();
    }

    public void waitforfinish(){
        Log.d("Load", "task is null " + (task == null));
        if (task!=null) {
            try {
                task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Loads a Snapshot from the user's synchronized storage.
     */
    public void loadFromSnapshot(final SnapshotMetadata snapshotMetadata, final Callable onFinish) {
//        if (mLoadingDialog == null) {
//            mLoadingDialog = new ProgressDialog(context);
//            mLoadingDialog.setMessage("loading");
//        }
//
//        mLoadingDialog.show();

        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult result;
                if (snapshotMetadata != null && snapshotMetadata.getUniqueName() != null) {
                    Log.i(TAG, "Opening snapshot by metadata: " + snapshotMetadata);
                    result = Games.Snapshots.open(mGoogleApiClient, snapshotMetadata).await();
                } else {
                    Log.i(TAG, "Opening snapshot by name: " + currentSaveName);
                    result = Games.Snapshots.open(mGoogleApiClient, currentSaveName, true).await();
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
                        Log.w(TAG, "Conflict was not resolved automatically");
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

                // Note that showing a toast is done here for debugging. Your application should
                // resolve the error appropriately to your app.
                if (status == GamesStatusCodes.STATUS_SNAPSHOT_NOT_FOUND) {
                    Log.i(TAG, "Error: Snapshot not found");
                    Toast.makeText(context, "Error: Snapshot not found",
                            Toast.LENGTH_SHORT).show();
                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
                    Log.i(TAG, "Error: Snapshot contents unavailable");
                    Toast.makeText(context, "Error: Snapshot contents unavailable",
                            Toast.LENGTH_SHORT).show();
                } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_FOLDER_UNAVAILABLE) {
                    Log.i(TAG, "Error: Snapshot folder unavailable");
                    Toast.makeText(context, "Error: Snapshot folder unavailable.",
                            Toast.LENGTH_SHORT).show();
                }

                try {
                    onFinish.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
//                    mLoadingDialog.dismiss();
//                    mLoadingDialog = null;
//                }
//                hideAlertBar();
//                updateUi();
            }
        };

        task.execute();
    }

    public void saveSnapshot(final String HuntID) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Snapshots.OpenSnapshotResult openResult;
                SnapshotMetadata snapshotMetadata = gameStatus.getSnapshotMetadataById(HuntID);

                if (snapshotMetadata == null) {
                    Log.d(TAG, "snapshot null");
                    currentSaveName = "GeoFind-" + HuntID;
                    openResult = Games.Snapshots.open(mGoogleApiClient, currentSaveName, true)
                            .await();
                } else {
                    Log.d(TAG, "continue snapshot");
                    openResult = Games.Snapshots.open(mGoogleApiClient, snapshotMetadata)
                            .await();
                }
                final Snapshot toWrite = processSnapshotOpenResult(RC_SAVE_SNAPSHOT, openResult, 0);
                writeSnapshot(toWrite, HuntID);
                return null;
            }
//
//                    @Override
//                    protected void onPostExecute(Snapshots.OpenSnapshotResult openSnapshotResult) {
//
//
////                        new AsyncTask<Void, Void, Void>() {
////                            @Override
////                            protected Void doInBackground(Void... params) {
////
////
////
////                                return null;
////                            }
////                        }.execute();
//
//
//                    }
        };
        task.execute();
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
//            Toast.makeText(context, "snapshot conflict", Toast.LENGTH_LONG);
//            final Snapshot snapshot = result.getSnapshot();
//            final Snapshot conflictSnapshot = result.getConflictingSnapshot();
//
//            Log.d(TAG,"snapshot:" + snapshot.getMetadata().getUniqueName());
//            Log.d(TAG,"conflict:" + conflictSnapshot.getMetadata().getUniqueName());

            Snapshot snapshot = result.getSnapshot();
            Snapshot conflictSnapshot = result.getConflictingSnapshot();

            // Resolve between conflicts by selecting the newest of the conflicting snapshots.
            Snapshot mResolvedSnapshot = snapshot;

            if (snapshot.getMetadata().getLastModifiedTimestamp() <
                    conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
                mResolvedSnapshot = conflictSnapshot;
            }

            Snapshots.OpenSnapshotResult resolveResult = Games.Snapshots.resolveConflict(
                    mGoogleApiClient, result.getConflictId(), mResolvedSnapshot)
                    .await();

            if (retryCount < 10) {
                return processSnapshotOpenResult(requestCode, resolveResult, retryCount + 1);
            } else {
                String message = "Could not resolve snapshot conflicts";
                Log.e(TAG, message);
                Toast.makeText(context, message, Toast.LENGTH_LONG);
            }


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
    private void writeSnapshot(final Snapshot snapshot, String HuntID) {
        // Set the data payload for the snapshot.


        snapshot.getSnapshotContents().writeBytes(gameStatus.HuntToBytes(HuntID));
        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                //.setCoverImage(getScreenShot())
                //.setDescription("Modified data at: " + Calendar.getInstance().getTime())
                .setDescription(gameStatus.isFinished(HuntID) ? "Finished" : "OnGoing")
                .build();

        Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange).setResultCallback(new ResultCallback<Snapshots.CommitSnapshotResult>() {
            @Override
            public void onResult(Snapshots.CommitSnapshotResult commitSnapshotResult) {
                Log.d(TAG, snapshot.toString());
            }
        });

    }


    public interface ExecFinished {
        void onFinish();
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
