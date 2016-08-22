package com.mountain.mytracker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mountain.mytracker.Track.FactoryTrack;

public class TrackDetailsActivity extends Activity {

    private FactoryTrack factoryTrack;
    private DatabaseReference mFirebaseDatabaseReference;
    private Query mQuery;
    private Integer factoryTrackId;

    private static final String TRACK_CHILD = "tracks";

	public void onCreate(Bundle savedInstanceState){

		TextView track_details_duration, track_details_difficulty, track_details_mark,
				track_details_description, track_details_availability;

		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.track_details_layout);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


        ValueEventListener trackListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                factoryTrack = dataSnapshot.getValue(FactoryTrack.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("as", "not working");
            }
        };


        if(this.getIntent().hasExtra("factoryTrackId")) {
            factoryTrackId = this.getIntent().getExtras().getInt("factoryTrackId");
            //factoryTrack = new FactoryTrack(factoryTrackId, this.getApplicationContext());
            mQuery = mFirebaseDatabaseReference.child(TRACK_CHILD).child(factoryTrackId.toString());
            mQuery.addValueEventListener(trackListener);
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
