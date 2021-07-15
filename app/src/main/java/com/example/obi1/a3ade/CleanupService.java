package com.example.obi1.a3ade;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CleanupService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE MESSAGE", "Service started, stopped(false).");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVICE MESSAGE", "Service stopped.");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //FirebaseUtil.userStatus = "Off";
        FirebaseUtil.mUserDataReference.child("status").setValue("Offline");
        Log.d("TASK MESSAGE", "Task stopped");
        stopSelf();
    }

    public CleanupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
