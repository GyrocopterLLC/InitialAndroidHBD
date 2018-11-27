package com.example.david.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String BA_LIST = "com.example.david.myapplication.BA_LIST";
    public static final String BA_MESSAGE = "com.example.david.myapplication.BA_MESSAGE";
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Check if BT adapter is disabled, and enable it.
        BA = BluetoothAdapter.getDefaultAdapter();
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
                Intent intent = new Intent(this, DisplayMessageActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    /** called when button is pressed **/
    public void SendMessage(View view)
    {
        // Do a thing when button is pressed.
        //pairedDevices = BA.getBondedDevices();
        //ArrayList<String> list_of_devices = new ArrayList<String>();
        //for(BluetoothDevice btd : pairedDevices)
        //{
        //    list_of_devices.add(btd.getName());
        //}
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        //Bundle extras = new Bundle();
        //extras.putStringArrayList(BA_LIST, list_of_devices);
        //intent.putExtra(BA_MESSAGE, extras);
        startActivity(intent);
    }
}