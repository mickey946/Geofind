package com.geofind.geofind;

import com.parse.ParseObject;

import java.io.Serializable;

/**
 * Created by Tzafrir on 31/10/2014.
 */

public class Comment implements Serializable {

    private String _title;
    private String _review;
    private float _rating;

    public Comment(String title, String review, float rating) {
        _title = title;
        _review = review;
        _rating = rating;
    }

    public Comment(ParseObject remoteComment) {
        _title = remoteComment.getString("title");
        _review = remoteComment.getString("review");
        _rating = (Float) remoteComment.get("rating");
    }

    public String getTitle() {
        return _title;
    }

    public String getReview() {
        return _review;
    }

    public float getRating() {
        return _rating;
    }

    public ParseObject toParseObject() {
        ParseObject remoteComment = new ParseObject("Comment");
        remoteComment.put("title", _title);
        remoteComment.put("review", _review);
        remoteComment.put("rating", _rating);

        return remoteComment;
    }


}
