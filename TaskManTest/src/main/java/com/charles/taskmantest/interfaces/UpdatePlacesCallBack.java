package com.charles.taskmantest.interfaces;

import com.charles.taskmantest.Place;

/**
 * Created by charles on 10/28/13.
 */
public interface UpdatePlacesCallBack {
    public void updatePlaces(Place place);

    public Place getPlaceById(String id);
}
