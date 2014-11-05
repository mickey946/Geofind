package com.geofind.geofind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;


public class CommentListActivity extends ActionBarActivity {

    private CommentListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);

        // show the back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get a reference to recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an adapter and set it's data
        ArrayList<CommentDummy> comments = new ArrayList<CommentDummy>();
        comments.add(new CommentDummy("Mickey Mickey", "Fuck this", "Shit", new Date(), 2.f));
        comments.add(new CommentDummy("Mickey Mickey", "Fuck fdthis", "xcfd", new Date(), 4.f));
        comments.add(new CommentDummy("Mickey Mickey", "Fucdfk this", "dfds", new Date(), 3.f));
        comments.add(new CommentDummy("Mickey Mickey", "Fuddck this", "Sfdfdit", new Date(), 1.f));
        comments.add(new CommentDummy("Ilia Marin", "Fuck thsdis", "df", new Date(), 5.f));
        adapter = new CommentListAdapter(comments, this);

        // set adapter
        recyclerView.setAdapter(adapter);

        // set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        onBackPressed();
        return null;
    }

    @Override
    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
        super.onCreateSupportNavigateUpTaskStack(builder);
        onBackPressed();
    }

    // TODO remove this class
    public class CommentDummy {
        String userName;
        String title;
        String comment;
        Date date;
        float rating;

        private CommentDummy(String userName, String title, String comment, Date date, float rating) {
            this.userName = userName;
            this.title = title;
            this.comment = comment;
            this.date = date;
            this.rating = rating;
        }

        public String getUserName() {
            return userName;
        }

        public float getRating() {
            return rating;
        }

        public Date getDate() {
            return date;
        }

        public String getTitle() {
            return title;
        }

        public String getComment() {
            return comment;
        }
    }
}
