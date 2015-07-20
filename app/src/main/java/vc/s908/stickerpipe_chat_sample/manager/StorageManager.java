package vc.s908.stickerpipe_chat_sample.manager;

import android.content.Context;
import android.support.annotation.ColorRes;

import vc.s908.stickerpipe_chat_sample.R;


/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class StorageManager extends PreferenceHelper {
    private static final String KEY_IS_STICKER_USED = "key_is_sticker_used";
    private static final String KEY_PRIMARY_COLOR = "key_primary_color";
    private static final String KEY_PRIMARY_LIGHT_COLOR = "key_primary_light_color";
    private static final String KEY_PRIMARY_DARK_COLOR = "key_primary_dark_color";
    private static StorageManager instance;

    public static StorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new StorageManager(context);
        }
        return instance;
    }

    /**
     * Create helper instance and instantiate default shared preferences
     *
     * @param context preference context
     */
    private StorageManager(Context context) {
        super(context);
    }

    public void storeIsStickerUsed(boolean value) {
        storeValue(KEY_IS_STICKER_USED, value);
    }

    public boolean isStickerUsed() {
        return getBooleanValue(KEY_IS_STICKER_USED);
    }

    public void storePrimaryColor(@ColorRes int color) {
        storeValue(KEY_PRIMARY_COLOR, color);
    }

    public int getPrimaryColor() {
        return getIntValue(KEY_PRIMARY_COLOR, R.color.primary);
    }

    public void storePrimaryLightColor(@ColorRes int color) {
        storeValue(KEY_PRIMARY_LIGHT_COLOR, color);
    }

    public int getPrimaryLightColor() {
        return getIntValue(KEY_PRIMARY_LIGHT_COLOR, R.color.primary_light);
    }

    public void storePrimaryDarktColor(@ColorRes int color) {
        storeValue(KEY_PRIMARY_DARK_COLOR, color);
    }

    public int getPrimaryDarkColor() {
        return getIntValue(KEY_PRIMARY_DARK_COLOR, R.color.primary_dark);
    }}
