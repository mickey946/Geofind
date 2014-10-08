package com.geofind.geofind;

import java.io.Serializable;

/**
 * Created by mickey on 04/10/14.
 */
public class Hint implements Serializable {
    public enum State {
        UNREVEALED, REVEALED, SOLVED
    }

    private String title;
    private String description;
    private State state;

    // TODO add a Point field
    // TODO add picture, recording and video

    public Hint(String title, String description) {
        this.title = title;
        this.description = description;
        this.state = State.UNREVEALED; // a default state for a hint
    }

    public Hint(String title, String description, State state) {
        this.title = title;
        this.description = description;
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
