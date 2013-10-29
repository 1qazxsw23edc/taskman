package com.charles.taskmantest.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charles.taskmantest.R;

/**
 * Created by charles on 10/16/13.
 */
public class Selectors extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.action_layout, container, false);
    }
}
