package com.geofind.geofind;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.geofind.geofind.widget.SlidingTabLayoutl.SlidingTabLayout;
import com.geofind.geofind.basegameutils.BaseGameActivity;

public class HuntListActivity extends BaseGameActivity implements ActionBar.TabListener {

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
     * The {@link com.geofind.geofind.widget.SlidingTabLayoutl.SlidingTabLayout} that will display the tabs.
     */
    SlidingTabLayout slidingTabLayout;
    private SnapshotManager snapshotManager;


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
        huntListPagerAdapter = new HuntListPagerAdapter(getSupportFragmentManager(), this, snapshotManager);




    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("HuntListActivity","OnResume");
        huntListPagerAdapter.notifyDataSetChanged();
        viewPager = (ViewPager) findViewById(R.id.pagerHuntList);
        viewPager.setAdapter(huntListPagerAdapter);
        //huntListPagerAdapter.startUpdate(viewPager);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

        Resources resources = getResources();
        slidingTabLayout.setSelectedIndicatorColors(resources.getColor(R.color.tab_selected_strip));
        slidingTabLayout.setDistributeEvenly(true);
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
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {
        Log.d("Load", "sign in start");
        snapshotManager.loadSnapshot(
                new SnapshotManager.ExecFinished() {
                    @Override
                    public void onFinish() {
                        Log.d("Load", "on finish snapshot load");
//                        huntListPagerAdapter.finishUpdate(viewPager);
                    }
                }

        );
    }
}
