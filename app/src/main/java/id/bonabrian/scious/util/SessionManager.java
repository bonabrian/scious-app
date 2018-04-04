package id.bonabrian.scious.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import id.bonabrian.scious.source.dao.User;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SessionManager {
    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userJson = preferences.getString(AppConstant.PreferencesKey.SESSION_USER_LOGIN, null);
        if (userJson != null) {
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    public static User getLoggedUser(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userJson = preferences.getString(AppConstant.PreferencesKey.SESSION_USER_LOGIN, null);
        if (userJson != null) {
            User user = new Gson().fromJson(userJson, User.class);
            return user;
        } else {
            return null;
        }
    }

    public static boolean logout(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.clear();
        editor.commit();

        return true;
    }

    public static boolean setLoggedUser(Context context, User userModel) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        String profileJson = new Gson().toJson(userModel);
        editor.putString(AppConstant.PreferencesKey.SESSION_USER_LOGIN, profileJson);
        editor.commit();
        return true;
    }

    public static boolean isFinishedWalkthrough(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(AppConstant.PreferencesKey.KEY_WALKTHROUGH, false);
    }

    public static boolean setFinishedWalkthrough(Context context, boolean status) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(AppConstant.PreferencesKey.KEY_WALKTHROUGH, status);
        editor.commit();
        return true;
    }
}
