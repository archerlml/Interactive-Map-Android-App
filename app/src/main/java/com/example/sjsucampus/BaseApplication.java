package com.example.sjsucampus;

import android.app.Application;

/**
 * Created by chitoo on 10/22/16.
 */

public class BaseApplication extends Application {
    private static BaseApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

    }


    public static BaseApplication get() {
        return application;
    }
}
