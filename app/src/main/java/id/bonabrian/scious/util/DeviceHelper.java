package id.bonabrian.scious.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.database.DBHandler;
import id.bonabrian.scious.database.DBHelper;
import id.bonabrian.scious.libraryservice.device.IDeviceCoordinator;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Coordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.libraryservice.model.DeviceType;
import id.bonabrian.scious.source.dao.Device;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceHelper {
    private static final String TAG = DeviceHelper.class.getSimpleName();
    private static final DeviceHelper instance = new DeviceHelper();

    public static DeviceHelper getInstance() {
        return instance;
    }

    private List<IDeviceCoordinator> coordinators;

    public DeviceType getSupportedType(SciousDeviceCandidate candidate) {
        for (IDeviceCoordinator coordinator : getAllCoordinators()) {
            DeviceType deviceType = coordinator.getSupportedType(candidate);
            if (deviceType.isSupported()) {
                return deviceType;
            }
        }
        return DeviceType.UNKNOWN;
    }

    public boolean getSupportedType(SciousDevice device) {
        for (IDeviceCoordinator coordinator : getAllCoordinators()) {
            if (coordinator.supports(device)) {
                return true;
            }
        }
        return false;
    }

    public SciousDevice findAvailableDevice(String deviceAddress, Context context) {
        Set<SciousDevice> availableDevices = getAvailableDevices(context);
        for (SciousDevice availableDevice : availableDevices) {
            if (deviceAddress.equals(availableDevice.getAddress())) {
                return availableDevice;
            }
        }
        return null;
    }

    public Set<SciousDevice> getAvailableDevices(Context context) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<SciousDevice> availableDevices = new LinkedHashSet<SciousDevice>();

        if (btAdapter == null) {
            Toast.makeText(context, context.getString(R.string.bluetooth_is_not_supported), Toast.LENGTH_SHORT).show();
        } else if (!btAdapter.isEnabled()) {
            Toast.makeText(context, context.getString(R.string.bluetooth_is_disabled), Toast.LENGTH_SHORT).show();
        }
        List<SciousDevice> dbDevices = getDatabaseDevices();
        availableDevices.addAll(dbDevices);
        if (btAdapter != null) {
            List<SciousDevice> bondedDevices = getBondedDevices(btAdapter);
            availableDevices.addAll(bondedDevices);
        }

        Prefs prefs = SciousApplication.getPrefs();
        String miAddr = prefs.getString(MiBand2Const.PREF_MIBAND2_ADDRESS, "");
        if (miAddr.length() > 0) {
            SciousDevice miDevice = new SciousDevice(miAddr, "MI", DeviceType.MIBAND2);
            availableDevices.add(miDevice);
        }
        return availableDevices;
    }

    public SciousDevice toSupportedDevice(BluetoothDevice device) {
        SciousDeviceCandidate candidate = new SciousDeviceCandidate(device, SciousDevice.RSSI_UNKNOWN, device.getUuids());
        return toSupportedDevice(candidate);
    }

    public SciousDevice toSupportedDevice(SciousDeviceCandidate candidate) {
        for (IDeviceCoordinator coordinator : getAllCoordinators()) {
            if (coordinator.supports(candidate)) {
                return coordinator.createDevice(candidate);
            }
        }
        return null;
    }

    public IDeviceCoordinator getCoordinator(SciousDeviceCandidate device) {
        synchronized (this) {
            for (IDeviceCoordinator coord : getAllCoordinators()) {
                if (coord.supports(device)) {
                    return coord;
                }
            }
        }
        return null;
    }

    public IDeviceCoordinator getCoordinator(SciousDevice device) {
        synchronized (this) {
            for (IDeviceCoordinator coord : getAllCoordinators()) {
                if (coord.supports(device)) {
                    return coord;
                }
            }
        }
        return null;
    }

    public synchronized List<IDeviceCoordinator> getAllCoordinators() {
        if (coordinators == null) {
            coordinators = createCoordinators();
        }
        return coordinators;
    }

    private List<IDeviceCoordinator> createCoordinators() {
        List<IDeviceCoordinator> result = new ArrayList<>();
        result.add(new MiBand2Coordinator());

        return result;
    }

    private List<SciousDevice> getDatabaseDevices() {
        List<SciousDevice> result = new ArrayList<>();
        try (DBHandler lockHandler = SciousApplication.acquireDB()) {
            List<Device> activeDevices = DBHelper.getActiveDevices(lockHandler.getDaoSession());
            for (Device dbDevice : activeDevices) {
                SciousDevice sciousDevice = toSciousDevice(dbDevice);
                if (sciousDevice != null && DeviceHelper.getInstance().getSupportedType(sciousDevice)) {
                    result.add(sciousDevice);
                }
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving devices from database", e);
            return Collections.emptyList();
        }
    }

    public SciousDevice toSciousDevice(Device dbDevice) {
        DeviceType deviceType = DeviceType.fromKey(dbDevice.getType());
        SciousDevice sciousDevice = new SciousDevice(dbDevice.getIdentifier(), dbDevice.getName(), deviceType);
        sciousDevice.setModel(dbDevice.getModel());
        return sciousDevice;
    }

    private List<SciousDevice> getBondedDevices(BluetoothAdapter btAdapter) {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        List<SciousDevice> result = new ArrayList<>(pairedDevices.size());
        DeviceHelper deviceHelper = DeviceHelper.getInstance();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            SciousDevice device = deviceHelper.toSupportedDevice(pairedDevice);
            if (device != null) {
                result.add(device);
            }
        }
        return result;
    }

    public boolean removeBond(SciousDevice device) throws Exception {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            BluetoothDevice remoteDevice = defaultAdapter.getRemoteDevice(device.getAddress());
            if (remoteDevice != null) {
                try {
                    Method method = BluetoothDevice.class.getMethod("removeBond", (Class[]) null);
                    Object result = method.invoke(remoteDevice, (Object[]) null);
                    return Boolean.TRUE.equals(result);
                } catch (Exception e) {
                    throw new Exception("Error removing bond to device: " + device, e);
                }
            }
        }
        return false;
    }
}
