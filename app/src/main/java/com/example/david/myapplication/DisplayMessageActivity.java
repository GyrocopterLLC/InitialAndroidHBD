package com.example.david.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

public class DisplayMessageActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private String selectedBTDaddress = null;
    private BluetoothDeviceViewAdapter adapter;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        HandlerThread handlerThread = new HandlerThread("DisplayMessageHandlerThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ConnectThread.MessageConstants.MESSAGE_SNACKBAR:
                        Bundle msgData = msg.getData();
                        String snackbarString = msgData.getString("STR");
                        Snackbar.make(findViewById(R.id.display_message_layout), snackbarString, Snackbar.LENGTH_SHORT).show();
                }
            }
        };

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
//        Bundle message = intent.getBundleExtra(MainActivity.BA_MESSAGE);
//        ArrayList<String> mystrings = message.getStringArrayList(MainActivity.BA_LIST);
//        String[] mystringarray = new String[mystrings.size()];
//        mystringarray = mystrings.toArray(mystringarray);

        BA = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> btdevices =  BA.getBondedDevices();
        ArrayList<BluetoothDeviceViewModel> btviewmodels = new ArrayList<BluetoothDeviceViewModel>();
        for(BluetoothDevice btd : btdevices) {
            btviewmodels.add(new BluetoothDeviceViewModel(btd.getName(), btd.getAddress()));
        }

        adapter = new BluetoothDeviceViewAdapter(btviewmodels, this);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        final Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter,View v, int position, long id){
                //v.setSelected(true);
                BluetoothDeviceViewModel item = (BluetoothDeviceViewModel)adapter.getItemAtPosition(position);
                String name = item.getName();
                String mac = item.getMac();
                selectedBTDaddress = mac;

                Snackbar.make(v, "Name: "+name+"\r\nAddress: "+mac,Snackbar.LENGTH_SHORT).show();


//                Intent intent = new Intent(v.getContext(),destinationActivity.class);
//                //based on item add info to intent
//                startActivity(intent);
            }
        });
    }

    // Called when connect button is pressed
    public void ConnectToBTD(View view) {
        if(selectedBTDaddress != null) {
            BluetoothDevice mDevice = BA.getRemoteDevice(selectedBTDaddress);
//            AcceptThread acceptThread = new AcceptThread();
//            acceptThread.start();

            ConnectThread connectThread = new ConnectThread(mDevice,getResources().getString(R.string.bt_spp_uuid), mHandler);
            connectThread.start();
        }

    }
}
