package id.bonabrian.scious.libraryservice.device.miband2;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;

import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;
import id.bonabrian.scious.libraryservice.service.btle.GattCharacteristic;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.commons.SimpleNotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MiBand2NotificationStrategy extends BaseNotificationStrategy<MiBand2Support> {
    private final BluetoothGattCharacteristic alertLevelCharacteristic;

    public MiBand2NotificationStrategy(MiBand2Support support) {
        super(support);
        alertLevelCharacteristic = support.getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_ALERT_LEVEL);
    }

    @Override
    protected void sendCustomNotification(VibrationProfile vibrationProfile, @Nullable SimpleNotification simpleNotification, BTLEAction extraAction, TransactionBuilder builder) {
        for (short i = 0; i < vibrationProfile.getRepeat(); i++) {
            int[] onOffSequence = vibrationProfile.getOnOffSequence();
            for (int j = 0; j < onOffSequence.length; j++) {
                int on = onOffSequence[j];
                on = Math.min(500, on); // longer than 500ms is not possible
                startNotify(builder, vibrationProfile.getAlertLevel(), simpleNotification);
                builder.wait(on);
                stopNotify(builder);

                if (++j < onOffSequence.length) {
                    int off = Math.max(onOffSequence[j], 25); // wait at least 25ms
                    builder.wait(off);
                }

                if (extraAction != null) {
                    builder.add(extraAction);
                }
            }
        }
    }

    protected void startNotify(TransactionBuilder builder, int alertLevel, @Nullable SimpleNotification simpleNotification) {
        builder.write(alertLevelCharacteristic, new byte[] {(byte) alertLevel});

    }

    protected void stopNotify(TransactionBuilder builder) {
        builder.write(alertLevelCharacteristic, new byte[]{GattCharacteristic.NO_ALERT});
    }

    @Override
    public void sendCustomNotification(VibrationProfile vibrationProfile, @Nullable SimpleNotification simpleNotification, int flashTimes, int flashColour, int originalColour, long flashDuration, BTLEAction extraAction, TransactionBuilder builder) {
        sendCustomNotification(vibrationProfile, simpleNotification, extraAction, builder);
    }
}
