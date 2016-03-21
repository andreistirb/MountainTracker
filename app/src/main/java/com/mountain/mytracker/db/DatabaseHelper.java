package com.mountain.mytracker.db;

import java.io.Serializable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;

/**
 * Helper pentru baza de date a traseelor
 */

public class DatabaseHelper extends SQLiteOpenHelper implements Serializable {

	private static final String DB_NAME = "MyTracks.db";
	private static final int DB_VERSION = 1;

	//Table cu traseele create de utilizator
	private static final String SQL_CREATE_TABLE_TRACK = "CREATE TABLE "
			+ DatabaseEntry.TABLE_MY_TRACKS + " ( "
			+ DatabaseEntry._ID + " integer primary key autoincrement, "
			+ DatabaseEntry.COL_TRACK_NO + " , " //should be integer autoincrement!!!
			+ DatabaseEntry.COL_TRACK_NAME + " , "
			+ DatabaseEntry.COL_TRACK_ID + " , "
			+ DatabaseEntry.COL_DISTANCE + " , "
			+ DatabaseEntry.COL_TIME + " , "
			+ DatabaseEntry.COL_MED_SPEED + " , "
			+ DatabaseEntry.COL_MAX_SPEED + " , "
			+ DatabaseEntry.COL_TRACK_MAX_ALT + " , "
			+ DatabaseEntry.COL_TRACK_MIN_ALT 
			+ " )";
	
	// Tabel cu punctele traseelor create de utilizator
	private static final String SQL_CREATE_TABLE_TRACK_POINT = "CREATE TABLE "
			+ DatabaseEntry.TABLE_MY_TRACKS_POINTS + " (" 
			+ DatabaseEntry._ID	+ " integer primary key autoincrement, "
			+ DatabaseEntry.COL_TRACK_NO + " , " 
			+ DatabaseEntry.COL_LAT + " , " 
			+ DatabaseEntry.COL_LON + ", " 
			+ DatabaseEntry.COL_ALT + " , " 
			+ DatabaseEntry.COL_ORD
			+ ")";


	private static final String SQL_DELETE_TABLE_MY_TRACKS = "DROP TABLE IF EXISTS "
			+ DatabaseEntry.TABLE_MY_TRACKS;
	private static final String SQL_DELETE_TABLE_MY_TRACKS_POINTS = "DROP TABLE IF EXISTS "
			+ DatabaseEntry.TABLE_MY_TRACKS_POINTS;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_TABLE_TRACK);
		db.execSQL(SQL_CREATE_TABLE_TRACK_POINT);

	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_TABLE_MY_TRACKS);
		db.execSQL(SQL_DELETE_TABLE_MY_TRACKS_POINTS);
		onCreate(db);
	}

}
