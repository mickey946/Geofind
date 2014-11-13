package com.geofind.geofind;

import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tzafrir on 31/10/2014.
 */

public class Comment implements Serializable {

    private static final String PARSE_CLASS_NAME = "Comment";
    private static final String PARSE_TITLE_FIELD = "title";
    private static final String PARSE_REVIEW_FIELD = "review";
    private static final String PARSE_RATING_FIELD = "rating";
    private static final String PARSE_USER_ID_FIELD = "userID";



    private String _title;
    private String _review;
    private float _rating;
    private Date _dateCreated;
    private String _creatorID;


    public Comment(String title, String review, float rating) {
        _title = title;
        _review = review;
        _rating = rating;
        //TODO change "creatorID"
        _creatorID = "creatorID";
    }

    public Comment(ParseObject remoteComment) {
        _title = remoteComment.getString(PARSE_TITLE_FIELD);
        _review = remoteComment.getString(PARSE_REVIEW_FIELD);
        _rating = (float) remoteComment.getDouble(PARSE_RATING_FIELD);
        _dateCreated = remoteComment.getCreatedAt();
        _creatorID = remoteComment.getString(PARSE_USER_ID_FIELD);
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

    public String getCreatorID() {
        return _creatorID;
    }

    public ParseObject toParseObject() {
        ParseObject remoteComment = new ParseObject(PARSE_CLASS_NAME);
        remoteComment.put(PARSE_TITLE_FIELD, _title);
        remoteComment.put(PARSE_REVIEW_FIELD, _review);
        remoteComment.put(PARSE_RATING_FIELD, _rating);
        remoteComment.put(PARSE_USER_ID_FIELD, _creatorID);

        return remoteComment;
    }


}
