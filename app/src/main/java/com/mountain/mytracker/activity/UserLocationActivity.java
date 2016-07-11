package com.mountain.mytracker.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class UserLocationActivity extends Activity {

    private MyLocationNewOverlay mMyLocationNewOverlay;
    private SharedPreferences mSharedPreferences;
    MapView mMapView;
    IMapController mMapController;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        setTitle(R.string.user_location_activity);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mMapView = (MapView) this.findViewById(R.id.user_location_mapView);
        mMyLocationNewOverlay = new MyLocationNewOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationNewOverlay);
        mMapController = mMapView.getController();

        setMap();
    }

    @Override
    public void onResume(){
        super.onResume();

        checkGPS();

        mMyLocationNewOverlay.enableMyLocation();
        mMyLocationNewOverlay.enableFollowLocation();

    }

    @Override
    public void onPause(){
        super.onPause();

        mMyLocationNewOverlay.disableMyLocation();
        mMyLocationNewOverlay.disableFollowLocation();
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
                                    startActivity(new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            }).create().show();
        }
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
        mMapController.setZoom(16);

    }

}
