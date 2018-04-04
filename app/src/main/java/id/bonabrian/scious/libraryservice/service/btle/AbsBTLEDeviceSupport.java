package id.bonabrian.scious.libraryservice.service.btle;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.service.AbsDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.actions.CheckInitializedAction;
import id.bonabrian.scious.libraryservice.service.btle.profiles.AbsBTLEProfile;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsBTLEDeviceSupport extends AbsDeviceSupport implements IGattCallback {
    private static final String TAG = AbsBTLEDeviceSupport.class.getSimpleName();
    private BTLEQueue mQueue;
    private Map<UUID, BluetoothGattCharacteristic> mAvailableCharacteristics;
    private final Set<UUID> mSupportedServices = new HashSet<>(4);
    private Logger logger;

    private final List<AbsBTLEProfile<?>> mSupportedProfiles = new ArrayList<>();
    public static final String BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb"; //this is common for all BTLE devices. see http://stackoverflow.com/questions/18699251/finding-out-android-bluetooth-le-gatt-profiles
    private final Object characteristicsMonitor = new Object();

    public AbsBTLEDeviceSupport(Logger logger) {
        this.logger = logger;
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null");
        }
    }

    @Override
    public boolean connect() {
        if (mQueue == null) {
            mQueue = new BTLEQueue(getBluetoothAdapter(), getDevice(), this, getContext());
            mQueue.setAutoReconnect(getAutoReconnect());
        }
        return mQueue.connect();
    }

    @Override
    public void setAutoReconnect(boolean enable) {
        super.setAutoReconnect(enable);
        if (mQueue != null) {
            mQueue.setAutoReconnect(enable);
        }
    }

    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        return builder;
    }

    @Override
    public void dispose() {
        if (mQueue != null) {
            mQueue.dispose();
            mQueue = null;
        }
    }

    public TransactionBuilder createTransactionBuilder(String taskName) {
        return new TransactionBuilder(taskName);
    }

    public TransactionBuilder performInitialized(String taskName) throws IOException {
        if (!isConnected()) {
            if (!connect()) {
                throw new IOException("1: Unable to connect to device: " + getDevice());
            }
        }

        if (!isInitialized()) {
            TransactionBuilder builder = createTransactionBuilder("Initialize device");
            builder.add(new CheckInitializedAction(sciousDevice));
            initializeDevice(builder).queue(getQueue());
        }
        return createTransactionBuilder(taskName);
    }

    public void performImmediately(TransactionBuilder builder) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to device: " + getDevice());
        }
        getQueue().insert(builder.getTransaction());
    }

    public BTLEQueue getQueue() {
        return mQueue;
    }

    protected void addSupportedService(UUID aSupportedService) {
        mSupportedServices.add(aSupportedService);
    }

    protected void addSupportedProfile(AbsBTLEProfile<?> profile) {
        mSupportedProfiles.add(profile);
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        synchronized (characteristicsMonitor) {
            if (mAvailableCharacteristics == null) {
                return null;
            }
            return mAvailableCharacteristics.get(uuid);
        }
    }

    private void gattServicesDiscovered(List<BluetoothGattService> discoveredGattServices) {
        if (discoveredGattServices == null) {
            Log.w(TAG, "No gatt services discovered: null");
            return;
        }
        Set<UUID> supportedServices = getSupportedServices();
        Map<UUID, BluetoothGattCharacteristic> newCharacteristics = new HashMap<>();
        for (BluetoothGattService service : discoveredGattServices) {
            if (supportedServices.contains(service.getUuid())) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                if (characteristics == null || characteristics.isEmpty()) {
                    Log.w(TAG, "Supported LE service " + service.getUuid() + "did not return any characteristics");
                    continue;
                }
                HashMap<UUID, BluetoothGattCharacteristic> intmAvailableCharacteristics = new HashMap<>(characteristics.size());
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    intmAvailableCharacteristics.put(characteristic.getUuid(), characteristic);
                }
                newCharacteristics.putAll(intmAvailableCharacteristics);

                synchronized (characteristicsMonitor) {
                    mAvailableCharacteristics = newCharacteristics;
                }
            } else {
                Log.d(TAG, "Discovered unsupported service");
            }
        }
    }

    protected Set<UUID> getSupportedServices() {
        return mSupportedServices;
    }

    public void logMessageContent(byte[] value) {
        logger.info("RECEIVED DATA WITH LENGTH: " + ((value != null) ? value.length : "(null)"));
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            profile.onConnectionStateChange(gatt, status, newState);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        gattServicesDiscovered(gatt.getServices());

        if (getDevice().getState().compareTo(SciousDevice.State.INITIALIZING) >= 0) {
            Log.w(TAG, "Services discovered, but device state is already " + getDevice().getState() + " for device: " + getDevice());
            return;
        }
        initializeDevice(createTransactionBuilder("Initializing device")).queue(getQueue());
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            if (profile.onCharacteristicRead(gatt, characteristic, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            if (profile.onCharacteristicWrite(gatt, characteristic, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            if (profile.onDescriptorRead(gatt, descriptor, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            if (profile.onDescriptorWrite(gatt, descriptor, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            if (profile.onCharacteristicChanged(gatt, characteristic)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        for (AbsBTLEProfile profile : mSupportedProfiles) {
            profile.onReadRemoteRssi(gatt, rssi, status);
        }
    }
}
