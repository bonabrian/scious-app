package id.bonabrian.scious.libraryservice.device.miband2;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import de.greenrobot.dao.query.QueryBuilder;
import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.entities.DaoSession;
import id.bonabrian.scious.libraryservice.device.AbsDeviceCoordinator;
import id.bonabrian.scious.libraryservice.impl.SciousDevice;
import id.bonabrian.scious.libraryservice.impl.SciousDeviceCandidate;
import id.bonabrian.scious.libraryservice.model.ActivityUser;
import id.bonabrian.scious.libraryservice.model.DeviceType;
import id.bonabrian.scious.main.MainActivity;
import id.bonabrian.scious.pairing.DevicePairingActivity;
import id.bonabrian.scious.source.dao.Device;
import id.bonabrian.scious.util.Prefs;
import id.bonabrian.scious.util.SessionManager;
import id.bonabrian.scious.util.UserInfo;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MiBand2Coordinator extends AbsDeviceCoordinator {
    private static final String TAG = MiBand2Coordinator.class.getSimpleName();

    public MiBand2Coordinator() {

    }

    public static boolean getValidateUserInfo(String miBandAddress) {
        try {
            UserInfo userInfo = getConfiguredUserInfo(miBandAddress);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static UserInfo getConfiguredUserInfo(String miBandAddress) throws IllegalArgumentException {
        ActivityUser activityUser = new ActivityUser();
        Prefs prefs = SciousApplication.getPrefs();

        UserInfo info = UserInfo.create(
                miBandAddress,
                SessionManager.getLoggedUser(SciousApplication.getContext()).getName(),
                SessionManager.getLoggedUser(SciousApplication.getContext()).getEmail(),
                activityUser.getGender(),
                activityUser.getAge(),
                activityUser.getHeight(),
                activityUser.getWeight()
        );
        return info;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MIBAND2;
    }

    @NonNull
    @Override
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        ParcelUuid mi2Service = new ParcelUuid(MiBand2Service.UUID_SERVICE_MIBAND2_SERVICE);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(mi2Service).build();
        return Collections.singleton(filter);
    }

    @Override
    protected void deleteDevice(SciousDevice sciousDevice, Device device, DaoSession session) throws Exception {
        Long deviceId = device.getId();
        QueryBuilder<?> queryBuilder = session.getDeviceDao().queryBuilder();
        queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(SciousDeviceCandidate candidate) {
        if (candidate.supportsService(MiBand2Service.UUID_SERVICE_MIBAND2_SERVICE)) {
            return DeviceType.MIBAND2;
        }
        try {
            BluetoothDevice device = candidate.getDevice();
            String name = device.getName();
            if (name != null && name.equalsIgnoreCase(MiBand2Const.MIBAND2_NAME)) {
                return DeviceType.MIBAND2;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to check device support", ex);
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public boolean supportsHeartRateMeasurement(SciousDevice device) {
        return true;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Nullable
    @Override
    public Class<? extends Activity> getPairingActivity() {
        return DevicePairingActivity.class;
    }

    @Nullable
    @Override
    public Class<? extends Activity> getPrimaryActivity() {
        return MainActivity.class;
    }

    @Override
    public boolean supportsActivityTracking() {
        return true;
    }

    @Override
    public String getManufacturer() {
        return "Xiaomi";
    }

    @Override
    public boolean supportsRealtimeData() {
        return true;
    }

    // TODO create samples
//    @Override
//    public SampleProvider<? extends AbsActivitySample> getSampleProvider(SciousDevice device, DaoSession session) {
//        return new MiBand2SampleProvider(device, session);
//    }

    public static int getDeviceTimeOffsetHours() throws IllegalArgumentException {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getInt(MiBand2Const.PREF_MIBAND2_DEVICE_TIME_OFFSET_HOURS, 0);
    }

    public static boolean getActivateDisplayOnLiftWrist() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getBoolean(MiBand2Const.PREF_MIBAND2_ACTIVATE_DISPLAY_ON_LIFT, true);
    }

    public static boolean getGoalNotification() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getBoolean(MiBand2Const.PREF_MIBAND2_GOAL_NOTIFICATION, false);
    }

    public static Set<String> getDisplayItems() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getStringSet(MiBand2Const.PREF_MIBAND2_DISPLAY_ITEMS, null);
    }

    public static boolean getRotateWristToSwitchInfo() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getBoolean(MiBand2Const.PREF_MIBAND2_ROTATE_WRIST_TO_SWITCH_INFO, false);
    }

    public static DateTimeDisplay getDateDisplay(Context context) throws IllegalArgumentException {
        Prefs prefs = SciousApplication.getPrefs();
        String dateFormatTime = context.getString(R.string.p_dateformat_time);
        if (dateFormatTime.equals(prefs.getString(MiBand2Const.PREF_MIBAND2_DATEFORMAT, dateFormatTime))) {
            return DateTimeDisplay.TIME;
        }
        return DateTimeDisplay.DATE_TIME;
    }

    public static int getWearLocation(String miBandAddress) throws IllegalArgumentException {
        int location = 0; //left hand
        Prefs prefs = SciousApplication.getPrefs();
        if ("right".equals(prefs.getString(MiBand2Const.PREF_MIBAND2_WEARSIDE, "left"))) {
            location = 1; // right hand
        }
        return location;
    }

    public static boolean getHeartrateSleepSupport(String miBandAddress) {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getBoolean(MiBand2Const.PREF_MIBAND2_USE_HEARTRATE_FOR_SLEEP_DETECTION, false);
    }


    public static DoNotDisturb getDoNotDisturb(Context context) {
        Prefs prefs = SciousApplication.getPrefs();

        String dndOff = context.getString(R.string.p_off);
        String dndAutomatic = context.getString(R.string.p_automatic);
        String dndScheduled = context.getString(R.string.p_scheduled);

        String pref = prefs.getString(MiBand2Const.PREF_MIBAND2_DO_NOT_DISTURB, dndOff);

        if (dndAutomatic.equals(pref)) {
            return DoNotDisturb.AUTOMATIC;
        } else if (dndScheduled.equals(pref)) {
            return DoNotDisturb.SCHEDULED;
        }

        return DoNotDisturb.OFF;
    }

    public static Date getDoNotDisturbStart() {
        return getTimePreference(MiBand2Const.PREF_MIBAND2_DO_NOT_DISTURB_START, "01:00");
    }

    public static Date getDoNotDisturbEnd() {
        return getTimePreference(MiBand2Const.PREF_MIBAND2_DO_NOT_DISTURB_END, "06:00");
    }

    public static Date getTimePreference(String key, String defaultValue) {
        Prefs prefs = SciousApplication.getPrefs();
        String  time = prefs.getString(key, defaultValue);

        DateFormat df = new SimpleDateFormat("HH:mm");
        try {
            return df.parse(time);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception in MiBand2Coordinator.getTime: " + e.getMessage());
        }

        return new Date();
    }

    public static boolean getInactivityWarnings() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getBoolean(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS, false);
    }

    public static int getInactivityWarningsThreshold() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getInt(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_THRESHOLD, 60);
    }

    public static boolean getInactivityWarningsDnd() {
        Prefs prefs = SciousApplication.getPrefs();
        return prefs.getBoolean(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_DND, false);
    }

    public static Date getInactivityWarningsStart() {
        return getTimePreference(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_START, "06:00");
    }

    public static Date getInactivityWarningsEnd() {
        return getTimePreference(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_END, "22:00");
    }

    public static Date getInactivityWarningsDndStart() {
        return getTimePreference(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_DND_START, "12:00");
    }

    public static Date getInactivityWarningsDndEnd() {
        return getTimePreference(MiBand2Const.PREF_MIBAND2_INACTIVITY_WARNINGS_DND_END, "14:00");
    }

    public static MiBand2Const.DistanceUnit getDistanceUnit() {
        Prefs prefs = SciousApplication.getPrefs();
        //String unit = prefs.getString(SettingsActivity.PREF_MEASUREMENT_SYSTEM, SciousApplication.getContext().getString(R.string.p_unit_metric));
//        if (unit.equals(SciousApplication.getContext().getString(R.string.p_unit_metric))) {
//            return MiBand2Const.DistanceUnit.METRIC;
//        } else {
//            return MiBand2Const.DistanceUnit.IMPERIAL;
//        }
        return null;
    }
}
