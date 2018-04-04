package id.bonabrian.scious.libraryservice.device.miband2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.events.CalendarEvents;
import id.bonabrian.scious.libraryservice.events.SciousDeviceEventBatteryInfo;
import id.bonabrian.scious.libraryservice.events.SciousDeviceEventVersionInfo;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.model.ActivityUser;
import id.bonabrian.scious.libraryservice.model.IDeviceService;
import id.bonabrian.scious.libraryservice.model.NotificationSpec;
import id.bonabrian.scious.libraryservice.model.NotificationType;
import id.bonabrian.scious.libraryservice.service.RealtimeSamplesSupport;
import id.bonabrian.scious.libraryservice.service.btle.AbsBTLEDeviceSupport;
import id.bonabrian.scious.libraryservice.service.btle.BLETypeConversions;
import id.bonabrian.scious.libraryservice.service.btle.BTLEAction;
import id.bonabrian.scious.libraryservice.service.btle.GattCharacteristic;
import id.bonabrian.scious.libraryservice.service.btle.GattService;
import id.bonabrian.scious.libraryservice.service.btle.TransactionBuilder;
import id.bonabrian.scious.libraryservice.service.btle.actions.AbortTransactionAction;
import id.bonabrian.scious.libraryservice.service.btle.actions.SetDeviceStateAction;
import id.bonabrian.scious.libraryservice.service.btle.profiles.alertnotification.AlertCategory;
import id.bonabrian.scious.libraryservice.service.btle.profiles.deviceinfo.DeviceInfo;
import id.bonabrian.scious.libraryservice.service.btle.profiles.deviceinfo.DeviceInfoProfile;
import id.bonabrian.scious.libraryservice.service.btle.profiles.heartrate.HeartRateProfile;
import id.bonabrian.scious.libraryservice.service.commons.SimpleNotification;
import id.bonabrian.scious.libraryservice.service.operations.FetchActivityDataOperation;
import id.bonabrian.scious.libraryservice.service.operations.InitOperation;
import id.bonabrian.scious.util.NotificationUtils;
import id.bonabrian.scious.util.Prefs;
import id.bonabrian.scious.util.Version;

