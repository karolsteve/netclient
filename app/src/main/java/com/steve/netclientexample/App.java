package com.steve.netclientexample;

import android.app.Application;

import com.steve.netclient.NetClient;

/**
 * Created by steve.tchatchouang on 29/04/2018
 */

public class App extends Application {

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        NetClient.init("testApp",BuildConfig.DEBUG);
    }

    public static synchronized App getInstance() {
        return sInstance;
    }

}
