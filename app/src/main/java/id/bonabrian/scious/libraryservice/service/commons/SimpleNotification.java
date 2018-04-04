package id.bonabrian.scious.libraryservice.service.commons;

import id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification.AlertCategory;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SimpleNotification {
    private final String message;
    private final AlertCategory alertCategory;

    public SimpleNotification(String message, AlertCategory alertCategory) {
        this.message = message;
        this.alertCategory = alertCategory;
    }

    public AlertCategory getAlertCategory() {
        return alertCategory;
    }

    public String getMessage() {
        return message;
    }
}
