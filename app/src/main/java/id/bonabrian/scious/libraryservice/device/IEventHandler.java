package id.bonabrian.scious.libraryservice.device;

import id.bonabrian.scious.libraryservice.model.NotificationSpec;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IEventHandler {
    void onNotification(NotificationSpec notificationSpec);

    void onDeleteNotification(int id);

    void onSetTime();

    void onEnableRealtimeSteps(boolean enable);

    void onFetchActivityData();

    void onHeartRateTest();

    void onEnableRealtimeHeartRateMeasurement(boolean enable);

    void onFindDevice(boolean start);

    void onEnableHeartRateSleepSupport(boolean enable);

    void onSendConfiguration(String config);
}
