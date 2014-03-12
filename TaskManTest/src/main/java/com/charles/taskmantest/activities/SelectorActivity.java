package com.charles.taskmantest.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.charles.taskmantest.R;
import com.charles.taskmantest.fragments.ActionSelector;

/**
 * Created by charles on 2/5/14.
 */
public class SelectorActivity extends Activity {
    ActionBar.Tab Tab1,Tab2;
    Fragment ingress = new ActionSelector();
    Fragment egress = new ActionSelector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("TaskMan");
        setContentView(R.layout.tab_container);

        //Construct the action bar
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);




        Tab1 = ab.newTab().setText("Enter");
        Tab2 = ab.newTab().setText("Leave");

        Bundle ingressBundle = new Bundle();
        ingressBundle.putString("role", "ingress");
        ingress.setArguments(ingressBundle);
        Tab1.setTabListener(new TabListener(ingress));

        Bundle egressBundle = new Bundle();
        egressBundle.putString("role", "egress");
        egress.setArguments(egressBundle);
        Tab2.setTabListener(new TabListener(egress));

        ab.addTab(Tab1);
        ab.addTab(Tab2);


    }

    private class TabListener implements ActionBar.TabListener {
        Fragment fragment;

        public TabListener (Fragment fragment) {
            this.fragment = fragment;
        }
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            ft.replace(R.id.tab_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }
}
