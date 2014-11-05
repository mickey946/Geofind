package com.geofind.geofind;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 * sections of the app.
 * <p/>
 * Created by mickey on 23/10/14.
 */
public class HuntListPagerAdapter extends FragmentPagerAdapter {
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
    Context context;

    public HuntListPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(final int i) {
        //TODO need to get data on user - List of Finished Hunts, List of OngoingHunts.
        // create new Hint fragment
        final Fragment fragment = new HuntListFragment();

        // create and fill the hunts array to display them.

        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserData");
        final ArrayList<Object> onGoingHunts = new ArrayList<Object>();
        final ArrayList<Object> finishedHunts = new ArrayList<Object>();

        final ArrayList<Hunt> hunts = new ArrayList<Hunt>();
        final ParseQuery<ParseObject> huntsQuery = ParseQuery.getQuery("Hunt");

        //TODO replace the SECOND "userID" with google use id.
        userQuery.whereEqualTo("userID", "userID");
        userQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (!parseObjects.isEmpty()) {
                        ParseObject userData = parseObjects.get(0);
                        onGoingHunts.addAll(userData.getList("ongingHunts"));
                        finishedHunts.addAll(userData.getList("finishedHunts"));

                        switch (i) {
                            case NEW_HUNTS:
                                huntsQuery.whereNotEqualTo("objectId", onGoingHunts);
                                huntsQuery.whereNotEqualTo("objectId", finishedHunts);
                            case ONGOING_HUNTS:
                                huntsQuery.whereEqualTo("objectId", onGoingHunts);

                            case FINISHED_HUNTS:
                                huntsQuery.whereEqualTo("objectId", finishedHunts);
                        }
                    } else {
                        Log.v("Retrieving Hunts Failed: ", "Hunt List is empty.");
                    }
                } else {
                    Log.v("Retrieving Hunts Failed: ", "Parse Exception - " + e.getMessage());
                }
            }
        });


        huntsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : parseObjects) {
                        hunts.add(new Hunt(parseObject));

                    }
                    ((HuntListFragment) fragment).setHunts(hunts);
                } else {
                    Toast.makeText(context, "Could NOT load Hunt list. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // create and add arguments to pass them to it
        Bundle args = new Bundle();
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

        public Context context;
        public HuntListAdapter adapter;

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

            // get a reference to recyclerView
            RecyclerView recyclerView = (RecyclerView)
                    view.findViewById(R.id.recycler_view);

            // get the related hunt list
            Bundle bundle = getArguments();
            ArrayList<Hunt> hunts = (ArrayList<Hunt>) bundle.getSerializable(HUNT_LIST_TAG);

            // set layoutManger
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // create an adapter
            adapter = new HuntListAdapter(hunts, context);

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
}
