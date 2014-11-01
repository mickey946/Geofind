package com.geofind.geofind;


import com.parse.ParseFile;
import com.parse.ParseObject;

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
    private byte[] _image;
    private byte[] _video;
    private byte[] _audio;

    public Hint(String text, Point location, byte[] image, byte[] video, byte[] audio) {
        _text = text;
        _state = State.UNREVEALED; // a default state for a hint
        _location = location;
        if (image != null) _image = image.clone();
        if (video != null) _video = video.clone();
        if (audio != null) _audio = audio.clone();
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

    public ParseObject toParseObject() {
        ParseObject remoteHint = new ParseObject("Hint");
        remoteHint.put("text", _text);
        remoteHint.put("location", _location.toParseGeoPoint());

        //TODO not sure this is the right place to perform the files upload to parse!
        //TODO consider changing this.
        if (_image != null) {
            ParseFile image = new ParseFile("image.png", _image);
            image.saveInBackground();
            remoteHint.put("image", image);
        }

        if (_video != null) {
            ParseFile video = new ParseFile("video.mp4", _video);
            video.saveInBackground();
            remoteHint.put("video", video);
        }

        if (_audio != null) {
            ParseFile audio = new ParseFile("audio.mp3", _audio);
            audio.saveInBackground();
            remoteHint.put("audio", audio);
        }

        return remoteHint;
    }

}
