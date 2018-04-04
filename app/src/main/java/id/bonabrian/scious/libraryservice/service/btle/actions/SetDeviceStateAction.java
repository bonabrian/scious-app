package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SetDeviceStateAction extends PlainAction {

    private final SciousDevice device;
    private final SciousDevice.State deviceState;
    private final Context context;

    public SetDeviceStateAction(SciousDevice device, SciousDevice.State deviceState, Context context) {
        this.device = device;
        this.deviceState = deviceState;
        this.context = context;
    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        device.setState(deviceState);
        device.sendDeviceUpdateIntent(getContext());
        return true;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        return super.toString() + " to " + deviceState;
    }
}
