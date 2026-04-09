package com.example.mindshield.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface DeviceFoundListener {
    void onDeviceFound(BluetoothDevice device);
}