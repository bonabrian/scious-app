package id.bonabrian.scious.libraryservice.service.btle.actions;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SetDeviceBusyAction extends PlainAction {

    private final SciousDevice device;
    private final Context context;
    private final String busyTask;

    public SetDeviceBusyAction(SciousDevice device, String busyTask, Context context) {
        this.device = device;
        this.busyTask = busyTask;
        this.context = context;
    }

    @Override
    public boolean run(BluetoothGatt gatt) {
        device.setBusyTask(busyTask);
        device.sendDeviceUpdateIntent(context);
        return true;
    }

    @Override
    public String toString() {
        return getCreationTime() + ": " + getClass().getName() + ": " + busyTask;
    }
}
