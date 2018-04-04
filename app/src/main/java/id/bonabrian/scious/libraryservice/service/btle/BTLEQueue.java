package id.bonabrian.scious.libraryservice.service.btle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDevice.State;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public final class BTLEQueue {
    private static final String TAG = BTLEQueue.class.getSimpleName();

    private final Object mGattMonitor = new Object();
    private final SciousDevice mSciousDevice;
    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private final BlockingQueue<Transaction> mTransactions = new LinkedBlockingQueue<>();
    private volatile boolean mDisposed;
    private volatile boolean mCrashed;
    private volatile boolean mAbortTransaction;

    private final Context mContext;
    private CountDownLatch mWaitForActionResultLatch;
    private CountDownLatch mConnectionLatch;
    private BluetoothGattCharacteristic mWaitCharacteristic;
    private final InternalGattCallback internalGattCallback;
    private boolean mAutoReconnect;

    private Thread dispatchThread = new Thread("Scious GATT Dispatcher") {
        @Override
        public void run() {
            Log.d(TAG, "Queue Dispatch Thread started.");

            while (!mDisposed && !mCrashed) {
                try {
                    Transaction transaction = mTransactions.take();
                    if (!isConnected()) {
                        Log.d(TAG, "Not connected, waiting for connection...");
                        internalGattCallback.reset();

                        mConnectionLatch = new CountDownLatch(1);
                        mConnectionLatch.await();
                        mConnectionLatch = null;
                    }

                    internalGattCallback.setTransactionGattCallback(transaction.getGattCallback());
                    mAbortTransaction = false;

                    for (BTLEAction action : transaction.getActions()) {
                        if (mAbortTransaction) {
                            Log.i(TAG, "Aborting running transaction");
                            break;
                        }
                        mWaitCharacteristic = action.getCharacteristic();
                        mWaitForActionResultLatch = new CountDownLatch(1);
                        if (action.run(mBluetoothGatt)) {
                            boolean waitForResult = action.expectsResult();
                            if (waitForResult) {
                                mWaitForActionResultLatch.await();
                                mWaitForActionResultLatch = null;
                                if (mAbortTransaction) {
                                    break;
                                }
                            }
                        } else {
                            Log.e(TAG, "Action returned false: " + action);
                            break;
                        }
                    }
                } catch (InterruptedException ignored) {
                    mConnectionLatch = null;
                    Log.d(TAG, "Thread interrupted");
                } catch (Throwable ex) {
                    Log.e(TAG, "Queue Dispatch Thread died: " + ex.getMessage(), ex);
                    mCrashed = true;
                    mConnectionLatch = null;
                } finally {
                    mWaitForActionResultLatch = null;
                    mWaitCharacteristic = null;
                }
            }
            Log.i(TAG, "Queue Dispatch Thread terminated.");
        }
    };

    public BTLEQueue(BluetoothAdapter bluetoothAdapter, SciousDevice sciousDevice, IGattCallback externalGattCallback, Context context) {
        mBluetoothAdapter = bluetoothAdapter;
        mSciousDevice = sciousDevice;
        internalGattCallback = new InternalGattCallback(externalGattCallback);
        mContext = context;

        dispatchThread.start();
    }

    public void setAutoReconnect(boolean enable) {
        mAutoReconnect = enable;
    }

    protected boolean isConnected() {
        return mSciousDevice.isConnected();
    }

    public boolean connect() {
        if (isConnected()) {
            Log.w(TAG, "Already connected");
            return false;
        }
        synchronized (mGattMonitor) {
            if (mBluetoothGatt != null) {
                Log.i(TAG, "connect() requested, disconnecting previous connection: " + mSciousDevice.getName());
                disconnect();
            }
        }
        Log.i(TAG, "Attempting to connect to " + mSciousDevice.getName());
        mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mSciousDevice.getAddress());
        synchronized (mGattMonitor) {
            mBluetoothGatt = remoteDevice.connectGatt(mContext, false, internalGattCallback);
        }
        boolean result = mBluetoothGatt != null;
        if (result) {
            setDeviceConnectionState(State.CONNECTING);
        }
        return result;
    }

    private void setDeviceConnectionState(State newState) {
        Log.d(TAG, "New device connection state: " + newState);
        mSciousDevice.setState(newState);
        mSciousDevice.sendDeviceUpdateIntent(mContext);
        if (mConnectionLatch != null && newState == State.CONNECTED) {
            mConnectionLatch.countDown();
        }
    }

    public void disconnect() {
        synchronized (mGattMonitor) {
            Log.d(TAG, "disconnect()");
            BluetoothGatt gatt = mBluetoothGatt;
            if (gatt != null) {
                mBluetoothGatt = null;
                Log.i(TAG, "Disconnecting BTLEQueue from GATT device");
                gatt.disconnect();
                gatt.close();
                setDeviceConnectionState(State.NOT_CONNECTED);
            }
        }
    }

    private void handleDisconnected(int status) {
        Log.d(TAG, "handleDisconnected: " + status);
        internalGattCallback.reset();
        mTransactions.clear();
        mAbortTransaction = true;
        if (mWaitForActionResultLatch != null) {
            mWaitForActionResultLatch.countDown();
        }
        boolean wasInitialized = mSciousDevice.isInitialized();
        setDeviceConnectionState(State.NOT_CONNECTED);

        if (mBluetoothGatt != null) {
            if (!wasInitialized || !maybeReconnect()) {
                disconnect();
            }
        }
    }

    private boolean maybeReconnect() {
        if (mAutoReconnect && mBluetoothGatt != null) {
            Log.i(TAG, "Enabling automatic BTLE reconnect");
            boolean result = mBluetoothGatt.connect();
            if (result) {
                setDeviceConnectionState(State.WAITING_FOR_RECONNECT);
            }
            return result;
        }
        return false;
    }

    public void dispose() {
        if (mDisposed) {
            return;
        }
        mDisposed = true;
        disconnect();
        dispatchThread.interrupt();
        dispatchThread = null;
    }

    public void add(Transaction transaction) {
        Log.d(TAG, "About to add: " + transaction);
        if (!transaction.isEmpty()) {
            mTransactions.add(transaction);
        }
    }

    public void insert(Transaction transaction) {
        Log.d(TAG, "About to insert: " + transaction);
        if (!transaction.isEmpty()) {
            List<Transaction> tail = new ArrayList<>(mTransactions.size() + 2);
            mTransactions.drainTo(tail);
            mTransactions.add(transaction);
            mTransactions.addAll(tail);
        }
    }

    public void clear() {
        mTransactions.clear();
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt is null => no services available.");
            return Collections.emptyList();
        }
        return mBluetoothGatt.getServices();
    }

    private boolean checkCorrectGattInstance(BluetoothGatt gatt, String where) {
        if (gatt != mBluetoothGatt && mBluetoothGatt != null) {
            Log.i(TAG, "Ignoring event from wrong BluetoothGatt instance: " + where + "; " + gatt);
            return false;
        }
        return true;
    }

    private final class InternalGattCallback extends BluetoothGattCallback {
        private
        @Nullable
        IGattCallback mTransactionGattCallback;
        private final IGattCallback mExternalGattCallback;

        public InternalGattCallback(IGattCallback externalGattCallback) {
            mExternalGattCallback = externalGattCallback;
        }

        public void setTransactionGattCallback(@Nullable IGattCallback callback) {
            mTransactionGattCallback = callback;
        }

        private IGattCallback getCallbackToUse() {
            if (mTransactionGattCallback != null) {
                return mTransactionGattCallback;
            }
            return mExternalGattCallback;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection state change, newState: " + newState + getStatusString(status));
            synchronized (mGattMonitor) {
                if (mBluetoothGatt == null) {
                    mBluetoothGatt = gatt;
                }
            }

            if (!checkCorrectGattInstance(gatt, "connection state event")) {
                return;
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "Connection state event with error status " + status);
            }

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "Connected to GATT server.");
                    setDeviceConnectionState(State.CONNECTED);
                    List<BluetoothGattService> cachedServices = gatt.getServices();
                    if (cachedServices != null && cachedServices.size() > 0) {
                        Log.i(TAG, "Using cached services, skipping discovery");
                        onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS);
                    } else {
                        Log.i(TAG, "Attempting to start service discovery:" +
                                gatt.discoverServices());
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Disconnected from GATT server");
                    handleDisconnected(status);
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i(TAG, "Connecting to GATT server");
                    setDeviceConnectionState(State.CONNECTING);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (!checkCorrectGattInstance(gatt, "services discovered: " + getStatusString(status))) {
                return;
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (getCallbackToUse() != null) {
                    // only propagate the successful event
                    getCallbackToUse().onServicesDiscovered(gatt);
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "characteristic write: " + characteristic.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "characteristic write")) {
                return;
            }
            if (getCallbackToUse() != null) {
                getCallbackToUse().onCharacteristicWrite(gatt, characteristic, status);
            }
            checkWaitingCharacteristic(characteristic, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "characteristic read: " + characteristic.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "characteristic read")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onCharacteristicRead(gatt, characteristic, status);
                } catch (Throwable ex) {
                    Log.e(TAG, "onCharacteristicRead: " + ex.getMessage());
                }
            }
            checkWaitingCharacteristic(characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "descriptor read: " + descriptor.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "descriptor read")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onDescriptorRead(gatt, descriptor, status);
                } catch (Throwable ex) {
                    Log.e(TAG, "onDescriptorRead: " + ex.getMessage(), ex);
                }
            }
            checkWaitingCharacteristic(descriptor.getCharacteristic(), status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "descriptor write: " + descriptor.getUuid() + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "descriptor write")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onDescriptorWrite(gatt, descriptor, status);
                } catch (Throwable ex) {
                    Log.e(TAG, "onDescriptorWrite: " + ex.getMessage(), ex);
                }
            }
            checkWaitingCharacteristic(descriptor.getCharacteristic(), status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String content = "";
            for (byte b : characteristic.getValue()) {
                content += String.format(" 0x%1x", b);
            }
            Log.d(TAG, "characteristic changed: " + characteristic.getUuid() + " value: " + content);
            if (!checkCorrectGattInstance(gatt, "characteristic changed")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onCharacteristicChanged(gatt, characteristic);
                } catch (Throwable ex) {
                    Log.e(TAG, "onCharaceristicChanged: " + ex.getMessage(), ex);
                }
            } else {
                Log.i(TAG, "No gattcallback registered, ignoring characteristic change");
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "remote rssi: " + rssi + getStatusString(status));
            if (!checkCorrectGattInstance(gatt, "remote rssi")) {
                return;
            }
            if (getCallbackToUse() != null) {
                try {
                    getCallbackToUse().onReadRemoteRssi(gatt, rssi, status);
                } catch (Throwable ex) {
                    Log.e(TAG, "onReadRemoteRssi: " + ex.getMessage(), ex);
                }
            }
        }

        private void checkWaitingCharacteristic(BluetoothGattCharacteristic characteristic, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Failed btle action, aborting transaction: " + characteristic.getUuid() + getStatusString(status));
                mAbortTransaction = true;
            }
            if (characteristic != null && BTLEQueue.this.mWaitCharacteristic != null && characteristic.getUuid().equals(BTLEQueue.this.mWaitCharacteristic.getUuid())) {
                if (mWaitForActionResultLatch != null) {
                    mWaitForActionResultLatch.countDown();
                }
            } else {
                if (BTLEQueue.this.mWaitCharacteristic != null) {
                    Log.e(TAG, "checkWaitingCharacteristic: mismatched characteristic received: " + ((characteristic != null && characteristic.getUuid() != null) ? characteristic.getUuid().toString() : "(null)"));
                }
            }
        }

        private String getStatusString(int status) {
            return status == BluetoothGatt.GATT_SUCCESS ? " (success)" : " (failed: " + status + ")";
        }

        public void reset() {
            mTransactionGattCallback = null;
        }
    }
}
