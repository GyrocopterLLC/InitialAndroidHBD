package com.example.david.myapplication;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class GlobalSettings extends Application {
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private Boolean mIsConnected;

    public GlobalSettings() {
        super();
        mDevice = null;
        mSocket = null;
        mIsConnected = false;
    }

    public BluetoothDevice getDevice()
    {
        return mDevice;
    }

    public void setDevice(BluetoothDevice device)
    {
        mDevice = device;
    }

    public BluetoothSocket getSocket()
    {
        return mSocket;
    }

    public void setSocket(BluetoothSocket socket)
    {
        mSocket = socket;
    }

    public Boolean isConnected()
    {
        return mIsConnected;
    }

    public void setConnected(Boolean connected)
    {
        mIsConnected = connected;
    }

}
