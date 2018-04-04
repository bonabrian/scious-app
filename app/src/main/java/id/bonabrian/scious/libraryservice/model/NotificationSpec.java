package id.bonabrian.scious.libraryservice.model;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class NotificationSpec {
    // TODO
    public static final int FLAG_WEARABLE_REPLY = 0x00000001;

    public int flags;
    public int id;
    public NotificationType type;
    public String sourceName;

    public String sourceAppId;
}
