package com.charles.taskmantest.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.charles.taskmantest.R;
import com.charles.taskmantest.datahandler.EgressTable;
import com.charles.taskmantest.datahandler.IngressTable;
import com.charles.taskmantest.datahandler.TaskManContentProvider;
import com.charles.taskmantest.datahandler.json.Actions;
import com.charles.taskmantest.eventhandlers.AreaFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by charles on 12/10/13.
 */
public class ActionSelector extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private long idCode = 0;
    private String name = "";
    private View v = null;
    private final int INGRESS_LOADER_ID = 3;
    private final int EGRESS_LOADER_ID = 4;
    private Gson gson = new Gson();
    private HashMap<String, ImageButton> buttonMap = new HashMap<String, ImageButton>();
    private Actions actions = null;
    private String cursorID;
    private String cursorConstruct;
    private String cursorURL;
    private double lat = 0;
    private double lon = 0;
    private float radius = 0;


    //Set the layout and initialize the role variables.  You want to know what table you're loading
    //on a per-fragment basis.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {
        Bundle args = getArguments();

        v = inflater.inflate(R.layout.action_selector_layout, view, false);

        idCode = getActivity().getIntent().getLongExtra("id", -1);
        lat = getActivity().getIntent().getDoubleExtra("lat", -1);
        lon = getActivity().getIntent().getDoubleExtra("lon", -1);
        radius = getActivity().getIntent().getFloatExtra("radius", -1);
        //Log.v("Params: ", "Lat: " + Double.toString(getActivity().getIntent().getDoubleExtra("lat", -1)));
        //Log.v("Params: ", "Radius: " + Float.toString(getActivity().getIntent().getFloatExtra("radius", -1)));
        //Log.v("Id Code: ", Long.toString(idCode));
        //Modify the variables based on id
        if (args.get("role").equals("ingress")) {
            cursorID = IngressTable.ID;
            cursorConstruct = IngressTable.CONSTRUCT;
            cursorURL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/ingress_table";
            getLoaderManager().initLoader(INGRESS_LOADER_ID, null, this);
        } else if(args.get("role").equals("egress")) {
            cursorID = EgressTable.ID;
            cursorConstruct = EgressTable.CONSTRUCT;
            cursorURL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/egress_table";
            getLoaderManager().initLoader(EGRESS_LOADER_ID, null, this);
        }

        setupButtons(inflater);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri places = Uri.parse(cursorURL);
        String[] projection = new String[] {cursorID, cursorConstruct};
        return new CursorLoader(getActivity(), places, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Log.v("Ingress Loader Finished loading", "From Ingress");
        if (loader.getId() == INGRESS_LOADER_ID || loader.getId() == EGRESS_LOADER_ID) {
            new UpdateIngressOptions().execute(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


    //Save the data to the database when the Fragment is destroyed
    @Override
    public void onDestroy() {

        String json = gson.toJson(actions);
        if (json == null || json.equals("")) return ;
        ContentValues values = new ContentValues();
        if (getArguments().get("role").equals("ingress")) {
            values.put(IngressTable.CONSTRUCT, json);
            ActionSelector.this.getActivity().getContentResolver().update(TaskManContentProvider.INGRESS_URI, values,
                    IngressTable.ID + "=" + Long.toString(idCode), null);
        } else if (getArguments().get("role").equals("egress")) {
            values.put(EgressTable.CONSTRUCT, json);
            ActionSelector.this.getActivity().getContentResolver().update(TaskManContentProvider.EGRESS_URI, values,
                    EgressTable.ID + "=" + Long.toString(idCode), null);
        }
        //new WriteOutData().execute(gson.toJson(actions));
        super.onDestroy();
    }

    //This needs some help, it's to setup the buttons and bind actions to them when the fragment is created
    //it's super ugly right now and needs some help.
    private void setupButtons(LayoutInflater inflater) {
        GridLayout ll = ((GridLayout)v.findViewById(R.id.ingress_layout));
        for (int i = 0; i < 4; i++) {
            ImageButton b = null;
            switch(i)  {
                case 0:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.accessnetworkwifi);
                    buttonMap.put("wifi", b);
                    ll.addView(b);
                    break;
                case 1:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.deviceaccessbluetooth);
                    buttonMap.put("bluetooth", b);
                    ll.addView(b);
                    break;
                case 2:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.deviceaccessmic);
                    buttonMap.put("sound", b);
                    ll.addView(b);
                    break;
                case 3:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.airplanemodeoff);
                    buttonMap.put("airplane", b);
                    ll.addView(b);
                    break;
                default:
                    throw new IllegalStateException();

            }

            //Each button gets a listener.  This registers that listener based on the type of button
            //set with the key in the @buttonMap
            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ImageButton b = (ImageButton)v;

                    Iterator it = buttonMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String)it.next();
                        if (buttonMap.get(key).equals(b)) {
                            handleClick(key, b);
                        }
                    }
                }
            });

        }
    }


    //These handle the individual button clicks each one will start an Intent to change the various parameters
    private void handleClick(String key, ImageButton b) {
        boolean enable = false;
        if (key.equals("wifi")) {
            enable = handleWifiClick();
        } else if (key.equals("bluetooth")) {
            enable = handleBlueToothClick();
        } else if (key.equals("sound")) {
            enable = handleSoundClick();
        } else if (key.equals("airplane")) {
            enable = handleAirplaneClick();
        }
        toggleButton(enable, b);
    }

    private boolean handleWifiClick() {
        if (actions.wifi != null && actions.wifi.isEnabled()) {
            actions.wifi.setEnabled(false);
            return false;
        } else {
            actions.wifi = new Actions.WIFI();
            actions.wifi.setEnabled(true);
        }
        return true;
    }

    private boolean handleBlueToothClick() {
        if (actions.bluetooth != null && actions.bluetooth.isEnabled()) {
            actions.bluetooth.setEnabled(false);
            return false;
        } else {
            actions.bluetooth = new Actions.BlueTooth();
            actions.bluetooth.setEnabled(true);
        }
        return true;
    }

    private boolean handleSoundClick() {
        if (actions.audio != null && actions.audio.isEnabled()) {
            actions.audio.setEnabled(false);
            return false;
        } else {
            actions.audio = new Actions.Sound();
            actions.audio.setEnabled(true);
        }
        return true;
    }

    private boolean handleAirplaneClick() {
        if (actions.airplane) {
            actions.airplane = false;
            return false;
        } else {
            actions.airplane = true;
        }
        return true;
    }

    private boolean handleSMSClick() {
        return true;
    }

    //I'm not sure if I'm using this right now.
    public void reloadActions() {
        if (getArguments().get("role").equals("ingress")) {
            Log.v("RELOAD", "reloading from ingress");
            getLoaderManager().restartLoader(INGRESS_LOADER_ID, null, this);
        } else if (getArguments().get("role").equals("egress")) {
            Log.v("RELOAD", "reloading from egress");
            getLoaderManager().restartLoader(EGRESS_LOADER_ID, null, this);
        }
    }

    public void resetId(long idCode) {
        this.idCode = idCode;
    }


    //Initialize the buttons with their original state
    public boolean initializeButtons(String key, ImageButton b) {
        if (key.equals("wifi")) {
            if (actions.wifi != null && actions.wifi.isEnabled()) {
               return true;
            }
        } else if (key.equals("bluetooth")) {
            if (actions.bluetooth != null && actions.bluetooth.isEnabled()) {
                return true;
            }
        } else if (key.equals("sound")) {
            if (actions.audio != null && actions.audio.isEnabled()) {
                return true;
            }
        } else if (key.equals("airplane")) {
            if (actions.airplane) {
                return true;
            }
        }
        return false;
    }

    //Method to modfiy the background of the button based on its current state
    private void toggleButton(boolean state, ImageButton b) {
        if (state) {
            b.setBackgroundResource(R.drawable.translucent_green);
        } else {
            b.setBackgroundResource(R.drawable.translucent_grey);
        }
    }


    //Async task to read in the JSON from the database and set up the initial @actions state
    private class UpdateIngressOptions extends AsyncTask<Cursor, Integer, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(ActionSelector.this.getActivity(), "Loading Actions",
                    "Loading Action Data", true);
        }

        @Override
        protected String doInBackground(Cursor... params) {
            Cursor c = params[0];
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

                int id = c.getInt(c.getColumnIndexOrThrow(cursorID));
                if (id == idCode) {
                    String gsonString = c.getString(c.getColumnIndexOrThrow(cursorConstruct));
                    if (gsonString.length() == 0) {
                        Log.v("Empty GSON", "Empty GSON");
                            actions = new Actions();
                    } else {
                        gson.toJson(gsonString);
                        actions = gson.fromJson(gsonString, Actions.class);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... item) {

        }

        @Override
        protected void onPostExecute(String result) {
            Iterator it = buttonMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                ImageButton b = buttonMap.get(key);
                boolean enable = initializeButtons(key, b);
                toggleButton(enable, b);
            }
            progressDialog.dismiss();
        }
    }

    private class CreateGeofence extends AsyncTask<HashMap<String, Object>, Integer, String> implements
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener,
            LocationClient.OnAddGeofencesResultListener  {

        private LocationClient mLocationClient;
        private PendingIntent mGeofenceRequestIntent;
        private ArrayList<Geofence> fencesList = new ArrayList();
        private Geofence.Builder fenceBuilder = new Geofence.Builder();


        @Override
        protected String doInBackground(HashMap<String, Object>... params) {
            HashMap parameters = params[0];

            String role = (String)ActionSelector.this.getArguments().get("role");
            fenceBuilder.setCircularRegion(lat, lon, (float)radius);
            fenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);

            fenceBuilder.setRequestId(Integer.toString((int)idCode)+ ":" + role);//Store both the id and the role for differentiation later

            if (role.equals("ingress")) {
                fenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
            } else {
                fenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);
            }

            Geofence fence = fenceBuilder.build();
            fencesList.add(fence);
            mLocationClient = new LocationClient(ActionSelector.this.getActivity(), this, this);
            mLocationClient.connect();
            return null;
        }

        //These methods override the Location Services methods to add a Geofence into the system.

    /*
    Captain's Log, I need to implement these methods to create the Geofence.  The @onAddGeofencesResult will tell me
    when I have successfully added a geofence.  I need to investigate whether or not I can then call the placAdded method
    from my Map object and update it with the new fence then.  That means that it was successfully added into the location services
    and I can handle a failure if it happens.
     */

        @Override
        public void onConnected(Bundle bundle) {

            mGeofenceRequestIntent = createRequestPendingIntent();

            LocationRequest localRequest = LocationRequest.create();
            localRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            localRequest.setInterval(5000);

            mLocationClient.addGeofences(fencesList, mGeofenceRequestIntent, this);
            Log.v("Added GeoFence: " , "Added INTENT to Services");
        }

        @Override
        public void onDisconnected() {

        }

        @Override

        public void onAddGeofencesResult(int i, String[] strings) {
            // If adding the geofences was successful
            int statusCode = LocationStatusCodes.SUCCESS;
            if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
            } else {
                // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
            }
            mLocationClient.disconnect();
            Log.v("GeoFence Added: ", "Added Fence Successfully");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        private PendingIntent createRequestPendingIntent() {

            if (null != mGeofenceRequestIntent) {

                return mGeofenceRequestIntent;

            } else {


                Intent intent = new Intent(ActionSelector.this.getActivity(), AreaFence.class);
                return PendingIntent.getService(
                        ActionSelector.this.getActivity(),
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
    }
}
