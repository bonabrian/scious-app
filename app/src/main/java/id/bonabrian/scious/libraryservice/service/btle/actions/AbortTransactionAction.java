package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;
import android.util.Log;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbortTransactionAction extends PlainAction {
    private static final String TAG = AbortTransactionAction.class.getSimpleName();

    public AbortTransactionAction() {

    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        if (shouldAbort()) {
            Log.i(TAG, "Aborting transaction because abort criteria met");
            return false;
        }
        return true;
    }

    protected abstract boolean shouldAbort();

    @Override
    public String toString() {
        return getCreationTime() + ": " + getClass().getSimpleName() + ": aborting? " + shouldAbort();
    }
}
