package com.charles.taskmantest.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
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
import com.charles.taskmantest.datahandler.IngressTable;
import com.charles.taskmantest.datahandler.TaskManContentProvider;

/**
 * Created by charles on 12/10/13.
 */
public class IngressSelector extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private long idCode = 0;
    private String name = "";
    private View v = null;
    private final int LOADER_ID = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ingress_layout, view, false);
        idCode = getActivity().getIntent().getLongExtra("id", -1);
        setupButtons(inflater);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("Loader", "Starting Loader");
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/ingress_table";
        Uri places = Uri.parse(URL);
        String[] projection = new String[] {IngressTable.ID, IngressTable.WIFI, IngressTable.SOUND, IngressTable.SMS,IngressTable.AIRPLANE};
        return new CursorLoader(getActivity(), places, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v("Ingress Loader Finished loading", "From MyMap");
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
        ContentValues values = new ContentValues();
        values.put(IngressTable.ID, idCode);
        for (int i = 0; i < 4; i++) {
            ImageButton b = null;
            switch(i)  {
                case 0:


                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.accessnetworkwifi);
                    values.put(IngressTable.WIFI, "connect");
                    ll.addView(b);
                    break;
                case 1:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.deviceaccessbluetooth);
                    values.put(IngressTable.AIRPLANE, 0);
                    ll.addView(b);
                    break;
                case 2:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.deviceaccessmic);
                    values.put(IngressTable.SMS, "SMS");
                    ll.addView(b);
                    break;
                case 3:
                    b = (ImageButton) inflater.inflate(R.layout.custom_button, ll, false);
                    b.setImageResource(R.drawable.airplanemodeoff);
                    values.put(IngressTable.SOUND, 0);
                    ll.addView(b);

            }

            assert b != null;
            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.v("ImageButton", "Clicked");

                }
            });
            Uri ins = getActivity().getContentResolver().insert(TaskManContentProvider.INGRESS_URI, values);
            getLoaderManager().initLoader(LOADER_ID, null, this);

        }
    }

    public void changeId(int idCode, String name) {
        this.idCode = idCode;
        this.name = name;
    }

    private class UpdateIngressOptions extends AsyncTask<Cursor, Integer, String> {

        @Override
        protected String doInBackground(Cursor... params) {
            Log.v("Update buttons", "Operating on Cursor");
            Cursor c = params[0];
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(IngressTable.ID));
                if (id == idCode) {
                    Log.v("Found my match", "found my match");
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... item) {

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private class WriteOutData extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return null;
        }
    }

    private class CheckData extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return null;
        }
    }



}
