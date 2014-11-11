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

    public final String HUNT_CLASS_NAME_PARSE = "Hunt";


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
        _title = remoteHunt.getString("title");
        _description = remoteHunt.getString("description");
        _creatorID = remoteHunt.getString("creatorID");
        _parseID = remoteHunt.getObjectId();
        _firstPoint = new Point(remoteHunt.getParseGeoPoint("firstPoint"));
        _radius = (float) remoteHunt.getDouble("radius");
        _totalDistance = (float) remoteHunt.getDouble("totalDistance");
        _totalRating = (float) remoteHunt.getDouble("totalRating");
        _numOfRaters = remoteHunt.getInt("numOfRaters");
        _hints = new ArrayList<Hint>();
        _comments = new ArrayList<Comment>();


        final List<ParseObject> remoteComments = remoteHunt.getList("comments");
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
            return _totalRating / _numOfRaters;
        }
        return 0;
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

    public void addHint(Hint newHint) {
        _hints.add(newHint);
    }

    public ParseObject toParseObject(Context c) {
        ParseObject remoteHunt = new ParseObject("Hunt");

        remoteHunt.put("title", _title);
        remoteHunt.put("description", _description);
        remoteHunt.put("creatorID", _creatorID);
        remoteHunt.put("firstPoint", _firstPoint.toParseGeoPoint());
        remoteHunt.put("radius", _radius);
        remoteHunt.put("totalDistance", _totalDistance);
        remoteHunt.put("rating", _rating);
        remoteHunt.put("totalRating", _totalRating);
        remoteHunt.put("numOfRaters", _numOfRaters);

        ArrayList<ParseObject> remoteComments = new ArrayList<ParseObject>();

        for (Comment comment : _comments) {
            remoteComments.add(comment.toParseObject());
        }

        remoteHunt.put("comments", remoteComments);

        ArrayList<ParseObject> remoteHints = new ArrayList<ParseObject>();

        //set first hint to solved
        _hints.get(0).setState(Hint.State.SOLVED);

        for (Hint hint : _hints) {
            remoteHints.add(hint.toParseObject(c));
        }

        remoteHunt.put("hints", remoteHints);

        return remoteHunt;

    }

    public ArrayList<Comment> getComments() {
        return _comments;
    }
}
