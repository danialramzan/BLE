package com.danialramza.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BLEActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private TextView heartRateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        heartRateTextView = findViewById(R.id.heartRateTextView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported.", Toast.LENGTH_SHORT).show();
            finish();
        }


        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }


        startScanning();


    }

    private void startScanning() {

        // get necessary permissions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    1);
            return;
        }

        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null && device.getName().equals("HeartRateSim")) {
                    connectToDevice(device);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.wtf("GNX", String.valueOf(errorCode));
            }
        });
    }

    private void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        if (bluetoothGatt != null) {
            Log.wtf("GNX", "connectGatt() called successfully.");
        } else {
            Log.wtf("GNX", "Failed to call connectGatt()");
        }
    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.wtf("GNX", "onConnectionStateChange() called, status: " + status + ", newState: " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.wtf("GNX", "Connected to GATT server.");

                Log.wtf("GNX", "Starting service discovery...");
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.wtf("GNX", "Disconnected from GATT server.");
            } else {
                Log.wtf("GNX", "Unexpected connection state change, status: " + status + ", newState: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.wtf("GNX", "onServicesDiscovered() called, status: " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.wtf("GNX", "Services discovered successfully.");

                // query available services
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
//                    Log.wtf("GNX", "Discovered service: " + service.getUuid());

                    // log characteristics for all services
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        Log.wtf("GNX", "found characteristic: " + characteristic.getUuid());
                        if (characteristic.getUuid().equals(UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb"))) {
                            Log.wtf("GNX", "Heart Rate Measurement characteristic found.");

                            // Read the heart rate characteristic
                            boolean success = gatt.readCharacteristic(characteristic);
                            if (success) {
                                Log.wtf("GNX", "Started reading HRS characteristic");
                            } else {
                                Log.wtf("GNX", "failed HRS char");
                            }

                        }
                    }
                }
            } else {
                Log.wtf("GNX", "Failed to discover services, status: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.wtf("GNX", "onCharacteristicRead status: " + status + ", characteristic: " + characteristic.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.wtf("FINALCHAR", "Characteristic read successfully: " + Arrays.toString(characteristic.getValue()));
                String heartRateData = Arrays.toString(characteristic.getValue());
                runOnUiThread(() -> heartRateTextView.setText("Heart Rate: " + heartRateData));
            } else {
                Log.wtf("GNX", "ERROR READING CHAR, status: " + status);
            }
        }


//        @Override
//        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            super.onDescriptorRead(gatt, descriptor, status);
//
//            Log.d("GNX", "onDescriptorRead() called, status: " + status + ", descriptor: " + descriptor.getUuid());
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.d("GNX", "Descriptor read successfully.");
//            } else {
//                Log.wtf("GNX", "Failed to read descriptor, status: " + status);
//            }
//        }


    };


}