import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_FLASH_COLOUR;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_FLASH_COUNT;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_FLASH_DURATION;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_FLASH_ORIGINAL_COLOUR;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_VIBRATION_COUNT;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_VIBRATION_DURATION;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_VIBRATION_PAUSE;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.DEFAULT_VALUE_VIBRATION_PROFILE;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.FLASH_COLOUR;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.FLASH_COUNT;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.FLASH_DURATION;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.FLASH_ORIGINAL_COLOUR;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.VIBRATION_COUNT;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.VIBRATION_DURATION;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.VIBRATION_PAUSE;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.VIBRATION_PROFILE;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.getNotificationPrefIntValue;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.getNotificationPrefStringValue;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MiBand2Support extends AbsBTLEDeviceSupport {

    private static int currentButtonActionId = 0;
    private static int currentButtonPressCount = 0;
    private static long currentButtonPressTime = 0;
    private static long currentButtonTimerActivationTime = 0;

    private static final String TAG = MiBand2Support.class.getSimpleName();
    private final DeviceInfoProfile<MiBand2Support> deviceInfoProfile;
    private final HeartRateProfile<MiBand2Support> heartRateProfile;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DeviceInfoProfile.ACTION_DEVICE_INFO)) {
                handleDeviceInfo((DeviceInfo) intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
            }
        }
    };

    private boolean needsAuth;
    private volatile boolean telephoneRinging;
    private volatile boolean isLocatingDevice;

    private final SciousDeviceEventVersionInfo versionCmd = new SciousDeviceEventVersionInfo();
    private final SciousDeviceEventBatteryInfo batteryCmd = new SciousDeviceEventBatteryInfo();
    private RealtimeSamplesSupport realtimeSamplesSupport;
    private boolean alarmClockRinging;

    private static final Logger LOG = Logger.getLogger(String.valueOf(MiBand2Support.class));

    public MiBand2Support() {
        this(LOG);
    }

    public MiBand2Support(Logger logger) {
        super(logger);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_HEART_RATE);
        addSupportedService(GattService.UUID_SERVICE_IMMEDIATE_ALERT);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_ALERT_NOTIFICATION);

        addSupportedService(MiBand2Service.UUID_SERVICE_MIBAND_SERVICE);
        addSupportedService(MiBand2Service.UUID_SERVICE_MIBAND2_SERVICE);
        addSupportedService(MiBand2Service.UUID_SERVICE_FIRMWARE_SERVICE);

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        addSupportedProfile(deviceInfoProfile);
        heartRateProfile = new HeartRateProfile<>(this);
        addSupportedProfile(heartRateProfile);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceInfoProfile.ACTION_DEVICE_INFO);
        intentFilter.addAction(IDeviceService.ACTION_MIBAND2_AUTH);
        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void dispose() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        broadcastManager.unregisterReceiver(mReceiver);
        super.dispose();
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        try {
            boolean authenticate = needsAuth;
            needsAuth = false;
            new InitOperation(authenticate, this, builder).perform();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Initializing Mi Band 2 failed", Toast.LENGTH_SHORT).show();
        }
        return builder;
    }

    public byte[] getTimeBytes(Calendar calendar, TimeUnit precision) {
        byte[] bytes;
        if (precision == TimeUnit.MINUTES) {
            bytes = BLETypeConversions.shortCalendarToRawBytes(calendar, true);
        } else if (precision == TimeUnit.SECONDS) {
            bytes = BLETypeConversions.calendarToRawBytes(calendar, true);
        } else {
            throw new IllegalArgumentException("Unsupported precision, only MINUTES and SECONDS are supported till now");
        }
        byte[] tail = new byte[] { 0, BLETypeConversions.mapTimeZone(calendar.getTimeZone()) }; // 0 = adjust reason bitflags? or DST offset?? , timezone
//        byte[] tail = new byte[] { 0x2 }; // reason
        byte[] all = BLETypeConversions.join(bytes, tail);
        return all;
    }

    public Calendar fromTimeBytes(byte[] bytes) {
        GregorianCalendar timestamp = BLETypeConversions.rawBytesToCalendar(bytes, true);
        return timestamp;
    }

    public MiBand2Support setCurrentTimeWithService(TransactionBuilder builder) {
        GregorianCalendar now = BLETypeConversions.createCalendar();
        byte[] bytes = getTimeBytes(now, TimeUnit.SECONDS);
        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_CURRENT_TIME), bytes);
        return this;
    }

    public MiBand2Support setLowLatency(TransactionBuilder builder) {
        // TODO: low latency?
        return this;
    }

    public MiBand2Support setHighLatency(TransactionBuilder builder) {
        // TODO: high latency?
        return this;
    }

    public void setInitialized(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), SciousDevice.State.INITIALIZED, getContext()));
    }

    public MiBand2Support enableNotifications(TransactionBuilder builder, boolean enable) {
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_NOTIFICATION), enable);
        builder.notify(getCharacteristic(GattService.UUID_SERVICE_CURRENT_TIME), enable);
        // Notify CHARACTERISTIC9 to receive random auth code
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_AUTH), enable);
        return this;
    }

    public MiBand2Support enableFurtherNotifications(TransactionBuilder builder, boolean enable) {
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), enable);
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO), enable);
        builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_10_BUTTON), enable);
        BluetoothGattCharacteristic heartrateCharacteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT);
        if (heartrateCharacteristic != null) {
            builder.notify(heartrateCharacteristic, enable);
        }
        return this;
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    public boolean connectFirstTime() {
        needsAuth = true;
        return super.connect();
    }

    private MiBand2Support sendDefaultNotification(TransactionBuilder builder, SimpleNotification simpleNotification, short repeat, BTLEAction extraAction) {
        Log.i(TAG, "Sending notification to MiBand: (" + repeat + " times)");
        NotificationStrategy strategy = getNotificationStrategy();
        for (short i = 0; i < repeat; i++) {
            strategy.sendDefaultNotification(builder, simpleNotification, extraAction);
        }
        return this;
    }

    private MiBand2Support sendCustomNotification(VibrationProfile vibrationProfile, SimpleNotification simpleNotification, int flashTimes, int flashColour, int originalColour, long flashDuration, BTLEAction extraAction, TransactionBuilder builder) {
        getNotificationStrategy().sendCustomNotification(vibrationProfile, simpleNotification, flashTimes, flashColour, originalColour, flashDuration, extraAction, builder);
        Log.i(TAG, "Sending notification to MiBand");
        return this;
    }

    public NotificationStrategy getNotificationStrategy() {
        String firmwareVersion = getDevice().getFirmwareVersion();
        if (firmwareVersion != null) {
            Version ver = new Version(firmwareVersion);
            if (MiBand2Const.MI2_FW_VERSION_MIN_TEXT_NOTIFICATIONS.compareTo(ver) > 0) {
                return new MiBand2NotificationStrategy(this);
            }
        }
        if (SciousApplication.getPrefs().getBoolean(MiBand2Const.PREF_MIBAND2_ENABLE_TEXT_NOTIFICATIONS, true)) {
            return new MiBand2TextNotificationStrategy(this);
        }
        return new MiBand2NotificationStrategy(this);
    }

    private static final byte[] startHeartMeasurementManual = new byte[]{0x15, MiBand2Service.COMMAND_SET_HEARTRATE_MANUAL, 1};
    private static final byte[] stopHeartMeasurementManual = new byte[]{0x15, MiBand2Service.COMMAND_SET_HEARTRATE_MANUAL, 0};
    private static final byte[] startHeartMeasurementContinuous = new byte[]{0x15, MiBand2Service.COMMAND_SET_HEARTRATE_CONTINUOUS, 1};
    private static final byte[] stopHeartMeasurementContinuous = new byte[]{0x15, MiBand2Service.COMMAND_SET_HEARTRATE_CONTINUOUS, 0};

    private MiBand2Support requestBatteryInfo(TransactionBuilder builder) {
        Log.d(TAG, "Requesting Battery Info!");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO);
        builder.read(characteristic);
        return this;
    }

    public MiBand2Support requestDeviceInfo(TransactionBuilder builder) {
        Log.d(TAG, "Requesting Device Info!");
        deviceInfoProfile.requestDeviceInfo(builder);
        return this;
    }

    private MiBand2Support setFitnessGoal(TransactionBuilder transaction) {
        Log.i(TAG, "Attempting to set Fitness Goal...");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_8_USER_SETTINGS);
        if (characteristic != null) {
            int fitnessGoal = SciousApplication.getPrefs().getInt(ActivityUser.PREF_USER_STEPS_GOAL, 10000);
            byte[] bytes = ArrayUtils.addAll(
                    MiBand2Service.COMMAND_SET_FITNESS_GOAL_START,
                    BLETypeConversions.fromUint16(fitnessGoal));
            bytes = ArrayUtils.addAll(bytes,
                    MiBand2Service.COMMAND_SET_FITNESS_GOAL_END);
            transaction.write(characteristic, bytes);
        } else {
            LOG.info("Unable to set Fitness Goal");
        }
        return this;
    }

    private MiBand2Support setUserInfo(TransactionBuilder transaction) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_8_USER_SETTINGS);
        if (characteristic != null) {
            return this;
        }
        Log.i(TAG, "Attempting to set user info...");
        Prefs prefs = SciousApplication.getPrefs();
        String alias = prefs.getString(MiBand2Const.PREF_USER_ALIAS, null);
        String email = prefs.getString(MiBand2Const.PREF_USER_EMAIL, null);
        ActivityUser activityUser = new ActivityUser();
        int height = activityUser.getHeight();
        int weight = activityUser.getWeight();
        int birth_year = activityUser.getYearOfBirth();
        byte birth_month = 7; // not in user attributes
        byte birth_day = 1; // not in user attributes

        if (alias == null || email == null || weight == 0 || height == 0 || birth_year == 0) {
            Log.w(TAG, "Unable to set user info, make sure it is set up");
            return this;
        }

        byte sex = 0;
        switch (activityUser.getGender()) {
            case ActivityUser.GENDER_MALE:
                sex = 0;
                break;
            case ActivityUser.GENDER_FEMALE:
                sex = 1;
        }
        int userid = alias.hashCode(); // hash from alias like mi1

        byte bytes[] = new byte[]{
                MiBand2Service.COMMAND_SET_USERINFO,
                0,
                0,
                (byte) (birth_year & 0xff),
                (byte) ((birth_year >> 8) & 0xff),
                birth_month,
                birth_day,
                sex,
                (byte) (height & 0xff),
                (byte) ((height >> 8) & 0xff),
                (byte) ((weight * 200) & 0xff),
                (byte) (((weight * 200) >> 8) & 0xff),
                (byte) (userid & 0xff),
                (byte) ((userid >> 8) & 0xff),
                (byte) ((userid >> 16) & 0xff),
                (byte) ((userid >> 24) & 0xff)
        };

        transaction.write(characteristic, bytes);
        return this;
    }

    private MiBand2Support setWearLocation(TransactionBuilder builder) {
        LOG.info("Attempting to set wear location...");
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_8_USER_SETTINGS);
        if (characteristic != null) {
            builder.notify(characteristic, true);
            int location = MiBand2Coordinator.getWearLocation(getDevice().getAddress());
            switch (location) {
                case 0: // left hand
                    builder.write(characteristic, MiBand2Service.WEAR_LOCATION_LEFT_WRIST);
                    break;
                case 1: // right hand
                    builder.write(characteristic, MiBand2Service.WEAR_LOCATION_RIGHT_WRIST);
                    break;
            }
            builder.notify(characteristic, false); // TODO: this should actually be in some kind of finally-block in the queue. It should also be sent asynchronously after the notifications have completely arrived and processed.
        }
        return this;
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized("enable heart rate sleep support: " + enable);
            setHeartrateSleepSupport(builder);
            builder.queue(getQueue());
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error toggling heart rate sleep support: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private MiBand2Support setHeartrateSleepSupport(TransactionBuilder builder) {
        BluetoothGattCharacteristic characteristicHRControlPoint = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
        final boolean enableHrSleepSupport = MiBand2Coordinator.getHeartrateSleepSupport(getDevice().getAddress());
        if (characteristicHRControlPoint != null) {
            builder.notify(characteristicHRControlPoint, true);
            if (enableHrSleepSupport) {
                Log.i(TAG, "Enabling heartrate sleep support...");
                builder.write(characteristicHRControlPoint, MiBand2Service.COMMAND_ENABLE_HEARTRATE_SLEEP_MEASUREMENT);
            } else {
                Log.i(TAG, "Disabling heartrate sleep support...");
                builder.write(characteristicHRControlPoint, MiBand2Service.COMMAND_DISABLE_HEARTRATE_SLEEP_MEASUREMENT);
            }
            builder.notify(characteristicHRControlPoint, false);
        }
        return this;
    }

    private void performDefaultNotification(String task, SimpleNotification simpleNotification, short repeat, BTLEAction extraAction) {
        try {
            TransactionBuilder builder = performInitialized(task);
            sendDefaultNotification(builder, simpleNotification, repeat, extraAction);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.e(TAG, "Unable to send notification to MI device", ex);
        }
    }

    protected void performPreferredNotification(String task, String notificationOrigin, SimpleNotification simpleNotification, int alertLevel, BTLEAction extraAction) {
        try {
            TransactionBuilder builder = performInitialized(task);
            Prefs prefs = SciousApplication.getPrefs();
            int vibrateDuration = getPreferredVibrateDuration(notificationOrigin, prefs);
            int vibratePause = getPreferredVibratePause(notificationOrigin, prefs);
            short vibrateTimes = getPreferredVibrateCount(notificationOrigin, prefs);
            VibrationProfile profile = getPreferredVibrateProfile(notificationOrigin, prefs, vibrateTimes);
            profile.setAlertLevel(alertLevel);

            int flashTimes = getPreferredFlashCount(notificationOrigin, prefs);
            int flashColour = getPreferredFlashColour(notificationOrigin, prefs);
            int originalColour = getPreferredOriginalColour(notificationOrigin, prefs);
            int flashDuration = getPreferredFlashDuration(notificationOrigin, prefs);

            sendCustomNotification(profile, simpleNotification, flashTimes, flashColour, originalColour, flashDuration, extraAction, builder);

            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.e(TAG, "Unable to send notification to MI device", ex);
        }
    }

    private int getPreferredFlashDuration(String notificationOrigin, Prefs prefs) {
        return getNotificationPrefIntValue(FLASH_DURATION, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_DURATION);
    }

    private int getPreferredOriginalColour(String notificationOrigin, Prefs prefs) {
        return getNotificationPrefIntValue(FLASH_ORIGINAL_COLOUR, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_ORIGINAL_COLOUR);
    }

    private int getPreferredFlashColour(String notificationOrigin, Prefs prefs) {
        return getNotificationPrefIntValue(FLASH_COLOUR, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_COLOUR);
    }

    private int getPreferredFlashCount(String notificationOrigin, Prefs prefs) {
        return getNotificationPrefIntValue(FLASH_COUNT, notificationOrigin, prefs, DEFAULT_VALUE_FLASH_COUNT);
    }

    private int getPreferredVibratePause(String notificationOrigin, Prefs prefs) {
        return getNotificationPrefIntValue(VIBRATION_PAUSE, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_PAUSE);
    }

    private short getPreferredVibrateCount(String notificationOrigin, Prefs prefs) {
        return (short) Math.min(Short.MAX_VALUE, getNotificationPrefIntValue(VIBRATION_COUNT, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_COUNT));
    }

    private int getPreferredVibrateDuration(String notificationOrigin, Prefs prefs) {
        return getNotificationPrefIntValue(VIBRATION_DURATION, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_DURATION);
    }

    private VibrationProfile getPreferredVibrateProfile(String notificationOrigin, Prefs prefs, short repeat) {
        String profileId = getNotificationPrefStringValue(VIBRATION_PROFILE, notificationOrigin, prefs, DEFAULT_VALUE_VIBRATION_PROFILE);
        return VibrationProfile.getProfile(profileId, repeat);
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        int alertLevel = MiBand2Service.ALERT_LEVEL_MESSAGE;
        if (notificationSpec.type == NotificationType.UNKNOWN) {
            alertLevel = MiBand2Service.ALERT_LEVEL_VIBRATE_ONLY;
        }
        String message = NotificationUtils.getPreferredTextFor(notificationSpec, 40, 40, getContext()).trim();
        String origin = notificationSpec.type.getGenericType();
        SimpleNotification simpleNotification = new SimpleNotification(message, BLETypeConversions.toAlertCategory(notificationSpec.type));
        performPreferredNotification(origin + " received", origin, simpleNotification, alertLevel, null);
    }

    @Override
    public void onDeleteNotification(int id) {
        alarmClockRinging = false;
    }

    @Override
    public void onSetTime() {
        try {
            TransactionBuilder builder = performInitialized("Set date and time");
            setCurrentTimeWithService(builder);
            sendCalendarEvents(builder);
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.e(TAG, "Unable to set time on Mi Band 2", ex);
        }
    }

    private boolean isAlarmClockRinging() {
        // don't synchronize, this is not really important
        return alarmClockRinging;
    }

    private boolean isTelephoneRinging() {
        // don't synchronize, this is not really important
        return telephoneRinging;
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized("Enable realtime heart rate measurement");
            if (enable) {
                builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementManual);
                builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), startHeartMeasurementContinuous);
            } else {
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementContinuous);
            }
            builder.queue(getQueue());
            enableRealtimeSamplesTimer(enable);
        } catch (IOException ex) {
            Log.e(TAG, "Unable to enable realtime heart rate measurement", ex);
        }
    }

    @Override
    public void onFindDevice(boolean start) {
        isLocatingDevice = start;

        if (start) {
            AbortTransactionAction abortTransactionAction = new AbortTransactionAction() {
                @Override
                protected boolean shouldAbort() {
                    return !isLocatingDevice;
                }
            };
            SimpleNotification simpleNotification = new SimpleNotification(getContext().getString(R.string.stop_vibrate), AlertCategory.HighPriorityAlert);
            performDefaultNotification("locating device", simpleNotification, (short) 255, abortTransactionAction);
        }
    }

    @Override
    public void onFetchActivityData() {
        try {
            new FetchActivityDataOperation(this).perform();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to fetch Mi Band 2 activity data", ex);
        }
    }

    @Override
    public void onHeartRateTest() {
        try {
            TransactionBuilder builder = performInitialized("HeartRate");
            builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementContinuous);
            builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), stopHeartMeasurementManual);
            builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT), startHeartMeasurementManual);
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read heartrate", ex);
        }
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        try {
            TransactionBuilder builder = performInitialized(enable ? "Enabling realtime steps notifications" : "Disabling realtime steps notifications");
            if (enable) {
                builder.read(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS));
            }
            builder.notify(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS), enable);
            builder.queue(getQueue());
            enableRealtimeSamplesTimer(enable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to change realtime steps notification to: " + enable, e);
        }
    }

    private byte[] getHighLatency() {
        int minConnectionInterval = 460;
        int maxConnectionInterval = 500;
        int latency = 0;
        int timeout = 500;
        int advertisementInterval = 0;

        return getLatency(minConnectionInterval, maxConnectionInterval, latency, timeout, advertisementInterval);
    }

    private byte[] getLatency(int minConnectionInterval, int maxConnectionInterval, int latency, int timeout, int advertisementInterval) {
        byte result[] = new byte[12];
        result[0] = (byte) (minConnectionInterval & 0xff);
        result[1] = (byte) (0xff & minConnectionInterval >> 8);
        result[2] = (byte) (maxConnectionInterval & 0xff);
        result[3] = (byte) (0xff & maxConnectionInterval >> 8);
        result[4] = (byte) (latency & 0xff);
        result[5] = (byte) (0xff & latency >> 8);
        result[6] = (byte) (timeout & 0xff);
        result[7] = (byte) (0xff & timeout >> 8);
        result[8] = 0;
        result[9] = 0;
        result[10] = (byte) (advertisementInterval & 0xff);
        result[11] = (byte) (0xff & advertisementInterval >> 8);

        return result;
    }

    private byte[] getLowLatency() {
        int minConnectionInterval = 39;
        int maxConnectionInterval = 49;
        int latency = 0;
        int timeout = 500;
        int advertisementInterval = 0;

        return getLatency(minConnectionInterval, maxConnectionInterval, latency, timeout, advertisementInterval);
    }

    public void runButtonAction() {
        Prefs prefs = SciousApplication.getPrefs();

        if (currentButtonTimerActivationTime != currentButtonPressTime) {
            return;
        }

        String requiredButtonPressMessage = prefs.getString(MiBand2Const.PREF_MIBAND2_BUTTON_PRESS_BROADCAST,
                this.getContext().getString(R.string.mi2_prefs_button_press_broadcast_default_value));

        Intent in = new Intent();
        in.setAction(requiredButtonPressMessage);
        in.putExtra("button_id", currentButtonActionId);
        LOG.info("Sending " + requiredButtonPressMessage + " with button_id " + currentButtonActionId);
        this.getContext().getApplicationContext().sendBroadcast(in);
        if (prefs.getBoolean(MiBand2Const.PREF_MIBAND2_BUTTON_ACTION_VIBRATE, false)) {
            performPreferredNotification(null, null, null, MiBand2Service.ALERT_LEVEL_VIBRATE_ONLY, null);
        }

        currentButtonActionId = 0;

        currentButtonPressCount = 0;
        currentButtonPressTime = System.currentTimeMillis();
    }

    public void handleButtonPressed(byte[] value) {
        LOG.info("Button pressed");
        ///logMessageContent(value);

        // If disabled we return from function immediately
        Prefs prefs = SciousApplication.getPrefs();
        if (!prefs.getBoolean(MiBand2Const.PREF_MIBAND2_BUTTON_ACTION_ENABLE, false)) {
            return;
        }

        int buttonPressMaxDelay = prefs.getInt(MiBand2Const.PREF_MIBAND2_BUTTON_PRESS_MAX_DELAY, 2000);
        int buttonActionDelay = prefs.getInt(MiBand2Const.PREF_MIBAND2_BUTTON_ACTION_DELAY, 0);
        int requiredButtonPressCount = prefs.getInt(MiBand2Const.PREF_MIBAND2_BUTTON_PRESS_COUNT, 0);

        if (requiredButtonPressCount > 0) {
            long timeSinceLastPress = System.currentTimeMillis() - currentButtonPressTime;

            if ((currentButtonPressTime == 0) || (timeSinceLastPress < buttonPressMaxDelay)) {
                currentButtonPressCount++;
            }
            else {
                currentButtonPressCount = 1;
                currentButtonActionId = 0;
            }

            currentButtonPressTime = System.currentTimeMillis();
            if (currentButtonPressCount == requiredButtonPressCount) {
                currentButtonTimerActivationTime = currentButtonPressTime;
                if (buttonActionDelay > 0) {
                    LOG.info("Activating timer");
                    final Timer buttonActionTimer = new Timer("Mi Band Button Action Timer");
                    buttonActionTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runButtonAction();
                            buttonActionTimer.cancel();
                        }
                    }, buttonActionDelay, buttonActionDelay);
                }
                else {
                    LOG.info("Activating button action");
                    runButtonAction();
                }
                currentButtonActionId++;
                currentButtonPressCount = 0;
            }
        }
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        UUID characteristicUUID = characteristic.getUuid();
        if (MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO.equals(characteristicUUID)) {
            handleBatteryInfo(characteristic.getValue(), BluetoothGatt.GATT_SUCCESS);
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
            return true;
        } else if (GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            //handleHeartrate(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_AUTH.equals(characteristicUUID)) {
            LOG.info("AUTHENTICATION?? " + characteristicUUID);
            logMessageContent(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_10_BUTTON.equals(characteristicUUID)) {
            handleButtonPressed(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
            return true;
        } else {
            Log.i(TAG, "Unhandled characteristic changed: " + characteristicUUID);
            logMessageContent(characteristic.getValue());
        }

        return false;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        UUID characteristicUUID = characteristic.getUuid();
        if (GattCharacteristic.UUID_CHARACTERISTIC_GAP_DEVICE_NAME.equals(characteristicUUID)) {
            handleDeviceName(characteristic.getValue(), status);
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_6_BATTERY_INFO.equals(characteristicUUID)) {
            handleBatteryInfo(characteristic.getValue(), status);
            return true;
        } else if (GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            logHeartrate(characteristic.getValue(), status);
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_7_REALTIME_STEPS.equals(characteristicUUID)) {
            handleRealtimeSteps(characteristic.getValue());
            return true;
        } else if (MiBand2Service.UUID_CHARACTERISTIC_10_BUTTON.equals(characteristicUUID)) {
            handleButtonPressed(characteristic.getValue());
            return true;
        } else {
            LOG.info("Unhandled characteristic read: " + characteristicUUID);
            logMessageContent(characteristic.getValue());
        }

        return false;
    }

    @Override
    public boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        UUID characteristicUUID = characteristic.getUuid();
        if (MiBand2Service.UUID_CHARACTERISTIC_AUTH.equals(characteristicUUID)) {
            Log.i(TAG, "KEY AES SEND");
            logMessageContent(characteristic.getValue());
            return true;
        }
        return false;
    }

    public void logHeartrate(byte[] value, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
            LOG.info("Got heartrate:");
            if (value.length == 2 && value[0] == 0) {
                int hrValue = (value[1] & 0xff);
                Toast.makeText(getContext(), "Heart rate measured: " + hrValue, Toast.LENGTH_LONG).show();
            }
            return;
        }
        logMessageContent(value);
    }

    private void handleHeartrate(byte[] value) {
        if (value.length == 2 && value[0] == 0) {
            int hrValue = (value[1] & 0xff);
            RealtimeSamplesSupport realtimeSamplesSupport = getRealtimeSamplesSupport();
            realtimeSamplesSupport.setHeartrateBpm(hrValue);
            if (!realtimeSamplesSupport.isRunning()) {
                // single shot measurement, manually invoke storage and result publishing
                realtimeSamplesSupport.triggerCurrentSample();
            }
        }
    }

    // TODO Create samples steps and heartrate
    private void handleRealtimeSteps(byte[] value) {
        if (value == null) {
            Log.e(TAG, "Realtime steps: value is null");
            return;
        }

        if (value.length == 13) {
            byte[] stepsValue = new byte[] {value[1], value[2]};
            int steps = BLETypeConversions.toUint16(stepsValue);
            getRealtimeSamplesSupport().setSteps(steps);
        } else {
            Log.w(TAG, "Unrecognized realtime steps value");
        }
    }

    private void enableRealtimeSamplesTimer(boolean enable) {
        if (enable) {
            getRealtimeSamplesSupport().start();
        } else {
            if (realtimeSamplesSupport != null) {
                realtimeSamplesSupport.stop();
            }
        }
    }

