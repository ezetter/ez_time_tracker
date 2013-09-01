package com.zetterbaum.timetracker;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private static String currentTimeFormat = "ampm";

    public static int getPunchDialogFontSize (Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("punch_font", "20"));
    }

    public static String getPunchOrder (Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("punch_sort_by", "category_name");
    }

    public static String getCurrentTimeFormat() {
        return currentTimeFormat;
    }

    public static void initializeCurrentTimeFormatSetting(Context context) {
        currentTimeFormat = getOrInitializeSetting(context, "timeformat", "ampm");
        DateTimeFormatter.initializeCurrentTimeFormat();
    }

    public static String getOrInitializeSetting(Context context, String setting, String defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(setting, "unset");
        if ("unset".equals(value)) {
            value = defaultValue;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(setting, defaultValue);
            editor.commit();
        }
        return value;
    }

}
