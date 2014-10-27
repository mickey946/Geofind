package com.geofind.geofind;

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

import java.util.ArrayList;

/**
 * Created by mickey on 01/10/14.
 * Edited by Ilia on 16/10/14.
 */
public class HuntListAdapter extends RecyclerView.Adapter<HuntListAdapter.ViewHolder> {

    /**
     * The array of displayed hunts.
     */
    private ArrayList<Hunt> hunts;

    /**
     * The host activity.
     */
    private Context context;

    /**
     * This is true when the map image is drawn
     */
    private int mapWidth, mapHeight;

    public HuntListAdapter(ArrayList<Hunt> hunts, Context context) {
        this.hunts = hunts;
        this.context = context;
        this.mapHeight = -1;
        this.mapWidth = -1;
    }

    /**
     * Get the hunts array list.
     *
     * @return The array list of the hunts.
     */
    public ArrayList<Hunt> getHunts() {
        return hunts;
    }

    /**
     * Set the array list of the hunts.
     *
     * @param hunts The array list of the hunts to be displayed
     */
    public void setHunts(ArrayList<Hunt> hunts) {
        this.hunts = hunts;
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
        viewHolder.textViewTotalDistance.setText(hunts.get(i).getTotalDistance().toString());
        viewHolder.ratingBar.setRating(hunts.get(i).getRating());
        viewHolder.textViewDescription.setText(hunts.get(i).getDescription());

        ViewTreeObserver vto = viewHolder.itemView.getViewTreeObserver();
        if (mapHeight == -1 || mapWidth == -1) {
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewHolder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mapHeight = viewHolder.imgMapPreview.getHeight();
                        mapWidth = viewHolder.imgMapPreview.getWidth();

                        // should be called when imgMapPreview exists
                        new StaticMap(viewHolder.imgMapPreview, viewHolder.progressBar)
                                .execute(new StaticMap.StaticMapDescriptor(
                                        hunts.get(i).getCenterPosition(),
                                        hunts.get(i).getRadius(), mapWidth, mapHeight));

                    }
                });
            }
        } else {
            // The recycler view doesn't create new tiles, so we reuse previous tile and assume
            // the same dimension for image view
            new StaticMap(viewHolder.imgMapPreview, viewHolder.progressBar)
                    .execute(new StaticMap.StaticMapDescriptor(
                            hunts.get(i).getCenterPosition(),
                            hunts.get(i).getRadius(), mapWidth, mapHeight));
        }

        // set a listener to the button to start the hunt
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

    @Override
    public int getItemCount() {
        return hunts.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Context context;
        public TextView textViewTitle;
        public TextView textViewTotalDistance;
        public RatingBar ratingBar;
        public TextView textViewDescription;
        public Button startHuntButton;
        public ImageView imgMapPreview;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            // set an on click listener to make the cards clickable
            itemView.setOnClickListener(this);

            // get the views from the layout
            textViewTitle = (TextView) itemView.findViewById(R.id.item_hunt_list_title);
            textViewTotalDistance = (TextView)
                    itemView.findViewById(R.id.item_hunt_list_total_distance);
            ratingBar = (RatingBar) itemView.findViewById(R.id.item_hunt_list_rating);
            textViewDescription = (TextView) itemView.findViewById(R.id.item_hunt_list_description);
            startHuntButton = (Button) itemView.findViewById(R.id.item_hunt_list_start_hunt_button);
            imgMapPreview = (ImageView) itemView.findViewById(R.id.item_hunt_list_map_preview);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        @Override
        public void onClick(View v) {
            // create intent to open the hunt details
            Intent intent = new Intent(v.getContext(), HuntDetailsActivity.class);

            // pass the hunt itself to the HuntDetailActivity
            intent.putExtra(v.getResources().getString(R.string.intent_hunt_extra),
                    hunts.get(getPosition()));

            // start the activity
            v.getContext().startActivity(intent);
        }
    }
}
