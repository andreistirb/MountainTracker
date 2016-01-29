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

import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MyTrackDetailsMapFragment extends Fragment {

    private Integer mTrackNo;
    private DatabaseHelper db;
    private MapView harta;
    private IMapController hartaController;
    private ArrayList<GeoPoint> track;

    public MyTrackDetailsMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrackNo = getArguments().getInt("track_id");
            db = new DatabaseHelper(getActivity().getApplicationContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.mytrackdetails_map_fragment, container, false);
        harta = (MapView) rootView.findViewById(R.id.mytrackdetails_mapview);
        hartaController = harta.getController();
        setMap();

        String selection = DatabaseContract.DatabaseEntry.COL_TRACK_NO + " = ? ";
        String[] selectionArgs = new String[] { mTrackNo.toString() };
        Log.v("in map view", mTrackNo.toString());
        String table = DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS_POINTS;
        String sortOrder = DatabaseContract.DatabaseEntry.COL_ORD;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        Cursor c = qb.query(db.getReadableDatabase(), null, selection, selectionArgs, null,
                null, sortOrder);
        if(c.getCount() > 0) {
            track = buildGeoPoint(c);
            harta.getOverlays().add(buildPolyline(getActivity().getApplicationContext(), track, Color.BLUE, 3.0f));
            hartaController.setZoom(14);
            hartaController.setCenter(track.get(0));
        }

        return rootView;
    }

    private void setMap(){
        harta.setClickable(true);
        harta.setBuiltInZoomControls(true);
        harta.setTileSource(TileSourceFactory.CYCLEMAP);
        harta.setMultiTouchControls(true);
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
        ArrayList<GeoPoint> traseu = new ArrayList<>();
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
    }

}
