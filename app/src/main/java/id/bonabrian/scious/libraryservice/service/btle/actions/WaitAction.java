package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class WaitAction extends PlainAction {
    private final int mMillis;

    public WaitAction(int millis) {
        mMillis = millis;
    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        try {
            Thread.sleep(mMillis);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
