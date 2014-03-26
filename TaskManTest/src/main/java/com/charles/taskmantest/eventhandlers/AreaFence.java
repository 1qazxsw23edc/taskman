package com.charles.taskmantest.eventhandlers;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.charles.taskmantest.MainActivity;
import com.charles.taskmantest.R;
import com.charles.taskmantest.datahandler.GeoFenceTable;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charles on 10/7/13.
 * This is the service that gets ran when you enter or leave a fenced area.  It doesn't do a whole
 * lot right now because I haven't gotten that far.
 */
public class AreaFence extends BroadcastReceiver implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private ArrayList triggerIds = new ArrayList();
    private String URL = null;
    private final int LOADER_ID = 5;
    private enum GTYPE {ENTER, EXIT};
    private GTYPE gType;
    private Context context;

    /*public AreaFence() {
        super("AreaFence");
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {

            List<Geofence> fenceList = LocationClient.getTriggeringGeofences(intent);
            if (fenceList.size() == 0) return;//Safety check
            Log.v("Intent from fence: ", "SUCCESS!");
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/ingress_table";
                Log.v("Geofence: ", "Modifying URL for entry");
                gType = GTYPE.ENTER;
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/egress_table";
                Log.v("Geofence: ", "Modifying URL for exit");
                gType = GTYPE.EXIT;
            }

            for (int i = 0; i < fenceList.size(); i++) {
                String id = ((Geofence)fenceList.get(i)).getRequestId();
                triggerIds.add(id);
            }
            sendNotification("test");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (URL == null) return null;
        Uri places = Uri.parse(URL);
        String[] projection = new String[] {GeoFenceTable.ID, GeoFenceTable.NAME};
        return new CursorLoader(context, places,projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void sendNotification(String test) {
        Intent notificationIntent = new Intent(context, MainActivity.class);

        //Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        //Add the main activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        //Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        //Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a nofification builder that's compatible with the platform versions >=4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //Set the notification contents
        builder.setSmallIcon(R.drawable.add_action)
                .setContentTitle("From Geofence: " + getType())
                .setContentText("From Geofence" + getType())
                .setContentIntent(notificationPendingIntent);

        //Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Issue the notifications
        mNotificationManager.notify(0, builder.build());
    }

    private String getType() {
        if (gType == GTYPE.ENTER) {
            return "Enter";
        } else {
            return "Exit";
        }
    }
}
