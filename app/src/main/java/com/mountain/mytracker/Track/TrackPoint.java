package com.mountain.mytracker.Track;

/**
 * Created by astirb on 26.01.2016.
 */

public class TrackPoint {

    private int trackId;
    private Double altitude, latitude, longitude;
    private Float speed, accuracy;
    private Long time;

    public TrackPoint(int trackId, Double latitude, Double longitude, Double altitude, Float speed, Float accuracy, Long time){
        this.trackId = trackId;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.time = time;
    }

    public void fromDatabase(){

    }

    public void toDatabase(){

    }
}
