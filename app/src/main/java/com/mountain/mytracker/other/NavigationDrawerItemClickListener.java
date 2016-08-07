package com.mountain.mytracker.other;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mountain.mytracker.activity.R;


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
                break;
            }
            case 2 : {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse(view.getContext().getString(R.string.app_website)));//"http://cdascalu.com/projects/mountaintracker/index.html"));
                view.getContext().startActivity(myWebLink);
                break;
            }
        }
    }

}
