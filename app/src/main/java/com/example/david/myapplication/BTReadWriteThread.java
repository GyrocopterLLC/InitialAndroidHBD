package com.example.david.myapplication;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTReadWriteThread extends Thread {
    private BluetoothSocket mSocket;
    private final String TAG = "BTReadWriteThread";
    private OutputStream mOutStream;
    private InputStream mInStream;
    private DataInputStream mmInStream = null;
    private DataOutputStream mmOutStream = null;
    private byte[] mBuffer;
    private Handler mHandler;

    public interface MessageConstants {
        int MESSAGE_READ = 2;
        int MESSAGE_WRITE = 3;
        int MESSAGE_ERROR = 4;
    }

    public BTReadWriteThread(BluetoothSocket socket, Handler handler) {
        mSocket = socket;

        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the socket's streams. Use temp holders first within the try block, since
        // we can't assign the final version until it succeeds
        try {
            tmpIn = socket.getInputStream();
        } catch(IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch(IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
        mmInStream = new DataInputStream(mInStream);
        mmOutStream = new DataOutputStream(mOutStream);
    }

    public void run() {
        mBuffer = new byte[1024];
        int numBytes; // Number of bytes read

        while (true) { // Keep listening until exception
            try {
                numBytes = mmInStream.read(mBuffer);
                String newString = new String(mBuffer, 0, numBytes);
                Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes,
                        -1, newString);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while reading data. Input stream maybe disconnected.", e);
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1,
                    mBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error while sending data", e);
            Message writeErrorMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_ERROR);
            mHandler.sendMessage(writeErrorMsg);
        }
    }

    public void cancel() {
        try{
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't close the connection",e);
        }
    }
}
