package com.mountain.mytracker.Track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.ContactsContract;
import android.util.Log;

import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.NewDatabaseHelper;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by astirb on 26.01.2016.
 */

public class Track implements Serializable {

    private Integer trackId, mountainId, factoryTrackId;
    private String trackName;
    //factory database
    private String trackDifficulty, trackMark, trackLength, trackDescription, trackAvailability;

    //user database

    private ArrayList<TrackPoint> trackPoints;
    private ArrayList<GeoPoint> trackGeoPoints;

    //Database
    private DatabaseHelper mDatabase;
    private NewDatabaseHelper factoryDatabase;

    public Track(Context context){
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();

        mDatabase = new DatabaseHelper(context);
        factoryDatabase = new NewDatabaseHelper(context);
    }

    public Track(Integer trackId, Context context){
        this.trackId = trackId;
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();

        mDatabase = new DatabaseHelper(context);
        factoryDatabase = new NewDatabaseHelper(context);

        this.fromFactoryDatabase(trackId, context);
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    public Integer getMountainId() {
        return mountainId;
    }

    public void setMountainId(Integer mountainId) {
        this.mountainId = mountainId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackDifficulty() {
        return trackDifficulty;
    }

    public void setTrackDifficulty(String trackDifficulty) {
        this.trackDifficulty = trackDifficulty;
    }

    public String getTrackMark() {
        return trackMark;
    }

    public void setTrackMark(String trackMark) {
        this.trackMark = trackMark;
    }

    public String getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(String trackLength) {
        this.trackLength = trackLength;
    }

    public String getTrackDescription() {
        return trackDescription;
    }

    public void setTrackDescription(String trackDescription) {
        this.trackDescription = trackDescription;
    }

    public String getTrackAvailability() {
        return trackAvailability;
    }

    public void setTrackAvailability(String trackAvailability) {
        this.trackAvailability = trackAvailability;
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(ArrayList<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    public void addTrackPoint(TrackPoint trackPoint){
        this.trackPoints.add(trackPoint);
    }

    public void addTrackGeoPoint(GeoPoint trackGeoPoint){
        this.trackGeoPoints.add(trackGeoPoint);
    }

    public ArrayList<GeoPoint> getTrackGeoPoints() {
        return trackGeoPoints;
    }

    public void setTrackGeoPoints(ArrayList<GeoPoint> trackGeoPoints) {
        this.trackGeoPoints = trackGeoPoints;
    }

    public Integer getTrackPointsCount(){
        return this.getTrackPoints().size();
    }

    public void fromFactoryDatabase(Integer trackId, Context context){
        TrackPoint newTrackPoint;
        String selection, table, sortOrder;
        String[] selectionArgs;
        Cursor c;

        //fetch data about track
        selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
        selectionArgs = new String[] { trackId.toString() };
        table = DatabaseEntry.TABLE_MOUNTAIN_TRACK;

        c = factoryDatabase.myQuery(table, null, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        this.setMountainId(c.getInt(c.getColumnIndex(DatabaseEntry.COL_MOUNTAIN_ID)));
        this.setTrackAvailability(c.getString(c.getColumnIndex(DatabaseEntry.COL_AVLB)));
        this.setTrackDifficulty(c.getString(c.getColumnIndex(DatabaseEntry.COL_DIFF)));
        this.setTrackDescription(c.getString(c.getColumnIndex(DatabaseEntry.COL_DESCRIPTION)));
        this.setTrackLength(c.getString(c.getColumnIndex(DatabaseEntry.COL_LENGTH)));
        this.setTrackMark(c.getString(c.getColumnIndex(DatabaseEntry.COL_MRK)));
        this.setTrackName(c.getString(c.getColumnIndex(DatabaseEntry.COL_TRACK_NAME)));

        //fetch data about trackpoints
        table = DatabaseEntry.TABLE_TRACK_POINTS;
        sortOrder = DatabaseEntry.COL_ORD;

        c = factoryDatabase.myQuery(table, null, selection, selectionArgs, null,
                null, sortOrder);

        c.moveToFirst();

        do {
            Double latitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LAT));
            Double longitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LON));
            Double altitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_ALT));


            newTrackPoint = new TrackPoint(trackId, latitude, longitude, altitude, null, null, null, context);
            this.addTrackPoint(newTrackPoint);
            this.addTrackGeoPoint(new GeoPoint(latitude,longitude));

        } while (c.moveToNext());

    }

    public void fromDatabase(Integer trackId, Context context) {
        TrackPoint newTrackPoint;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Cursor c;

        // TO-DO
        // Must get details about Track, like title, description, etc

        qb.setTables(DatabaseEntry.TABLE_MY_TRACKS_POINTS);
        c = qb.query(mDatabase.getReadableDatabase(), null, DatabaseEntry.COL_TRACK_NO + " = ? ",
                new String[]{trackId.toString()}, null, null, DatabaseEntry._ID);
        c.moveToFirst();
        if (c.getCount() > 0) {

            do {
                Double latitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LAT));
                Double longitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LON));
                Double altitude = c.getDouble(c.getColumnIndex(DatabaseEntry.COL_ALT));

                // TO-DO
                // Must modify database to contain this columns!!!
                //Float speed = c.getFloat(c.getColumnIndex(DatabaseEntry.COL_SPD));
                //Float accuracy = c.getFloat(c.getColumnIndex(DatabaseEntry.COL_ACC));
                //Long time = c.getLong(c.getColumnIndex(DatabaseEntry.COL_TMP));

                newTrackPoint = new TrackPoint(trackId, latitude, longitude, altitude, null, null, null, context);
                this.addTrackPoint(newTrackPoint);
                this.addTrackGeoPoint(new GeoPoint(latitude, longitude));

            } while (c.moveToNext());

        }
    }
    
    public void createDatabaseEntry(Integer trackId){
        ContentValues row = new ContentValues();
        Integer mTrackId;
        String mTrackName;

        if (!checkEmptyDatabase()) {
            mTrackId = 1;
        } else {
            mTrackId = getTrackNo() + 1;
        }
        mTrackName = "Track#" + mTrackId.toString();

        this.trackId = mTrackId;

        row.put(DatabaseEntry.COL_TRACK_NO, mTrackId);
        row.put(DatabaseEntry.COL_TRACK_NAME, mTrackName);
        if(trackId != null) {
            row.put(DatabaseEntry.COL_TRACK_ID, trackId);
            this.factoryTrackId = trackId;
        }
        mDatabase.getWritableDatabase().insert(DatabaseEntry.TABLE_MY_TRACKS, null, row);
        mDatabase.close();
    }

    private int getTrackNo() {
        Integer mTrackNo;
        SQLiteDatabase db;

        db = mDatabase.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();


        qb.setTables(DatabaseEntry.TABLE_MY_TRACKS);
        Cursor c = qb.query(db, new String[]{"max(" + DatabaseEntry.COL_TRACK_NO + ")"},
                null, null, null, null, null);

        c.moveToFirst();
        mTrackNo = c.getInt(c.getColumnIndex("max(" + DatabaseEntry.COL_TRACK_NO + ")") + 1);

        db.close();
        return mTrackNo;
    }

    private boolean checkEmptyDatabase() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseEntry.TABLE_MY_TRACKS);
        Cursor c = qb.query(mDatabase.getReadableDatabase(), null, null, null,
                null, null, null);
        if (c.getCount() == 0) {
            Log.v("verifica BD null", "are 0 intrari");
            c.close();
            return false;
        } else {
            c.close();
            return true;
        }
    }
}