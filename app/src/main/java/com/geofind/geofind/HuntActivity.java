package com.geofind.geofind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.MapFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;


public class HuntActivity extends ActionBarActivity {

    public static final int GEOFENCE_RADIUS = 10;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
    private HintPagerAdapter hintPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    private ViewPager viewPager;

    /**
     * The tag used for debugger log.
     */
    private static final String TAG = "HuntActivity";

    /**
     * The sliding up panel layout.
     */
    private SlidingUpPanelLayout slidingUpPanel;

    /**
     * The anchor point of the sliding up panel.
     */
    private static final float SLIDING_UP_PANEL_ANCHOR_POINT = 0.7f;

    /**
     * The hunt on which the activity displays the details.
     */
    private Hunt hunt;

    /**
     * The hints that would be displayed and used.
     */
    ArrayList<Hint> hints = new ArrayList<Hint>();

    /**
     * The map manager controller
     */
    private MapManager mapManager;

    /**
     * This manages the geofence control
     */
    GeofenceManager geofence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);


        // TODO retrieve the hints on the fly using the hunt
        hints.add(new Hint("Description1", new Point(31.66831, 35.11371), Hint.State.SOLVED));
        hints.add(new Hint("Description2", new Point(31.86831, 35.21371), Hint.State.SOLVED));
        hints.add(new Hint("Description3", new Point(31.56831, 35.11371), Hint.State.REVEALED));
        hints.add(new Hint("Description4", new Point(31.76831, 35.21371), Hint.State.UNREVEALED));


        setContentView(R.layout.activity_hunt);

        View layout = findViewById(R.id.main_content);
        layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setUpHunt();

        setUpGeofence();

        setUpPagerView();

        setUpSlidingUpPanel();

        setUpMap();


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapManager.focusOnCurrentLocation();
    }

    /**
     * Retrieve the hunt from the intent that started the activity and set it up.
     */
    private void setUpHunt() {

        Intent intent = getIntent();
        Log.d(TAG, "got intent is " + (intent == null ? "null" : intent.getType()));
        if (intent != null) {
            hunt = (Hunt) intent.getExtras().getSerializable(
                    getResources().getString(R.string.intent_hunt_extra));
            setTitle(hunt.getTitle());
        }
    }

    /**
     * Set up the sliding up panel with listeners.
     */
    private void setUpSlidingUpPanel() {
        slidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanel.setAnchorPoint(SLIDING_UP_PANEL_ANCHOR_POINT);
        slidingUpPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                if (slideOffset >= SLIDING_UP_PANEL_ANCHOR_POINT - 0.01f) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
            }

            @Override
            public void onPanelExpanded(View panel) {
                // when the panel is expanded the map is not visible, so there is nothing to show
                Log.i(TAG, "onPanelExpanded");
            }

            @Override
            public void onPanelCollapsed(View panel) {
                // when the panel had collapsed, the user would like to see the map rather than the
                // point
                Log.i(TAG, "onPanelCollapsed");
                mapManager.setMapOffset(0, 0);
                focusOnPoint(viewPager.getCurrentItem());
            }

            @Override
            public void onPanelAnchored(View panel) {
                // when the user anchors the panel, he sees both the hint and both the map, so it's
                // good to assume that he wants to focus on the point (if it is visible)
                Log.i(TAG, "onPanelAnchored");

                int height = findViewById(R.id.main_content).getMeasuredHeight();
                float panDistance = ((1 - (1 - SLIDING_UP_PANEL_ANCHOR_POINT) / 2) - 0.5f) * height
                        - getResources().getDimension(R.dimen.sliding_up_panel_paralax)
                        * SLIDING_UP_PANEL_ANCHOR_POINT;
                mapManager.setMapOffset(0, panDistance);

                focusOnPoint(viewPager.getCurrentItem());
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });

        // wait until the layout is drawn, then wait a little bit to display the animation of
        // the sliding up panel
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_hunt);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // the layout has been drawn, now we wait a bit to show the animation
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        slidingUpPanel.expandPanel(SLIDING_UP_PANEL_ANCHOR_POINT);
                    }

                }, getResources().getInteger(R.integer.sliding_up_panel_anchor_delay));

            }
        });

    }

    /**
     * Set up the pager view.
     */
    private void setUpPagerView() {
        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        Log.i(TAG, "create geofence HinatAdapter valid:" + (geofence == null));
        hintPagerAdapter = new HintPagerAdapter(getSupportFragmentManager(), hints, geofence );

        // Set up the ViewPager, attaching the adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(hintPagerAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                // not useful for now, ignore
            }

            @Override
            public void onPageSelected(int index) {
                focusOnPoint(index);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // not useful for now, ignore
            }
        });
    }

    /**
     * Set up the map view.
     */
    private void setUpMap() {
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.hunt_map);
        mapManager = new MapManager(this, mapFragment);
        mapManager.setMarkerCallback(new IndexCallback() {
            /**
             * Slide to the point page at the given index.
             *
             * @param index The index of the point in the adapter.
             */
            @Override
            public void executeCallback(int index) {
                viewPager.setCurrentItem(index, true); // scroll smoothly to the given index
            }
        });

        int index = 0;
        for (Hint hint : hints) {
            index++;
            if (hint.getState() != Hint.State.UNREVEALED) {
                mapManager.setMarker(hint.getLocation().toLatLng(),
                        getString(R.string.hunt_activity_hint_number_title) + index,
                        hint.getState());
            }
        }
    }

    /**
     * Set up the geofence
     */
    private void setUpGeofence(){
        Log.d(TAG, "setUpGeofence");
        geofence = new GeofenceManager(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String id = intent.getStringExtra(getString(R.string.PointIdIntentExtra));
                int indx = intent.getIntExtra(getString(R.string.PointIndexExtra), -1);
                Log.d(TAG, "recieved from geofence point index" + indx);
                Log.d(TAG, "geofence point recieved: " + id);

                // Mark the current hint as solved

                hints.get(indx).setState(Hint.State.SOLVED);
                mapManager.setMarker(hints.get(indx).getLocation().toLatLng(),
                        getString(R.string.hunt_activity_hint_number_title) + indx,
                        hints.get(indx).getState());

                //TODO prepare for next hint

                hintPagerAdapter.notifyDataSetChanged();
            }
        }, new IntentFilter(getString(R.string.GeofenceResultIntent)));

        int unrevealedIndex = 0;
        while (unrevealedIndex < hints.size() &&
                hints.get(unrevealedIndex).getState() != Hint.State.UNREVEALED){
            unrevealedIndex++;
        }

        geofence.createGeofence(hints.get(unrevealedIndex).getLocation(),
                GEOFENCE_RADIUS, unrevealedIndex);

        geofence.setCancelCallback(new IndexCallback() {
            @Override
            public void executeCallback(int index) {
                Log.d(TAG,"recieved from geofence cancel point index" + index);

                hints.get(index).setState(Hint.State.REVEALED);
                Point hintPoint = hints.get(index).getLocation();
                mapManager.setMarker(hintPoint.toLatLng(),
                        getString(R.string.hunt_activity_hint_number_title) + index, hints.get(index).getState());
                mapManager.onLocationChanged(hintPoint.toLocation());


                // Mark the current hint as solved

                //TODO prepare for next hint

                hintPagerAdapter.notifyDataSetChanged();

            }
        });


    }

    /**
     * Focus on a point on a map.
     *
     * @param index The index of the point in the adapter.
     */
    private void focusOnPoint(int index) {
        Fragment fragment = hintPagerAdapter.getItem(index);
        Bundle bundle = fragment.getArguments();
        if (bundle != null) { // for extra safety
            Hint hint = (Hint) bundle.getSerializable(HintPagerAdapter.HintFragment.HINT_TAG);
            if (hint != null) { // for ultra safety
                if (hint.getState() != Hint.State.UNREVEALED) {

                    Point point = hint.getLocation();
                    if (!slidingUpPanel.isPanelAnchored()) {
                        mapManager.onLocationChanged(point.toLocation());
                    } else {
                        mapManager.onLocationChangedAnchored(point.toLocation());
                    }

                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hunt, menu);
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
            case R.id.action_temp_finish:
                intent = new Intent(this, HuntFinishActivity.class);

                // TODO pass arguments for statistics
                intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                startActivity(intent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        boolean isAnchored = false;
        try {
            isAnchored = slidingUpPanel.isPanelAnchored();
        } catch (Exception e) {
            super.onBackPressed(); // error occurred, act like nothing happened
        }

        if (slidingUpPanel != null && slidingUpPanel.isPanelExpanded() || isAnchored) {
            slidingUpPanel.collapsePanel(); // collapse the panel if opened (on back press)
        } else {
            super.onBackPressed(); // if the panel is collapsed, proceed as usual
        }
    }
}
