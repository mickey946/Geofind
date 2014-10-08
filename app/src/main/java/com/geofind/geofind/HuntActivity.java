package com.geofind.geofind;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.MapFragment;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class HuntActivity extends FragmentActivity {

    public static final int MIN_UPDATE_TIME = 0; //TODO decide the correct values
    public static final float MIN_UPDATE_DISTANCE = 30.f; //TODO decide the correct values

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
     * The map manager controller
     */
    private MapManager mapManager;

    /**
     * The displayed hints.
     */
    private Hint[] hints = {// TODO retrieve the hints on the fly using the hunt
            new Hint("Hint1", "Description1", new Point(31.66831, 35.11371), Hint.State.SOLVED),
            new Hint("Hint2", "Description2", new Point(31.86831, 35.21371), Hint.State.SOLVED),
            new Hint("Hint3", "Description3", new Point(31.56831, 35.11371), Hint.State.REVEALED),
            new Hint("Hint4", "Description4", new Point(31.76831, 35.21371), Hint.State.UNREVEALED)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_hunt);

        setUpHunt();

        setUpPagerView();

        setUpSlidingUpPanel();

        setUpMap();
    }

    /**
     * Retrieve the hunt from the intent that started the activity and set it up.
     */
    private void setUpHunt() {
        Intent intent = getIntent();

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
                setActionBarTranslation(slidingUpPanel.getCurrentParalaxOffset());
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
            }

            @Override
            public void onPanelAnchored(View panel) {
                // when the user anchors the panel, he sees both the hint and both the map, so it's
                // good to assume that he wants to focus on the point (if it is visible)
                Log.i(TAG, "onPanelAnchored");

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
        hintPagerAdapter = new HintPagerAdapter(getSupportFragmentManager(), hints);

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
        mapManager.focusOnCurrentLocation(MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE);

        for (Hint hint : hints) {
            if (hint.getState() != Hint.State.UNREVEALED) {
                mapManager.setMarker(hint.getLocation().toLocation(), hint.getTitle(), hint.getState());
            }
        }
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
            Hint hint = (Hint) bundle.getSerializable(HintPagerAdapter.HintFragment.TAG);
            if (hint != null) { // for ultra safety
                if (hint.getState() != Hint.State.UNREVEALED) {
                    Point point = hint.getLocation();
                    Log.d("WHAT", "HEY");
                    // TODO use the point to focus
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
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_temp_finish:
                Intent intent = new Intent(this, HuntFinishActivity.class);

                // TODO pass arguments for statistics
                intent.putExtra(getResources().getString(R.string.intent_hunt_extra), hunt);

                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get the height of the action bar for animation.
     *
     * @return the height of the action bar.
     */
    private int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * Translate the action bar up as the user slides up the bottom panel.
     *
     * @param y The height of the translation.
     */
    public void setActionBarTranslation(float y) {
        // Figure out the actionbar height
        int actionBarHeight = getActionBarHeight();
        // A hack to add the translation to the action bar
        ViewGroup content = ((ViewGroup) findViewById(android.R.id.content).getParent());
        int children = content.getChildCount();
        for (int i = 0; i < children; i++) {
            View child = content.getChildAt(i);
            if (child.getId() != android.R.id.content) {
                if (y <= -actionBarHeight) {
                    child.setVisibility(View.GONE);
                } else {
                    child.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        child.setTranslationY(y);
                    } else {
                        AnimatorProxy.wrap(child).setTranslationY(y);
                    }
                }
            }
        }
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
