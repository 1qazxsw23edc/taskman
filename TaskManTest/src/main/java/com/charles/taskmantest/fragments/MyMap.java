package com.charles.taskmantest.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.charles.taskmantest.MainActivity;
import com.charles.taskmantest.activities.AddLocationActivity;
import com.charles.taskmantest.activities.SelectorActivity;
import com.charles.taskmantest.datahandler.EgressTable;
import com.charles.taskmantest.datahandler.GeoFenceTable;
import com.charles.taskmantest.datahandler.IngressTable;
import com.charles.taskmantest.datahandler.TaskManContentProvider;
import com.charles.taskmantest.eventhandlers.AreaFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by charles on 10/16/13.
 */
public class MyMap extends MapFragment implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleMap.OnMapLongClickListener,
        DrawerListFragment.ItemSelectedListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener{

    private static GoogleMap gmap = null;
    private static int distance = 100;
    private static LocationManager mLocManager;
    private static LocationListener myLocationListener;
    private static Location loc = null;
    protected static double lat;
    protected static double lon;
    private boolean currentLocationSet = false;
    private static HashMap<Long, Place> fencesMap = new HashMap<Long, Place>();
    private static Activity mActivity;
    private static final int LOADER_ID = 2;

    private LocationClient mLocationClient = null;
    private Geofence.Builder fenceBuilder = null;
    private PendingIntent mGeofenceRequestIntent;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null ) {
            Log.v("Found Saved Instance State", "FOUND MUTHEREFFER");
        }
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
        distance = 100;
        myLocationListener = new MyLocationListener();
        mLocationClient = new LocationClient(getActivity(), this, this);
        mLocManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        fenceBuilder = new Geofence.Builder();
        gmap.setOnMarkerClickListener(this);
        gmap.setOnMapLongClickListener(this);
        fillData();
        this.setRetainInstance(true);
    }

    public void placeCreated(String name, double lat, double lon, double radius, int id) {
        LatLng position = new LatLng(lat, lon);
        CameraPosition camperPosition = new CameraPosition.Builder().target(position).zoom(16.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(camperPosition);
        gmap.animateCamera(cameraUpdate);
    }

    public interface MapIsLoaded {
        public void finishedLoading();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onPause() {
        super.onPause();

        //Stop GPS
        mLocManager.removeUpdates(myLocationListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Resume GPS
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, myLocationListener);
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 10, myLocationListener);
        mLocManager.getLastKnownLocation(Context.LOCATION_SERVICE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        //Find marker associated with a place and make the circle disappear
        Iterator it = fencesMap.values().iterator();
        while (it.hasNext()) {
            Place p = (Place)it.next();
            if (p.getMarker().equals(marker)) {
                p.getCircle().setVisible(false);
                break;
            }
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLon = dragPosition.longitude;

        //Find the marker associated with a place and redraw the circle plus update map
        Iterator it = fencesMap.values().iterator();
        while (it.hasNext()) {
            Place p = (Place)it.next();
            if (p.getMarker().equals(marker)) {
                p.getCircle().setCenter(new LatLng(dragLat, dragLon));
                p.getCircle().setVisible(true);
                p.setLatitude(dragLat);
                p.setLongitude(dragLon);
                updateDB(dragLat, dragLon, p.getCircle().getRadius(), (int)p.getId());

                //new RegisterGeofence().execute(p);
                break;
            }
        }
        mLocationClient.connect();
    }

    //Long click on the map to add a new place
    @Override
    public void onMapLongClick(LatLng latLng) {
        Intent addy = new Intent(getActivity(), AddLocationActivity.class);
        addy.putExtra("latitude", latLng.latitude);
        addy.putExtra("longitude", latLng.longitude);
        if (addy.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivityForResult(addy, MainActivity.LOCATION_REQUEST_CODE);
        }
    }

    //Pretty self-explanatory.  Creates a place and stores that in our HashMap tracker
    private void createGeoFence(Place p) {
        Marker marker = gmap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(new LatLng(p.getLatitude(), p.getLongitude()))
                .title(p.getName())
                .icon(BitmapDescriptorFactory.defaultMarker())
        );
        Circle circle = gmap.addCircle(new CircleOptions()
                .center(new LatLng(p.getLatitude(), p.getLongitude()))
                .radius(p.getRadius())
                .fillColor(0x40ff0000)
                .strokeColor(Color.BLACK)
                .strokeWidth(2)
        );
        p.setCircle(circle);
        p.setMarker(marker);
        fencesMap.put(p.getId(), p);
    }

    //Listeners that handle places based on the id they're assigned in the database.
    @Override
    public void onItemSelected(long id) {
        Place p = fencesMap.get(id);
        LatLng position = new LatLng(p.getLatitude(), p.getLongitude());
        CameraPosition camperPosition = new CameraPosition.Builder().target(position).zoom(16.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(camperPosition);
        gmap.animateCamera(cameraUpdate);
    }

    @Override
    public boolean onItemDeleted(long id) {
        Place p = fencesMap.get(id);
        if (p.getCircle() != null) p.getCircle().remove();
        if (p.getMarker() != null) p.getMarker().remove();
        fencesMap.remove(id);
        return false;
    }

    @Override
    public boolean onItemAdded(long id) {
        return false;
    }

    @Override
    public void toggleDrawerOpen() {

    }

    @Override
    public void toggleDrawerClosed() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.v("Marker Clicked" , "Clicked");
        Iterator it = fencesMap.keySet().iterator();
        while (it.hasNext()) {
            Long id = (Long)it.next();
            Place p = fencesMap.get(id);
            if (p.getMarker().equals(marker)) {
                Intent place = new Intent(getActivity(), SelectorActivity.class);
                if (place.resolveActivity(getActivity().getPackageManager()) != null) {
                    place.putExtra("id", id);
                    place.putExtra("radius", (float)p.getRadius());//Has to be a float for the Geofence, so I just cast it here.
                    place.putExtra("lat", p.getLatitude());
                    place.putExtra("lon", p.getLongitude());
                    getActivity().startActivityForResult(place, MainActivity.SELECTOR_REQUEST_CODE);
                }
            }
        }
        return false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            ActionBar ab = getActivity().getActionBar();
            ab.setTitle("TaskMan");
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    /*
    Data Manager Code here.  Initiate a Loader and implement the callbacks so that it can listen for
    changes to the SQLite database.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("Loader", "Starting Loader");
        String URL = "content://com.charles.taskmantest.datahandler.TaskManContentProvider/fences_table";
        Uri places = Uri.parse(URL);
        String[] projection = new String[] {GeoFenceTable.ID, GeoFenceTable.NAME, GeoFenceTable.RADIUS,GeoFenceTable.LATITUDE,GeoFenceTable.LONGITUDE};
        return new CursorLoader(getActivity(), places, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v("Loader Finished loading", "From MyMap");
        switch (loader.getId()) {
            case LOADER_ID:
                new UpdatePlaces().execute(cursor);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
        ArrayList<Geofence> fencesList = new ArrayList();
        Log.v("Geofence: ", "Location Services Connected!");
        mGeofenceRequestIntent = createRequestPendingIntent();

        Iterator it = fencesMap.values().iterator();

        while (it.hasNext()) {
            Place p = (Place)it.next();
            fencesList.add(p.getEgressFence());
            fencesList.add(p.getIngressFence());
        }

        mLocationClient.addGeofences(fencesList, mGeofenceRequestIntent, this);
        Log.v("Added GeoFence: " , "Added INTENT to Services");
    }

    @Override
    public void onDisconnected() {
        Log.v("Location Services: ", "Geofence disconnected");
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
        // If adding the geofences was successful
        int statusCode = LocationStatusCodes.SUCCESS;
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        mLocationClient.disconnect();
        //mLocationClient = null;
        Log.v("GeoFence Added: ", "Added Fence Successfully");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private PendingIntent createRequestPendingIntent() {

        if (null != mGeofenceRequestIntent) {

            return mGeofenceRequestIntent;

        } else {
            Intent intent = new Intent(getActivity(), AreaFence.class);
            return PendingIntent.getService(
                    getActivity(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void fillData() {
        Log.v("Calling Fill Data", "From MyMap");
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }


    //Update the database with new values
    private void updateDB(double lat, double lon, double radius, int id) {
        ContentValues values = new ContentValues();
        values.put(GeoFenceTable.LATITUDE, lat);
        values.put(GeoFenceTable.LONGITUDE, lon);
        values.put(GeoFenceTable.RADIUS, radius);
        getActivity().getContentResolver().update(TaskManContentProvider.FENCE_URI, values,GeoFenceTable.ID + "=" + Integer.toString(id), null);
        values.clear();
        values.put(IngressTable.CONSTRUCT, "");
        getActivity().getContentResolver().update(TaskManContentProvider.INGRESS_URI,values,IngressTable.ID + "=" + Integer.toString(id), null);
        getActivity().getContentResolver().update(TaskManContentProvider.EGRESS_URI, values, EgressTable.ID + "=" + Integer.toString(id), null);
    }


    //Listen for location changes and update the map with that information
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            DrawerListFragment.setCurrentLatitude(lat);
            DrawerListFragment.setCurrentLongitude(lon);
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

    private class UpdatePlaces extends AsyncTask<Cursor, Integer, Place[]> {

        @Override
        protected Place[] doInBackground(Cursor... params) {
            Log.v("UpdatePlaces", "Operating on Cursor");
            Cursor c = params[0];
            ArrayList<Place> places = new ArrayList();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(GeoFenceTable.ID));

                //Check if a place has already been plotted
                if (!fencesMap.containsKey(id)) {
                    Place p = new Place();
                    p.setId(id);
                    p.setName(c.getString(c.getColumnIndexOrThrow(GeoFenceTable.NAME)));
                    p.setLatitude(c.getDouble(c.getColumnIndexOrThrow(GeoFenceTable.LATITUDE)));
                    p.setLongitude(c.getDouble(c.getColumnIndex(GeoFenceTable.LONGITUDE)));
                    p.setRadius(c.getDouble(c.getColumnIndex(GeoFenceTable.RADIUS)));
                    places.add(p);
                }
            }
            Place[] placesArray = new Place[places.size()];
            places.toArray(placesArray);

            return placesArray;
        }

        @Override
        protected void onProgressUpdate(Integer... item) {

        }

        @Override
        protected void onPostExecute(Place[] result) {

            for (int i = 0; i < result.length; i++) {
                Place p = result[i];
                //Don't re-create places
                if (fencesMap.containsKey(p.getId())) continue;
                createGeoFence(p);
            }
            mLocationClient.connect();
        }
    }

    /*
    An ugly kludge that makes a convenient way for me to store the values of places.  Makes it easier to manage the database as well as state of places
     */
    private class Place implements Serializable {
        private String name = null;
        private double latitude = 0.0f;
        private double longitude = 0.0f;
        private int id = 0;
        private double radius = 0.0f;
        private Marker marker = null;
        private Circle circle = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public long getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            if (radius == 0) radius = 100;
            this.radius = radius;
        }

        public Marker getMarker() { return marker;}

        public void setMarker(Marker marker) {
            this.marker = marker;
        }

        public Circle getCircle() {
            return circle;
        }

        public void setCircle(Circle circle) {
            this.circle = circle;
        }

        public Geofence getIngressFence() {
            fenceBuilder.setCircularRegion(this.latitude, this.longitude, (float)this.radius);
            fenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
            fenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);
            fenceBuilder.setRequestId(Integer.toString(id) + ":ingress");
            return fenceBuilder.build();
        }

        public Geofence getEgressFence() {
            fenceBuilder.setCircularRegion(this.latitude, this.longitude, (float)this.radius);
            fenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
            fenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);
            fenceBuilder.setRequestId(Integer.toString(id) + ":egress");
            return fenceBuilder.build();
        }
    }

}
