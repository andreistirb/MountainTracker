package com.mountain.mytracker.gps;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.Track.TrackPoint;
import com.mountain.mytracker.Track.UserTrack;
import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.activity.TrackLoggerActivity;
import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;

public class GPSLogger extends Service implements LocationListener {

    //Location
    private Location mCurrentLocation;
    private LocationManager mLocationManager;
    private String provider;

    private Intent notification;

    private static final String TAG = GPSLogger.class.getSimpleName();

    private static boolean isTracking; // variabila globala care arata daca
                                       // serviciul este pornit sau nu


    private Integer userTrackId;   //id-ul traseului inregistrat de user

    private Vibrator mVibrator;

    //track battery level
    private IntentFilter batteryIntentFilter;
    private Intent batteryStatus;
    private int batteryLevel;

    //implementing GeoFence
    ArrayList<Geofence> mGeofenceList;
    PendingIntent mPendingIntent;
    boolean shouldGeofence = false;

    private UserTrack userTrack;

    private Track factoryTrack;

    // the service is being created
    @Override
    public void onCreate() {

        userTrack = new UserTrack(this.getApplicationContext());

        //location
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        provider = "gps";

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


        if (intent.hasExtra("factoryTrackId")) {
            factoryTrack = new FactoryTrack(intent.getExtras().getInt("factoryTrackId"), this.getApplicationContext());
            userTrack.createDatabaseEntry(factoryTrack.getTrackId());
            shouldGeofence = true;
        }
        else
            userTrack.createDatabaseEntry(null);

        userTrackId = userTrack.getTrackId();

        notification.putExtra("userTrackId", userTrack.getTrackId());

        sendBroadcast(notification);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                Log.i("gpslogger", "inside check for build version");
                mLocationManager.requestLocationUpdates(provider, 5000, 10, this);

            }
        }
        else{
            mLocationManager.requestLocationUpdates(provider, 5000, 10, this);
        }
        // start tracking
        startTracking();

        Log.v(TAG, "Service onStartCommand(-," + flags + "," + startId + ")");
        //startForeground(1, getNotification());

        Log.v("in gpslogger", "am primit numele");

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

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cruce_galbena)
                .setContentTitle(getResources().getString(R.string.notification_ticker_text))
                .setContentText("Hello");

        Intent startTrackLoggerIntent = new Intent(this, TrackLoggerActivity.class);
        startTrackLoggerIntent.putExtra("userTrackId", userTrackId);
        if (factoryTrack != null) {
            startTrackLoggerIntent.putExtra("factoryTrackId", factoryTrack.getTrackId());
        }

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                startTrackLoggerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(resultPendingIntent);

        nmgr.notify(1, mNotificationBuilder.build());

        Log.v("in startTracking()", "notification");
        isTracking = true;
    }

    private void stopTracking() {
        isTracking = false;
        mVibrator.vibrate(500);
        userTrack.updateDatabase();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            mLocationManager.removeUpdates(this);
        }
        this.stopSelf();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){

    }

    @Override
    public void onProviderEnabled(String provider){

    }

    @Override
    public void onProviderDisabled(String provider){

    }


    @Override
    public void onLocationChanged(Location location) {
        TrackPoint mTrackPoint;
        mCurrentLocation = location;

        mTrackPoint = new TrackPoint(userTrackId, location, this.getApplicationContext());

        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mTrackPoint = new TrackPoint(
                    userTrackId,
                    location.getLatitude(), location.getLongitude(),
                    location.getAltitude(), location.getSpeed(), location.getAccuracy(),
                    location.getElapsedRealtimeNanos(), this.getApplicationContext());
            Log.i("in locationChanged", "api mai mare de 16");
        }
        else{
            mTrackPoint = new TrackPoint(
                    userTrackId,
                    location.getLatitude(), location.getLongitude(),
                    location.getAltitude(), location.getSpeed(), location.getAccuracy(),
                    location.getTime() * 1000000, this.getApplicationContext());
            Log.i("in locationChanged", "api mai mic sau egal cu 16");
        }*/

        mTrackPoint.toDatabase();
        userTrack.addTrackPoint(mTrackPoint);
        userTrack.addTrackGeoPoint(new GeoPoint(mTrackPoint.getLocation().getLatitude(), mTrackPoint.getLocation().getLongitude()));
        userTrack.updateDatabase();

        buildNotification();
        sendBroadcast(notification);

        Log.v("in sender", "trimit date");
        Log.v("in sender", String.valueOf(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)));

        batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        /*if (batteryLevel >= 40) {
            mLocationRequest.setInterval(15000);
            mLocationRequest.setFastestInterval(10000);
        } else if (batteryLevel > 20 && batteryLevel < 40) {
            mLocationRequest.setInterval(30000);
            mLocationRequest.setFastestInterval(15000);
        } else if (batteryLevel <= 20) {
            mLocationRequest.setInterval(60000);
            mLocationRequest.setFastestInterval(30000);
        }*/
        //if (shouldGeofence) { }

    }

    private void buildNotification() {

        notification.putExtra("altitude", mCurrentLocation.getAltitude());
        notification.putExtra("latitude", mCurrentLocation.getLatitude());
        notification.putExtra("longitude", mCurrentLocation.getLongitude());
        notification.putExtra("speed", mCurrentLocation.getSpeed());
        notification.putExtra("time", userTrack.getTime());
        notification.putExtra("userTrackId", userTrackId);
        notification.putExtra("distance", userTrack.getDistance());
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
        startTrackLogger.putExtra("userTrackId", userTrackId);
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

    /*private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }*/

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
