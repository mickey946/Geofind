package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;


public class PickPointActivity extends Activity {

    MapManager _mapManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_point);
        TextView tvLocation = (TextView) findViewById(R.id.pick_point_txt);

        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.pick_point_map);
        _mapManager = new MapManager(this,mapFragment, tvLocation);
        _mapManager.enableMarkers(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pick_point, menu);
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

            /**
             * TODO add the point to the Intent like following:
             * intent.putExtra(getString(R.string.intent_hint_extra), hint);
             */

            setResult(RESULT_OK, intent);

            //close this Activity...
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
