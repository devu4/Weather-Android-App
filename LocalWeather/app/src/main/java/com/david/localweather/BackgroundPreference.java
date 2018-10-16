package com.david.localweather;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by david on 21/12/2017.
 */

public class BackgroundPreference {

    SharedPreferences prefs;

    public BackgroundPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }


    String getBackground(){
        return prefs.getString("background", "#ffffff");
    }


    void setBackground(String background){
        prefs.edit().putString("background", background).commit();
    }
}
