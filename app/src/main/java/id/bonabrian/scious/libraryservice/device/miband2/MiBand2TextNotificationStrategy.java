package id.bonabrian.scious.libraryservice.device.miband2;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;
import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;
import id.bonabrian.scious.libraryservice.service.btle.GattCharacteristic;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification.AlertCategory;
import id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification.AlertNotificationProfile;
import id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification.NewAlert;
import id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification.OverflowStrategy;
import id.bonabrian.scious.libraryservice.service.commons.SimpleNotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MiBand2TextNotificationStrategy extends MiBand2NotificationStrategy {
    private final BluetoothGattCharacteristic newAlertCharacteristic;

    public MiBand2TextNotificationStrategy(MiBand2Support support) {
        super(support);
        newAlertCharacteristic = support.getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_NEW_ALERT);
    }

    @Override
    protected void sendCustomNotification(VibrationProfile vibrationProfile, @Nullable SimpleNotification simpleNotification, BTLEAction extraAction, TransactionBuilder builder) {
        if (simpleNotification != null && simpleNotification.getAlertCategory() == AlertCategory.IncomingCall) {
            sendAlert(simpleNotification, builder);
            return;
        }
        super.sendCustomNotification(vibrationProfile, simpleNotification, extraAction, builder);
        // and finally send the text message, if any
        if (simpleNotification != null && !StringUtils.isEmpty(simpleNotification.getMessage())) {
            sendAlert(simpleNotification, builder);
        }
    }

    @Override
    protected void startNotify(TransactionBuilder builder, int alertLevel, SimpleNotification simpleNotification) {
        builder.write(newAlertCharacteristic, getNotifyMessage(simpleNotification));
    }

    protected byte[] getNotifyMessage(SimpleNotification simpleNotification) {
        int numAlerts = 1;
        if (simpleNotification != null) {
            switch (simpleNotification.getAlertCategory()) {
                case Email:
                    return new byte[] { BLETypeConversions.fromUint8(AlertCategory.Email.getId()), BLETypeConversions.fromUint8(numAlerts)};
                case InstantMessage:
                    return new byte[] { BLETypeConversions.fromUint8(AlertCategory.CustomMiBand2.getId()), BLETypeConversions.fromUint8(numAlerts), MiBand2Service.ICON_CHAT};
                case News:
                    return new byte[] { BLETypeConversions.fromUint8(AlertCategory.CustomMiBand2.getId()), BLETypeConversions.fromUint8(numAlerts), MiBand2Service.ICON_PENGUIN};
            }
        }
        return new byte[] { BLETypeConversions.fromUint8(AlertCategory.SMS.getId()), BLETypeConversions.fromUint8(numAlerts)};
    }

    protected void sendAlert(@NonNull SimpleNotification simpleNotification, TransactionBuilder builder) {
        AlertNotificationProfile<?> profile = new AlertNotificationProfile<>(getSupport());
        // override the alert category,  since only SMS and incoming call support text notification
        AlertCategory category = AlertCategory.SMS;
        if (simpleNotification.getAlertCategory() == AlertCategory.IncomingCall) {
            category = simpleNotification.getAlertCategory();
        }
        NewAlert alert = new NewAlert(category, 1, simpleNotification.getMessage());
        profile.newAlert(builder, alert, OverflowStrategy.MAKE_MULTIPLE);
    }
}
