package id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;
import id.bonabrian.scious.libraryservice.service.btle.GattCharacteristic;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.btle.profiles.AbsBTLEProfile;
import id.bonabrian.scious.util.StringUtils;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class AlertNotificationProfile<T extends AbsBTLEDeviceSupport> extends AbsBTLEProfile<T> {
    private static final String TAG = AlertNotificationProfile.class.getSimpleName();
    private int maxLength = 18; // Mi2-ism?

    public AlertNotificationProfile(T support) {
        super(support);
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void configure(TransactionBuilder builder, AlertNotificationControl control) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_ALERT_NOTIFICATION_CONTROL_POINT);
        if (characteristic != null) {
            builder.write(characteristic, control.getControlMessage());
        }
    }

    public void updateAlertLevel(TransactionBuilder builder, AlertLevel level) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_ALERT_LEVEL);
        if (characteristic != null) {
            builder.write(characteristic, new byte[] {BLETypeConversions.fromUint8(level.getId())});
        }
    }

    public void newAlert(TransactionBuilder builder, NewAlert alert, OverflowStrategy strategy) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_NEW_ALERT);
        if (characteristic != null) {
            String message = StringUtils.ensureNotNull(alert.getMessage());
            if (message.length() > maxLength && strategy == OverflowStrategy.TRUNCATE) {
                message = StringUtils.truncate(message, maxLength);
            }

            int numChunks = message.length() / maxLength;
            if (message.length() % maxLength > 0) {
                numChunks++;
            }

            try {
                boolean hasAlerted = false;
                for (int i = 0; i < numChunks; i++) {
                    int offset = i * maxLength;
                    int restLength = message.length() - offset;
                    message = message.substring(offset, offset + Math.min(maxLength, restLength));
                    if (hasAlerted && message.length() == 0) {
                        // no need to do it again when there is no text content
                        break;
                    }
                    builder.write(characteristic, getAlertMessage(alert, message, 1));
                    hasAlerted = true;
                }
                if (!hasAlerted) {
                    builder.write(characteristic, getAlertMessage(alert, "", 1));
                }
            } catch (IOException ex) {
                // ain't gonna happen
                Log.e(TAG, "Error writing alert message to ByteArrayOutputStream");
            }
        } else {
            Log.w(TAG, "NEW_ALERT characteristic not available");
        }
    }

    public void newAlert(TransactionBuilder builder, NewAlert alert) {
        newAlert(builder, alert, OverflowStrategy.TRUNCATE);
    }

    protected byte[] getAlertMessage(NewAlert alert, String message, int chunk) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(100);
        stream.write(BLETypeConversions.fromUint8(alert.getCategory().getId()));
        stream.write(BLETypeConversions.fromUint8(alert.getNumAlerts()));
        if (alert.getCategory() == AlertCategory.CustomMiBand2) {
            stream.write(BLETypeConversions.fromUint8(alert.getCustomIcon()));
        }

        if (message.length() > 0) {
            stream.write(BLETypeConversions.toUtf8s(message));
        } else {
            // some write a null byte instead of leaving out this optional value
//                stream.write(new byte[] {0});
        }
        return stream.toByteArray();
    }
}
