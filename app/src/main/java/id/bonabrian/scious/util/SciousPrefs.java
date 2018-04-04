package id.bonabrian.scious.util;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SciousPrefs {
    public static final String AUTO_RECONNECT = "general_autoreconnect";
    private static final String AUTO_START = "general_autostart";

    private static final boolean AUTO_START_DEFAULT = true;
    public static boolean AUTO_RECONNECT_DEFAULT = true;

    private final Prefs mPrefs;

    public SciousPrefs(Prefs prefs) {
        mPrefs = prefs;
    }

    public boolean getAutoReconnect() {
        return mPrefs.getBoolean(AUTO_RECONNECT, AUTO_RECONNECT_DEFAULT);
    }

    public boolean getAutoStart() {
        return mPrefs.getBoolean(AUTO_START, AUTO_START_DEFAULT);
    }
}
