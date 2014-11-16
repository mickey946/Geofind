package com.geofind.geofind;

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
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
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

        // TODO get the userID from the comment
        GoogleApiClient googleApiClient = gameHelper.getApiClient();
        String userId = Plus.PeopleApi.getCurrentPerson(googleApiClient).getId();
        Plus.PeopleApi.load(googleApiClient, userId)
                .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(People.LoadPeopleResult loadPeopleResult) {
                        PersonBuffer persons = loadPeopleResult.getPersonBuffer();
                        Person currentPerson = persons.get(0);
                        viewHolder.userName.setText(currentPerson.getDisplayName());

                        DownloadImageTask downloadImageTask = new
                                DownloadImageTask(viewHolder.userImage);
                        downloadImageTask.execute(currentPerson.getImage().getUrl());
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

    /**
     * An {@link android.os.AsyncTask} used to download a user's Google+ profile image .
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView userImage;

        private DownloadImageTask(ImageView userImage) {
            this.userImage = userImage;
        }

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

        protected void onPostExecute(Bitmap result) {
            userImage.setImageBitmap(result);
        }
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
