package id.bonabrian.scious.libraryservice.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.util.EnumSet;

import id.bonabrian.scious.R;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Support;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class DeviceSupportFactory {
    private final BluetoothAdapter mBtAdapter;
    private final Context mContext;

    public DeviceSupportFactory(Context context) {
        mContext = context;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized IDeviceSupport createDeviceSupport(SciousDevice device) throws Exception {
        IDeviceSupport deviceSupport = null;
        String deviceAddress = device.getAddress();
        int indexFirstColon = deviceAddress.indexOf(":");
        if (indexFirstColon > 0) {
            deviceSupport = createBTDeviceSupport(device);
        } else {
            // no colon at all, maybe a class name?
            deviceSupport = createClassNameDeviceSupport(device);
        }

        if (deviceSupport != null) {
            return deviceSupport;
        }

        // no device found, check transport availability and warn
        checkBtAvailability();
        return null;
    }

    private IDeviceSupport createClassNameDeviceSupport(SciousDevice device) throws Exception {
        String className = device.getAddress();
        try {
            Class<?> deviceSupportClass = Class.forName(className);
            Constructor<?> constructor = deviceSupportClass.getConstructor();
            IDeviceSupport support = (IDeviceSupport) constructor.newInstance();
            // has to create the device itself
            support.setContext(device, null, mContext);
            return support;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new Exception("Error creating DeviceSupport instance for " + className, e);
        }
    }

    private void checkBtAvailability() {
        if (mBtAdapter == null) {
            Toast.makeText(mContext, mContext.getString(R.string.bluetooth_is_not_supported), Toast.LENGTH_SHORT).show();
        } else if (!mBtAdapter.isEnabled()) {
            Toast.makeText(mContext, mContext.getString(R.string.bluetooth_is_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    private IDeviceSupport createBTDeviceSupport(SciousDevice sciousDevice) throws Exception {
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            IDeviceSupport deviceSupport = null;
            try {
                switch (sciousDevice.getType()) {
                    case MIBAND2:
                        deviceSupport = new ServiceDeviceSupport(new MiBand2Support(), EnumSet.of(ServiceDeviceSupport.Flags.THROTTLING, ServiceDeviceSupport.Flags.BUSY_CHECKING));
                        break;
                }
                if (deviceSupport != null) {
                    deviceSupport.setContext(sciousDevice, mBtAdapter, mContext);
                    return deviceSupport;
                }
            } catch (Exception e) {
                throw new Exception(mContext.getString(R.string.cannot_connect), e);
            }
        }
        return null;
    }
}
