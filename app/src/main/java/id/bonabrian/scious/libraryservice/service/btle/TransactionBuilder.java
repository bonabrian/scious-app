package id.bonabrian.scious.libraryservice.service.btle;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;
import android.util.Log;

import id.bonabrian.scious.libraryservice.service.btle.actions.NotifyAction;
import id.bonabrian.scious.libraryservice.service.btle.actions.ReadAction;
import id.bonabrian.scious.libraryservice.service.btle.actions.WaitAction;
import id.bonabrian.scious.libraryservice.service.btle.actions.WriteAction;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class TransactionBuilder {
    private static final String TAG = TransactionBuilder.class.getSimpleName();

    private final Transaction mTransaction;
    private boolean mQueued;

    public TransactionBuilder(String taskName) {
        mTransaction = new Transaction(taskName);
    }

    public TransactionBuilder read(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            Log.w(TAG, "Unable to read characteristic: null");
            return this;
        }
        ReadAction action = new ReadAction(characteristic);
        return add(action);
    }

    public TransactionBuilder write(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (characteristic == null) {
            Log.w(TAG, "Unable to write characteristic: null");
            return this;
        }
        WriteAction action = new WriteAction(characteristic, data);
        return add(action);
    }

    public TransactionBuilder notify(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (characteristic == null) {
            Log.w(TAG, "Unable to notify characteristic: null");
            return this;
        }
        NotifyAction action = createNotifyAction(characteristic, enable);
        return add(action);
    }

    protected NotifyAction createNotifyAction(BluetoothGattCharacteristic characteristic, boolean enable) {
        return new NotifyAction(characteristic, enable);
    }

    public TransactionBuilder wait(int millis) {
        WaitAction action = new WaitAction(millis);
        return add(action);
    }

    public TransactionBuilder add(BTLEAction action) {
        mTransaction.add(action);
        return this;
    }

    public void setGattCallback(@Nullable IGattCallback callback) {
        mTransaction.setGattCallback(callback);
    }

    public
    @Nullable
    IGattCallback getGattCallback() {
        return mTransaction.getGattCallback();
    }

    public void queue(BTLEQueue queue) {
        if (mQueued) {
            throw new IllegalStateException("This builder had already been queued. You must not reuse it.");
        }
        mQueued = true;
        queue.add(mTransaction);
    }

    public Transaction getTransaction() {
        return mTransaction;
    }
}