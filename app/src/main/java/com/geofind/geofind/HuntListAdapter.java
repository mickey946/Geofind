package com.geofind.geofind;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.textViewTitle.setText(hunts[i].getTitle());
        viewHolder.textViewTotalDistance.setText(hunts[i].getTotalDistance().toString());
        viewHolder.ratingBar.setRating(hunts[i].getRating());
        viewHolder.textViewDescription.setText(hunts[i].getDescription());
    }

    @Override
    public int getItemCount() {
        return hunts.length;
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewTotalDistance;
        public RatingBar ratingBar;
        public TextView textViewDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.item_hunt_list_title);
            textViewTotalDistance = (TextView)
                    itemView.findViewById(R.id.item_hunt_list_total_distance);
            ratingBar = (RatingBar) itemView.findViewById(R.id.item_hunt_list_rating);
            textViewDescription = (TextView) itemView.findViewById(R.id.item_hunt_list_description);
        }
    }
}
