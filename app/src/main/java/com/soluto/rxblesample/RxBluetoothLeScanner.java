package com.soluto.rxblesample;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by tal on 6/20/2016.
 */
public class RxBluetoothLeScanner {

    public static Observable<ScanResult> startScan(){
        return Observable.create(new Observable.OnSubscribe<ScanResult>() {
            @Override
            public void call(Subscriber<? super ScanResult> subscriber) {

                // getting android scanner
                BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

                if (scanner == null) {
                    subscriber.onError(new Exception("bluetooth is disabled"));
                    return;
                }

                // create android bluetooth scan callback instance
                ScanCallback scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        subscriber.onNext(result);
                    }
                };

                // start scanning
                scanner.startScan(scanCallback);

                // stop scanning when observable unsubscribes
                subscriber.add(Subscriptions.create(() -> scanner.stopScan(scanCallback)));
            }
        });

    }
}
