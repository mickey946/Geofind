package com.geofind.geofind;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


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

            // get the related hint
            Bundle bundle = getArguments();
            Hint hint = (Hint) bundle.getSerializable(TAG);

            // put hint details in the view
            TextView hintTitleTextView = (TextView) view.findViewById(R.id.item_hint_title);
            hintTitleTextView.setText(hint.getTitle());

            TextView hintDescriptionTextView = (TextView)
                    view.findViewById(R.id.item_hint_description);
            hintDescriptionTextView.setText(hint.getDescription());

            Button revealButton = (Button) view.findViewById(R.id.item_hint_reveal_button);
            Drawable drawable;
            switch (hint.getState()) {
                case REVEALED:
                    drawable = getResources().getDrawable(R.drawable.ic_action_cancel);
                    revealButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    revealButton.setText(getResources().getText(R.string.item_hint_revealed));
                    revealButton.setEnabled(false);
                    break;
                case UNREVEALED:
                    drawable = getResources().getDrawable(R.drawable.ic_action_place);
                    revealButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    revealButton.setText(getResources().getText(R.string.item_hint_reveal));
                    break;
                case SOLVED:
                    drawable = getResources().getDrawable(R.drawable.ic_action_accept);
                    revealButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    revealButton.setText(getResources().getText(R.string.item_hint_solved));
                    revealButton.setEnabled(false);
                    break;
            }

            return view;
        }
    }
}
