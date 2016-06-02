package com.mountain.mytracker.activity;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mountain.mytracker.Track.UserTrack;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MyTrackDetailsMapFragment extends Fragment {

    private MapView mMapView;
    private UserTrack userTrack;

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
        setMap();

        if(userTrack.getTrackPointsCount() > 0) {
            mMapView.getOverlays().add(buildPolyline(getActivity().getApplicationContext(), userTrack.getTrackGeoPoints()));
            hartaController.setZoom(16);
            hartaController.setCenter(userTrack.getTrackGeoPoints().get(0));
        }

        return rootView;
    }

    private void setMap(){
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setTileSource(TileSourceFactory.CYCLEMAP);
        mMapView.setMultiTouchControls(true);
    }

    private Polyline buildPolyline(Context context, ArrayList<GeoPoint> trackPoints){
        Polyline track = new Polyline(context);
        track.setPoints(trackPoints);
        track.setColor(Color.BLUE);
        track.setWidth(3.0f);
        return track;
    }

}
