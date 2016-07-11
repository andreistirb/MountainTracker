package com.mountain.mytracker.activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mountain.mytracker.Track.UserTrack;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;

public class MyTrackDetailsMapFragment extends Fragment {

    private MapView mMapView;
    private UserTrack userTrack;
    private SharedPreferences mSharedPreferences;

    public MyTrackDetailsMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Integer userTrackId;

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userTrackId = getArguments().getInt("userTrackId");
            userTrack = new UserTrack(userTrackId, this.getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        IMapController hartaController;

        View rootView = inflater.inflate(R.layout.mytrackdetails_map_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mytrackdetails_mapview);
        hartaController = mMapView.getController();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        setMap();

        if(userTrack.getTrackPointsCount() > 0) {
            mMapView.getOverlays().add(buildPolyline(getActivity().getApplicationContext(), userTrack.getTrackGeoPoints()));
            hartaController.setZoom(16);
            hartaController.setCenter(userTrack.getTrackGeoPoints().get(0));
        }

        return rootView;
    }

    private void setMap(){
        Integer tileSource;
        Boolean mapCompass, mapRotate;

        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        tileSource = Integer.parseInt(mSharedPreferences.getString("pref_key_map_tile_source_settings","2"));
        mapCompass = mSharedPreferences.getBoolean("pref_key_compass_settings",false);
        mapRotate = mSharedPreferences.getBoolean("pref_key_rotation_settings", false);

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
            CompassOverlay mCompassOverlay = new CompassOverlay(this.getContext(), new InternalCompassOrientationProvider(this.getContext()), mMapView);
            mMapView.getOverlays().add(mCompassOverlay);
        }

        //setting multi gesture map rotate
        if(mapRotate == true){
            RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this.getContext(), mMapView);
            mRotationGestureOverlay.setEnabled(true);
            mMapView.setMultiTouchControls(true);
            mMapView.getOverlays().add(mRotationGestureOverlay);
        }

        //setting zoom
        //hartaController.setZoom(14);
    }

    private Polyline buildPolyline(Context context, ArrayList<GeoPoint> trackPoints){
        Polyline track = new Polyline(context);
        track.setPoints(trackPoints);
        track.setColor(Color.BLUE);
        track.setWidth(3.0f);
        return track;
    }

}
