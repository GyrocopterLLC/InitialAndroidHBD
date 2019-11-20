package com.example.david.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.david.myapplication.SettingsConstants.adcFormats;
import static com.example.david.myapplication.SettingsConstants.adcIDs;
import static com.example.david.myapplication.SettingsConstants.adcNames;
import static com.example.david.myapplication.SettingsConstants.focFormats;
import static com.example.david.myapplication.SettingsConstants.focIDs;
import static com.example.david.myapplication.SettingsConstants.focNames;
import static com.example.david.myapplication.SettingsConstants.limitFormats;
import static com.example.david.myapplication.SettingsConstants.limitIDs;
import static com.example.david.myapplication.SettingsConstants.limitNames;
import static com.example.david.myapplication.SettingsConstants.mainFormats;
import static com.example.david.myapplication.SettingsConstants.mainIDs;
import static com.example.david.myapplication.SettingsConstants.mainNames;
import static com.example.david.myapplication.SettingsConstants.motorFormats;
import static com.example.david.myapplication.SettingsConstants.motorIDs;
import static com.example.david.myapplication.SettingsConstants.motorNames;
import static com.example.david.myapplication.SettingsConstants.throttleFormats;
import static com.example.david.myapplication.SettingsConstants.throttleIDs;
import static com.example.david.myapplication.SettingsConstants.throttleNames;

public class SettingsFragment extends BluetoothUserFragment {
    private static final String FRAGMENT_KEY = "com.example.david.myapplication.SETTINGS";
    private static final int MODE_RAM = 0;
    private static final int MODE_EEPROM = 1;
    private String[] mSettingsNames;
    private Float[] mSettingsValues;
    private Float[] mEditedValues;
    private Integer[] mSettingsFormats;
    private Integer[] mSettingsIDs;
    private int mRamOrEeprom = MODE_RAM;
    private int mVarNum;
    private int mEditVarNum;

    @Override
    public String GetFragmentID() {
        return FRAGMENT_KEY;
    }

    BluetoothUserFragmentInteractionListener mListener;


    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private StringBuffer mReadBuffer;
    private Handler mHandler;


    // Required empty constructer
    public SettingsFragment() {}

    // New instance - returns the empty constructor
    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
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

        View v = inflater.inflate(R.layout.fragment_settings, container, false);
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
        // Pull handler from main activity
        mHandler = mListener.getActivityHandler();

        // Set up the tabs
        TabLayout tb_categories = getActivity().findViewById(R.id.settings_category_tabs);
        TabLayout tb_ramoreeprom = getActivity().findViewById(R.id.settings_ram_or_eeprom_tabs);
        if(tb_categories.getTabCount() == 0) {
            tb_categories.addTab(tb_categories.newTab().setText("ADC").setIcon(R.drawable.ic_play_arrow_black_24dp));
            tb_categories.addTab(tb_categories.newTab().setText("FOC").setIcon(R.drawable.ic_foc_icon));
            tb_categories.addTab(tb_categories.newTab().setText("Main").setIcon(R.drawable.ic_format_list_numbered_black_24dp));
            tb_categories.addTab(tb_categories.newTab().setText("Throttle").setIcon(R.drawable.ic_flight_takeoff_black_24dp));
            tb_categories.addTab(tb_categories.newTab().setText("Limits").setIcon(R.drawable.ic_pan_tool_black_24dp));
            tb_categories.addTab(tb_categories.newTab().setText("Motor").setIcon(R.drawable.ic_motor_icon));
        }
        if(tb_ramoreeprom.getTabCount() == 0) {
            tb_ramoreeprom.addTab(tb_ramoreeprom.newTab().setText("RAM"));
            tb_ramoreeprom.addTab(tb_ramoreeprom.newTab().setText("EEPROM"));
        }

