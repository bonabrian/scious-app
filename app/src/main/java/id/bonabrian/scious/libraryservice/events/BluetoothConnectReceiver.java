package id.bonabrian.scious.libraryservice.events;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.service.DeviceCommunicationService;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class BluetoothConnectReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothConnectReceiver.class.getSimpleName();
    final DeviceCommunicationService service;

    public BluetoothConnectReceiver(DeviceCommunicationService service) {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            return;
        }
        Log.i(TAG, "Got connection attempt");
        SciousDevice sciousDevice = service.getSciousDevice();
        if (sciousDevice != null) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getAddress().equals(sciousDevice.getAddress()) && sciousDevice.getState() == SciousDevice.State.WAITING_FOR_RECONNECT) {
                Log.i(TAG, "Will connect to " + sciousDevice.getName());
                SciousApplication.deviceService().connect();
            } else {
                Log.i(TAG, "Won't connect to " + device.getAddress() + "(" + device.getName() + ")");
            }
        }
    }
}
