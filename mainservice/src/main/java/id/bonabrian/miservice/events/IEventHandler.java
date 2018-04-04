package id.bonabrian.miservice.events;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IEventHandler {
    void onEnableRealtimeSteps(boolean enable);
    void onHeartRateMeasurement();
    void onEnableRealtimeHeartRateMeasurement(boolean enable);
    void onFindDevice(boolean find);
    void onSendConfiguration(String config);
}
