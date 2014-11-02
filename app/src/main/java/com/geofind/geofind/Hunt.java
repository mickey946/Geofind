package com.geofind.geofind;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mickey on 01/10/14.
 */
public class Hunt implements Serializable {

    public static final float METERS_TO_MILES = 0.000621371f;
    public static final float METERS_TO_KILOMETERS = 0.001f;
    public static final int DIGIT_PRECISION = 3;

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
        //TODO Calculate radius - Ilia?
        _radius = 50;
        //TODO Calculate totalDistance - ilia?
        _totalDistance = 15;
        _rating = 0;
        _totalRating = 0;
        _numOfRaters = 0;
        _hints = hints;
        _comments = new ArrayList<Comment>();
    }

    public Hunt(ParseObject remoteHunt) {
        _title = remoteHunt.getString("title");
        _description = remoteHunt.getString("description");
        _creatorID = remoteHunt.getString("creatorID");
        _parseID = remoteHunt.getObjectId();
        _firstPoint = new Point(remoteHunt.getParseGeoPoint("firstPoint"));
        _radius = (float) remoteHunt.getDouble("radius");
        _totalDistance = (float) remoteHunt.getDouble("totalDistance");
        _totalRating = (float) remoteHunt.getDouble("totalRating");


        //TODO need to figure out how to retrieve hints on the fly.
        /*_hints = new ArrayList<Hint>();

        ArrayList<ParseObject> remoteHints = (ArrayList<ParseObject>) remoteHunt.get("hints");
        for (ParseObject remoteHint : remoteHints) {
            _hints.add(new Hint(remoteHint));
        }

        ArrayList<ParseObject> remoteComments = (ArrayList<ParseObject>) remoteHunt.get("comments");
        for (ParseObject remoteComment : remoteComments) {
            _comments.add(new Comment(remoteComment));
        }*/
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
            _rating = _totalRating / _numOfRaters;
        }
        return _rating;
    }

    public float getTotalRating() {
        return _totalRating;
    }

    public int getNumOfRaters() {
        return _numOfRaters;
    }

    public ArrayList<Hint> getHints() {
        return _hints;
    }

    public ParseObject toParseObject() {
        ParseObject remoteHunt = new ParseObject("Hunt");

        remoteHunt.put("title", _title);
        remoteHunt.put("description", _description);
        remoteHunt.put("creatorID", _creatorID);
        remoteHunt.put("firstPoint", _firstPoint.toParseGeoPoint());
        remoteHunt.put("radius", _radius);
        remoteHunt.put("totalDistance", _totalDistance);
        remoteHunt.put("rating", _rating);
        remoteHunt.put("totalRating", _totalDistance);
        remoteHunt.put("numOfRaters", _numOfRaters);

        ArrayList<ParseObject> remoteComments = new ArrayList<ParseObject>();

        for (Comment comment : _comments) {
            remoteComments.add(comment.toParseObject());
        }

        remoteHunt.put("comments", remoteComments);

        ArrayList<ParseObject> remoteHints = new ArrayList<ParseObject>();

        for (Hint hint : _hints) {
            remoteHints.add(hint.toParseObject());
        }

        remoteHunt.put("hints", remoteHints);

        return remoteHunt;
    }

}
