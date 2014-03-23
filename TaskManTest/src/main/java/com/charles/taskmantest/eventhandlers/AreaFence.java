package com.charles.taskmantest.eventhandlers;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * Created by charles on 10/7/13.
 * This is the service that gets ran when you enter or leave a fenced area.  It doesn't do a whole
 * lot right now because I haven't gotten that far.
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
        Log.v("Intent from fence: ", Integer.toString(fenceList.size()));
        Log.v("Intent from fence: ", "SUCCESS!");
    }
}
