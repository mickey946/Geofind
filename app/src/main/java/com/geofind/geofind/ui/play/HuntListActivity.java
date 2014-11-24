package com.geofind.geofind.ui.play;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.R;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.playutils.SnapshotManager;
import com.geofind.geofind.ui.settings.SettingsActivity;
import com.geofind.geofind.ui.widget.slidingtablayout.SlidingTabLayout.SlidingTabLayout;

/**
 * An {@link android.app.Activity} that shows the lists of the available
 * {@link com.geofind.geofind.structures.Hunt}s to play.
 */
public class HuntListActivity extends BaseGameActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    HuntListPagerAdapter huntListPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the three primary sections of
     * the app, one at a time.
     */
    ViewPager viewPager;

    /**
     * The {@link com.geofind.geofind.ui.widget.slidingtablayout.SlidingTabLayout.SlidingTabLayout}
     * that will display the tabs.
     */
    SlidingTabLayout slidingTabLayout;

    /**
     * The {@link com.geofind.geofind.playutils.SnapshotManager} that loads the saved hunts of the
     * player.
     */
    SnapshotManager snapshotManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_list);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        snapshotManager = new SnapshotManager(this, getGameHelper().getApiClient());

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the activity.
        huntListPagerAdapter = new HuntListPagerAdapter(getSupportFragmentManager(), this,
                snapshotManager);

        viewPager = (ViewPager) findViewById(R.id.pagerHuntList);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

        Resources resources = getResources();
        slidingTabLayout.setSelectedIndicatorColors(resources.getColor(R.color.tab_selected_strip));
        slidingTabLayout.setDistributeEvenly(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load or reload the snapshot list
        snapshotManager.loadSnapshotList();

        // Update the UI
        huntListPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(huntListPagerAdapter);
        slidingTabLayout.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hunt_list, menu);
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
    }
}
