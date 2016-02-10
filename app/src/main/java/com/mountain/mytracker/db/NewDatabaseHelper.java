package com.mountain.mytracker.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.Serializable;

public class NewDatabaseHelper extends SQLiteAssetHelper implements Serializable {

	private static final String DB_NAME = "MountainTracker.s3db";
	private static final int DB_VERSION = 1;
	
	public NewDatabaseHelper(Context context){
		super(context,DB_NAME, null, DB_VERSION);
	}
	
	public Cursor myQuery(String table, String[] projectionIn, String selection, String[] selectionArgs, 
			String groupBy, String having, String sortOrder){
		
		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		qb.setTables(table);
		
		Cursor c = qb.query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder);
		c.moveToFirst();
		return c;
	}
	
	public Cursor getMountainsList(){
		
		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String sqlTables = DatabaseEntry.TABLE_MOUNTAIN;

		qb.setTables(sqlTables);
		Cursor c = qb.query(db, null, null, null,
				null, null, DatabaseEntry.COL_MOUNTAIN_NAME);

		c.moveToFirst();
		return c;
		
	}
	
}
