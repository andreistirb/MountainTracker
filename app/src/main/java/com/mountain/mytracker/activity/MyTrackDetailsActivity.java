package com.mountain.mytracker.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.other.GPXExport;

public class MyTrackDetailsActivity extends Activity {
	
	private Integer mTrackNo;
	private DatabaseHelper db;
	private TextView duration, distance, avg_speed, max_speed, min_alt, max_alt;
	Button export_btn;
	private String track_name;
	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.mytrackdetails_layout);
		
		duration = (TextView) this.findViewById(R.id.mytrackdetails_duration);
		distance = (TextView) this.findViewById(R.id.mytrackdetails_distance);
		avg_speed = (TextView) this.findViewById(R.id.mytrackdetails_avg_speed);
		max_speed = (TextView) this.findViewById(R.id.mytrackdetails_max_speed);
		min_alt = (TextView) this.findViewById(R.id.mytrackdetails_min_alt);
		max_alt = (TextView) this.findViewById(R.id.mytrackdetails_max_alt);
		export_btn = (Button) this.findViewById(R.id.mytrackdetails_export);
		
		
		if(this.getIntent().hasExtra(DatabaseEntry.COL_TRACK_NO)){
			//mTrackNo = this.getIntent().getExtras().getInt(DatabaseEntry.COL_TRACK_NO);
			mTrackNo = this.getIntent().getExtras().getInt("track_id");
			Log.v("in detalii", mTrackNo.toString());
		}
		
		db = new DatabaseHelper(this);
		
		String selection = DatabaseEntry.COL_TRACK_NO + "=? ";
		String[] selectionArgs = new String[] {"9"};
		String table = DatabaseEntry.TABLE_MY_TRACKS;
		
		Cursor c = db.getReadableDatabase().query(table, null, null, null, null, null, null);
		c.moveToFirst();
		
		
		if(c.move(mTrackNo-1)){
			
			Integer durationi = c.getInt(c.getColumnIndex(DatabaseEntry.COL_TIME));
			
			
			duration.setText(String.format("%d:%02d:%02d", durationi/3600, (durationi%3600)/60, (durationi%60)));
			distance.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_DISTANCE)));
			avg_speed.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_MED_SPEED)));
			max_speed.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_MAX_SPEED)));
			min_alt.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_MIN_ALT)));
			max_alt.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_MAX_ALT)));
			
			Log.v("in mytrackdetails", c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_NO)));
			
			track_name = c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_NAME));
			
			
			export_btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					GPXExport gpx = new GPXExport();
					gpx.createFile(mTrackNo, mTrackNo.toString() + "track.gpx" , track_name, db);
					Toast.makeText(getApplicationContext(), "Export successful",
							   Toast.LENGTH_LONG).show();
				}
			});
		}		
	}

}
