package com.mountain.mytracker.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mountain.mytracker.Track.UserTrack;
import com.mountain.mytracker.other.GPXExport;

public class MyTrackDetailsTrackFragment extends Fragment {

    private Integer userTrackId;
    private UserTrack userTrack;

    public MyTrackDetailsTrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userTrackId = getArguments().getInt("userTrackId");
            Log.v("onCreate", userTrackId.toString());
            userTrack = new UserTrack(userTrackId, getActivity().getApplicationContext());
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

        if (userTrack != null) {

            Log.v("oncreateview", userTrack.getTrackId().toString());

            Long trackDuration = (userTrack.getTime()) / 1000000000;
            Double average_speed = userTrack.getAvg_speed() * 3.6D;

            duration.setText(String.format("%d:%02d:%02d", trackDuration / 3600, (trackDuration % 3600) / 60, trackDuration % 60));
            distance.setText(String.format("%f",Math.floor(userTrack.getDistance() * 100) / 100000));
            avg_speed.setText(String.format("%f",Math.floor(userTrack.getAvg_speed()*3.6D * 100D) / 100D));
            max_speed.setText(String.format("%f",Math.floor(userTrack.getMax_speed()*3.6F * 100) / 100));
            min_alt.setText(String.format("%d",Math.round(userTrack.getMin_alt())));
            max_alt.setText(String.format("%d",Math.round(userTrack.getMax_alt())));

            /*export_btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    GPXExport gpx = new GPXExport(getContext());
                    gpx.createFile(userTrackId, userTrack.getTrackName() + ".gpx", userTrack.getTrackName());
                    Toast.makeText(getActivity(), "Export successful",
                            Toast.LENGTH_LONG).show();
                }
            });*/

        }

        return rootView;
    }
}
