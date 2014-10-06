package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


public class HintListActivity extends Activity {

    HintListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint_list);

        // get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an adapter
        adapter = new HintListAdapter();

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
            case R.id.action_add_point:
                Intent intent = new Intent(this, CreateHintActivity.class);
                startActivityForResult(intent, getResources().getInteger(R.integer.intent_hint_result));
                return true;
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
}
