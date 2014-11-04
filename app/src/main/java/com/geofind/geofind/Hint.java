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


    public enum State {
        UNREVEALED, REVEALED, SOLVED
    }

    private String _text;
    private State _state;
    private Point _location;
    private String _image, _video, _audio;
    private String _parseId;
    private Context _context;

    public Hint(String text, Point location, String image, String video, String audio) {
        _text = text;
        _state = State.UNREVEALED; // a default state for a hint
        _location = location;
        _image = image;
        _video = video;
        _audio = audio;
    }

    public Hint(String text, Point location, State state) {
        _text = text;
        _location = location;
        _state = state;
    }

    public Hint(ParseObject remoteHint) {
        _text = remoteHint.getString("text");
        _location = new Point(remoteHint.getParseGeoPoint("location"));
        _state = State.UNREVEALED;
        _parseId = remoteHint.getObjectId();
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

    public ParseObject toParseObject(Context c) {
        ParseObject remoteHint = new ParseObject("Hint");
        remoteHint.put("text", _text);
        remoteHint.put("location", _location.toParseGeoPoint());
        _context = c;

        //TODO not sure this is the right place to perform the files upload to parse!
        //TODO consider changing this.
        try {
            if (_image != null) {
                ParseFile image = new ParseFile("image.png", uriToByteArray(Uri.parse(_image)));
                image.saveInBackground();
                remoteHint.put("image", image);
            }

            if (_video != null) {
                ParseFile video = new ParseFile("video.mp4", uriToByteArray(Uri.parse(_video)));
                video.saveInBackground();
                remoteHint.put("video", video);
            }

            if (_audio != null) {
                ParseFile audio = new ParseFile("audio.mp3", uriToByteArray(Uri.parse(_audio)));
                audio.saveInBackground();
                remoteHint.put("audio", audio);
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

    public interface DownloadFiles {
        void updateImage(Bitmap im);

        void updateVideo(MediaStore.Video vid);

        void updateAudio(MediaStore.Audio aud);
    }

    public void downloadFiles(DownloadFiles callback) {
        getFile(callback, "image");
        //TODO video
        //TODO audio
    }

    private void getFile(final DownloadFiles callback, final String fileType) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("hint");
        query.whereExists(fileType);
        query.getInBackground(_parseId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    final ParseFile file = (ParseFile) parseObject.get(fileType);
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e == null) {
                                if (fileType.equals("image")) {
                                    callback.updateImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                }
                                //TODO video
                                //TODO audio
                            }
                        }
                    });
                }
            }
        });
    }

}
