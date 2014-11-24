package com.geofind.geofind.structures;

import android.content.Context;
import android.util.Log;

import com.geofind.geofind.geoutils.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseException;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An object which represents a Geofind hunt.
 */
public class Hunt implements Serializable {

    //Constants for unit conversion.
    public static final float METERS_TO_MILES = 0.000621371f;
    public static final float METERS_TO_KILOMETERS = 0.001f;
    public static final int DIGIT_PRECISION = 3;

    //String constants for Parse.com class and fields names.
    public static final String PARSE_CLASS_NAME = "Hunt";
    private static final String PARSE_TITLE_FIELD = "title";
    private static final String PARSE_DESCRIPTION_FIELD = "description";
    private static final String PARSE_CREATOR_ID_FIELD = "creatorID";
    private static final String PARSE_FIRST_POINT_FIELD = "firstPoint";
    private static final String PARSE_RADIUS_FIELD = "radius";
    private static final String PARSE_TOTAL_DISTANCE_FIELD = "totalDistance";
    private static final String PARSE_RATING_FIELD = "rating";
    public static final String PARSE_TOTAL_RATING_FIELD = "totalRating";
    public static final String PARSE_NUM_OF_RATERS_FIELD = "numOfRaters";
    public static final String PARSE_HINTS_FIELD = "hints";
    public static final String PARSE_COMMENTS_FIELD = "comments";

    /**
     * This hunts title.
     */
    private String _title;

    /**
     * This hunts description.
     */
    private String _description;

    /**
     * The google id of user who created this hunt.
     */
    private String _creatorID;

    /**
     * The parse.com database id of this hunt.
     */
    private String _parseID;

    /**
     * The {@link com.geofind.geofind.structures.Point} representation of this hunt's first point.
     */
    private Point _firstPoint;

    /**
     * The distance between the two furthest point inf this hunt.
     */
    private float _radius;

    /**
     * The sum of distances between every two adjacent points in this hunt.
     */
    private float _totalDistance;

    /**
     * The current rating of this hunt.
     */
    private float _rating;

    /**
     * The sum of all user's rating's this hunt has received.
     */
    private float _totalRating;

    /**
     * The number of users who rated this hunt.
     */
    private int _numOfRaters;

    /**
     * The list of hints for this hunt.
     */
    private ArrayList<Hint> _hints;

    /**
     * the list of comments this hunt has received.
     */
    private ArrayList<Comment> _comments;

    /**
     * Constructor for HuntListActivity.
     */
    public Hunt(String title, String description, String creatorID, ParseGeoPoint firstPoint,
                float rating, float radius, float totalDistance) {
        _title = title;
        _description = description;
        _creatorID = creatorID;
        _firstPoint = new Point(firstPoint);
        _rating = rating;
        _radius = radius;
        _totalDistance = totalDistance;
    }

    /**
     * Constructor for CreateHuntActivity.
     */
    public Hunt(String title, String description, String creatorID, ArrayList<Hint> hints) {
        _title = title;
        _description = description;
        _creatorID = creatorID;
        _firstPoint = hints.get(0).getLocation();
        _radius = GeoUtils.calcRadius(hints);
        _totalDistance = GeoUtils.calcPathLength(hints);
        _rating = 0;
        _totalRating = 0;
        _numOfRaters = 0;
        _hints = hints;
        _comments = new ArrayList<Comment>();
    }

