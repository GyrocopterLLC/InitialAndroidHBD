package com.example.david.myapplication;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;


public class BatteryFragment extends BluetoothUserFragment {
    private static final String FRAGMENT_KEY = "com.example.david.myapplication.BATTERY";
    private static final int DEFAULT_NUM_BATTERIES = 16;
    private static final float DEFAULT_BATT_VOLTAGE = 3.7f;
    private long mTimeout = 250;
    enum BMS_State {
        CHECK_CONNECTION,
        GET_NUM_BATTERIES,
        GET_BATTERY_VOLTAGES,
        DONE
    }

    @Override
    public String GetFragmentID() {
        return FRAGMENT_KEY;
    }

    BluetoothUserFragmentInteractionListener mListener;
    private BMS_State mBMSstate;

    private Float[] mBatteryVoltages;
    private Integer[] mBatteryStatuses;
    private int mNumBatteries;
    private int mCurrentBattery;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private StringBuffer mReadBuffer;
    private Handler mHandler;

    public Runnable timerExpired = new Runnable() {
        @Override
        public void run() {
            Snackbar.make(getActivity().findViewById(R.id.battery_fragment),"Comms timeout.",Snackbar.LENGTH_SHORT).show();
        }
    };

    private void startTimeout() {
        mHandler.postDelayed(timerExpired, mTimeout);
    }

    private void stopTimeout() {
        mHandler.removeCallbacks(timerExpired);
    }


    // Required empty constructer
    public BatteryFragment() {}

