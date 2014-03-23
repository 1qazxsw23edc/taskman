package com.charles.taskmantest.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
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
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by charles on 12/10/13.
 * This Fragment is representative of a tab.  It has a role so that it's aware of whether or not
 * it's the kind for entering or leaving a fenced area.  This doesn't have any real implications other
 * than this Fragment knowing which table to write to in the database.
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

    //The next three methods are created for the Database loader.  They load the database and then fill
    //in the GUI based on what's found in the database.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri places = Uri.parse(cursorURL);
        String[] projection = new String[] {cursorID, cursorConstruct};
        return new CursorLoader(getActivity(), places, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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

    //Each of these methods sets parameters in the JSON
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

        //Spinny dialog to delay application until database is loaded
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(ActionSelector.this.getActivity(), "Loading Actions",
                    "Loading Action Data", true);
        }

        //Pull the JSON from the database for the relevant fence area.  If there is no JSON then create it.
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

        //Iterate over the buttonmap and enable or disable buttons
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
}
