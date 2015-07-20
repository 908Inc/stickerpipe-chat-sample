package vc.s908.stickerpipe_chat_sample.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper incapsulate work with preferences
 *
 * @author Dmytriy Nezhydenko
 */
abstract class PreferenceHelper {

    private SharedPreferences prefs;

    /**
     * Create helper instance and instantiate default shared preferences
     *
     * @param context preference context
     */
    protected PreferenceHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Return sting value by string key from preferences
     *
     * @param key          Preference key
     * @return string value by key
     */

    protected String getStringValue(String key) {
        return getStringValue(key, null);
    }
    /**
     * Return sting value by string key from preferences
     *
     * @param key          Preference key
     * @param defaultValue Default string value
     * @return string value by key
     */
    protected String getStringValue(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    /**
     * Return int value by string key from preferences.
     * Return 0 if not set yet
     *
     * @param key preference key
     * @return int value by key
     */

    protected int getIntValue(String key) {
        return getIntValue(key, 0);
    }

    /**
     * Return int value by string key from preferences.
     * Return default value if not set yet
     *
     * @param key          Preference key
     * @param defaultValue Default value
     * @return int value by key
     */

    protected int getIntValue(String key, int defaultValue) {
        String stringValue = getStringValue(key);
        if (stringValue == null) {
            return defaultValue;
        } else {
            return Integer.valueOf(stringValue);
        }
    }

    /**
     * Return long value by string key from preferences.
     * Return 0 if not set yet
     *
     * @param key preference key
     * @return long value by key
     */

    protected long getLongValue(String key) {
        String stringValue = getStringValue(key);
        if (stringValue == null) {
            return 0;
        } else {
            return Long.valueOf(stringValue);
        }
    }


    /**
     * Return boolean value by string key from preferences
     *
     * @param key preference key
     * @return boolean value by key
     */

    protected boolean getBooleanValue(String key) {
        return getBooleanValue(key, false);
    }

    /**
     * Return sting value by string key from preferences.
     * If preference by key not exists, return default value
     *
     * @param key          preference key
     * @param defaultValue default value
     * @return boolean value by key or default value
     */
    protected boolean getBooleanValue(String key, boolean defaultValue) {
        String stringValue = getStringValue(key);
        if (stringValue == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(stringValue);
        }
    }

    /**
     * Store value with key
     *
     * @param key   string key
     * @param value value for store
     * @param <U>   value object class
     */
    protected <U> void storeValue(String key, U value) {
        String resultValue = String.valueOf(value);
        prefs.edit().putString(key, resultValue).apply();
    }

    /**
     * Remove preference entry by key
     *
     * @param key entry key for remove
     */
    protected void removeValue(String key) {
        prefs.edit().remove(key).apply();
    }

}
