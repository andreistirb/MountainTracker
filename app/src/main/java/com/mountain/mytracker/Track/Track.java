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

public class Track{

    protected Integer trackId;
    protected String trackName;

    protected ArrayList<TrackPoint> trackPoints;
    protected ArrayList<GeoPoint> trackGeoPoints;

    public Track(){
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();
    }

    public Track(Integer trackId){
        this.trackId = trackId;
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
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

}
