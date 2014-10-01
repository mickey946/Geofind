package com.geofind.geofind;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;


public class HuntListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_list);

        // get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // create and fill the hunts array to display them.
        // TODO retrieve the hunts from parse
        Hunt[] hunts = {
                new Hunt("Title1", 1, 1, "Hunt1"),
                new Hunt("Title2", 2, 2, "Hunt2"),
                new Hunt("Title3", 3, 3, "Hunt3"),
                new Hunt("Title4", 4, 4, "Hunt4"),
                new Hunt("Title5", 5, 5, "Hunt5")
        };

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an adapter
        HuntListAdapter adapter = new HuntListAdapter(hunts);

        // set adapter
        recyclerView.setAdapter(adapter);

        // set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hunt_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
