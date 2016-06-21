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

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    ArrayList<String> foundDevices = new ArrayList<>();
    ArrayAdapter<String> adapter;
    BluetoothLeScanner scanner;

    private ScanCallback solutoDeviceScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String name = result.getDevice().getName() == null ? " " : result.getDevice().getName().toLowerCase();

            if (name.contains("soluto")){ // filter logic
                addDeviceToList(name); // ui logic
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ArrayAdapter<>(this, R.layout.device_scan_result, R.id.textViewItem, foundDevices);
         scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();


        ListView devicesList = initDeviceView();
        View scanButton = findViewById(R.id.scan);
        TextView selectedDevice = (TextView) findViewById(R.id.selectedDevice);



        // also stop scanning when user selects a device
        devicesList.setOnItemClickListener((adapterView, view, i, l) -> {
            scanButton.setBackgroundColor(Color.GRAY);
            scanner.stopScan(solutoDeviceScanCallback);

            String name = (String) adapterView.getAdapter().getItem(i);
            selectedDevice.setText(name);


        });


        // setup scan button to start scan with callback instance
        scanButton.setOnClickListener(view -> {
            requestLocationPermission();

            clearDeviceList();

            // show indication that we're scanning
            scanButton.setBackgroundColor(Color.GREEN);
            scanner.startScan(solutoDeviceScanCallback);

            // set a time to stop scanning in 5 seconds
            new android.os.Handler().postDelayed(() -> {
                scanButton.setBackgroundColor(Color.GRAY);
                scanner.stopScan(solutoDeviceScanCallback);
            },5000);

        });

    }

    private void clearDeviceList(){
        foundDevices.clear();
        adapter.notifyDataSetChanged();
    }

    // utils
    private  void addDeviceToList(String name){
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
