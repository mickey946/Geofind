package com.geofind.geofind;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mickey on 06/10/14.
 */
public class HintListAdapter extends RecyclerView.Adapter<HintListAdapter.ViewHolder> {

    /**
     * The hints to display.
     */
    private ArrayList<Hint> hints;

    /**
     * The host activity.
     */
    private Context context;

    public HintListAdapter(Context context) {
        hints = new ArrayList<Hint>();
        this.context = context;
    }

    /**
     * Get the length of the list of the hints.
     *
     * @return The length of the list of the hints.
     */
    public int getSize() {
        return hints.size();
    }

    /**
     * Get the hints list.
     * @return ArrayList of hints.
     */
    public ArrayList<Hint> getHints() {
        return hints;
    }

    /**
     * Set the current hint ArrayList.
     * @param hints The list hint to display.
     */
    public void setHints(ArrayList<Hint> hints) {
        this.hints = hints;
        this.notifyDataSetChanged();
    }

    /**
     * Add a new hint at the end of the list.
     * @param hint The new hint to add.
     */
    public void addHint(Hint hint) {
        hints.add(hint);
        this.notifyDataSetChanged();
    }

    /**
     * Set the hint in the given index (used as editing).
     * @param i The index on which we replace (edit) the hint.
     * @param hint The new hint we put in the adapter.
     */
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

        // put the values of the hunt in all of the views
        viewHolder.hintTitleTextView.setText(hints.get(i).getTitle());
        viewHolder.hintTextTextView.setText(hints.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return hints.size();
    }

    protected Object actionMode;

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener, View.OnTouchListener {
        public Hint hint;
        public TextView hintTitleTextView;
        public TextView hintTextTextView;
        public Context context;
        public LinearLayout cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            // set an on click listeners to make the cards clickable and long clickable
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnTouchListener(this);

            // get the views from the layout
            cardView = (LinearLayout) itemView.findViewById(R.id.item_hint_card_view);
            hintTitleTextView = (TextView) itemView.findViewById(R.id.item_hint_list_title);
            hintTextTextView = (TextView) itemView.findViewById(R.id.item_hint_list_text);
        }

        /**
         * Delete the hint from the adapter.
         */
        public void deleteHint() {
            hints.remove(hint);
            HintListAdapter.this.notifyDataSetChanged();
        }

        public void editHint() {
            // send away the Hint and it's index to the CreateHintActivity. It would later end in
            // HintListActivity as a result intent.
            Intent intent = new Intent(context, CreateHintActivity.class);
            intent.putExtra(context.getResources().getString(R.string.intent_hint_extra), hint);
            intent.putExtra(context.getResources().getString(R.string.intent_hint_index_extra),
                    hints.indexOf(hint));
            ((Activity) context).startActivityForResult(intent,
                    context.getResources().getInteger(R.integer.intent_hint_edit_extra));
        }

        @Override
        public void onClick(View view) {
            if (actionMode != null) { // we're in action mode
                return;
            }

            editHint();
        }

        @Override
        public boolean onLongClick(View view) {
            if (actionMode != null) {
                return false;
            }

            // Start the CAB using the ActionMode.Callback defined above
            cardView.setBackgroundColor(
                    context.getResources().getColor(R.color.colorLongPressedHighlight));
            actionMode = ((Activity) context).startActionMode(actionModeCallback);
            view.setSelected(true);
            return true;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (actionMode != null) {
                return false;
            }

            // set click background
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cardView.setBackgroundColor(
                        context.getResources().getColor(R.color.colorPressedHighlight));
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                cardView.setBackgroundColor(
                        context.getResources().getColor(R.color.colorUnpressedHighlight));
            }

            return false;
        }

        // on long click, show the contextual action bar
        private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.hint_list_action, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit_point:
                        editHint();
                        finish();
                        return true;
                    case R.id.action_discard_point:
                        deleteHint();
                        finish();
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // reset the highlight
                cardView.setBackgroundColor(
                        context.getResources().getColor(R.color.colorUnpressedHighlight));

                actionMode = null;
            }

            /**
             * Close the contextual action bar.
             */
            private void finish() {
                if (actionMode != null) {
                    ((ActionMode) actionMode).finish();
                }
            }
        };
    }
}
