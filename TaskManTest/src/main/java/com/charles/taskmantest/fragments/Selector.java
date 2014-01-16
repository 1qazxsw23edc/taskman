package com.charles.taskmantest.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.charles.taskmantest.R;

/**
 * Created by charles on 12/10/13.
 */
public class Selector extends Fragment implements ActionBar.TabListener{
    private double lat = 0.0;
    private double lon = 0.0;
    private double radius = 0.0;
    private long idCode = 0;
    private String name = "";
    private View v = null;

    public Selector(String name, Long id, double lat, double lon, double radius) {
        this.idCode = id;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.name = name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {
        ActionBar ab = getActivity().getActionBar();
        for (int i = 0; i < ab.getTabCount(); i++) {
            ab.getTabAt(i).setTabListener(this);
        }

        v = inflater.inflate(R.layout.selector_layout, null);
        v.findViewById(R.id.exit).setVisibility(View.INVISIBLE);
        LinearLayout ll = ((LinearLayout)v.findViewById(R.id.enter));
        for (int i = 0; i < 4; i++) {
            Button b = (Button) inflater.inflate(R.layout.custom_button, ll, false);
            ll.addView(b);
        }
        return v;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (tab.getText().toString().equals("Leave")) {
            v.findViewById(R.id.enter).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.exit).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.exit).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.enter).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    private class WriteOutData extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return null;
        }
    }
}
