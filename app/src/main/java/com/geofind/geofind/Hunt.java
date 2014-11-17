package com.geofind.geofind;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseException;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mickey on 01/10/14.
 */
public class Hunt implements Serializable {

    public static final float METERS_TO_MILES = 0.000621371f;
    public static final float METERS_TO_KILOMETERS = 0.001f;
    public static final int DIGIT_PRECISION = 3;

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


    private String _title;
    private String _description;
    private String _creatorID;
    private String _parseID;
    private Point _firstPoint;
    private float _radius;
    private float _totalDistance;
    private float _rating;
    private float _totalRating;
    private int _numOfRaters;
    private ArrayList<Hint> _hints;
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

    public String getTitle() {
        return _title;
    }

    public String getDescription() {
        return _description;
    }

    public String getCreator() {
        return _creatorID;
    }

    public String getParseID() {
        return _parseID;
    }

    public LatLng getCenterPosition() {
        return _firstPoint.toLatLng();
    }

    public float getRadius() {
        return _radius;
    }

    public Float getTotalDistance() {
        return _totalDistance;
    }

    public float getRating() {
        if (_numOfRaters != 0) {
            return _totalRating / _numOfRaters;
        }
        return 0;
    }

    public ArrayList<Comment> getComments() {
        return _comments;
    }

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
