package com.example.david.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.david.myapplication.settingsFragments.OnFragmentInteractionListener;
import com.example.david.myapplication.settingsFragments.SettingsData;
import com.example.david.myapplication.settingsFragments.SettingsFOC;

public class SettingsActivity extends AppCompatActivity implements
        OnFragmentInteractionListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TabLayout tb = findViewById(R.id.settings_tabs);
        ViewPager vp = findViewById(R.id.view_pager);
        SettingsPageAdapter spa = new SettingsPageAdapter(getSupportFragmentManager());

        vp.setAdapter(spa);
        tb.setupWithViewPager(vp);
        tb.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tb.getTabAt(0).setIcon(R.drawable.ic_format_list_numbered_black_24dp);    // Data
        tb.getTabAt(1).setIcon(R.drawable.ic_foc_icon);     // FOC
        tb.getTabAt(2).setIcon(R.drawable.ic_motor_icon);     // Motor
        tb.getTabAt(3).setIcon(R.drawable.ic_build_black_24dp);     // Utilities
        tb.getTabAt(4).setIcon(R.drawable.ic_settings_applications_black_24dp);     // Controller
        tb.getTabAt(5).setIcon(R.drawable.ic_flight_takeoff_black_24dp);     // Throttle

        // Slap the top level fragment down
//        SettingsData settingsTopFrag = SettingsData.newInstance();
//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        transaction.add(R.id.fragment_holder, settingsTopFrag).commit();

        // Set default toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.settings_title);

    }

    @Override
    public void onFragmentInteraction() {

    }
}
