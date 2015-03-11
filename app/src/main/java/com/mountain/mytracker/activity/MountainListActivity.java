package com.mountain.mytracker.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.MountainListAdapter;
import com.mountain.mytracker.db.NewDatabaseHelper;

public class MountainListActivity extends ListActivity {

	NewDatabaseHelper db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.mountain_list_layout);

		db = new NewDatabaseHelper(this);
		Cursor c = db.getMountainsList();

		this.setListAdapter(new MountainListAdapter(MountainListActivity.this,
				c, 1));
	}

	@Override
	protected void onListItemClick(ListView lv, View v, final int position,
			final long id) {
		Intent i;
		String munte,munte_id;
		i = new Intent(this, MountainTrackListActivity.class);
		Cursor nume_munte = (Cursor) lv.getItemAtPosition(position);
		munte = nume_munte.getString(nume_munte
				.getColumnIndex(DatabaseEntry.COL_MOUNTAIN_NAME));
		munte_id = nume_munte.getString(nume_munte.getColumnIndex(DatabaseEntry.COL_MOUNTAIN_ID));
		i.putExtra(DatabaseEntry.COL_MOUNTAIN_NAME, munte);
		i.putExtra(DatabaseEntry.COL_MOUNTAIN_ID, munte_id);
		this.startActivity(i);
	}
}
