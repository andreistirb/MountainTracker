package com.mountain.mytracker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.mountain.mytracker.Track.FactoryTrack;

public class TrackDetailsActivity extends Activity {

	private TextView track_details_duration;
	private TextView track_details_difficulty;
	private TextView track_details_mark;
	private TextView track_details_description;
	private TextView track_details_availability;
    private FactoryTrack factoryTrack;

	public void onCreate(Bundle savedInstanceState){

		TextView track_details_duration, track_details_difficulty, track_details_mark,
				track_details_description, track_details_availability;

		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.track_details_layout);

        if(this.getIntent().hasExtra("factoryTrackId")) {
            factoryTrack = new FactoryTrack(this.getIntent().getExtras().getInt("factoryTrackId"), this.getApplicationContext());
            setTitle(factoryTrack.getTrackName());
        }
		
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
