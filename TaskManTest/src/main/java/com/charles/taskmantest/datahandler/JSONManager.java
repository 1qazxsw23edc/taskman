package com.charles.taskmantest.datahandler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.charles.taskmantest.Place;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charles on 9/29/13.
 */
public class JSONManager {
    private static final JSONManager INSTANCE = new JSONManager();
    private SharedPreferences mPreferences;
    private String LOCS = "locations";

    private JSONManager() {};

    public static JSONManager getInstance() {
        return INSTANCE;
    }

    public void setmPreferences(Context context) {
        //Setup shared preferences
        mPreferences = context.getSharedPreferences("com.charles.TaskManTest", Activity.MODE_PRIVATE);
        if (mPreferences.getBoolean("firstrun", true)) {
            //initJSON();
        }
    }

    public boolean saveJSON(ArrayList<Place> places) {
        Gson gson = new Gson();
        String json = gson.toJson(places);
        Log.v("Magic", json);
        mPreferences.edit().putString(LOCS, json).commit();
        return false;
    }

    //This needs to go, I only want to use the manager for saving and retrieving once per run
    public Place getPlace(String key) {
        String locationsJSON = mPreferences.getString(LOCS ,null);
        if (locationsJSON == null) return null;

        Gson gson = new Gson();
        //ArrayList<Place> tasks = gson.fromJson(locationsJSON, ArrayList.class);
        ArrayList<Place> tasks = gson.fromJson(locationsJSON, new TypeToken<List<Place>>(){}.getType());
        for (Place place : tasks) {
            String name = place.getName();
            if (name.equals(key)) return place;
        }
        return null;
    }

    //Generate the list of items
    public ArrayList<Place> getTasks() {
        String locationsJSON = mPreferences.getString(LOCS ,null);
        if (locationsJSON == null) return new ArrayList<Place>();
        Gson gson = new Gson();
        //ArrayList<Place> tasks = gson.fromJson(locationsJSON, ArrayList.class);
        ArrayList<Place> tasks = gson.fromJson(locationsJSON, new TypeToken<List<Place>>(){}.getType());

        Log.v("Magic", Integer.toString(tasks.size()));
        return tasks;
    }


    public boolean initJSON() {
        Place p = new Place();
        p.setName("Home");
        ArrayList<Place> places = new ArrayList<Place>();
        places.add(p);
        saveJSON(places);
        return true;
    }
}
