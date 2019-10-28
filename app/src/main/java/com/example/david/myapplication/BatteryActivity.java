package com.example.david.myapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.Random;

public class BatteryActivity extends AppCompatActivity {

    private Float[] mBatteryVoltages;
    private Integer[] mBatteryStatuses;
    private int mNumBatteries;


    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private CountDownTimer cdt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        // Set default toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.batteryToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.battery_title);

        // Let's fake some data
        Random mRand = new Random();
        mNumBatteries = 16;
        mBatteryVoltages = new Float[mNumBatteries];
        mBatteryStatuses = new Integer[mNumBatteries];
        for(int i = 0; i < mNumBatteries; i++) {
            mBatteryVoltages[i] = mRand.nextFloat() + 3.2f; // Range from 3.2 to 4.2
            mBatteryStatuses[i] = 0;
        }
        setTitleVoltage();

        recyclerView = findViewById(R.id.batteryRecycler);

        // use a grid layout manager
        layoutManager = new GridLayoutManager(getApplicationContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        // specify the adapter
        mAdapter = new BatteryAdapter(mBatteryVoltages,mBatteryStatuses);
        recyclerView.setAdapter(mAdapter);

        // How to restart?
        cdt = new CountDownTimer(30000, 1000) {
            int whichBattery = 0;
            Random mRand = new Random();
            public void onTick(long millisUntilFinished) {
                mBatteryVoltages[whichBattery] = mRand.nextFloat() + 3.2f;
                ((BatteryAdapter)mAdapter).setBattery(mBatteryVoltages[whichBattery], whichBattery);
                whichBattery = whichBattery + 1;
                if(whichBattery >= mNumBatteries) { whichBattery = 0;}
                setTitleVoltage();
            }

            public void onFinish() {
            }
        }.start();


    }

    private void setTitleVoltage() {
        float totalVoltage = 0.0f;
        for(int i = 0; i < mNumBatteries; i++) {
            totalVoltage = totalVoltage + mBatteryVoltages[i];
        }
        getSupportActionBar().setTitle(getString(R.string.battery_title) + String.format(" -- Total: %.2f",totalVoltage));

    }

    private void randomizeBatteries() {
        Random mRand = new Random();
        for(int i = 0; i < mNumBatteries; i++) {
            mBatteryVoltages[i] = mRand.nextFloat() + 3.2f; // Range from 3.2 to 4.2
            ((BatteryAdapter)mAdapter).setBattery(mBatteryVoltages[i], i);
        }
        // specify the adapter

    }
}
