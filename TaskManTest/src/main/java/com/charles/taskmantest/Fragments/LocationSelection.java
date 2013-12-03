package com.charles.taskmantest.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.charles.taskmantest.R;
import com.google.android.gms.maps.model.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by charles on 11/29/13.
 */
public class LocationSelection extends Fragment {

    private static ArrayAdapter<String> autoCompleteAdapter;
    private View v;
    private LocationSelectionCallbacks lsc;
    private static double lat = 0.0;
    private static double lon = 0.0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setDefaultKeyMode(getActivity().DEFAULT_KEYS_SEARCH_LOCAL);

        lsc = (LocationSelectionCallbacks)getActivity();
    }

    //Create the view and bind actions to the buttons
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.location_selection, container, false);

        final AutoCompleteTextView locationinput = (AutoCompleteTextView) v.findViewById(R.id.location_autocomplete);
        locationinput.setAdapter(new AutoCompleteAdapter(getActivity()));
        locationinput.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address selection = (Address)parent.getItemAtPosition(position);
                lat = selection.getLatitude();
                lon = selection.getLongitude();
                //Log.v("LocationSelection", getFormattedAddress(selection));
                locationinput.setText(getFormattedAddress(selection));
                locationinput.dismissDropDown();
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(LocationSelection.this.getView().getWindowToken(), 0);

            }
        });
        Button button = (Button) v.findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lsc.searchButtonPressed();
            }
        });
        return v;
    }

    public interface LocationSelectionCallbacks {
        public void searchButtonPressed();
    }

    // And the corresponding Adapter
    private class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable {
        private long max_meters = 10000;
        private LayoutInflater mInflater;
        private Geocoder mGeocoder;
        private StringBuilder mSb = new StringBuilder();
        private String locale;

        public AutoCompleteAdapter(final Context context) {
            super(context, -1);
            mInflater = LayoutInflater.from(context);
            mGeocoder = new Geocoder(context, Locale.US);
            locale = getActivity().getResources().getConfiguration().locale.getCountry();
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
            if (addy.trim().length() > 0) {
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

        @Override
        public Filter getFilter() {
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(final CharSequence constraint) {
                    List<Address> addressList = null;
                    if (constraint != null) {
                        try {
                            double lat = MyMap.lat;
                            double lon = MyMap.lon;

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

    private final String getFormattedAddress(Address address) {
        StringBuilder mSb = new StringBuilder();
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

    private LatLng getBearingCoord(double lat, double lon, int bearing, long distance) {
        com.javadocmd.simplelatlng.LatLng newPoint =  LatLngTool.travel(new com.javadocmd.simplelatlng.LatLng(lat, lon), bearing, distance, LengthUnit.KILOMETER);
        return new LatLng(newPoint.getLatitude(), newPoint.getLongitude());
    }

}
