package id.bonabrian.scious.libraryservice.device.miband2;

import android.support.annotation.Nullable;

import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.commons.SimpleNotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface NotificationStrategy {
    void sendDefaultNotification(TransactionBuilder builder, SimpleNotification simpleNotification, BTLEAction extraAction);
    void sendCustomNotification(VibrationProfile vibrationProfile, @Nullable SimpleNotification simpleNotification, int flashTimes, int flashColour, int originalColour, long flashDuration, BTLEAction extraAction, TransactionBuilder builder);
    void stopCurrentNotification(TransactionBuilder builder);
}
