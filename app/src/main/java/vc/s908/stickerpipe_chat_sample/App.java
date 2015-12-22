package vc.s908.stickerpipe_chat_sample;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.User;
import vc908.stickerfactory.utils.Utils;
import vc908.stickerpipe.gcmintegration.GcmManager;

/**
 * Created by Dmitry Nezhydenko
 * Date 4/7/15
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StickersManager.setLoggingEnabled(true);
        StickersManager.initialize("72921666b5ff8651f374747bfefaf7b2", this);
        Map<String, String> meta = new HashMap<>();
        meta.put(User.KEY_GENDER, User.GENDER_FEMALE);
        meta.put(User.KEY_AGE, "33");
        StickersManager.setUser(Utils.getDeviceId(this), meta);
        StickersManager.setUserSubscribed(false);
        GcmManager.setGcmSenderId(this, "86472317986");
    }
}
