package com.geofind.geofind.ui.play;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.R;
import com.geofind.geofind.geoutils.MapManager;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.structures.Hunt;
import com.geofind.geofind.ui.settings.SettingsActivity;
import com.google.android.gms.maps.MapFragment;

/**
 * An {@link android.app.Activity} that shows an overview of the
 * {@link com.geofind.geofind.structures.Hunt}.
 */
public class HuntDetailsMapActivity extends BaseGameActivity {

    /**
     * The displayed {@link com.geofind.geofind.structures.Hunt}
     */
    Hunt hunt;

    /**
     * Is the hunt finished?
     */
    Boolean isFinished;

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
                isFinished = bundle.getBoolean(getString(R.string.hunt_is_finished));
                if (hunt != null) {
                    MapFragment mapFragment =
                            (MapFragment) getFragmentManager().
                                    findFragmentById(R.id.hunt_details_map_preview);

                    MapManager mapManager = new MapManager(this, mapFragment, false);
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
        menu.findItem(R.id.action_start_hunt).setVisible(!isFinished);
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
