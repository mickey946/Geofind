package com.geofind.geofind;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private Context context;

    public HintListAdapter(Context context) {
        hints = new ArrayList<Hint>();
        this.context = context;
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

    public void setHint(int i, Hint hint) {
        hints.set(i, hint);
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
        // bind the context activity to the view holder
        viewHolder.context = context;

        // assign the hint to it's ViewHolder
        viewHolder.hint = hints.get(i);
        viewHolder.i = i;

        // put the values of the hunt in all of the views
        viewHolder.hintTitleTextView.setText(hints.get(i).getTitle());
        viewHolder.hintTextTextView.setText(hints.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return hints.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Hint hint;
        public Integer i;
        public TextView hintTitleTextView;
        public TextView hintTextTextView;
        public Context context;

        public ViewHolder(View itemView) {
            super(itemView);

            // set an on click listener to make the cards clickable
            itemView.setOnClickListener(this);

            // get the views from the layout
            hintTitleTextView = (TextView) itemView.findViewById(R.id.item_hint_list_title);
            hintTextTextView = (TextView) itemView.findViewById(R.id.item_hint_list_text);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), CreateHintActivity.class);
            intent.putExtra(view.getResources().getString(R.string.intent_hint_extra), hint);
            intent.putExtra(view.getResources().getString(R.string.intent_hint_index_extra), i);
            ((Activity) context).startActivityForResult(intent,
                    view.getResources().getInteger(R.integer.intent_hint_edit_extra));
        }
    }
}
