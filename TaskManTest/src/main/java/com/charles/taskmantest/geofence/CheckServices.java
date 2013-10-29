package com.charles.taskmantest.geofence;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by charles on 10/9/13.
 */
public class CheckServices {
    //Global constants
    /*
    Define a request cod to send to Google Play services
    This code is returned in Activity.onActivityResult
     */
    Context context;
    private final static int
        CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //Define a Dialog Fragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        //Global field to contain the error dialog
        private Dialog mDialog;

        //Default constructor. Sets the dialog field to null

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        //Set the Dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        //Return a Dialog to the DialogFragment
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
    Handle the results returned to the FragementActivity
    by Google Play Services
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                /*
                If the result code is Acitivity.RESULT_OK, try to connect again
                 */
                switch(resultCode) {
                    case Activity.RESULT_OK:
                        //Try again
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        //Check that Play Services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        //If Play Services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            //In debug mode, log the status
            Log.d("GeoFence Detection", "Google Play services is available");
            return true;
        } else {
           //Get the error code
           Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                   resultCode,
                   null,
                   CONNECTION_FAILURE_RESOLUTION_REQUEST);
            return false;
        }
    }
}
