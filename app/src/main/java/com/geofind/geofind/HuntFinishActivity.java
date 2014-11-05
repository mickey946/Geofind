package com.geofind.geofind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class HuntFinishActivity extends ActionBarActivity {

    /**
     * The hunt that was finished.
     */
    private Hunt hunt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_finish);

        Intent intent = getIntent();
        hunt = (Hunt) intent.getSerializableExtra(getString(R.string.intent_hunt_extra));
        if (hunt != null) {
            setTitle(hunt.getTitle());

            TextView totalPointsTextView = (TextView) findViewById(R.id.hunt_finish_total_points);
            TextView solvedPointsTextView = (TextView) findViewById(R.id.hunt_finish_solved_points);
            TextView totalTimeTextView = (TextView) findViewById(R.id.hunt_finish_total_time);

            totalPointsTextView.setText(
                    Integer.toString(intent.getIntExtra(getResources().getString(R.string.hunt_finish_total_points), 0)));
            solvedPointsTextView.setText(
                    Integer.toString(intent.getIntExtra(getResources().getString(R.string.hunt_finish_solved_points), 0)));
            Date date = new Date(intent.getLongExtra(getResources().getString(R.string.hunt_finish_total_time), 0));
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateFormatted = formatter.format(date);
            totalTimeTextView.setText(dateFormatted);
        }

        setUpReviewCard();
    }

    /**
     * Set up the review card view so that when of it's elements is in focus - hide the floating
     * action button. When done, show the button again.
     */
    private void setUpReviewCard() {
        // get the floating action button
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // hide the floating action button when reviewing the hunt
        EditText reviewTitleEditText = (EditText) findViewById(R.id.hunt_finish_review_title);
        reviewTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });

        EditText reviewEditText = (EditText) findViewById(R.id.hunt_finish_review);
        reviewEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hunt_finish, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Close all the activities in the current stack and go back to the main menu.
     *
     * @param view The current view.
     */
    public void goToMainScreen(View view) {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void submitReview(View view) {
        RatingBar commentRatingRatingBar = (RatingBar) findViewById(R.id.hunt_finish_review_rating);
        EditText commentTitleTextView = (EditText) findViewById(R.id.hunt_finish_review_title);
        EditText commentReviewTextView = (EditText) findViewById(R.id.hunt_finish_review);

        if ((commentTitleTextView.getText().toString().isEmpty()) |
                (commentReviewTextView.getText().toString().isEmpty())) {
            Toast.makeText(getApplicationContext(), "Cannot submit an incomplete review.",
                    Toast.LENGTH_LONG).show();
            return;
        }


        final Comment comment = new Comment(commentTitleTextView.getText().toString(),
                commentReviewTextView.getText().toString(), commentRatingRatingBar.getRating());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Hunt");
        query.getInBackground(hunt.getParseID(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    parseObject.add("comments", comment.toParseObject());
                    parseObject.increment("numOfRaters");
                    parseObject.increment("totalRating", comment.getRating());
                    parseObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), "Your review was submitted successfully!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.v("Review was not saved", "Parse Exception: " + e.getMessage());
                                Toast.makeText(getApplicationContext(), "Review was NOT submitted, please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Log.v("Review was not saved", "Parse Exception: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Review was NOT submitted, please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        //TODO make "submit review" button unavailable, to avoid adding same comment twice.
    }

    //TODO add button to return to main screen.

    @Override
    protected void onResume() {
        super.onResume();

        if (hunt == null)
            return;

        // hunt total distance
        final String distanceUnit = getCurrentDistanceUnit();
        Float totalDistance = hunt.getTotalDistance();

        // set distance units
        TextView totalDistanceUnitTextView = (TextView)
                findViewById(R.id.hunt_details_total_distance_unit);

        // km or miles
        if (distanceUnit.equals(
                getString(R.string.preferences_distance_units_kilometers))) {
            totalDistance *= Hunt.METERS_TO_KILOMETERS;
            totalDistanceUnitTextView.setText(getText(R.string.item_hunt_list_distance_unit_km));
        } else {
            totalDistance *= Hunt.METERS_TO_MILES;
            totalDistanceUnitTextView.setText(getText(R.string.item_hunt_list_distance_unit_miles));
        }

        // set the formatted numbers
        TextView totalDistanceTextView = (TextView)
                findViewById(R.id.hunt_details_total_distance);
        final DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(Hunt.DIGIT_PRECISION);
        totalDistanceTextView.setText(decimalFormat.format(totalDistance));
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
}
