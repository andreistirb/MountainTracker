package com.mountain.mytracker.gps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.activity.TrackLoggerActivity;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.NewDatabaseHelper;

import org.osmdroid.util.GeoPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GPSLogger extends Service implements LocationListener {

    //database
    private DatabaseHelper mDatabase;

    //Location
    protected Location mLastLocation;
    protected Location mOldLocation;
    protected Location mCurrentLocation;
    protected LocationManager mLocationManager;

    private float distance;

    protected String mLastUpdateTime;
    private boolean allowSendingNotifications = false;
    private ArrayList<GeoPoint> trackPoints;
    private NewDatabaseHelper factoryDB;

    private Intent notification;

    private static final String TAG = GPSLogger.class.getSimpleName();

    int mStartMode; // indicates how to behave if the service is killed
    IBinder mBinder; // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    private static boolean isTracking; // variabila globala care arata daca
    // serviciul este pornit sau nu
    private boolean isGPSEnabled;

    private Integer mTrackNo;   //id-ul traseului inregistrat de user
    private String track_name;  //numele traseului (ori cel predefinit, din baza de date implicita,
    //ori cel dat de user
    private String track_id;    //id-ul traseului din baza de date implicita

    int trackPointsCount; //pentru a determina prima locatie

    private Vibrator mVibrator;

    //track battery level
    IntentFilter batteryIntentFilter;
    Intent batteryStatus;
    int batteryLevel;

    //track data
    long time, first_fix;
    float max_speed, sum_speed, avg_speed;
    double max_alt, min_alt;

    //implementing GeoFence
    boolean shouldGeofence = false;


    // the service is being created
    @Override
    public void onCreate() {

        trackPointsCount = 0;

        //database
        mDatabase = new DatabaseHelper(this.getBaseContext());
        factoryDB = new NewDatabaseHelper(this.getBaseContext());

        //location
        distance = 0.0f;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Vibrator
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Battery
        batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, batteryIntentFilter);

        super.onCreate();
    }

    // The service is starting, due to a call to startService()
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // receive available info
        track_name = intent.getExtras().getString("track_name");
        if (intent.hasExtra("track_id")) {
            track_id = intent.getExtras().getString("track_id");
//            new ParseAsync().execute();
            shouldGeofence = true;
        }
        Log.v("in gpslogger", "am primit numele");

        // start tracking
        startTracking();

        Log.v(TAG, "Service onStartCommand(-," + flags + "," + startId + ")");
        startForeground(1, getNotification());

        return Service.START_STICKY;
    }

    private final IBinder binder = new GPSLoggerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Service onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "Service onUnbind()");
        // If we aren't currently tracking we can
        // stop ourselves
        if (!isTracking) {
            Log.v(TAG, "Service self-stopping");
            stopSelf();
        }

        // We don't want onRebind() to be called, so return false.
        return false;
    }

    public class GPSLoggerBinder extends Binder {
        public GPSLogger getService() {
            return GPSLogger.this;
        }
    }

    @Override
    public void onDestroy() {
        if (isTracking) {
            stopTracking();
        }

        stopNotifyBackgroundService();
    }


    private void startTracking() {

        mVibrator.vibrate(500);
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.notify(1, getNotification());

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        if (!checkEmptyDatabase(mDatabase)) {
            mTrackNo = 1;
        } else {
            mTrackNo = getTrackNo(mDatabase) + 1;
        }
        createEntry(mTrackNo);

        trackPointsCount = 0;
        min_alt = 9999;
        max_alt = 0;

        Log.v("in startTracking()", "notification");
        isTracking = true;

    }

    private void stopTracking() {
        isTracking = false;
        mVibrator.vibrate(500);
        mLocationManager.removeUpdates(this);
        this.stopSelf();
    }


    @Override
    public void onLocationChanged(Location location) {

        if((location != null) && (location.getAccuracy() < 20)) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            computeTime(location);
            computeSpeed(location);
            computeDistance(location);
            computeAlt(location);
            insertLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mCurrentLocation.getAltitude());
            buildNotification();
            sendBroadcast(notification);
            Log.v("in sender", "trimit date");
            Log.v("in sender", String.valueOf(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)));
            batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (batteryLevel >= 40) {

            } else if (batteryLevel > 20 && batteryLevel < 40) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 100, this);
            } else if (batteryLevel <= 20) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 500, this);
            }
            if (shouldGeofence) {
                //  new Geofencing().execute(new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void insertLocation(double latitude, double longitude,
                               double altitude) {
        ContentValues row = new ContentValues();
        row.put(DatabaseEntry.COL_ALT, altitude);
        row.put(DatabaseEntry.COL_LAT, latitude);
        row.put(DatabaseEntry.COL_LON, longitude);
        row.put(DatabaseEntry.COL_TRACK_NO, mTrackNo.toString());
        mDatabase.getWritableDatabase().insert(DatabaseEntry.TABLE_MY_TRACKS_POINTS,
                null, row);
        Integer x = mTrackNo;
        Log.v("cand insereaza in Db", x.toString());
        mDatabase.close();

    }

    private void buildNotification() {
        notification = new Intent("broadcastGPS");
        notification.putExtra("altitude", mCurrentLocation.getAltitude());
        notification.putExtra("latitude", mCurrentLocation.getLatitude());
        notification.putExtra("longitude", mCurrentLocation.getLongitude());
        notification.putExtra("speed", mCurrentLocation.getSpeed());
        notification.putExtra("time", time);
        notification.putExtra("mTrackNo", mTrackNo);
        notification.putExtra("distance", distance);
        notification.putExtra("max_speed", max_speed);
        notification.putExtra("avg_speed", avg_speed);
        notification.putExtra("max_alt", max_alt);
        notification.putExtra("min_alt", min_alt);
    }

    private void computeDistance(Location location) {
        float distance_to = 0;
        if (trackPointsCount == 0) {
            mOldLocation = location;
            trackPointsCount++;
        } else {
            distance_to = location.distanceTo(mOldLocation);
            mOldLocation = location;
        }

        distance_to = ((float) Math.floor(distance_to) / 1000);
        distance += distance_to;
    }

    private void computeTime(Location location) {
        if (trackPointsCount == 0) {
            first_fix = location.getTime();
            time = 0;
        } else {
            time = location.getTime() - first_fix;
        }
    }

    private void computeSpeed(Location location) {
        if (trackPointsCount == 0) {
            max_speed = 0;
            sum_speed = 0;
            max_speed = location.getSpeed();
        } else {
            if (location.getSpeed() > max_speed)
                max_speed = location.getSpeed();
        }
        sum_speed += location.getSpeed();
        avg_speed = sum_speed / trackPointsCount;
    }

    private void computeAlt(Location location) {
        if (max_alt < location.getAltitude()) {
            max_alt = location.getAltitude();
        }
        if (min_alt > location.getAltitude())
            min_alt = location.getAltitude();
    }

    private Notification getNotification() {
        Notification n = new Notification(R.drawable.cruce_galbena,
                getResources().getString(R.string.notification_ticker_text),
                System.currentTimeMillis());

        Intent startTrackLogger = new Intent(this, TrackLoggerActivity.class);
        startTrackLogger.putExtra("track_name", track_name);
        startTrackLogger.putExtra("mTrackNo", mTrackNo);
        if (track_id != null) {
            startTrackLogger.putExtra("track_id", track_id);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                startTrackLogger, PendingIntent.FLAG_UPDATE_CURRENT);
        n.flags = Notification.FLAG_FOREGROUND_SERVICE
                | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        n.setLatestEventInfo(getApplicationContext(),
                getResources().getString(R.string.notification_title),
                getResources().getString(R.string.notification_text),
                contentIntent);
        return n;
    }

    private void stopNotifyBackgroundService() {
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.cancel(1);
    }

    public static boolean isTracking() {
        return isTracking;
    }

    private boolean checkEmptyDatabase(DatabaseHelper database) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseEntry.TABLE_MY_TRACKS);
        Cursor c = qb.query(database.getReadableDatabase(), null, null, null,
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

    private int getTrackNo(DatabaseHelper database) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseEntry.TABLE_MY_TRACKS);
        Cursor c = qb.query(database.getReadableDatabase(),
                new String[]{"max(" + DatabaseEntry.COL_TRACK_NO + ")"},
                null, null, null, null, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("max(" + DatabaseEntry.COL_TRACK_NO
                + ")"));
    }

    public void createEntry(int mTrackNo) {
        ContentValues row = new ContentValues();
        row.put(DatabaseEntry.COL_TRACK_NAME, track_name);
        row.put(DatabaseEntry.COL_TRACK_NO, mTrackNo);
        row.put(DatabaseEntry.COL_TRACK_ID, track_id);
        mDatabase.getWritableDatabase().insert(DatabaseEntry.TABLE_MY_TRACKS, null, row);
        Integer x = mTrackNo;
        Log.v("creeaza o intrare in Db", x.toString());
        mDatabase.close();
    }

    private void populateTrackPoints() {

        String selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
        String[] selectionArgs = new String[]{track_id};
        Log.v("in map view", track_id);
        String table = DatabaseEntry.TABLE_TRACK_POINTS;
        String sortOrder = DatabaseEntry.COL_ORD;

        Cursor c = factoryDB.myQuery(table, null, selection, selectionArgs, null,
                null, sortOrder);
        c.moveToFirst();
        do {
            double latitude = c.getDouble(c
                    .getColumnIndex(DatabaseEntry.COL_LAT));
            double longitude = c.getDouble(c
                    .getColumnIndex(DatabaseEntry.COL_LON));
            trackPoints.add(new GeoPoint(latitude, longitude));
        } while (c.moveToNext());

    }

}
