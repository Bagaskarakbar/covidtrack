package com.fadhlillahb.covidtracker;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    public static final String SHARED_PREF_NAME = "session";
    public SharedPreferences setting;
    public SharedPreferences.Editor editor;

    public Session(Context context){
        setting = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
    }

    public void setUserID(String userID){
        editor = setting.edit();
        editor.putString("userId",userID);
        editor.commit();
    }

    public void clear(){
        editor = setting.edit();
        editor.clear();
        editor.commit();
    }

    public String getUserID(){
        return setting.getString("userId","");
    }
}
