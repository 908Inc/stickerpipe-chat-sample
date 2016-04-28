package vc.s908.stickerpipe_chat_sample;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import vc.s908.stickerpipe_chat_sample.ui.MainActivity;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class PushNotificationManager extends vc908.stickerpipe.gcmintegration.NotificationManager {


    @Override
    public int getColorNotificationIcon() {
        return R.drawable.ic_launcher;
    }

    @Override
    public int getBwNotificationIcon() {
        return R.drawable.ic_launcher;
    }

    @NonNull
    @Override
    public Intent createNotificationIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }
}
