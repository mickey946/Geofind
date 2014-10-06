package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class CreateHintActivity extends Activity {

    private TextView hintTitleTextView;
    private TextView hintTextTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hint);

        hintTitleTextView = (TextView) findViewById(R.id.create_hint_title);
        hintTextTextView = (TextView) findViewById(R.id.create_hint_description);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_hint, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_hint) {
            submitHint();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if the user filled all the required fields
     *
     * @return whether the user filled all the required fields or not
     */
    public boolean checkInput() {
        return !hintTitleTextView.getText().toString().trim().equals("")
                && !hintTextTextView.getText().toString().trim().equals("");
    }

    /**
     * Try to send away the created hint (if all the required fields are legal) and close the
     * activity.
     */
    public void submitHint() {
        // check if the user filled all required fields
        if (checkInput()) { // all fields are legal
            Hint hint = new Hint(hintTitleTextView.getText().toString(),
                    hintTextTextView.getText().toString());

            // send away the hint
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.intent_hint_extra), hint);
            setResult(RESULT_OK, intent);

            //close this Activity...
            finish();
        } else { // one or more fields are not legal
            Toast.makeText(this, getString(R.string.create_hint_fields_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void openPointPicker(View view) {
        Intent intent = new Intent(this, PickPointActivity.class);
        startActivityForResult(intent, getResources().getInteger(R.integer.intent_point_result));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == getResources().getInteger(R.integer.intent_point_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user picked a point
                Bundle bundle = data.getExtras();
                // TODO add the coordinates to the Hint object and display it on the map
            }
        }
    }
}
