package com.geofind.geofind;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

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
    public Fragment getItem(int i) {
        // create new Hint fragment
        Fragment fragment = new HuntListFragment();

//        ((HuntListFragment) fragment).context = context;

        // create and fill the hunts array to display them.
        // TODO retrieve the hunts from parse
        ArrayList<Hunt> hunts = new ArrayList<Hunt>();
        hunts.add(new Hunt("Titleb1", 1, 1, "Hunt1", new LatLng(31.76831, 35.21371), 500));
        hunts.add(new Hunt("Title2", 2, 2, "Hunt2", new LatLng(31.76831, 35.21371), 1000));
        hunts.add(new Hunt("Title3", 3, 3, "Hunt3", new LatLng(31.76831, 35.21371), 200));
        hunts.add(new Hunt("Title4", 4, 4, "Hunt4", new LatLng(31.76831, 35.21371), 10000));
        hunts.add(new Hunt("Title5", 5, 5, "Hunt5", new LatLng(31.76831, 35.21371), 800));

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
            HuntListAdapter adapter = new HuntListAdapter(hunts, context);

            // set adapter
            recyclerView.setAdapter(adapter);

            // set item animator to DefaultAnimator
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            return view;
        }
    }
}
