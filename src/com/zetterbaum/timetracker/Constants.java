package com.zetterbaum.timetracker;

import android.content.Context;

public enum Constants {
    FROM_DATE, TO_DATE;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static void initializeConstants(Context context) {
        FROM_DATE.setValue(context.getString(R.string.from_date));
        TO_DATE.setValue(context.getString(R.string.to_date));
    }
}
