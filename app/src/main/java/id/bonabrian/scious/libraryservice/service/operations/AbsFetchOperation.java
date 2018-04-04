package id.bonabrian.scious.libraryservice.service.operations;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.SharedPreferences;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Service;
import id.bonabrian.scious.libraryservice.device.miband2.MiBand2Support;
import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.btle.actions.SetDeviceBusyAction;
import id.bonabrian.scious.util.ArrayUtils;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public abstract class AbsFetchOperation extends AbsBaseOperation {
    private static final String TAG = AbsFetchOperation.class.getSimpleName();

    protected byte lastPacketCounter;
    protected int fetchCount;
    protected BluetoothGattCharacteristic characteristicActivityData;
    protected BluetoothGattCharacteristic characteristicFetch;
    protected Calendar startTimestamp;

    public AbsFetchOperation(MiBand2Support support) {
        super(support);
    }

    @Override
    protected void enableNeededNotifications(TransactionBuilder builder, boolean enable) {
        if (!enable) {
            builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_5_ACTIVITY_DATA), enable);
        }
    }

    @Override
    protected void doPerform() throws IOException {
        startFetching();
    }

    protected void startFetching() throws IOException {
        lastPacketCounter = -1;
        TransactionBuilder builder = performInitialized("Fetching activity data");
        getSupport().setLowLatency(builder);
        if (fetchCount == 0) {
            builder.add(new SetDeviceBusyAction(getDevice(), getContext().getString(R.string.busy_task_fetch_activity_data), getContext()));
        }
        fetchCount++;

        characteristicActivityData = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_5_ACTIVITY_DATA);
        builder.notify(characteristicActivityData, false);

        characteristicFetch = getCharacteristic(MiBand2Service.UUID_UNKNOWN_CHARACTERISTIC4);
        builder.notify(characteristicFetch, true);

        startFetching(builder);
        builder.queue(getQueue());
    }

    protected abstract void startFetching(TransactionBuilder builder);

    protected abstract String getLastSyncTimeKey();

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID characteristicUUID = characteristic.getUuid();
        if (MiBand2Service.UUID_CHARACTERISTIC_5_ACTIVITY_DATA.equals(characteristicUUID)) {
            handleActivityNotif(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_UNKNOWN_CHARACTERISTIC4.equals(characteristicUUID)) {
            handleActivityMetadata(characteristic.getValue());
            return true;
        } else {
            return super.onCharacteristicChanged(gatt, characteristic);
        }
    }

    @CallSuper
    protected void handleActivityFetchFinish() {
        operationFinished();
        unsetBusy();
    }

    protected abstract void handleActivityNotif(byte[] value);

    protected abstract void bufferActivityData(byte[] value);

    protected void handleActivityMetadata(byte[] value) {
        if (value.length == 15) {
            if (ArrayUtils.equals(value, MiBand2Service.RESPONSE_ACTIVITY_DATA_START_DATE_SUCCESS, 0)) {
                Calendar startTimestamp = getSupport().fromTimeBytes(Arrays.copyOfRange(value, 7, value.length));
                setStartTimestamp(startTimestamp);
                // TODO
            } else {
                Log.w(TAG, "Unexpected activity metadata");
                handleActivityFetchFinish();
            }
        } else if (value.length == 3) {
            if (Arrays.equals(MiBand2Service.RESPONSE_FINISH_SUCCESS, value)) {
                handleActivityFetchFinish();
            } else {
                Log.w(TAG, "Unexpected activity metadata");
                handleActivityFetchFinish();
            }
        } else {
            handleActivityFetchFinish();
        }
    }

    protected void setStartTimestamp(Calendar startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    protected void saveLastSyncTimestamp(@NonNull GregorianCalendar timestamp) {
        SharedPreferences.Editor editor = SciousApplication.getPrefs().getPreferences().edit();
        editor.putLong(getLastSyncTimeKey(), timestamp.getTimeInMillis());
        editor.apply();
    }

    protected GregorianCalendar getLastSuccessfulSyncTime() {
        long timeStampMillis = SciousApplication.getPrefs().getLong(getLastSyncTimeKey(), 0);
        if (timeStampMillis != 0) {
            GregorianCalendar calendar = BLETypeConversions.createCalendar();
            calendar.setTimeInMillis(timeStampMillis);
            return calendar;
        }
        GregorianCalendar calendar = BLETypeConversions.createCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -10);
        return calendar;
    }
}
