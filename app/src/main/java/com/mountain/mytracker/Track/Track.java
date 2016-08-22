package com.mountain.mytracker.Track;

import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;

/**
 * Created by astirb on 26.01.2016.
 */

public class Track{

    protected Integer trackId;
    protected String trackName;

    protected ArrayList<TrackPoint> trackPoints;
    protected ArrayList<GeoPoint> trackGeoPoints;

    public Track(){
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();
    }

    public Track(Integer trackId){
        this.trackId = trackId;
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();
    }

    public Track(Integer trackId, String trackName){
        this.trackId = trackId;
        this.trackName = trackName;
        trackPoints = new ArrayList<TrackPoint>();
        trackGeoPoints = new ArrayList<GeoPoint>();
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(ArrayList<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    public void addTrackPoint(TrackPoint trackPoint){
        this.trackPoints.add(trackPoint);
    }

    public void addTrackGeoPoint(GeoPoint trackGeoPoint){
        this.trackGeoPoints.add(trackGeoPoint);
    }

    public ArrayList<GeoPoint> getTrackGeoPoints() {
        return trackGeoPoints;
    }

    public void setTrackGeoPoints(ArrayList<GeoPoint> trackGeoPoints) {
        this.trackGeoPoints = trackGeoPoints;
    }

    public Integer getTrackPointsCount(){
        return this.getTrackPoints().size();
    }

}
