package com.example.firestorechatapp.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class ChatApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }
}
class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks{
           private int numStarted=0;
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if(numStarted==0){
            if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                FirestoreUtil.updateOnlineStatus(true, null);
            }
        }
        numStarted++;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        numStarted--;
        if(numStarted==0){
            if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                FirestoreUtil.updateOnlineStatus(false, Calendar.getInstance().getTime());
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
