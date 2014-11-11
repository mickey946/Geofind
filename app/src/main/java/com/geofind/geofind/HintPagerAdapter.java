package com.geofind.geofind;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


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

    private List<HintFragment> _fragments;

    public HintPagerAdapter(FragmentManager fm, ArrayList<Hint> hints, GeofenceManager geofenceManager) {
        super(fm);
        this.hints = hints;
        Log.i(this.getClass().getName(), "set geofence to Hint adapter:" + (geofenceManager == null));
        this.geofence = geofenceManager;
        _fragments = new ArrayList<HintFragment>();
    }

    @Override
    public Fragment getItem(int i) {
        // create new Hint fragment
        HintFragment fragment = new HintFragment();

        // create and add arguments to pass them to it
        Bundle args = new Bundle();
        args.putSerializable(HintFragment.HINT_TAG, hints.get(i));
        args.putSerializable(HintFragment.INDEX_TAG, i);
        fragment.setArguments(args);
        fragment.set_geofenceManager(geofence);
        _fragments.add(fragment);

        return fragment;
    }

    public void invalidateFragment(int index) {
        if (index < _fragments.size()) {
            _fragments.get(index).getView().findViewById(R.id.item_hint_reveal_button).invalidate();
        }

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
        int i = 0;
        while ((i < hints.size()) && (hints.get(i).getState() != Hint.State.UNREVEALED)) {
            i++;
        }
        return Math.min(i + 1, hints.size());
    }

    // TODO remove this function when using the approach described in:
    // http://stackoverflow.com/questions/7263291/viewpager-pageradapter-not-updating-the-view/8024557#8024557
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * The actual hint fragment class. From here we would inflate the hint item to the swipe view.
     */
    public static class HintFragment extends Fragment {
        /**
         * The tag used to pass the hint from the adapter to the fragment.
         */
        public static String HINT_TAG = "HINT";

        /**
         * The tag used to pass the index of the hint from the adapter to the fragment.
         */
        public static String INDEX_TAG = "HINT_INDEX";

        /**
         * The geofence control class for revealing the hint point
         */
        private GeofenceManager _geofenceManager;

        /**
         * give the access to the geofence manager for revealing the hint point
         */
        public void set_geofenceManager(GeofenceManager geofenceManager) {
            this._geofenceManager = geofenceManager;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.item_hint_swipe_view, container, false);

            // get the related hint
            Bundle bundle = getArguments();
            final Hint hint = (Hint) bundle.getSerializable(HINT_TAG);
            int index = bundle.getInt(INDEX_TAG) + 1;

            // put hint details in the view
            TextView hintTitleTextView = (TextView) view.findViewById(R.id.item_hint_title);
            hintTitleTextView.setText(getString(R.string.hunt_activity_hint_number_title) + index);

            TextView hintDescriptionTextView = (TextView)
                    view.findViewById(R.id.item_hint_description);
            hintDescriptionTextView.setText(hint.getText());

            final Button revealButton = (Button) view.findViewById(R.id.item_hint_reveal_button);
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
                    _geofenceManager.removeGeofences(hint.getLocation());
                    /**
                     * on revealing the point update it's text and icon
                     */
                    Drawable drawable1 = getResources().getDrawable(R.drawable.ic_clear_grey600_24dp);
                    revealButton.setText(getResources().getText(R.string.item_hint_revealed));
                    revealButton.setEnabled(false);
                    revealButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable1, null, null);
                    /**
                     * must be done for visualize the change
                     */
                    revealButton.invalidate();

                }
            });

            if (hint.hasImage()) {
                hint.downloadImage(new Hint.DownloadImage() {
                    @Override
                    public void updateImage(Bitmap inputBitmap) {
                        View imageLayout = view.findViewById(R.id.item_hint_image_layout);
                        imageLayout.setVisibility(View.VISIBLE);

                        ImageView hintImage = (ImageView) view.findViewById(R.id.item_hint_picture);

                        // zoom the image to improve view responsiveness
                        Bitmap displayBitmap;
                        if (inputBitmap.getWidth() >= inputBitmap.getHeight()) {
                            displayBitmap = Bitmap.createBitmap(
                                    inputBitmap,
                                    inputBitmap.getWidth() / 2 - inputBitmap.getHeight() / 2,
                                    0,
                                    inputBitmap.getHeight(),
                                    inputBitmap.getHeight()
                            );
                        } else {
                            displayBitmap = Bitmap.createBitmap(
                                    inputBitmap,
                                    0,
                                    inputBitmap.getHeight() / 2 - inputBitmap.getWidth() / 2,
                                    inputBitmap.getWidth(),
                                    inputBitmap.getWidth()
                            );
                        }
                        hintImage.setImageBitmap(displayBitmap);

                        hintImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ContentViewActivity.class);
                                intent.putExtra(ContentViewActivity.IMAGE_PARSE, hint);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }

            if (hint.hasVideo()) {
                hint.downloadVideoAudio(new Hint.DownloadVideoAudio() {
                    @Override
                    public void updateVideoAudio(String link) {
                        //TODO: implement this.
                        Log.v("In update Video", "Video url: " + link);
                    }
                }, Hint.PARSE_VIDEO_FIELD);
            }

            if (hint.hasAudio()) {
                hint.downloadVideoAudio(new Hint.DownloadVideoAudio() {
                    @Override
                    public void updateVideoAudio(String link) {
                        //TODO: implement this.
                        Log.v("In update Audio", "Audio url: " + link);
                    }
                }, Hint.PARSE_AUDIO_FIELD);
            }

            return view;
        }
    }
}
