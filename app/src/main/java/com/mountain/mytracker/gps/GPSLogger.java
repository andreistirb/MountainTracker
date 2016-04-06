package com.mountain.mytracker.gps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.Track.TrackPoint;
import com.mountain.mytracker.Track.UserTrack;

import org.osmdroid.util.GeoPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GPSLogger extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    //database

    //Google Api
    private GoogleApiClient mGoogleApiClient;

    //Location
    protected Location mLastLocation;
    private Location mOldLocation;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;

    private float distance;

    private String mLastUpdateTime;
    private boolean allowSendingNotifications = false;
    private ArrayList<GeoPoint> trackPoints;


    private Intent notification;

    private static final String TAG = GPSLogger.class.getSimpleName();

    int mStartMode; // indicates how to behave if the service is killed
    IBinder mBinder; // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    private static boolean isTracking; // variabila globala care arata daca
    // serviciul este pornit sau nu
    private boolean isGPSEnabled;

    private Integer mTrackId;   //id-ul traseului inregistrat de user


    private int trackPointsCount; //pentru a determina prima locatie

    private Vibrator mVibrator;

    //track battery level
    private IntentFilter batteryIntentFilter;
    private Intent batteryStatus;
    private int batteryLevel;

    //track data
    private long time, first_fix;
    private float max_speed, sum_speed, avg_speed;
    private double max_alt, min_alt;

    //implementing GeoFence
    ArrayList<Geofence> mGeofenceList;
    PendingIntent mPendingIntent;
    boolean shouldGeofence = false;

    private UserTrack userTrack;

    private Track factoryTrack;

    // the service is being created
    @Override
    public void onCreate() {

        /*mGeofenceList = new ArrayList<Geofence>();*/
        userTrack = new UserTrack(this.getApplicationContext());

        //location
        buildGoogleApiClient();
        distance = 0.0f;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Vibrator
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Battery
        batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, batteryIntentFilter);

        notification = new Intent("broadcastGPS");

        super.onCreate();
    }

    // The service is starting, due to a call to startService()
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient.connect();


        if (intent.hasExtra("factoryTrackId")) {
            factoryTrack = new FactoryTrack(intent.getExtras().getInt("factoryTrackId"), this.getApplicationContext());
            userTrack.createDatabaseEntry(factoryTrack.getTrackId());
            shouldGeofence = true;
        }
        else
            userTrack.createDatabaseEntry(null);

        notification.putExtra("mTrackId", userTrack.getTrackId());
        sendBroadcast(notification);

        Log.v("in gpslogger", "am primit numele");

        // start tracking
        startTracking();

        Log.v(TAG, "Service onStartCommand(-," + flags + "," + startId + ")");
        //startForeground(1, getNotification());

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
        //NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //nmgr.notify(1, getNotification());

        Log.v("in startTracking()", "notification");
        isTracking = true;
    }

    private void stopTracking() {
        stopLocationUpdates();
        isTracking = false;
        mVibrator.vibrate(500);
        if (shouldGeofence) {
        }
        userTrack.updateDatabase();
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
        //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        TrackPoint mTrackPoint;
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        buildNotification();

        mTrackPoint = new TrackPoint(mTrackId, location.getLatitude(), location.getLongitude(),
                location.getAltitude(), location.getSpeed(), location.getAccuracy(),
                location.getElapsedRealtimeNanos(), this.getApplicationContext());

        mTrackPoint.toDatabase();
        userTrack.addTrackPoint(mTrackPoint);
        userTrack.addTrackGeoPoint(new GeoPoint(mTrackPoint.getLatitude(), mTrackPoint.getLongitude()));

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
        //if (shouldGeofence) { }

    }

    private void buildNotification() {

        notification.putExtra("altitude", mCurrentLocation.getAltitude());
        notification.putExtra("latitude", mCurrentLocation.getLatitude());
        notification.putExtra("longitude", mCurrentLocation.getLongitude());
        notification.putExtra("speed", mCurrentLocation.getSpeed());
        //notification.putExtra("time", time);
        notification.putExtra("mTrackId", mTrackId);
        //notification.putExtra("distance", distance);
        //notification.putExtra("max_speed", max_speed);
        //notification.putExtra("avg_speed", avg_speed);
        //notification.putExtra("max_alt", max_alt);
        //notification.putExtra("min_alt", min_alt);
    }

    /*private Notification getNotification() {
        Notification n = new Notification(R.drawable.cruce_galbena,
                getResources().getString(R.string.notification_ticker_text),
                System.currentTimeMillis());

        Intent startTrackLogger = new Intent(this, TrackLoggerActivity.class);
        startTrackLogger.putExtra("track_name", );
        startTrackLogger.putExtra("mTrackId", mTrackId);
        if (factoryTrack != null) {
            startTrackLogger.putExtra("factoryTrackId", factoryTrack.getTrackId());
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
    }*/

    private void stopNotifyBackgroundService() {
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.cancel(1);
    }

    public static boolean isTracking() {
        return isTracking;
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    /*private void addGeofences() {
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
    }*/

    /*private void removeGeofences() {
        try {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
    }*/

    @Override
    public void onResult(Status status) {

    }

    private PendingIntent getGeofencePendingIntent() {
        if (mPendingIntent != null)
            return mPendingIntent;
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*private void addGeofence(GeoPoint point) {

        Geofence mGeofence = new Geofence.Builder()
                .setRequestId(String.valueOf(point.getLatitude()))//String.valueOf(mGeofenceList.size()))//point.getLatitude() + point.getLongitude()))
                        .setCircularRegion(point.getLatitude(), point.getLongitude(), 50) //50 meters
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build();

        if (!mGeofenceList.contains(mGeofence))
            mGeofenceList.add(mGeofence);
    }*/
}
