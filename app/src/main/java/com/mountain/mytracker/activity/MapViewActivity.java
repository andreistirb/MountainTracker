package com.mountain.mytracker.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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
import org.osmdroid.views.overlay.MyLocationOverlay;

import java.util.ArrayList;

public class MapViewActivity extends Activity {

	private IMapController hartaController;
	private static final float polylineWidth = 3.0f;
	private MapView mMapView;
	private MyLocationOverlay mLocationOverlay;
	private Integer userTrackId;
	private boolean has_track;
	private FactoryTrack factoryTrack;
    private UserTrack userTrack;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
            UserTrack localUserTrack;
            int pointsNo;

            if (bundle != null) {
                userTrackId = bundle.getInt("userTrackId");

                try {
                    userTrack = new UserTrack(userTrackId, context);
                    //userTrack.fromDatabase(userTrackId, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mMapView.getOverlays().add(buildPolyline(context, userTrack.getTrackGeoPoints(), Color.RED));
                    pointsNo = userTrack.getTrackPointsCount();
                    if (pointsNo > 0)
                        hartaController.setCenter(userTrack.getTrackGeoPoints().get(pointsNo - 1));
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
        has_track = false;

        this.setContentView(R.layout.display_track_map);
		this.registerReceiver(receiver, new IntentFilter("broadcastGPS"));

		if(this.getIntent().hasExtra("factoryTrackId")){
			factoryTrackId = this.getIntent().getExtras().getInt("factoryTrackId");
			factoryTrack = new FactoryTrack(factoryTrackId, this.getApplicationContext());
			has_track = true;
            setTitle(factoryTrack.getTrackName());
		}

		if(this.getIntent().hasExtra("userTrackId")){
			userTrackId = this.getIntent().getExtras().getInt("userTrackId");
            userTrack = new UserTrack(userTrackId, this.getApplicationContext());
		}

		mMapView = (MapView) this.findViewById(R.id.displaytrackmap_osmView);
		hartaController = mMapView.getController();
		mLocationOverlay = new MyLocationOverlay(this, mMapView);

        mMapView.getOverlays().add(mLocationOverlay);
        setMap();

	}

	@Override
	public void onResume() {

        if(has_track){
            mMapView.getOverlays().add(buildPolyline(this, factoryTrack.getTrackGeoPoints(), Color.BLUE));

            hartaController.setZoom(14);
            hartaController.setCenter(factoryTrack.getTrackGeoPoints().get(0));
        }

        if (GPSLogger.isTracking()) {
            try {
                userTrack.fromDatabase(userTrackId, this);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if(userTrack.getTrackPointsCount() > 0) {
                mMapView.getOverlays().add(buildPolyline(this, userTrack.getTrackGeoPoints(), Color.RED));
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
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setTileSource(TileSourceFactory.CYCLEMAP);
        mMapView.setMultiTouchControls(true);
        hartaController.setZoom(14);
    }

    private Polyline buildPolyline(Context context, ArrayList<GeoPoint> trackPoints, int color){
        Polyline track = new Polyline(context);
        track.setPoints(trackPoints);
        track.setColor(color);
        track.setWidth(polylineWidth);
        return track;
    }

}
