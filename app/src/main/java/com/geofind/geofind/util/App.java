package com.geofind.geofind.util;

import android.app.Application;
import android.content.Context;

/**
 * A general class to get the resources of the app outside functions.
 * <p/>
 * Created by Mickey on 30/10/2014.
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
