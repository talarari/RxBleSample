package com.soluto.rxblesample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    private void scanForDevices(){
        // setup scan button to start scan with callback instance
        scanButton.setOnClickListener(view -> {
            requestLocationPermission();
            clearDeviceList();

            // show indication that we're scanning
            scanButton.setBackgroundColor(Color.GREEN);
            scanner.startScan(solutoDeviceScanCallback);

            // set a time to stop scanning in 3 seconds, better be quick
            new android.os.Handler().postDelayed(() -> {
                scanButton.setBackgroundColor(Color.GRAY);
                scanner.stopScan(solutoDeviceScanCallback);
            }, 3000);
        });

        // stop scanning when user selects a device
        devicesList.setOnItemClickListener((adapterView, view, i, l) -> {
            // show indication that we're not scanning anymore
            scanButton.setBackgroundColor(Color.GRAY);
            // stop scanning, pass the callback
            scanner.stopScan(solutoDeviceScanCallback);
        });
    }

    private ScanCallback solutoDeviceScanCallback = new ScanCallback() {

        private HashSet<String> foundAlready = new HashSet<>();

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            // get name or address
            String name = result.getDevice().getName() == null ? result.getDevice().getAddress() :
                    result.getDevice().getName().toLowerCase();

            // filter for devices named 'soluto'
            // dont show duplicates
            if (name.contains("soluto") && !foundAlready.contains(name)){

                // remember device
                foundAlready.add(name);

                // add name to device view
                addDeviceToView(name);
            }
        }
    };




    ArrayList<String> foundDevices = new ArrayList<>();
    ArrayAdapter<String> adapter;
    BluetoothLeScanner scanner;
    ListView devicesList;
    View scanButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ArrayAdapter<>(this, R.layout.device_scan_result, R.id.textViewItem, foundDevices);
        scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        if (scanner == null) return;

        devicesList = initDeviceView();
        scanButton = findViewById(R.id.scan);

        scanForDevices();

//        // also stop scanning when user selects a device
//        devicesList.setOnItemClickListener((adapterView, view, i, l) -> {
//            scanButton.setBackgroundColor(Color.GRAY);
//            scanner.stopScan(solutoDeviceScanCallback);
//        });


//        // setup scan button to start scan with callback instance
//        scanButton.setOnClickListener(view -> {
//            requestLocationPermission();
//            clearDeviceList();
//
//            // show indication that we're scanning
//            scanButton.setBackgroundColor(Color.GREEN);
//
//            scanner.startScan(solutoDeviceScanCallback);
//
//            // set a time to stop scanning in 5 seconds
//            new android.os.Handler().postDelayed(() -> {
//                scanButton.setBackgroundColor(Color.GRAY);
//                scanner.stopScan(solutoDeviceScanCallback);
//            },5000);
//
//        });

    }

    private void clearDeviceList(){
        foundDevices.clear();
        adapter.notifyDataSetChanged();
    }

    // utils
    private  void addDeviceToView(String name){
        foundDevices.add(name);
        adapter.notifyDataSetChanged();

    }

    @NonNull
    private ListView initDeviceView() {
        // set device list view
        ListView devicesList = (ListView) findViewById(R.id.listView);
        devicesList.setAdapter(adapter);
        return devicesList;
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }
    }

}
