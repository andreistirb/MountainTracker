package com.mountain.mytracker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class UserLocationActivity extends Activity {

    private MapView mMapView;
    private MyLocationNewOverlay mMyLocationNewOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);

        mMapView = (MapView) this.findViewById(R.id.user_location_mapView);
        mMyLocationNewOverlay = new MyLocationNewOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationNewOverlay);
    }

    @Override
    public void onResume(){
        super.onResume();

        checkGPS();

        mMyLocationNewOverlay.enableMyLocation();
    }

    @Override
    public void onPause(){
        super.onPause();

        mMyLocationNewOverlay.disableMyLocation();
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
