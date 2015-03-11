package com.mountain.mytracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.gps.GPSLogger;

public class TrackLoggerActivity extends Activity {

	private LocationManager lm;
	private Intent MapViewActivityIntent, TrackDetailsActivityIntent, GPSLoggerServiceIntent;
	private GPSLogger gpsLogger;
	boolean GPSflag = false;
	private Button start, stop, harta, detalii;
	private TextView alt, lat, lon, speed, dist, timp;
	private String traseu;
	private String traseu_id;
	private Context context;
	private DatabaseHelper mDatabase;
	private Integer mTrackNo;
	private Double avg_speed, max_speed;
	private Float distance;
	public boolean detalii_btn; //daca sa apara sau nu butonul detalii
	private boolean service_started;
	private Double max_alt, min_alt;
	private long duration, start_time;
	private int counter;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Double altitude = bundle.getDouble("altitude");
				Double longitude = bundle.getDouble("longitude");
				Double latitude = bundle.getDouble("latitude");
				Double speeds = bundle.getDouble("speed");
				Long time = bundle.getLong("time");
				counter++;
				if (duration == 0){
					start_time = time;
					duration = 1;
				}
				else{
					duration = time - start_time;
					duration /= 1000;
				}
				distance = bundle.getFloat("distance");
				if(speeds.compareTo(max_speed) > 0){
					max_speed = speeds;
				}
				if(altitude < min_alt){
					min_alt = altitude;
				}
				if(max_alt < altitude){
					max_alt = altitude;
				}
				avg_speed += speeds;
				alt.setText(altitude.toString());
				lon.setText(longitude.toString());
				lat.setText(latitude.toString());
				speed.setText(speeds.toString());
				dist.setText(distance.toString());
				
				//aici mai setez sa arate timpul parcurs
				timp.setText(String.format("%d:%02d:%02d", duration/3600, (duration%3600)/60, (duration%60)));
				insertLocation(latitude, longitude, altitude);
				Log.v("In receiver", "am primit date");
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		service_started = false;
		context = this;
		checkGPS();

		this.setContentView(R.layout.track_logger_layout);

		traseu = this.getIntent().getExtras().getString("track_name");
		if(this.getIntent().hasExtra("track_id")){
			traseu_id = this.getIntent().getExtras().getString("track_id");
		}
		this.setTitle(traseu);
		if(this.getIntent().hasExtra("detalii")){
			detalii_btn = this.getIntent().getExtras().getBoolean("detalii");
		}
		else {
			detalii_btn = false;
		}
		if(this.getIntent().hasExtra("mTrackNo")){
			mTrackNo = this.getIntent().getExtras().getInt("mTrackNo");
			service_started = true;
		}

		mDatabase = new DatabaseHelper(this);

		GPSLoggerServiceIntent = new Intent(this, GPSLogger.class);
		MapViewActivityIntent = new Intent(this, MapViewActivity.class);
		TrackDetailsActivityIntent = new Intent(this, TrackDetailsActivity.class);
		alt = (TextView) this.findViewById(R.id.track_logger_alt);
		lat = (TextView) this.findViewById(R.id.track_logger_lat);
		lon = (TextView) this.findViewById(R.id.track_logger_lon);
		speed = (TextView) this.findViewById(R.id.track_logger_speed);
		dist = (TextView) this.findViewById(R.id.track_logger_distance);
		timp = (TextView) this.findViewById(R.id.track_logger_duration);

