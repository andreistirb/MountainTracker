package com.mountain.mytracker.other;

import android.content.Context;

import com.mountain.mytracker.Track.Track;
import com.mountain.mytracker.Track.UserTrack;

import org.w3c.dom.Document;
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

    }

    public ArrayList<UserTrack> getmUserTrackArrayList() {
        return mUserTrackArrayList;
    }

    public void setmUserTrackArrayList(ArrayList<UserTrack> mUserTrackArrayList) {
        this.mUserTrackArrayList = mUserTrackArrayList;
    }

    public void restoreUserTrack(){
        Track mTrack = new UserTrack(mContext);

        NodeList trackList = document.getDocumentElement().getChildNodes();

    }

}
