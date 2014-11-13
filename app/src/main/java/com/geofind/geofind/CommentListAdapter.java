package com.geofind.geofind;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Mickey on 05/11/2014.
 */
public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    // TODO change this to Comment
    private ArrayList<Comment> comments;
    private Context context;

    public CommentListAdapter(ArrayList<Comment> comments,
                              Context context) {
        this.comments = comments;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_comment_list, viewGroup, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Comment comment = comments.get(i);
        viewHolder.commentTitle.setText(comment.getTitle());
        viewHolder.comment.setText(comment.getReview());
        viewHolder.ratingBar.setRating(comment.getRating());

        Log.d("isConnected: ", UserData.getGoogleApiClient().isConnected() + "");

        Plus.PeopleApi.load(UserData.getGoogleApiClient(), UserData.getId())
                .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(People.LoadPeopleResult loadPeopleResult) {
                        PersonBuffer persons = loadPeopleResult.getPersonBuffer();
                        Log.d("got person ", persons.get(0).getDisplayName());
                    }
                });

        // get user's preferred date format
        Format format = android.text.format.DateFormat.getDateFormat(context);
        String formatString = ((SimpleDateFormat) format).toLocalizedPattern();
        DateFormat dateFormat;
        if (TextUtils.isEmpty(formatString)) {
            dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        } else {
            dateFormat = new SimpleDateFormat(formatString);
        }

        viewHolder.date.setText(dateFormat.format(comment.getDateCreated()));

        // TODO get user image
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView commentTitle;
        public TextView comment;
        public TextView date;
        public ImageView userImage;
        public RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.item_comment_list_user_name);
            commentTitle = (TextView) itemView.findViewById(R.id.item_comment_list_title);
            comment = (TextView) itemView.findViewById(R.id.item_comment_list_comment);
            date = (TextView) itemView.findViewById(R.id.item_comment_list_date);
            userImage = (ImageView) itemView.findViewById(R.id.item_comment_list_user_image);
            ratingBar = (RatingBar) itemView.findViewById(R.id.item_comment_list_rating);
        }
    }
}
