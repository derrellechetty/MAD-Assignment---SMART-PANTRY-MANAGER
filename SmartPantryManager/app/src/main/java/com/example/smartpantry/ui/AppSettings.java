package com.example.smartpantry.ui;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the two user settings. */
public final class AppSettings {

    private static final String PREFS = "smart_pantry_prefs";
    private static final String KEY_EXPIRING_ALERTS = "expiring_alerts";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired";

    /** Items expiring within this many days trigger the banner. */
    public static final int EXPIRING_SOON_DAYS = 3;

    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private AppSettings() { }

    public static String defaultUnit(Context c) {
        return prefs(c).getString(KEY_DEFAULT_UNIT, "g");
    }

    public static void setDefaultUnit(Context c, String unit) {
        prefs(c).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean expiringAlertsEnabled(Context c) {
        return prefs(c).getBoolean(KEY_EXPIRING_ALERTS, true);
    }

    public static void setExpiringAlerts(Context c, boolean on) {
        prefs(c).edit().putBoolean(KEY_EXPIRING_ALERTS, on).apply();
    }

    public static boolean ignoreExpired(Context c) {
        return prefs(c).getBoolean(KEY_IGNORE_EXPIRED, true);
    }

    public static void setIgnoreExpired(Context c, boolean on) {
        prefs(c).edit().putBoolean(KEY_IGNORE_EXPIRED, on).apply();
    }
}
