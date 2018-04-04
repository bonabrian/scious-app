package id.bonabrian.scious.libraryservice.model;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public enum DeviceType {
    UNKNOWN(-1),
    MIBAND2(11);

    private final int key;

    DeviceType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public boolean isSupported() {
        return this != UNKNOWN;
    }

    public static DeviceType fromKey(int key) {
        for (DeviceType type : values()) {
            if (type.key == key) {
                return type;
            }
        }
        return DeviceType.UNKNOWN;
    }
}
