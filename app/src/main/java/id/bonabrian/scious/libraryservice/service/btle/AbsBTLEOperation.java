package id.bonabrian.scious.libraryservice.service.btle;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.service.BTLEOperation;
import id.bonabrian.scious.libraryservice.service.operations.OperationStatus;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsBTLEOperation<T extends AbsBTLEDeviceSupport> implements IGattCallback, BTLEOperation {
    private final T mSupport;
    protected OperationStatus operationStatus = OperationStatus.INITIAL;

    protected AbsBTLEOperation(T support) {
        mSupport = support;
    }

    @Override
    public final void perform() throws IOException {
        operationStatus = OperationStatus.STARTED;
        prePerform();
        operationStatus = OperationStatus.RUNNING;
        doPerform();
    }

    protected void prePerform() throws IOException {
    }

    protected abstract void doPerform() throws IOException;

    protected void operationFinished() throws IOException {
    }

    public TransactionBuilder performInitialized(String taskName) throws IOException {
        TransactionBuilder builder = mSupport.performInitialized(taskName);
        builder.setGattCallback(this);
        return builder;
    }

    protected Context getContext() {
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

    protected void unsetBusy() {
        if (getDevice().isBusy()) {
            getDevice().unsetBusyTask();
            getDevice().sendDeviceUpdateIntent(getContext());
        }
    }

    public boolean isOperationRunning() {
        return operationStatus == OperationStatus.RUNNING;
    }

    public boolean isOperationFinished() {
        return operationStatus == OperationStatus.FINISHED;
    }

    public T getSupport() {
        return mSupport;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        mSupport.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        mSupport.onServicesDiscovered(gatt);
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        return mSupport.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        return mSupport.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        return mSupport.onCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        return mSupport.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        return mSupport.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        mSupport.onReadRemoteRssi(gatt, rssi, status);
    }
}
