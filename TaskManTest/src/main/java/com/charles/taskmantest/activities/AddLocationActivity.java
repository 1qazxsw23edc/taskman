package com.charles.taskmantest.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.charles.taskmantest.R;
import com.charles.taskmantest.datahandler.GeoFenceTable;
import com.charles.taskmantest.datahandler.TaskManContentProvider;
import com.google.android.gms.maps.model.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by charles on 2/7/14.
 */
public class AddLocationActivity extends Activity {

    private static ArrayAdapter<String> autoCompleteAdapter;
    private View v;
    private static double lat = 0.0;
    private static double lon = 0.0;
    private static double clat = 0.0;
    private static double clon = 0.0;
    private final int radMin = 1;
    private final int radMax = 500;
    private static double radius;
    private static TextView radiusView;
    private static EditText editPlaceName;
    private static SeekBar radiusBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultKeyMode(this.DEFAULT_KEYS_SEARCH_LOCAL);
        setContentView(R.layout.location_selection);
        Intent intent = getIntent();
        clat = intent.getDoubleExtra("latitude", 0);
        clon = intent.getDoubleExtra("longitude", 0);
        v = this.getWindow().getDecorView().getRootView();
        //v = inflater.inflate(R.layout.location_selection, container, false);

        final AutoCompleteTextView locationinput = (AutoCompleteTextView) v.findViewById(R.id.location_autocomplete);
        setupAutoCompleteAddress(locationinput);

        radiusView = (TextView) v.findViewById(R.id.radiusView);
        editPlaceName = (EditText) v.findViewById(R.id.editPlaceName);
        radiusBar = (SeekBar) v.findViewById(R.id.seekBar);
        radiusBar.setMax(radMax);
        setupRadiusSeekBar(radiusBar);

