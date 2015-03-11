package com.mountain.mytracker.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.NewDatabaseHelper;

public class TrackDetailsActivity extends Activity {
	
	private NewDatabaseHelper db;
	private String traseu;
	private String traseu_id;
	private TextView track_details_duration;
	private TextView track_details_difficulty;
	private TextView track_details_mark;
	private TextView track_details_description;
	private TextView track_details_availability;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.track_details_layout);
		
		traseu = this.getIntent().getExtras().getString("track_name");
		this.setTitle(traseu);
		traseu_id = this.getIntent().getExtras().getString("track_id");
		
		//baza de date
		db = new NewDatabaseHelper(this);
		
		String selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
		String[] selectionArgs = new String[] { traseu_id };
		String table = DatabaseEntry.TABLE_MOUNTAIN_TRACK;
		
		Cursor c = db.myQuery(table, null, selection, selectionArgs, null, null, null);
		
		//seteaza TextView-urile
		
		track_details_duration = (TextView) this.findViewById(R.id.track_details_duration);
		track_details_difficulty = (TextView) this.findViewById(R.id.track_details_difficulty);
		track_details_mark = (TextView) this.findViewById(R.id.track_details_mark);
		track_details_description = (TextView) this.findViewById(R.id.track_details_description);
		track_details_availability = (TextView) this.findViewById(R.id.track_details_availability);
		
		track_details_duration.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_LENGTH)));
		track_details_difficulty.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_DIFF)));
		track_details_mark.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_MRK)));
		track_details_description.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_DESCRIPTION)));
		track_details_availability.setText(c.getString(c.getColumnIndex(DatabaseEntry.COL_AVLB)));
		
	}

}
