package id.bonabrian.scious.libraryservice.service.btle.profiles;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.UUID;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.AbsGattCallback;
import id.bonabrian.scious.libraryservice.service.btle.BTLEQueue;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsBTLEProfile<T extends AbsBTLEDeviceSupport> extends AbsGattCallback {
    private final T mSupport;

    public AbsBTLEProfile(T support) {
        this.mSupport = support;
    }

    protected void notify(Intent intent) {
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    public TransactionBuilder performInitialized(String taskName) throws IOException {
        TransactionBuilder builder = mSupport.performInitialized(taskName);
        builder.setGattCallback(this);
        return builder;
    }

    public Context getContext() {
        return mSupport.getContext();
    }

    protected SciousDevice getDevice() {
        return mSupport.getDevice();
    }

    protected BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        return mSupport.getCharacteristic(uuid);
    }

    protected BTLEQueue getQueue() {
        return mSupport.getQueue();
    }
}
