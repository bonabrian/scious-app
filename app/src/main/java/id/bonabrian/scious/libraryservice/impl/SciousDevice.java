package id.bonabrian.scious.libraryservice.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.model.BatteryState;
import id.bonabrian.scious.libraryservice.model.DeviceType;
import id.bonabrian.scious.libraryservice.model.GenericItem;
import id.bonabrian.scious.libraryservice.model.IDeviceInfo;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousDevice implements Parcelable {
    public static final String ACTION_DEVICE_CHANGED = "id.bonabrian.scious.device.action.device_changed";
    public static final Creator<SciousDevice> CREATOR = new Creator<SciousDevice>() {
        @Override
        public SciousDevice createFromParcel(Parcel source) {
            return new SciousDevice(source);
        }

        @Override
        public SciousDevice[] newArray(int size) {
            return new SciousDevice[size];
        }
    };
    private static final String TAG = SciousDevice.class.getSimpleName();

    public static final short RSSI_UNKNOWN = 0;
    public static final short BATTERY_UNKNOWN = -1;
    private static final short BATTERY_THRESHOLD_PERCENT = 10;
    public static final String EXTRA_DEVICE = "device";
    private static final String DEVICEINFO_HW_VER = "HW: ";
    private static final String DEVICEINFO_FW_VER = "FW: ";
    private static final String DEVICEINFO_HR_VER = "HR: ";
    private static final String DEVICEINFO_ADDR = "ADDR: ";

    private String mName;
    private final String mAddress;
    private final DeviceType mDeviceType;
    private String mFirmwareVersion;
    private String mModel;
    private State mState = State.NOT_CONNECTED;
    private short mBatteryLevel = BATTERY_UNKNOWN;
    private short mBatteryThresholdPercent = BATTERY_THRESHOLD_PERCENT;
    private BatteryState mBatteryState;
    private short mRssi = RSSI_UNKNOWN;
    private String mBusyTask;
    private List<IDeviceInfo> mDeviceInfos;

    public SciousDevice(String address, DeviceType deviceType) {
        this(address, null, deviceType);
    }

    public SciousDevice(String address, String name, DeviceType deviceType) {
        mAddress = address;
        mName = (name != null) ? name : mAddress;
        mDeviceType = deviceType;

        validate();
    }

    private SciousDevice(Parcel in) {
        mName = in.readString();
        mAddress = in.readString();
        mDeviceType = DeviceType.values()[in.readInt()];
        mFirmwareVersion = in.readString();
        mModel = in.readString();
        mState = State.values()[in.readInt()];
        mBatteryLevel = (short) in.readInt();
        mBatteryThresholdPercent = (short) in.readInt();
        mBatteryState = (BatteryState) in.readSerializable();
        mRssi = (short) in.readInt();
        mBusyTask = in.readString();
        mDeviceInfos = in.readArrayList(getClass().getClassLoader());

        validate();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeInt(mDeviceType.ordinal());
        dest.writeString(mFirmwareVersion);
        dest.writeString(mModel);
        dest.writeInt(mState.ordinal());
        dest.writeInt(mBatteryLevel);
        dest.writeInt(mBatteryThresholdPercent);
        dest.writeSerializable(mBatteryState);
        dest.writeInt(mRssi);
        dest.writeString(mBusyTask);
        dest.writeList(mDeviceInfos);
    }

    private void validate() {
        if (getAddress() == null) {
            throw new IllegalArgumentException("Address must not be null");
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        if (name == null) {
            Log.w(TAG, "Ignoring setting of Device name to null for " + this);
            return;
        }
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        mFirmwareVersion = firmwareVersion;
    }

    @Nullable
    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public boolean isConnected() {
        return mState.ordinal() >= State.CONNECTED.ordinal();
    }

    public boolean isInitializing() {
        return mState == State.INITIALIZING;
    }

    public boolean isInitialized() {
        return mState.ordinal() >= State.INITIALIZED.ordinal();
    }

    public boolean isConnecting() {
        return mState == State.CONNECTING;
    }

    public boolean isBusy() {
        return mBusyTask != null;
    }

    public String getBusyTask() {
        return mBusyTask;
    }

    public void setBusyTask(String task) {
        if (task == null) {
            throw new IllegalArgumentException("busy task must not be null");
        }
        if (mBusyTask != null) {
            Log.w(TAG, "Attempt to mark device as busy with: " + task + ", but is already busy with: " + mBusyTask);
        }
        Log.i(TAG, "Mark device as busy: " + task);
        mBusyTask = task;
    }

    public void unsetBusyTask() {
        if (mBusyTask == null) {
            Log.e(TAG, "Attempt to mark device as not busy anymore, but was not busy before.");
            return;
        }
        Log.i(TAG, "Mark device as not busy anymore: " + mBusyTask);
        mBusyTask = null;
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
        if (state.ordinal() <= State.CONNECTED.ordinal()) {
            unsetDynamicState();
        }
    }

    private void unsetDynamicState() {
        setBatteryLevel(BATTERY_UNKNOWN);
        setBatteryState(BatteryState.UNKNOWN);
        setFirmwareVersion(null);
        setRssi(RSSI_UNKNOWN);
        if (mBusyTask != null) {
            unsetBusyTask();
        }
    }

    public String getStateString() {
        return getStateString(true);
    }

    private String getStateString(boolean simple) {
        switch (mState) {
            case NOT_CONNECTED:
                return SciousApplication.getContext().getString(R.string.not_connected);
            case WAITING_FOR_RECONNECT:
                return SciousApplication.getContext().getString(R.string.waiting_for_connect);
            case CONNECTING:
                return SciousApplication.getContext().getString(R.string.connecting);
            case CONNECTED:
                if (simple) {
                    return SciousApplication.getContext().getString(R.string.connecting);
                }
                return SciousApplication.getContext().getString(R.string.connected);
            case INITIALIZING:
                if (simple) {
                    return SciousApplication.getContext().getString(R.string.connecting);
                }
                return SciousApplication.getContext().getString(R.string.initializing);
            case AUTHENTICATION_REQUIRED:
                return SciousApplication.getContext().getString(R.string.authentication_required);
            case AUTHENTICATING:
                return SciousApplication.getContext().getString(R.string.authenticating);
            case INITIALIZED:
                if (simple) {
                    return SciousApplication.getContext().getString(R.string.connected);
                }
                return SciousApplication.getContext().getString(R.string.initialized);
        }
        return SciousApplication.getContext().getString(R.string.unknown_state);
    }

    @NonNull
    public DeviceType getType() {
        return mDeviceType;
    }

    public void setRssi(short rssi) {
        if (rssi < 0) {
            Log.w(TAG, "Illegal RSSI value " + rssi + ", setting to RSSI_UNKNOWN");
            mRssi = RSSI_UNKNOWN;
        } else {
            mRssi = rssi;
        }
    }

    public short getRssi() {
        return mRssi;
    }

    public void sendDeviceUpdateIntent(Context context) {
        Intent intent = new Intent(ACTION_DEVICE_CHANGED);
        intent.putExtra(EXTRA_DEVICE, this);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SciousDevice)) {
            return false;
        }
        if (((SciousDevice) obj).getAddress().equals(this.mAddress)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mAddress.hashCode() ^ 37;
    }

    public short getBatteryLevel() {
        return mBatteryLevel;
    }

    public void setBatteryLevel(short batteryLevel) {
        if ((batteryLevel >= 0 && batteryLevel <= 100) || batteryLevel == BATTERY_UNKNOWN) {
            mBatteryLevel = batteryLevel;
        } else {
            Log.e(TAG, "Battery level musts be within range 0-100: " + batteryLevel);
        }
    }

    public BatteryState getBatteryState() {
        return mBatteryState;
    }

    public void setBatteryState(BatteryState mBatteryState) {
        this.mBatteryState = mBatteryState;
    }

    public short getBatteryThresholdPercent() {
        return mBatteryThresholdPercent;
    }

    public void setBatteryThresholdPercent(short batteryThresholdPercent) {
        this.mBatteryThresholdPercent = batteryThresholdPercent;
    }

    @Override
    public String toString() {
        return "Device " + getName() + ", " + getAddress() + ", " + getStateString(false);
    }

    @NonNull
    public String getShortAddress() {
        String address = getAddress();
        if (address != null) {
            if (address.length() > 5) {
                return address.substring(address.length() - 5);
            }
            return address;
        }
        return "";
    }

    public boolean hasDeviceInfos() {
        return getDeviceInfos().size() > 0;
    }

    public IDeviceInfo getDeviceInfo(String name) {
        for (IDeviceInfo item : getDeviceInfos()) {
            if (name.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }

    public List<IDeviceInfo> getDeviceInfos() {
        List<IDeviceInfo> result = new ArrayList<>();
        if (mDeviceInfos != null) {
            result.addAll(mDeviceInfos);
        }
        if (mModel != null) {
            result.add(new GenericItem(DEVICEINFO_HW_VER, mModel));
        }
        if (mFirmwareVersion != null) {
            result.add(new GenericItem(DEVICEINFO_FW_VER, mFirmwareVersion));
        }
        if (mAddress != null) {
            result.add(new GenericItem(DEVICEINFO_ADDR, mAddress));
        }
        Collections.sort(result);
        return result;
    }

    public void setDeviceInfos(List<IDeviceInfo> deviceInfos) {
        this.mDeviceInfos = deviceInfos;
    }

    public void addDeviceInfo(IDeviceInfo info) {
        if (mDeviceInfos == null) {
            mDeviceInfos = new ArrayList<>();
        } else {
            int index = mDeviceInfos.indexOf(info);
            if (index >= 0) {
                mDeviceInfos.set(index, info); // replace item with new one
                return;
            }
        }
        mDeviceInfos.add(info);
    }

    public boolean removeDeviceInfo(IDeviceInfo info) {
        if (mDeviceInfos == null) {
            return false;
        }
        return mDeviceInfos.remove(info);
    }

    public enum State {
        // Note: the order is important!
        NOT_CONNECTED,
        WAITING_FOR_RECONNECT,
        CONNECTING,
        CONNECTED,
        INITIALIZING,
        AUTHENTICATION_REQUIRED,
        AUTHENTICATING,
        INITIALIZED,
    }
}
