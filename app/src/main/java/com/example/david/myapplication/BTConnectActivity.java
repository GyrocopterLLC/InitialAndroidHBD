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
    public BTReadWriteThread btReadWriteThread;
    private StringBuffer mReadBuffer;
    private HandlerThread mHandlerThread;
    private ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mReadBuffer = new StringBuffer(1024);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        // Set default toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.disp_message_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.btconnect_title);

        mHandlerThread = new HandlerThread("DisplayMessageHandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ConnectThread.MessageConstants.MESSAGE_CONNECTED:
                        mSocket = (BluetoothSocket) msg.obj;
                        Snackbar.make(findViewById(R.id.display_message_layout), "Connected!", Snackbar.LENGTH_SHORT).show();
//                        btReadWriteThread = new BTReadWriteThread(mSocket, mHandler);
//                        btReadWriteThread.start();
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

                        break;
                    case ConnectThread.MessageConstants.MESSAGE_ERROR:
                        Snackbar.make(findViewById(R.id.display_message_layout),"Could not connect", Snackbar.LENGTH_SHORT).show();
                        findViewById(R.id.textConnectingLabel).setVisibility(View.INVISIBLE);
                        findViewById(R.id.connectingProgressBar).setVisibility(View.INVISIBLE);
                        break;
//                    case BTReadWriteThread.MessageConstants.MESSAGE_READ:
//                        mReadBuffer.append((String)msg.obj);
//                        if(mReadBuffer.toString().endsWith("\r\n")) {
//                            Snackbar.make(findViewById(R.id.display_message_layout), mReadBuffer.substring(0,mReadBuffer.indexOf("\r\n")), Snackbar.LENGTH_SHORT).show();
//                            mReadBuffer.delete(0, mReadBuffer.length());
//                        }
//                        break;
//                    case BTReadWriteThread.MessageConstants.MESSAGE_WRITE:
//                        break;
//                    case BTReadWriteThread.MessageConstants.MESSAGE_ERROR:
//                        break;
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

    @Override
    protected void onPause() {
        // We can safely close the handler thread and its looper now
        mHandlerThread.quitSafely();
        HandlerThread thisGuyGonnaStopNow = mHandlerThread;
        mHandlerThread = null;
        thisGuyGonnaStopNow.interrupt();


        super.onPause();
    }

    // Called when connect button is pressed
    public void ConnectToBTD(View view) {
        if(selectedBTDaddress != null) {
            BluetoothDevice mDevice = BA.getRemoteDevice(selectedBTDaddress);
//            AcceptThread acceptThread = new AcceptThread();
//            acceptThread.start();

            mConnectThread = new ConnectThread(mDevice,getResources().getString(R.string.bt_spp_uuid), mHandler);
            mConnectThread.start();
            findViewById(R.id.textConnectingLabel).setVisibility(View.VISIBLE);
            findViewById(R.id.connectingProgressBar).setVisibility(View.VISIBLE);
        }

    }
}
