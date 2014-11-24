package com.geofind.geofind.ui.create;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geofind.geofind.geoutils.GeofenceManager;
import com.geofind.geofind.R;
import com.geofind.geofind.structures.Hint;
import com.geofind.geofind.ui.common.ContentViewActivity;

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
                // show the card view of the image with a loading spinner until the image is loaded
                View imageCardView = view.findViewById(R.id.item_hint_image_layout);
                imageCardView.setVisibility(View.VISIBLE);

                final ImageView hintImage = (ImageView) view.findViewById(R.id.item_hint_picture);

                hint.downloadImage(new Hint.DownloadImage() {
                    @Override
                    public void onImageReceive(Bitmap inputBitmap) {
                        // the image has loaded, hide the spinner and show the image
                        ProgressBar progressBar = (ProgressBar)
                                view.findViewById(R.id.image_progress_bar);
                        progressBar.setVisibility(View.GONE);

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
                    }

                    @Override
                    public void onUrlReceive(final String url) {
                        // the url of the image has been received - pass it to the content view
                        hintImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ContentViewActivity.class);
                                intent.putExtra(ContentViewActivity.IMAGE_URL, url);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }

            if (hint.hasVideo()) {
                View videoCardView = view.findViewById(R.id.item_hint_video_layout);
                videoCardView.setVisibility(View.VISIBLE);

                hint.downloadVideoAudio(new Hint.DownloadVideoAudio() {
                    @Override
                    public void onUrlReceive(final String url) {
                        View videoPlayLayout = view.findViewById(R.id.item_hint_play_video_layout);
                        videoPlayLayout.setVisibility(View.VISIBLE);

                        Button playVideo = (Button) view.findViewById(R.id.item_hint_play_video);
                        playVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ContentViewActivity.class);
                                intent.putExtra(ContentViewActivity.VIDEO_AUDIO_URL, url);
                                startActivity(intent);
                            }
                        });

                        Log.v("In update Video", "Video url: " + url);
                    }
                }, Hint.PARSE_VIDEO_FIELD);
            }

            if (hint.hasAudio()) {
                View audioCardView = view.findViewById(R.id.item_hint_audio_layout);
                audioCardView.setVisibility(View.VISIBLE);

                hint.downloadVideoAudio(new Hint.DownloadVideoAudio() {
                    @Override
                    public void onUrlReceive(final String url) {
                        View videoPlayLayout = view.findViewById(R.id.item_hint_play_audio_layout);
                        videoPlayLayout.setVisibility(View.VISIBLE);

                        Button playAudio = (Button) view.findViewById(R.id.item_hint_play_audio);
                        playAudio.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ContentViewActivity.class);
                                intent.putExtra(ContentViewActivity.VIDEO_AUDIO_URL, url);
                                startActivity(intent);
                            }
                        });

                        Log.v("In update Audio", "Audio url: " + url);
                    }
                }, Hint.PARSE_AUDIO_FIELD);
            }

            return view;
        }
    }
}
