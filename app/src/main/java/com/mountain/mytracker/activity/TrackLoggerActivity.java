package com.mountain.mytracker.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.gps.GPSLogger;

public class TrackLoggerActivity extends Activity {

	private Intent MapViewActivityIntent, TrackDetailsActivityIntent, GPSLoggerServiceIntent, TrackerManagerActivityIntent;
	boolean GPSflag = false;
	private Integer factoryTrackId, userTrackId;
    private ImageButton harta,detalii,trekking, start, stop;
	private TextView alt, lat, lon, speed, dist, timp;
	private Context context;
	public boolean detalii_btn; //daca sa apara sau nu butonul detalii

    private FactoryTrack factoryTrack;

    //track data
    long time;
    float max_speed, avg_speed, distance;
    //double max_alt,min_alt;

    private Activity activity;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Double altitude = bundle.getDouble("altitude");
				Double longitude = bundle.getDouble("longitude");
				Double latitude = bundle.getDouble("latitude");
				Float speeds = bundle.getFloat("speed") * 3.6F;
				time = bundle.getLong("time") / 1000000000;
                distance = bundle.getFloat("distance") / 1000;
                max_speed = bundle.getFloat("max_speed");
                avg_speed = bundle.getFloat("avg_speed");
                //max_alt = bundle.getDouble("max_alt");
                //min_alt = bundle.getDouble("min_alt");
                listDetails(altitude, latitude, longitude, speeds);
				Log.v("In receiver", "am primit date");
                userTrackId = bundle.getInt("userTrackId");
			}
		}
	};

    private void listDetails(Double altitude, Double latitude, Double longitude, Float speeds){
        dist.setText(Double.toString(Math.floor(distance * 100) / 100));
        alt.setText(Double.toString(Math.round(altitude)));
        lon.setText(Double.toString(Math.floor(longitude * 10000) / 10000));
        lat.setText(Double.toString(Math.floor(latitude * 10000) / 10000));
        this.speed.setText(Double.toString(Math.floor(speeds * 100) / 100));
        timp.setText(String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60)));
    }

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this.getBaseContext();
        activity = this;
		checkGPS();

		this.setContentView(R.layout.track_logger_layout);

		if(this.getIntent().hasExtra("factoryTrackId")){
			factoryTrackId = this.getIntent().getExtras().getInt("factoryTrackId");
            factoryTrack = new FactoryTrack(factoryTrackId, this.getApplicationContext());
            setTitle(factoryTrack.getTrackName());
		}

		detalii_btn = this.getIntent().hasExtra("detalii") && this.getIntent().getExtras().getBoolean("detalii");

		if(this.getIntent().hasExtra("userTrackId")){
			userTrackId = this.getIntent().getExtras().getInt("userTrackId");
		}


		GPSLoggerServiceIntent = new Intent(this, GPSLogger.class);
		MapViewActivityIntent = new Intent(this, MapViewActivity.class);
		TrackDetailsActivityIntent = new Intent(this, TrackDetailsActivity.class);
        TrackerManagerActivityIntent = new Intent(this, TrackerManagerActivity.class);

		alt = (TextView) this.findViewById(R.id.track_logger_alt);
		lat = (TextView) this.findViewById(R.id.track_logger_lat);
		lon = (TextView) this.findViewById(R.id.track_logger_lon);
		speed = (TextView) this.findViewById(R.id.track_logger_speed);
		dist = (TextView) this.findViewById(R.id.track_logger_distance);
		timp = (TextView) this.findViewById(R.id.track_logger_duration);

        start = (ImageButton) this.findViewById(R.id.track_logger_start);
        harta = (ImageButton) this.findViewById(R.id.track_logger_map);
        stop = (ImageButton) this.findViewById(R.id.track_logger_stop);
        detalii = (ImageButton) this.findViewById(R.id.track_logger_details);
        trekking = (ImageButton) this.findViewById(R.id.track_logger_trekking);
		
	}

    //TO-DO should add onSavedInstanceState...

	public void onResume() {


		this.registerReceiver(receiver, new IntentFilter("broadcastGPS"));

		if(detalii_btn){
			detalii.invalidate();
			Log.v("in trackLogger", "stergem butonul de detalii");
		}
		else {
			detalii.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (factoryTrack != null) {
                        TrackDetailsActivityIntent.putExtra("factoryTrackId", factoryTrackId);
                        activity.startActivity(TrackDetailsActivityIntent);
                    }
                }
            });
		}
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

                Log.v("in trackloggeractivity", "click");
				
                if(factoryTrack != null)
				    GPSLoggerServiceIntent.putExtra("factoryTrackId", factoryTrackId);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    startService(GPSLoggerServiceIntent);
                }
                else{
                    startService(GPSLoggerServiceIntent);
                }
			}
		});

		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopService(GPSLoggerServiceIntent);
			}
		});

		harta.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
                if(factoryTrack != null)
                    MapViewActivityIntent.putExtra("factoryTrackId", factoryTrackId);
                if(userTrackId != null) {
                    MapViewActivityIntent.putExtra("userTrackId", userTrackId);
                    Log.d("onClick harta", userTrackId.toString());
                }
				activity.startActivity(MapViewActivityIntent);
			}
		});

        trekking.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                activity.startActivity(TrackerManagerActivityIntent);
            }
        });

		super.onResume();
	}

	public void onPause() {
        try{
            unregisterReceiver(receiver);
        }
		catch (RuntimeException e){
            e.printStackTrace();
        }
		super.onPause();
	}

	@Override
	public void onDestroy() {
        try {
            unregisterReceiver(receiver);
		}
		catch(RuntimeException e){
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public void checkGPS() {
		LocationManager lm;
		lm = (LocationManager) this.getSystemService(TrackLoggerActivity.LOCATION_SERVICE);

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

}
