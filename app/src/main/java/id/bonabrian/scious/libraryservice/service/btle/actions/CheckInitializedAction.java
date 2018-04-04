package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.util.Log;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class CheckInitializedAction extends AbortTransactionAction {

    private static final String TAG = CheckInitializedAction.class.getSimpleName();
    private final SciousDevice device;

    public CheckInitializedAction(SciousDevice sciousDevice) {
        device = sciousDevice;
    }

    @Override
    protected boolean shouldAbort() {
        boolean abort = device.isInitialized();
        if (abort) {
            Log.i(TAG, "Aborting device initialization, because already initialized: " + device);
        }
        return abort;
    }
}
