package id.bonabrian.scious.libraryservice.model;

import android.support.annotation.Nullable;

import id.bonabrian.scious.libraryservice.device.IEventHandler;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface IDeviceService extends IEventHandler {
    String PREFIX = "id.bonabrian.scious.device";

    String ACTION_MIBAND2_AUTH = PREFIX + ".action.miband2_auth";
    String ACTION_START = PREFIX + ".action.start";
    String ACTION_CONNECT = PREFIX + ".action.connect";
    String ACTION_NOTIFICATION = PREFIX + ".action.notification";
    String ACTION_DELETE_NOTIFICATION = PREFIX + ".action.delete_notification";
    String ACTION_SET_TIME = PREFIX + ".action.set_time";
    String ACTION_REQUEST_DEVICEINFO = PREFIX + ".action.request_deviceinfo";
    String ACTION_FETCH_ACTIVITY_DATA = PREFIX + ".action.fetch_activity_data";
    String ACTION_DISCONNECT = PREFIX + ".action.disconnect";
    String ACTION_ENABLE_REALTIME_STEPS = PREFIX + ".action.enable_realtime_steps";
    String ACTION_ENABLE_REALTIME_HEARTRATE_MEASUREMENT = PREFIX + ".action.realtime_heartrate_measurement";
    String ACTION_ENABLE_HEARTRATE_SLEEP_SUPPORT = PREFIX + ".action.enable_heartrate_sleep_support";
    String ACTION_HEARTRATE_MEASUREMENT = PREFIX + ".action.heartrate_measurement";
    String ACTION_SEND_CONFIGURATION = PREFIX + ".action.send_configuration";
    String ACTION_REALTIME_SAMPLES = PREFIX + ".action.realtime_samples";
    String ACTION_FIND_DEVICE = PREFIX + ".action.find_device";
    String ACTION_HEARTRATE = PREFIX + ".action.heartrate";

    String EXTRA_NOTIFICATION_FLAGS = "notification_flags";
    String EXTRA_NOTIFICATION_ID = "notification_id";
    String EXTRA_NOTIFICATION_TYPE = "notification_type";
    String EXTRA_FIND_START = "find_start";

    String EXTRA_VIBRATION_INTENSITY = "vibration_intensity";
    String EXTRA_URI = "uri";
    String EXTRA_CONFIG = "config";
    String EXTRA_CONNECT_FIRST_TIME = "connect_first_time";
    String EXTRA_BOOLEAN_ENABLE = "enable_realtime_steps";

    @Deprecated
    String EXTRA_REALTIME_STEPS = "realtime_steps";
    String EXTRA_REALTIME_SAMPLE = "realtime_sample";
    String EXTRA_TIMESTAMP = "timestamp";

    @Deprecated
    String EXTRA_HEART_RATE_VALUE = "heartrate_value";

    void start();

    void connect();

    void connect(@Nullable SciousDevice device);

    void connect(@Nullable SciousDevice device, boolean performPair);

    void disconnect();

    void quit();

    void requestDeviceInfo();
}
