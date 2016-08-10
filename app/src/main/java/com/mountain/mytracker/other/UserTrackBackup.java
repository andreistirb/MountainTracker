package com.mountain.mytracker.other;

import com.mountain.mytracker.Track.UserTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UserTrackBackup {
    private ArrayList<UserTrack> mUserTrackList;
    private File root = android.os.Environment.getExternalStorageDirectory();
    private File backupFile, directory;
    private DateFormat dateFormat;
    private Date date;
    private String fileName;
    private FileOutputStream mFileOutputStream;
    private PrintWriter pw;

    public UserTrackBackup(ArrayList<UserTrack> userTrackArrayList){

        mUserTrackList = userTrackArrayList;

        directory = new File(root.getAbsolutePath() + "/MountainTracker");
        directory.mkdirs();

        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        date = new Date();
        fileName = "backup_" + dateFormat.format(date).toString() + ".txt";

        backupFile = new File(directory, fileName);

        try {
            mFileOutputStream = new FileOutputStream(backupFile);
            pw = new PrintWriter(mFileOutputStream);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void backUpList(){
        try {
            pw.println("<?xml version='1.0' encoding='UTF-8'?>");
            pw.println("<gpx>");
            for (int i = 0; i < mUserTrackList.size(); i++) {
                backUpTrack(mUserTrackList.get(i));
            }
            pw.println("</gpx>");
            pw.flush();
            pw.close();
            mFileOutputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void backUpTrack(UserTrack userTrack){
        pw.println("<track>");
        pw.println("<metadata>");

        pw.print("<trackId>");
        pw.print(userTrack.getTrackId());
        pw.println("</trackId>");

        pw.print("<trackName>");
        pw.print(userTrack.getTrackName());
        pw.println("</trackName>");

        if(userTrack.getFactoryTrackId() != null) {
            pw.print("<factoryTrackId>");
            pw.print(userTrack.getFactoryTrackId());
            pw.println("</factoryTrackId>");
        }

        pw.print("<max_alt>");
        pw.print(userTrack.getMax_alt());
        pw.println("</max_alt>");

        pw.print("<min_alt>");
        pw.print(userTrack.getMin_alt());
        pw.println("</min_alt>");

        pw.print("<avg_speed>");
        pw.print(userTrack.getAvg_speed());
        pw.println("</avg_speed>");

        pw.print("<max_speed>");
        pw.print(userTrack.getMax_speed());
        pw.println("</max_speed>");

        pw.print("<distance>");
        pw.print(userTrack.getDistance());
        pw.println("</distance>");

        pw.print("<time>");
        pw.print(userTrack.getTime());
        pw.println("</time>");

        pw.println("</metadata>");

        pw.println("<trk>");

        for(int j=0;j<userTrack.getTrackPointsCount()-1;j++){
            pw.println("<trkpt lat=" + '"' + userTrack.getTrackPoints().get(j).getLocation().getLatitude() + '"' +
                    " lon=" + '"' + userTrack.getTrackPoints().get(j).getLocation().getLongitude() + '"' + "> " +
                    "<ele>" + userTrack.getTrackPoints().get(j).getLocation().getAltitude() + "</ele>" + "</trkpt>");
        }

        pw.println("</trk>");
        pw.println("</track>");

    }


}
