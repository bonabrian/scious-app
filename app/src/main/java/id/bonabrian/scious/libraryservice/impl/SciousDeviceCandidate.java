package id.bonabrian.scious.libraryservice.impl;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.model.DeviceType;
import id.bonabrian.scious.util.AndroidUtils;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousDeviceCandidate implements Parcelable {
    private static final String TAG = SciousDeviceCandidate.class.getSimpleName();

    private final BluetoothDevice device;
    private final short rssi;
    private final ParcelUuid[] serviceUuids;
    private DeviceType deviceType = DeviceType.UNKNOWN;

    public SciousDeviceCandidate(BluetoothDevice device, short rssi, ParcelUuid[] serviceUuids) {
        this.device = device;
        this.rssi = rssi;
        this.serviceUuids = mergeServiceUuids(serviceUuids, device.getUuids());
    }

    private SciousDeviceCandidate(Parcel in) {
        device = in.readParcelable(getClass().getClassLoader());
        if (device == null) {
            throw new IllegalStateException("Unable to read state from Parcel");
        }
        rssi = (short) in.readInt();
        deviceType = DeviceType.valueOf(in.readString());

        ParcelUuid[] uuids = AndroidUtils.toParcelUUids(in.readParcelableArray(getClass().getClassLoader()));
        serviceUuids = mergeServiceUuids(uuids, device.getUuids());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, 0);
        dest.writeInt(rssi);
        dest.writeString(deviceType.name());
        dest.writeParcelableArray(serviceUuids, 0);
    }

    public static final Creator<SciousDeviceCandidate> CREATOR = new Creator<SciousDeviceCandidate>() {
        @Override
        public SciousDeviceCandidate createFromParcel(Parcel in) {
            return new SciousDeviceCandidate(in);
        }

        @Override
        public SciousDeviceCandidate[] newArray(int size) {
            return new SciousDeviceCandidate[size];
        }
    };

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDeviceType(DeviceType type) {
        deviceType = type;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getMacAddress() {
        return device != null ? device.getAddress() : SciousApplication.getContext().getString(R.string.unknown);
    }

    private ParcelUuid[] mergeServiceUuids(ParcelUuid[] serviceUuds, ParcelUuid[] deviceUuids) {
        Set<ParcelUuid> uuids = new HashSet<>();
        if (serviceUuds != null) {
            uuids.addAll(Arrays.asList(serviceUuds));
        }
        if (deviceUuids != null) {
            uuids.addAll(Arrays.asList(deviceUuids));
        }
        return uuids.toArray(new ParcelUuid[0]);
    }

    @NonNull
    public ParcelUuid[] getServiceUuids() {
        return serviceUuids;
    }

    public boolean supportsService(UUID aService) {
        ParcelUuid[] uuids = getServiceUuids();
        if (uuids.length == 0) {
            Log.w(TAG, "No cached services available for " + this);
            return false;
        }

        for (ParcelUuid uuid : uuids) {
            if (uuid != null && aService.equals(uuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        String deviceName = null;
        try {
            Method method = device.getClass().getMethod("getAliasName");
            if (method != null) {
                deviceName = (String) method.invoke(device);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {
            Log.i(TAG, "Could not get device alias for " + device.getName());
        }
        if (deviceName == null || deviceName.length() == 0) {
            deviceName = device.getName();
        }
        if (deviceName == null || deviceName.length() == 0) {
            deviceName = "(unknown)";
        }
        return deviceName;
    }

    public short getRssi() {
        return rssi;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SciousDeviceCandidate that = (SciousDeviceCandidate) o;
        return device.getAddress().equals(that.device.getAddress());
    }

    @Override
    public int hashCode() {
        return device.getAddress().hashCode() ^ 37;
    }

    @Override
    public String toString() {
        return getName() + ": " + getMacAddress() + " (" + getDeviceType() + ")";
    }
}
