package com.geofind.geofind;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mickey on 06/10/14.
 */
public class HintListAdapter extends RecyclerView.Adapter<HintListAdapter.ViewHolder> {

    private ArrayList<Hint> hints;

    public HintListAdapter() {
        hints = new ArrayList<Hint>();
    }

    public int getSize() {
        return hints.size();
    }

    public ArrayList<Hint> getHints() {
        return hints;
    }

    public void setHints(ArrayList<Hint> hints) {
        this.hints = hints;
        this.notifyDataSetChanged();
    }

    public void addHint(Hint hint) {
        hints.add(hint);
        this.notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_hint_list, viewGroup, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        // assign the hint to it's ViewHolder
        viewHolder.hint = hints.get(i);

        // put the values of the hunt in all of the views
        viewHolder.hintTitleTextView.setText(hints.get(i).getTitle());
        viewHolder.hintTextTextView.setText(hints.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return hints.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {
        public Hint hint;
        public TextView hintTitleTextView;
        public TextView hintTextTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            // get the views from the layout
            hintTitleTextView = (TextView) itemView.findViewById(R.id.item_hint_list_title);
            hintTextTextView = (TextView) itemView.findViewById(R.id.item_hint_list_text);
        }
    }
}
