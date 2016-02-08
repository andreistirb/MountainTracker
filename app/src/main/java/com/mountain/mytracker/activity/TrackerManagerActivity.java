package com.mountain.mytracker.activity;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.TrackListAdapter;
import com.mountain.mytracker.other.NameDialog;

/**
 * Aici o sa fie lista cu traseele inregistrate de utilizator
 */

public class TrackerManagerActivity extends ListActivity implements NameDialog.NoticeDialogListener {

	private int currentTrackId = 0;
	private String currentTrackName;
	private static final long TRACK_ID_NO_TRACK = -1;
	private DatabaseHelper db;
	private Cursor c;
	private String table, selection, sortOrder;
	private String[] selectionArgs;
	Intent mTrackLoggerActivity;
	
	@Override
	public void onDialogPositiveClick(String titlu){		
		currentTrackName = titlu;
		mTrackLoggerActivity.putExtra("track_name", titlu);
		Log.v("dupa dialog", currentTrackName);
		this.startActivity(mTrackLoggerActivity);
		
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tracker_manager);
		this.setTitle(getString(R.string.activity_tracker_manager));

		db = new DatabaseHelper(this);

		table = DatabaseEntry.TABLE_MY_TRACKS;
		
		this.mTrackLoggerActivity = new Intent(this,TrackLoggerActivity.class);
		mTrackLoggerActivity.putExtra("detalii", true);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		c = db.getReadableDatabase().query(table, null, null, null, null, null, null);
		c.moveToFirst();
		this.setListAdapter(new TrackListAdapter(TrackerManagerActivity.this,c,1));
		this.registerForContextMenu(this.getListView());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.getMenuInflater().inflate(R.menu.track_manager_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		
		case R.id.trackmgr_menu_newtrack: {
			DialogFragment dialog = new NameDialog();
			dialog.show(getFragmentManager(), "dialog");
		}
		
		}
		return true;
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, final int position, final long id){
		
		Intent i;
		String mTrackNo;
		i = new Intent(this, MyTrackDetailsActivity.class);
		Cursor traseu = (Cursor) lv.getItemAtPosition(position);
		currentTrackId = traseu.getInt(traseu.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
		mTrackNo = traseu.getString(traseu.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
		i.putExtra(DatabaseEntry.COL_TRACK_NO, mTrackNo);
		i.putExtra("track_id",currentTrackId);
		this.startActivity(i);
	}

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.track_manager_contextmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        c.moveToFirst();
        c.moveToPosition(info.position);

        switch(item.getItemId()){
            case R.id.contextmenu_delete_track : {
                deleteTrack(c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_NO)));
                Log.i("delete row","deleting row");
                //TO-DO update the list from database
                break;
            }
        }
        return true;
    }

    private void deleteTrack(String track_id){
        db.getWritableDatabase().delete(DatabaseEntry.TABLE_MY_TRACKS,DatabaseEntry.COL_TRACK_NO + " = " + track_id, null);
    }

}
