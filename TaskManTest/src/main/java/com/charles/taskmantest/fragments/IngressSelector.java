package com.charles.taskmantest.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
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
import com.charles.taskmantest.activities.SelectorActivity;
import com.charles.taskmantest.datahandler.EgressTable;
import com.charles.taskmantest.datahandler.IngressTable;
import com.charles.taskmantest.datahandler.json.Actions;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by charles on 12/10/13.
 */
public class IngressSelector extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private long idCode = 0;
    private String name = "";
    private View v = null;
    private final int LOADER_ID = 3;
    private Gson gson = new Gson();
    private HashMap<String, ImageButton> buttonMap = new HashMap<String, ImageButton>();
    private Actions actions = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ingress_layout, view, false);
        idCode = getActivity().getIntent().getLongExtra("id", -1);
        Log.v("Id Code: ", Long.toString(idCode));
        getLoaderManager().initLoader(LOADER_ID, null, this);
        setupButtons(inflater);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/ingress_table";
        Uri places = Uri.parse(URL);
        String[] projection = new String[] {IngressTable.ID, IngressTable.CONSTRUCT};
        return new CursorLoader(getActivity(), places, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v("Ingress Loader Finished loading", "From Ingress");
        switch (loader.getId()) {
            case LOADER_ID:
                Log.v("Found my Loader", "Found My  LOADER");
                new UpdateIngressOptions().execute(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

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
    public void handleClick(String key, ImageButton b) {
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

    public boolean handleWifiClick() {
        if (actions.wifi != null && actions.wifi.isEnabled()) {
            actions.wifi.setEnabled(false);
            return false;
        } else {
            actions.wifi = new Actions.WIFI();
            actions.wifi.setEnabled(true);
        }
        return true;
    }

    public boolean handleBlueToothClick() {
        if (actions.bluetooth != null && actions.bluetooth.isEnabled()) {
            actions.bluetooth.setEnabled(false);
            return false;
        } else {
            actions.bluetooth = new Actions.BlueTooth();
            actions.bluetooth.setEnabled(true);
        }
        return true;
    }

    public boolean handleSoundClick() {
        if (actions.audio != null && actions.audio.isEnabled()) {
            actions.audio.setEnabled(false);
            return false;
        } else {
            actions.audio = new Actions.Sound();
            actions.audio.setEnabled(true);
        }
        return true;
    }

    public boolean handleAirplaneClick() {
        if (actions.airplane) {
            actions.airplane = false;
            return false;
        } else {
            actions.airplane = true;
        }
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
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(IngressSelector.this.getActivity(), "Loading Actions",
                    "Loading Action Data", true);
        }

        @Override
        protected String doInBackground(Cursor... params) {
            Cursor c = params[0];
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

                int id = c.getInt(c.getColumnIndexOrThrow(IngressTable.ID));
                if (id == idCode) {
                    String gsonString = c.getString(c.getColumnIndexOrThrow(EgressTable.CONSTRUCT));
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

    private class WriteOutData extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return null;
        }
    }
}
