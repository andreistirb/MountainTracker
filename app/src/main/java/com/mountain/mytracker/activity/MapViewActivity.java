package com.mountain.mytracker.activity;

import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mountain.mytracker.db.DatabaseContract.DatabaseEntry;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.db.NewDatabaseHelper;
import com.mountain.mytracker.gps.GPSLogger;

public class MapViewActivity extends Activity {

    private String numeTraseu;
	private String traseu_id;
	private MapView harta;
	private IMapController hartaController;
	private ArrayList<GeoPoint> track;
	private ArrayList<GeoPoint> mTrack;
	private MyLocationOverlay mLocationOverlay;
	private NewDatabaseHelper db;
	private DatabaseHelper mDatabase;
	private Integer mTrackNo;
	private boolean has_track;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();

			mTrackNo = bundle.getInt("mTrackNo");

			GeoPoint curent = new GeoPoint(bundle.getDouble("latitude"),
					bundle.getDouble("longitude"));
			mTrack.add(curent);

            harta.getOverlays().add(buildPolyline(context,mTrack,Color.RED,3.0f));
			hartaController.setCenter(curent);
			mLocationOverlay.enableMyLocation();
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        has_track = false;

        this.setContentView(R.layout.display_track_map);
		this.registerReceiver(receiver, new IntentFilter("broadcastGPS"));

		numeTraseu = this.getIntent().getExtras().getString("track_name");
		if(this.getIntent().hasExtra("track_id")){
			traseu_id = this.getIntent().getExtras().getString("track_id");
			has_track = true;
		}
		if(this.getIntent().hasExtra("mTrackNo")){
			mTrackNo = this.getIntent().getExtras().getInt("mTrackNo");
		}

        mTrack = new ArrayList<GeoPoint>();
		harta = (MapView) this.findViewById(R.id.displaytrackmap_osmView);
		hartaController = (MapController) harta.getController();
		mLocationOverlay = new MyLocationOverlay(this,harta);
		
		/* ca sa aducem punctele traseului din baza de date */
		db = new NewDatabaseHelper(this);
		mDatabase = new DatabaseHelper(this);

        harta.getOverlays().add(mLocationOverlay);
        setMap();
        setTitle(numeTraseu);

	}

	@Override
	public void onResume() {

        if(has_track){

            // cautam punctele traseului
            String selection = DatabaseEntry.COL_TRACK_ID + " = ? ";
            String[] selectionArgs = new String[] { traseu_id };
            Log.v("in map view", traseu_id);
            String table = DatabaseEntry.TABLE_TRACK_POINTS;
            String sortOrder = DatabaseEntry.COL_ORD;

            Cursor c = db.myQuery(table, null, selection, selectionArgs, null,
                    null, sortOrder);
            track = buildGeoPoint(c);
            harta.getOverlays().add(buildPolyline(this,track,Color.BLUE,3.0f));
            hartaController.setZoom(14);
            hartaController.setCenter(track.get(0));
        }

        if (GPSLogger.isTracking()) {
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(DatabaseEntry.TABLE_MY_TRACKS_POINTS);
			Cursor c = qb.query(mDatabase.getReadableDatabase(), null,
					DatabaseEntry.COL_TRACK_NO + " = ? ",
					new String[] { mTrackNo.toString() }, null, null,
					DatabaseEntry._ID);
			c.moveToFirst();
			if(c.getCount() > 0){
				Integer x = c.getCount();
				Log.v("in mapViewActivitysid", x.toString());
				do {
					mTrack.add(new GeoPoint(c.getDouble(c
							.getColumnIndex(DatabaseEntry.COL_LAT)), c.getDouble(c
							.getColumnIndex(DatabaseEntry.COL_LON))));
				} while (c.moveToNext());
				
			}
			else{
				Integer x = c.getCount();
				Log.v("in mapViewActivity", x.toString());
			}
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

    private void setMap(){
        harta.setClickable(true);
        harta.setBuiltInZoomControls(true);
        harta.setTileSource(TileSourceFactory.CYCLEMAP);
        harta.setMultiTouchControls(true);
        hartaController.setZoom(14);
    }

    private Polyline buildPolyline(Context context, ArrayList<GeoPoint> trackPoints, int color, float width){
        Polyline track = new Polyline(context);
        track.setPoints(trackPoints);
        track.setColor(color);
        track.setWidth(width);
        return track;
    }

	// gets track points from database and builds an ArrayList of GeoPoints
	private ArrayList<GeoPoint> buildGeoPoint(Cursor c) {
		ArrayList<GeoPoint> traseu = new ArrayList<GeoPoint>();
		c.moveToFirst();
		do {
			double latitude = c.getDouble(c
					.getColumnIndex(DatabaseEntry.COL_LAT));
			double longitude = c.getDouble(c
					.getColumnIndex(DatabaseEntry.COL_LON));
			traseu.add(new GeoPoint(latitude, longitude));
		} while (c.moveToNext());

		return traseu;
	}

}
