package id.bonabrian.scious.util;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Prefs {
    private static final String TAG = Prefs.class.getSimpleName();
    private static SharedPreferences preferences;

    public Prefs(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getString(String key, String defaultValue) {
        String value = preferences.getString(key, defaultValue);
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        return value;
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        Set<String> value = preferences.getStringSet(key, defaultValue);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        try {
            return preferences.getInt(key, defaultValue);
        } catch (Exception ex) {
            try {
                String value = preferences.getString(key, String.valueOf(defaultValue));
                if ("".equals(value)) {
                    return defaultValue;
                }
                return Integer.parseInt(value);
            } catch (Exception ex2) {
                logReadError(key, ex);
                return defaultValue;
            }
        }
    }

    public long getLong(String key, long defaultValue) {
        try {
            return preferences.getLong(key, defaultValue);
        } catch (Exception ex) {
            try {
                String value = preferences.getString(key, String.valueOf(defaultValue));
                if ("".equals(value)) {
                    return defaultValue;
                }
                return Long.parseLong(value);
            } catch (Exception ex2) {
                logReadError(key, ex);
                return defaultValue;
            }
        }
    }

    public float getFloat(String key, float defaultValue) {
        try {
            return preferences.getFloat(key, defaultValue);
        } catch (Exception ex) {
            try {
                String value = preferences.getString(key, String.valueOf(defaultValue));
                if ("".equals(value)) {
                    return defaultValue;
                }
                return Float.parseFloat(value);
            } catch (Exception ex2) {
                logReadError(key, ex);
                return defaultValue;
            }
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return preferences.getBoolean(key, defaultValue);
        } catch (Exception ex) {
            try {
                String value = preferences.getString(key, String.valueOf(defaultValue));
                if ("".equals(value)) {
                    return defaultValue;
                }
                return Boolean.parseBoolean(value);
            } catch (Exception ex2) {
                logReadError(key, ex);
                return defaultValue;
            }
        }
    }

    private void logReadError(String key, Exception ex) {
        Log.e(TAG, "Error reading preference value: " + key + "; returning default value", ex); // log the first exception
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public static void putStringSet(SharedPreferences.Editor editor, String preference, HashSet<String> value) {
        editor.putStringSet(preference, null);
        editor.commit();
        editor.putStringSet(preference, new HashSet<>(value));
    }
}
