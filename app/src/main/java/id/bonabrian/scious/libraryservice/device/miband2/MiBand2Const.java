package id.bonabrian.scious.libraryservice.device.miband2;

import id.bonabrian.scious.util.Prefs;
import id.bonabrian.scious.util.Version;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public final class MiBand2Const {

    public enum DistanceUnit {
        METRIC,
        IMPERIAL
    }

    private static final String TAG = MiBand2Const.class.getSimpleName();

    public static final String PREF_USER_ALIAS = "mi_user_alias";
    public static final String PREF_USER_EMAIL = "mi_user_email";
    public static final String PREF_MIBAND2_WEARSIDE = "mi_wearside";
    public static final String PREF_MIBAND2_ADDRESS = "mi_addr";
    public static final String MIBAND2_NAME = "MI Band 2";
    public static final String PREF_MIBAND2_RESERVE_ALARM_FOR_CALENDAR = "mi_reserve_alarm_calendar";

    public static final String PREF_MIBAND2_SETUP_BT_PAIRING = "mi_setup_bt_pairing";

    public static final String PREF_MIBAND2_DO_NOT_DISTURB = "mi_do_not_disturb";
    public static final String PREF_MIBAND2_DO_NOT_DISTURB_OFF = "off";
    public static final String PREF_MIBAND2_DO_NOT_DISTURB_SCHEDULED = "scheduled";
    public static final String PREF_MIBAND2_DO_NOT_DISTURB_START = "mi_do_not_disturb_start";
    public static final String PREF_MIBAND2_DO_NOT_DISTURB_END = "mi_do_not_disturb_end";

    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS = "mi_inactivity_warnings";
    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS_THRESHOLD = "mi_inactivity_warnings_threshold";
    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS_START = "mi_inactivity_warnings_start";
    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS_END = "mi_inactivity_warnings_end";
    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS_DND = "mi_inactivity_warnings_dnd";
    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS_DND_START = "mi_inactivity_warnings_dnd_start";
    public static final String PREF_MIBAND2_INACTIVITY_WARNINGS_DND_END = "mi_inactivity_warnings_dnd_end";

    public static final String PREF_MIBAND2_DEVICE_TIME_OFFSET_HOURS = "mi_device_time_offset_hours";
    public static final String PREF_MIBAND2_BUTTON_PRESS_BROADCAST = "mi_button_press_broadcast";
    public static final String PREF_MIBAND2_BUTTON_ACTION_VIBRATE = "mi_button_action_vibrate";
    public static final String PREF_MIBAND2_BUTTON_ACTION_ENABLE = "mi_enable_button_action";
    public static final String PREF_MIBAND2_BUTTON_PRESS_MAX_DELAY = "mi_button_press_count_max_delay";
    public static final String PREF_MIBAND2_BUTTON_ACTION_DELAY = "mi_button_press_count_match_delay";
    public static final String PREF_MIBAND2_BUTTON_PRESS_COUNT = "mi_button_press_count";
    public static final String PREF_MIBAND2_ENABLE_TEXT_NOTIFICATIONS = "mi_enable_text_notifications";

    public static final String PREF_MIBAND2_DISPLAY_ITEMS = "mi_display_items";
    public static final String PREF_MIBAND2_DISPLAY_ITEM_STEPS = "steps";
    public static final String PREF_MIBAND2_DISPLAY_ITEM_DISTANCE = "distance";
    public static final String PREF_MIBAND2_DISPLAY_ITEM_CALORIES = "calories";
    public static final String PREF_MIBAND2_DISPLAY_ITEM_HEART_RATE = "heart_rate";
    public static final String PREF_MIBAND2_DISPLAY_ITEM_BATTERY = "battery";
    public static final String PREF_MIBAND2_ROTATE_WRIST_TO_SWITCH_INFO = "mi_rotate_wrist_to_switch_info";

    public static final String PREF_MIBAND2_DATEFORMAT = "mi_dateformat";
    public static final String PREF_MIBAND2_GOAL_NOTIFICATION = "mi_goal_notification";
    public static final String PREF_MIBAND2_ACTIVATE_DISPLAY_ON_LIFT = "mi_activate_display_on_lift_wrist";

    public static final String PREF_MIBAND2_USE_HEARTRATE_FOR_SLEEP_DETECTION = "mi_heartrate_sleep_detection";

    public static final String VIBRATION_PROFILE = "mi_vibration_profile";
    public static final String VIBRATION_COUNT = "mi_vibration_count";
    public static final String VIBRATION_DURATION = "mi_vibration_duration";
    public static final String VIBRATION_PAUSE = "mi_vibration_pause";
    public static final String FLASH_COUNT = "mi_flash_count";
    public static final String FLASH_DURATION = "mi_flash_duration";
    public static final String FLASH_COLOUR = "mi_flash_colour";
    public static final String FLASH_ORIGINAL_COLOUR = "mi_flash_original_colour";

    public static final String DEFAULT_VALUE_VIBRATION_PROFILE = "short";
    public static final int DEFAULT_VALUE_VIBRATION_COUNT = 3;
    public static final int DEFAULT_VALUE_VIBRATION_DURATION = 500; // ms
    public static final int DEFAULT_VALUE_VIBRATION_PAUSE = 500; // ms
    public static final int DEFAULT_VALUE_FLASH_COUNT = 10; // ms
    public static final int DEFAULT_VALUE_FLASH_DURATION = 500; // ms
    public static final int DEFAULT_VALUE_FLASH_COLOUR = 1; // TODO: colour!
    public static final int DEFAULT_VALUE_FLASH_ORIGINAL_COLOUR = 1; // TODO: colour!

    public static final Version MI2_FW_VERSION_MIN_TEXT_NOTIFICATIONS = new Version("1.0.1.28");

    public static int getNotificationPrefIntValue(String pref, String origin, Prefs prefs, int defaultValue) {
        String key = getNotificationPrefKey(pref, origin);
        return prefs.getInt(key, defaultValue);
    }

    public static String getNotificationPrefStringValue(String pref, String origin, Prefs prefs, String defaultValue) {
        String key = getNotificationPrefKey(pref, origin);
        return prefs.getString(key, defaultValue);
    }

    public static String getNotificationPrefKey(String pref, String origin) {
        return pref + '_' + origin;
    }
}
