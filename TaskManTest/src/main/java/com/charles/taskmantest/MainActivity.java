package com.charles.taskmantest;

import android.app.ActionBar;
import android.app.Activity;
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

import com.charles.taskmantest.Fragments.DrawerListFragment;
import com.charles.taskmantest.Fragments.MyMap;
import com.charles.taskmantest.geofence.CheckServices;


public class MainActivity extends Activity implements DrawerListFragment.ItemSelectedListener {

    private static DrawerLayout mDrawerLayout;
    private SharedPreferences mPreferences;
    private static MyMap mapFragment = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("TaskMan");



        //Detect whether or not it's first run
        mPreferences = getSharedPreferences("com.charles.TaskManTest", MODE_PRIVATE);

        //Construct the action bar
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);


        //Construct the slide out drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mDrawerList = (ListView) findViewById(R.id.drawer_view);

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
                //mDrawerLayout.openDrawer(mDrawerList);
            case R.id.new_action:
                //mDrawerLayout.openDrawer(mDrawerList);
            case R.id.remove_place:
                //adapter.remove(currentPlace);
                //mDrawerLayout.openDrawer(mDrawerList);
        }
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/fences_table";
        Uri placesUri = Uri.parse(URL);
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
            firstRun();
        }

    }

    /*
    Next two methods need to be implemented
     */
    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
    Custom methods that don't override built in methods
     */
    private void firstRun() {
        //mDrawerLayout.openDrawer(mDrawerList);
        mPreferences.edit().putBoolean("firstrun", false).commit();
    }

    @Override
    public void onItemSelected(int id) {
        Log.v("Successful Callback", "Is Successful");
    }
}
