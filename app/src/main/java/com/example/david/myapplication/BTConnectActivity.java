package com.example.david.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class BTConnectActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private String selectedBTDaddress = null;
    private BluetoothDeviceViewAdapter adapter;
    private Handler mHandler;
    private BluetoothSocket mSocket = null;
    private ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        // Set default toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.disp_message_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.btconnect_title);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ConnectThread.MessageConstants.MESSAGE_CONNECTED:
                        mSocket = (BluetoothSocket) msg.obj;
                        Snackbar.make(findViewById(R.id.display_message_layout), "Connected!", Snackbar.LENGTH_SHORT).show();
                        // Update application
                        ((GlobalSettings)getApplication()).setConnected(true);
                        ((GlobalSettings)getApplication()).setDevice(mSocket.getRemoteDevice());
                        ((GlobalSettings)getApplication()).setSocket(mSocket);
                        // Update view list
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ListView lv = findViewById(R.id.listView);
                                BluetoothDeviceViewAdapter adapter = (BluetoothDeviceViewAdapter)lv.getAdapter();
                                for(int i = 0; i < lv.getAdapter().getCount(); i++) {
                                    BluetoothDeviceViewModel item = adapter.getItem(i);
                                    if(item.getName().equals(mSocket.getRemoteDevice().getName())) {
                                        item.setConnected(true);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                findViewById(R.id.textConnectingLabel).setVisibility(View.INVISIBLE);
                                findViewById(R.id.connectingProgressBar).setVisibility(View.INVISIBLE);
                            }
                        });
                        mConnectThread.stop();

                        break;
                    case ConnectThread.MessageConstants.MESSAGE_ERROR:
                        Snackbar.make(findViewById(R.id.display_message_layout),"Could not connect", Snackbar.LENGTH_SHORT).show();
                        findViewById(R.id.textConnectingLabel).setVisibility(View.INVISIBLE);
                        findViewById(R.id.connectingProgressBar).setVisibility(View.INVISIBLE);
                        mConnectThread.stop();
                        break;
                }
            }
        };

        BA = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> btdevices =  BA.getBondedDevices();
        ArrayList<BluetoothDeviceViewModel> btviewmodels = new ArrayList<BluetoothDeviceViewModel>();
        for(BluetoothDevice btd : btdevices) {

            /* This junk is needed because there isn't just a simple
             * BluetoothDevice.isConnected() in the API.
             */
            if(((GlobalSettings)getApplication()).isConnected()){
                if(((GlobalSettings)getApplication()).getDevice().getName().equals(btd.getName())) {
                    btviewmodels.add(new BluetoothDeviceViewModel(btd.getName(),btd.getAddress(),
                            true));
                } else {
                    btviewmodels.add(new BluetoothDeviceViewModel(btd.getName(), btd.getAddress(),
                            false));
                }
            } else {
                btviewmodels.add(new BluetoothDeviceViewModel(btd.getName(), btd.getAddress(),
                        false));
            }

        }

        adapter = new BluetoothDeviceViewAdapter(btviewmodels, this);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        final Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter,View v, int position, long id){
                BluetoothDeviceViewModel item = (BluetoothDeviceViewModel)adapter.getItemAtPosition(position);
                String name = item.getName();
                String mac = item.getMac();
                selectedBTDaddress = mac;

                Snackbar.make(v, "Name: "+name+"\r\nAddress: "+mac,Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // Called when connect button is pressed
    public void ConnectToBTD(View view) {
        if(selectedBTDaddress != null) {
            BluetoothDevice mDevice = BA.getRemoteDevice(selectedBTDaddress);

            mConnectThread = new ConnectThread(mDevice,getResources().getString(R.string.bt_spp_uuid), mHandler);
            new Thread(mConnectThread).start();
            findViewById(R.id.textConnectingLabel).setVisibility(View.VISIBLE);
            findViewById(R.id.connectingProgressBar).setVisibility(View.VISIBLE);
        }

    }
}
