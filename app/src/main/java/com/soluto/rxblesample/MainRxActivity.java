package com.soluto.rxblesample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * Created by tal on 6/20/2016.
 */
@SuppressWarnings("ALL")
public class MainRxActivity  extends AppCompatActivity {

    private void scanForDevices(){
        Observable<AdapterViewItemClickEvent> deviceSelected = RxAdapterView.itemClickEvents(devicesList);
        Observable<Long> timeout = Observable.timer(5,TimeUnit.SECONDS);

        // using RxAndroid utilities to get click stream from scan button
        RxView.clicks(scanButton)
                .doOnNext(__ -> {
                    // clear device list
                    clearDeviceList();
                    // show scanning indication
                    scanButton.setBackgroundColor(Color.GREEN);
                })
                .flatMap(__-> RxBluetoothLeScanner.startScan() // start scanning using our wrapper
                        .map(scanResult -> scanResult.getDevice()) // only need the device
                        .filter(device-> device.getName().toLowerCase().contains("soluto")) //only soluto devices
                )
                .subscribe(device->{
                    // add name to device view
                    addDeviceToView(device.getName());
                });
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter =new ArrayAdapter<>(this, R.layout.device_scan_result, R.id.textViewItem, foundDevices);

        // set   device list view
        devicesList = initDeviceView();
        scanButton = findViewById(R.id.scan);

        scanForDevices();


//        Observable<AdapterViewItemClickEvent> deviceSelected = RxAdapterView.itemClickEvents(devicesList);
//        Observable<Long> timeout = Observable.timer(5,TimeUnit.SECONDS);
//
//
//        RxView.clicks(scanButton)
//                .doOnNext(__ -> {
//                    clearDeviceList();
//                    scanButton.setBackgroundColor(Color.GREEN);
//                })
//                .flatMap(__-> RxBluetoothLeScanner.startScan()
//                        .map(scanResult -> scanResult.getDevice())
//                        .filter(device-> device.getName().toLowerCase().contains("soluto"))
//                        .distinct(device-> device.getName())
//                        .takeUntil(deviceSelected)
//                        .takeUntil(timeout)
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnCompleted(() ->scanButton.setBackgroundColor(Color.GRAY))
//                )
//                .subscribe(device->{
//                    addDeviceToView(device.getName());
//                },ex -> {
//                    Log.e("MainRxActivity","oh no",ex);
//                });
    }

    ArrayList<String> foundDevices = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ListView devicesList;
    View scanButton;

    private  void addDeviceToView(String name){
        foundDevices.add(name);
        adapter.notifyDataSetChanged();

    }
    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }
    }

    @NonNull
    private ListView initDeviceView() {
        // set device list view
        ListView devicesList = (ListView) findViewById(R.id.listView);
        devicesList.setAdapter(adapter);
        return devicesList;
    }

    private void clearDeviceList(){
        foundDevices.clear();
        adapter.notifyDataSetChanged();
    }
}
