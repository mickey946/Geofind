package com.geofind.geofind.ui.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.geofind.geofind.R;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.ui.settings.SettingsActivity;

import java.io.InputStream;

/**
 * An {@link android.app.Activity} that is used for displaying images, videos and audios.
 */
public class ContentViewActivity extends BaseGameActivity {

    /**
     * Intent tag for passing image uri (image stored in user's device).
     */
    public static final String IMAGE_URI = "IMAGE_URI";

    /**
     * Intent tag for passing video or audio uri (video or audio stored in user's device).
     */
    public static final String VIDEO_AUDIO_URI = "VIDEO_AUDIO_URI";

    /**
     * Intent tag for passing image parse id (image stored in parse).
     */
    public static final String IMAGE_URL = "IMAGE_URL";

    /**
     * Intent tag for passing video or audio parse url (video or audio stored in parse).
     */
    public static final String VIDEO_AUDIO_URL = "VIDEO_AUDIO_URL";

    /**
     * A tag used to preserve video or audio playback position on orientation change.
     */
    private static final String POS_TAG = "POS";

    /**
     * A tag used to preserve video or audio playback on orientation change.
     */
    private static final String PLAYING_TAG = "PLAYING";

    /**
     * The {@link android.widget.VideoView} that shows hint video or audio.
     */
    private VideoView videoView;

    /**
     * The {@link android.widget.ProgressBar} that is used when loading content.
     */
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_view);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String selectedImageUriString = bundle.getString(IMAGE_URI);
        String selectedVideoAudioString = bundle.getString(VIDEO_AUDIO_URI);
        String remoteImageUrl = bundle.getString(IMAGE_URL);
        String remoteVideoAudioUrl = bundle.getString(VIDEO_AUDIO_URL);

        if (selectedImageUriString != null) { // user views his selected image
            setupImageView(selectedImageUriString);
        } else if (selectedVideoAudioString != null) { // user views his selected video or audio
            setupVideoAudioView(selectedVideoAudioString);
        } else if (remoteImageUrl != null) { // user views a hint image
            DownloadImageTask downloadImageTask = new DownloadImageTask();
            downloadImageTask.execute(remoteImageUrl);
        } else if (remoteVideoAudioUrl != null) { // user views a hint video or audio
            setupVideoAudioView(remoteVideoAudioUrl);
        }
    }

    /**
     * An {@link android.os.AsyncTask} used to download an image from Parse.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap outputBitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                outputBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error loading an image: ", e.getMessage());
                e.printStackTrace();
            }
            return outputBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(View.GONE);
            setupImageView(result);
        }
    }

    /**
     * Set up the image view to show the hint image.
     *
     * @param selectedImageUriString The hint image uri.toString().
     */
    private void setupImageView(String selectedImageUriString) {
        Uri selectedImageUri = Uri.parse(selectedImageUriString);
        ImageView imageView = (ImageView) findViewById(R.id.content_image_view);
        imageView.setImageURI(selectedImageUri);
        imageView.setVisibility(View.VISIBLE);
    }

    /**
     * Setup the {@link android.widget.ImageView} to show the given bitmap.
     * @param bitmap The bitmap to show.
     */
    private void setupImageView(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.content_image_view);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
    }

    /**
     * Set up the video view to show the video or the audio of the hint.
     *
     * @param selectedVideoAudioString The hint video or audio uri.toString().
     */
    private void setupVideoAudioView(String selectedVideoAudioString) {
        Uri selectedVideoAudio = Uri.parse(selectedVideoAudioString);
        videoView = (VideoView) findViewById(R.id.content_video_view);
        videoView.setVisibility(View.VISIBLE);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(selectedVideoAudio);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
                mp.start();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (videoView != null) {
            videoView.seekTo(savedInstanceState.getInt(POS_TAG));
            if (savedInstanceState.getBoolean(PLAYING_TAG)) {
                videoView.start();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (videoView != null) {
            outState.putInt(POS_TAG, videoView.getCurrentPosition());
            outState.putBoolean(PLAYING_TAG, videoView.isPlaying());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_content_view, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        onBackPressed();
        return null;
    }

    @Override
    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onCreateSupportNavigateUpTaskStack(builder);
        onBackPressed();
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }
}
