package com.mountain.mytracker.gps;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.mountain.mytracker.activity.MainActivity;
import com.mountain.mytracker.activity.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GeofenceTransitionsIntentService extends IntentService {

    static int enterCounter = 0;
    static int exitCounter = 0;
    static int dwellCounter = 0;

    static Set<Geofence> currentArea = new HashSet<Geofence>();

    protected static final String TAG = "geofence-transitions";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"s-a inregistrat ceva miscare!");
        GeofencingEvent mGeofencingEvent = GeofencingEvent.fromIntent(intent);
        if(mGeofencingEvent.hasError()){
            //String errorMessage = GeofenceErrorMessages.getErrorString(this,
            //        mGeofencingEvent.getErrorCode());
            Log.e(TAG, "err");//errorMessage);
            return;
        }
        int geofenceTransition = mGeofencingEvent.getGeofenceTransition();
        switch(geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_EXIT : {
                List<Geofence> triggeringGeofences = mGeofencingEvent.getTriggeringGeofences();
                exitCounter = triggeringGeofences.size();
                String geofenceTransitionDetails = getGeofenceTransitionDetails(this,
                        geofenceTransition, triggeringGeofences);
                currentArea.removeAll(triggeringGeofences);
                if(currentArea.size() == 0) {
                    sendNotification(geofenceTransitionDetails);
                }
                Log.i(TAG,geofenceTransitionDetails);
                Log.i(TAG,exitCounter + " zone din care ai iesit");
                break;
            }
            case Geofence.GEOFENCE_TRANSITION_ENTER : {
                List<Geofence> triggeringGeofences = mGeofencingEvent.getTriggeringGeofences();
                enterCounter = triggeringGeofences.size();
                String geofenceTransitionDetails = getGeofenceTransitionDetails(this,
                        geofenceTransition, triggeringGeofences);
                //sendNotification(geofenceTransitionDetails);
                Log.i(TAG,geofenceTransitionDetails);
                Log.i(TAG,enterCounter + " zone in care ai intrat");
                currentArea.addAll(triggeringGeofences);
                break;
            }
            case Geofence.GEOFENCE_TRANSITION_DWELL : {
                List<Geofence> triggeringGeofences = mGeofencingEvent.getTriggeringGeofences();
                dwellCounter += triggeringGeofences.size();
                String geofenceTransitionDetails = getGeofenceTransitionDetails(this,
                        geofenceTransition, triggeringGeofences);
                sendNotification(geofenceTransitionDetails);
                Log.i(TAG,geofenceTransitionDetails);
                Log.i(TAG,exitCounter + " zone in care esti");
                break;
            }

        }
        /*if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            List<Geofence> triggeringGeofences = mGeofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTransitionDetails(this,
                    geofenceTransition, triggeringGeofences);
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG,geofenceTransitionDetails);
        }
        else{
            Log.i(TAG, "invalid transition type");
        }*/
    }

    private String getGeofenceTransitionDetails(Context context, int geoFenceTransition,
                                              List<Geofence> triggeringGeofences){

        String geofenceTransitionString = getTransitionString(geoFenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;

    }

    private String getTransitionString(int transitionType){
        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER : return "You just entered the area";
            case Geofence.GEOFENCE_TRANSITION_EXIT : return "Ai iesit de pe traseu";
            case Geofence.GEOFENCE_TRANSITION_DWELL : return "";// "You are lingering in the area";
            default : return  "Unknown move. Are you an alien?";
        }
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Ai iesit de pe traseu")//getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
