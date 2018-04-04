package id.bonabrian.scious.libraryservice.service.btle;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Date;

import id.bonabrian.scious.util.DateTimeUtils;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class BTLEAction {
    private final BluetoothGattCharacteristic characteristic;
    private final long creationTimestamp;

    public BTLEAction(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        creationTimestamp = System.currentTimeMillis();
    }

    public abstract boolean expectsResult();

    public abstract boolean run(BluetoothGatt gatt);

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    protected String getCreationTime() {
        return DateTimeUtils.formatDateTime(new Date(creationTimestamp));
    }

    public String toString() {
        BluetoothGattCharacteristic characteristic = getCharacteristic();
        String uuid = characteristic == null ? "(null)" : characteristic.getUuid().toString();
        return getCreationTime() + ": " + getClass().getSimpleName() + " on characteristic: " + uuid;
    }
}
