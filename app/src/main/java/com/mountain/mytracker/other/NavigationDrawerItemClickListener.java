package com.mountain.mytracker.other;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * Created by astirb on 06.05.2016.
 */
public class NavigationDrawerItemClickListener implements ListView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView parent, View view,int position, long id){
        switch (position){
            case 0 : {
                Intent i = new Intent(view.getContext(), SettingsActivity.class);
                view.getContext().startActivity(i);
            }
        }
    }

}
