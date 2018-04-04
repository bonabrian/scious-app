package id.bonabrian.scious.libraryservice.device;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.database.DBHandler;
import id.bonabrian.scious.database.DBHelper;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.util.DeviceHelper;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();

    public static final String BLUETOOTH_DEVICE_ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";

    public static final String ACTION_DEVICE_CHANGED = "id.bonabrian.scious.device.devicemanager.action.device_changed";

    public static final String ACTION_REFRESH_DEVICELIST = "id.bonabrian.scious.device.devicemanager.action.set_version";

    private final Context context;

    private final List<SciousDevice> deviceList = new ArrayList<>();
    private SciousDevice selectedDevice = null;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_REFRESH_DEVICELIST: // fall through
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    refreshPairedDevices();
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:
                case BLUETOOTH_DEVICE_ACTION_ALIAS_CHANGED:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String newName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    updateDeviceName(device, newName);
                    break;
                case SciousDevice.ACTION_DEVICE_CHANGED:
                    SciousDevice dev = intent.getParcelableExtra(SciousDevice.EXTRA_DEVICE);
                    if (dev.getAddress() != null) {
                        int index = deviceList.indexOf(dev); // search by address
                        if (index >= 0) {
                            deviceList.set(index, dev);
                        } else {
                            deviceList.add(dev);
                        }
                        if (dev.isInitialized()) {
                            try (DBHandler dbHandler = SciousApplication.acquireDB()) {
                                DBHelper.getDevice(dev, dbHandler.getDaoSession());
                            } catch (Exception ignore) {

                            }
                        }
                    }
                    updateSelectedDevice(dev);
                    refreshPairedDevices();
                    break;
            }
        }
    };

    public DeviceManager(Context context) {
        this.context = context;
        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(DeviceManager.ACTION_REFRESH_DEVICELIST);
        filterLocal.addAction(SciousDevice.ACTION_DEVICE_CHANGED);
        filterLocal.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filterLocal);

        IntentFilter filterGlobal = new IntentFilter();
        filterGlobal.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filterGlobal.addAction(BLUETOOTH_DEVICE_ACTION_ALIAS_CHANGED);
        filterGlobal.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mReceiver, filterGlobal);

        refreshPairedDevices();
    }

    private void updateDeviceName(BluetoothDevice device, String newName) {
        for (SciousDevice dev : deviceList) {
            if (device.getAddress().equals(dev.getAddress())) {
                if (!dev.getName().equals(newName)) {
                    dev.setName(newName);
                    notifyDevicesChanged();
                    return;
                }
            }
        }
    }

    private void updateSelectedDevice(SciousDevice dev) {
        if (selectedDevice == null) {
            selectedDevice = dev;
        } else {
            if (selectedDevice.equals(dev)) {
                selectedDevice = dev; // equality vs identity!
            } else {
                if (selectedDevice.isConnected() && dev.isConnected()) {
                    Log.w(TAG, "Multiple connected devices");
                    selectedDevice = dev; // use the last one that changed
                } else if (!selectedDevice.isConnected()) {
                    selectedDevice = dev; // use the last one that changed
                }
            }
        }
    }

    private void refreshPairedDevices() {
        Set<SciousDevice> availableDevices = DeviceHelper.getInstance().getAvailableDevices(context);
        deviceList.retainAll(availableDevices);
        for (SciousDevice availableDevice : availableDevices) {
            if (!deviceList.contains(availableDevice)) {
                deviceList.add(availableDevice);
            }
        }

        Collections.sort(deviceList, new Comparator<SciousDevice>() {
            @Override
            public int compare(SciousDevice lhs, SciousDevice rhs) {
                return Collator.getInstance().compare(lhs.getName(), rhs.getName());
            }
        });
        notifyDevicesChanged();
    }

    public List<SciousDevice> getDevices() {
        return Collections.unmodifiableList(deviceList);
    }

    private void notifyDevicesChanged() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DEVICE_CHANGED));
    }
}
