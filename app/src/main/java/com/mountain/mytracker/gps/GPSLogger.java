package com.mountain.mytracker.gps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.activity.TrackLoggerActivity;
import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.MountainTrackerContentProvider;
import com.mountain.mytracker.db.NewDatabaseHelper;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.osmdroid.util.GeoPoint;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GPSLogger extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private class ParseAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params){

            setUpDatabaseTrackPoints();
            return null;
        }
    }


    private class Geofencing extends AsyncTask<ParseGeoPoint, Void, String> {

        private int MAX_GEOFENCE_POINTS = 99;

        @Override
        protected String doInBackground(ParseGeoPoint... params) {

            List<ParseObject> trackPointsList = new ArrayList<ParseObject>();
            //mGeofenceList = new ArrayList<Geofence>();
            //query database for closest 99 points to user's location
            ParseQuery<ParseObject> query = ParseQuery.getQuery("TrackPoint");
            query.fromLocalDatastore();
            query.whereNear("coordinates", params[0]);
            query.setLimit(99);
            //try {
            //trackPointsList =
//            query.findInBackground(new FindCallback<ParseObject>() {
//                                       @Override
//                                       public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
//                                          // Log.i("asyncTask", "retrieved some Parse points");
//
//                                       }
//                                   }
//
//            );
            try {
                trackPointsList = query.find();
                for (ParseObject o : trackPointsList) {
                    ParseGeoPoint pct = (ParseGeoPoint) o.get("coordinates");
                    addGeofence(new GeoPoint(pct.getLatitude(), pct.getLongitude()));
                }
                addGeofences();
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
            //}
            /*catch (com.parse.ParseException e){
                e.printStackTrace();
            }*/
            /*if (c.getCount() <= MAX_GEOFENCE_POINTS){
                c.moveToFirst();
                do{
                    addGeofence(new GeoPoint(c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LAT)),
                            c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LON))));
                   // Log.i("cand adauga geofence","am adaugat inca un geofence");
                }
                while(c.moveToNext());
            }*/

            return "Done!";
        }

        @Override
        protected void onPostExecute(String result) {
           // addGeofences();
            Log.i("In asyncTask", result);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... results) {
        }
    }

    //database
    private DatabaseHelper mDatabase;

    //Google Api
    protected GoogleApiClient mGoogleApiClient;

    //Location
    protected Location mLastLocation;
    protected Location mOldLocation;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
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
    ArrayList<Geofence> mGeofenceList;
    PendingIntent mPendingIntent;
    boolean shouldGeofence = false;


    Track mTrack, factoryTrack;

    // the service is being created
    @Override
    public void onCreate() {

        trackPointsCount = 0;

        //database
        mDatabase = new DatabaseHelper(this.getBaseContext());
        factoryDB = new NewDatabaseHelper(this.getBaseContext());
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "y74djiFMOlXnb6illRwJx7k30xnPzabHCEkM8lQe", "jLvZaXy1OfmnufzNRBIKBwugBKWY06RUyP7pRIzD");

        mGeofenceList = new ArrayList<Geofence>();
        mTrack = new Track();

        //location
        buildGoogleApiClient();
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

        mGoogleApiClient.connect();

        // receive available info
        //track_name = intent.getExtras().getString("track_name");
        if (intent.hasExtra("factoryTrackId")) {
            //track_id = intent.getExtras().getString("track_id");
            factoryTrack = new Track(intent.getExtras().getInt("factoryTrackId"), this.getApplicationContext());
            new ParseAsync().execute();
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

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        stopNotifyBackgroundService();
    }


    private void startTracking() {

        mVibrator.vibrate(500);
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.notify(1, getNotification());

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
        stopLocationUpdates();
        isTracking = false;
        mVibrator.vibrate(500);
        if (shouldGeofence) {
            removeGeofences();
            try {
                ParseObject.unpinAll("trackpoints");
            } catch (com.parse.ParseException parseException) {
                parseException.printStackTrace();
            }
        }
        this.stopSelf();
    }

    //Google API - Location

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
        Log.i("conectat googleapi", "conectat");

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed : ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.

        Log.i(TAG, "Connection suspended, trying to reconnect");
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    //Location Request
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {

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
            mLocationRequest.setInterval(15000);
            mLocationRequest.setFastestInterval(10000);
        } else if (batteryLevel > 20 && batteryLevel < 40) {
            mLocationRequest.setInterval(30000);
            mLocationRequest.setFastestInterval(15000);
        } else if (batteryLevel <= 20) {
            mLocationRequest.setInterval(60000);
            mLocationRequest.setFastestInterval(30000);
        }
        if (shouldGeofence) {
            new Geofencing().execute(new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }

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
        notification.putExtra("trackObject", new Track(14));
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

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
           // Toast.makeText(this, "GoogleApiClient not connected", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
            //Toast.makeText(this, "GoogleApiClient is connected", Toast.LENGTH_LONG).show();
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
    }

    private void removeGeofences() {
        try {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
    }

    @Override
    public void onResult(Status status) {

    }

    private PendingIntent getGeofencePendingIntent() {
        if (mPendingIntent != null)
            return mPendingIntent;
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofence(GeoPoint point) {

        Geofence mGeofence = new Geofence.Builder()
                .setRequestId(String.valueOf(point.getLatitude()))//String.valueOf(mGeofenceList.size()))//point.getLatitude() + point.getLongitude()))
                        .setCircularRegion(point.getLatitude(), point.getLongitude(), 50) //50 meters
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build();

        if (!mGeofenceList.contains(mGeofence))
            mGeofenceList.add(mGeofence);
    }

    private void setUpDatabaseTrackPoints() {

        String selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
        String[] projection = new String[]{DatabaseEntry.COL_LAT, DatabaseEntry.COL_LON};
        String[] selectionArgs = new String[]{track_id};
        //Log.v("in map view", track_id);
        String table = DatabaseEntry.TABLE_TRACK_POINTS;
        String sortOrder = DatabaseEntry.COL_ORD;

        Cursor c = factoryDB.myQuery(table, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.moveToFirst();
        do {
            ParseObject trackPoint = new ParseObject("TrackPoint");
            ParseGeoPoint coord = new ParseGeoPoint(c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LAT)), c.getDouble(c.getColumnIndex(DatabaseEntry.COL_LON)));
            trackPoint.put("coordinates", coord);
            try {
                trackPoint.pin("trackpoints");
                Log.i("setUpdatabaseParse", "adaugam puncte");
            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }
        }
        while (c.moveToNext());
    }
}
