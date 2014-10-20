package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;

import java.util.concurrent.Callable;


public class HuntDetailsActivity extends Activity {

    /**
     * The hunt on which the activity displays the details.
     */
    private Hunt hunt;
    private MapManager _mapManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_details);

        // retrieve the hunt from the intent that started the activity
        Intent intent = getIntent();

        if (intent != null) {
            hunt = (Hunt) intent.getExtras().getSerializable(
                    getResources().getString(R.string.intent_hunt_extra));

            // hunt title
            TextView titleTextView = (TextView) findViewById(R.id.hunt_details_title);
            titleTextView.setText(hunt.getTitle());

            // hunt description
            TextView descriptionTextView = (TextView) findViewById(R.id.hunt_details_description);
            descriptionTextView.setText(hunt.getDescription());

            MapFragment mapFragment =
                    (MapFragment) getFragmentManager().findFragmentById(R.id.hunt_details_map_preview);
            _mapManager = new MapManager(this, mapFragment);
            _mapManager.showMyLocationButton(false);

            _mapManager.drawCircle(hunt.getCenterPosition(),hunt.getRadius());
            _mapManager.setOnMapClick(new Callable() {
                @Override
                public Object call() throws Exception {
                    Intent intent = new Intent(HuntDetailsActivity.this, HuntActivity.class);

                    // pass the hunt itself to the HuntDetailActivity
                    intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                    startActivity(intent);
                    return null;
                }
            });

            // hunt total distance
            TextView totalDistanceTextView = (TextView)
                    findViewById(R.id.hunt_details_total_distance);
            totalDistanceTextView.setText(hunt.getTotalDistance().toString());

            // TODO distance from start point

            // hunt rating
            RatingBar ratingBar = (RatingBar) findViewById(R.id.hunt_details_rating);
            ratingBar.setRating(hunt.getRating());

            // TODO comments
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hunt_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_start_hunt:
                Intent intent = new Intent(this, HuntActivity.class);

                // pass the hunt itself to the HuntDetailActivity
                intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
