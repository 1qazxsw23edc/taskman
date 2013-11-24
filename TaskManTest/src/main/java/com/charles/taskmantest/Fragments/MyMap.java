package com.charles.taskmantest.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.charles.taskmantest.Place;
import com.charles.taskmantest.geofence.SimpleGeoFence;
import com.charles.taskmantest.interfaces.UpdatePlacesCallBack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by charles on 10/16/13.
 */
public class MyMap extends MapFragment implements GoogleMap.OnMarkerDragListener{
    private static GoogleMap gmap = null;
    final int RQS_GooglePlayServices = 1;
    private static int distance = 100;
    private static LocationManager mLocManager;
    private static LocationListener myLocationListener;
    private static LocationClient mLocationClient;
    private static Location loc = null;
    protected static volatile double lat;
    protected static volatile double lon;
    private boolean currentLocationSet = false;
    private static HashMap<Marker, Circle> fencesMap = new HashMap<Marker, Circle>();
    private static Activity mActivity;
    private static UpdatePlacesCallBack upc = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getApplicationContext());
        if (resultCode == ConnectionResult.SUCCESS){

            if(gmap == null){
                gmap = this.getMap();
                if(gmap != null){
                    gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    gmap.setOnMarkerDragListener(this);
                    gmap.setMyLocationEnabled(true);
                    //gmap.setOnMapLongClickListener(this);
                }else{
                    return;
                }
            }
        }else{
            GooglePlayServicesUtil.getErrorDialog(resultCode, this.getActivity(), RQS_GooglePlayServices);
        }
        distance = 100;
        myLocationListener = new MyLocationListener();
        mLocManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocManager.removeUpdates(myLocationListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,10, myLocationListener);
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 10, myLocationListener);
        mLocManager.getLastKnownLocation(Context.LOCATION_SERVICE);

    }

    @Override
    public void onMarkerDragStart(Marker marker){
        Circle c = fencesMap.get(marker);
        c.remove();

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLon = dragPosition.longitude;
        Circle circle = fencesMap.get(marker);
        Place place = upc.getPlaceById(marker.getId());
        place.setGeofence(marker.getTitle(), dragLat, dragLon, circle.getRadius(), 0, 0);
        if (marker != null) marker.remove();
        if (circle != null) circle.remove();
        fencesMap.remove(marker);
        createGeoFence(place);
    }

    /*@Override
    public void onMapLongClick(LatLng latLng) {
        createGeoFence(latLng.latitude, latLng.longitude, distance, "CIRCLE", "A clever title");
    }*/

    //Create the actual drawn geofences on the map
    private void createGeoFence(Place place) {
        SimpleGeoFence sgf = place.getGeoFence();
        double latitude = sgf.getLatitude();
        double longitude = sgf.getLongitude();
        String title = place.getName();
        double size = sgf.getRadius();
        Log.v("Magic", Double.toString(size));

        Marker fence = gmap.addMarker(new MarkerOptions()
            .draggable(true)
            .position(new LatLng(latitude,longitude))
            .title(title)
            .icon(BitmapDescriptorFactory.defaultMarker())
        );
        Circle circle = gmap.addCircle(new CircleOptions()
              .center(new LatLng(latitude,longitude))
              .radius(size)
              .fillColor(0x40ff0000)
              .strokeColor(Color.BLACK)
              .strokeWidth(2)
        );
        place.setId(fence.getId());
        fencesMap.put(fence, circle);
    }

    //Create a new Place, plot it on the map and then update the list to reflect those changes by using a callback
    public void newPlace(Place p) {
        Log.v("Magic", "Name: " + p.getName() + "\nLat: " + Double.toString(lat) + "\nLon:" + Double.toString(lon));
        new ConstructPlaceTask().execute(p);
        upc.updatePlaces(p);
    }

    //Listen for location changes and update the map with that information
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();


            Log.v("Magic", "LOCATION CHANGED!" + "\nLat: " + Double.toString(lat) + "\nLon: " +  Double.toString(lon));
            if (!currentLocationSet) {
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));
                currentLocationSet = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private class MarkerClickedListener implements GoogleMap.OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker marker) {
            String title = marker.getTitle();
            Place place = upc.getPlaceById(marker.getId());
            return false;
        }
    }

    public void setUpdatePlacesCallback(UpdatePlacesCallBack upc) {
        this.upc = upc;
    }

    public void drawPlaces(ArrayList<Place> places) {
        Place [] ps = new Place[places.size()];
        places.toArray(ps);
        Log.v("Magic", Integer.toString(ps.length));
        new ConstructPlaceTask().doInBackground(ps);
    }

    private class ConstructPlaceTask extends AsyncTask<Place, Integer, Place[]> {

        @Override
        protected Place[] doInBackground(Place... params) {
            for (int i = 0; i < params.length; i++) {
                Place p = params[0];
                if (p.getGeoFence() == null) {
                    p.setGeofence(p.getName(),lat, lon, distance, 0, 0);
                    Log.v("Magic", "Name: " + p.getName() + "\nLat: " + Double.toString(MyMap.this.lat) + "\nLon:" + Double.toString(MyMap.this.lon));
                }
                publishProgress(i);
            }
            return params;
        }

        @Override
        protected void onProgressUpdate(Integer... item) {
            Log.v("Magic", "Processed: " + Integer.toString(item[0]) +" fences");
        }

        @Override
        protected void onPostExecute(Place[] result) {
            for (int i = 0; i < result.length; i++) {
                createGeoFence(result[i]);
            }
        }
    }
}
