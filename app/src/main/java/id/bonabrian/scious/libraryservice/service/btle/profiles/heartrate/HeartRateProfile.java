package id.bonabrian.scious.libraryservice.service.btle.profiles.heartrate;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.GattCharacteristic;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.btle.profiles.AbsBTLEProfile;

/**
 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.heart_rate.xml
 */

public class HeartRateProfile<T extends AbsBTLEDeviceSupport> extends AbsBTLEProfile<T> {
    private static final String TAG = HeartRateProfile.class.getSimpleName();

    public static final int ERR_CONTROL_POINT_NOT_SUPPORTED = 0x80;

    public HeartRateProfile(T support) {
        super(support);
    }

    public void resetEnergyExpended(TransactionBuilder builder) {
        writeToControlPoint((byte) 0x01, builder);
    }

    protected void writeToControlPoint(byte value, TransactionBuilder builder) {
        writeToControlPoint(new byte[] { value }, builder);
    }

    protected void writeToControlPoint(byte[] value, TransactionBuilder builder) {
        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), value);
    }

    public void requestBodySensorLocation(TransactionBuilder builder) {

    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.i(TAG, "Heart rate: " + heartRate);
        }
        return false;
    }
}
