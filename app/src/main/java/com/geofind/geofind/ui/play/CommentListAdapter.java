package com.geofind.geofind.ui.play;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.geofind.geofind.R;
import com.geofind.geofind.structures.Comment;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.geofind.geofind.playutils.BaseGameActivity;
import com.geofind.geofind.playutils.GameHelper;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * An {@link android.support.v7.widget.RecyclerView.Adapter} for the
 * {@link com.geofind.geofind.ui.play.CommentListActivity}.
 *
 * Created by Mickey on 05/11/2014.
 */
public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private ArrayList<Comment> comments;
    private Context context;
    private GameHelper gameHelper;

    public CommentListAdapter(ArrayList<Comment> comments, Context context) {
        this.comments = comments;
        this.context = context;
        this.gameHelper = ((BaseGameActivity) context).getGameHelper();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_comment_list, viewGroup, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Comment comment = comments.get(i);
        viewHolder.commentTitle.setText(comment.getTitle());
        viewHolder.comment.setText(comment.getReview());
        viewHolder.ratingBar.setRating(comment.getRating());

        GoogleApiClient googleApiClient = gameHelper.getApiClient();
        String userId = comment.getCreatorID();
        if (userId != null) {
            Plus.PeopleApi.load(googleApiClient, userId)
                    .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                        @Override
                        public void onResult(People.LoadPeopleResult loadPeopleResult) {
                            if (loadPeopleResult.getStatus().getStatusCode() ==
                                    CommonStatusCodes.SUCCESS) {
                                PersonBuffer persons = loadPeopleResult.getPersonBuffer();
                                Person currentPerson = persons.get(0);
                                viewHolder.userName.setText(currentPerson.getDisplayName());

                                if (currentPerson.hasImage()) {
                                    DownloadImageTask downloadImageTask = new
                                            DownloadImageTask(viewHolder.userImage);
                                    downloadImageTask.execute(currentPerson.getImage().getUrl());
                                }

                                // show the comment and hide the progress bar
                                viewHolder.commentView.setVisibility(View.VISIBLE);
                                viewHolder.progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        } else {
            // should not happen
            Log.wtf(getClass().getName(), "Comment creator id is null");
        }

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
    }

    /**
     * An {@link android.os.AsyncTask} used to download a user's Google+ profile image .
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView userImage;

        private DownloadImageTask(ImageView userImage) {
            this.userImage = userImage;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap outputBitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                outputBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error loading an image: ", e.getMessage());
                e.printStackTrace();
            }
            return outputBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            userImage.setImageBitmap(result);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * A {@link android.support.v7.widget.RecyclerView.ViewHolder} that holds the view of a comment.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * The {@link android.view.View} that holds the comment.
         */
        public View commentView;

        /**
         * The {@link android.widget.TextView} that shows the user display name.
         */
        public TextView userName;

        /**
         * The {@link android.widget.TextView} that shows the comment title.
         */
        public TextView commentTitle;

        /**
         * The {@link android.widget.TextView} that shows the comment.
         */
        public TextView comment;

        /**
         * The {@link android.widget.TextView} that shows the comment date.
         */
        public TextView date;

        /**
         * The {@link android.widget.ImageView} that shows the user profile picture.
         */
        public ImageView userImage;

        /**
         * The {@link android.widget.RatingBar} that shows the rating of the comment.
         */
        public RatingBar ratingBar;

        /**
         * The {@link android.widget.ProgressBar} that is shown until the comment is loaded.
         */
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            commentView = itemView.findViewById(R.id.item_comment_list_view);
            userName = (TextView) itemView.findViewById(R.id.item_comment_list_user_name);
            commentTitle = (TextView) itemView.findViewById(R.id.item_comment_list_title);
            comment = (TextView) itemView.findViewById(R.id.item_comment_list_comment);
            date = (TextView) itemView.findViewById(R.id.item_comment_list_date);
            userImage = (ImageView) itemView.findViewById(R.id.item_comment_list_user_image);
            ratingBar = (RatingBar) itemView.findViewById(R.id.item_comment_list_rating);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }
}
