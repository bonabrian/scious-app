package id.bonabrian.scious.pairing;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashSet;
import java.util.Set;

import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.libraryservice.device.DeviceManager;
import id.bonabrian.scious.libraryservice.model.ActivityUser;
import id.bonabrian.scious.util.Prefs;

import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.PREF_MIBAND2_ADDRESS;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.PREF_MIBAND2_DATEFORMAT;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.PREF_MIBAND2_DISPLAY_ITEMS;
import static id.bonabrian.scious.libraryservice.device.miband2.MiBand2Const.PREF_USER_ALIAS;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MiBand2PreferencesActivity extends AbsPreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.users_preferences);

        Prefs prefs = SciousApplication.getPrefs();

        final Preference setDateFormat = findPreference(PREF_MIBAND2_DATEFORMAT);
        setDateFormat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SciousApplication.deviceService().onSendConfiguration(PREF_MIBAND2_DATEFORMAT);
                    }
                });
                return true;
            }
        });

        final Preference displayItems = findPreference(PREF_MIBAND2_DISPLAY_ITEMS);
        displayItems.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SciousApplication.deviceService().onSendConfiguration(PREF_MIBAND2_DISPLAY_ITEMS);
                    }
                });
                return true;
            }
        });
    }

    private void invokeLater(Runnable runnable) {
        getListView().post(runnable);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final Preference miAddr = findPreference(PREF_MIBAND2_ADDRESS);
        miAddr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                Intent refreshIntent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(refreshIntent);
                preference.setSummary(newVal.toString());
                return true;
            }
        });
    }

    @Override
    protected String[] getPreferenceKeysWithSummary() {
        Set<String> prefKeys = new HashSet<>();
        prefKeys.add(PREF_USER_ALIAS);
        prefKeys.add(PREF_MIBAND2_ADDRESS);
//        prefKeys.add(ActivityUser.PREF_USER_STEPS_GOAL);
//        prefKeys.add(PREF_MIBAND2_INACTIVITY_WARNINGS_THRESHOLD);
        prefKeys.add(ActivityUser.PREF_USER_HEIGHT_CM);
        prefKeys.add(ActivityUser.PREF_USER_WEIGHT_KG);
        prefKeys.add(ActivityUser.PREF_USER_YEAR_OF_BIRTH);
        prefKeys.add(ActivityUser.PREF_USER_GENDER);

        return prefKeys.toArray(new String[0]);
    }
}
