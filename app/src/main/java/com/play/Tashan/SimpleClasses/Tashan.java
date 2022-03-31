package com.play.Tashan.SimpleClasses;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import io.fabric.sdk.android.Fabric;

/**
 * Created by AQEEL on 3/18/2019.
 */

public class Tashan extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Fabric.with(this, new Crashlytics());
    }

}
