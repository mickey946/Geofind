package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class CreateHuntActivity extends ActionBarActivity {

    ArrayList<Hint> hints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hunt);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_hunt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_submit_hunt) {
            TextView huntTitleTextView = (TextView) findViewById(R.id.create_hunt_title);
            TextView huntDescriptionTextView = (TextView) findViewById(R.id.create_hunt_description);

            String huntTitle = huntTitleTextView.getText().toString(),
                    huntDescription = huntDescriptionTextView.getText().toString();
            String creatorID = "creatorID"; // TODO change to appropriate type

            Hunt hunt = new Hunt(huntTitle, huntDescription, hints, creatorID);

            // TODO save hunt to parse

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openHintCreation(View view) {
        Intent intent = new Intent(this, HintListActivity.class);
        // put existing hints (if any)
        intent.putExtra(getString(R.string.intent_hints_extra), hints);
        startActivityForResult(intent, getResources().getInteger(R.integer.intent_hints_result));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == getResources().getInteger(R.integer.intent_hints_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user created a list of hints
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    hints = (ArrayList<Hint>)
                            bundle.getSerializable(getString(R.string.intent_hints_extra));
                    if (hints != null) {
                        Button createHintsButton = (Button)
                                findViewById(R.id.create_hunt_create_points_button);
                        createHintsButton.setText(getString(R.string.hunt_create_edit_button));
                    }
                }
            }
        }
    }
}
