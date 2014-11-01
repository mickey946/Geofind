package com.geofind.geofind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.melnykov.fab.FloatingActionButton;


public class HuntFinishActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_finish);

        Intent intent = getIntent();
        Hunt hunt = (Hunt) intent.getSerializableExtra(getString(R.string.intent_hunt_extra));

        setTitle(hunt.getTitle());

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
}
