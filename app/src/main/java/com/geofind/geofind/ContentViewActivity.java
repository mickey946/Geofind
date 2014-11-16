package com.geofind.geofind;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.example.games.basegameutils.BaseGameActivity;


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
    public static final String IMAGE_PARSE = "IMAGE_PARSE";

    /**
     * Intent tag for passing video or audio parse id (video or audio stored in parse).
     */
    public static final String VIDEO_AUDIO_PARSE = "VIDEO_AUDIO";

    /**
     * A tag used to preserve video or audio playback position on orientation change.
     */
    private static final String POS_TAG = "POS";

    /**
     * A tag used to preserve video or audio playback on orientation change.
     */
    private static final String PLAYING_TAG = "PLAYING";

    /**
     * The video view that shows hint video or audio.
     */
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_view);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String selectedImageUriString = bundle.getString(IMAGE_URI);
        String selectedVideoAudioString = bundle.getString(VIDEO_AUDIO_URI);
        Hint hintWithImage = (Hint) bundle.getSerializable(IMAGE_PARSE);

        if (selectedImageUriString != null) { // user views his selected image
            setUpImageView(selectedImageUriString);
        } else if (selectedVideoAudioString != null) { // user views his selected video or audio
            setUpVideoAudioView(selectedVideoAudioString);
        } else if (hintWithImage != null) { // user views a hint image
            hintWithImage.downloadFiles(new Hint.DownloadFiles() {
                @Override
                public void updateImage(Bitmap bitmap) {
                    setUpImageView(bitmap);
                }

                @Override
                public void updateVideo(MediaStore.Video vid) {
                    // TODO remove this for not downloading video for nothing
                }

                @Override
                public void updateAudio(MediaStore.Audio aud) {
                    // TODO remove this for not downloading audio for nothing
                }
            });
        }

        // TODO retrieve files from parse
    }

    /**
     * Set up the image view to show the hint image.
     *
     * @param selectedImageUriString The hint image uri.toString().
     */
    private void setUpImageView(String selectedImageUriString) {
        Uri selectedImageUri = Uri.parse(selectedImageUriString);
        ImageView imageView = (ImageView) findViewById(R.id.content_image_view);
        imageView.setImageURI(selectedImageUri);
        imageView.setVisibility(View.VISIBLE);
    }

    private void setUpImageView(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.content_image_view);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
    }

    /**
     * Set up the video view to show the video or the audio of the hint.
     *
     * @param selectedVideoAudioString The hint video or audio uri.toString().
     */
    private void setUpVideoAudioView(String selectedVideoAudioString) {
        Uri selectedVideoAudio = Uri.parse(selectedVideoAudioString);
        videoView = (VideoView) findViewById(R.id.content_video_view);
        videoView.setVisibility(View.VISIBLE);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(selectedVideoAudio);
        videoView.start();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        videoView.seekTo(savedInstanceState.getInt(POS_TAG));
        if (savedInstanceState.getBoolean(PLAYING_TAG)) {
            videoView.start();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(POS_TAG, videoView.getCurrentPosition());
        outState.putBoolean(PLAYING_TAG, videoView.isPlaying());
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
