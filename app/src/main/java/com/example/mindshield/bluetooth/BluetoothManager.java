package com.example.mindshield.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothManager {

    private BluetoothAdapter bluetoothAdapter;

    public BluetoothManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public ArrayList<String> getPairedDevices() {

        ArrayList<String> devices = new ArrayList<>();

        if (bluetoothAdapter == null) {
            return devices;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            devices.add(device.getName() + " - " + device.getAddress());
        }

        return devices;
    }
}
