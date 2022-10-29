package com.staygrateful.app.androidble;

import android.app.Application;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class BLEApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }
}
