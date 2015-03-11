package com.mountain.mytracker.track;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by Andrei on 3/7/2015.
 */
public class Track {

    private String name, id;
    private ArrayList<GeoPoint> points;

    public Track(){

    }

    public Track(String name, String id){
        this.name = name;
        this.id = id;
    }

    public Track(String name, String id, ArrayList<GeoPoint> points) {
        this.name = name;
        this.id = id;
        this.points = points;
    }

    public ArrayList<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<GeoPoint> points) {
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
