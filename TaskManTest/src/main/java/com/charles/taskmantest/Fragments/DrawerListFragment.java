package com.charles.taskmantest.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.charles.taskmantest.R;
import com.charles.taskmantest.datahandler.EgressTable;
import com.charles.taskmantest.datahandler.GeoFenceTable;
import com.charles.taskmantest.datahandler.IngressTable;
import com.charles.taskmantest.datahandler.TaskManContentProvider;
import com.google.android.gms.maps.MapFragment;

/**
 * Created by charles on 11/23/13.
 */
public class DrawerListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>{



    private SimpleCursorAdapter mAdapter;
    ItemSelectedListener onItemSelected;
    private static final int LOADER_ID =1;
    private static View header;
    private static View mainView;
    private static double currentLatitude = 0.0f;
    private static double currentLongitude = 0.0f;
    private static double viewLatitude = 0.0f;
    private static double viewLongitude = 0.0f;
    private static double radius = 0.0f;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fillData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.drawer_list_view, null);
        header = inflater.inflate(R.layout.drawer_header, null);
        ((ListView)v).addHeaderView(header);
        mainView =v;
        initPlaces();
        return v;
    }

    //For when you click something
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        onItemSelected.onItemSelected(id);
        TextView tv = (TextView)v;
        //Log.v("ListItemClicked", Long.toString(id) + " " + tv.getText() );
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Assign the main activity callback
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onItemSelected = (ItemSelectedListener)activity;
            //onItemSelected.onItemSelected(5);
        } catch (Exception e) {
            throw new ClassCastException(" must implement ItemSelectedListener");
        }
    }

    //Define the callback
    public interface ItemSelectedListener {
        public void onItemSelected(long id);
        public boolean onItemDeleted(long id);
        public boolean onItemAdded(long id);
        public void toggleDrawerOpen();
        public void toggleDrawerClosed();
    }

    //Bind actions to the listview
    private void initPlaces() {
        ImageButton addPlacesButton = (ImageButton)header.findViewById(R.id.add_place_button);
        addPlacesButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPlacesPressed();
                    }
                }
        );

        ((ListView)mainView).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                removePlacePressed(((TextView) view), position, id);
                return false;
            }
        });
    }

    /*
   Show an alert to ask for the name of the new place they want to add
    */
    private void addPlacesPressed() {
        FragmentManager fm = getActivity().getFragmentManager();

        Fragment frag = new LocationSelection();
        MapFragment mMap = (MapFragment)fm.findFragmentById(R.id.content_view);
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(mMap).addToBackStack("map");
        ft.add(R.id.container, frag, "location_select");
        ft.show(frag);
        ft.commit();
        onItemSelected.toggleDrawerClosed();

    }

    private void removePlacePressed(final TextView v, final int position, final long id) {
        onItemSelected.onItemSelected(id);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Delete");
        alert.setMessage("Do you wish do delete " + v.getText() + "?");
        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().getContentResolver().delete(TaskManContentProvider.FENCE_URI, GeoFenceTable.ID + "=" + id, null);
                getActivity().getContentResolver().delete(TaskManContentProvider.EGRESS_URI, EgressTable.ID + "=" + id, null);
                getActivity().getContentResolver().delete(TaskManContentProvider.INGRESS_URI, IngressTable.ID + "=" + id, null);
                onItemSelected.onItemDeleted(id);
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

    //Loader from the database
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/fences_table";
        Uri places = Uri.parse(URL);
        String[] projection = new String[] {GeoFenceTable.ID, GeoFenceTable.NAME};
        return new CursorLoader(getActivity(), places,projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                mAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    //Method to assigne the cursoradapter and fill the listview with the already assigned values
    private void fillData() {
        String[] from = new String[] {GeoFenceTable.NAME};

        int [] to = new int[] {R.id.text_list_item};

        getLoaderManager().initLoader(LOADER_ID, null, this);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.drawer_list_item, null, from, to, 0);
        setListAdapter(mAdapter);
    }

    /*
    Getters and setters for the constant variables
     */
    public static void setCurrentLongitude(double longitude) {
        currentLongitude = longitude;
    }

    public static void setCurrentLatitude(double latitude) {
        currentLatitude = latitude;
    }
}
