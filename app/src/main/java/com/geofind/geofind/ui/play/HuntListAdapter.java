package com.geofind.geofind.ui.play;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.geofind.geofind.R;
import com.geofind.geofind.geoutils.StaticMap;
import com.geofind.geofind.structures.Hunt;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A {@link android.support.v7.widget.RecyclerView.Adapter} for the
 * {@link com.geofind.geofind.ui.play.HuntListActivity}.
 *
 * Created by mickey on 01/10/14.
 * Edited by Ilia on 16/10/14.
 */
public class HuntListAdapter extends RecyclerView.Adapter<HuntListAdapter.ViewHolder> {

    /**
     * The {@link java.util.ArrayList} of displayed hunts.
     */
    private ArrayList<Hunt> hunts;

    /**
     * The host {@link android.app.Activity}.
     */
    private Context context;

    /**
     * Is this a list of finished hunts (disabled start).
     */
    private Boolean isFinished;

    /**
     * The distance unit of the hunts.
     */
    private String distanceUnit;

    /**
     * This is true when the map image is drawn
     */
    private int mapWidth, mapHeight;

    public HuntListAdapter(ArrayList<Hunt> hunts, Boolean isFinished, Context context) {
        this.hunts = hunts;
        this.context = context;
        this.mapHeight = -1;
        this.mapWidth = -1;
        this.distanceUnit = context.getString(R.string.preferences_distance_units_kilometers);
        this.isFinished = isFinished;
    }

    /**
     * Set the array list of the hunts.
     *
     * @param hunts The array list of the hunts to be displayed
     */
    public void setHunts(ArrayList<Hunt> hunts) {
        this.hunts = hunts;
    }

    /**
     * Set the distance unit that will be displayed to the user.
     *
     * @param distanceUnit km or miles
     */
    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    /**
     * Get the distance unit that should be displayed to the user.
     * @return The displayed distance unit (km or miles).
     */
    public String getDistanceUnit() {
        return distanceUnit;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HuntListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_hunt_list, viewGroup, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        // put the values of the hunt in all of the views
        viewHolder.context = context;
        viewHolder.textViewTitle.setText(hunts.get(i).getTitle());

        viewHolder.ratingBar.setRating(hunts.get(i).getRating());
        viewHolder.textViewDescription.setText(hunts.get(i).getDescription());

        // set the distance unit
        Float totalDistance = hunts.get(i).getTotalDistance();
        if (distanceUnit.equals(
                context.getString(R.string.preferences_distance_units_kilometers))) {
            totalDistance *= Hunt.METERS_TO_KILOMETERS;
            viewHolder.textViewDistanceUnit.setText(
                    context.getText(R.string.item_hunt_list_distance_unit_km));
        } else {
            totalDistance *= Hunt.METERS_TO_MILES;
            viewHolder.textViewDistanceUnit.setText(
                    context.getText(R.string.item_hunt_list_distance_unit_miles));
        }

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(Hunt.DIGIT_PRECISION);
        viewHolder.textViewTotalDistance.setText(decimalFormat.format(totalDistance));

        ViewTreeObserver vto = viewHolder.itemView.getViewTreeObserver();
        if (mapHeight == -1 || mapWidth == -1) {
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewHolder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mapHeight = viewHolder.mapPreview.getHeight();
                        mapWidth = viewHolder.mapPreview.getWidth();

                        // should be called when mapPreview exists
                        new StaticMap(viewHolder.mapPreview, viewHolder.progressBar)
                                .execute(new StaticMap.StaticMapDescriptor(
                                        hunts.get(i).getCenterPosition(),
                                        hunts.get(i).getRadius(), mapWidth, mapHeight));

                    }
                });
            }
        } else {
            // The recycler view doesn't create new tiles, so we reuse previous tile and assume
            // the same dimension for image view
            new StaticMap(viewHolder.mapPreview, viewHolder.progressBar)
                    .execute(new StaticMap.StaticMapDescriptor(
                            hunts.get(i).getCenterPosition(),
                            hunts.get(i).getRadius(), mapWidth, mapHeight));
        }

        if (isFinished) { // this is a finished hunt, disable the start button
            viewHolder.startHuntButton.setVisibility(View.GONE);
        } else { // set a listener to the button to start the hunt
            viewHolder.startHuntButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), HuntActivity.class);

                    // pass the hunt itself to the HuntDetailActivity
                    intent.putExtra(v.getResources().getString(R.string.intent_hunt_extra),
                            hunts.get(i));

                    // start the activity
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return hunts.size();
    }

    /**
     * Inner class to hold a reference to each item of
     * {@link android.support.v7.widget.RecyclerView}.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /**
         * The {@link android.content.Context} where the item is displayed.
         */
        public Context context;

        /**
         * The {@link android.widget.TextView} that shows the title of the hunt.
         */
        public TextView textViewTitle;

        /**
         * The {@link android.widget.TextView} that shows the total distance of the hunt.
         */
        public TextView textViewTotalDistance;

        /**
         * The {@link android.widget.TextView} that shows the distance unit of the total distance.
         */
        public TextView textViewDistanceUnit;

        /**
         * The {@link android.widget.RatingBar} that shows the rating of the hunt.
         */
        public RatingBar ratingBar;

        /**
         * The {@link android.widget.TextView} that shows the description of the hunt.
         */
        public TextView textViewDescription;

        /**
         * The {@link android.widget.Button} that starts the hunt.
         */
        public Button startHuntButton;

        /**
         * The {@link android.widget.ImageView} that shows the map preview of the hunt.
         */
        public ImageView mapPreview;

        /**
         * The {@link android.widget.ProgressBar} that is shown until the map preview loads.
         */
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            // set an on click listener to make the cards clickable
            itemView.setOnClickListener(this);

            // get the views from the layout
            textViewTitle = (TextView) itemView.findViewById(R.id.item_hunt_list_title);
            textViewTotalDistance = (TextView)
                    itemView.findViewById(R.id.item_hunt_list_total_distance);
            textViewDistanceUnit = (TextView)
                    itemView.findViewById(R.id.item_hunt_list_distance_unit);
            ratingBar = (RatingBar) itemView.findViewById(R.id.item_hunt_list_rating);
            textViewDescription = (TextView) itemView.findViewById(R.id.item_hunt_list_description);
            startHuntButton = (Button) itemView.findViewById(R.id.item_hunt_list_start_hunt_button);
            mapPreview = (ImageView) itemView.findViewById(R.id.item_hunt_list_map_preview);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        @Override
        public void onClick(View v) {
            // create intent to open the hunt details
            Intent intent = new Intent(v.getContext(), HuntDetailsActivity.class);

            // pass the hunt itself to the HuntDetailActivity
            intent.putExtra(v.getResources().getString(R.string.intent_hunt_extra),
                    hunts.get(getPosition()));
            intent.putExtra(v.getResources().getString(R.string.hunt_is_finished), isFinished);

            // start the activity
            v.getContext().startActivity(intent);
        }
    }
}
