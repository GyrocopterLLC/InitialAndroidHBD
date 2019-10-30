package com.example.david.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class BatteryActivity extends AppCompatActivity {
    private static final int DEFAULT_NUM_BATTERIES = 16;
    private static final float DEFAULT_BATT_VOLTAGE = 3.7f;
    enum BMS_State {
        CHECK_CONNECTION,
        GET_NUM_BATTERIES,
        GET_BATTERY_VOLTAGES,
        DONE
    }

    private BMS_State mBMSstate;

    private Float[] mBatteryVoltages;
    private Integer[] mBatteryStatuses;
    private int mNumBatteries;

    private int tracker;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private CountDownTimer cdt;
    private StringBuffer mReadBuffer;
    private BluetoothSocket mSocket;
    private Handler mHandler;
    public BTReadWriteThread btReadWriteThread;

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
        float newVoltage = 0.0f;
        // Load in the voltages passed by the Bundle
        newVoltage = getIntent().getExtras().getFloat(MainActivity.BATTVOLTAGE_KEY);

        setTitleVoltage(newVoltage);
        // Initially set up a view with 16 batteries at 3.70 volts
        mNumBatteries = DEFAULT_NUM_BATTERIES;
        mBatteryVoltages = new Float[mNumBatteries];
        mBatteryStatuses = new Integer[mNumBatteries];
//        Random rand = new Random();
        for(int i = 0; i < mNumBatteries; i++) {
            mBatteryVoltages[i] = DEFAULT_BATT_VOLTAGE;
//            mBatteryVoltages[i] = rand.nextFloat() + 3.2f;
            mBatteryStatuses[i] = 0;
        }

        recyclerView = findViewById(R.id.batteryRecycler);

        // use a grid layout manager
        layoutManager = new GridLayoutManager(getApplicationContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        // specify the adapter
        mAdapter = new BatteryAdapter(mBatteryVoltages,mBatteryStatuses);
        recyclerView.setAdapter(mAdapter);
        recyclerView.invalidate();

        // How to restart?
        tracker = 0;
        cdt = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
            }
        }.start();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(((GlobalSettings)getApplication()).isConnected()){
            ((TextView)findViewById(R.id.batteryBTstatusText)).setText("Connected to: "+((GlobalSettings)getApplication()).getDevice().getName());
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
                                    if(pkt.PacketID == (char)0x81) {
                                        // Packet is get ram data response
                                        if(mBMSstate == BMS_State.CHECK_CONNECTION) {
                                            // Let's see if we need to even continue
                                            if(pkt.Data.length() == 1) {
                                                if (pkt.Data.charAt(0) == 0) {
                                                    // No BMS connected
                                                    mBMSstate = BMS_State.DONE;
                                                    Snackbar.make(findViewById(R.id.batteryView), "No BMS connected", Snackbar.LENGTH_SHORT).show();
                                                } else {
                                                    // Continue to next step
                                                    mBMSstate = BMS_State.GET_NUM_BATTERIES;
                                                    char[] packet_data = {0x06, 0x02};
                                                    StringBuffer myBuf = PacketTools.Pack((char) 0x01, packet_data);
                                                    btReadWriteThread.write(myBuf);
                                                }
                                            }
                                            else {
                                                // Wrong data length.
                                                mBMSstate = BMS_State.DONE;
                                            }
                                        } else if(mBMSstate == BMS_State.GET_NUM_BATTERIES) {
                                            if(pkt.Data.length() == 2) {
                                                mNumBatteries = 256 * ((byte) pkt.Data.charAt(0)) + ((byte) pkt.Data.charAt(1));
                                                if (mNumBatteries != mBatteryVoltages.length) {
                                                    resizeBatteryArrays(mNumBatteries);
                                                }
                                                mBMSstate = BMS_State.GET_BATTERY_VOLTAGES;
                                            }
                                        }else if(mBMSstate == BMS_State.GET_BATTERY_VOLTAGES) {
                                            // Do the next thing
                                        }else if(mBMSstate == BMS_State.DONE) {
                                            // Don't do no thing
                                        }

                                    } else {
                                        // This is bad news. Anything besides the proper response
                                        // is probably an error.
                                    }
                                }
                            }
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_WRITE:
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_ERROR:
                            break;
                        case BTReadWriteThread.MessageConstants.MESSAGE_DISCONNECTED:
                            Snackbar.make(findViewById(R.id.batteryView), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
                            ((TextView)findViewById(R.id.batteryBTstatusText)).setText("Not connected");
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

            // Start reading batteries. First, ask if the BMS is connected.
            mBMSstate = BMS_State.CHECK_CONNECTION;
            char[] packet_data = {0x06, 0x01};
            StringBuffer myBuf = PacketTools.Pack((char) 0x01, packet_data);
            btReadWriteThread.write(myBuf);
        } else {
            ((TextView)findViewById(R.id.batteryBTstatusText)).setText("Not connected");
        }
    }


    private void setTitleVoltage(float newVoltage) {
        getSupportActionBar().setTitle(getString(R.string.battery_title) + String.format(" -- Total: %.2f",newVoltage));
    }

    private float calcTitleVoltage() {
        float totalVoltage = 0.0f;
        for(int i = 0; i < mNumBatteries; i++) {
            totalVoltage = totalVoltage + mBatteryVoltages[i];
        }
        return totalVoltage;
    }

    private void resizeBatteryArrays(int newSize) {
        // Need to make new arrays and redo the RecyclerView's adapter
        if(newSize != mNumBatteries) {
            mNumBatteries = newSize;
            Float[] tempVoltages = new Float[mNumBatteries];
            Integer[] tempStatuses = new Integer[mNumBatteries];
            for (int i = 0; i < (mNumBatteries < mBatteryVoltages.length ? mNumBatteries : mBatteryVoltages.length); i++) {
                tempVoltages[i] = mBatteryVoltages[i];
                tempStatuses[i] = mBatteryStatuses[i];
            }
            if (mNumBatteries > mBatteryVoltages.length) {
                for (int j = mBatteryVoltages.length; j < mNumBatteries; j++) {
                    tempVoltages[j] = DEFAULT_BATT_VOLTAGE;
                    tempStatuses[j] = 0;
                }
            }
            mBatteryVoltages = tempVoltages;
            mBatteryStatuses = tempStatuses;
            mAdapter = new BatteryAdapter(mBatteryVoltages, mBatteryStatuses);
            recyclerView.setAdapter(mAdapter);
            recyclerView.postInvalidate();
        }
    }
}
