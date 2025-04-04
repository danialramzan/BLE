package com.danialramza.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.wtf("GNX", "Success");
                BluetoothGattService heartRateService = gatt.getService(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"));
                BluetoothGattCharacteristic heartRateMeasurementCharacteristic = heartRateService.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));

//                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
////                    // T/ODO: Consider calling
////                    //    ActivityCompat#requestPermissions
////                    // here to request the missing permissions, and then overriding
////                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
////                    //                                          int[] grantResults)
////                    // to handle the case where the user grants the permission. See the documentation
////                    // for ActivityCompat#requestPermissions for more details.
////                    return;
//                }
                gatt.readCharacteristic(heartRateMeasurementCharacteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                int heartRate = data[0];
                Log.d("HeartRate", "Heart Rate: " + heartRate);
            }
        }
    };
}
