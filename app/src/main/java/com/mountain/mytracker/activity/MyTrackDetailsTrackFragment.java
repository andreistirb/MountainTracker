package com.mountain.mytracker.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseHelper;
import com.mountain.mytracker.other.GPXExport;

public class MyTrackDetailsTrackFragment extends Fragment {

    private Integer mTrackNo;
    private DatabaseHelper db;
    Button export_btn;
    private String track_name;

    public MyTrackDetailsTrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrackNo = getArguments().getInt("track_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TextView duration, distance, avg_speed, max_speed, min_alt, max_alt;

        View rootView = inflater.inflate(R.layout.mytrackdetails_track_fragment, container, false);

        duration = (TextView) rootView.findViewById(R.id.mytrackdetails_duration);
        distance = (TextView) rootView.findViewById(R.id.mytrackdetails_distance);
        avg_speed = (TextView) rootView.findViewById(R.id.mytrackdetails_avg_speed);
        max_speed = (TextView) rootView.findViewById(R.id.mytrackdetails_max_speed);
        min_alt = (TextView) rootView.findViewById(R.id.mytrackdetails_min_alt);
        max_alt = (TextView) rootView.findViewById(R.id.mytrackdetails_max_alt);
        export_btn = (Button) rootView.findViewById(R.id.mytrackdetails_export);

        if (getArguments() != null) {

            db = new DatabaseHelper(getActivity().getApplicationContext());

            String table = DatabaseContract.DatabaseEntry.TABLE_MY_TRACKS;

            Cursor c = db.getReadableDatabase().query(table, null, null, null, null, null, null);
            c.moveToFirst();


            if (c.move(mTrackNo - 1)) {

                Integer durationi = c.getInt(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TIME));

                duration.setText(String.format("%d:%02d:%02d", durationi / 3600, (durationi % 3600) / 60, (durationi % 60)));
                distance.setText(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_DISTANCE)));
                avg_speed.setText(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MED_SPEED)));
                max_speed.setText(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MAX_SPEED)));
                min_alt.setText(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_MIN_ALT)));
                max_alt.setText(c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_MAX_ALT)));

                Log.v("in mytrackdetails", c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_NO)));

                track_name = c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_TRACK_NAME));


                export_btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        GPXExport gpx = new GPXExport();
                        gpx.createFile(mTrackNo, mTrackNo.toString() + "track.gpx", track_name, db);
                        Toast.makeText(getActivity(), "Export successful",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        return rootView;
    }
}
