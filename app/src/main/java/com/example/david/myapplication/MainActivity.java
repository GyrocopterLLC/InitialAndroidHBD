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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String BA_LIST = "com.example.david.myapplication.BA_LIST";
    public static final String BA_MESSAGE = "com.example.david.myapplication.BA_MESSAGE";
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

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
        mSpeedoView.setCurrentSpeed(mCurrentSpeed);
        if(((GlobalSettings)getApplication()).isConnected()){


            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Connected to :"+((GlobalSettings)getApplication()).getDevice().getName());
        } else {
            ((TextView)findViewById(R.id.text_btdevice_info)).setText("Not connected");
        }

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
            case R.id.action_settings:
                Intent intent2 = new Intent(this, SettingsActivity.class);
                startActivity(intent2);
                break;
        }
        return true;
    }

    /** called when speed increase button is pressed **/
    public void onSpeedIncrease(View view)
    {
        if(mCurrentSpeed < 30){
            mCurrentSpeed = mCurrentSpeed + 1.0f;
            mSpeedoView.setCurrentSpeed(mCurrentSpeed);
        }
    }

    /** called when speed decrease button is pressed **/
    public void onSpeedDecrease(View view)
    {
        if(mCurrentSpeed > 0){
            mCurrentSpeed = mCurrentSpeed - 1.0f;
            mSpeedoView.setCurrentSpeed(mCurrentSpeed);
        }
    }
}