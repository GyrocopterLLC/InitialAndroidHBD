package com.example.david.myapplication;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BTReadWriteThread implements Runnable{
    private BluetoothSocket mSocket;
    private final String TAG = "BTReadWriteThread";
    private OutputStream mOutStream;
    private InputStream mInStream;
    private DataInputStream mmInStream = null;
    private DataOutputStream mmOutStream = null;
    private byte[] mBuffer;
    private Handler mHandler;

    // Hold on to the Thread context for safe keeping
    private Thread mCurrentThread;

    public interface MessageConstants {
        int MESSAGE_READ = 2;
        int MESSAGE_WRITE = 3;
        int MESSAGE_ERROR = 4;
        int MESSAGE_DISCONNECTED = 5;
    }

    public BTReadWriteThread(BluetoothSocket socket, Handler handler) {
        mSocket = socket;

        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the socket's streams. Use temp holders first within the try block, since
        // we can't assign the final version until it succeeds
        try {
            tmpIn = mSocket.getInputStream();
        } catch(IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = mSocket.getOutputStream();
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

        mCurrentThread = Thread.currentThread();

        while (true) { // Keep listening until exception
            try {
                numBytes = mmInStream.read(mBuffer);
                StringBuffer newStringB = new StringBuffer(numBytes);
                for(int i = 0; i < numBytes; i++) {
                    newStringB.append((char)(((char)mBuffer[i])&0xFF));
                }

                Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes,
                        -1, newStringB);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while reading data. Input stream maybe disconnected.", e);
                try {
                    mSocket.close();
                } catch(IOException eClose) {
                    Log.e(TAG, "Could not close the client socket", eClose);
                }
                Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_DISCONNECTED);
                readMsg.sendToTarget();
                break;
            }
        }
    }

    public void write(StringBuffer stringB) {
        try {
            byte[] bytes = new byte[stringB.length()];
            for(int i = 0; i < stringB.length(); i++) {
                bytes[i] = (byte)(stringB.charAt(i) & 0xFF);
            }

            mmOutStream.write(bytes);

            Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error while sending data", e);
            Message writeErrorMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_ERROR);
            mHandler.sendMessage(writeErrorMsg);
        }
    }
}
