package com.soluto.rxblesample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by tal on 6/20/2016.
 */
@SuppressWarnings("ALL")
public class MainRxActivity  extends AppCompatActivity {

    ArrayList<String> foundDevices = new ArrayList<>();
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.device_scan_result, R.id.textViewItem, foundDevices);


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set   device list view
        ListView devicesList = initDeviceView();
        View scanButton = findViewById(R.id.scan);

        // device list item click stream
        Observable<AdapterViewItemClickEvent> deviceSelected = RxAdapterView.itemClickEvents(devicesList);

        // timeout stream
        Observable<AdapterViewItemClickEvent> timeout = Observable.timer(20,TimeUnit.SECONDS).map(x-> null);

        RxView.clicks(scanButton)
                .doOnNext(__ -> requestLocationPermission())
                .flatMap(__ -> RxBluetoothLeScanner.startScan()
                        .filter(scanResult -> scanResult.getDevice().getName().contains("soluto"))
                        .takeUntil(timeout.mergeWith(deviceSelected))
                )
                .subscribe(scanResult->{
                    addDeviceToList(scanResult.getDevice().getName());
                },
                throwable -> {
                    Log.e("MainRxActivity","failed to show scanned devices",throwable);
                });

    }

    private  void addDeviceToList(String name){
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
}
