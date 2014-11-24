package com.geofind.geofind.structures;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Date;

/**
 * An Object that represents a user's Comment(review) on a Geofind Hunt.
 */

public class Comment implements Serializable {


    //String constants for Parse.com class and fields names.
    private static final String PARSE_CLASS_NAME = "Comment";
    private static final String PARSE_TITLE_FIELD = "title";
    private static final String PARSE_REVIEW_FIELD = "review";
    private static final String PARSE_RATING_FIELD = "rating";
    private static final String PARSE_USER_ID_FIELD = "userID";


    /**
     * The title of this comment.
     */
    private String _title;

    /**
     * The body of this comment.
     */
    private String _review;

    /**
     * The rating that the commenter gave to the hunt on which he commented(1-5 stars).
     */
    private float _rating;

    /**
     * The date this comment was created.
     */
    private Date _dateCreated;

    /**
     * The Google id of the commenter who created this comment.
     */
    private String _creatorID;

    /**
     * Constructor.
     *
     * @param title           - the title of this comment.
     * @param review          - the body of this comment.
     * @param rating          - the rating of this comment.
     * @param googleApiClient - the {@link com.google.android.gms.common.api.GoogleApiClient} object to
     *                        extract the google id of the current user.
     */
    public Comment(String title, String review, float rating, GoogleApiClient googleApiClient) {
        _title = title;
        _review = review;
        _rating = rating;
        _creatorID =  Plus.PeopleApi.getCurrentPerson(googleApiClient).getId();
    }

    /**
     * Constructor.
     *
     * @param remoteComment - The {@link com.parse.ParseObject} representation of this comment,
     *                      which was received from parse.com database.
     */
    public Comment(ParseObject remoteComment) {
        _title = remoteComment.getString(PARSE_TITLE_FIELD);
        _review = remoteComment.getString(PARSE_REVIEW_FIELD);
        _rating = (float) remoteComment.getDouble(PARSE_RATING_FIELD);
        _dateCreated = remoteComment.getCreatedAt();
        _creatorID = remoteComment.getString(PARSE_USER_ID_FIELD);
    }

    /**
     * Returns this comment's title.
     * @return - this comment's title.
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Return this comment's body.
     * @return - this comment's body.
     */
    public String getReview() {
        return _review;
    }

    /**
     * Returns this comment's rating.
     * @return - this comment's rating.
     */
    public float getRating() {
        return _rating;
    }

    /**
     * Returns this comment's creation date.
     * @return - this comment's creation date.
     */
    public Date getDateCreated() {
        return _dateCreated;
    }

    /**
     * Returns the google id of the user who created this comment.
     * @return - the google id of the user who created this comment.
     */
    public String getCreatorID() {
        return _creatorID;
    }

    /**
     * Returns a {@link com.parse.ParseObject} representation of this comment.
     * @return - a {@link com.parse.ParseObject} representation of this comment.
     */
    public ParseObject toParseObject() {
        ParseObject remoteComment = new ParseObject(PARSE_CLASS_NAME);
        remoteComment.put(PARSE_TITLE_FIELD, _title);
        remoteComment.put(PARSE_REVIEW_FIELD, _review);
        remoteComment.put(PARSE_RATING_FIELD, _rating);
        remoteComment.put(PARSE_USER_ID_FIELD, _creatorID);

        return remoteComment;
    }

}
