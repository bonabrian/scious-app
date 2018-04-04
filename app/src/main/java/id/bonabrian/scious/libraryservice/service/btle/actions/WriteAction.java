package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class WriteAction extends BTLEAction {
    private final byte[] value;

    public WriteAction(BluetoothGattCharacteristic characteristic, byte[] value) {
        super(characteristic);
        this.value = value;
    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = getCharacteristic();
        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 || ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)) {
            return writeValue(gatt, characteristic, value);
        }
        return false;
    }

    protected boolean writeValue(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (characteristic.setValue(value)) {
            return gatt.writeCharacteristic(characteristic);
        }
        return false;
    }

    protected final byte[] getValue() {
        return value;
    }

    @Override
    public boolean expectsResult() {
        return true;
    }
}
