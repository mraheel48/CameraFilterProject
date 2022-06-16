package com.example.deafultproject;


import android.content.Context;
import androidx.lifecycle.LifecycleObserver;
import androidx.multidex.MultiDexApplication;

public class MyApplication extends MultiDexApplication implements LifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

}


