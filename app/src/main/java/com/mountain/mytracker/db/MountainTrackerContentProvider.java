package com.mountain.mytracker.db;

import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.mountain.mytracker.activity.TrackerManagerActivity;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;

public class MountainTrackerContentProvider extends ContentProvider {

	public static final String AUTHORITY = TrackerManagerActivity.class.getPackage()
			.getName() + ".provider";
	public static final Uri CONTENT_URI_TRACK = Uri.parse("content://"
			+ AUTHORITY + "/" + DatabaseEntry.TABLE_TRACK);
	public static final Uri CONTENT_URI_MOUNTAIN = Uri.parse("content://"
			+ AUTHORITY + "/" + DatabaseEntry.TABLE_MOUNTAIN);
	public static final Uri CONTENT_URI_MOUNTAIN_TRACK = Uri.parse("content://"
			+ AUTHORITY + "/" + DatabaseEntry.TABLE_MOUNTAIN_TRACK);
    public static final Uri CONTENT_URI_TABLE_TRACK_POINTS = Uri.parse("content://"
            + AUTHORITY + "/" + DatabaseEntry.TABLE_TRACK_POINTS);
    public static final Uri MAP_VIEW_TRACK = Uri.parse("content://" + AUTHORITY
			+ "/" + DatabaseEntry.TABLE_MOUNTAIN_TRACK);

	private static final UriMatcher uriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		uriMatcher.addURI(AUTHORITY, DatabaseEntry.TABLE_TRACK, 1);
		uriMatcher.addURI(AUTHORITY, DatabaseEntry.TABLE_MOUNTAIN, 2);
		uriMatcher.addURI(AUTHORITY, DatabaseEntry.TABLE_MOUNTAIN_TRACK, 3);
	}

	/*
	 * partea pentru baza de date
	 */
	private NewDatabaseHelper mDBHelper;

	@Override
	public boolean onCreate() {
		mDBHelper = new NewDatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		switch (uriMatcher.match(uri)) {
		case 1: {
			long rowId = mDBHelper.getWritableDatabase().insert(
					DatabaseEntry.TABLE_TRACK, null, contentValues);
			break;
		}
		case 2: {
			long rowId = mDBHelper.getWritableDatabase().insert(
					DatabaseEntry.TABLE_MOUNTAIN, null, contentValues);
			break;
		}
		case 3: {
			long rowId = mDBHelper.getWritableDatabase().insert(
					DatabaseEntry.TABLE_MOUNTAIN_TRACK, null, contentValues);
			break;
		}
		}
		return uri; // revision
	}

	/*
	 * Sterge linia cu _id din tabel (deocamdata pentru tabelul cu trasee
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case 1: {
			String trackId = Long.toString(ContentUris.parseId(uri));
			count = mDBHelper.getWritableDatabase().delete(
					DatabaseEntry.TABLE_TRACK, DatabaseEntry.COL_ID + " = ?",
					new String[] { trackId });
			break;
		}
		case 2: {
			String mountainId = Long.toString(ContentUris.parseId(uri));
			count = mDBHelper.getWritableDatabase().delete(
					DatabaseEntry.TABLE_MOUNTAIN,
					DatabaseEntry.COL_ID + " = ?", new String[] { mountainId });
			break;
		}
		case 3: {
			String mountainId = Long.toString(ContentUris.parseId(uri));
			count = mDBHelper.getWritableDatabase().delete(
					DatabaseEntry.TABLE_MOUNTAIN_TRACK,
					DatabaseEntry.COL_ID + " = ?", new String[] { mountainId });
			break;
		}
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null; // revision
	}

	@Override
	public Cursor query(Uri uri, String[] projectionIn, String selectionIn,
			String[] selectionArgsIn, String sortOrder) {
		Cursor c = null;
		String table = "";
		String groupBy = null;
		switch (uriMatcher.match(uri)) {
		case 1: {
			table = DatabaseEntry.TABLE_TRACK;
			break;
		}
		case 2: {
			table = DatabaseEntry.TABLE_MOUNTAIN;
			Log.v("aici", "am schimbat tabela");
			break;
		}
		case 3: {
			table = DatabaseEntry.TABLE_MOUNTAIN_TRACK;
			if (uri == this.MAP_VIEW_TRACK) {
				groupBy = null;
			} else
				groupBy = DatabaseEntry.COL_TRACK_NAME;
			break;
		}
        case 4: {
            table = DatabaseEntry.TABLE_TRACK_POINTS;
            break;
        }
		}
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(table);
		c = qb.query(mDBHelper.getReadableDatabase(), projectionIn,
				selectionIn, selectionArgsIn, groupBy, null, sortOrder);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String selectionIn,
			String[] selectionArgsIn) {

		String table = "";
		switch (uriMatcher.match(uri)) {
		case 1: {
			table = DatabaseEntry.TABLE_TRACK;
			break;
		}
		case 2: {
			table = DatabaseEntry.TABLE_MOUNTAIN;
			break;
		}
		case 3: {
			table = DatabaseEntry.TABLE_MOUNTAIN_TRACK;
			break;
		}
		}
		return mDBHelper.getWritableDatabase().update(table, contentValues,
				selectionIn, selectionArgsIn); // revision
	}
}
