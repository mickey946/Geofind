package com.geofind.geofind;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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
 * Created by mickey on 04/10/14.
 */

public class Hint implements Serializable {


    private static final String PARSE_CLASS_NAME = "Hint";
    private static final String PARSE_TEXT_FIELD = "text";
    private static final String PARSE_LOCATION_FIELD = "location";
    private static final String PARSE_IMAGE_FIELD = "image";
    public static final String PARSE_VIDEO_FIELD = "video";
    public static final String PARSE_AUDIO_FIELD = "audio";

    private static final String IMAGE_FILE_SUFFIX = ".jpg";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";
    private static final String AUDIO_FILE_SUFFIX = ".mp3";


    public enum State {
        UNREVEALED, REVEALED, SOLVED
    }

    private String _text;
    private State _state;
    private Point _location;
    private String _image, _video, _audio;
    private String _parseId;
    private boolean _hasImage = false;
    private boolean _hasVideo = false;
    private boolean _hasAudio = false;

    private Context _context;

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

    public Hint(String text, Point location, State state) {
        _text = text;
        _location = location;
        _state = state;
    }

    public Hint(ParseObject remoteHint) {
        _text = remoteHint.getString(PARSE_TEXT_FIELD);
        _location = new Point(remoteHint.getParseGeoPoint(PARSE_LOCATION_FIELD));
        _state = State.UNREVEALED;
        _parseId = remoteHint.getObjectId();
        _hasImage = remoteHint.has(PARSE_IMAGE_FIELD);
        _hasVideo = remoteHint.has(PARSE_VIDEO_FIELD);
        _hasAudio = remoteHint.has(PARSE_AUDIO_FIELD);
    }

    public String getText() {
        return _text;
    }

    public Point getLocation() {
        return _location;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getParseId() {
        return _parseId;
    }

    public boolean hasImage() {
        return _hasImage;
    }

    public boolean hasVideo() {
        return _hasVideo;
    }

    public boolean hasAudio() {
        return _hasAudio;
    }

    public ParseObject toParseObject(Context c) {
        ParseObject remoteHint = new ParseObject(PARSE_CLASS_NAME);
        remoteHint.put(PARSE_TEXT_FIELD, _text);
        remoteHint.put(PARSE_LOCATION_FIELD, _location.toParseGeoPoint());
        _context = c;

        //TODO not sure this is the right place to perform the files upload to parse!
        //TODO consider changing this.
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




    public interface DownloadImage {
        void updateImage(Bitmap im);
    }

    public interface DownloadVideoAudio {
        void updateVideoAudio(String link);
    }

    public void downloadImage(DownloadImage callback) {
        getImage(callback);

    }

    public void downloadVideoAudio(DownloadVideoAudio callback, String fileType) {
        getVideoAudio(callback, fileType);

    }


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
                                    callback.updateImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
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
                        callback.updateVideoAudio(file.getUrl());
                    }
                } else {
                    System.out.println("parse exception2 " + e.getMessage());
                }
            }
        });
    }

}
