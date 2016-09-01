package com.mountain.mytracker.other;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by andrei on 30.08.2016.
 */

public class MountainTrackerApplication extends android.app.Application {

    @Override
    public void onCreate(){
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
