package com.mountain.mytracker.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;

/**
 * adapter for the ListView in Tracker
 */

public class TrackListAdapter extends CursorAdapter { 

	public TrackListAdapter(Context context, Cursor cursor, int flags){
		super(context, cursor, flags);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup vg){
		View view = LayoutInflater.from(vg.getContext()).inflate(R.layout.tracker_manager_item, vg, false);
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		//TextView vId = (TextView) view.findViewById(R.id.tracker_manager_id);
		TextView vName = (TextView) view.findViewById(R.id.tracker_manager_name);
		//TextView vDetails = (TextView) view.findViewById(R.id.tracklist_item_details);
		
		//vId.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_TRACK_NO)));
		vName.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_TRACK_NAME)));
	//	vName.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_NAME)));
	//	vDetails.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_DETAILS)));
	}
}
