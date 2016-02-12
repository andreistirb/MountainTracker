package com.mountain.mytracker.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.NewDatabaseHelper;

public class TrackDetailsActivity extends Activity {

	//private NewDatabaseHelper db;
	//private String traseu;
	//private String traseu_id;
	private TextView track_details_duration;
	private TextView track_details_difficulty;
	private TextView track_details_mark;
	private TextView track_details_description;
	private TextView track_details_availability;
    private Track factoryTrack;

	public void onCreate(Bundle savedInstanceState){

		NewDatabaseHelper db;
		String traseu, traseu_id;
		TextView track_details_duration, track_details_difficulty, track_details_mark,
				track_details_description, track_details_availability;

		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.track_details_layout);
		
		//traseu = this.getIntent().getExtras().getString("track_name");
		//this.setTitle(traseu);
        if(this.getIntent().hasExtra("factoryTrackId")) {
            factoryTrack = new Track(this.getIntent().getExtras().getInt("factoryTrackId"), this.getApplicationContext());
            setTitle(factoryTrack.getTrackName());
            //traseu_id = this.getIntent().getExtras().getString("track_id");
        }

		//baza de date
		//db = new NewDatabaseHelper(this);
		
		//String selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
		//String[] selectionArgs = new String[] { traseu_id };
		//String table = DatabaseEntry.TABLE_MOUNTAIN_TRACK;
		
		//Cursor c = db.myQuery(table, null, selection, selectionArgs, null, null, null);
		
		//seteaza TextView-urile
		
		track_details_duration = (TextView) this.findViewById(R.id.track_details_duration);
		track_details_difficulty = (TextView) this.findViewById(R.id.track_details_difficulty);
		track_details_mark = (TextView) this.findViewById(R.id.track_details_mark);
		track_details_description = (TextView) this.findViewById(R.id.track_details_description);
		track_details_availability = (TextView) this.findViewById(R.id.track_details_availability);

        if(factoryTrack != null) {
            track_details_duration.setText(factoryTrack.getTrackLength());
            track_details_difficulty.setText(factoryTrack.getTrackDifficulty());
            track_details_mark.setText(factoryTrack.getTrackMark());
            track_details_description.setText(factoryTrack.getTrackDescription());
            track_details_availability.setText(factoryTrack.getTrackAvailability());
        }
		
	}

}
