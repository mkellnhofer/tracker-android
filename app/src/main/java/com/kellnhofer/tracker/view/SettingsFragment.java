package com.kellnhofer.tracker.view;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerSettings;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set parent activity as callback
        try {
            mActivity = (SettingsActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + SettingsActivity.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        initServerUrl();
    }

    private void initServerUrl() {
        Preference preference = findPreference(TrackerSettings.PREF_KEY_SERVER_URL);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mActivity.onServerUrlClicked();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        initPreferences(getPreferenceScreen().getSharedPreferences(), getPreferenceScreen());
        initSummary(getPreferenceScreen());

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset callback
        mActivity = null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(sharedPreferences, findPreference(key));
        updateSummary(findPreference(key));
    }

    // --- Methods called from the activity ---

    public void updatePreferenceSummary(String key, String summary) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setSummary(summary);
        }
    }

    // --- Helper methods ---

    private static void initPreferences(SharedPreferences sharedPreferences, Preference preference) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) preference;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initPreferences(sharedPreferences, pGrp.getPreference(i));
            }
        } else {
            updatePreference(sharedPreferences, preference);
        }
    }

    private static void updatePreference(SharedPreferences sharedPreferences, Preference preference) {
        if (preference instanceof SwitchPreference) {
            boolean isChecked = sharedPreferences.getBoolean(preference.getKey(), false);
            SwitchPreference p = (SwitchPreference) preference;
            p.setChecked(isChecked);
        }
    }

    private static void initSummary(Preference preference) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) preference;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updateSummary(preference);
        }
    }

    private static void updateSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            preference.setSummary(((ListPreference) preference).getEntry());
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(((EditTextPreference) preference).getText());
        }
    }

}
