package com.geofind.geofind;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by mickey on 04/10/14.
 */
public class HintPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * The hints that are currently displayed in the HuntActivity.
     */
    private ArrayList<Hint> hints;

    /**
     * The geofence manager instance for revealing the point
     */
    private GeofenceManager geofence;

    public HintPagerAdapter(FragmentManager fm) {
        super(fm);
        this.hints = new ArrayList<Hint>();
        this.geofence = null;
    }

    public HintPagerAdapter(FragmentManager fm, ArrayList<Hint> hints, GeofenceManager geofenceManager) {
        super(fm);
        this.hints = hints;
        Log.i(this.getClass().getName(), "set geofence to Hint adapter:" + (geofenceManager == null));
        this.geofence = geofenceManager;
    }

    @Override
    public Fragment getItem(int i) {
        // create new Hint fragment
        HintFragment fragment = new HintFragment();

        // create and add arguments to pass them to it
        Bundle args = new Bundle();
        args.putSerializable(HintFragment.TAG, hints.get(i));

        fragment.setArguments(args);
        Log.i(this.getClass().getName(), "set geofence to fragment valid:" + (geofence == null));
        fragment.set_geofenceManager(geofence);

        return fragment;
    }

    /**
     * Add a hint to the end of the list.
     *
     * @param hint The new hint to add.
     */
    public void addHint(Hint hint) {
        hints.add(hint);
    }

    @Override
    public int getCount() {
        return hints.size();
    }

    /**
     * The actual hint fragment class. From here we would inflate the hint item to the swipe view.
     */
    public static class HintFragment extends Fragment {
        /**
         * The tag used to pass the hint from the adapter to the fragment.
         */
        public static String TAG = "HINT";

        private GeofenceManager _geofenceManager;

        public void set_geofenceManager(GeofenceManager geofenceManager){
            Log.i(TAG, "set geo fence valid:" + (geofenceManager == null));
            this._geofenceManager = geofenceManager;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.item_hint_swipe_view, container, false);

            // get the related hint
            Bundle bundle = getArguments();
            final Hint hint = (Hint) bundle.getSerializable(TAG);

            // put hint details in the view
            TextView hintTitleTextView = (TextView) view.findViewById(R.id.item_hint_title);
            hintTitleTextView.setText(hint.getTitle());

            TextView hintDescriptionTextView = (TextView)
                    view.findViewById(R.id.item_hint_description);
            hintDescriptionTextView.setText(hint.getText());

            Button revealButton = (Button) view.findViewById(R.id.item_hint_reveal_button);
            Drawable drawable = null;
            switch (hint.getState()) {
                case REVEALED:
                    drawable = getResources().getDrawable(R.drawable.ic_clear_grey600_24dp);
                    revealButton.setText(getResources().getText(R.string.item_hint_revealed));
                    revealButton.setEnabled(false);
                    break;
                case UNREVEALED:
                    drawable = getResources().getDrawable(R.drawable.ic_room_grey600_24dp);
                    revealButton.setText(getResources().getText(R.string.item_hint_reveal));
                    break;
                case SOLVED:
                    drawable = getResources().getDrawable(R.drawable.ic_done_grey600_24dp);
                    revealButton.setText(getResources().getText(R.string.item_hint_solved));
                    revealButton.setEnabled(false);
                    break;
            }

            // set the needed drawable
            revealButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            revealButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "using geofence valid:" + (_geofenceManager == null));
                    _geofenceManager.removeGeofences(hint.getLocation());
                }
            });

            return view;
        }
    }
}
