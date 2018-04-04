package id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public enum Command {
    EnableNewIncomingAlertNotification(0),
    EnableUnreadCategoryStatusNotification(1),
    DisableNewIncomingAlertNotification(2),
    DisbleUnreadCategoryStatusNotification(3),
    NotifyNewIncomingAlertImmediately(4),
    NotifyUnreadCategoryStatusImmediately(5),;
    // 6-255 reserved for future use

    private final int id;

    Command(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
