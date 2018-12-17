package com.example.david.myapplication;

public class BluetoothDeviceViewModel {
    String name;
    String mac;
    Boolean connected;

    public BluetoothDeviceViewModel() {
        this.name = "NULL";
        this.mac = "00:00:00:00:00:00";
        this.connected = false;
    }

    public BluetoothDeviceViewModel(String name, String mac, Boolean connected) {
        this.name = name;
        this.mac = mac;
        this.connected = connected;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean newConnectState) {
        connected = newConnectState;
    }
}
