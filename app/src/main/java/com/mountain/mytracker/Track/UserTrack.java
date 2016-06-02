package com.mountain.mytracker.Track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseHelper;

import org.osmdroid.util.GeoPoint;

/**
 * Created by astirb on 17.02.2016.
 */
public class UserTrack extends Track {
    private Integer factoryTrackId;
    private Double max_alt, min_alt;
    private Float avg_speed, max_speed, distance;
    private Long time;
    private DatabaseHelper mDatabase;
    private String name;

    public UserTrack(Context context) {
        super();
        mDatabase = new DatabaseHelper(context);

        this.setTime(0L);
        this.setDistance(0F);
        this.setAvg_speed(0F);
        this.setMax_speed(0F);
        this.setMax_alt(0D);
        this.setMin_alt(0D);
    }

    public UserTrack(Integer trackId, Context context) {
        super(trackId);
        mDatabase = new DatabaseHelper(context);

        Log.d("in constructor", context.toString());

        this.setTime(0L);
        this.setDistance(0F);
        this.setAvg_speed(0F);
        this.setMax_speed(0F);
        this.setMax_alt(0D);
        this.setMin_alt(0D);

        try {
            this.fromDatabase(trackId, context);
            Log.v("in constructor", this.getName());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Integer getFactoryTrackId() {
        return factoryTrackId;
    }

    public void setFactoryTrackId(Integer factoryTrackId) {
        this.factoryTrackId = factoryTrackId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Float getAvg_speed() {
        return avg_speed;
    }

    public void setAvg_speed(Float avg_speed) {
        this.avg_speed = avg_speed;
    }

    public Float getMax_speed() {
        return max_speed;
    }

    public void setMax_speed(Float max_speed) {
        this.max_speed = max_speed;
    }

    public Double getMax_alt() {
        return max_alt;
    }

    public void setMax_alt(Double max_alt) {
        this.max_alt = max_alt;
    }

    public Double getMin_alt() {
        return min_alt;
    }

    public void setMin_alt(Double min_alt) {
        this.min_alt = min_alt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void fromDatabase(Integer trackId, Context context) throws Exception{
        TrackPoint newTrackPoint;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Cursor c;

        Log.d("in from Database", trackId.toString());
        /*qb.setTables(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS);
        c = qb.query(mDatabase.getReadableDatabase(), null,
                DatabaseContract.DatabaseEntry.COL_TRACK_NO + " = ? ",
                new String[]{trackId.toString()}, null, null, null);*/
        c = mDatabase.getReadableDatabase().query(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS,
                null, "CAST(" + DatabaseContract.DatabaseEntry.COL_TRACK_NO + " as TEXT)" + " = ? ",
                new String[]{trackId.toString()}, null, null, null);
        /*c = mDatabase.getReadableDatabase().query(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS, null, null, null, null, null, null);*/

        c.moveToFirst();

        if (c.getCount() > 0) {
            Log.v("found", "asd");
            this.setTime(c.getLong(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TIME)));
            this.setDistance(c.getFloat(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_DISTANCE)));
            this.setFactoryTrackId(c.getInt(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_ID)));
            this.setAvg_speed(c.getFloat(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MED_SPEED)));
            this.setMax_speed(c.getFloat(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MAX_SPEED)));
            this.setMin_alt(c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_MIN_ALT)));
            this.setMax_alt(c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_MAX_ALT)));
            this.setName(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_NAME)));
        }
        else{
            throw new Exception();
        }

        qb.setTables(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS_POINTS);
        c = qb.query(mDatabase.getReadableDatabase(), null,
                "CAST(" + DatabaseContract.DatabaseEntry.COL_TRACK_NO + " as TEXT)" + " = ? ",
                new String[]{trackId.toString()}, null, null, DatabaseContract.DatabaseEntry._ID);
        c.moveToFirst();
        if (c.getCount() > 0) {

            do {
                Double latitude = c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_LAT));
                Double longitude = c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_LON));
                Double altitude = c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_ALT));
                Location l = new Location("gps");
                l.setLatitude(latitude);
                l.setLongitude(longitude);
                l.setAltitude(altitude);

                // TO-DO
                // Must modify database to contain this columns!!!
                //Float speed = c.getFloat(c.getColumnIndex(DatabaseEntry.COL_SPD));
                //Float accuracy = c.getFloat(c.getColumnIndex(DatabaseEntry.COL_ACC));
                //Long time = c.getLong(c.getColumnIndex(DatabaseEntry.COL_TMP));

                newTrackPoint = new TrackPoint(trackId, l, context);
                this.addTrackPoint(newTrackPoint);
                this.addTrackGeoPoint(new GeoPoint(latitude, longitude));

            } while (c.moveToNext());

        }
        else{
            throw new Exception();
        }
        c.close();
        mDatabase.close();
    }

    public void createDatabaseEntry(Integer trackId) {
        ContentValues row = new ContentValues();
        Integer mTrackId;

        if (!checkEmptyDatabase()) {
            mTrackId = 1;
        } else {
            mTrackId = getTrackNo() + 1;
        }
        name = "Track#" + mTrackId.toString();

        Log.i("createDatabaseEntry", name);

        this.trackId = mTrackId;

        row.put(DatabaseContract.DatabaseEntry.COL_TRACK_NO, mTrackId);
        row.put(DatabaseContract.DatabaseEntry.COL_TRACK_NAME, name);
        if (trackId != null) {
            row.put(DatabaseContract.DatabaseEntry.COL_TRACK_ID, trackId);
            this.factoryTrackId = trackId;
        }
        mDatabase.getWritableDatabase().insert(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS, null, row);
        mDatabase.close();
    }

    private void computeTime(){

        Long firstTime, lastTime;

        if(this.getTrackPointsCount() > 0) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                firstTime = trackPoints.get(0).getLocation().getElapsedRealtimeNanos();
                lastTime = trackPoints.get(this.getTrackPointsCount() - 1).getLocation().getElapsedRealtimeNanos();
            }
            else {
                firstTime = trackPoints.get(0).getLocation().getTime();
                lastTime = trackPoints.get(this.getTrackPointsCount() - 1).getLocation().getTime();
            }

            this.setTime(lastTime - firstTime);
        }
    }

    private void computeDistance(){

        if(this.getTrackPointsCount() > 0) {
            Float totalDistance = 0F;
            for (int i = 0; i < this.getTrackPointsCount() - 2; i++) {
                totalDistance += this.getTrackPoints().get(i).getLocation().distanceTo(this.getTrackPoints().get(i + 1).getLocation());
            }
            this.setDistance(totalDistance);
        }
    }

    private void computeAvgSpeed(){

        if(this.getTrackPointsCount() > 0) {
            Float total_speed = 0F;
            for (int i = 0; i < this.getTrackPointsCount() - 1; i++) {
                total_speed += this.getTrackPoints().get(i).getLocation().getSpeed();
            }
            this.setAvg_speed(total_speed / this.getTrackPointsCount());
        }
    }

    private void computeMaxSpeed(){

        if(this.getTrackPointsCount() > 0) {
            Float max_speed = 0F;
            for (int i = 0; i < this.getTrackPointsCount() - 1; i++) {
                if (max_speed < this.getTrackPoints().get(i).getLocation().getSpeed())
                    max_speed = this.getTrackPoints().get(i).getLocation().getSpeed();
            }
            this.setMax_speed(max_speed);
        }
    }

    private void computeMinAlt(){

        if(this.getTrackPointsCount() > 0) {
            Double min_alt = 9999D;
            for (int i = 0; i < this.getTrackPointsCount() - 1; i++) {
                if (min_alt > this.getTrackPoints().get(i).getLocation().getAltitude())
                    min_alt = this.getTrackPoints().get(i).getLocation().getAltitude();
            }
            this.setMin_alt(min_alt);
        }
    }

    private void computeMaxAlt(){
        if(this.getTrackPointsCount() > 0) {
            Double max_alt = 0D;
            for (int i = 0; i < this.getTrackPointsCount() - 1; i++) {
                if (max_alt < this.getTrackPoints().get(i).getLocation().getAltitude())
                    max_alt = this.getTrackPoints().get(i).getLocation().getAltitude();
            }
            this.setMax_alt(max_alt);
        }
    }

    public void updateDatabase() {

        Integer returnCode;

        this.computeTime();
        this.computeAvgSpeed();
        this.computeMaxSpeed();
        this.computeMinAlt();
        this.computeMaxAlt();
        this.computeDistance();

        Log.d("updateDatabase", this.getTime().toString());

        ContentValues row = new ContentValues();
        row.put(DatabaseContract.DatabaseEntry.COL_TRACK_NAME, name);
        row.put(DatabaseContract.DatabaseEntry.COL_TIME, time);
        row.put(DatabaseContract.DatabaseEntry.COL_DISTANCE, distance);
        row.put(DatabaseContract.DatabaseEntry.COL_MED_SPEED, avg_speed);
        row.put(DatabaseContract.DatabaseEntry.COL_MAX_SPEED, max_speed);
        row.put(DatabaseContract.DatabaseEntry.COL_TRACK_MIN_ALT, min_alt);
        row.put(DatabaseContract.DatabaseEntry.COL_TRACK_MAX_ALT, max_alt);
        returnCode = mDatabase.getWritableDatabase().update(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS, row,
                "CAST(" + DatabaseContract.DatabaseEntry.COL_TRACK_NO + " as TEXT)" + " = ? ",
                new String[]{trackId.toString()});

        Log.d("updateDatabase", returnCode.toString());
        mDatabase.close();
    }

    public int updateDatabaseName(String trackName){
        int returnCode;

        ContentValues row = new ContentValues();

        row.put(DatabaseContract.DatabaseEntry.COL_TRACK_NAME, trackName);
        returnCode = mDatabase.getWritableDatabase().update(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS,row,
                "CAST(" + DatabaseContract.DatabaseEntry.COL_TRACK_NO + " as TEXT)" + " = ? ",
                new String[]{trackId.toString()});
        mDatabase.close();

        return returnCode;
    }

    private int getTrackNo() {
        Integer mTrackNo;
        SQLiteDatabase db;

        db = mDatabase.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();


        qb.setTables(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS);
        Cursor c = qb.query(db, new String[]{"max(" + DatabaseContract.DatabaseEntry.COL_TRACK_NO + ")"},
                null, null, null, null, null);

        c.moveToFirst();
        mTrackNo = c.getInt(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_NO) + 1);

        db.close();
        return mTrackNo;
    }

    private boolean checkEmptyDatabase() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS);
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
