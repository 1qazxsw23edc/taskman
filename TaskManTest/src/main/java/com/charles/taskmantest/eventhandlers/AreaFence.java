package com.charles.taskmantest.eventhandlers;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * Created by charles on 10/7/13.
 */
public class AreaFence extends IntentService {

    public AreaFence() {
        super("AreaFence");
    }

    //Heck yeah!  It works!

    @Override
    protected void onHandleIntent(Intent intent) {
        //Toast.makeText(null, "A fence happened", Toast.LENGTH_LONG).show();
        List<Geofence> fenceList = LocationClient.getTriggeringGeofences(intent);
        Log.v("Intent from fnece: ", Integer.toString(fenceList.size()));
        Log.v("Intent from fence: ", "SUCCESS!");
    }
}
