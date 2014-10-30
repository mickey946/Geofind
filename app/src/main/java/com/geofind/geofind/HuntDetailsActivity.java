package com.geofind.geofind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class HuntDetailsActivity extends ActionBarActivity {

    /**
     * The hunt on which the activity displays the details.
     */
    private Hunt hunt;

    /**
     * map image dimensions
     */
    int mapWidth = -1, mapHeight = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_details);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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

            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.hunt_details_progress_bar);

            final ImageView mapView = (ImageView) findViewById(R.id.hunt_details_map_preview);
            ViewTreeObserver vto = mapView.getViewTreeObserver();
            if (mapHeight == -1 || mapWidth == -1){
                if (vto.isAlive()) {
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mapHeight = mapView.getHeight();
                            mapWidth = mapView.getWidth();

                            // should be called when imgMapPreview exists
                            new StaticMap(mapView, progressBar).execute(
                                    new StaticMap.StaticMapDescriptor(
                                            hunt.getCenterPosition(), hunt.getRadius(),
                                            mapWidth,mapHeight));


                        }
                    });
                }
            }else
            {
                // The recycler view doesn't create new tiles, so we reuse previous tile and assume
                // the same dimension for image view
                new StaticMap(mapView, progressBar).execute(
                        new StaticMap.StaticMapDescriptor(
                                hunt.getCenterPosition(), hunt.getRadius(),
                                mapWidth,mapHeight));
            }

            mapView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HuntDetailsActivity.this,
                            HuntDetailsMapActivity.class);

                    // pass the hunt to the map
                    intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                    startActivity(intent);

                }
            });

            // hunt total distance
            String distanceUnit = getCurrentDistanceUnit();
            Float totalDistance = hunt.getTotalDistance();

            // set distance units
            TextView totalDistanceUnitTextView = (TextView)
                    findViewById(R.id.hunt_details_total_distance_unit);
            TextView distanceFromUserUnitTextView = (TextView)
                    findViewById(R.id.hunt_details_distance_from_user_unit);

            // km or miles
            if (distanceUnit.equals(
                    getString(R.string.preferences_distance_units_kilometers))) {
                totalDistance *= Hunt.METERS_TO_KILOMETERS;
                totalDistanceUnitTextView.setText(
                        getText(R.string.item_hunt_list_distance_unit_km));
                distanceFromUserUnitTextView.setText(
                        getText(R.string.item_hunt_list_distance_unit_km));
            } else {
                totalDistance *= Hunt.METERS_TO_MILES;
                totalDistanceUnitTextView.setText(
                        getText(R.string.item_hunt_list_distance_unit_miles));
                distanceFromUserUnitTextView.setText(
                        getText(R.string.item_hunt_list_distance_unit_miles));
            }

            // set the formatted numbers
            TextView totalDistanceTextView = (TextView)
                    findViewById(R.id.hunt_details_total_distance);
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setMaximumFractionDigits(Hunt.DIGIT_PRECISION);
            totalDistanceTextView.setText(decimalFormat.format(totalDistance));

            // TODO distance from start point

            // hunt rating
            RatingBar ratingBar = (RatingBar) findViewById(R.id.hunt_details_rating);
            ratingBar.setRating(hunt.getRating());

            // TODO comments
        }
    }


    /**
     * Get the current distance unit that is saved in the settings file.
     *
     * @return A string representing the distance unit.
     */
    private String getCurrentDistanceUnit() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(
                this.getString(R.string.pref_key_distance_units),
                this.getString(R.string.preferences_distance_units_kilometers));
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
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_start_hunt:
                intent = new Intent(this, HuntActivity.class);

                // pass the hunt itself to the HuntDetailActivity
                intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}