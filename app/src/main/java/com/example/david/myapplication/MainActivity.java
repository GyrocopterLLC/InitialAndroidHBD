package com.example.david.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String MAIN_TAG = "DEBUG_LOG_MAIN_TAG";
    public static final String BA_LIST = "com.example.david.myapplication.BA_LIST";
    public static final String BA_MESSAGE = "com.example.david.myapplication.BA_MESSAGE";
    public static final String SAVESTATE_KEY = "SAVESTATE_KEY";
    public static final String SAVESPEED_KEY = "SAVESPEED_KEY";
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private BluetoothSocket mSocket = null;
    public BTReadWriteThread btReadWriteThread;
    private StringBuffer mReadBuffer;

    SpeedometerView mSpeedoView;
    float mCurrentSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // Setup speedometer
        mSpeedoView = (SpeedometerView) findViewById(R.id.speedometer);
        mCurrentSpeed = 10;

        if(((GlobalSettings)getApplication()).isConnected()){
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Connected to :"+((GlobalSettings)getApplication()).getDevice().getName());
        } else {
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Not connected");
        }

        // Check if BT adapter is disabled, and enable it.
        BA = BluetoothAdapter.getDefaultAdapter();
        Boolean alreadyStarted = false;
        try {
            alreadyStarted = savedInstanceState.getBoolean(SAVESTATE_KEY);
            mCurrentSpeed = savedInstanceState.getFloat(SAVESPEED_KEY);
        } catch (Exception e)
        {
            Log.d(MAIN_TAG, "Could not read from bundle.");
        }
        if(!alreadyStarted) {
            if(BA.isEnabled()) {
                // Yay it's enabled! For now, let's Toast.
                Snackbar.make(findViewById(R.id.main_view), "Bluetooth is already enabled!",Snackbar.LENGTH_SHORT).setAction("No action",null).show();
    //            Toast toast = Toast.makeText(this,"Bluetooth is already enabled!", Toast.LENGTH_SHORT);
    //            toast.show();
            }
            else {
                Snackbar.make(findViewById(R.id.main_view), "Bluetooth is needed. Turn on?", Snackbar.LENGTH_LONG).setAction("TURN ON BLUETOOTH", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                    }
                }).show();
            }
        }

        mSpeedoView.setCurrentSpeed(mCurrentSpeed);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVESTATE_KEY, true);
        outState.putFloat(SAVESPEED_KEY, mCurrentSpeed);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_connect:
                Intent intent = new Intent(this, BTConnectActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                Intent intent2 = new Intent(this, SettingsActivity.class);
                startActivity(intent2);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(((GlobalSettings)getApplication()).isConnected()){
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Connected to :"+((GlobalSettings)getApplication()).getDevice().getName());
            mSocket = ((GlobalSettings)getApplication()).getSocket();
            // Create new handler for messaging the BT device
            mHandlerThread = new HandlerThread("MainActivityHandlerThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case BTReadWriteThread.MessageConstants.MESSAGE_READ:
                            mReadBuffer.append((String)msg.obj);
                            if(mReadBuffer.toString().endsWith("\r\n")) {
                                Snackbar.make(findViewById(R.id.display_message_layout), mReadBuffer.substring(0,mReadBuffer.indexOf("\r\n")), Snackbar.LENGTH_SHORT).show();
                                mReadBuffer.delete(0, mReadBuffer.length());
                            }
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_WRITE:
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_ERROR:
                            break;
                    }
                }
            };
            // Start the BT read-write thread
            btReadWriteThread = new BTReadWriteThread(mSocket, mHandler);
            btReadWriteThread.start();
        } else {
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Not connected");
        }
    }


}