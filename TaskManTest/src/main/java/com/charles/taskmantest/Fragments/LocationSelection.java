package com.charles.taskmantest.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.charles.taskmantest.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charles on 11/29/13.
 */
public class LocationSelection extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener {
    private static final int MESSAGE_TEXT_CHANGED = 0;
    private static final int AUTOCOMPLETE_DELAY = 500;
    private static final int THRESHOLD = 3;
    private String latitude, longitude;
    private List<Address> autoCompleteSuggestionAddresses;
    private static ArrayAdapter<String> autoCompleteAdapter;
    private static Handler messageHandler;
    private View v;
    private LocationSelectionCallbacks lsc;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the layout inflater
        //LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        getActivity().setDefaultKeyMode(getActivity().DEFAULT_KEYS_SEARCH_LOCAL);
        messageHandler = new MyMessageHandler(getActivity());
        autoCompleteAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        autoCompleteAdapter.setNotifyOnChange(false);
        lsc = (LocationSelectionCallbacks)getActivity();
        /*AutoCompleteTextView locationinput = (AutoCompleteTextView) getActivity().findViewById(R.id.locationInput);
        locationinput.addTextChangedListener(this);
        locationinput.setOnItemSelectedListener(this);
        locationinput.setThreshold(THRESHOLD);
        locationinput.setAdapter(autoCompleteAdapter);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.location_selection, container, false);
        AutoCompleteTextView locationinput = (AutoCompleteTextView) v.findViewById(R.id.locationInput);
        locationinput.addTextChangedListener(this);
        locationinput.setOnItemSelectedListener(this);
        locationinput.setThreshold(THRESHOLD);
        locationinput.setAdapter(autoCompleteAdapter);
        Button button = (Button) v.findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lsc.searchButtonPressed();
            }
        });
        return v;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        messageHandler.removeMessages(MESSAGE_TEXT_CHANGED);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String value = s.toString();
        if (!"".equals(value) && value.length() >= THRESHOLD) {
            Message msg = Message.obtain(messageHandler, MESSAGE_TEXT_CHANGED, s.toString());
            messageHandler.sendMessageDelayed(msg, AUTOCOMPLETE_DELAY);
        } else {
            autoCompleteAdapter.clear();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 < autoCompleteSuggestionAddresses.size()) {
            Address selected = autoCompleteSuggestionAddresses.get(arg2);
            latitude = Double.toString(selected.getLatitude());
            longitude = Double.toString(selected.getLongitude());
        }
    }

    private void notifyResult(List<Address> suggestions) {
        //latitude = longitude = null;
        autoCompleteAdapter.clear();
        for (Address a : autoCompleteSuggestionAddresses) {
            autoCompleteAdapter.add(a.toString());//TODO: figure out a nice way to display this address in list
        }
        autoCompleteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        latitude = longitude = null;
    }

    private class MyMessageHandler extends Handler {

        private Context context;

        public MyMessageHandler(Context context) {
            this.context = context;

        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_TEXT_CHANGED) {
                String enteredText = (String) msg.obj;

                try {
                    autoCompleteSuggestionAddresses = new Geocoder(context).getFromLocationName(enteredText, 10);
                    notifyResult(autoCompleteSuggestionAddresses);
                    //notifyResult(response);
                } catch (IOException ex) {
                    Log.e(LocationSelection.class.getName(), "Failed to get autocomplete suggestions", ex);
                }
            }
        }
    }

    public interface LocationSelectionCallbacks {
        public void searchButtonPressed();
    }
}
