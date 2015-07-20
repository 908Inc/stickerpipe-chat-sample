package vc.s908.stickerpipe_chat_sample;

import android.app.Application;

import vc908.stickerfactory.StickersManager;

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
    }
}
