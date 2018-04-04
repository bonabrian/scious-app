package id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class NewAlert {
    private final AlertCategory category;
    private final int numAlerts;
    private final String message;
    private byte customIcon = -1;

    public NewAlert(AlertCategory category, int /*uint8*/ numAlerts, String /*utf8s*/ message) {
        this.category = category;
        this.numAlerts = numAlerts;
        this.message = message;
    }

    public NewAlert(AlertCategory category, int /*uint8*/ numAlerts, String /*utf8s*/ message, byte customIcon) {
        this.category = category;
        this.numAlerts = numAlerts;
        this.message = message;
        this.customIcon = customIcon;
    }

    public AlertCategory getCategory() {
        return category;
    }

    public int getNumAlerts() {
        return numAlerts;
    }

    public String getMessage() {
        return message;
    }

    public byte getCustomIcon() {
        return customIcon;
    }
}
