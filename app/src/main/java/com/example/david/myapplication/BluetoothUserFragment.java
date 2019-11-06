package com.example.david.myapplication;

import android.support.v4.app.Fragment;

public abstract class BluetoothUserFragment extends Fragment {

    public abstract void ReceiveDataCallback(StringBuffer newData);
    public abstract void ErrorCallback();
    public abstract void DisconnectCallback();
}
