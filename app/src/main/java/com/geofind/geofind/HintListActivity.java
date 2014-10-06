package com.geofind.geofind;

import android.app.Activity;
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
    ArrayList<Hint> hints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint_list);

        // get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // create and fill the hints array to display them
        hints = new ArrayList<Hint>();

        // if the list is empty, display the empty message
        if (hints.size() > 0) {
            findViewById(R.id.hint_list_empty).setVisibility(View.GONE);
        }

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an adapter
        adapter = new HintListAdapter(hints);

        // set adapter
        recyclerView.setAdapter(adapter);

        // set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
                // add a new hint
                adapter.addHint(new Hint("Title1", "Text1"));
                // the list is not empty, remove the empty message
                findViewById(R.id.hint_list_empty).setVisibility(View.GONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