		this.registerReceiver(receiver, new IntentFilter("broadcastGPS"));
		
		
	}

	public void onResume() {

		start = (Button) this.findViewById(R.id.track_logger_start);
		harta = (Button) this.findViewById(R.id.track_logger_map);
		stop = (Button) this.findViewById(R.id.track_logger_stop);
		detalii = (Button) this.findViewById(R.id.track_logger_details);

		this.setTitle(traseu);
		this.registerReceiver(receiver, new IntentFilter("broadcastGPS"));
		if(detalii_btn){
			detalii.invalidate();
			Log.v("in trackLogger", "stergem butonul de detalii");
		}
		else {
			detalii.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					TrackDetailsActivityIntent.putExtra("track_name", traseu);
					TrackDetailsActivityIntent.putExtra("track_id", traseu_id);
					context.startActivity(TrackDetailsActivityIntent);
				}
			});
		}
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// verifica daca sunt trasee inregistrate;
				// daca nu sunt, pune contorul pe 1
				// iar daca sunt, incrementeaza contorul de la ultima valoare
				if(!service_started){
					if (!checkEmptyDatabase(mDatabase)) {
						mTrackNo = 1;
					} else {
						mTrackNo = getTrackNo(mDatabase) + 1;
					}
					createEntry(mTrackNo);
					GPSLoggerServiceIntent.putExtra("mTrackNo", mTrackNo);
				
					avg_speed = 0.0;
					max_speed = 0.0;
					distance = 0.0f;
					duration = 0;
					max_alt = 0.0;
					min_alt = 9999.0;
					counter = 0;

					Log.v("in trackloggeractivity", "click");
				
					GPSLoggerServiceIntent.putExtra("track_name", traseu);
				
					service_started = true;
					startService(GPSLoggerServiceIntent);
				}
			}
		});

		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				service_started = false;
				
				updateDatabase();
				
				stopService(GPSLoggerServiceIntent);
			}
		});

		harta.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MapViewActivityIntent.putExtra("track_name", traseu);
				if(!detalii_btn){
					MapViewActivityIntent.putExtra("track_id", traseu_id);
				}
				MapViewActivityIntent.putExtra("mTrackNo", mTrackNo);
				context.startActivity(MapViewActivityIntent);
			}
		});
		
		

		super.onResume();

	}

	public void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		try {
			unregisterReceiver(receiver);
		}
		catch(RuntimeException e){
			
		}
		super.onDestroy();
	}

	public void checkGPS() {
		lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// GPS isn't enabled. Offer user to go enable it
			new AlertDialog.Builder(this)
					.setTitle(R.string.track_logger_gps_disabled)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(
							getResources().getString(
									R.string.track_logger_gps_disabled_hint))
					.setCancelable(true)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									GPSflag = true;
									startActivity(new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									GPSflag = false;
									dialog.cancel();
								}
							}).create().show();
		}
	}

	public GPSLogger getGPSLogger() {

		return this.gpsLogger;
	}

	public void setGPSLogger(GPSLogger l) {
		this.gpsLogger = l;
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
				new String[] { "max(" + DatabaseEntry.COL_TRACK_NO + ")" },
				null, null, null, null, null);
		c.moveToFirst();
		return c.getInt(c.getColumnIndex("max(" + DatabaseEntry.COL_TRACK_NO
				+ ")"));
	}
	
	public void createEntry(int mTrackNo){
		ContentValues row = new ContentValues();
		row.put(DatabaseEntry.COL_TRACK_NAME, traseu);
		row.put(DatabaseEntry.COL_TRACK_NO, mTrackNo);
		row.put(DatabaseEntry.COL_TRACK_ID, traseu_id);
		mDatabase.getWritableDatabase().insert(DatabaseEntry.TABLE_MY_TRACKS, null, row);
		Integer x = mTrackNo;
		Log.v("cand creeaza o intrare in Db", x.toString());
		mDatabase.close();
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
	
	public void updateDatabase(){
		ContentValues row = new ContentValues();
		row.put(DatabaseEntry.COL_MAX_SPEED, this.max_speed);
		row.put(DatabaseEntry.COL_MED_SPEED, Math.floor((this.avg_speed/counter) * 10)/10);
		row.put(DatabaseEntry.COL_DISTANCE, this.distance);
		row.put(DatabaseEntry.COL_TIME, this.duration);
		row.put(DatabaseEntry.COL_TRACK_MAX_ALT, this.max_alt);
		row.put(DatabaseEntry.COL_TRACK_MIN_ALT, this.min_alt);
		mDatabase.getWritableDatabase().update(DatabaseEntry.TABLE_MY_TRACKS, row, DatabaseEntry.COL_TRACK_NO + " = " + mTrackNo, null);
		Log.v("in trackLogger", "s-o updatat baza de date");
	}

}
