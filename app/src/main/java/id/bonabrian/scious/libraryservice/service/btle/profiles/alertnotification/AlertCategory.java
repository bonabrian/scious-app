package id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

import java.util.ArrayList;
import java.util.List;

/**
 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.alert_category_id.xml
 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.alert_category_id_bit_mask.xml
 */

public enum AlertCategory {
    Simple(0),
    Email(1),
    News(2),
    IncomingCall(3),
    MissedCall(4),
    SMS(5),
    VoiceMail(6),
    Schedule(7),
    HighPriorityAlert(8),
    InstantMessage(9),
    Any(255),
    Custom(-1),
    CustomMiBand2(-6);

    private final int id;

    AlertCategory(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private int realBitNumber() {
        // the ID corresponds to the bit for the bitset
        return id;
    }

    private int bitNumberPerByte() {
        // the ID corresponds to the bit for the bitset (per byte)
        return realBitNumber() % 8;
    }

    private int asBit() {
        return 1 << bitNumberPerByte();
    }

    private int byteNumber() {
        return id <= 7 ? 0 : 1;
    }

    /**
     * Converts the given categories to an array of bytes.
     * @param categories
     * @return
     */
    public static byte[] toBitmask(AlertCategory... categories) {
        byte[] result = new byte[2];

        for (AlertCategory category : categories) {
            result[category.byteNumber()] |= category.asBit();
        }
        return result;
    }

    //    SupportedNewAlertCategory
    public static AlertCategory[] fromBitMask(byte[] bytes) {
        List<AlertCategory> result = new ArrayList<>();
        byte b = bytes[0];

        return null;
    }
}
