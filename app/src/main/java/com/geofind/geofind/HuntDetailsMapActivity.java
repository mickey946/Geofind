package com.geofind.geofind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.MapFragment;
import com.google.example.games.basegameutils.BaseGameActivity;


public class HuntDetailsMapActivity extends BaseGameActivity {

    /**
     * Control dynamic map
     */
    private MapManager mapManager;

    /**
     * the displayed hunt
     */
    Hunt hunt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_details_map);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // retrieve the hunt from the intent that started the activity
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                hunt = (Hunt) bundle.getSerializable(getString(R.string.intent_hunt_extra));
                if (hunt != null) {
                    MapFragment mapFragment =
                            (MapFragment) getFragmentManager().
                                    findFragmentById(R.id.hunt_details_map_preview);
                    mapManager = new MapManager(this, mapFragment, false);
                    mapManager.showMyLocationButton(true);
                    mapManager.drawCircle(hunt.getCenterPosition(), hunt.getRadius());
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hunt_details_map, menu);
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
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_start_hunt:
                intent = new Intent(this, HuntActivity.class);

                // pass the hunt itself to the HuntDetailActivity
                intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        onBackPressed();
        return null;
    }

    @Override
    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onCreateSupportNavigateUpTaskStack(builder);
        onBackPressed();
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }
}
