package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;


public class HuntDetailsActivity extends Activity {

    /**
     * The hunt on which the activity displays the details.
     */
    private Hunt hunt;

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
