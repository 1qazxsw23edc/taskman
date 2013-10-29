package com.charles.taskmantest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by charles on 10/4/13.
 */
public class ActionMenu extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.action_layout, container, false);
    }

    @Override
    public void onPause() {

    }
}
