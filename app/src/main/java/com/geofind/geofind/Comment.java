package com.geofind.geofind;

import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tzafrir on 31/10/2014.
 */

public class Comment implements Serializable {

    private String _title;
    private String _review;
    private float _rating;
    private Date _dateCreated;


    public Comment(String title, String review, float rating) {
        _title = title;
        _review = review;
        _rating = rating;
    }

    public Comment(ParseObject remoteComment) {
        _title = remoteComment.getString("title");
        _review = remoteComment.getString("review");
        _rating = (float) remoteComment.getDouble("rating");
        _dateCreated = remoteComment.getCreatedAt();
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

    public Date getDateCreated() {
        return _dateCreated;
    }

    public ParseObject toParseObject() {
        ParseObject remoteComment = new ParseObject("Comment");
        remoteComment.put("title", _title);
        remoteComment.put("review", _review);
        remoteComment.put("rating", _rating);

        return remoteComment;
    }


}
