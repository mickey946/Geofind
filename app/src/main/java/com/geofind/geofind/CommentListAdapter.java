package com.geofind.geofind;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Mickey on 05/11/2014.
 */
public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    // TODO change this to Comment
    private ArrayList<CommentListActivity.CommentDummy> comments;
    private Context context;

    public CommentListAdapter(ArrayList<CommentListActivity.CommentDummy> comments,
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
        CommentListActivity.CommentDummy comment = comments.get(i);
        viewHolder.userName.setText(comment.getUserName());
        viewHolder.commentTitle.setText(comment.getTitle());
        viewHolder.comment.setText(comment.getComment());
        viewHolder.ratingBar.setRating(comment.rating);

        // get user's preferred date format
        Format format = android.text.format.DateFormat.getDateFormat(context);
        String formatString = ((SimpleDateFormat) format).toLocalizedPattern();
        DateFormat dateFormat;
        if (TextUtils.isEmpty(formatString)) {
            dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        } else {
            dateFormat = new SimpleDateFormat(formatString);
        }

        viewHolder.date.setText(dateFormat.format(comment.date));

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
