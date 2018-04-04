package id.bonabrian.scious.libraryservice.events;

import java.util.GregorianCalendar;

import id.bonabrian.scious.libraryservice.model.BatteryState;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousDeviceEventBatteryInfo extends SciousDeviceEvent {
    public GregorianCalendar lastChargeTime = null;
    public BatteryState state = BatteryState.UNKNOWN;
    public short level = 50;
    public int numCharges = -1;

    public boolean extendedInfoAvailable() {
        if (numCharges != -1 && lastChargeTime != null) {
            return true;
        }
        return false;
    }
}
