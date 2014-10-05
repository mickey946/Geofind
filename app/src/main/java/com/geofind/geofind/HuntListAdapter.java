package com.geofind.geofind;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * Created by mickey on 01/10/14.
 */
public class HuntListAdapter extends RecyclerView.Adapter<HuntListAdapter.ViewHolder> {

    /**
     * The array of displayed hunts.
     */
    private Hunt[] hunts;

    public HuntListAdapter(Hunt[] hunts) {
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
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        // put the values of the hunt in all of the views
        viewHolder.textViewTitle.setText(hunts[i].getTitle());
        viewHolder.textViewTotalDistance.setText(hunts[i].getTotalDistance().toString());
        viewHolder.ratingBar.setRating(hunts[i].getRating());
        viewHolder.textViewDescription.setText(hunts[i].getDescription());

        // set a listener to the button to start the hunt
        viewHolder.startHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), HuntActivity.class);

                // pass the hunt itself to the HuntDetailActivity
                intent.putExtra(v.getResources().getString(R.string.intent_hunt_extra),
                        hunts[i]);

                // start the activity
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hunts.length;
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textViewTitle;
        public TextView textViewTotalDistance;
        public RatingBar ratingBar;
        public TextView textViewDescription;
        public Button startHuntButton;

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
        }

        @Override
        public void onClick(View v) {
            // create intent to open the hunt details
            Intent intent = new Intent(v.getContext(), HuntDetailsActivity.class);

            // pass the hunt itself to the HuntDetailActivity
            intent.putExtra(v.getResources().getString(R.string.intent_hunt_extra),
                    hunts[getPosition()]);

            // start the activity
            v.getContext().startActivity(intent);
        }
    }
}
