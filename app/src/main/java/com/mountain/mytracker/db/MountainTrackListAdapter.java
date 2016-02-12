package com.mountain.mytracker.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;

public class MountainTrackListAdapter extends CursorAdapter {
	
	public MountainTrackListAdapter(Context context, Cursor cursor){
		super(context, cursor, 1);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup vg){
		return LayoutInflater.from(vg.getContext()).inflate(R.layout.mountain_track_list_item, vg, false);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		TextView vName = (TextView) view.findViewById(R.id.mountain_track_list_text);
		TextView vDiff = (TextView) view.findViewById(R.id.mountain_track_list_diff);
		TextView vLength = (TextView) view.findViewById(R.id.mountain_track_list_length);
		ImageView vImg = (ImageView) view.findViewById(R.id.mountain_track_list_pic);
	
		vName.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_TRACK_NAME)));
		vDiff.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_DIFF)));
		vLength.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_LENGTH)));
		
		String imgName = cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_MRK));
		int id = context.getResources().getIdentifier(imgName, "drawable","com.mountain.mytracker.activity");
		vImg.setImageResource(id);	
	}
}
