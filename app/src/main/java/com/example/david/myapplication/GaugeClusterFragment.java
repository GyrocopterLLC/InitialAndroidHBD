package com.example.david.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class GaugeClusterFragment extends BluetoothUserFragment {

    private static final String FRAGMENT_KEY = "com.example.david.myapplication.GAUGECLUSTER";
    private StringBuffer mReadBuffer; // For saving incoming Bluetooth data
    public BluetoothUserFragmentInteractionListener mListener; // To be attached to the calling context
    private Handler mHandler; // Passed down from activity
    private CountDownTimer mTimer; // For simulating the gauges
    private boolean mSpeedDir = false; // For changing direction during gauge simulation
    private boolean mAskingForData = false; // For toggling data on/off
    // Saved Views for easy updating
    private GaugeView mSpeedoView, mPhaseView, mBatteryView;
    private ThrottleView mThrottleView;
    // Local variables holding the gauge values
    private float mCurrentSpeed;
    private float mCurrentThrottle;
    private float mCurrentPhaseAmps;
    private float mCurrentBatteryAmps;
    private float mCurrentBatteryVolts;
    private float mCurrentFetTemp;
    private float mCurrentMotorTemp;
    private int mCurrentFaultCode;
    private float mWheelSizeMM; // Diameter in mm

    @Override
    public String GetFragmentID() {
        return FRAGMENT_KEY;
    }

    // Required empty constructer
    public GaugeClusterFragment() {}

    // New instance - returns the empty constructor
    public static GaugeClusterFragment newInstance() {

        Bundle args = new Bundle();

        GaugeClusterFragment fragment = new GaugeClusterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Attach the listener
        if(context instanceof BluetoothUserFragmentInteractionListener) {
            mListener = (BluetoothUserFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BluetoothUserFragmentInteractionListener.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_gaugecluster, container, false);
        return v;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSpeedoView = getView().findViewById(R.id.speedometer);
        mThrottleView = getView().findViewById(R.id.throttleBar);
        mPhaseView = getView().findViewById(R.id.phaseCurrentBar);
        mBatteryView = getView().findViewById(R.id.batteryCurrentBar);
        mCurrentSpeed = 0;
        mCurrentThrottle = 0;
        mCurrentBatteryAmps = 0;
        mCurrentPhaseAmps = 0;
        mWheelSizeMM = 700.28f;
        if(((GlobalSettings)getActivity().getApplication()).isConnected()) {
            ((TextView) getView().findViewById(R.id.text_btdevice_info)).setText("Connected to: " + ((GlobalSettings) getActivity().getApplication()).getDevice().getName());
            mReadBuffer = new StringBuffer(1024);
            // Grab Handler from main - allows this fragment to set up recurring function calls
            mHandler = mListener.getActivityHandler();
        } else {
            ((TextView) getView().findViewById(R.id.text_btdevice_info)).setText("Bluetooth not connected.");
        }
    }

    @Override
    public void onPause() {
        // If we have the Runnable posted to the handler, gotta remove it
        if(mHandler != null) {
            if(mAskingForData) {
                mHandler.removeCallbacks(askDataRunnable);
                mAskingForData = false;
                ((Button) getView().findViewById(R.id.btn_askForData)).setText("ASK FOR DATA");
            }
        }
        super.onPause();
    }

    @Override
    public void ReceiveDataCallback(StringBuffer newData) {
        // add the new data to our buffer
        mReadBuffer.append(newData);
        // Check if a valid packet has arrived
        Packet pkt = PacketTools.Unpack(mReadBuffer);
        if(pkt.SOPposition == -1) {
            // No SOPs were found. Delete all of it.
            mReadBuffer.delete(0, mReadBuffer.length());
        } else {
            if(pkt.PacketLength > 0) {
                // Good packet!
                mReadBuffer.delete(0,pkt.SOPposition+pkt.PacketLength);
                if(pkt.PacketID == PacketTools.DASHBOARD_DATA_RESULT) {
                    // This is our dashboard data response
                    mCurrentThrottle = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(0,4)));
                    mCurrentSpeed = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(4,8)));
                    mCurrentPhaseAmps = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(8,12)));
                    mCurrentBatteryAmps = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(12,16)));
                    mCurrentBatteryVolts = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(16,20)));
                    mCurrentFetTemp = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(20,24)));
                    mCurrentMotorTemp = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(24,28)));
                    mCurrentFaultCode = PacketTools.stringToInt(new StringBuffer(pkt.Data.substring(28,32)));
                    // Convert RPM to MPH --- 6.2137e-7 mm per mile, multiply by PI to get circumference from diameter, and by 60 to get hours from minutes
//                    mCurrentSpeed = mWheelSizeMM * (float)Math.PI * 6.2137e-7f * 60.0f * mCurrentSpeed;
                    mCurrentSpeed = 0.08202f * mCurrentSpeed; // Shortcut for 700.28mm diameter
                    mSpeedoView.setCurrentValue(mCurrentSpeed);
                    mThrottleView.setThrottlePosition((int)mCurrentThrottle);
                    mPhaseView.setCurrentValue(mCurrentPhaseAmps);
                    mBatteryView.setCurrentValue(mCurrentBatteryAmps);
                    ((TextView)getView().findViewById(R.id.batteryVoltageDisplay)).setText(String.format(Locale.US,"%02.1f",mCurrentBatteryVolts));
                    ((TextView)getView().findViewById(R.id.fetTempDisplay)).setText(String.format(Locale.US,"%02.1f",mCurrentFetTemp));
                    ((TextView)getView().findViewById(R.id.motorTempDisplay)).setText(String.format(Locale.US,"%02.1f",mCurrentMotorTemp));
                }
            }
        }
    }

    public void onClickSimulate() {
        // When the Simulate button is pressed, the Main activity should call this function
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
                mThrottleView.setThrottlePosition((int)(mCurrentSpeed/30.0f*100));
            }

            @Override
            public void onFinish() {
                // Nothing nada zilch
            }
        };
        mTimer.start();
    }

    public Runnable askDataRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuffer myBuf = PacketTools.Pack(PacketTools.REQUEST_DASHBOARD_DATA, new char[0]);
            if (((GlobalSettings) getActivity().getApplication()).isConnected()) {
                mListener.Write(myBuf);
            }

            mHandler.postDelayed(this, 50);
        }
    };

    public void onClickAskForData() {
        // When the Ask for Data button is pressed, the Main activity should call this function
        if(((GlobalSettings)getActivity().getApplication()).isConnected()) {
            if(!mAskingForData) {
                // Send a timed event
                if(mHandler != null) {
                    mHandler.postDelayed(askDataRunnable, 50);
                    mAskingForData = true;
                    ((Button) getView().findViewById(R.id.btn_askForData)).setText("STOP");
                }
            } else {
                // Stop the event
                mHandler.removeCallbacks(askDataRunnable);
                mAskingForData = false;
                ((Button) getView().findViewById(R.id.btn_askForData)).setText("ASK FOR DATA");
            }
        } else {
            // Popup telling user that it can't be done
            Snackbar.make(getView().findViewById(R.id.gaugeLayout), "No connected device!", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void ErrorCallback() {
        // Can implement this later if needed.
    }

    @Override
    public void DisconnectCallback() {
        // Just announce to the user that a disconnect has occurred.
        // Using a snackbar for its pretty looks
        Snackbar.make(getView().findViewById(R.id.gaugeLayout), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
        // Also update the textview for a more permanent user interaction
        ((TextView) getView().findViewById(R.id.text_btdevice_info)).setText("Bluetooth not connected.");
    }
}
