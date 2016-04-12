package com.mountain.mytracker.activity;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mountain.mytracker.Track.UserTrack;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.TrackListAdapter;
import com.mountain.mytracker.other.NameDialog;

/**
 * Aici o sa fie lista cu traseele inregistrate de utilizator
 */

public class TrackerManagerActivity extends ListActivity implements NameDialog.NoticeDialogListener {

	private static final long TRACK_ID_NO_TRACK = -1;
	private DatabaseHelper db;
	private Cursor c;
	private String table;
	Intent mTrackLoggerActivity;
	
	@Override
	public void onDialogPositiveClick(String title, Integer trackId){
		String currentTrackName;
        UserTrack mUserTrack;

		currentTrackName = title;
        mUserTrack = new UserTrack(trackId, this.getApplicationContext());
        mUserTrack.setName(currentTrackName);
        mUserTrack.updateDatabaseName(currentTrackName);

		updateList();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tracker_manager);
		this.setTitle(getString(R.string.activity_tracker_manager));

		db = new DatabaseHelper(this.getApplicationContext());

		table = DatabaseEntry.TABLE_MY_TRACKS;
		
		this.mTrackLoggerActivity = new Intent(this,TrackLoggerActivity.class);
		mTrackLoggerActivity.putExtra("detalii", true);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		updateList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.getMenuInflater().inflate(R.menu.track_manager_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		
		case R.id.trackmgr_menu_newtrack: {
            this.startActivity(mTrackLoggerActivity);
		}

		}
		return true;
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, final int position, final long id){
		int currentTrackId = 0;
		Intent i;
		String mTrackNo;
		i = new Intent(this, MyTrackDetailsActivity.class);
		Cursor traseu = (Cursor) lv.getItemAtPosition(position);
		currentTrackId = traseu.getInt(traseu.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
		mTrackNo = traseu.getString(traseu.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
		i.putExtra(DatabaseEntry.COL_TRACK_NO, mTrackNo);
		i.putExtra("userTrackId",currentTrackId);
		this.startActivity(i);
	}

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.track_manager_contextmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        Integer trackId;
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        c.moveToFirst();
        c.moveToPosition(info.position);
        trackId = c.getInt(c.getColumnIndex(DatabaseEntry.COL_TRACK_NO));

        switch(item.getItemId()){
            case R.id.contextmenu_delete_track : {
                deleteTrack(trackId.toString());
                updateList();
                break;
            }

            case R.id.contextmenu_rename_track : {
                Bundle fragmentArgs = new Bundle();
                fragmentArgs.putInt(DatabaseEntry.COL_TRACK_NO,trackId);

                DialogFragment dialog = new NameDialog();
                dialog.setArguments(fragmentArgs);
                dialog.show(getFragmentManager(), "dialog");
                break;
            }
        }
        return true;
    }

    private void deleteTrack(String track_id){
        db.getWritableDatabase().delete(DatabaseEntry.TABLE_MY_TRACKS, DatabaseEntry.COL_TRACK_NO + " = " + track_id, null);
    }

    private void updateList(){
        c = db.getReadableDatabase().query(table, null, null, null, null, null, null);
        c.moveToFirst();
        this.setListAdapter(new TrackListAdapter(this.getApplicationContext(),c));
        this.registerForContextMenu(this.getListView());
    }

}
