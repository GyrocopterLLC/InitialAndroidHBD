package com.example.david.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.david.myapplication.settingsFragments.SettingsFOC;
import com.example.david.myapplication.settingsFragments.SettingsTop;

public class SettingsActivity extends AppCompatActivity implements
        SettingsTop.OnFragmentInteractionListener, SettingsFOC.OnFragmentInteractionListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Slap the top level fragment down
        SettingsTop settingsTopFrag = SettingsTop.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_holder, settingsTopFrag).commit();

        // Set default toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onFragmentInteraction(String string) {
        Snackbar.make(findViewById(R.id.fragment_holder), string,Snackbar.LENGTH_SHORT).show();
        if(string == "FOC pressed") {
            SettingsFOC settingsFOCfrag = SettingsFOC.newInstance();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_holder, settingsFOCfrag).addToBackStack(null).commit();
        }
    }

    @Override
    public void onFragmentInteraction() {

    }
}
