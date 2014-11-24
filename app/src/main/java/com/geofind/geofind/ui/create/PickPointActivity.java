package com.geofind.geofind.ui.create;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.geoutils.MapManager;
import com.geofind.geofind.R;
import com.geofind.geofind.structures.Point;
import com.google.android.gms.maps.MapFragment;
import com.geofind.geofind.playutils.BaseGameActivity;

/**
 * An {@link android.app.Activity} that is used for picking a
 * {@link com.geofind.geofind.structures.Point} for a {@link com.geofind.geofind.structures.Hint}.
 */
public class PickPointActivity extends BaseGameActivity {

    /**
     * The {@link com.geofind.geofind.geoutils.MapManager} for the map.
     */
    MapManager mapManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_point);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pick_point, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Configure the search info and add any event listeners

        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.pick_point_map);
        mapManager = new MapManager(this, mapFragment, searchView);
        mapManager.enableMarkers(true);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Point point = (Point) bundle.getSerializable(
                        getResources().getString(R.string.intent_hint_point_extra));
                mapManager.displayFoundLocation(point.toLatLng());
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_pick_point) {
            // send away the point
            Intent intent = new Intent();

            Point resultPoint = mapManager.get_selectedPoint();

            if (resultPoint == null) {
                setResult(RESULT_CANCELED, intent);
            } else {
                intent.putExtra(getString(R.string.intent_hint_extra), mapManager.get_selectedPoint());
                setResult(RESULT_OK, intent);
            }

            //close this Activity...
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
    }
}
