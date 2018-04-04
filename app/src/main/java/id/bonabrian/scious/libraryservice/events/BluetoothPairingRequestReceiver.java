package id.bonabrian.scious.libraryservice.events;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.service.DeviceCommunicationService;
import id.bonabrian.scious.util.DeviceHelper;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class BluetoothPairingRequestReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothPairingRequestReceiver.class.getSimpleName();
    final DeviceCommunicationService service;

    public BluetoothPairingRequestReceiver(DeviceCommunicationService service) {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            return;
        }
        SciousDevice sciousDevice = service.getSciousDevice();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (sciousDevice == null || device == null) return;

        IDeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(sciousDevice);
        try {
            if (coordinator.getBondingStyle(sciousDevice) == IDeviceCoordinator.BONDING_STYLE_NONE) {
                Log.i(TAG, "Aborting unwanted pairing request");
                abortBroadcast();
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not abort pairing request process");
        }
    }
}

