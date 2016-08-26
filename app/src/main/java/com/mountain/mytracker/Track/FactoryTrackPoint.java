package com.mountain.mytracker.Track;

/**
 * Created by andrei on 26.08.2016.
 */

public class FactoryTrackPoint {
    private String trackId, trackPointId, altitude, latitude, longitude;

    public FactoryTrackPoint(){}

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getTrackPointId() {
        return trackPointId;
    }

    public void setTrackPointId(String trackPointId) {
        this.trackPointId = trackPointId;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
