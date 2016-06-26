package com.mountain.mytracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	Button about_btn, my_tracks, mountain_list, map_button;

    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_layout);

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
                myWebLink.setData(Uri.parse(getString(R.string.app_website)));//"http://cdascalu.com/projects/mountaintracker/index.html"));
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

		map_button = (Button) this.findViewById(R.id.main_map_button);
        map_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent i = new Intent(v.getContext(), UserLocationActivity.class);
                startActivity(i);
            }

        });


	}
}
