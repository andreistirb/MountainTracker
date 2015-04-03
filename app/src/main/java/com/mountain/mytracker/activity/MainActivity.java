package com.mountain.mytracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {
	

	Button mountain_list;
	Button about_btn, my_tracks;

    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;


    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_layout);

        int i;
        //check if Google Play services are available
        if ((i = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) != ConnectionResult.SUCCESS){
            GooglePlayServicesUtil.getErrorDialog(i, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
        }
		
		mountain_list = (Button) this.findViewById(R.id.main_mountain_btn);		
		mountain_list.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(),MountainListActivity.class);
				startActivity(i);				
			}
		});
		about_btn = (Button) this.findViewById(R.id.main_about_btn);
		about_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://cdascalu.com/projects/mountaintracker/index.html"));
                    startActivity(myWebLink);
			}
		});
		
		my_tracks = (Button) this.findViewById(R.id.main_tracks_btn);
		my_tracks.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), TrackerManagerActivity.class);
				startActivity(i);
				
			}
		});


	}
}
