package id.bonabrian.scious.libraryservice.service.btle;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IGattCallback {
    void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

    void onServicesDiscovered(BluetoothGatt gatt);

    boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);
}
