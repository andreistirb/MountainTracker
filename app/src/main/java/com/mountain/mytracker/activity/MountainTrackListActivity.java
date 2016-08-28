package com.mountain.mytracker.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.MountainTrackListAdapter;

public class MountainTrackListActivity extends ListActivity {

    private DatabaseReference mFireBaseDatabaseReference;
    private FirebaseListAdapter<FactoryTrack> mFirebaseListAdapter;
    public static final String TRACK_CHILD = "tracks";
    public static final String SORT_BY_NAME = "trackName";
    public static final String SORT_BY_DURATION = "trackDuration";
    public static final String SORT_BY_DIFFICULTY = "trackDifficulty";

    private ProgressBar mProgressBar;

    String mountainName;
    Integer mountainId;
	
	@Override
	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.mountain_track_list_layout);
		
		//seteaza titlul -> numele grupei de munti
		mountainName = this.getIntent().getExtras().getString(DatabaseEntry.COL_MOUNTAIN_NAME);
		this.setTitle(mountainName);

		mountainId = this.getIntent().getExtras().getInt(DatabaseEntry.COL_MOUNTAIN_ID);
        Log.w("MountainTrackListActivi",mountainId.toString());

        mFireBaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mProgressBar = (ProgressBar) this.findViewById(R.id.mountain_track_list_progressBar);

        mFirebaseListAdapter = new MountainTrackListAdapter(
                this,
                FactoryTrack.class,
                R.layout.mountain_track_list_item,
                mFireBaseDatabaseReference.child(TRACK_CHILD).child(mountainId.toString()),
                mProgressBar
        );

        this.setListAdapter(mFirebaseListAdapter);
		this.registerForContextMenu(this.getListView());
	}

    @Override
    public void onResume(){
        super.onResume();
        this.setListAdapter(mFirebaseListAdapter);
    }

    @Override
    public void onPause(){
        super.onPause();
    }
	
	public boolean onCreateOptionsMenu(Menu menu) {
		//this.getMenuInflater().inflate(R.menu.mountain_track_list_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
        String sortOrder;

		switch(item.getItemId()){
		case R.id.mountain_track_list_menu_sort_name:{
			sortOrder = SORT_BY_NAME;
			break;
		}
		case R.id.mountain_track_list_menu_sort_dur:{
			sortOrder = SORT_BY_DURATION;
			break;
		}
		case R.id.mountain_track_list_menu_sort_diff:{
			sortOrder = SORT_BY_DIFFICULTY;
            break;
		}
        default:
            sortOrder = SORT_BY_NAME;
		}


        mFirebaseListAdapter = new MountainTrackListAdapter(
                this,
                FactoryTrack.class,
                R.layout.mountain_track_list_item,
                mFireBaseDatabaseReference.child(TRACK_CHILD).equalTo(mountainId, "mountainId").orderByChild(sortOrder),
                mProgressBar);

		this.setListAdapter(mFirebaseListAdapter);
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo ){
		super.onCreateContextMenu(menu, v, menuInfo);
		this.getMenuInflater().inflate(R.menu.mountain_track_list_contextmenu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {

        Integer factoryTrackId;
        Intent i;

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

        factoryTrackId = mFirebaseListAdapter.getItem(info.position).getTrackId();

		switch (item.getItemId()) {
		case R.id.mountain_track_list_contextmenu_show: {
			i = new Intent(this, MapViewActivity.class);
            i.putExtra("factoryTrackId", factoryTrackId);
            this.startActivity(i);
			break;
		}
		case R.id.mountain_track_list_contextmenu_details: {
			i = new Intent(this, TrackDetailsActivity.class);
			i.putExtra("mountainId", mountainId);
            i.putExtra("factoryTrackId", factoryTrackId);
            this.startActivity(i);
			break;
		}
		case R.id.mountain_track_list_contextmenu_try:{
			i = new Intent(this, TrackLoggerActivity.class);
            i.putExtra("mountainId", mountainId);
            i.putExtra("factoryTrackId", factoryTrackId);
            this.startActivity(i);
            break;
		}
		}

		return true;
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, final int position, final long id){
        Integer factoryTrackId;

        Intent i = new Intent(this, TrackLoggerActivity.class);
        factoryTrackId = mFirebaseListAdapter.getItem(position).getTrackId();
        i.putExtra("factoryTrackId", factoryTrackId);
		this.startActivity(i);
	}
}