    /**
     * Constructor.
     *
     * @param remoteHunt - The {com.parse.ParseObject} representation of this hunt, which was received
     *                   from parse.com database.
     */
    public Hunt(ParseObject remoteHunt) {
        _title = remoteHunt.getString(PARSE_TITLE_FIELD);
        _description = remoteHunt.getString(PARSE_DESCRIPTION_FIELD);
        _creatorID = remoteHunt.getString(PARSE_CREATOR_ID_FIELD);
        _parseID = remoteHunt.getObjectId();
        _firstPoint = new Point(remoteHunt.getParseGeoPoint(PARSE_FIRST_POINT_FIELD));
        _radius = (float) remoteHunt.getDouble(PARSE_RADIUS_FIELD);
        _totalDistance = (float) remoteHunt.getDouble(PARSE_TOTAL_DISTANCE_FIELD);
        _totalRating = (float) remoteHunt.getDouble(PARSE_TOTAL_RATING_FIELD);
        _numOfRaters = remoteHunt.getInt(PARSE_NUM_OF_RATERS_FIELD);
        _hints = new ArrayList<Hint>();
        _comments = new ArrayList<Comment>();


        final List<ParseObject> remoteComments = remoteHunt.getList(PARSE_COMMENTS_FIELD);
        ParseObject.fetchAllInBackground(remoteComments, new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject remoteComment : remoteComments) {
                        _comments.add(new Comment(remoteComment));
                        Log.v("Parse Comment List fetching: ", "Success");
                    }
                } else {
                    Log.v("Parse Comment List fetching: ", "failed");
                }
            }
        });
    }

    /**
     * Returns this hunt's title.
     *
     * @return - this hunt's title.
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Returns this hunt's description.
     * @return - this hunt's description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Returns this hunt's parse.com database id.
     * @return - this hunt's parse.com database id.
     */
    public String getParseID() {
        return _parseID;
    }

    /**
     * Returns a LatLng object which represents the first point of this hunt.
     * @return - a LatLng object which represents the first point of this hunt.
     */
    public LatLng getCenterPosition() {
        return _firstPoint.toLatLng();
    }

    /**
     * Returns this hunt's radius.
     * @return - this hunt's radius.
     */
    public float getRadius() {
        return _radius;
    }

    /**
     * Returns this hun's total distance.
     * @return - this hun's total distance.
     */
    public Float getTotalDistance() {
        return _totalDistance;
    }

    /**
     * Returns this hunt's current rating.
     * @return - this hunt's current rating.
     */
    public float getRating() {
        if (_numOfRaters != 0) {
            return _totalRating / _numOfRaters;
        }
        return 0;
    }

    /**
     * Returns this hunt's comment list.
     * @return - this hunt's comment list.
     */
    public ArrayList<Comment> getComments() {
        return _comments;
    }

    /**
     * Returns a {@link com.parse.ParseObject} representation of this hunt.
     * @param c - The application context this hunt exists in.
     * @return - a {@link com.parse.ParseObject} representation of this hunt.
     */
    public ParseObject toParseObject(Context c) {
        ParseObject remoteHunt = new ParseObject(PARSE_CLASS_NAME);

        remoteHunt.put(PARSE_TITLE_FIELD, _title);
        remoteHunt.put(PARSE_DESCRIPTION_FIELD, _description);
        remoteHunt.put(PARSE_CREATOR_ID_FIELD, _creatorID);
        remoteHunt.put(PARSE_FIRST_POINT_FIELD, _firstPoint.toParseGeoPoint());
        remoteHunt.put(PARSE_RADIUS_FIELD, _radius);
        remoteHunt.put(PARSE_TOTAL_DISTANCE_FIELD, _totalDistance);
        remoteHunt.put(PARSE_RATING_FIELD, _rating);
        remoteHunt.put(PARSE_TOTAL_RATING_FIELD, _totalRating);
        remoteHunt.put(PARSE_NUM_OF_RATERS_FIELD, _numOfRaters);

        ArrayList<ParseObject> remoteComments = new ArrayList<ParseObject>();

        for (Comment comment : _comments) {
            remoteComments.add(comment.toParseObject());
        }

        remoteHunt.put(PARSE_COMMENTS_FIELD, remoteComments);

        ArrayList<ParseObject> remoteHints = new ArrayList<ParseObject>();

        for (Hint hint : _hints) {
            remoteHints.add(hint.toParseObject(c));
        }

        remoteHunt.put(PARSE_HINTS_FIELD, remoteHints);

        return remoteHunt;

    }

}
