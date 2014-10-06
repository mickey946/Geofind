package com.geofind.geofind;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;


public class HintListActivity extends Activity {

    private RetainedFragment<ArrayList<Hint>> retainedFragment;
    private HintListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint_list);

        // find the retained fragment on activity restarts
        FragmentManager fragmentManager = getFragmentManager();
        retainedFragment = (RetainedFragment<ArrayList<Hint>>) fragmentManager.findFragmentByTag(
                getResources().getString(R.string.hint_list_retained_fragment));

        // create the fragment and data the first time
        if (retainedFragment == null) {
            retainedFragment = new RetainedFragment<ArrayList<Hint>>();
            fragmentManager.beginTransaction().add(retainedFragment,
                    getResources().getString(R.string.hint_list_retained_fragment)).commit();

            // create new list
            retainedFragment.setData(new ArrayList<Hint>());
        }

        // get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an adapter and set it's data
        adapter = new HintListAdapter();
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
        Intent intent;
        switch (id) {
            case R.id.action_add_point:
                intent = new Intent(this, CreateHintActivity.class);
                startActivityForResult(intent,
                        getResources().getInteger(R.integer.intent_hint_result));
                return true;

            case R.id.action_submit_points:
                if (adapter.getSize() < 2) { // not enough points
                    Toast.makeText(this, getString(R.string.hint_list_not_enough_points_error),
                            Toast.LENGTH_LONG).show();
                    return true;
                }

                // send away the hints
                intent = new Intent();
                intent.putExtra(getString(R.string.intent_hints_extra), adapter.getHints());
                setResult(RESULT_OK, intent);

                // TODO save the hints locally to get to them later

                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
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
        }
    }

    @Override
    public void onDestroy() {
        retainedFragment.setData(adapter.getHints());
        super.onDestroy();
    }

    public void clearData() {
        retainedFragment.setData(null);
    }
}
