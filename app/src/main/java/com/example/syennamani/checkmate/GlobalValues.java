package com.example.syennamani.checkmate;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by syennamani on 4/6/2017.
 */

public class GlobalValues {

    public static final String PREFS_NAME = "APPWIDEPREFS";
    private static SharedPreferences appwidePrefs;
    private static final String TAG = "GlobalValues";

    public static String getInstanceIdToken() {
        appwidePrefs = MyApplication.getAppContext().getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        Log.d(TAG, "InstanceIdToken: get" + appwidePrefs.getString("InstanceIdToken",""));
        return appwidePrefs.getString("InstanceIdToken","");
    }

    public static void setInstanceIdToken(String instanceIdToken) {
        appwidePrefs = MyApplication.getAppContext().getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        SharedPreferences.Editor editor = appwidePrefs.edit();
        editor.putString("InstanceIdToken", instanceIdToken);
        editor.commit();
        Log.d(TAG, "InstanceIdToken: " + instanceIdToken);
    }

}
