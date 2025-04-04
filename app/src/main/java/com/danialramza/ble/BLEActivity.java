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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;


import java.util.List;
import java.util.UUID;

public class BLEActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }


        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }


        startScanning();


    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startScanning() {

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

//            <uses-permission android:name="android.permission.BLUETOOTH"/>
//    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
//    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
//    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
//    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
//    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

        Log.wtf("GNX", "INIT");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.wtf("GNX", "[PERMISSION ISSUE");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    1);
            return;
        }

        Log.wtf("GNX", "A");

        Log.wtf("GNX", "Starting BLE scan...");
        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.wtf("GNX", "Scan result received: " + result.getDevice().getName());
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null && device.getName().equals("HeartRateSim")) {
                    Log.wtf("GNX", "YOOOOOOOOOOOOOOOOOOOO");
                    connectToDevice(device);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e("GNX", "Scan failed with error code: " + errorCode);
            }
        });


//        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
//            @Override
//            public void onScanResult(int callbackType, ScanResult result) {
//                Log.wtf("GNX", "B");
//                BluetoothDevice device = result.getDevice();
//                if (device.getName() != null && device.getName().equals("HeartRateSim")) {
//                    connectToDevice(device);
//                }
//                Log.wtf("GNX", "C");
//            }
//
//            @Override
//            public void onScanFailed(int errorCode) {
//                Log.e("GNX", "Scan failed with error code: " + errorCode);
//            }
//        });
    }

    private void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        if (bluetoothGatt != null) {
            Log.d("ANX", "connectGatt() called successfully.");
        } else {
            Log.e("ANX", "Failed to call connectGatt()");
        }
    }

//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            Log.wtf("DNX", "PRE");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.wtf("DNX", "Success");
//
//                BluetoothGattService heartRateService = gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
//                Log.wtf("DNX", "ABCD");
//                BluetoothGattCharacteristic heartRateMeasurementCharacteristic = heartRateService.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
//                Log.wtf("DNX", String.valueOf(heartRateMeasurementCharacteristic.getProperties()));
//
//
//                gatt.readCharacteristic(heartRateMeasurementCharacteristic);
//            } else {
//                Log.wtf("DNX", "Failure!!");
//            }
//        }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("GNX", "onConnectionStateChange() called, status: " + status + ", newState: " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("GNX", "Connected to GATT server.");

                // After connection, discover services
                Log.d("GNX", "Starting service discovery...");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("GNX", "Disconnected from GATT server.");
            } else {
                Log.d("GNX", "Unexpected connection state change, status: " + status + ", newState: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.d("GNX", "onServicesDiscovered() called, status: " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GNX", "Services discovered successfully.");

                // Log available services
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.d("GNX", "Discovered service: " + service.getUuid());

                    // Log characteristics for each service
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        Log.d("GNX", "Discovered characteristic: " + characteristic.getUuid());
                    }
                }
            } else {
                Log.e("GNX", "Failed to discover services, status: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.d("GNX", "onCharacteristicRead() called, status: " + status + ", characteristic: " + characteristic.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GNX", "Characteristic read successfully: " + characteristic.getValue());
            } else {
                Log.e("GNX", "Failed to read characteristic, status: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.d("GNX", "onCharacteristicWrite() called, status: " + status + ", characteristic: " + characteristic.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GNX", "Characteristic written successfully.");
            } else {
                Log.e("GNX", "Failed to write characteristic, status: " + status);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            Log.d("GNX", "onDescriptorRead() called, status: " + status + ", descriptor: " + descriptor.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GNX", "Descriptor read successfully.");
            } else {
                Log.e("GNX", "Failed to read descriptor, status: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            Log.d("GNX", "onDescriptorWrite() called, status: " + status + ", descriptor: " + descriptor.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GNX", "Descriptor written successfully.");
            } else {
                Log.e("GNX", "Failed to write descriptor, status: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.d("GNX", "onCharacteristicChanged() called, characteristic: " + characteristic.getUuid());
            Log.d("GNX", "New characteristic value: " + characteristic.getValue());
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            Log.d("GNX", "onReadRemoteRssi() called, status: " + status + ", RSSI: " + rssi);
        }
    };


//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            Log.wtf("DNX", "PRE");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.wtf("DNX", "Success");
//
//                BluetoothGattService heartRateService = gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
//                Log.wtf("DNX", "ABCD");
//                BluetoothGattCharacteristic heartRateMeasurementCharacteristic = heartRateService.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
//                Log.wtf("DNX", String.valueOf(heartRateMeasurementCharacteristic.getProperties()));
//
////                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//////                    // T/ODO: Consider calling
//////                    //    ActivityCompat#requestPermissions
//////                    // here to request the missing permissions, and then overriding
//////                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//////                    //                                          int[] grantResults)
//////                    // to handle the case where the user grants the permission. See the documentation
//////                    // for ActivityCompat#requestPermissions for more details.
//////                    return;
////                }
//                gatt.readCharacteristic(heartRateMeasurementCharacteristic);
//            }
//        }

//    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            byte[] data = characteristic.getValue();
//            int heartRate = data[0];
//            Log.d("HeartRate", "Heart Rate: " + heartRate);
//        }
//    }
}

