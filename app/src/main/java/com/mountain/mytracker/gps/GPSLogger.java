package com.mountain.mytracker.gps;

import org.osmdroid.util.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mountain.mytracker.activity.R;
import com.mountain.mytracker.activity.TrackLoggerActivity;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.NewDatabaseHelper;

public class GPSLogger extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

	//database
    private DatabaseHelper mDatabase;


    private static final String TAG = GPSLogger.class.getSimpleName();



	int mStartMode; // indicates how to behave if the service is killed
	IBinder mBinder; // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used
	private static boolean isTracking; // variabila globala care arata daca
										// serviciul este pornit sau nu
	private boolean isGPSEnabled;
	//private LocationManager mLocationManager;
	//private long lastGPSTimestamp = 0;
	//private int gpsLoggingInterval;

	private Integer mTrackNo;
	private String track_id;
	private String track_name;
	int q;
	//private Vibrator vibratie;
	//private Location old_location;
	//private boolean track_number_bool;
	//private float distance;
    private GoogleApiClient mGoogleApiClient;

	// the service is being created
	@Override
	public void onCreate() {
		Integer a;
		q = 0;

        //database
        mDatabase = new DatabaseHelper(this.getBaseContext());

        //location
        buildGoogleApiClient();

        //old implementation
		//gpsLoggingInterval = 3000;
		//a = this.gpsLoggingInterval;
		//Log.v("in gpslogger", a.toString());
		//mLocationManager = (LocationManager) this
		//		.getSystemService(Context.LOCATION_SERVICE);
		//mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		//		gpsLoggingInterval, 5, this);

		//vibratie = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		//distance = 0.0f;

		super.onCreate();
	}

	// The service is starting, due to a call to startService()
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// primeste informatii

		//vibratie.vibrate(500);
		Log.v("in gpslogger", "am primit numele");
		track_name = intent.getExtras().getString("track_name");
		mTrackNo = intent.getExtras().getInt("mTrackNo");

		q = 0;

		// incepe tracking

		startTracking();
		Log.v(TAG, "Service onStartCommand(-," + flags + "," + startId + ")");
		startForeground(1, getNotification());
		// }
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

		//mLocationManager.removeUpdates(this);
		stopNotifyBackgroundService();
	}

    @Override
    public void onConnected(Bundle connectionHint){
        mLast
    }

	private void startTracking() {
		NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nmgr.notify(1, getNotification());
		q = 0;
		Log.v("in startTracking()", "notification");
		isTracking = true;
	}

	private void stopTracking() {
		isTracking = false;
		//vibratie.vibrate(500);
		this.stopSelf();
	}

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

	/*@Override
	public void onLocationChanged(Location location) {

		float distance_to = 0;
		double altitude = Math.floor(location.getAltitude() * 1000) / 1000;
		double longitude = Math.floor(location.getLongitude() * 100000) / 100000;
		double latitude = Math.floor(location.getLatitude() * 100000) / 100000;
		double speed = (Math.floor(location.getSpeed() * 10) / 10) * 3.6;
		
		lastGPSTimestamp = location.getTime();
		
		if(q==0){
			old_location = location;
			q++;
		}
		else{
			distance_to = location.distanceTo(old_location);
			old_location = location;
		}
		
		distance_to = ((float) Math.floor(distance_to))/1000;
		distance += distance_to;

		// sendNotificationBroadcast!!!!!
		Intent notification = new Intent("broadcastGPS");
		notification.putExtra("altitude", altitude);
		notification.putExtra("mTrackNo", mTrackNo);
		notification.putExtra("latitude", latitude);
		notification.putExtra("longitude", longitude);
		notification.putExtra("speed", speed);
		notification.putExtra("time", lastGPSTimestamp);
		notification.putExtra("distance", distance);
		sendBroadcast(notification);
		Log.v("in sender", "trimit date");

	} */



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

	@Override
	public void onProviderDisabled(String provider) {
		isGPSEnabled = false;
	}

	@Override
	public void onProviderEnabled(String provider) {
		isGPSEnabled = true;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Not interested in provider status
	}

	public boolean isGpsEnabled() {
		return isGPSEnabled;
	}

	public static boolean isTracking() {
		return isTracking;
	}

	//public LocationManager getLocationManager() {
	//	return mLocationManager;
	//}



};