//    public MiBand2ActivitySample createActivitySample(Device device, User user, int timestampInSeconds, SampleProvider provider) {
//        MiBand2ActivitySample sample = new MiBand2ActivitySample();
//        sample.setDevice(device);
//        sample.setUser(user);
//        sample.setTimestamp(timestampInSeconds);
//        sample.setProvider(provider);
//
//        return sample;
//    }

    private RealtimeSamplesSupport getRealtimeSamplesSupport() {
        if (realtimeSamplesSupport == null) {
//            realtimeSamplesSupport = new RealtimeSamplesSupport(1000, 1000) {
//                @Override
//                protected void doCurrentSample() {
//                    try (DBHandler handler = `lication.acquireDB()) {
//                        DaoSession session = handler.getDaoSession();
//                        Device device = DBHelper.getDevice(getDevice(), session);
//                        User user = DBHelper.getUser(session);
//                        int ts = (int) (System.currentTimeMillis() / 1000);
//                        MiBand2SampleProvider provider = new MiBand2SampleProvider(sciousDevice, session);
//                        MiBand2ActivitySample sample = createActivitySample(device, user, ts, provider);
//                        sample.setHeartRate(getHeartrateBpm());
//                        sample.setSteps(getSteps());
//                        sample.setRawIntensity(ActivitySample.NOT_MEASURED);
//                        sample.setRawKind(MiBand2SampleProvider.TYPE_ACTIVITY);
//
//                        provider.addSciousActivitySample(sample);
//                        sample.setSteps(getSteps());
//
//                        Intent intent = new Intent(IDeviceService.ACTION_REALTIME_SAMPLES)
//                                .putExtra(IDeviceService.EXTRA_REALTIME_SAMPLE, sample);
//                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
//                    } catch (Exception e) {
//                        Log.w(TAG, "Unable to acquire db for saving realtime samples", e);
//                    }
//                }
//            };
        }
        return realtimeSamplesSupport;
    }

    private void handleDeviceName(byte[] value, int status) {

    }

    private void handleDeviceInfo(DeviceInfo info) {

        Log.w(TAG, "Device info: " + info);
        versionCmd.hwVersion = info.getHardwareRevision();

        versionCmd.fwVersion = info.getSoftwareRevision();
        if (versionCmd.fwVersion != null && versionCmd.fwVersion.length() > 0 && versionCmd.fwVersion.charAt(0) == 'V') {
            versionCmd.fwVersion = versionCmd.fwVersion.substring(1);
        }
        handleSciousDeviceEvent(versionCmd);
    }

    private void handleBatteryInfo(byte[] value, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BatteryInfo info = new BatteryInfo(value);
            batteryCmd.level = ((short) info.getLevelInPercent());
            batteryCmd.state = info.getState();
            batteryCmd.lastChargeTime = info.getLastChargeTime();
            batteryCmd.numCharges = info.getNumCharges();
            handleSciousDeviceEvent(batteryCmd);
        }
    }

    private MiBand2Support sendCalendarEvents(TransactionBuilder builder) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION);

        Prefs prefs = SciousApplication.getPrefs();
        int availableSlots = prefs.getInt(MiBand2Const.PREF_MIBAND2_RESERVE_ALARM_FOR_CALENDAR, 0);
        if (availableSlots > 0) {
            CalendarEvents upcomingEvents = new CalendarEvents();
            List<CalendarEvents.CalendarEvent> mEvents = upcomingEvents.getCalendarEventList(getContext());

            int iteration = 0;
            for (CalendarEvents.CalendarEvent mEvt : mEvents) {
                if (iteration >= availableSlots || iteration > 2) {
                    break;
                }
                int slotToUse = 2 - iteration;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(mEvt.getBegin());
            }
        }
        return this;
    }

    @Override
    public void onSendConfiguration(String config) {
        TransactionBuilder builder;
        try {
            builder = performInitialized("Sending configuration for option: " + config);
            switch (config) {
                case MiBand2Const.PREF_MIBAND2_DATEFORMAT:
                    setDateDisplay(builder);
                    break;
                case MiBand2Const.PREF_MIBAND2_GOAL_NOTIFICATION:
                    setGoalNotification(builder);
                    break;
                case MiBand2Const.PREF_MIBAND2_ACTIVATE_DISPLAY_ON_LIFT:
                    setActivateDisplayOnLiftWrist(builder);
                    break;
                case MiBand2Const.PREF_MIBAND2_DISPLAY_ITEMS:
                    setDisplayItems(builder);
                    break;
                case MiBand2Const.PREF_MIBAND2_ROTATE_WRIST_TO_SWITCH_INFO:
                    setRotateWristToSwitchInfo(builder);
                    break;
                case ActivityUser.PREF_USER_STEPS_GOAL:
                    setFitnessGoal(builder);
                    break;
                case MiBand2Const.PREF_MIBAND2_DO_NOT_DISTURB:
                case MiBand2Const.PREF_MIBAND2_DO_NOT_DISTURB_START:
                case MiBand2Const.PREF_MIBAND2_DO_NOT_DISTURB_END:
                    setDoNotDisturb(builder);
                    break;
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS:
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_THRESHOLD:
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_START:
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_END:
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_DND:
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_DND_START:
                case MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_DND_END:
                    setInactivityWarnings(builder);
                    break;
            }
            builder.queue(getQueue());
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error setting configuration", Toast.LENGTH_LONG).show();
        }
    }

    private MiBand2Support setDateDisplay(TransactionBuilder builder) {
        DateTimeDisplay dateTimeDisplay = MiBand2Coordinator.getDateDisplay(getContext());
        Log.i(TAG, "Setting date display to " + dateTimeDisplay);
        switch (dateTimeDisplay) {
            case TIME:
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_TIME);
                break;
            case DATE_TIME:
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_DATE_TIME);
                break;
        }
        return this;
    }

    private MiBand2Support setTimeFormat(TransactionBuilder builder) {
        boolean is24Format = DateFormat.is24HourFormat(getContext());
        Log.i(TAG, "Setting 24h time format to " + is24Format);
        if (is24Format) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_TIME_24_HOURS);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.DATEFORMAT_TIME_12_HOURS);
        }
        return this;
    }

    private MiBand2Support setGoalNotification(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getGoalNotification();
        Log.i(TAG, "Setting goal notification to " + enable);
        if (enable) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_GOAL_NOTIFICATION);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_GOAL_NOTIFICATION);
        }
        return this;
    }

    private MiBand2Support setActivateDisplayOnLiftWrist(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getActivateDisplayOnLiftWrist();
        Log.i(TAG, "Setting activate display on lift wrist to " + enable);
        if (enable) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_DISPLAY_ON_LIFT_WRIST);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_DISPLAY_ON_LIFT_WRIST);
        }
        return this;
    }

    private MiBand2Support setDisplayItems(TransactionBuilder builder) {
        Set<String> pages = MiBand2Coordinator.getDisplayItems();
        LOG.info("Setting display items to " + (pages == null ? "none" : pages));

        byte[] data = MiBand2Service.COMMAND_CHANGE_SCREENS.clone();

        if (pages != null) {
            if (pages.contains(MiBand2Const.PREF_MIBAND2_DISPLAY_ITEM_STEPS)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_STEPS;
            }
            if (pages.contains(MiBand2Const.PREF_MIBAND2_DISPLAY_ITEM_DISTANCE)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_DISTANCE;
            }
            if (pages.contains(MiBand2Const.PREF_MIBAND2_DISPLAY_ITEM_CALORIES)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_CALORIES;
            }
            if (pages.contains(MiBand2Const.PREF_MIBAND2_DISPLAY_ITEM_HEART_RATE)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_HEART_RATE;
            }
            if (pages.contains(MiBand2Const.PREF_MIBAND2_DISPLAY_ITEM_BATTERY)) {
                data[MiBand2Service.SCREEN_CHANGE_BYTE] |= MiBand2Service.DISPLAY_ITEM_BIT_BATTERY;
            }
        }

        builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), data);
        return this;
    }

    private MiBand2Support setRotateWristToSwitchInfo(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getRotateWristToSwitchInfo();
        Log.i(TAG, "Setting rotate wrist to cycle info to " + enable);
        if (enable) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_ROTATE_WRIST_TO_SWITCH_INFO);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_ROTATE_WRIST_TO_SWITCH_INFO);
        }
        return this;
    }

    private MiBand2Support setDisplayCaller(TransactionBuilder builder) {
        builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_ENABLE_DISPLAY_CALLER);
        return this;
    }

    private MiBand2Support setDoNotDisturb(TransactionBuilder builder) {
        DoNotDisturb doNotDisturb = MiBand2Coordinator.getDoNotDisturb(getContext());
        Log.i(TAG, "Setting do not disturb to " + doNotDisturb);
        switch (doNotDisturb) {
            case OFF:
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DO_NOT_DISTURB_OFF);
                break;
            case AUTOMATIC:
                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DO_NOT_DISTURB_AUTOMATIC);
                break;
            case SCHEDULED:
                byte[] data = MiBand2Service.COMMAND_DO_NOT_DISTURB_SCHEDULED.clone();

                Calendar calendar = GregorianCalendar.getInstance();

                Date start = MiBand2Coordinator.getDoNotDisturbStart();
                calendar.setTime(start);
                data[MiBand2Service.DND_BYTE_START_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                data[MiBand2Service.DND_BYTE_START_MINUTES] = (byte) calendar.get(Calendar.MINUTE);

                Date end = MiBand2Coordinator.getDoNotDisturbEnd();
                calendar.setTime(end);
                data[MiBand2Service.DND_BYTE_END_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                data[MiBand2Service.DND_BYTE_END_MINUTES] = (byte) calendar.get(Calendar.MINUTE);

                builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), data);
                break;
        }
        return this;
    }

    private MiBand2Support setInactivityWarnings(TransactionBuilder builder) {
        boolean enable = MiBand2Coordinator.getInactivityWarnings();
        Log.i(TAG, "Setting inactivity warnings to " + enable);

        if (enable) {
            byte[] data = MiBand2Service.COMMAND_ENABLE_INACTIVITY_WARNINGS.clone();

            int threshold = MiBand2Coordinator.getInactivityWarningsThreshold();
            data[MiBand2Service.INACTIVITY_WARNINGS_THRESHOLD] = (byte) threshold;

            Calendar calendar = GregorianCalendar.getInstance();

            boolean enableDnd = MiBand2Coordinator.getInactivityWarningsDnd();

            Date intervalStart = MiBand2Coordinator.getInactivityWarningsStart();
            Date intervalEnd = MiBand2Coordinator.getInactivityWarningsEnd();
            Date dndStart = MiBand2Coordinator.getInactivityWarningsDndStart();
            Date dndEnd = MiBand2Coordinator.getInactivityWarningsDndEnd();

            // The first interval always starts when the warnings interval starts
            calendar.setTime(intervalStart);
            data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_1_START_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_1_START_MINUTES] = (byte) calendar.get(Calendar.MINUTE);

            if (enableDnd) {
                // The first interval ends when the dnd interval starts
                calendar.setTime(dndStart);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_1_END_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_1_END_MINUTES] = (byte) calendar.get(Calendar.MINUTE);

                // The second interval starts when the dnd interval ends
                calendar.setTime(dndEnd);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_2_START_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_2_START_MINUTES] = (byte) calendar.get(Calendar.MINUTE);

                // ... and it ends when the warnings interval ends
                calendar.setTime(intervalEnd);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_2_END_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_2_END_MINUTES] = (byte) calendar.get(Calendar.MINUTE);
            } else {
                // No Dnd, use the first interval
                calendar.setTime(intervalEnd);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_1_END_HOURS] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                data[MiBand2Service.INACTIVITY_WARNINGS_INTERVAL_1_END_MINUTES] = (byte) calendar.get(Calendar.MINUTE);
            }

            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), data);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISABLE_INACTIVITY_WARNINGS);
        }

        return this;
    }

    private MiBand2Support setDistanceUnit(TransactionBuilder builder) {
        MiBand2Const.DistanceUnit unit = MiBand2Coordinator.getDistanceUnit();
        Log.i(TAG, "Setting distance unit to " + unit);
        if (unit == MiBand2Const.DistanceUnit.METRIC) {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISTANCE_UNIT_METRIC);
        } else {
            builder.write(getCharacteristic(MiBand2Service.UUID_CHARACTERISTIC_3_CONFIGURATION), MiBand2Service.COMMAND_DISTANCE_UNIT_IMPERIAL);
        }
        return this;
    }

    public void phase2Initialize(TransactionBuilder builder) {
        Log.i(TAG, "phase2Initialize...");
        requestBatteryInfo(builder);
    }

    public void phase3Initialize(TransactionBuilder builder) {
        Log.i(TAG, "phase3Initialize...");
        setDateDisplay(builder);
        setTimeFormat(builder);
        setUserInfo(builder);
        setDistanceUnit(builder);
        setWearLocation(builder);
        setFitnessGoal(builder);
        setDisplayItems(builder);
        setDoNotDisturb(builder);
        setRotateWristToSwitchInfo(builder);
        setActivateDisplayOnLiftWrist(builder);
        setDisplayCaller(builder);
        setGoalNotification(builder);
        setInactivityWarnings(builder);
        setHeartrateSleepSupport(builder);
    }
}
