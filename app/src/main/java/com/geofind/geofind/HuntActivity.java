package com.geofind.geofind;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;


public class HuntActivity extends FragmentActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_hunt);

        setUpHunt();

        setUpSlidingUpPanel();

        setUpPagerView();
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
                Log.i(TAG, "onPanelExpanded");
            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
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
        // TODO retrieve the hints on the fly using the hunt
        ArrayList<Hint> hints = new ArrayList<Hint>();
        hints.add(new Hint("Hint1", "Description1", Hint.State.SOLVED));
        hints.add(new Hint("Hint2", "Description2", Hint.State.SOLVED));
        hints.add(new Hint("Hint3", "Description3", Hint.State.REVEALED));
        hints.add(new Hint("Hint4", "Description4", Hint.State.UNREVEALED));

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        hintPagerAdapter = new HintPagerAdapter(getSupportFragmentManager(), hints);

        // Set up the ViewPager, attaching the adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(hintPagerAdapter);
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
