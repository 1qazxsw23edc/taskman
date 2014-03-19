package com.charles.taskmantest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.charles.taskmantest.fragments.DrawerListFragment;
import com.charles.taskmantest.fragments.MyMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.MapFragment;


public class MainActivity extends Activity implements
        DrawerListFragment.ItemSelectedListener,
        MyMap.MapIsLoaded,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener {

    private static DrawerLayout mDrawerLayout;
    private SharedPreferences mPreferences;
    private static MyMap mapFragment = null;
    private static ListView mDrawerList;
    //private static final int LOADER_ID = 0;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    public final static int LOCATION_REQUEST_CODE = 1002;
    public final static int SELECTOR_REQUEST_CODE = 1003;

    //Geofence variables
    // Holds the location client
    private LocationClient mLocationClient;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    // Defines the allowable request types.
    public static enum REQUEST_TYPE = {ADD}

    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("TaskMan");
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);


        //Detect whether or not it's first run
        mPreferences = getSharedPreferences("com.charles.TaskManTest", MODE_PRIVATE);

        //Construct the slide out drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_view);

        //Construct a new MapFragment, this will start the initialization process to create a viewable map
        mapFragment = new MyMap();
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
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                break;
            case R.id.new_action:
                mDrawerLayout.openDrawer(mDrawerList);
                break;
            case R.id.remove_place:
                //adapter.remove(currentPlace);
                //mDrawerLayout.openDrawer(mDrawerList);
                break;
        }
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/fences_table";
        Uri placesUri = Uri.parse(URL);
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPlayServices()) {
            Log.v("Play Services Check", "Play Services Found");
        }
        //fm = getFragmentManager();
        if (mPreferences.getBoolean("firstrun", true)) {
            //FIX THIS - needs to run to check for play services
            ProgressDialog showWait = new ProgressDialog(this);
            showWait.setTitle("Initializing");
            showWait.setMessage("Please wait, checking for Google Play Services");
            showWait.show();
            showWait.dismiss();
            firstRun();
        }
    }

    /*
    Custom methods that don't override built in methods
     */
    private void firstRun() {
        mPreferences.edit().putBoolean("firstrun", false).commit();
    }

    @Override
    public void onItemSelected(long id) {
        mDrawerLayout.closeDrawer(mDrawerList);
        mapFragment.onItemSelected(id);
    }

    @Override
    public boolean onItemDeleted(long id) {
        mDrawerLayout.closeDrawer(mDrawerList);
        mapFragment.onItemDeleted(id);
        return false;
    }

    @Override
    public boolean onItemAdded(long id) {
        mDrawerLayout.closeDrawer(mDrawerList);
        return false;
    }

    @Override
    public void toggleDrawerOpen() {
        mDrawerLayout.openDrawer(mDrawerList);
    }

    @Override
    public void toggleDrawerClosed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void finishedLoading() {

    }

    //Run a check for Google Play Services, prompt to install it if you don't have it

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    //Take the results of the internal intents and parse them
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services muse be installed.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case LOCATION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("name") && data.hasExtra("lat") && data.hasExtra("lon")) {
                        String name = data.getExtras().getString("name");
                        double lat = data.getExtras().getDouble("lat");
                        double lon = data.getExtras().getDouble("lon");
                        double radius = data.getExtras().getDouble("radius");
                        int id = data.getExtras().getInt("id");
                        if (name != null && lat != 0 && lon != 0) {
                            FragmentManager fm = getFragmentManager();
                            MapFragment mMap = (MapFragment)fm.findFragmentById(R.id.content_view);
                            ((MyMap)mMap).placeCreated(name, lat, lon, radius, id);
                        }
                    }
                }
        }
        super.onActivityResult(requestCode,resultCode,data);
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

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
