package com.mountain.mytracker.Track;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.NewDatabaseHelper;

import org.osmdroid.util.GeoPoint;

/**
 * Created by astirb on 17.02.2016.
 */
public class FactoryTrack extends Track {
    private String trackDifficulty, trackMark, trackLength, trackDescription, trackAvailability;
    private Integer mountainId;

    private NewDatabaseHelper factoryDatabase;

    public FactoryTrack(Context context){
        super();
        factoryDatabase = new NewDatabaseHelper(context);
    }

    public FactoryTrack(Integer trackId, Context context){
        super(trackId);
        factoryDatabase = new NewDatabaseHelper(context);
        this.fromFactoryDatabase(trackId, context);
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

    public Integer getMountainId() {
        return mountainId;
    }

    public void setMountainId(Integer mountainId) {
        this.mountainId = mountainId;
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

    public void fromFactoryDatabase(Integer trackId, Context context){
        TrackPoint newTrackPoint;
        String selection, table, sortOrder;
        String[] selectionArgs;
        Cursor c;

        //fetch data about track
        selection = DatabaseContract.DatabaseEntry.COL_TRACK_ID + " = ? ";
        selectionArgs = new String[] { trackId.toString() };
        table = DatabaseContract.DatabaseEntry.TABLE_MOUNTAIN_TRACK;

        c = factoryDatabase.myQuery(table, null, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        this.setMountainId(c.getInt(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID)));
        this.setTrackAvailability(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_AVLB)));
        this.setTrackDifficulty(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_DIFF)));
        this.setTrackDescription(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_DESCRIPTION)));
        this.setTrackLength(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_LENGTH)));
        this.setTrackMark(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MRK)));
        this.setTrackName(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_NAME)));

        //fetch data about trackpoints
        table = DatabaseContract.DatabaseEntry.TABLE_TRACK_POINTS;
        sortOrder = DatabaseContract.DatabaseEntry.COL_ORD;

        c = factoryDatabase.myQuery(table, null, selection, selectionArgs, null,
                null, sortOrder);

        c.moveToFirst();

        do {
            Double latitude = c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_LAT));
            Double longitude = c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_LON));
            Double altitude = c.getDouble(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_ALT));
            Location l = new Location("gps");
            l.setLatitude(latitude);
            l.setLongitude(longitude);
            l.setAltitude(altitude);

            newTrackPoint = new TrackPoint(trackId, l, context);
            this.addTrackPoint(newTrackPoint);
            this.addTrackGeoPoint(new GeoPoint(latitude,longitude));

        } while (c.moveToNext());

        factoryDatabase.close();

    }

}
