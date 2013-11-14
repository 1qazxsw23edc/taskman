package com.charles.taskmantest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by charles on 10/4/13.
 */
public class PlacesListAdapter extends ArrayAdapter<Place> {
    private Place place;
    private ArrayList<Place> places;
    private int resource;
    private Context context;

    public PlacesListAdapter(Context context, int resource, ArrayList<Place> places) {
        super(context, resource, places);
        this.places = places;
        this.resource = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        String place = null;
        if (!places.isEmpty()) {
            place = ((Place)places.get(position)).getName();
        }

        //Get the current place
        if (convertView == null) {

            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.drawer_list_item, null);
            if (place == null || places.isEmpty() ) {
                ((TextView)v).setText("No Places Yet");
            } else {
                ((TextView)v).setText(place);
            }

        } else {
            if (place == null || places.isEmpty() ) {
                ((TextView)v).setText("No Places Yet");
            } else {
                ((TextView)v).setText(place);
            }
        }
        return v;
    }

    public Place getPlaceById(String id) {
        for (Place place : places) {
            if (place.getId().equals(id)) return place;
        }
        return null;
    }

    public ArrayList<Place> getPlaces() {
        return places;
    }

}
