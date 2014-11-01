package com.geofind.geofind;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;


public class CreateHuntActivity extends ActionBarActivity {

    private static final String PREF_CREATE_HUNT_TITLE_DESCRIPTION_DISMISS =
            "PREF_CREATE_HUNT_TITLE_DESCRIPTION_DISMISS";

    private SharedPreferences sharedPreferences;

    ArrayList<Hint> hints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hunt);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get the preferences of the activity
        sharedPreferences = getPreferences(MODE_PRIVATE);

        // check and hide information cards if needed
        if (isTitleDescriptionInfoDismissed()) {
            hideTitleDescriptionInfo();
        }
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
        switch (id) {
            case R.id.action_submit_hunt:
                TextView huntTitleTextView = (TextView) findViewById(R.id.create_hunt_title);
                TextView huntDescriptionTextView = (TextView) findViewById(R.id.create_hunt_description);

                String huntTitle = huntTitleTextView.getText().toString(),
                        huntDescription = huntDescriptionTextView.getText().toString();

                if (isInputInvalid(huntTitle, huntDescription)) {
                    Toast.makeText(this, getString(R.string.create_hint_invalid_input),
                            Toast.LENGTH_LONG).show();
                    return true;
                }

                warnUserBeforeSubmitting(huntTitle, huntDescription);

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
                    hints = (ArrayList<Hint>) bundle.getSerializable(getString(R.string.intent_hints_extra));
                    if (hints != null) {
                        TextView createHintsText = (TextView)
                                findViewById(R.id.create_hunt_add_points_description_text);
                        createHintsText.setText(String.format(
                                getString(R.string.hunt_create_edit_points_text), hints.size()));

                        Button createHintsButton = (Button)
                                findViewById(R.id.create_hunt_create_points_button);
                        createHintsButton.setText(getString(R.string.hunt_create_edit_points_button));
                    }
                }
            }
        }
    }

    /**
     * Check if the card view was set to be hidden (dismissed) before.
     *
     * @return whether the card was dismissed in the past.
     */
    private boolean isTitleDescriptionInfoDismissed() {
        return sharedPreferences.getBoolean(PREF_CREATE_HUNT_TITLE_DESCRIPTION_DISMISS, false);
    }

    /**
     * Hide title & description info card view.
     */
    private void hideTitleDescriptionInfo() {
        View infoCard = findViewById(R.id.hunt_title_details_info);
        infoCard.setVisibility(View.GONE);
    }

    /**
     * Hide the title & description info card view and save it so it won't show again.
     *
     * @param view The current view.
     */
    public void dismissTitleDescriptionInfo(View view) {
        hideTitleDescriptionInfo();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_CREATE_HUNT_TITLE_DESCRIPTION_DISMISS, true);
        editor.apply();
    }

    public boolean isInputInvalid(String huntTitle, String huntDescription) {
        return hints == null || huntTitle.trim().equals("") || huntDescription.trim().equals("");
    }

    /**
     * Display a warning to the user before submitting the hunt. If the user is sure, submit the
     * hunt.
     */
    public void warnUserBeforeSubmitting(final String huntTitle, final String huntDetails) {
        // instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // chain together various setter methods to set the dialog characteristics
        builder.setMessage(getString(R.string.hunt_create_check_before_description))
                .setTitle(getString(R.string.hunt_create_check_before_title));

        // set icon
        builder.setIcon(getResources().getDrawable(R.drawable.ic_warning_grey600_48dp));

        // set positive button
        builder.setPositiveButton(getString(R.string.hint_list_data_loss_warning_positive),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        submitHunt(huntTitle, huntDetails); // save the changes
                    }
                });

        // set negative button
        builder.setNegativeButton(getString(R.string.hint_list_data_loss_warning_negative),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    /**
     * Submit the Hunt and save it in the database.
     */
    public void submitHunt(String huntTitle, String huntDescription) {
        String creatorID = "creatorID"; // TODO get the real creator ID.

        Hunt hunt = new Hunt(huntTitle, huntDescription, creatorID, hints);

        ParseObject remoteHunt = hunt.toParseObject();

        remoteHunt.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Hunt was successfully created!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.v("HUNT was not saved.", "Parse Exception: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Hunt was NOT created, please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        //TODO return to main application screen
        //TODO consider moving the finish call to after Toast.
        finish();

    }

}
