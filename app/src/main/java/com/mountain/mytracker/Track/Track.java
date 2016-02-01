package com.mountain.mytracker.Track;

import android.content.Context;
import android.database.Cursor;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.NewDatabaseHelper;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by astirb on 26.01.2016.
 */

public class Track implements Serializable {
    private Integer trackId, mountainId;
    private String trackName, trackDifficulty, trackMark, trackLength, trackDescription, trackAvailability;
    private ArrayList<TrackPoint> trackPoints;

    //Database
    private DatabaseHelper mDatabase;
    private NewDatabaseHelper factoryDatabase;

    public Track(Integer trackId){
        this.trackId = trackId;
    }

    public void addTrackPoint(TrackPoint trackPoint){
        this.trackPoints.add(trackPoint);
    }

    public Track fromFactoryDatabase(Integer trackId, Context context){
        Track newTrack = new Track(trackId);
        TrackPoint newTrackPoint;

        //We also need data about track, not only trackpoints!!!
        String selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
        String[] selectionArgs = new String[] { trackId.toString() };
        String table = DatabaseEntry.TABLE_TRACK_POINTS;
        String sortOrder = DatabaseEntry.COL_ORD;

        Cursor c = factoryDatabase.myQuery(table, null, selection, selectionArgs, null,
                null, sortOrder);

        c.moveToFirst();

        do {
            Double latitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LAT));
            Double longitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LON));
            Double altitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_ALT));
            //Must modify database to contain this columns!!!
            //Float speed = c.getFloat(c.getColumnIndex(DatabaseEntry.COL_SPD));
            //Float accuracy = c.getFloat(c.getColumnIndex(DatabaseEntry.COL_ACC));
            //Long time = c.getLong(c.getColumnIndex(DatabaseEntry.COL_TMP));

            newTrackPoint = new TrackPoint(trackId, latitude, longitude, altitude, null, null, null, context);
            newTrack.addTrackPoint(newTrackPoint);

        } while (c.moveToNext());

        return newTrack;
    }

    /*public Track fromDatabase(){
        Track newPoint;
        return newPoint;
    }*/

    public void toDatabase(){

    }
}
