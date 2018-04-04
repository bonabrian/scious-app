package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ReadAction extends BTLEAction {

    public ReadAction(BluetoothGattCharacteristic characteristic) {
        super(characteristic);
    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        int properties = getCharacteristic().getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            return gatt.readCharacteristic(getCharacteristic());
        }
        return false;
    }

    @Override
    public boolean expectsResult() {
        return true;
    }
}
