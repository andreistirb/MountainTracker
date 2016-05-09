package com.mountain.mytracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mountain.mytracker.other.NavigationDrawerItemClickListener;
import com.mountain.mytracker.other.SettingsActivity;

public class MainActivity extends Activity {

	private Button about_btn, my_tracks, mountain_list;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mNavigationDrawerItems;

    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_layout);

        mNavigationDrawerItems = getResources().getStringArray(R.array.navigation_drawer_items);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_list);

        //Set the adapter for the list view and list's click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.navigation_drawer_item, mNavigationDrawerItems));
        mDrawerList.setOnItemClickListener(new NavigationDrawerItemClickListener());


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

        Button settings_btn = (Button) this.findViewById(R.id.main_settings_btn);
        settings_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(i);
            }
        });


	}
}
