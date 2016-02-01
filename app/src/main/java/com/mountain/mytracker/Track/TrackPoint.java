package com.mountain.mytracker.Track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.NewDatabaseHelper;

/**
 * Created by astirb on 26.01.2016.
 */

public class TrackPoint {

    //Attributes
    private Integer trackId;
    private Double altitude, latitude, longitude;
    private Float speed, accuracy;
    private Long time;

    //Database
    private DatabaseHelper mDatabase;
    private NewDatabaseHelper factoryDatabase;


    public TrackPoint(int trackId, Double latitude, Double longitude, Double altitude,
                      Float speed, Float accuracy, Long time, Context context){
        this.trackId = trackId;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.time = time;

        this.mDatabase = new DatabaseHelper(context);
        this.factoryDatabase = new NewDatabaseHelper(context);
    }

    public void toDatabase(){
        ContentValues row = new ContentValues();

        row.put(DatabaseEntry.COL_ALT, altitude);
        row.put(DatabaseEntry.COL_LAT, latitude);
        row.put(DatabaseEntry.COL_LON, longitude);
        //Must modify database to contain this columns
        //row.put(DatabaseEntry.COL_ACC, accuracy);
        //row.put(DatabaseEntry.COL_SPD, speed);
        //row.put(DatabaseEntry.COL_TMP, time);
        row.put(DatabaseEntry.COL_TRACK_NO, trackId.toString());
        mDatabase.getWritableDatabase().insert(DatabaseEntry.TABLE_MY_TRACKS_POINTS, null, row);
        mDatabase.close();
    }
}
