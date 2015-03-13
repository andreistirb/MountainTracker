package com.mountain.mytracker.gps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.activity.TrackLoggerActivity;
import com.mountain.mytracker.db.DatabaseHelper;

import java.text.DateFormat;
import java.util.Date;

public class GPSLogger extends Service implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

	//database
    private DatabaseHelper mDatabase;

    //Google Api
    protected GoogleApiClient mGoogleApiClient;

    //Location
    protected Location mLastLocation;
    protected Location mOldLocation;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;

    private float distance;

    protected String mLastUpdateTime;

    private Intent notification;

    private static final String TAG = GPSLogger.class.getSimpleName();



	int mStartMode; // indicates how to behave if the service is killed
	IBinder mBinder; // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used
	private static boolean isTracking; // variabila globala care arata daca
										// serviciul este pornit sau nu
	private boolean isGPSEnabled;

	private Integer mTrackNo;
	private String track_name;

    int firstLocation; //pentru a determina prima locatie

	private Vibrator mVibrator;

	// the service is being created
	@Override
	public void onCreate() {
		firstLocation = 0;

        //database
        mDatabase = new DatabaseHelper(this.getBaseContext());

        //location
        buildGoogleApiClient();
        distance = 0.0f;

        //Vibrator
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


		super.onCreate();
	}

	// The service is starting, due to a call to startService()
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {


        mGoogleApiClient.connect();

		// primeste informatii
		track_name = intent.getExtras().getString("track_name");
		mTrackNo = intent.getExtras().getInt("mTrackNo");
        Log.v("in gpslogger", "am primit numele");



		// incepe tracking

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

        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }

		stopNotifyBackgroundService();
	}


	private void startTracking() {

        mVibrator.vibrate(500);
		NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nmgr.notify(1, getNotification());

        firstLocation = 0;

        //startLocationUpdates();

		Log.v("in startTracking()", "notification");
		isTracking = true;
	}

	private void stopTracking() {
        stopLocationUpdates();
		isTracking = false;
		mVibrator.vibrate(500);
		this.stopSelf();
	}


    //Google API - Location


    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint){
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result){
        Log.i(TAG, "Connection failed : ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause){
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.

        Log.i(TAG, "Connection suspended, trying to reconnect");
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    //Location Request
    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location){
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        computeDistance(location);
        buildNotification();
        sendBroadcast(notification);
        Log.v("in sender", "trimit date");
    }

    private void buildNotification(){
        notification = new Intent("broadcastGPS");
        notification.putExtra("altitude", mCurrentLocation.getAltitude());
        notification.putExtra("latitude", mCurrentLocation.getLatitude());
        notification.putExtra("longitude", mCurrentLocation.getLongitude());
        notification.putExtra("speed", mCurrentLocation.getSpeed());
        notification.putExtra("time", mCurrentLocation.getTime());
        notification.putExtra("mTrackNo", mTrackNo);
        notification.putExtra("distance", distance);
    }

    private void computeDistance(Location location){
        float distance_to = 0;
        if(firstLocation == 0){
            mOldLocation = location;
            firstLocation++;
        }
        else{
            distance_to = location.distanceTo(mOldLocation);
            mOldLocation = location;
        }

        distance_to = ((float) Math.floor(distance_to) / 1000);
        distance += distance_to;
    }

	private Notification getNotification() {
		Notification n = new Notification(R.drawable.cruce_galbena,
				getResources().getString(R.string.notification_ticker_text),
				System.currentTimeMillis());

		Intent startTrackLogger = new Intent(this, TrackLoggerActivity.class);
		startTrackLogger.putExtra("track_name", track_name);
		startTrackLogger.putExtra("mTrackNo", mTrackNo);
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


	public boolean isGpsEnabled() {
		return isGPSEnabled;
	}

	public static boolean isTracking() {
		return isTracking;
	}
};
