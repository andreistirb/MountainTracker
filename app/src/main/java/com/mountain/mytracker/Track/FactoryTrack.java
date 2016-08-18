package com.mountain.mytracker.Track;

/**
 * Created by astirb on 17.02.2016.
 */
public class FactoryTrack extends Track {
    private String trackDifficulty, trackMark, trackLength, trackDescription, trackAvailability;
    private Integer mountainId;

    public FactoryTrack(){
        super();
    }

    public FactoryTrack(Integer trackId){
        super(trackId);
    }

    public String getTrackDifficulty() {
        return trackDifficulty;
    }

    public void setTrackDifficulty(String trackDifficulty) {
        this.trackDifficulty = trackDifficulty;
    }

    public String getTrackMark() {
        return trackMark;
    }

    public void setTrackMark(String trackMark) {
        this.trackMark = trackMark;
    }

    public Integer getMountainId() {
        return mountainId;
    }

    public void setMountainId(Integer mountainId) {
        this.mountainId = mountainId;
    }

    public String getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(String trackLength) {
        this.trackLength = trackLength;
    }

    public String getTrackDescription() {
        return trackDescription;
    }

    public void setTrackDescription(String trackDescription) {
        this.trackDescription = trackDescription;
    }

    public String getTrackAvailability() {
        return trackAvailability;
    }

    public void setTrackAvailability(String trackAvailability) {
        this.trackAvailability = trackAvailability;
    }

}
