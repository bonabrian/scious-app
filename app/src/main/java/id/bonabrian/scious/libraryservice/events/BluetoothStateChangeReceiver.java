package id.bonabrian.scious.libraryservice.events;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.device.DeviceManager;
import id.bonabrian.scious.util.Prefs;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class BluetoothStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothStateChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                Intent refreshIntent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);

                Prefs prefs = SciousApplication.getPrefs();
                if (!prefs.getBoolean("general_auto_connect_on_bluetooth", false)) {
                    return;
                }
                Log.i(TAG, "Bluetooth turned on");
                SciousApplication.deviceService().connect();
            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                SciousApplication.deviceService().disconnect();
            }
        }
    }
}
