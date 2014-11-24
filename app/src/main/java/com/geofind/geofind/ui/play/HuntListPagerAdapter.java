package com.geofind.geofind.ui.play;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geofind.geofind.playutils.GameStatus;
import com.geofind.geofind.GeofindApp;
import com.geofind.geofind.R;
import com.geofind.geofind.playutils.SnapshotManager;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.structures.Hunt;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 * sections of the app.
 * <p/>
 * Created by mickey on 23/10/14.
 */
public class HuntListPagerAdapter extends FragmentStatePagerAdapter {
    /**
     * Number of different Hunt lists.
     */
    private static final int NUMBER_OF_PAGES = 3;

    /**
     * New Hunts page number.
     */
    private static final int NEW_HUNTS = 0;

    /**
     * Ongoing Hunts page number.
     */
    private static final int ONGOING_HUNTS = 1;

    /**
     * Finished Hunts page number.
     */
    private static final int FINISHED_HUNTS = 2;

    /**
     * The host activity.
     */
    BaseGameActivity context;

    /**
     * The snapshot manager from the host activity.
     */
    SnapshotManager snapshotManager;

    public HuntListPagerAdapter(FragmentManager fm, BaseGameActivity context,
                                SnapshotManager snapshotManager) {
        super(fm);
        this.context = context;
        this.snapshotManager = snapshotManager;
    }

    @Override
    public Fragment getItem(final int i) {
        Log.d("Load", "pager adapter get item " + i);

        // create new Hint fragment
        final Fragment fragment = new HuntListFragment();

        // create and fill the hunts array to display them.
        final ArrayList<Hunt> hunts = new ArrayList<Hunt>();
        final ParseQuery<ParseObject> huntsQuery = ParseQuery.getQuery(Hunt.PARSE_CLASS_NAME);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                snapshotManager.waitforfinish();

                Log.d("Load asyncTask", "snapshotManager finished");

                GameStatus gameStatus = ((GeofindApp) (context.getApplicationContext())).
                        getGameStatus();

                switch (i) {
                    case NEW_HUNTS:
                        huntsQuery.whereNotContainedIn(
                                context.getString(R.string.parse_objectID_field_name),
                                gameStatus.getPlayed());
                        break;
                    case ONGOING_HUNTS:
                        huntsQuery.whereContainedIn(
                                context.getString(R.string.parse_objectID_field_name),
                                gameStatus.getOnGoing());
                        break;
                    case FINISHED_HUNTS:
                        huntsQuery.whereContainedIn(
                                context.getString(R.string.parse_objectID_field_name),
                                gameStatus.getFinished());
                        break;
                }

                huntsQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            // remove progress bar
                            ((HuntListFragment) fragment).progressBar
                                    .setVisibility(View.GONE);

                            if (!parseObjects.isEmpty()) {
                                for (ParseObject parseObject : parseObjects) {
                                    hunts.add(new Hunt(parseObject));
                                }
                                ((HuntListFragment) fragment).setHunts(hunts);

                            } else { // empty list
                                ((HuntListFragment) fragment).emptyListTextView
                                        .setVisibility(View.VISIBLE);
                            }

                        } else {
                            Toast.makeText(context, "Could NOT load Hunt list. Please try again.",
                                    Toast.LENGTH_LONG).show();
                            System.out.println(e.getMessage());
                        }
                    }
                });

                return null;
            }
        }.execute();

        // create and add arguments to pass them to it
        Bundle args = new Bundle();
        args.putBoolean(HuntListFragment.FINISHED_LIST_TAG, i == FINISHED_HUNTS);
        args.putSerializable(HuntListFragment.HUNT_LIST_TAG, hunts);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return NUMBER_OF_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position) {
            case NEW_HUNTS:
                title = context.getString(R.string.hunt_list_pager_new_hunts_title);
                break;
            case ONGOING_HUNTS:
                title = context.getString(R.string.hunt_list_pager_ongoing_hunts_title);
                break;
            case FINISHED_HUNTS:
                title = context.getString(R.string.hunt_list_pager_finished_hunts_title);
                break;
            default: // should not happen
                title = context.getString(R.string.hunt_list_pager_new_hunts_title);
                break;
        }

        return title;
    }

    /**
     * The actual Hunt list.
     */
    public static class HuntListFragment extends Fragment {

        public static final String HUNT_LIST_TAG = "HUNT_LIST";
        public static final String FINISHED_LIST_TAG = "FINISHED_LIST";

        public Context context;
        public HuntListAdapter adapter;
        public ProgressBar progressBar;
        public TextView emptyListTextView;

        /**
         * Get the current distance unit that is saved in the settings file.
         *
         * @return A string representing the distance unit.
         */
        private String getCurrentDistanceUnit() {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getString(
                    this.getString(R.string.pref_key_distance_units),
                    this.getString(R.string.preferences_distance_units_kilometers));
        }

        @Override
        public void onResume() {
            String distanceUnit = getCurrentDistanceUnit();
            if (!adapter.getDistanceUnit().equals(distanceUnit)) {
                adapter.setDistanceUnit(distanceUnit);
                adapter.notifyDataSetChanged();
            }
            super.onResume();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.item_hunt_list_pager, container, false);
            context = view.getContext();

            // get a reference to the progress bar
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

            // get a reference to the empty list text view
            emptyListTextView = (TextView) view.findViewById(R.id.hunt_list_empty);

            // get a reference to recyclerView
            RecyclerView recyclerView = (RecyclerView)
                    view.findViewById(R.id.recycler_view);

            // get the related hunt list
            Bundle bundle = getArguments();
            ArrayList<Hunt> hunts = (ArrayList<Hunt>) bundle.getSerializable(HUNT_LIST_TAG);
            Boolean isFinished = bundle.getBoolean(FINISHED_LIST_TAG);

            // set layoutManger
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // create an adapter
            adapter = new HuntListAdapter(hunts, isFinished, context);

            // set the distance unit
            String distanceUnit = getCurrentDistanceUnit();
            adapter.setDistanceUnit(distanceUnit);

            // set adapter
            recyclerView.setAdapter(adapter);

            // set item animator to DefaultAnimator
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            return view;
        }

        public void setHunts(ArrayList<Hunt> hunts) {
            adapter.setHunts(hunts);
            adapter.notifyDataSetChanged();
        }
    }

    private List<String> parse(List<String> idList) {
        ArrayList<String> result = new ArrayList<String>();
        for (String id : idList) {
            result.add(id.substring(0, id.indexOf('$')));
        }
        return result;
    }
}
