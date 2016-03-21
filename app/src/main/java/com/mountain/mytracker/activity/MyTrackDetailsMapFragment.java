package com.mountain.mytracker.activity;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mountain.mytracker.Track.UserTrack;
import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MyTrackDetailsMapFragment extends Fragment {

    private Integer userTrackId;
    //private DatabaseHelper db;
    private MapView harta;
    private UserTrack userTrack;

    public MyTrackDetailsMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userTrackId = getArguments().getInt("userTrackId");
            userTrack = new UserTrack(userTrackId, this.getContext());
            //db = new DatabaseHelper(getActivity().getApplicationContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        IMapController hartaController;
        ArrayList<GeoPoint> track;

        View rootView = inflater.inflate(R.layout.mytrackdetails_map_fragment, container, false);
        harta = (MapView) rootView.findViewById(R.id.mytrackdetails_mapview);
        hartaController = harta.getController();
        setMap();


        /*String selection = DatabaseContract.DatabaseEntry.COL_TRACK_NO + " = ? ";
        String[] selectionArgs = new String[] { userTrackId.toString() };
        Log.v("in map view", userTrackId.toString());
        String table = DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS_POINTS;
        String sortOrder = DatabaseContract.DatabaseEntry.COL_ORD;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        Cursor c = qb.query(db.getReadableDatabase(), null, selection, selectionArgs, null,
                null, sortOrder);*/
        if(userTrack.getTrackPointsCount() > 0) {
            harta.getOverlays().add(buildPolyline(getActivity().getApplicationContext(), userTrack.getTrackGeoPoints()));
            hartaController.setZoom(14);
            hartaController.setCenter(userTrack.getTrackGeoPoints().get(0));
        }

        return rootView;
    }

    private void setMap(){
        harta.setClickable(true);
        harta.setBuiltInZoomControls(true);
        harta.setTileSource(TileSourceFactory.CYCLEMAP);
        harta.setMultiTouchControls(true);
    }

    private Polyline buildPolyline(Context context, ArrayList<GeoPoint> trackPoints){
        Polyline track = new Polyline(context);
        track.setPoints(trackPoints);
        track.setColor(Color.BLUE);
        track.setWidth(3.0f);
        return track;
    }

    // gets track points from database and builds an ArrayList of GeoPoints
    /*private ArrayList<GeoPoint> buildGeoPoint(Cursor c) {
        ArrayList<GeoPoint> traseu = new ArrayList<GeoPoint>();
        c.moveToFirst();
        do {
            double latitude = c.getDouble(c
                    .getColumnIndex(DatabaseContract.DatabaseEntry.COL_LAT));
            double longitude = c.getDouble(c
                    .getColumnIndex(DatabaseContract.DatabaseEntry.COL_LON));
            traseu.add(new GeoPoint(latitude, longitude));
            Log.i("face vectoru de pct",String.valueOf(latitude));
        } while (c.moveToNext());

        return traseu;
    }*/

}
