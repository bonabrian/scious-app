package id.bonabrian.scious.util;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class AppConstant {
    public static class Api {
        public static String BASE_URL = "http://scious.web.id/";
    }

    public static class Tag {
        public static final int RC_SIGN_IN_GOOGLE = 007;
        public static int TAG_ADAPTERTYPE_LIST = 0;
        public static int TAG_ADAPTERTYPE_LOAD = 1;
        public static String TAG_HOME_FRAGMENT = "home";
        public static String TAG_HISTORY_FRAGMENT = "history";
        public static String TAG_LEARNS_FRAGMENT = "learns";
        public static String TAG_PROFILE_FRAGMENT = "profile";
    }

    public static class PreferencesKey {
        public static final String SESSION_USER_LOGIN = "user-login";

        public static final String KEY_WALKTHROUGH = "key-walkthrough";
    }
}
