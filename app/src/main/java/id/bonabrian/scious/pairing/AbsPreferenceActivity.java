package id.bonabrian.scious.pairing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;

import id.bonabrian.scious.app.SciousApplication;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class AbsPreferenceActivity extends AppCompatPreferenceActivity {
    private static final String TAG = AbsPreferenceActivity.class.getSimpleName();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case SciousApplication.ACTION_QUIT:
                    finish();
                    break;
            }
        }
    };

    private static class SimpleSetSummaryOnChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof EditTextPreference) {
                if ((((EditTextPreference) preference).getEditText().getKeyListener().getInputType() & InputType.TYPE_CLASS_NUMBER) != 0) {
                    if ("".equals(String.valueOf(value))) {
                        return false;
                    }
                }
            }
            updateSummary(preference, value);
            return true;
        }

        public void updateSummary(Preference preference, Object value) {
            String stringValue = String.valueOf(value);

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0 ? listPreference.getEntries()[index] : null
                );
            } else {
                preference.setSummary(stringValue);
            }
        }
    }

    private static class ExtraSetSummaryOnChangeListener extends SimpleSetSummaryOnChangeListener {
        private final Preference.OnPreferenceChangeListener preferenceChangeListener;

        public ExtraSetSummaryOnChangeListener(Preference.OnPreferenceChangeListener preferenceChangeListener) {
            this.preferenceChangeListener = preferenceChangeListener;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean result = preferenceChangeListener.onPreferenceChange(preference, value);
            if (result) {
                return super.onPreferenceChange(preference, value);
            }
            return false;
        }
    }

    private static final SimpleSetSummaryOnChangeListener sBindPreferenceSummaryToValueListener = new SimpleSetSummaryOnChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SciousApplication.ACTION_QUIT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        for (String prefKey : getPreferenceKeysWithSummary()) {
            final Preference pref = findPreference(prefKey);
            if (pref != null) {
                bindPreferenceSummaryToValue(pref);
            } else {
                Log.e(TAG, "Unknown preference key: " + prefKey + ", unable to display value");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    protected String[] getPreferenceKeysWithSummary() {
        return new String[0];
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        SimpleSetSummaryOnChangeListener listener = null;
        Preference.OnPreferenceChangeListener existingListener = preference.getOnPreferenceChangeListener();
        if (existingListener != null) {
            listener = new ExtraSetSummaryOnChangeListener(existingListener);
        } else {
            listener = sBindPreferenceSummaryToValueListener;
        }
        preference.setOnPreferenceChangeListener(listener);

        try {
            listener.updateSummary(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        } catch (ClassCastException e) {
            listener.updateSummary(preference, preference.getSummary());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
