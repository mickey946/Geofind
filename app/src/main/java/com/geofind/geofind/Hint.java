package com.geofind.geofind;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;

/**
 * Created by mickey on 04/10/14.
 */
@ParseClassName("Hint")
public class Hint extends ParseObject implements Serializable {


    public enum State {
        UNREVEALED, REVEALED, SOLVED
    }

    // TODO add picture, recording and video (ParseFile)

    /**
     * Zero arguments constructor. required by Parse.
     */
    public Hint() {
    }

    public void initialize(String text, Point location) {
        put("text", text);
        put("location", location);
        put("state", State.UNREVEALED);
    }

    public String getText() {
        return getString("text");
    }

    public Point getLocation() {
        return (Point) get("location");
    }

    public State getState() {
        return (State) get("state");
    }

    public void setState(State state) {
        put("state", state);
    }
}
