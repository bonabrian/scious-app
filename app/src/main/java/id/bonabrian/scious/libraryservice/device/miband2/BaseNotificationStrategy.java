package id.bonabrian.scious.libraryservice.device.miband2;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;

import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;
import id.bonabrian.scious.libraryservice.service.btle.GattCharacteristic;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.commons.SimpleNotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class BaseNotificationStrategy<T extends AbsBTLEDeviceSupport> implements NotificationStrategy {

    private final T support;

    public BaseNotificationStrategy(T support) {
        this.support = support;
    }

    protected T getSupport() {
        return support;
    }

    @Override
    public void sendDefaultNotification(TransactionBuilder builder, SimpleNotification simpleNotification, BTLEAction extraAction) {
        VibrationProfile profile = VibrationProfile.getProfile(VibrationProfile.ID_MEDIUM, (short) 3);
        sendCustomNotification(profile, simpleNotification, extraAction, builder);
    }

    protected void sendCustomNotification(VibrationProfile vibrationProfile, @Nullable SimpleNotification simpleNotification, BTLEAction extraAction, TransactionBuilder builder) {
        //use the new alert characteristic
        BluetoothGattCharacteristic alert = support.getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_ALERT_LEVEL);
        for (short i = 0; i < vibrationProfile.getRepeat(); i++) {
            int[] onOffSequence = vibrationProfile.getOnOffSequence();
            for (int j = 0; j < onOffSequence.length; j++) {
                int on = onOffSequence[j];
                on = Math.min(500, on);
                builder.write(alert, new byte[]{GattCharacteristic.MILD_ALERT});
                builder.wait(on);
                builder.write(alert, new byte[]{GattCharacteristic.NO_ALERT});

                if (++j < onOffSequence.length) {
                    int off = Math.max(onOffSequence[j], 25);
                    builder.wait(off);
                }

                if (extraAction != null) {
                    builder.add(extraAction);
                }
            }
        }
    }

    @Override
    public void sendCustomNotification(VibrationProfile vibrationProfile, @Nullable SimpleNotification simpleNotification, int flashTimes, int flashColour, int originalColour, long flashDuration, BTLEAction extraAction, TransactionBuilder builder) {
        sendCustomNotification(vibrationProfile, simpleNotification, extraAction, builder);
    }

    @Override
    public void stopCurrentNotification(TransactionBuilder builder) {
        BluetoothGattCharacteristic alert = support.getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_ALERT_LEVEL);
        builder.write(alert, new byte[]{GattCharacteristic.NO_ALERT});
    }
}
