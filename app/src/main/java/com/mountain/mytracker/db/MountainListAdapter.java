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

public class MountainListAdapter extends CursorAdapter {
	
	public MountainListAdapter(Context context, Cursor cursor){
		super(context, cursor, 1);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup vg){
		return LayoutInflater.from(vg.getContext()).inflate(R.layout.mountain_list_item, vg, false);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		TextView vName = (TextView) view.findViewById(R.id.mountain_list_text);
	
		vName.setText(cursor.getString(cursor.getColumnIndex(DatabaseEntry.COL_MOUNTAIN_NAME)));

	}
}
