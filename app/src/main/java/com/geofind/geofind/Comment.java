package com.geofind.geofind;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by tzafrirharazy on 28/10/14.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {

    public Comment() {
    }

    public void initialize(String title, String review, Float rating, String creatorID) {
        put("title", title);
        put("review", review);
        put("rating", rating);
        put("creatorID", creatorID);
    }

    public String getTitle() {
        return getString("title");
    }

    public String getReview() {
        return getString("review");
    }

    public Float getRating() {
        return (Float) get("rating");
    }

    public String getCreatorID() {
        return getString("creatorID");
    }

    //TODO add the date this comment was created
    //TODO check if parse build in field "cretedAt" is enough
}
