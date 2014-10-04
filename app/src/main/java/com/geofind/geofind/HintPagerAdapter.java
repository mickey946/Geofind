package com.geofind.geofind;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by mickey on 04/10/14.
 */
public class HintPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * The hints that are currently displayed in the HuntActivity.
     */
    private Hint[] hints;

    public HintPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public HintPagerAdapter(FragmentManager fm, Hint[] hints) {
        super(fm);
        this.hints = hints;
    }

    @Override
    public Fragment getItem(int i) {
        // create new Hint fragment
        Fragment fragment = new HintFragment();

        // create and add arguments to pass them to it
        Bundle args = new Bundle();
        args.putSerializable(HintFragment.TAG, hints[i]);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return hints.length;
    }

    /**
     * The actual hint fragment class. From here we would inflate the hint item to the swipe view.
     */
    public static class HintFragment extends Fragment {
        /**
         * The tag used to pass the hint from the adapter to the fragment.
         */
        public static String TAG = "HINT";

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.item_hint_swipe_view, container, false);
            return view;
        }
    }
}
