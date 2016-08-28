package com.mountain.mytracker.db;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.mountain.mytracker.Track.FactoryTrack;
import com.mountain.mytracker.activity.R;

import java.util.Map;

public class MountainTrackListAdapter extends FirebaseListAdapter<FactoryTrack> {

    private ProgressBar mProgressBar;

    public MountainTrackListAdapter(Activity activity, Class<FactoryTrack> modelClass,
                                    int modelLayout,
                                    Query ref,
                                    ProgressBar progressBar){
        super(activity,modelClass, modelLayout, ref);
        mProgressBar = progressBar;
    }

    @Override
    protected void populateView(View v, FactoryTrack model, int position) {

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        ((TextView) v.findViewById(R.id.mountain_track_list_text)).setText(model.getTrackName());
        ((TextView) v.findViewById(R.id.mountain_track_list_diff)).setText(model.getTrackDifficulty());
        ((TextView) v.findViewById(R.id.mountain_track_list_length)).setText(model.getTrackLength());
        ((ImageView) v.findViewById(R.id.mountain_track_list_pic)).setImageResource(v.getResources()
                .getIdentifier(model.getTrackMark(),
                        "drawable","com.mountain.mytracker.activity"));
    }

    @Override
    protected FactoryTrack parseSnapshot(DataSnapshot snapshot) {
        ObjectMapper mapper = new ObjectMapper();
        GenericTypeIndicator<Map<String,Object>> indicator = new GenericTypeIndicator<Map<String, Object>>() {};
        FactoryTrack value = mapper.convertValue(snapshot.getValue(indicator), FactoryTrack.class);

        return value;
    }
}
