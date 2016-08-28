package com.mountain.mytracker.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.*;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mountain.mytracker.Track.Mountain;
import com.mountain.mytracker.db.DatabaseContract;

public class MountainListActivity extends ListActivity {

    private static final String TAG = "MountainListActivity";
    public static final String MOUNTAIN_CHILD = "mountains";

    //Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseListAdapter<Mountain> mFireBaseListAdapter;

    private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.mountain_list_layout);

        mProgressBar = (ProgressBar) findViewById(R.id.mountain_list_progressBar);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFireBaseListAdapter = new FirebaseListAdapter<Mountain>(
                this,
                Mountain.class,
                R.layout.mountain_list_item,
                mFirebaseDatabaseReference.child(MOUNTAIN_CHILD).orderByChild("name")
        ) {
            @Override
            protected void populateView(View v, Mountain model, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                ((TextView) v.findViewById(R.id.mountain_list_text)).setText(model.getName());
            }
        };

        this.setListAdapter(mFireBaseListAdapter);
    }

	@Override
	protected void onListItemClick(ListView lv, View v, final int position,
                                   final long id) {
		Intent i;
		String mountainName;
        Integer mountainId;
        Long mountainID;

		i = new Intent(this, MountainTrackListActivity.class);

        mountainId = this.mFireBaseListAdapter.getItem(position).getId();
        mountainName = this.mFireBaseListAdapter.getItem(position).getName();

        i.putExtra(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME, mountainName);
		i.putExtra(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID, mountainId);
        Log.i(TAG, mountainId.toString());
        this.startActivity(i);
	}
}