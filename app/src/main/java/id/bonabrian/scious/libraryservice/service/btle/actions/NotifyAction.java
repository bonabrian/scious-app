package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;

import static id.bonabrian.scious.libraryservice.service.btle.GattDescriptor.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class NotifyAction extends BTLEAction {
    private static final String TAG = NotifyAction.class.getSimpleName();
    protected final boolean enableFlag;
    private boolean hasWrittenDescriptor = true;

    public NotifyAction(BluetoothGattCharacteristic characteristic, boolean enable) {
        super(characteristic);
        enableFlag = enable;
    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        boolean result = gatt.setCharacteristicNotification(getCharacteristic(), enableFlag);
        if (result) {
            BluetoothGattDescriptor notifyDescriptor = getCharacteristic().getDescriptor(UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
            if (notifyDescriptor != null) {
                int properties = getCharacteristic().getProperties();
                if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    Log.d(TAG, "use NOTIFICATION");
                    notifyDescriptor.setValue(enableFlag ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    result = gatt.writeDescriptor(notifyDescriptor);
                } else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    Log.d(TAG, "use INDICATION");
                    notifyDescriptor.setValue(enableFlag ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    result = gatt.writeDescriptor(notifyDescriptor);
                    hasWrittenDescriptor = true;
                } else {
                    hasWrittenDescriptor = false;
                }
            } else {
                Log.w(TAG, "Descriptor CLIENT_CHARACTERISTIC_CONFIGURATION for characteristic " + getCharacteristic().getUuid() + " is null");
                hasWrittenDescriptor = false;
            }
        } else {
            hasWrittenDescriptor = false;
            Log.e(TAG, "Unable to enable notification for " + getCharacteristic().getUuid());
        }
        return result;
    }

    @Override
    public boolean expectsResult() {
        return hasWrittenDescriptor;
    }
}
