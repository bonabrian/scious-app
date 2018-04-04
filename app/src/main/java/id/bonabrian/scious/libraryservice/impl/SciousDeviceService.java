package id.bonabrian.scious.libraryservice.impl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import id.bonabrian.scious.libraryservice.model.IDeviceService;
import id.bonabrian.scious.libraryservice.model.NotificationSpec;
import id.bonabrian.scious.libraryservice.service.DeviceCommunicationService;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousDeviceService implements IDeviceService {
    protected final Context mContext;
    private final Class<? extends Service> mServiceClass;

    public SciousDeviceService(Context context) {
        mContext = context;
        mServiceClass = DeviceCommunicationService.class;
    }

    protected Intent createIntent() {
        return new Intent(mContext, mServiceClass);
    }

    protected void invokeService(Intent intent) {
        mContext.startService(intent);
    }

    protected void stopService(Intent intent) {
        mContext.stopService(intent);
    }

    @Override
    public void start() {
        Intent intent = createIntent().setAction(ACTION_START);
        invokeService(intent);
    }

    @Override
    public void connect() {
        connect(null, false);
    }

    @Override
    public void connect(@Nullable SciousDevice device) {
        connect(device, false);
    }

    @Override
    public void connect(@Nullable SciousDevice device, boolean firstTime) {
        Intent intent = createIntent().setAction(ACTION_CONNECT)
                .putExtra(SciousDevice.EXTRA_DEVICE, device)
                .putExtra(EXTRA_CONNECT_FIRST_TIME, firstTime);
        invokeService(intent);
    }

    @Override
    public void disconnect() {
        Intent intent = createIntent().setAction(ACTION_DISCONNECT);
        invokeService(intent);
    }

    @Override
    public void quit() {
        Intent intent = createIntent();
        stopService(intent);
    }

    @Override
    public void requestDeviceInfo() {
        Intent intent = createIntent().setAction(ACTION_REQUEST_DEVICEINFO);
        invokeService(intent);
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        Intent intent = createIntent().setAction(ACTION_NOTIFICATION)
                .putExtra(EXTRA_NOTIFICATION_FLAGS, notificationSpec.flags)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationSpec.id)
                .putExtra(EXTRA_NOTIFICATION_TYPE, notificationSpec.type);
        invokeService(intent);
    }

    @Override
    public void onDeleteNotification(int id) {
        Intent intent = createIntent().setAction(ACTION_DELETE_NOTIFICATION)
                .putExtra(EXTRA_NOTIFICATION_ID, id);
        invokeService(intent);
    }

    @Override
    public void onSetTime() {
        Intent intent = createIntent().setAction(ACTION_SET_TIME);
        invokeService(intent);
    }

    @Override
    public void onFetchActivityData() {
        Intent intent = createIntent().setAction(ACTION_FETCH_ACTIVITY_DATA);
        invokeService(intent);
    }

    @Override
    public void onHeartRateTest() {
        Intent intent = createIntent().setAction(ACTION_HEARTRATE);
        invokeService(intent);
    }

    @Override
    public void onFindDevice(boolean start) {
        Intent intent = createIntent().setAction(ACTION_FIND_DEVICE).putExtra(EXTRA_FIND_START, start);
        invokeService(intent);
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        Intent intent = createIntent().setAction(ACTION_ENABLE_REALTIME_STEPS)
                .putExtra(EXTRA_BOOLEAN_ENABLE, enable);
        invokeService(intent);
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        Intent intent = createIntent().setAction(ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT)
                .putExtra(EXTRA_BOOLEAN_ENABLE, enable);
        invokeService(intent);
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        Intent intent = createIntent().setAction(ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT)
                .putExtra(EXTRA_BOOLEAN_ENABLE, enable);
        invokeService(intent);
    }

    @Override
    public void onSendConfiguration(String config) {
        Intent intent = createIntent().setAction(ACTION_SEND_CONFIGURATION)
                .putExtra(EXTRA_CONFIG, config);
        invokeService(intent);
    }
}
