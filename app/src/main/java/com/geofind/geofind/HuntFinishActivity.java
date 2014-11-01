package com.geofind.geofind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


public class HuntFinishActivity extends ActionBarActivity {

    private Hunt hunt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_finish);

        Intent intent = getIntent();
        hunt = (Hunt) intent.getSerializableExtra(getString(R.string.intent_hunt_extra));

        setTitle(hunt.getTitle());
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void submitReview(View view) {
        RatingBar commentRatingRatingBar = (RatingBar) findViewById(R.id.hunt_finish_review_rating);
        EditText commentTitleTextView = (EditText) findViewById(R.id.hunt_finish_review_title);
        EditText commentReviewTextView = (EditText) findViewById(R.id.hunt_finish_review);

        final Comment comment = new Comment(commentTitleTextView.getText().toString(),
                commentReviewTextView.getText().toString(), commentRatingRatingBar.getRating());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Hunt");
        query.getInBackground(hunt.getParseID(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    parseObject.add("Comments", comment.toParseObject());
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
}
