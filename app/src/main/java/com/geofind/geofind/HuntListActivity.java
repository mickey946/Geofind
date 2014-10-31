package com.geofind.geofind;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.widget.SlidingTabLayout;

import java.util.concurrent.Callable;

public class HuntListActivity extends ActionBarActivity implements ActionBar.TabListener {

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
     * The {@link com.geofind.geofind.widget.SlidingTabLayout} that will display the tabs.
     */
    SlidingTabLayout slidingTabLayout;

    LocationFinder locationFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_list);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        locationFinder = new LocationFinder(this, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Log.d("HuntListActivity", locationFinder.getCurrentLocation().toString());

                return null;
            }
        });

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the activity.
        huntListPagerAdapter = new HuntListPagerAdapter(getSupportFragmentManager(), this);

        viewPager = (ViewPager) findViewById(R.id.pagerHuntList);
        viewPager.setAdapter(huntListPagerAdapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

        Resources resources = getResources();
        slidingTabLayout.setSelectedIndicatorColors(resources.getColor(R.color.tab_selected_strip));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationFinder.startLocation();
    }

    @Override
    protected void onStop() {
        locationFinder.stopLocation();
        super.onStart();
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
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
