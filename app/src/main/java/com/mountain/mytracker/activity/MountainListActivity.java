package com.mountain.mytracker.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.*;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mountain.mytracker.Track.FactoryTrackPoint;
import com.mountain.mytracker.Track.Mountain;
import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.MountainListAdapter;

import org.osmdroid.util.GeoPoint;

import java.util.Map;

public class MountainListActivity extends ListActivity {

    private static final String TAG = "MountainListActivity";
    public static final String MOUNTAIN_CHILD = "mountains";

    //Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseListAdapter<Mountain> mFireBaseListAdapter;

    private ProgressBar mProgressBar;

    private ConnectivityManager mConnectivityManager;
    private boolean isConnected;

    private MountainListAdapter mMountainListAdapter;
    private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.mountain_list_layout);

        mContext = this.getApplicationContext();

        mProgressBar = (ProgressBar) findViewById(R.id.mountain_list_progressBar);

        mConnectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        isConnected = networkInfo != null &&
                networkInfo.isConnectedOrConnecting();

        if(isConnected){
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

            mFirebaseDatabaseReference.child(MOUNTAIN_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Mountain mountain;
                    Mountain.deleteAllDatabase(mContext);
                    for(DataSnapshot i : dataSnapshot.getChildren()) {
                        ObjectMapper mapper = new ObjectMapper();
                        GenericTypeIndicator<Map<String, Object>> indicator = new GenericTypeIndicator<Map<String, Object>>() {};
                        mountain = mapper.convertValue(i.getValue(indicator), Mountain.class);
                        mountain.setmDatabaseHelper(new DatabaseHelper(mContext));
                        try {
                            //here check if entry already exists, if yes only update
                            mountain.toDatabase();
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{
            DatabaseHelper databaseHelper = new DatabaseHelper(this.getApplicationContext());
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            Cursor c;
            qb.setTables(DatabaseContract.DatabaseEntry.TABLE_MOUNTAIN);

            if(databaseHelper != null){
                c = qb.query(databaseHelper.getReadableDatabase(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME);
                this.setListAdapter(new MountainListAdapter(MountainListActivity.this, c));
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        mConnectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        isConnected = networkInfo != null &&
                networkInfo.isConnectedOrConnecting();
    }

	@Override
	protected void onListItemClick(ListView lv, View v, final int position,
                                   final long id) {
		Intent i;
		String mountainName;
        Integer mountainId;

		i = new Intent(this, MountainTrackListActivity.class);

        if(isConnected) {
            mountainId = Integer.getInteger(this.mFireBaseListAdapter.getItem(position).getId());
            mountainName = this.mFireBaseListAdapter.getItem(position).getName();
        }
        else{
            Cursor mountainCursor = (Cursor) lv.getItemAtPosition(position);
            mountainId = mountainCursor.getInt(mountainCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID));
            mountainName = mountainCursor.getString(mountainCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME));
        }

        i.putExtra(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME, mountainName);
		i.putExtra(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID, mountainId);
        Log.i(TAG, mountainId.toString());
        this.startActivity(i);
	}
}