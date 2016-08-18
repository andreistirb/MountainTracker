package com.mountain.mytracker.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.MountainTrackListAdapter;
import com.mountain.mytracker.db.NewDatabaseHelper;

public class MountainTrackListActivity extends ListActivity {

    private DatabaseReference mFireBaseDatabaseReference;
    private FirebaseListAdapter<FactoryTrack> mFirebaseListAdapter;
    public static final String TRACK_CHILD = "tracks";
    public static final String SORT_BY_NAME = "trackName";
    public static final String SORT_BY_DURATION = "trackDuration";
    public static final String SORT_BY_DIFFICULTY = "trackDifficulty";

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

        mFireBaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseListAdapter = new FirebaseListAdapter<FactoryTrack>(
                this,
                FactoryTrack.class,
                R.layout.mountain_track_list_item,
                mFireBaseDatabaseReference.child(TRACK_CHILD).equalTo(mountainId, "mountainId")
        ) {
            @Override
            protected void populateView(View v, FactoryTrack model, int position) {
                ((TextView) v.findViewById(R.id.mountain_track_list_text)).setText(model.getTrackName());
                ((TextView) v.findViewById(R.id.mountain_track_list_diff)).setText(model.getTrackDifficulty());
                ((TextView) v.findViewById(R.id.mountain_track_list_length)).setText(model.getTrackLength());
                ((ImageView) v.findViewById(R.id.mountain_track_list_pic)).setImageResource(v.getResources()
                        .getIdentifier(model.getTrackMark(),
                        "drawable","com.mountain.mytracker.activity"));
            }
        };

        this.setListAdapter(mFirebaseListAdapter);
		this.registerForContextMenu(this.getListView());
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.mountain_track_list_menu, menu);
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

        mFirebaseListAdapter = new FirebaseListAdapter<FactoryTrack>(
                this,
                FactoryTrack.class,
                R.layout.mountain_track_list_item,
                mFireBaseDatabaseReference.child(TRACK_CHILD).equalTo(mountainId, "mountainId").orderByChild(sortOrder)
        ) {
            @Override
            protected void populateView(View v, FactoryTrack model, int position) {
                ((TextView) v.findViewById(R.id.mountain_track_list_text)).setText(model.getTrackName());
                ((TextView) v.findViewById(R.id.mountain_track_list_diff)).setText(model.getTrackDifficulty());
                ((TextView) v.findViewById(R.id.mountain_track_list_length)).setText(model.getTrackLength());
                ((ImageView) v.findViewById(R.id.mountain_track_list_pic)).setImageResource(v.getResources()
                        .getIdentifier(model.getTrackMark(),
                                "drawable","com.mountain.mytracker.activity"));
            }
        };

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
            i.putExtra("factoryTrackId", factoryTrackId);
            this.startActivity(i);
			break;
		}
		case R.id.mountain_track_list_contextmenu_try:{
			i = new Intent(this, TrackLoggerActivity.class);
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
