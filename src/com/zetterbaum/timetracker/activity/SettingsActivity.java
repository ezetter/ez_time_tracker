package com.zetterbaum.timetracker.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Window;
import com.zetterbaum.timetracker.FileUtilities;
import com.zetterbaum.timetracker.R;
import com.zetterbaum.timetracker.Settings;

public class SettingsActivity extends PreferenceActivity {
    private static final String TIME_FORMAT_PREF_KEY = "timeformat";
    private static final String OUTPUT_DIR_PREF_KEY = "external_storage_loc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Preference timePref = findPreference(TIME_FORMAT_PREF_KEY);
        updateView(timePref);
        initializePreference(timePref);
        final Preference outputDirPref = findPreference(OUTPUT_DIR_PREF_KEY);
        updateView(outputDirPref);
        FileUtilities fileUtilities = new FileUtilities(this);
        String exportDir = fileUtilities.getExportLocation();
        outputDirPref.setDefaultValue(exportDir);
        initializePreference(outputDirPref);
    }

    private void initializePreference(Preference preference) {
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences customSharedPreference = preference.getSharedPreferences();
                SharedPreferences.Editor editor = customSharedPreference.edit();
                editor.putString(preference.getKey(), (String) newValue);
                editor.commit();
                updateView(preference);
                return true;
            }
        });
    }

    private void updateView(Preference pref) {
        if (pref.getKey().equals(TIME_FORMAT_PREF_KEY)) {
            Settings.initializeCurrentTimeFormatSetting(getBaseContext());
            String[] settingValues = getResources().getStringArray(R.array.timeFormatValues);
            String[] settingEntries = getResources().getStringArray(R.array.timeFormatEntries);
            String currSetting;
            if (settingValues[0].equals(Settings.getCurrentTimeFormat())) {
                currSetting = settingEntries[0];
            } else {
                currSetting = settingEntries[1];
            }
            String descriptionText = getResources().getString(R.string.time_format_description_text);
            pref.setSummary(descriptionText + "\n(currently " + currSetting + ")");
        } else if (pref.getKey().equals(OUTPUT_DIR_PREF_KEY)) {
            FileUtilities fileUtilities = new FileUtilities(this);
            String descriptionText = getResources().getString(R.string.export_location_setting_description_text);
            pref.setSummary(descriptionText + "\n(currently /" + fileUtilities.getExportLocation() + ")");
        }
    }

}
