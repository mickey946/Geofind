package com.geofind.geofind.ui.create;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.geofind.geofind.R;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.structures.Hint;
import com.geofind.geofind.structures.Hunt;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * An {@link android.app.Activity} that is used to create a
 * {@link com.geofind.geofind.structures.Hunt}.
 */
public class CreateHuntActivity extends BaseGameActivity {

    /**
     * A {@link android.preference.Preference} name for dismissing the info card.
     */
    private static final String PREF_CREATE_HUNT_TITLE_DESCRIPTION_DISMISS =
            "PREF_CREATE_HUNT_TITLE_DESCRIPTION_DISMISS";

    /**
     * The tag used for logging.
     */
    private static final String TAG = CreateHuntActivity.class.getSimpleName();

    /**
     * The {@link android.content.SharedPreferences} of the activity.
     */
    private SharedPreferences sharedPreferences;

    /**
     * The {@link java.util.ArrayList} of {@link com.geofind.geofind.structures.Hint}s of the
     * {@link com.geofind.geofind.structures.Hunt}.
     */
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

    /**
     * Open the {@link com.geofind.geofind.ui.create.HintListActivity}.
     * @param view The current view.
     */
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
                    //noinspection unchecked
                    hints = (ArrayList<Hint>) bundle.getSerializable(
                            getString(R.string.intent_hints_extra));
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

    /**
     * Check if the input is invalid.
     * @param huntTitle The hunt title.
     * @param huntDescription The hunt description.
     * @return True if the input is invalid. False otherwise.
     */
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
        builder.setIcon(getResources().getDrawable(R.drawable.ic_warning_grey600_24dp));

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
     * @param huntTitle The hunt title.
     * @param huntDescription The hunt description.
     */
    public void submitHunt(String huntTitle, String huntDescription) {
        String creatorID = Plus.PeopleApi.getCurrentPerson(getApiClient()).getId();

        Hunt hunt = new Hunt(huntTitle, huntDescription, creatorID, hints);

        ParseObject remoteHunt = hunt.toParseObject(getApplicationContext());

        remoteHunt.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.create_hunt_submit_successful),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "Hunt was not saved, Parse Exception: " + e.getMessage());
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.create_hunt_sumbit_failure),
                            Toast.LENGTH_LONG).show();
                }

                // TODO show progress bar for saving a hunt
                finish();
            }
        });

        if (isSignedIn()) {
            Games.Achievements.unlock(getApiClient(),
                    getString(R.string.achievement_bob_the_builder));

            Games.Achievements.increment(getApiClient(),
                    getString(R.string.achievement_parttime_contractor), 1);
        }
    }

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
    }
}
