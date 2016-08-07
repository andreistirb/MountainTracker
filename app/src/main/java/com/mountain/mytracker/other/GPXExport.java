package com.mountain.mytracker.other;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.util.Log;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;

public class GPXExport {
	
	private DatabaseHelper db;
	File root = android.os.Environment.getExternalStorageDirectory();

    public GPXExport(Context context){
        db = new DatabaseHelper(context);
    }
	
	/*public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();

		return (Environment.MEDIA_MOUNTED.equals(state));
	}*/
	
	/*public File getFileDir(String mDirName) {
	    File file = new File(Environment.getExternalStoragePublicDirectory(null), mDirName);
	    if (!file.mkdirs()) {
	        Log.e("in gpxexport", "Directory not created");
	    }
	    return file;
	}*/
	
	public void createFile(Integer mTrackNo, String filename, String track_name){
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DatabaseEntry.TABLE_MY_TRACKS_POINTS);
		Cursor c = qb.query(db.getReadableDatabase(), null,
				"CAST(" + DatabaseEntry.COL_TRACK_NO + " as TEXT)" + " = ? ",
				new String[] { mTrackNo.toString() }, null, null,
				DatabaseEntry._ID);
		c.moveToFirst();
		
		File dir = new File (root.getAbsolutePath() + "/MountainTracker");
		dir.mkdirs();
		File file = new File(dir, filename);
		
		try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        
	        pw.println("<?xml version='1.0' encoding='UTF-8'?>");
	        pw.println("<gpx>");
	        pw.println("<metadata>");
	        pw.println("<name>");
	        pw.println(track_name);
	        pw.println("</name>");
	        pw.println("</metadata>");
	        pw.println("<trk>");
	        pw.println("<trkseg>");
	        
	        do{
	        	pw.print("<trkpt lat=" + '"' + c.getString(c.getColumnIndex(DatabaseEntry.COL_LAT)) + '"' +
	        			" lon=" + '"' + c.getString(c.getColumnIndex(DatabaseEntry.COL_LON)) + '"' + "> " +
	        					"<ele>" + c.getString(c.getColumnIndex(DatabaseEntry.COL_ALT)) + "</ele>" + "</trkpt>");
	        }while(c.moveToNext());
	        
	        pw.println("</trkseg>");
	        pw.println("</trk>");
	        pw.println("</gpx>");
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