        tb_categories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()) {
                    case 0:
                        // ADC
                        mSettingsFormats = adcFormats;
                        mSettingsNames = adcNames;
                        mSettingsIDs = adcIDs;
                        break;
                    case 1:
                        // FOC
                        mSettingsFormats = focFormats;
                        mSettingsNames = focNames;
                        mSettingsIDs = focIDs;
                        break;
                    case 2:
                        // Main
                        mSettingsFormats = mainFormats;
                        mSettingsNames = mainNames;
                        mSettingsIDs = mainIDs;
                        break;
                    case 3:
                        // Throttle
                        mSettingsFormats = throttleFormats;
                        mSettingsNames = throttleNames;
                        mSettingsIDs = throttleIDs;
                        break;
                    case 4:
                        // Limits
                        mSettingsFormats = limitFormats;
                        mSettingsNames = limitNames;
                        mSettingsIDs = limitIDs;
                        break;
                    case 5:
                        // Motor
                        mSettingsFormats = motorFormats;
                        mSettingsNames = motorNames;
                        mSettingsIDs = motorIDs;
                        break;
                }
                mSettingsValues = new Float[mSettingsNames.length];
                mEditedValues = new Float[mSettingsNames.length];
                for(int i = 0; i < mSettingsValues.length; i++) {
                    mSettingsValues[i] = 0.0f;
                    mEditedValues[i] = 0.0f;
                }
                mAdapter = new SettingsAdapter(mSettingsNames, mSettingsValues, mSettingsFormats, mHandler);
                ((SettingsAdapter)mAdapter).setClickListener(mClickListener);
                recyclerView.setAdapter(mAdapter);
                recyclerView.invalidate();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tb_ramoreeprom.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Change mode based on what tab
                if(tab.getPosition() == 0) {
                    // RAM tab
                    mRamOrEeprom = MODE_RAM;
                } else {
                    // EEPROM tab
                    mRamOrEeprom = MODE_EEPROM;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        recyclerView = getActivity().findViewById(R.id.settingsRecycler);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        mSettingsNames = adcNames;
        mSettingsValues = new Float[mSettingsNames.length];
        mEditedValues = new Float[mSettingsNames.length];
        for(int i = 0; i < mSettingsValues.length; i++) {
            mSettingsValues[i] = 0.0f;
            mEditedValues[i] = 0.0f;
        }
        mSettingsFormats = adcFormats;
        mSettingsIDs = adcIDs;
                // specify the adapter
        mAdapter = new SettingsAdapter(mSettingsNames, mSettingsValues, mSettingsFormats, mHandler);

        // choose what to do when clicked
        ((SettingsAdapter)mAdapter).setClickListener(mClickListener);

        recyclerView.setAdapter(mAdapter);
        recyclerView.invalidate();

        if(((GlobalSettings)getActivity().getApplication()).isConnected()){
            ((TextView)getActivity().findViewById(R.id.settingsBTstatusText)).setText("Connected to: "+((GlobalSettings)getActivity().getApplication()).getDevice().getName());
            mReadBuffer = new StringBuffer(1024);


        } else {
            ((TextView)getActivity().findViewById(R.id.settingsBTstatusText)).setText("Not connected");
        }
    }

    SettingsAdapter.SettingsClickListener mClickListener = new SettingsAdapter.SettingsClickListener() {
        @Override
        public void onItemClick(int position, View v) {
            // Edit value popup
            AlertDialog.Builder popupValueChanger = new AlertDialog.Builder(getActivity());
            popupValueChanger.setTitle("New Value");
            popupValueChanger.setMessage("Enter a new value");
            mEditVarNum = position;
            final EditText input = new EditText(getActivity());

            if(mSettingsFormats[position] == SettingsAdapter.SettingsTypes.TYPE_FLOAT) {
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                input.setText(String.format("%.3f",mSettingsValues[position]));
            } else {
                input.setText(String.format("%d",mSettingsValues[position].intValue()));
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }
            popupValueChanger.setView(input);
            popupValueChanger.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Set the new value
                    Snackbar.make(getActivity().findViewById(R.id.settings_fragment), String.format("New value is %s",input.getText()), Snackbar.LENGTH_SHORT).show();
                    mEditedValues[mEditVarNum] = Float.parseFloat(input.getText().toString());
                    ((SettingsAdapter)mAdapter).setNewValue(mEditedValues[mEditVarNum],mEditVarNum,true);

                }
            });
            popupValueChanger.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Snackbar.make(getActivity().findViewById(R.id.settings_fragment), "Canceled.", Snackbar.LENGTH_SHORT).show();
                }
            });
            popupValueChanger.show();
        }

        @Override
        public void onItemLongClick(int position, View v) {
            // Ask to reset the value.
            mEditVarNum = position;
            AlertDialog.Builder popupValueResetter = new AlertDialog.Builder(getActivity());
            popupValueResetter.setTitle("Reset Value");
            popupValueResetter.setMessage("Reset value back to original?");
            popupValueResetter.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Snackbar.make(getActivity().findViewById(R.id.settings_fragment), String.format("%s reset.",mSettingsNames[mEditVarNum]),Snackbar.LENGTH_SHORT).show();
                    mEditedValues[mEditVarNum] = 0.0f;
                    ((SettingsAdapter)mAdapter).setNewValue(mSettingsValues[mEditVarNum],mEditVarNum,false);
                }
            });
            popupValueResetter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Snackbar.make(getActivity().findViewById(R.id.settings_fragment),"Canceled.",Snackbar.LENGTH_SHORT).show();
                        }
                    });
            popupValueResetter.show();
        }
    };

    @Override
    public void ReceiveDataCallback(StringBuffer newData) {
        mReadBuffer.append(newData);
        // Check if a valid packet has arrived
        Packet pkt = PacketTools.Unpack(mReadBuffer);
        if(pkt.SOPposition == -1) {
            // No SOPs were found. Delete all of it.
            mReadBuffer.delete(0, mReadBuffer.length());
        } else {
            if (pkt.PacketLength > 0) {
                // Good packet!
                mReadBuffer.delete(0, pkt.SOPposition + pkt.PacketLength);
                if (pkt.PacketID == PacketTools.GET_RAM_RESULT || pkt.PacketID == PacketTools.GET_EEPROM_RESULT) {
                    // Packet is get ram or get EEPROM data response
                    // Update the variable value
                    if(mSettingsFormats[mVarNum] == SettingsAdapter.SettingsTypes.TYPE_8BIT) {
                        mSettingsValues[mVarNum] = (float)(PacketTools.stringTo8bitInt(new StringBuffer(pkt.Data.substring(0,1))));
                    }
                    if(mSettingsFormats[mVarNum] == SettingsAdapter.SettingsTypes.TYPE_16BIT) {
                        mSettingsValues[mVarNum] = (float)(PacketTools.stringTo16bitInt(new StringBuffer(pkt.Data.substring(0,2))));
                    }
                    if(mSettingsFormats[mVarNum] == SettingsAdapter.SettingsTypes.TYPE_32BIT) {
                        mSettingsValues[mVarNum] = (float)(PacketTools.stringToInt(new StringBuffer(pkt.Data.substring(0,4))));
                    }
                    if(mSettingsFormats[mVarNum] == SettingsAdapter.SettingsTypes.TYPE_FLOAT) {
                        mSettingsValues[mVarNum] = PacketTools.stringToFloat(new StringBuffer(pkt.Data.substring(0,4)));
                    }
                    // Update the display
                    ((SettingsAdapter)mAdapter).setNewValue(mSettingsValues[mVarNum],mVarNum,false);
                    mVarNum++;
                    // Get the next if there are more
                    if(mVarNum < mSettingsNames.length) {
                        int this_var_id = mSettingsIDs[mVarNum];
                        char[] packet_data = {(char)((this_var_id & 0xFF00)>>8), (char)(this_var_id & 0x00FF)};
                        StringBuffer myBuf;
                        if(mRamOrEeprom == MODE_RAM) {
                            myBuf = PacketTools.Pack(PacketTools.GET_RAM_VARIABLE, packet_data); // Get RAM
                        } else {
                            myBuf = PacketTools.Pack(PacketTools.GET_EEPROM_VARIABLE, packet_data); // Get EEPROM
                        }
                        mListener.Write(myBuf);
                    }

                } else if(pkt.PacketID == PacketTools.CONTROLLER_ACK) {
                    // Ack packet. New data sent successfully.
                    // Find the next changed value
                    int i = mVarNum + 1;
                    mVarNum = -1;
                    for(; (i < mSettingsNames.length) && (mVarNum == -1); i++) {
                        if(((SettingsAdapter)mAdapter).mChanged[i]) {
                            mVarNum = i;
                        }
                    }
                    if(mVarNum != -1) {
                        StringBuffer packet_data = new StringBuffer(6);
                        int this_var_id = mSettingsIDs[mVarNum];
                        int this_var_fmt = mSettingsFormats[mVarNum];
                        // First two bytes - data ID
                        packet_data.append((char)((this_var_id & 0xFF00) >> 8));
                        packet_data.append((char)(this_var_id & 0x00FF));
                        // Next 1 to 4 bytes - data value
                        if(this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_8BIT) {
                            packet_data.append((char) ((SettingsAdapter)mAdapter).mValues[mVarNum].intValue());
                        } else if(this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_16BIT) {
                            packet_data.append(PacketTools.int16toString(((SettingsAdapter)mAdapter).mValues[mVarNum].intValue()));
                        } else if(this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_32BIT) {
                            packet_data.append(PacketTools.int32toString(((SettingsAdapter)mAdapter).mValues[mVarNum].intValue()));
                        } else if(this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_FLOAT) {
                            packet_data.append(PacketTools.floatToString(((SettingsAdapter)mAdapter).mValues[mVarNum]));
                        }
                        if(mRamOrEeprom == MODE_RAM) {
                            mListener.Write(PacketTools.Pack(PacketTools.SET_RAM_VARIABLE, packet_data.toString().toCharArray()));
                        } else {
                            mListener.Write(PacketTools.Pack(PacketTools.SET_EEPROM_VARIABLE, packet_data.toString().toCharArray()));
                        }
                    }

                } else if (pkt.PacketID == PacketTools.CONTROLLER_NACK) {
                    // Nack packet. Something screwed up.

                } else {
                    // This is bad news. Anything besides the proper response
                    // is probably an error.
                }
            }
        }
    }

    private void refreshData() {
        mVarNum = 0; // Starting at the first variable in the current list
        int this_var_id = mSettingsIDs[mVarNum];
        char[] packet_data = {(char)((this_var_id & 0xFF00)>>8), (char)(this_var_id & 0x00FF)};
        StringBuffer myBuf;
        if(mRamOrEeprom == MODE_RAM) {
            myBuf = PacketTools.Pack(PacketTools.GET_RAM_VARIABLE, packet_data);
        } else {
            myBuf = PacketTools.Pack(PacketTools.GET_EEPROM_VARIABLE, packet_data);
        }
        mListener.Write(myBuf);
    }

    private void sendData() {
        mVarNum = -1; // Invalid entry - for error checking below
        StringBuffer packet_data = new StringBuffer(6);
        // Find the first setting that is changed
        for(int i = 0; (i < mSettingsNames.length) && (mVarNum == -1); i++) {
            if(((SettingsAdapter)mAdapter).mChanged[i]) {
                // This one is changed. Needs to be sent.
                mVarNum = i;
            }
        }
        if(mVarNum != -1) {
            int this_var_id = mSettingsIDs[mVarNum];
            int this_var_fmt = mSettingsFormats[mVarNum];
            // First two bytes - data ID
            packet_data.append((char) ((this_var_id & 0xFF00) >> 8));
            packet_data.append((char) (this_var_id & 0x00FF));
            // Next 1 to 4 bytes - data value
            if (this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_8BIT) {
                packet_data.append((char) ((SettingsAdapter) mAdapter).mValues[mVarNum].intValue());
            } else if (this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_16BIT) {
                packet_data.append(PacketTools.int16toString(((SettingsAdapter) mAdapter).mValues[mVarNum].intValue()));
            } else if (this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_32BIT) {
                packet_data.append(PacketTools.int32toString(((SettingsAdapter) mAdapter).mValues[mVarNum].intValue()));
            } else if (this_var_fmt == SettingsAdapter.SettingsTypes.TYPE_FLOAT) {
                packet_data.append(PacketTools.floatToString(((SettingsAdapter) mAdapter).mValues[mVarNum]));
            }
            if (mRamOrEeprom == MODE_RAM) {
                mListener.Write(PacketTools.Pack(PacketTools.SET_RAM_VARIABLE, packet_data.toString().toCharArray()));
            } else {
                mListener.Write(PacketTools.Pack(PacketTools.SET_EEPROM_VARIABLE, packet_data.toString().toCharArray()));
            }
        } else {
            // Notify user that nothing is changed, so nothing will be sent
            Snackbar.make(getActivity().findViewById(R.id.settings_fragment),"No changes.",Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public void ErrorCallback() {

    }

    @Override
    public void DisconnectCallback() {
        Snackbar.make(getActivity().findViewById(R.id.settings_fragment), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
        ((TextView)getActivity().findViewById(R.id.settingsBTstatusText)).setText("Not connected");
    }

    public void onClickRead() {
        // Start the read process if we are connected
        if(((GlobalSettings)getActivity().getApplication()).isConnected()){
            refreshData();
        } else {
            // Warn user that there is no connection
            Snackbar.make(getActivity().findViewById(R.id.settings_fragment), "Bluetooth is not connected.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void onClickWrite() {
        // Start the write process if we are connected
        if(((GlobalSettings)getActivity().getApplication()).isConnected()) {
            sendData();
        } else {
            // Warn user that there is no connection
            Snackbar.make(getActivity().findViewById(R.id.settings_fragment), "Bluetooth is not connected.", Snackbar.LENGTH_SHORT).show();
        }
    }
}
