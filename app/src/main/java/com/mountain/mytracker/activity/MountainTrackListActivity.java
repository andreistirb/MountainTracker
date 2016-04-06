package com.mountain.mytracker.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.MountainTrackListAdapter;
import com.mountain.mytracker.db.NewDatabaseHelper;

public class MountainTrackListActivity extends ListActivity {

	private String selection;
	private String table;
	private String sortOrder;
	private String[] selectionArgs;
    NewDatabaseHelper db;
	Cursor c;
	
	@Override
	public void onCreate(Bundle savedInstanceState){

		String munte, munte_id;

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.mountain_track_list_layout);
		
		//seteaza titlul -> numele grupei de munti
		munte = this.getIntent().getExtras().getString(DatabaseEntry.COL_MOUNTAIN_NAME);
		this.setTitle(munte);
		munte_id = this.getIntent().getExtras().getString(DatabaseEntry.COL_MOUNTAIN_ID);
		
		//cauta id-ul muntelui 
		db = new NewDatabaseHelper(this);
		
		//cauta traseele cu id-ul muntelui == mountain_id
		selection = DatabaseEntry.COL_MOUNTAIN_ID + " = ? ";
		selectionArgs = new String[] { munte_id };
		table = DatabaseEntry.TABLE_MOUNTAIN_TRACK;
		sortOrder = DatabaseEntry.COL_TRACK_NAME;
		
		c = db.myQuery(table, null, selection, selectionArgs, null, null, sortOrder);
		
		this.setListAdapter(new MountainTrackListAdapter(MountainTrackListActivity.this,c));
		this.registerForContextMenu(this.getListView());
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.mountain_track_list_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.mountain_track_list_menu_sort_name:{
			sortOrder = DatabaseEntry.COL_TRACK_NAME;
			break;
		}
		case R.id.mountain_track_list_menu_sort_dur:{
			sortOrder = DatabaseEntry.COL_LENGTH;
			break;
		}
		case R.id.mountain_track_list_menu_sort_diff:{
			sortOrder = DatabaseEntry.COL_DIFF;
		}		
		}
		c = db.myQuery(table, null, selection, selectionArgs, null, null, sortOrder);
		this.setListAdapter(new MountainTrackListAdapter(MountainTrackListActivity.this,c));
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo ){
		super.onCreateContextMenu(menu, v, menuInfo);
		this.getMenuInflater().inflate(R.menu.mountain_track_list_contextmenu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		c.moveToFirst();
		c.moveToPosition(info.position);

		switch (item.getItemId()) {
		case R.id.mountain_track_list_contextmenu_show: {
			Intent i = new Intent(this, MapViewActivity.class);
			i.putExtra("factoryTrackId", c.getInt(c.getColumnIndex(DatabaseEntry.COL_TRACK_ID)));
			this.startActivity(i);
			break;
		}
		case R.id.mountain_track_list_contextmenu_details: {
			Intent i = new Intent(this, TrackDetailsActivity.class);
			i.putExtra("factoryTrackId", c.getInt(c.getColumnIndex(DatabaseEntry.COL_TRACK_ID)));
			this.startActivity(i);
			break;
		}
		case R.id.mountain_track_list_contextmenu_try:{
			Intent i = new Intent(this, TrackLoggerActivity.class);
			i.putExtra("factoryTrackId", c.getInt(c.getColumnIndex(DatabaseEntry.COL_TRACK_ID)));
			this.startActivity(i);
		}
		}
		return true;
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, final int position, final long id){
		Intent i = new Intent(this, TrackLoggerActivity.class);
		Cursor c = (Cursor) lv.getItemAtPosition(position);
		i.putExtra("factoryTrackId", c.getInt(c.getColumnIndex(DatabaseEntry.COL_TRACK_ID)));
		this.startActivity(i);
	}
}
