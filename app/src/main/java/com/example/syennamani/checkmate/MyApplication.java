package com.example.syennamani.checkmate;

import android.app.Application;
import android.content.Context;

/**
 * Created by syennamani on 4/6/2017.
 */

public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
