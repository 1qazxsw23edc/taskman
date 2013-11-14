package com.charles.taskmantest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.charles.taskmantest.Fragments.MyMap;
import com.charles.taskmantest.datahandler.JSONManager;
import com.charles.taskmantest.geofence.CheckServices;
import com.charles.taskmantest.interfaces.UpdatePlacesCallBack;

import java.util.ArrayList;


public class MainActivity extends Activity implements UpdatePlacesCallBack, LoaderManager.LoaderCallbacks<Cursor> {
    private static DrawerLayout mDrawerLayout;
    private static ListView mDrawerList;
    private SharedPreferences mPreferences;
    private JSONManager jsonManager;
    private static PlacesListAdapter adapter;
    private static Place currentPlace = null;
    private static MyMap mapFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("TaskMan");
        jsonManager = JSONManager.getInstance();
        jsonManager.setmPreferences(this);
        //jsonManager = new JSONManager(this);

        //Detect whether or not it's first run
        mPreferences = getSharedPreferences("com.charles.TaskManTest", MODE_PRIVATE);

        //Construct the action bar
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);


        //Construct the slide out drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mContentView = (FrameLayout) findViewById(R.id.content_frame);
        mDrawerList = (ListView) findViewById(R.id.drawer_view);

        //Construct a new MapFragment, this will start the initialization process to create a viewable map
        mapFragment = new MyMap();
        mapFragment.setUpdatePlacesCallback(this);

        //Create the header with the name "Places" and the plus sign
        View header = (View)getLayoutInflater().inflate(R.layout.drawer_header, null);
        mDrawerList.addHeaderView(header);
        initPlaces();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(mDrawerList);
            case R.id.new_action:
                mDrawerLayout.openDrawer(mDrawerList);
            case R.id.remove_place:
                adapter.remove(currentPlace);
                mDrawerLayout.openDrawer(mDrawerList);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onResume() {
        super.onResume();
        //fm = getFragmentManager();
        if (mPreferences.getBoolean("firstrun", true)) {
            //FIX THIS - needs to run to check for play services
            ProgressDialog showWait = new ProgressDialog(this);
            showWait.setTitle("Initializing");
            showWait.setMessage("Please wait, checking for Google Play Services");
            showWait.show();
            startActivity(new Intent(this, CheckServices.class));
            showWait.dismiss();
            //FIX THIS
            firstRun();
        }

    }

    /*
    Next two methods need to be implemented
     */
    @Override
    public void onPause() {
        super.onPause();
        jsonManager.saveJSON(adapter.getPlaces());

    }

    @Override
    public void onStop() {
        super.onStop();
        jsonManager.saveJSON(adapter.getPlaces());
    }

    /*
    Custom methods that don't override built in methods
     */
    private void firstRun() {
        mDrawerLayout.openDrawer(mDrawerList);
        mPreferences.edit().putBoolean("firstrun", false).commit();
    }

    /*
    Show an alert to ask for the name of the new place they want to add
     */
    private void addPlacesPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New Place");
        alert.setMessage("Add a New Place");
        //Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                Place p = new Place();
                p.setName(value);
                mapFragment.newPlace(p);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alert.show();

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void initPlaces() {
        ArrayList<Place> places = jsonManager.getTasks();
        ImageButton addPlacesButton = (ImageButton)findViewById(R.id.add_place_button);
        addPlacesButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPlacesPressed();
                    }
                }
        );

        //Construct and set the ListAdapter
        adapter = new PlacesListAdapter(this, R.layout.drawer_list_item, places);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Have to shift back one, position is 1 based, backing array is 0 based
                currentPlace = adapter.getItem(position - 1);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        mapFragment.drawPlaces(places);
    }

    @Override
    public void updatePlaces(Place place) {
        adapter.add(place);
    }

    @Override
    public Place getPlaceById(String id) {
        return adapter.getPlaceById(id);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
