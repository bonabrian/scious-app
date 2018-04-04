package id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public enum AlertLevel {

    NoAlert(0),
    MildAlert(1),
    HighAlert(2);
    // 3-255 reserved

    private final int id;

    AlertLevel(int id) {
        this.id = id;
    }

    /**
     * The alert level ID
     * To be used as uint8 value
     * @return
     */
    public int getId() {
        return id;
    }

}
