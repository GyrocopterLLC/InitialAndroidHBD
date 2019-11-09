package com.example.david.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements BluetoothUserFragmentInteractionListener {
    public static final String MAIN_TAG = "DEBUG_LOG_MAIN_TAG";
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter BA;
    private Handler mHandler;
    private BluetoothSocket mSocket = null;
    public BTReadWriteThread btReadWriteThread;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Load default fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.frameLayout, GaugeClusterFragment.newInstance());
        ft.commit();

        // Set up a back stack changed listener
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // What's my current fragment?
                if(getActiveFragment() instanceof GaugeClusterFragment) {
                    menuSetupForGauge();
                }
                if(getActiveFragment() instanceof BatteryFragment) {
                    menuSetupForBattery();
                }
            }
        });

        // Check if BT adapter is disabled, and ask to enable it.
        BA = BluetoothAdapter.getDefaultAdapter();

        if(!BA.isEnabled()) {
            Snackbar.make(findViewById(R.id.main_view), "Bluetooth is needed. Turn on?", Snackbar.LENGTH_LONG).setAction("TURN ON BLUETOOTH", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                }
            }).show();
        }
    }

    private void menuSetupForGauge() {
        mMenu.findItem(R.id.action_battery).setVisible(true);
        mMenu.findItem(R.id.action_connect).setVisible(true);
        mMenu.findItem(R.id.action_settings).setVisible(true);
        mMenu.findItem(R.id.action_gauge).setVisible(false);
    }

    private void menuSetupForBattery() {
        mMenu.findItem(R.id.action_battery).setVisible(false);
        mMenu.findItem(R.id.action_connect).setVisible(true);
        mMenu.findItem(R.id.action_settings).setVisible(true);
        mMenu.findItem(R.id.action_gauge).setVisible(true);
    }

    private void menuSetupForSettings() {
        mMenu.findItem(R.id.action_battery).setVisible(true);
        mMenu.findItem(R.id.action_connect).setVisible(true);
        mMenu.findItem(R.id.action_settings).setVisible(false);
        mMenu.findItem(R.id.action_gauge).setVisible(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean(_KEY_, _value_);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar,menu);
        menuSetupForGauge();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentTransaction ft;
        Intent intent;
        switch(item.getItemId()) {
            case R.id.action_connect:
                intent = new Intent(this, BTConnectActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                // Change menu items
                menuSetupForSettings();
                // Swap fragment
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, SettingsFragment.newInstance());
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.action_battery:
                // Change menu items
                menuSetupForBattery();
                // Swap fragment
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, BatteryFragment.newInstance());
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.action_gauge:
                // Change menu items
                menuSetupForGauge();
                // Swap fragment
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, GaugeClusterFragment.newInstance());
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(((GlobalSettings)getApplication()).isConnected()){
            mSocket = ((GlobalSettings)getApplication()).getSocket();
            // Create new handler for messaging the BT device
            mHandler = new Handler(getMainLooper(), new MainCallback());
            // Start the BT read-write thread
            btReadWriteThread = new BTReadWriteThread(mSocket, mHandler);
            new Thread(btReadWriteThread).start();
        }
    }

    @Override
    public void Write(StringBuffer outData) {
        if(((GlobalSettings)getApplication()).isConnected()){
            if(btReadWriteThread != null) {
                btReadWriteThread.write(outData);
            }
        }
    }

    @Override
    public Handler getActivityHandler() {
        return mHandler;
    }

    private BluetoothUserFragment getActiveFragment() {
        return (BluetoothUserFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout);
    }


    public class MainCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BTReadWriteThread.MessageConstants.MESSAGE_READ:
                    // Call the fragment's read function
                    getActiveFragment().ReceiveDataCallback((StringBuffer)msg.obj);
                    break;
                case BTReadWriteThread.MessageConstants.MESSAGE_WRITE:
                    break;
                case BTReadWriteThread.MessageConstants.MESSAGE_ERROR:
                    // Call the fragment's error function
                    getActiveFragment().ErrorCallback();
                    break;
                case BTReadWriteThread.MessageConstants.MESSAGE_DISCONNECTED:
                    ((GlobalSettings)getApplication()).setConnected(false);
                    ((GlobalSettings)getApplication()).setDevice(null);
                    ((GlobalSettings)getApplication()).setSocket(null);
                    // Call the fragment's disconnected function
                    getActiveFragment().DisconnectCallback();
                    break;
            }
            return true; // No further handling required.
        }
    }

    @Override
    protected void onPause() {

        // If the Bluetooth thread is running, stop it
        if(btReadWriteThread != null) {
            if (btReadWriteThread.isRunning()) {
                btReadWriteThread.stop();
            }
            btReadWriteThread = null;
        }

        if(mHandler != null) {
            // Remove handlers and callbacks
            mHandler = null;
        }

        super.onPause();
    }

    public void onClickSimulate(View v) {
        // Should only be called when gaugecluster fragment is active
        if(getActiveFragment() instanceof GaugeClusterFragment) {
            ((GaugeClusterFragment)getActiveFragment()).onClickSimulate();
        }
    }

    public void onClickAskForData(View v) {
        // Should only be called when gaugecluster fragment is active
        if(getActiveFragment() instanceof GaugeClusterFragment) {
            ((GaugeClusterFragment)getActiveFragment()).onClickAskForData();
        }
    }


    public void onClickSettingsRead(View view) {
        // Should only be called when settings fragment is active
        if(getActiveFragment() instanceof SettingsFragment) {
            ((SettingsFragment)getActiveFragment()).onClickRead();
        }
    }

    @Override
    protected void onDestroy() {
        if(((GlobalSettings)getApplication()).isConnected()){
            try {
                ((GlobalSettings) getApplication()).getSocket().close();
                ((GlobalSettings)getApplication()).setConnected(false);
            } catch (IOException e) {
                Log.e(MAIN_TAG, "Could not close the client socket", e);
            }
        }
        super.onDestroy();
    }


}