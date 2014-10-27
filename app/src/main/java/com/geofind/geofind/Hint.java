package com.geofind.geofind;

import java.io.Serializable;

/**
 * Created by mickey on 04/10/14.
 */
public class Hint implements Serializable {


    public enum State {
        UNREVEALED, REVEALED, SOLVED
    }

    private String text;
    private State state;
    private Point location;

    // TODO add picture, recording and video (ParseFile)

    public Hint(String text, Point location) {
        this.text = text;
        this.state = State.UNREVEALED; // a default state for a hint
        this.location = location;
    }

    public Hint(String text, Point location, State state) {
        this.text = text;
        this.location = location;
        this.state = state;
    }

    public String getText() {
        return text;
    }

    public Point getLocation() {
        return location;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
