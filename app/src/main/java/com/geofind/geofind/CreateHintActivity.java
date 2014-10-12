package com.geofind.geofind;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.List;


public class CreateHintActivity extends Activity {

    private TextView hintTitleTextView;
    private TextView hintTextTextView;
    private Hint hint = null;
    private Integer index = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hint);

        hintTitleTextView = (TextView) findViewById(R.id.create_hint_title);
        hintTextTextView = (TextView) findViewById(R.id.create_hint_description);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                hint = (Hint) bundle.getSerializable(
                        getResources().getString(R.string.intent_hint_extra));
                index = bundle.getInt(getResources().getString(R.string.intent_hint_index_extra));
                if (hint != null) { // the user is editing and existing hint
                    hintTitleTextView.setText(hint.getTitle());
                    hintTextTextView.setText(hint.getDescription());
                }
            }
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
        boolean legal = !hintTitleTextView.getText().toString().trim().equals("")
                && !hintTextTextView.getText().toString().trim().equals("");

        if (!legal) {
            Toast.makeText(this, getString(R.string.create_hint_fields_error),
                    Toast.LENGTH_LONG).show();
        }

        return legal;
    }

    /**
     * Try to send away the created hint (if all the required fields are legal) and close the
     * activity.
     */
    public void submitHint() {
        if (checkInput()) { // check if the user filled all required fields
            Hint hint = new Hint(hintTitleTextView.getText().toString(),
                    hintTextTextView.getText().toString(), new Point(0,0) ); //TODO change to real point

            // send away the hint (and it's index, if present)
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.intent_hint_extra), hint);
            intent.putExtra(getString(R.string.intent_hint_index_extra), index);
            setResult(RESULT_OK, intent);

            //close this Activity
            finish();
        } else {
            // do not exit
        }
    }

    public void openPointPicker(View view) {
        Intent intent = new Intent(this, PickPointActivity.class);
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
                // TODO add the coordinates to the Hint object and display it on the map
            }
        } else if (requestCode == getResources().getInteger(R.integer.intent_picture_result)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // The user picked a picture
                ImageView imageView = (ImageView) findViewById(R.id.create_hint_image);

                Uri selectedImageUri = data.getData();

                if (Build.VERSION.SDK_INT < 19) { // doesn't work on KitKat (issued bug)
                    String selectedImagePath = getPath(selectedImageUri);
                    // set the image as Hint Picture drawable
                    imageView.setImageDrawable(Drawable.createFromPath(selectedImagePath));
                } else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        // set the image as Hint Picture drawable
                        parcelFileDescriptor =
                                getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        imageView.setImageBitmap(bitmap);
                        parcelFileDescriptor.close();

                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.create_hint_kitkat_file_error),
                                Toast.LENGTH_LONG).show();
                    }
                }

                // show the image in the hint activity
                imageView.setVisibility(View.VISIBLE);

                // TODO add the picture to the Hint object
            }
        }
    }

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
            Toast.makeText(this, getString(R.string.create_hint_file_manager_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Helper to retrieve the path of an image URI.
     */
    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else {
            return uri.getPath();
        }
    }
}
