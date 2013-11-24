package com.charles.taskmantest.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.charles.taskmantest.R;
import com.charles.taskmantest.datahandler.GeoFenceTable;
import com.charles.taskmantest.datahandler.TaskManContentProvider;

import java.util.Random;

/**
 * Created by charles on 11/23/13.
 */
public class DrawerListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {



    private SimpleCursorAdapter mAdapter;
    ItemSelectedListener onItemSelected;
    private static final int LOADER_ID =1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //View header = (View)getActivity().getLayoutInflater().inflate(R.layout.drawer_header, null);

        String[] name = new String[] {GeoFenceTable.NAME};

        int [] to = new int[] {R.id.text_list_item};

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.drawer_list_item, null, name, to, 0);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.drawer_list_view, null);
        View header = inflater.inflate(R.layout.drawer_header, null);
        ((ListView)v).addHeaderView(header);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        onItemSelected.onItemSelected(position);
        Log.v("ListItemClicked", "");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onItemSelected = (ItemSelectedListener)activity;

        } catch (Exception e) {
            throw new ClassCastException(" must implement ItemSelectedListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("Loader", "Starting Loader");
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/fences_table";
        Uri places = Uri.parse(URL);
        return new CursorLoader(getActivity(), places, new String[] {"_id"}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public interface ItemSelectedListener {
        public void onItemSelected(int id);
    }

    private void initPlaces() {
        ImageButton addPlacesButton = (ImageButton)getActivity().findViewById(R.id.add_place_button);
        addPlacesButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPlacesPressed();
                    }
                }
        );
    }

    /*
   Show an alert to ask for the name of the new place they want to add
    */
    private void addPlacesPressed() {
        Log.e("Magic", "Button pressed");
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("New Place");
        alert.setMessage("Add a New Place");
        //Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Random random = new Random();
                String value = input.getText().toString();
                ContentValues values = new ContentValues();
                values.put(GeoFenceTable.ID, Integer.toString(random.nextInt()));
                values.put(GeoFenceTable.LATITUDE, 0.0f);
                values.put(GeoFenceTable.LONGITUDE, 0.0f);
                values.put(GeoFenceTable.RADIUS, 1.0f);
                values.put(GeoFenceTable.EXPIRATION, 0);
                values.put(GeoFenceTable.TRANSITION, 0);
                values.put(GeoFenceTable.NAME, "test");
                Uri uri = getActivity().getContentResolver().insert(TaskManContentProvider.FENCE_URI, values);
                Log.v("Finished Update", "Finished Update");
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alert.show();

    }
}
