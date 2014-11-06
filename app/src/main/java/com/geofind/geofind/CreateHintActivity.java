package com.geofind.geofind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class CreateHintActivity extends ActionBarActivity {

    private static final String PREF_CREATE_HINT_POINT_DISMISS = "PREF_CREATE_HINT_POINT_DISMISS";

    public static final int MAX_FILE_SIZE = 10000000;

    private SharedPreferences sharedPreferences;

    private TextView hintTextTextView;
    private Hint hint = null;
    private Integer index = null;
    private ImageView Map;
    private ProgressBar progressBar;
    private int mapWidth = -1, mapHeight = -1;
    private Point hintPoint;
    byte[] imageByteArray, videoByteArray, audioByteArray;
    private Uri imageUri, videoUri, audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hint);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get the preferences of the activity
        sharedPreferences = getPreferences(MODE_PRIVATE);

        // check and hide information cards if needed
        if (isPointInfoDismissed()) {
            hidePointInfo();
        }

        hintTextTextView = (TextView) findViewById(R.id.create_hint_description);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                hint = (Hint) bundle.getSerializable(getResources().getString(R.string.intent_hint_extra));
                index = bundle.getInt(getResources().getString(R.string.intent_hint_index_extra));
                if (hint != null) { // the user is editing and existing hint
                    hintTextTextView.setText(hint.getText());
                    hintPoint = hint.getLocation();
                }
            }
        }

        // load the picked point map
        Map = (ImageView) findViewById(R.id.create_hint_map);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        final StaticMap staticMap = new StaticMap(Map, progressBar);
        ViewTreeObserver vto = Map.getViewTreeObserver();
        if (mapHeight == -1 || mapWidth == -1) {
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mapHeight = Map.getHeight();
                        mapWidth = Map.getWidth();
                        if (hint == null) {
                            staticMap.execute(new StaticMap.StaticMapDescriptor(mapWidth, mapHeight));
                        } else {
                            staticMap.execute(new StaticMap.StaticMapDescriptor(
                                    hint.getLocation().toLatLng(), mapWidth, mapHeight));
                        }

                    }
                });
            }
        } else {
            staticMap.execute(new StaticMap.StaticMapDescriptor(mapWidth, mapHeight));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_hint, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_hint) {
            submitHint();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if the user filled all the required fields
     *
     * @return whether the user filled all the required fields or not
     */
    public boolean checkInput() {
        boolean legal = !hintTextTextView.getText().toString().trim().equals("");

        if (!legal) {
            Toast.makeText(this, getString(R.string.create_hint_fields_error), Toast.LENGTH_LONG).show();
        }

        if (hintPoint == null) {
            legal = false;
            Toast.makeText(this, getString(R.string.create_hint_point_missing), Toast.LENGTH_LONG).show();
        }

        return legal;
    }

    /**
     * Try to send away the created hint (if all the required fields are legal) and close the
     * activity.
     */
    public void submitHint() {
        if (checkInput()) { // check if the user filled all required fields

            String imageStr = null, videoStr = null, audioStr = null;

            if (imageUri != null) {
                imageStr = imageUri.toString();
            }

            if (videoUri != null) {
                videoStr = videoUri.toString();
            }

            if (audioUri != null) {
                audioStr = audioUri.toString();
            }

            Hint hint = new Hint(hintTextTextView.getText().toString(), hintPoint,
                    imageStr, videoStr, audioStr);

            // send away the hint (and it's index, if present)
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.intent_hint_extra), hint);
            intent.putExtra(getString(R.string.intent_hint_index_extra), index);
            setResult(RESULT_OK, intent);

            //close this Activity
            finish();
        } // else: do not exit
    }

    public void openPointPicker(View view) {
        Intent intent = new Intent(this, PickPointActivity.class);
        if (hintPoint != null)
            intent.putExtra(getString(R.string.intent_hint_point_extra), hintPoint);

        startActivityForResult(intent, getResources().getInteger(R.integer.intent_point_result));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == getResources().getInteger(R.integer.intent_point_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user picked a point
                Bundle bundle = data.getExtras();
                hintPoint = (Point) bundle.getSerializable(getString(R.string.intent_hint_extra));
                Map.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                new StaticMap(Map, progressBar).execute(
                        new StaticMap.StaticMapDescriptor(hintPoint.toLatLng(), mapWidth, mapHeight));
            }

        } else if (requestCode == getResources().getInteger(R.integer.intent_picture_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user picked a picture
                ImageView imageView = (ImageView) findViewById(R.id.create_hint_image);

                final Uri selectedImageUri = data.getData();
                imageUri = selectedImageUri;

                boolean isTooBig = false;
                try {
                    // convert the image to a byte array
                    imageByteArray = uriToByteArray(selectedImageUri);
                    if (imageByteArray == null) { // file is too big
                        isTooBig = true;
                        Toast.makeText(this, getString(R.string.create_hint_max_file_error),
                                Toast.LENGTH_LONG).show();
                    }

                    // Crop the center of the bitmap that fits the view
                    Bitmap inputBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            selectedImageUri);
                    Bitmap displayBitmap;

                    if (inputBitmap.getWidth() >= inputBitmap.getHeight()) {
                        displayBitmap = Bitmap.createBitmap(
                                inputBitmap,
                                inputBitmap.getWidth() / 2 - inputBitmap.getHeight() / 2,
                                0,
                                inputBitmap.getHeight(),
                                inputBitmap.getHeight()
                        );
                    } else {
                        displayBitmap = Bitmap.createBitmap(
                                inputBitmap,
                                0,
                                inputBitmap.getHeight() / 2 - inputBitmap.getWidth() / 2,
                                inputBitmap.getWidth(),
                                inputBitmap.getWidth()
                        );
                    }

                    // set the image as Hint Picture drawable
                    imageView.setImageBitmap(displayBitmap);

                    // clicking on the image enlarges it
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(),
                                    ContentViewActivity.class);
                            intent.putExtra(ContentViewActivity.IMAGE_URI,
                                    selectedImageUri.toString());
                            startActivity(intent);
                        }
                    });


                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.create_hint_kitkat_file_error),
                            Toast.LENGTH_LONG).show();
                } finally {
                    if (!isTooBig) {
                        // show the image in the hint activity
                        imageView.setVisibility(View.VISIBLE);

                        // show the remove button
                        Button removeButton = (Button) findViewById(R.id.create_hint_remove_picture);
                        removeButton.setVisibility(View.VISIBLE);

                        // hide the select button
                        Button selectButton = (Button) findViewById(R.id.create_hint_select_picture);
                        selectButton.setVisibility(View.GONE);
                    }
                }
            }

        } else if (requestCode == getResources().getInteger(R.integer.intent_video_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user picked a video

                final Uri selectedVideoUri = data.getData();
                videoUri = selectedVideoUri;

                boolean isTooBig = false;
                try { // convert the video to a byte array
                    videoByteArray = uriToByteArray(selectedVideoUri);
                    if (videoByteArray == null) { // file is too big
                        isTooBig = true;
                        Toast.makeText(this, getString(R.string.create_hint_max_file_error),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.create_hint_kitkat_file_error),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } finally {
                    if (!isTooBig) {
                        // show the remove button
                        Button removeButton = (Button) findViewById(R.id.create_hint_remove_video);
                        removeButton.setVisibility(View.VISIBLE);

                        // hide the select button
                        Button selectButton = (Button) findViewById(R.id.create_hint_select_video);
                        selectButton.setVisibility(View.GONE);

                        // Reveal the play video button
                        View playVideoView = findViewById(R.id.create_hint_play_video_layout);
                        playVideoView.setVisibility(View.VISIBLE);

                        Button playVideoButton = (Button) findViewById(R.id.create_hint_play_video);
                        playVideoButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(),
                                        ContentViewActivity.class);
                                intent.putExtra(ContentViewActivity.VIDEO_AUDIO_URI,
                                        selectedVideoUri.toString());
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

        } else if (requestCode == getResources().getInteger(R.integer.intent_audio_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user picked an audio

                final Uri selectedAudioUri = data.getData();
                audioUri = selectedAudioUri;

                boolean isTooBig = false;
                try {
                    // convert the video to a byte array
                    audioByteArray = uriToByteArray(selectedAudioUri);
                    if (audioByteArray == null) { // file is too big
                        isTooBig = true;
                        Toast.makeText(this, getString(R.string.create_hint_max_file_error),
                                Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.create_hint_kitkat_file_error), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } finally {
                    if (!isTooBig) {
                        // show the remove button
                        Button removeButton = (Button) findViewById(R.id.create_hint_remove_audio);
                        removeButton.setVisibility(View.VISIBLE);

                        // hide the select button
                        Button selectButton = (Button) findViewById(R.id.create_hint_select_audio);
                        selectButton.setVisibility(View.GONE);

                        // Reveal the play video button
                        View playVideoView = findViewById(R.id.create_hint_play_audio_layout);
                        playVideoView.setVisibility(View.VISIBLE);

                        Button playAudioButton = (Button) findViewById(R.id.create_hint_play_audio);
                        playAudioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(),
                                        ContentViewActivity.class);
                                intent.putExtra(ContentViewActivity.VIDEO_AUDIO_URI,
                                        selectedAudioUri.toString());
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Open image selection.
     *
     * @param view The current view.
     */
    public void openImageSelection(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        // Verify the intent resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        // Start an activity if it's safe
        if (activities.size() > 0) {
            startActivityForResult(intent,
                    getResources().getInteger(R.integer.intent_picture_result));
        } else { // No file manager available
            Toast.makeText(this, getString(R.string.create_hint_file_manager_error), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open video selection.
     *
     * @param view The current view.
     */
    public void openVideoSelection(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");

        // Verify the intent resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        // Start an activity if it's safe
        if (activities.size() > 0) {
            startActivityForResult(intent,
                    getResources().getInteger(R.integer.intent_video_result));
        } else { // No file manager available
            Toast.makeText(this, getString(R.string.create_hint_file_manager_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open audio selection.
     *
     * @param view The current view.
     */
    public void openAudioSelection(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");

        // Verify the intent resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        // Start an activity if it's safe
        if (activities.size() > 0) {
            startActivityForResult(intent,
                    getResources().getInteger(R.integer.intent_audio_result));
        } else { // No file manager available
            Toast.makeText(this, getString(R.string.create_hint_file_manager_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Convert a file given by it's uri to a byte array.
     *
     * @param uri The uri of the given file.
     * @return A byte array representing the file.
     * @throws IOException On a read error.
     */
    public byte[] uriToByteArray(Uri uri) throws IOException {
        // open streams
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // read the file 1024 bytes at a time and write them to the OutputStream
        byte[] b = new byte[1024];
        int totatlBytesRead = 0;
        int bytesRead;
        while ((bytesRead = inputStream.read(b)) != -1) {
            totatlBytesRead += bytesRead;
            byteArrayOutputStream.write(b, 0, bytesRead);
            if (totatlBytesRead > MAX_FILE_SIZE) {
                // close the streams
                byteArrayOutputStream.close();
                inputStream.close();
                return null;
            }
        }

        // get byte array from the stream
        byte[] bytes = byteArrayOutputStream.toByteArray();

        // close the streams
        byteArrayOutputStream.close();
        inputStream.close();

        return bytes;
    }

    /**
     * Check if the card view was set to be hidden (dismissed) before.
     *
     * @return whether the card was dismissed in the past.
     */
    private boolean isPointInfoDismissed() {
        return sharedPreferences.getBoolean(PREF_CREATE_HINT_POINT_DISMISS, false);
    }

    /**
     * Hide point info card view.
     */
    private void hidePointInfo() {
        View infoCard = findViewById(R.id.create_hint_point_info);
        infoCard.setVisibility(View.GONE);
    }

    /**
     * Dismiss the Point card view info and save so it won't show again.
     *
     * @param view The current view.
     */
    public void dismissPointInfo(View view) {
        hidePointInfo();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_CREATE_HINT_POINT_DISMISS, true);
        editor.apply();
    }

    public void removeSelectedImage(View view) {
        // hide the image view
        ImageView imageView = (ImageView) findViewById(R.id.create_hint_image);
        imageView.setVisibility(View.GONE);

        // hide the remove button
        Button removeButton = (Button) findViewById(R.id.create_hint_remove_picture);
        removeButton.setVisibility(View.GONE);

        // show the select button
        Button selectButton = (Button) findViewById(R.id.create_hint_select_picture);
        selectButton.setVisibility(View.VISIBLE);

    }

    public void removeSelectedVideo(View view) {
        // hide the play video button
        View playVideoView = findViewById(R.id.create_hint_play_video_layout);
        playVideoView.setVisibility(View.GONE);

        // hide the remove button
        Button removeButton = (Button) findViewById(R.id.create_hint_remove_video);
        removeButton.setVisibility(View.GONE);

        // show the select button
        Button selectButton = (Button) findViewById(R.id.create_hint_select_video);
        selectButton.setVisibility(View.VISIBLE);

    }

    public void removeSelectedAudio(View view) {
        // hide the play audio button
        View playVideoView = findViewById(R.id.create_hint_play_audio_layout);
        playVideoView.setVisibility(View.GONE);

        // hide the remove button
        Button removeButton = (Button) findViewById(R.id.create_hint_remove_audio);
        removeButton.setVisibility(View.GONE);

        // show the select button
        Button selectButton = (Button) findViewById(R.id.create_hint_select_audio);
        selectButton.setVisibility(View.VISIBLE);

    }
}
