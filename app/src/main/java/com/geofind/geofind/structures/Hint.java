package com.geofind.geofind.structures;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * An object which represents a hint for a point in a Geofind hunt.
 */
public class Hint implements Serializable {

    //String constants for Parse.com class and fields names.
    private static final String PARSE_CLASS_NAME = "Hint";
    private static final String PARSE_TEXT_FIELD = "text";
    private static final String PARSE_LOCATION_FIELD = "location";
    private static final String PARSE_IMAGE_FIELD = "image";
    public static final String PARSE_VIDEO_FIELD = "video";
    public static final String PARSE_AUDIO_FIELD = "audio";
    private static final String IMAGE_FILE_SUFFIX = ".jpg";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";
    private static final String AUDIO_FILE_SUFFIX = ".mp3";

    /**
     * An enum for the 3 states a Hint can get(Unrevealed, Revealed, Solved).
     */
    public enum State {
        UNREVEALED, REVEALED, SOLVED
    }

    /**
     * The text of this hint.
     */
    private String _text;

    /**
     * The state of this hint.
     */
    private State _state;

    /**
     * The exact location {@link com.geofind.geofind.structures.Point} this hint hints to.
     */
    private Point _location;

    /**
     * String representation of the Uri's of this hint's media files
     */
    private String _image, _video, _audio;

    /**
     * The id of this hint object in the parse.com database.
     */
    private String _parseId;

    /**
     * Indicates if this hint has media files, respectively.
     */
    private boolean _hasImage = false;
    private boolean _hasVideo = false;
    private boolean _hasAudio = false;

    /**
     * The application context this hint currently exists in.
     */
    private Context _context;


    /**
     * Constructor.
     *
     * @param text     - The text of this hint.
     * @param location - The location of this hint.
     * @param image    - The path for this hint's image file.
     * @param video    - The path for this hint's video file.
     * @param audio    - The path for this hint's audio file.
     */
    public Hint(String text, Point location, String image, String video, String audio) {
        _text = text;
        _state = State.UNREVEALED; // a default state for a hint
        _location = location;
        _image = image;
        _hasImage = (image != null);
        _video = video;
        _hasVideo = (video != null);
        _audio = audio;
        _hasAudio = (audio != null);
    }

    /**
     * Constructor.
     *
     * @param text     - The text of this hint.
     * @param location - The {@link com.geofind.geofind.structures.Point} of this hint.
     * @param state    - The state of this hint.
     */
    public Hint(String text, Point location, State state) {
        _text = text;
        _location = location;
        _state = state;
    }

    /**
     * Constructor.
     * @param remoteHint - The {@link com.parse.ParseObject} representation of this hint, which was received
     * from parse.com database.
     */
    public Hint(ParseObject remoteHint) {
        _text = remoteHint.getString(PARSE_TEXT_FIELD);
        _location = new Point(remoteHint.getParseGeoPoint(PARSE_LOCATION_FIELD));
        _state = State.UNREVEALED;
        _parseId = remoteHint.getObjectId();
        _hasImage = remoteHint.has(PARSE_IMAGE_FIELD);
        _hasVideo = remoteHint.has(PARSE_VIDEO_FIELD);
        _hasAudio = remoteHint.has(PARSE_AUDIO_FIELD);
    }

    /**
     * Returns this hint's text.
     * @return - this hint's text.
     */
    public String getText() {
        return _text;
    }

    /**
     * Returns this hints location.
     * @return  - this hint's location.
     */
    public Point getLocation() {
        return _location;
    }

    /**
     * Returns this hint's current state.
     * @return - this hint's current state.
     */
    public State getState() {
        return _state;
    }

    /**
     * Sets this hint's state to the given state.
     * @param state - the state that should be set for this hint.
     */
    public void setState(State state) {
        _state = state;
    }

    /**
     * Returns if this hint has an image file.
     * @return - True if this hint has an image file, False otherwise.
     */
    public boolean hasImage() {
        return _hasImage;
    }

    /**
     * Returns if this hint has a video file.
     * @return - True if this hint has an video file, False otherwise.
     */
    public boolean hasVideo() {
        return _hasVideo;
    }

    /**
     * Returns if this hint has an audio file.
     * @return - True if this hint has an audio file, False otherwise.
     */
    public boolean hasAudio() {
        return _hasAudio;
    }

