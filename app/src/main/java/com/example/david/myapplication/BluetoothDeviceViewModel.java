package com.example.david.myapplication;

public class BluetoothDeviceViewModel {
    String name;
    String mac;

    public BluetoothDeviceViewModel() {
        this.name = "NULL";
        this.mac = "00:00:00:00:00:00";
    }

    public BluetoothDeviceViewModel(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }
}
