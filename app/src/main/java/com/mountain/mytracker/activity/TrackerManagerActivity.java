package com.mountain.mytracker.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mountain.mytracker.Track.UserTrack;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.TrackListAdapter;
import com.mountain.mytracker.other.GPXExport;
import com.mountain.mytracker.other.NameDialog;
import com.mountain.mytracker.other.UserTrackBackup;

import java.io.File;
import java.util.ArrayList;

/**
 * Aici o sa fie lista cu traseele inregistrate de utilizator
 */

public class TrackerManagerActivity extends ListActivity implements NameDialog.NoticeDialogListener {

	private DatabaseHelper db;
	private Cursor c;
	private String table;
	Intent mTrackLoggerActivity;

    //used for restoring backup
    private UserTrackBackup mUserTrackBackup;
    private ArrayList<UserTrack> mUserTrackList;
	
	@Override
	public void onDialogPositiveClick(String title, Integer trackId){
		String currentTrackName;
        UserTrack mUserTrack;

		currentTrackName = title;
        mUserTrack = new UserTrack(trackId, this.getApplicationContext());
        mUserTrack.setTrackName(currentTrackName);
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
                break;
            }

            case R.id.trackmgr_menu_delete_all: {
                int trackCount;
                String trackId;
                trackCount = this.getListView().getCount();
                for(int i=0; i<trackCount; i++){
                    Cursor trackCursor = (Cursor) this.getListView().getItemAtPosition(i);
                    trackId = trackCursor.getString(trackCursor.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
                    deleteTrack(trackId);
                }
                updateList();
                break;
            }

            case R.id.trackmgr_menu_backup: {

                UserTrackBackup mUserTrackBackup;
                ArrayList<UserTrack> mUserTrackList;
                UserTrack mUserTrack;
                Integer trackId;

                mUserTrackList = new ArrayList<>();

                int trackCount = this.getListView().getCount();
                for(int i=0; i<trackCount; i++){
                    Cursor trackCursor = (Cursor) this.getListView().getItemAtPosition(i);
                    trackId = trackCursor.getInt(trackCursor.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
                    mUserTrack = new UserTrack(trackId, this.getApplicationContext());
                    mUserTrackList.add(mUserTrack);
                }

                mUserTrackBackup = new UserTrackBackup(mUserTrackList);
                mUserTrackBackup.backUpList();

                Toast.makeText(this.getApplicationContext(), "Backup successful", Toast.LENGTH_LONG).show();

                break;
            }

            case R.id.trackmgr_menu_restore_backup: {

                //instantiate objects needed for restoring backup
                mUserTrackList = new ArrayList<>();
                mUserTrackBackup = new UserTrackBackup(mUserTrackList);

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 42);
                break;
            }

		}
		return true;
	}

    //callback for file picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData){

        if(requestCode == 42 && resultCode == Activity.RESULT_OK){
            Uri uri;
            if(resultData != null){
                uri = resultData.getData();
                restoreBackup(uri);
            }
        }
    }

    private void restoreBackup(Uri uri){

        Cursor c = getContentResolver().query(uri, null, null, null, null, null);
        File f;

        try{
            if(c != null && c.moveToFirst()){
                String displayName = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                f = new File(displayName);
            }
            c.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
	
	@Override
	public void onListItemClick(ListView lv, View v, final int position, final long id){
		int currentTrackId;
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
        String trackName;
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        c.moveToFirst();
        c.moveToPosition(info.position);
        trackId = c.getInt(c.getColumnIndex(DatabaseEntry.COL_TRACK_NO));
        trackName = c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_NAME));

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

            case R.id.contextmenu_export_track : {
                GPXExport gpx = new GPXExport(this.getApplicationContext());
                gpx.createFile(trackId, trackName + ".gpx", trackName);
                Toast.makeText(this.getApplicationContext(), "Export successful",
                        Toast.LENGTH_LONG).show();
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