    /**
     * Returns a ParseObject representation of this hint.
     * @param c  - The application context this hint currently exists in.
     * @return - a ParseObject representation of this hint.
     */
    public ParseObject toParseObject(Context c) {
        ParseObject remoteHint = new ParseObject(PARSE_CLASS_NAME);
        remoteHint.put(PARSE_TEXT_FIELD, _text);
        remoteHint.put(PARSE_LOCATION_FIELD, _location.toParseGeoPoint());
        _context = c;

        try {
            if (_image != null) {
                ParseFile image = new ParseFile(PARSE_IMAGE_FIELD + IMAGE_FILE_SUFFIX,
                                                        uriToByteArray(Uri.parse(_image)));
                image.saveInBackground();
                remoteHint.put(PARSE_IMAGE_FIELD, image);
            }

            if (_video != null) {
                ParseFile video = new ParseFile(PARSE_VIDEO_FIELD + VIDEO_FILE_SUFFIX,
                                                        uriToByteArray(Uri.parse(_video)));
                video.saveInBackground();
                remoteHint.put(PARSE_VIDEO_FIELD, video);
            }

            if (_audio != null) {
                ParseFile audio = new ParseFile(PARSE_AUDIO_FIELD + AUDIO_FILE_SUFFIX,
                                                        uriToByteArray(Uri.parse(_audio)));
                audio.saveInBackground();
                remoteHint.put(PARSE_AUDIO_FIELD, audio);
            }
        } catch (IOException e) {
            Log.v("IO Exception in saving a hint file.", e.getMessage());
        }


        return remoteHint;
    }

    /**
     * Returns a byte array representation of the given Uri.
     * @param uri - the {@link android.net.Uri} to convert to byte array.
     * @return - a byte array representation of the given Uri.
     * @throws IOException
     */
    public byte[] uriToByteArray(Uri uri) throws IOException {
        // open streams
        InputStream inputStream = _context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // read the file 1024 bytes at a time and write them to the OutputStream
        byte[] b = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(b)) != -1) {
            byteArrayOutputStream.write(b, 0, bytesRead);

        }

        return byteArrayOutputStream.toByteArray();
    }


    /**
     * An interface for downloading an image from parse.com database.
     */
    public interface DownloadImage {
        void onImageReceive(Bitmap im);
        void onUrlReceive(String link);
    }

    /**
     * An interface for downloading a video/audio file from parse.com database.
     */
    public interface DownloadVideoAudio {
        void onUrlReceive(String link);
    }

    /**
     * Initiates an image file download from the parse.com database,
     * and signals the given callback when the download is complete.
     * @param callback - the callback to signal to when the image download is complete.
     */
    public void downloadImage(DownloadImage callback) {
        getImage(callback);

    }

    /**
     * Initiates a video/aucio file download from the parse.com database,
     * and signals the given callback when the download is complete.
     * @param callback - the callback to signal to when the download is complete.
     * @param fileType - the type of the file to download(video/audio).
     */
    public void downloadVideoAudio(DownloadVideoAudio callback, String fileType) {
        getVideoAudio(callback, fileType);

    }

    /**
     * Gets this hint's image file url from the parse.com database,
     * and sends it through the given callback.
     * @param callback - callback
     */
    private void getImage(final DownloadImage callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_CLASS_NAME);
        query.getInBackground(_parseId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    final ParseFile file = (ParseFile) parseObject.get(PARSE_IMAGE_FIELD);
                    if (file != null) {
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                if (e == null) {
                                    callback.onUrlReceive(file.getUrl());
                                    callback.onImageReceive(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                }
                                else {
                                    System.out.println("parse exception1 " + e.getMessage());
                                }
                            }
                        });
                    }
                } else {
                    System.out.println("parse exception2 " + e.getMessage());
                }
            }
        });
    }

    /**
     * Gets this hint's video/audio file url from the parse.com database,
     * and sends it through the given callback.
     * @param callback - callback
     * @param fileType - the type of file to return.
     */
    private void getVideoAudio(final DownloadVideoAudio callback, final String fileType) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_CLASS_NAME);
        query.getInBackground(_parseId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    String field = PARSE_VIDEO_FIELD;
                    if (fileType.equals(PARSE_AUDIO_FIELD)) {
                        field = PARSE_AUDIO_FIELD;
                    }
                    ParseFile file = (ParseFile) parseObject.get(field);
                    if (file != null) {
                        callback.onUrlReceive(file.getUrl());
                    }
                } else {
                    System.out.println("parse exception2 " + e.getMessage());
                }
            }
        });
    }

}
