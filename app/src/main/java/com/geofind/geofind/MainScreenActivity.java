package com.geofind.geofind;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.geofind.geofind.basegameutils.BaseGameActivity;
import com.google.android.gms.games.Games;


public class MainScreenActivity extends BaseGameActivity {

    /**
     * Direction of moving of the image.
     */
    private enum Direction {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT
    }

    /**
     * Animation duration in ms. The bigger the value the slower the animation.
     */
    private static final int DURATION = 100000;

    /**
     * The animator of the background.
     */
    private ValueAnimator valueAnimator;

    /**
     * Transform matrix of the image.
     */
    private final Matrix matrix = new Matrix();

    /**
     * The background image.
     */
    private ImageView background;

    /**
     * Scale factor of the image.
     */
    private float scaleFactor;

    /**
     * Current direction of the image.
     */
    private Direction direction = Direction.RIGHT_TO_LEFT;

    /**
     * Display rectangle (used to check when the image reached it's bounds).
     */
    private RectF displayRect = new RectF();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_screen);

        background = (ImageView) findViewById(R.id.background_image);
        background.post(new Runnable() {
            @Override
            public void run() {
                scaleFactor = (float) background.getHeight() /
                        (float) background.getDrawable().getIntrinsicHeight();
                matrix.postScale(scaleFactor, scaleFactor);
                background.setImageMatrix(matrix);
                animate();
            }
        });
    }

    /**
     * Animate the background image moving from left to right and back.
     */
    private void animate() {
        updateDisplayRect();
        if (direction == Direction.RIGHT_TO_LEFT) {
            animate(displayRect.left, displayRect.left -
                    (displayRect.right - background.getWidth()));
        } else {
            animate(displayRect.left, 0.0f);
        }
    }

    /**
     * Animate the background image moving by the given coordinates.
     *
     * @param from Animation start point.
     * @param to   Animation end point.
     */
    private void animate(float from, float to) {
        valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                matrix.reset();
                matrix.postScale(scaleFactor, scaleFactor);
                matrix.postTranslate(value, 0);

                background.setImageMatrix(matrix);
            }
        });

        valueAnimator.setDuration(DURATION);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (direction == Direction.RIGHT_TO_LEFT)
                    direction = Direction.LEFT_TO_RIGHT;
                else
                    direction = Direction.RIGHT_TO_LEFT;

                animate();
            }
        });
        valueAnimator.start();
    }

    /**
     * Check when the image reached it's bounds.
     */
    private void updateDisplayRect() {
        displayRect.set(0, 0, background.getDrawable().getIntrinsicWidth(),
                background.getDrawable().getIntrinsicHeight());
        matrix.mapRect(displayRect);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (valueAnimator != null) {
                valueAnimator.resume();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (valueAnimator != null) {
                valueAnimator.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    /**
     * Start {@link com.geofind.geofind.HuntListActivity} so the user can choose a hunt to play.
     *
     * @param view The current view.
     */
    public void openHuntList(View view) {
        Intent intent = new Intent(this, HuntListActivity.class);
        startActivity(intent);
    }

    /**
     * Start {@link com.geofind.geofind.CreateHuntActivity} so the user can create a hunt
     */
    public void openHuntCreation() {
        Intent intent = new Intent(this, CreateHuntActivity.class);
        startActivity(intent);
    }

    /**
     * Start {@link com.geofind.geofind.SettingsActivity} so the user can modify settings.
     *
     * @param view The current view.
     */
    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void notifiyForSignIn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.main_screen_sign_in_dialog))
                .setTitle(getString(R.string.preferences_account_sign_in_title))
                .setPositiveButton(getString(R.string.preferences_account_sign_in_title),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                beginUserInitiatedSignIn();
                                d.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_dismiss),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    @Override
    public void onSignInFailed() {
        Button createHuntButton = (Button) findViewById(R.id.main_screen_create_hunt);
        createHuntButton.setEnabled(true);
        createHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifiyForSignIn();
            }
        });
    }

    @Override
    public void onSignInSucceeded() {
        Button createHuntButton = (Button) findViewById(R.id.main_screen_create_hunt);
        createHuntButton.setEnabled(true);
        createHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHuntCreation();
            }
        });
    }

    public void openAchievements(View view) {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 1);
        }
    }
}