    // New instance - returns the empty constructor
    public static BatteryFragment newInstance() {

        Bundle args = new Bundle();

        BatteryFragment fragment = new BatteryFragment();
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

        View v = inflater.inflate(R.layout.fragment_battery, container, false);
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


        List mBMS_settings_names = Arrays.asList(SettingsConstants.bmsNames);
        recyclerView = getActivity().findViewById(R.id.batteryRecycler);

        // use a grid layout manager
        layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        // specify the adapter
        mAdapter = new BatteryAdapter(mBatteryVoltages,mBatteryStatuses);
        // choose what to do when clicked
        ((BatteryAdapter)mAdapter).setClickListener(new BatteryAdapter.BatteryClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Snackbar.make(getActivity().findViewById(R.id.battery_fragment), String.format("Battery %d clicked.",position+1), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(int position, View v) {
                Snackbar.make(getActivity().findViewById(R.id.battery_fragment), String.format("Battery %d long clicked.",position+1), Snackbar.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.invalidate();

        // Pull handler from main activity
        mHandler = mListener.getActivityHandler();

        if(((GlobalSettings)getActivity().getApplication()).isConnected()){
            ((TextView)getActivity().findViewById(R.id.batteryBTstatusText)).setText("Connected to: "+((GlobalSettings)getActivity().getApplication()).getDevice().getName());
            mReadBuffer = new StringBuffer(1024);


            // Start reading batteries. First, ask if the BMS is connected.
            mBMSstate = BMS_State.CHECK_CONNECTION;
            int var_index = mBMS_settings_names.indexOf("BMS_IS_CONNECTED");
            int this_var_id = SettingsConstants.bmsIDs[var_index]; // BMS_IS_CONNECTED
            char[] packet_data = {(char)((this_var_id & 0xFF00)>>8), (char)(this_var_id & 0x00FF)};
            StringBuffer myBuf = PacketTools.Pack(PacketTools.GET_RAM_VARIABLE, packet_data);
            mListener.Write(myBuf);
            startTimeout();
        } else {
            ((TextView)getActivity().findViewById(R.id.batteryBTstatusText)).setText("Not connected");
        }
    }

    @Override
    public void ReceiveDataCallback(StringBuffer newData) {
        int this_var_id;
        int var_index;
        List mBMS_settings_names = Arrays.asList(SettingsConstants.bmsNames);
        mReadBuffer.append(newData);
        // Check if a valid packet has arrived
        Packet pkt = PacketTools.Unpack(mReadBuffer);
        if(pkt.SOPposition == -1) {
            // No SOPs were found. Delete all of it.
            mReadBuffer.delete(0, mReadBuffer.length());
        } else {
            if (pkt.PacketLength > 0) {
                // Good packet!
                stopTimeout();
                mReadBuffer.delete(0, pkt.SOPposition + pkt.PacketLength);
                if (pkt.PacketID == PacketTools.GET_RAM_RESULT) {
                    // Packet is get ram data response

                    if (mBMSstate == BMS_State.CHECK_CONNECTION) {
                        // Let's see if we need to even continue
                        if (pkt.Data.length() == 1) {
                            if (pkt.Data.charAt(0) == 0) {
                                // No BMS connected
                                mBMSstate = BMS_State.DONE;
                                Snackbar.make(getActivity().findViewById(R.id.battery_fragment), "No BMS connected", Snackbar.LENGTH_SHORT).show();
                            } else {
                                // Continue to next step
                                mBMSstate = BMS_State.GET_NUM_BATTERIES;
                                var_index = mBMS_settings_names.indexOf("BMS_NUMBATTS");
                                this_var_id = SettingsConstants.bmsIDs[var_index]; // BMS_NUMBATTS
                                char[] packet_data = {(char)((this_var_id & 0xFF00)>>8), (char)(this_var_id & 0x00FF)};
                                StringBuffer myBuf = PacketTools.Pack(PacketTools.GET_RAM_VARIABLE, packet_data);
                                mListener.Write(myBuf);
                                startTimeout();
                            }
                        } else {
                            // Wrong data length.
                            mBMSstate = BMS_State.DONE;
                        }
                    } else if (mBMSstate == BMS_State.GET_NUM_BATTERIES) {
                        if (pkt.Data.length() == 2) {
                            int newNumBatteries = 256 * ((byte) pkt.Data.charAt(0)) + ((byte) pkt.Data.charAt(1));
                            if (newNumBatteries != mNumBatteries) {
                                resizeBatteryArrays(newNumBatteries);
                            }
                            mBMSstate = BMS_State.GET_BATTERY_VOLTAGES;
                            mCurrentBattery = 0;
                            var_index = mBMS_settings_names.indexOf("BMS_BATVOLT_N");
                            this_var_id = SettingsConstants.bmsIDs[var_index]; // BMS_BATVOLT_N
                            // TODO: Change following line if need more than 256 batteries.
                            char[] packet_data = {(char)((this_var_id & 0xFF00)>>8), (char)(this_var_id & 0x00FF), 0, (char)mCurrentBattery};
                            StringBuffer myBuf = PacketTools.Pack(PacketTools.GET_RAM_VARIABLE, packet_data);
                            mListener.Write(myBuf);
                            startTimeout();
                        }
                    } else if (mBMSstate == BMS_State.GET_BATTERY_VOLTAGES) {
                        float new_volts = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(0,4)));
                        ((BatteryAdapter)mAdapter).setBattery(new_volts, mCurrentBattery);
                        mCurrentBattery++;
                        if(mCurrentBattery < mNumBatteries) {
                            var_index = mBMS_settings_names.indexOf("BMS_BATVOLT_N");
                            this_var_id = SettingsConstants.bmsIDs[var_index]; // BMS_BATVOLT_N
                            // TODO: Change following line if need more than 256 batteries.
                            char[] packet_data = {(char)((this_var_id & 0xFF00)>>8), (char)(this_var_id & 0x00FF), 0, (char)mCurrentBattery};
                            StringBuffer myBuf = PacketTools.Pack(PacketTools.GET_RAM_VARIABLE, packet_data);
                            mListener.Write(myBuf);
                            startTimeout();
                        } else {
                            mBMSstate = BMS_State.DONE;
                        }

                    } else if (mBMSstate == BMS_State.DONE) {
                        // Don't do anything
                    }

                } else {
                    // This is bad news. Anything besides the proper response
                    // is probably an error.
                    stopTimeout();
                }
            }
        }
    }

    @Override
    public void ErrorCallback() {

    }

    @Override
    public void DisconnectCallback() {
        Snackbar.make(getActivity().findViewById(R.id.battery_fragment), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
        ((TextView)getActivity().findViewById(R.id.batteryBTstatusText)).setText("Not connected");
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
