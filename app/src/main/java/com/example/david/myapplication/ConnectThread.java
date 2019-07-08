package com.example.david.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class ConnectThread implements Runnable {

    private final String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private BluetoothSocket mmFallbackSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;

    public enum ConnectedState {
        DISCONNECTED_STATE,
        CONNECTED_STATE
    };

    private ConnectedState mConnectedState;

    // Hold on to the Thread context for safe keeping
    private Thread mCurrentThread;

    public interface MessageConstants {
        int MESSAGE_CONNECTED = 0;
        int MESSAGE_ERROR = 1;
    }

    public ConnectThread(BluetoothDevice device, String UUID, Handler handler) {
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        mConnectedState = ConnectedState.DISCONNECTED_STATE;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        mCurrentThread = Thread.currentThread();

        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            Log.d(TAG, "Default socket connect() succeeded");
            mConnectedState = ConnectedState.CONNECTED_STATE;
            Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_CONNECTED);
            msg.obj = mmSocket;
            msg.sendToTarget();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                Log.e(TAG, "Default socket connect() failed", connectException);
 /*               // Try the fallback method
                try {
                    Class<?> clazz = mmSocket.getRemoteDevice().getClass();
                    Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};
                    mmFallbackSocket = (BluetoothSocket) m.invoke(mmSocket.getRemoteDevice(), params);
                    mmFallbackSocket.connect();
                    Log.d(TAG, "Fallback socket connect() succeeded");
                    Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_CONNECTED);
                    msg.obj = mmFallbackSocket;
                    msg.sendToTarget();
                } catch (Exception e) {
                    Log.e(TAG, "Fallback socket connect() failed", e);
                    mmFallbackSocket.close();
                    Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_ERROR);
                    msg.sendToTarget();
                }*/
                mmSocket.close();
                Message msg = mHandler.obtainMessage(MessageConstants.MESSAGE_ERROR);
                mConnectedState = ConnectedState.DISCONNECTED_STATE;
                msg.obj = mmSocket;
                msg.sendToTarget();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }

    public boolean isConnected() {
        if(mConnectedState == ConnectedState.CONNECTED_STATE) {
            return true;
        }
        return false;
    }

    // Closes the client socket
    public void close() {
        mConnectedState = ConnectedState.DISCONNECTED_STATE;
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    // Stops the thread
    public void stop() {
        mCurrentThread.interrupt();
    }
}