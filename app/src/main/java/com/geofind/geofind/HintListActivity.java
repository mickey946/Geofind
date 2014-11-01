package com.geofind.geofind;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;


public class HintListActivity extends ActionBarActivity {

    private RetainedFragment<ArrayList<Hint>> retainedFragment;
    private HintListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint_list);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // find the retained fragment on activity restarts
        FragmentManager fragmentManager = getFragmentManager();
        //noinspection unchecked
        retainedFragment = (RetainedFragment<ArrayList<Hint>>) fragmentManager.findFragmentByTag(
                getResources().getString(R.string.hint_list_retained_fragment));

        // create the fragment and data the first time
        if (retainedFragment == null) {
            retainedFragment = new RetainedFragment<ArrayList<Hint>>();
            fragmentManager.beginTransaction().add(retainedFragment,
                    getResources().getString(R.string.hint_list_retained_fragment)).commit();
        }

        // get previously created hints and display them (if any)
        ArrayList<Hint> hints = null;
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //noinspection unchecked
                hints = (ArrayList<Hint>)
                        bundle.getSerializable(getString(R.string.intent_hints_extra));
            }
        }

        if (hints != null) { // retain previously created hints
            retainedFragment.setData(hints);
        } else { // create new list
            retainedFragment.setData(new ArrayList<Hint>());
        }

        // get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an adapter and set it's data
        adapter = new HintListAdapter(this);
        adapter.setHints(retainedFragment.getData());

        // set adapter
        recyclerView.setAdapter(adapter);

        // set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // if the list is empty, display the empty message
        if (adapter.getSize() > 0) {
            findViewById(R.id.hint_list_empty).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hint_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_submit_points:
                submitPoints();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Submit points to the CreateHuntActivity.
     */
    public void submitPoints() {
        if (adapter.getSize() < 2) { // not enough points
            Toast.makeText(this, getString(R.string.hint_list_not_enough_points_error),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // send away the hints
        Intent intent = new Intent();
        intent.putExtra(getString(R.string.intent_hints_extra), adapter.getHints());
        setResult(RESULT_OK, intent);

        finish();
    }

    /**
     * Add a new hint to the list.
     */
    public void addHint(Hint hint) {
        // add a new hint
        adapter.addHint(hint);
        // the list is not empty, remove the empty message
        findViewById(R.id.hint_list_empty).setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == getResources().getInteger(R.integer.intent_hint_result)) {
            // the user added a new hint

            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user created a point
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Hint hint = (Hint) bundle.getSerializable(getString(R.string.intent_hint_extra));
                    if (hint != null) {
                        addHint(hint);
                    }
                }
            }
        } else if (requestCode == getResources().getInteger(R.integer.intent_hint_edit_extra)) {
            // the user edited an existing hint

            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user created a point
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Hint hint = (Hint) bundle.getSerializable(getString(R.string.intent_hint_extra));
                    Integer i = bundle.getInt(getString(R.string.intent_hint_index_extra));
                    if (hint != null) {
                        adapter.setHint(i, hint);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        retainedFragment.setData(adapter.getHints());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // warn the user about loss of data

        // instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // chain together various setter methods to set the dialog characteristics
        builder.setMessage(getString(R.string.hint_list_data_loss_warning))
                .setTitle(getString(R.string.hint_list_data_loss_warning_title));

        if (adapter.getSize() < 2) { // we do not save changes on less then 2 points
            builder.setMessage(getString(R.string.hint_list_data_loss_warning_no_save));
        }

        // set icon
        builder.setIcon(getResources().getDrawable(R.drawable.ic_warning_grey600_48dp));

        // set positive button
        builder.setPositiveButton(getString(R.string.hint_list_data_loss_warning_positive),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        if (adapter.getSize() < 2) { // the user will add at least one point
                            return;
                        }

                        submitPoints(); // save the changes
                        HintListActivity.super.onBackPressed();
                    }
                });

        // set negative button
        builder.setNegativeButton(getString(R.string.hint_list_data_loss_warning_negative),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        HintListActivity.super.onBackPressed(); // don't save the changes
                    }
                });

        // get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    /**
     * Open hint creation activity.
     *
     * @param view The current view.
     */
    public void openHintCreation(View view) {
        Intent intent = new Intent(this, CreateHintActivity.class);
        startActivityForResult(intent, getResources().getInteger(R.integer.intent_hint_result));
    }
}
