package com.mountain.mytracker.other;

import android.content.Context;
import android.location.Location;

import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.Track.TrackPoint;
import com.mountain.mytracker.Track.UserTrack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by andrei on 02.08.2016.
 */

public class UserTrackRestoreBackUp {

    private File backUpFile;
    private ArrayList<UserTrack> mUserTrackArrayList;
    private Context mContext;
    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    Document document;

    public UserTrackRestoreBackUp(File f, Context context){
        backUpFile = f;
        mContext = context;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(backUpFile);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        mUserTrackArrayList = new ArrayList<>();

    }

    public ArrayList<UserTrack> getmUserTrackArrayList() {
        return mUserTrackArrayList;
    }

    public void setmUserTrackArrayList(ArrayList<UserTrack> mUserTrackArrayList) {
        this.mUserTrackArrayList = mUserTrackArrayList;
    }

    public void restoreUserTrack(){
        UserTrack mTrack;

        NodeList trackList = document.getDocumentElement().getChildNodes();

        for(int i=0; i<trackList.getLength(); i++){
            Node track = trackList.item(i);
            if(track.getNodeName().equals("track")){
                mTrack = new UserTrack(mContext);
                NodeList trackNodeList = track.getChildNodes();
                for(int j=0; j<trackNodeList.getLength();j++){
                    Node trackNode = trackNodeList.item(j);
                        switch (trackNode.getNodeName()){
                            case "metadata" : {
                                NodeList metadata = trackNode.getChildNodes();
                                for(int k=0; k<metadata.getLength(); k++){
                                    Node metadataNode = metadata.item(k);
                                    String metadataString = metadataNode.getTextContent();
                                    switch (metadataNode.getNodeName()){
                                        case "trackId" : {
                                            mTrack.setTrackId(Integer.parseInt(metadataString));
                                            break;
                                        }
                                        case "trackName" : {
                                            mTrack.setTrackName(metadataString);
                                            break;
                                        }
                                        case "factoryTrackId" : {
                                            mTrack.setFactoryTrackId(Integer.parseInt(metadataString));
                                            break;
                                        }
                                        case "max_alt" : {
                                            mTrack.setMax_alt(Double.parseDouble(metadataString));
                                            break;
                                        }
                                        case "min_alt" : {
                                            mTrack.setMin_alt(Double.parseDouble(metadataString));
                                            break;
                                        }
                                        case "avg_speed" : {
                                            mTrack.setAvg_speed(Float.parseFloat(metadataString));
                                            break;
                                        }
                                        case "max_speed" : {
                                            mTrack.setMax_speed(Float.parseFloat(metadataString));
                                            break;
                                        }
                                        case "distance" : {
                                            mTrack.setDistance(Float.parseFloat(metadataString));
                                            break;
                                        }
                                        case "time" : {
                                            mTrack.setTime(Long.parseLong(metadataString));
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case "trk" : {
                                NodeList trk = trackNode.getChildNodes();
                                for(int k=0; k<trk.getLength(); k++){
                                    Node trackPoint = trk.item(k);
                                    if(trackPoint instanceof Element){
                                        TrackPoint mTrackPoint;
                                        Location mLocation = new Location("gps");
                                        mLocation.setLatitude(Double.parseDouble(((Element) trackPoint).getAttributes().getNamedItem("lat").getNodeValue()));
                                        mLocation.setLongitude(Double.parseDouble(((Element) trackPoint).getAttributes().getNamedItem("lon").getNodeValue()));
                                        NodeList altList = trackPoint.getChildNodes();
                                        for(int l=0; l<altList.getLength(); l++){
                                            Node altNode = altList.item(l);
                                            if(altNode instanceof Element){
                                                mLocation.setAltitude(Double.parseDouble(altNode.getTextContent()));
                                            }
                                        }
                                        mTrackPoint = new TrackPoint(mTrack.getTrackId(), mLocation, mContext);
                                        mTrack.addTrackPoint(mTrackPoint);
                                    }
                                }
                                break;
                            }

                        }
                }
                this.mUserTrackArrayList.add(mTrack);
            }
        }

    }

}
