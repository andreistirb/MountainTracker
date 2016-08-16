package com.mountain.mytracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.crash.FirebaseCrash;
import com.mountain.mytracker.other.NavigationDrawerItemClickListener;

public class MainActivity extends Activity {

	private Button my_tracks, mountain_list, map_button;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mNavigationDrawerItems;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_layout);

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        mNavigationDrawerItems = getResources().getStringArray(R.array.navigation_drawer_items);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_list);

        //Set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.main_settings, false);

        //Set the adapter for the list view and list's click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.navigation_drawer_item, mNavigationDrawerItems));
        mDrawerList.setOnItemClickListener(new NavigationDrawerItemClickListener());

        //set the actionBarDrawerToggle so that user can open NavigationDrawer from appIcon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_launcher,
                R.string.navigation_drawer_open,
                R.string.app_name
        ){
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
                getActionBar().setTitle(R.string.app_name);
            }

            public void onDrawerOpened(View v){
                super.onDrawerOpened(v);
                getActionBar().setTitle(R.string.navigation_drawer_open);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


		mountain_list = (Button) this.findViewById(R.id.main_mountain_btn);		
		mountain_list.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(),MountainListActivity.class);
				startActivity(i);				
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* TO-DO see if this is a real thing and if it can boost somehow the app */
    /*@Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }*/

    /*@Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }*/
}
