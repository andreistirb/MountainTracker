package com.mountain.mytracker.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.Track.UserTrack;
import com.mountain.mytracker.gps.GPSLogger;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MapViewActivity extends Activity {

	private IMapController mapController;
	private static final float polylineWidth = 3.0f;
	private MapView mMapView;
	private MyLocationNewOverlay mLocationOverlay;
	private Integer userTrackId;
	private boolean has_track = false;
	private FactoryTrack factoryTrack;
    private UserTrack userTrack;
	private SharedPreferences mSharedPreferences;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
            int pointsNo;

            if (bundle != null) {
                userTrackId = bundle.getInt("userTrackId");

                try {
                    userTrack = new UserTrack(userTrackId, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mMapView.getOverlays().add(buildPolyline(context, userTrack.getTrackGeoPoints(), Color.RED));
                    pointsNo = userTrack.getTrackPointsCount();
                    if (pointsNo > 0)
                        mapController.setCenter(userTrack.getTrackGeoPoints().get(pointsNo - 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mLocationOverlay.enableMyLocation();
            }
		}
	};

	public void onCreate(Bundle savedInstanceState) {
        Integer factoryTrackId;

		super.onCreate(savedInstanceState);

        this.setContentView(R.layout.display_track_map);
		this.registerReceiver(receiver, new IntentFilter("broadcastGPS"));

		if(this.getIntent().hasExtra("factoryTrackId")){
			factoryTrackId = this.getIntent().getExtras().getInt("factoryTrackId");
			factoryTrack = new FactoryTrack(/*factoryTrackId, this.getApplicationContext()*/);
			has_track = true;
            setTitle(factoryTrack.getTrackName());
		}

		if(this.getIntent().hasExtra("userTrackId")){
			userTrackId = this.getIntent().getExtras().getInt("userTrackId");
            userTrack = new UserTrack(userTrackId, this.getApplicationContext());
            setTitle(userTrack.getTrackName());
		}

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mMapView = (MapView) this.findViewById(R.id.displaytrackmap_osmView);
		mapController = mMapView.getController();
		mLocationOverlay = new MyLocationNewOverlay(this, mMapView);

        mMapView.getOverlays().add(mLocationOverlay);
        setMap();

	}

	@Override
	public void onResume() {

        if(has_track){
            mMapView.getOverlays().add(buildPolyline(this, factoryTrack.getTrackGeoPoints(), Color.BLUE));
            mapController.setZoom(16);
            mapController.setCenter(factoryTrack.getTrackGeoPoints().get(0));
        }

        if (GPSLogger.isTracking()) {
            Log.d("in onResume MapView", "isTracking");

            if(userTrack.getTrackPointsCount() > 0) {
                Log.d("inside if ", "count>0");
                mMapView.getOverlays().add(buildPolyline(this, userTrack.getTrackGeoPoints(), Color.RED));
                mapController.setCenter(userTrack.getTrackGeoPoints().get(0));
            }
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mapview_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.mapview_menu_download_view_area : {
				CacheManager cacheManager = new CacheManager(mMapView);
				int zoomMin = mMapView.getZoomLevel();
				int zoomMax = mMapView.getZoomLevel()+4;
				cacheManager.downloadAreaAsync(this, mMapView.getBoundingBox(), zoomMin, zoomMax);
				break;
			}
			case R.id.mapview_menu_delete_view_area : {
				CacheManager cacheManager = new CacheManager(mMapView);
				int zoomMin = mMapView.getZoomLevel();
				int zoomMax = mMapView.getZoomLevel()+7;
				cacheManager.cleanAreaAsync(this, mMapView.getBoundingBox(), zoomMin, zoomMax);
				break;
			}
			case R.id.mapview_menu_tile_mapnik : {
				mMapView.setTileSource(TileSourceFactory.MAPNIK);
				item.setChecked(true);
				break;
			}
			case R.id.mapview_menu_tile_cyclemap : {
				mMapView.setTileSource(TileSourceFactory.CYCLEMAP);
				item.setChecked(true);
				break;
			}
			case R.id.mapview_menu_tile_mapquest_osm : {
				mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
				item.setChecked(true);
				break;
			}
		}
		return true;
	}

    private void setMap(){
		Integer tileSource;
        Boolean mapCompass, mapRotate;

        tileSource = Integer.parseInt(mSharedPreferences.getString("pref_key_map_tile_source_settings","2"));
        mapCompass = mSharedPreferences.getBoolean("pref_key_compass_settings",false);
        mapRotate = mSharedPreferences.getBoolean("pref_key_rotation_settings", false);

        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);

		//setting the tile source based on user settings
		switch(tileSource){
			case 0: {
				mMapView.setTileSource(TileSourceFactory.MAPNIK);
                Log.w("setting tile source","mapnik");
				break;
			}
			case 1: {
				mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
                Log.w("setting tile source","mapquest");
				break;
			}
			case 2: {
				mMapView.setTileSource(TileSourceFactory.CYCLEMAP);
                Log.w("setting tile source", "cyclemap");
				break;
			}
			default: {
				mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                Log.w("setting tile source","default");
                break;
			}
		}

        //setting the compass
        if (mapCompass == true){
                Log.w("set compass on map", "true");
                CompassOverlay mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mMapView);
                mMapView.getOverlays().add(mCompassOverlay);
        }

        //setting multi gesture map rotate
        if(mapRotate == true){
            RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, mMapView);
            mRotationGestureOverlay.setEnabled(true);
            mMapView.setMultiTouchControls(true);
            mMapView.getOverlays().add(mRotationGestureOverlay);
        }

        //setting zoom
        mapController.setZoom(16);
    }

    private Polyline buildPolyline(Context context, ArrayList<GeoPoint> trackPoints, int color){
        Polyline track = new Polyline(context);
        track.setPoints(trackPoints);
        track.setColor(color);
        track.setWidth(polylineWidth);
        return track;
    }

}
