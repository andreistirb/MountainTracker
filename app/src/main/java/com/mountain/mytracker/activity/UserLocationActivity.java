package com.mountain.mytracker.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class UserLocationActivity extends Activity {

    private MyLocationNewOverlay mMyLocationNewOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        MapView mMapView;
        IMapController mMapController;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        setTitle(R.string.user_location_activity);

        mMapView = (MapView) this.findViewById(R.id.user_location_mapView);
        mMyLocationNewOverlay = new MyLocationNewOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationNewOverlay);
        mMapController = mMapView.getController();

        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapController.setZoom(16);
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

}
