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

public class MainActivity extends AppCompatActivity {
    public static final String MAIN_TAG = "DEBUG_LOG_MAIN_TAG";
    public static final String BA_LIST = "com.example.david.myapplication.BA_LIST";
    public static final String BA_MESSAGE = "com.example.david.myapplication.BA_MESSAGE";
    public static final String SAVESTATE_KEY = "SAVESTATE_KEY";
    public static final String SAVESPEED_KEY = "SAVESPEED_KEY";
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter BA;
    private Handler mHandler;
    private BluetoothSocket mSocket = null;
    public BTReadWriteThread btReadWriteThread;
    private StringBuffer mReadBuffer;
    private CountDownTimer mTimer;
    private boolean mSpeedDir = false;

    GaugeView mSpeedoView, mPhaseView, mBatteryView;
    ThrottleView mThrottleView;
    float mCurrentSpeed;
    float mCurrentThrottle;
    float mCurrentPhaseAmps;
    float mCurrentBatteryAmps;
    float mCurrentBatteryVolts;
    float mCurrentFetTemp;
    float mCurrentMotorTemp;
    int mCurrentFaultCode;

    private boolean askingForData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // Setup speedometer
        mSpeedoView = (GaugeView) findViewById(R.id.speedometer);
        mThrottleView = (ThrottleView) findViewById(R.id.throttleBar);
        mPhaseView = (GaugeView) findViewById(R.id.phaseCurrentBar);
        mBatteryView = (GaugeView) findViewById(R.id.batteryCurrentBar);
        mCurrentSpeed = 0;
        mCurrentThrottle = 0;
        mCurrentBatteryAmps = 0;
        mCurrentPhaseAmps = 0;

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
        } catch (Exception e)
        {
            Log.d(MAIN_TAG, "Could not read from bundle.");
        }
        if(!alreadyStarted) {
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
//                Intent intent2 = new Intent(this, ScrollingSettings.class);
                startActivity(intent2);
                break;
            case R.id.action_battery:
                Intent intent3 = new Intent(this, BatteryActivity.class);
                startActivity(intent3);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(((GlobalSettings)getApplication()).isConnected()){
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Connected to: "+((GlobalSettings)getApplication()).getDevice().getName());
            mReadBuffer = new StringBuffer(1024);
            mSocket = ((GlobalSettings)getApplication()).getSocket();
            // Create new handler for messaging the BT device
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case BTReadWriteThread.MessageConstants.MESSAGE_READ:
                            mReadBuffer.append(msg.obj);
                            // Check if a valid packet has arrived
                            Packet pkt = PacketTools.Unpack(mReadBuffer);
                            if(pkt.SOPposition == -1) {
                                // No SOPs were found. Delete all of it.
                                mReadBuffer.delete(0, mReadBuffer.length());
                            } else {
                                if(pkt.PacketLength > 0) {
                                    // Good packet!
                                    mReadBuffer.delete(0,pkt.SOPposition+pkt.PacketLength);
                                    if(pkt.PacketID == (char)0xA7) {
                                        // This is our dashboard data response

                                        mCurrentThrottle = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(0,4)));
                                        mCurrentSpeed = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(4,8)));
                                        mCurrentPhaseAmps = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(8,12)));
                                        mCurrentBatteryAmps = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(12,16)));
                                        mCurrentBatteryVolts = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(16,20)));
                                        mCurrentFetTemp = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(20,24)));
                                        mCurrentMotorTemp = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(24,28)));
                                        mCurrentFaultCode = PacketTools.stringToInt(new StringBuffer(pkt.Data.substring(28,32)));
                                        mSpeedoView.setCurrentValue(mCurrentSpeed);
                                        mThrottleView.setThrottlePosition((int)mCurrentThrottle);
                                        mPhaseView.setCurrentValue(mCurrentPhaseAmps);
                                        mBatteryView.setCurrentValue(mCurrentBatteryAmps);
                                        ((TextView)findViewById(R.id.batteryVoltageDisplay)).setText(String.format(Locale.US,"%02.1f",mCurrentBatteryVolts));
                                        ((TextView)findViewById(R.id.fetTempDisplay)).setText(String.format(Locale.US,"%02.1f",mCurrentFetTemp));
                                        ((TextView)findViewById(R.id.motorTempDisplay)).setText(String.format(Locale.US,"%02.1f",mCurrentMotorTemp));
                                    }
                                }
                            }
                            String allTheInput = mReadBuffer.toString();
                            if(allTheInput.endsWith("\r\n")) {
                                if(allTheInput.startsWith("S:")) {
                                    // Speed data coming in
                                    mCurrentSpeed = getFloat(allTheInput.substring(2, allTheInput.indexOf("\r\n")),mCurrentSpeed);
                                    mSpeedoView.setCurrentValue(mCurrentSpeed);
                                }
                                if(allTheInput.startsWith("T:")) {
                                    mCurrentThrottle = getFloat (allTheInput.substring(2, allTheInput.indexOf("\r\n")),mCurrentThrottle);
                                    mThrottleView.setThrottlePosition((int)mCurrentThrottle);
                                }
                                if(allTheInput.startsWith("P:")) {
                                    mCurrentPhaseAmps = getFloat (allTheInput.substring(2, allTheInput.indexOf("\r\n")),mCurrentPhaseAmps);
                                    mPhaseView.setCurrentValue(mCurrentPhaseAmps);
                                }
                                if(allTheInput.startsWith("B:")) {
                                    mCurrentBatteryAmps = getFloat (allTheInput.substring(2, allTheInput.indexOf("\r\n")),mCurrentBatteryAmps);
                                    mBatteryView.setCurrentValue(mCurrentBatteryAmps);
                                }

                                Snackbar.make(findViewById(R.id.main_view), mReadBuffer.substring(0,mReadBuffer.indexOf("\r\n")), Snackbar.LENGTH_SHORT).show();
                                mReadBuffer.delete(0, mReadBuffer.length());
                            }
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_WRITE:
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_ERROR:
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_DISCONNECTED:
                            Snackbar.make(findViewById(R.id.main_view), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
                            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Not connected");
                            ((GlobalSettings)getApplication()).setConnected(false);
                            ((GlobalSettings)getApplication()).setDevice(null);
                            ((GlobalSettings)getApplication()).setSocket(null);
                            break;
                    }
                }
            };
            // Start the BT read-write thread
            btReadWriteThread = new BTReadWriteThread(mSocket, mHandler);
            new Thread(btReadWriteThread).start();
        } else {
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Not connected");
        }
    }

    public void onClick(View v) {
        mTimer = new CountDownTimer(5000, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(mSpeedDir) {
                    if (mCurrentSpeed < 30.0f) {
                        mCurrentSpeed += 1.0f;
                    } else {
                        mSpeedDir = false;
                    }
                } else {
                    if(mCurrentSpeed > 0.0f) {
                        mCurrentSpeed -= 1.0f;
                    } else {
                        mSpeedDir = true;
                    }
                }
                mSpeedoView.setCurrentValue(mCurrentSpeed);
                mPhaseView.setCurrentValue(mPhaseView.getMaxValue()*(2.0f*mCurrentSpeed/30.0f - 1.0f));
                mBatteryView.setCurrentValue(mBatteryView.getMaxValue()*(1.0f-2.0f*mCurrentSpeed/30.0f));
                ThrottleView pb = (ThrottleView) findViewById(R.id.throttleBar);
                pb.setThrottlePosition((int)(mCurrentSpeed/30.0f*100));
            }

            @Override
            public void onFinish() {
                // Nothing nada zilch
            }
        };
        mTimer.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void askForData(View v) {
//        PacketTools pkt = new PacketTools();
        if (((GlobalSettings) getApplication()).isConnected()) {
            if (!askingForData) {
                // Start a timed event
                mHandler.postDelayed(sendAskPacket, 50);
                askingForData = true;
                ((Button) v).setText("STOP");
            } else {
                mHandler.removeCallbacks(sendAskPacket);
                askingForData = false;
                ((Button) v).setText("ASK FOR DATA");
            }
        } else {
            Snackbar.make(findViewById(R.id.main_view), "No connected device!", Snackbar.LENGTH_SHORT).show();
        }
    }

    Runnable sendAskPacket = new Runnable() {
        @Override
        public void run() {
            StringBuffer myBuf = PacketTools.Pack((char) 0x27, new char[0]);
            if (((GlobalSettings) getApplication()).isConnected()) {
                btReadWriteThread.write(myBuf);
            }

            mHandler.postDelayed(this, 50);
        }
    };

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

    public float getFloat(String toFloat, float deflt) {
        float out;
        try {
            out = Float.parseFloat(toFloat);
        } catch (NumberFormatException e) {
            out = deflt;
            Log.d(MAIN_TAG, "Could not parse float: "+toFloat);
        }
        return out;
    }


}