        Button button = (Button) v.findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editPlaceName.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddLocationActivity.this, "Must set a Name", Toast.LENGTH_LONG).show();
                    return;
                }
                ContentValues values = new ContentValues();
                values.put(GeoFenceTable.NAME, editPlaceName.getText().toString());
                values.put(GeoFenceTable.RADIUS, radius);
                values.put(GeoFenceTable.LATITUDE, lat);
                values.put(GeoFenceTable.LONGITUDE, lon);
                Uri ins = AddLocationActivity.this.getContentResolver().insert(TaskManContentProvider.FENCE_URI, values);
                //lsc.placeCreated(editPlaceName.getText().toString(), lat, lon);
                InputMethodManager in = (InputMethodManager) AddLocationActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(AddLocationActivity.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
                AddLocationActivity.this.finish();
            }
        });


    }

    //Setup the autocomplete
    private final void setupAutoCompleteAddress(final AutoCompleteTextView locationinput) {
        Geocoder gcoder = new Geocoder(this);
        try {
            List<Address> currentAddress = gcoder.getFromLocation(clat, clon, 1);
            //locationinput.setText(getFormattedAddress(currentAddress.get(0)));
            locationinput.setHint(getFormattedAddress(currentAddress.get(0)));
        } catch (IOException ioe) {

        }

        locationinput.setAdapter(new AutoCompleteAdapter(this));
        locationinput.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address selection = (Address)parent.getItemAtPosition(position);
                lat = selection.getLatitude();
                lon = selection.getLongitude();
                locationinput.setText(getFormattedAddress(selection));
                locationinput.dismissDropDown();
                InputMethodManager in = (InputMethodManager) AddLocationActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(AddLocationActivity.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            }
        });
    }

    //Add an action to the SeekBar to update a textview with the size of the radius in meters
    private final void setupRadiusSeekBar(final SeekBar radBar) {
        radBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress == 0) {
                        radiusView.setText("Radius: " + Integer.toString(1));
                        radius = (double)1;
                    } else {
                        radiusView.setText("Radius: " + Integer.toString(progress));
                        radius = (double)progress;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //Convert and Address object into something readable
    private final String getFormattedAddress(Address address) {
        StringBuilder mSb = new StringBuilder();
        mSb.setLength(0);
        lat = address.getLatitude();
        lon = address.getLongitude();
        final int addressLineSize = address.getMaxAddressLineIndex();
        for (int i = 0; i < addressLineSize; i++) {
            mSb.append(address.getAddressLine(i));
            if (i != addressLineSize - 1) {
                mSb.append(", ");
            }
        }
        return mSb.toString();
    }

    //The method to get the current address based on your current lat/long
    private List<Address> getListFromCoord (Geocoder gCoder, String name, double lat, double lon, int distance, int depth) throws IOException {
        //Get two coordinates based on bearing and distance
        LatLng norEast = getBearingCoord(lat, lon, 45, distance);
        LatLng soWest = getBearingCoord(lat, lon, 225, distance);

        double lowLefLat = soWest.latitude;
        double lowLefLon = soWest.longitude;
        double upRighLat = norEast.latitude;
        double upRighLon = norEast.longitude;
        List<Address> addresses = gCoder.getFromLocationName(name, 5, lowLefLat, lowLefLon, upRighLat, upRighLon);

        if (addresses.size() == 0 && depth != 5) {
            //Recurse because no results were returned
            return getListFromCoord(gCoder, name, lat, lon, distance +500, depth + 1);
        } else {
            return addresses;
        }

    }

    /*
    Compute a LatLng that is based on your current location, distance, and a bearing using the Haversine forumula
     */
    private LatLng getBearingCoord(double lat, double lon, int bearing, long distance) {
        com.javadocmd.simplelatlng.LatLng newPoint =  LatLngTool.travel(new com.javadocmd.simplelatlng.LatLng(lat, lon), bearing, distance, LengthUnit.KILOMETER);
        return new LatLng(newPoint.getLatitude(), newPoint.getLongitude());
    }

    // An adapter that automatically takes what you're typing into the text field and tries to find addresses based on that
    private class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable {
        //private long max_meters = 10000;
        private LayoutInflater mInflater;
        private Geocoder mGeocoder;
        private StringBuilder mSb = new StringBuilder();
        private String locale;

        public AutoCompleteAdapter(final Context context) {
            super(context, -1);
            mInflater = LayoutInflater.from(context);
            mGeocoder = new Geocoder(context, Locale.US);
            locale = AddLocationActivity.this.getResources().getConfiguration().locale.getCountry();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            String addy = createFormattedAddressFromAddress(getItem(position));
            if (addy.trim().length() > 0) { //Ignore 0 length addresses
                tv.setText(addy);
            }
            return tv;
        }

        private String createFormattedAddressFromAddress(final Address address) {
            mSb.setLength(0);
            final int addressLineSize = address.getMaxAddressLineIndex();
            for (int i = 0; i < addressLineSize; i++) {
                mSb.append(address.getAddressLine(i));
                if (i != addressLineSize - 1) {
                    mSb.append(", ");
                }
            }
            return mSb.toString();
        }

        //This creates a filter that returns results based on a square drawn around you with the Haversine Formula
        @Override
        public Filter getFilter() {
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(final CharSequence constraint) {
                    List<Address> addressList = null;
                    if (constraint != null) {
                        try {
                            double lat = clat;
                            double lon = clon;

                            addressList = getListFromCoord(mGeocoder,(String) constraint, lat, lon, 500, 0);
                            //
                        } catch (IOException e) {
                        }
                    }
                    if (addressList == null) {
                        addressList = new ArrayList<Address>();
                    }

                    final FilterResults filterResults = new FilterResults();
                    filterResults.values = addressList;
                    filterResults.count = addressList.size();

                    return filterResults;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(final CharSequence contraint, final FilterResults results) {
                    clear();
                    for (Address address : (List<Address>) results.values) {
                        String cc = address.getCountryCode();
                        if (address != null && cc != null && address.getCountryCode().equals(locale)) {
                            //Log.v("LocationSelection", getFormattedAddress(address));
                            add(address);
                        }
                    }
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }

                @Override
                public CharSequence convertResultToString(final Object resultValue) {
                    return resultValue == null ? "" : ((Address) resultValue).getAddressLine(0);
                }
            };
            return myFilter;
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        String name = editPlaceName.getText().toString();
        if (name != null && name.length() > 0 && lat != 0 && lon != 0) {
            intent.putExtra("name", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lon", lon);
            setResult(RESULT_OK, intent);
        } else {
            setResult(this.RESULT_CANCELED);
        }

        super.finish();
    }
}
