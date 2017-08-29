package com.mytpg.program.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.mytpg.engines.settings.AppSettings;
import com.mytpg.program.MainActivity;
import com.mytpg.program.R;

import java.util.Locale;


/**
 * Created by stalker-mac on 17.11.16.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    SharedPreferences sharedPreferences;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getMainActivity() != null)
        {
            getMainActivity().setTitle(getString(R.string.action_settings));
            getMainActivity().updateFabVisibility(false);
        }

        loadData();
    }

    private void loadData() {
        loadCheckBoxPrefData(AppSettings.PREF_ALARM_TICKET_ACTIVE, true);

        loadListPrefData(AppSettings.PREF_ALARM_TICKET_TIME, "5");
        loadListPrefData(AppSettings.PREF_ALARM_DEPARTURES_TIME, "5");
        loadListPrefData(AppSettings.PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    private void loadListPrefData(String argKey, String argDefaultValue) {
        Preference pref = findPreference(argKey);
        if (pref != null && pref instanceof ListPreference)
        {
            ListPreference listPref = (ListPreference) findPreference(argKey);
            int prefIndex = argDefaultValue.equalsIgnoreCase("-1") ? 0 : listPref.findIndexOfValue(sharedPreferences.getString(argKey, argDefaultValue));
            if (prefIndex >= 0) {
                listPref.setSummary(listPref.getEntries()[prefIndex]);
            }
        }
    }

    private void loadCheckBoxPrefData(String argKey, boolean argDefaultValue)
    {
        Preference pref = findPreference(argKey);
        if (pref != null && pref instanceof CheckBoxPreference)
        {
            CheckBoxPreference alarmTicketActivePref = (CheckBoxPreference)findPreference(argKey);
            alarmTicketActivePref.setChecked(sharedPreferences.getBoolean(argKey, true));
            checkBoxChanged(alarmTicketActivePref);
        }
    }

    private void checkBoxChanged(CheckBoxPreference argChBPref) {
        if (argChBPref.getKey().equalsIgnoreCase(AppSettings.PREF_ALARM_TICKET_ACTIVE))
        {
            Preference alarmTicketTimePref = findPreference(AppSettings.PREF_ALARM_TICKET_TIME);
            alarmTicketTimePref.setEnabled(argChBPref.isChecked());
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    public MainActivity getMainActivity()
    {
        if (!isAdded())
        {
            return null;
        }

        if (getActivity() instanceof MainActivity)
        {
            return (MainActivity)getActivity();
        }

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            loadListPrefData(preference.getKey(), sharedPreferences.getString(key,""));
            if (preference.getKey().equalsIgnoreCase(AppSettings.PREF_LANGUAGE))
            {
                if (getMainActivity() != null)
                {
                    getMainActivity().changeLanguage(true);
                }
            }
        }
        else if (preference instanceof CheckBoxPreference)
        {
            checkBoxChanged((CheckBoxPreference) preference);
        }
        else
        {
            //preference.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
