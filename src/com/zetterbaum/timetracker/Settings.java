package com.zetterbaum.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private static String currentTimeFormat = "ampm";

    public static String getCurrentTimeFormat() {
        return currentTimeFormat;
    }

    public static void initializeCurrentTimeFormatSetting(Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String pref = prefs.getString("timeformat", "unset");
//        if ("unset".equals(pref)) {
//            SharedPreferences.Editor editor = prefs.edit();
//            pref = "ampm";
//            editor.putString("timeformat", pref);
//            editor.commit();
//        }
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
