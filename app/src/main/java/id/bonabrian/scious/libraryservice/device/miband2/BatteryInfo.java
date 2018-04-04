package id.bonabrian.scious.libraryservice.device.miband2;

import java.util.GregorianCalendar;

import id.bonabrian.scious.libraryservice.model.BatteryState;
import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class BatteryInfo extends AbsInfo {
    public static final byte DEVICE_BATTERY_NORMAL = 0;
    public static final byte DEVICE_BATTERY_LOW = 1;
    public static final byte DEVICE_BATTERY_CHARGING = 2;
    public static final byte DEVICE_BATTERY_CHARGING_FULL = 3;
    public static final byte DEVICE_BATTERY_CHARGE_OFF = 4;

    public BatteryInfo(byte[] data) {
        super(data);
    }

    public int getLevelInPercent() {
        if (mData.length >= 2) {
            return mData[1];
        }
        return 50; // actually unknown
    }

    public BatteryState getState() {
        if (mData.length >= 3) {
            int value = mData[2];
            switch (value) {
                case DEVICE_BATTERY_NORMAL:
                    return BatteryState.BATTERY_NORMAL;
                case DEVICE_BATTERY_CHARGING:
                    return BatteryState.BATTERY_CHARGING;
//                case DEVICE_BATTERY_CHARGING:
//                    return BatteryState.BATTERY_CHARGING;
//                case DEVICE_BATTERY_CHARGING_FULL:
//                    return BatteryState.BATTERY_CHARGING_FULL;
//                case DEVICE_BATTERY_CHARGE_OFF:
//                    return BatteryState.BATTERY_NOT_CHARGING_FULL;
            }
        }
        return BatteryState.UNKNOWN;
    }

    public int getLastChargeLevelInParcent() {
        if (mData.length >= 20) {
            return mData[19];
        }
        return 50; // actually unknown
    }

    public GregorianCalendar getLastChargeTime() {
        GregorianCalendar lastCharge = MiBand2DateConverter.createCalendar();

        if (mData.length >= 18) {
            lastCharge = BLETypeConversions.rawBytesToCalendar(new byte[]{
                    mData[10], mData[11], mData[12], mData[13], mData[14], mData[15], mData[16], mData[17]
            }, true);
        }

        return lastCharge;
    }

    public int getNumCharges() {
//        if (mData.length >= 10) {
//            return ((0xff & mData[7]) | ((0xff & mData[8]) << 8));
//
//        }
        return -1;
    }
}
