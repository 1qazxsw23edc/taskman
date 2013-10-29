package com.charles.taskmantest.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.charles.taskmantest.Place;
import com.charles.taskmantest.datahandler.JSONManager;
import com.charles.taskmantest.geofence.SimpleGeoFence;
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
public class MyMap extends MapFragment implements GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {
    private GoogleMap gmap = null;
    final int RQS_GooglePlayServices = 1;
    private int distance;
    private LocationManager mLocManager;
    private LocationListener myLocationListener;
    private LocationClient mLocationClient;
    private double lat;
    private double lon;
    private boolean currentLocationSet = false;
    private JSONManager jsonManager =null;
    private HashMap<Marker, Circle> fencesMap = new HashMap<Marker, Circle>();
    private ArrayList<Place> places = new ArrayList<Place>();

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
                    gmap.setOnMapLongClickListener(this);
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
        jsonManager = JSONManager.getInstance();
        places = jsonManager.getTasks();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocManager.removeUpdates(myLocationListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l,0.0f, myLocationListener);
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        mLocManager.getLastKnownLocation(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMarkerDragStart(Marker marker){

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
        Place place = jsonManager.getPlace(marker.getTitle());
        place.setGeofence(marker.getTitle(), dragLat, dragLon, circle.getRadius(), 0, 0);
        if (marker != null) marker.remove();
        if (circle != null) circle.remove();
        fencesMap.remove(marker);
        createGeoFence(place);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        createGeoFence(latLng.latitude, latLng.longitude, distance, "CIRCLE", "A clever title");
    }

    private void zoomHome() {
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));
    }

    //Create the actual drawn geofences on the map

    private void createGeoFence(double latitude, double longitude, int radius, String geoFenceType, String title) {

        Marker fence = gmap.addMarker(new MarkerOptions()
            .draggable(true)
            .position(new LatLng(latitude, longitude))
            .title(title)
            .icon(BitmapDescriptorFactory.defaultMarker())
        );

        Circle circle = gmap.addCircle(new CircleOptions()
            .center(new LatLng(latitude, longitude))
            .radius(radius)
            .fillColor(0x40ff0000)
            .strokeColor(Color.BLACK)
            .strokeWidth(2)
        );
        fencesMap.put(fence, circle);
    }

    private void createGeoFence(String geoFenceType, String title) {

    }

    private void createGeoFence(Place place) {
        SimpleGeoFence sgf = place.getGeoFence();
        double latitude = sgf.getLatitude();
        double longitude = sgf.getLongitude();
        String title = place.getName();
        double size = sgf.getRadius();

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
        fencesMap.put(fence, circle);
    }

    //Listen for location changes and update the map with that information
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            if (!currentLocationSet) {
                zoomHome();
                currentLocationSet = true;
            }
            //Log.v("TaskMan", "LAT" + Double.toString(lat) + " LONG" + Double.toString(lon));
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
            Place place = jsonManager.getPlace(title);
            return false;
        }
    }
}
