package vc.s908.stickerpipe_chat_sample;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import vc.s908.stickerpipe_chat_sample.ui.ShopActivity;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.User;
import vc908.stickerfactory.billing.Prices;
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
        StickersManager.initialize("cf524cb1eba30e438d575453808b228c", this);

        // set user neta info
        Map<String, String> meta = new HashMap<>();
        meta.put(User.KEY_GENDER, User.GENDER_FEMALE);
        meta.put(User.KEY_AGE, "33");
        // Put your user id when you know it
        StickersManager.setUser(Utils.getDeviceId(this), meta);
        // Set sender id for push notifications
        GcmManager.setGcmSenderId(this, "86472317986");
        // Set push notification listener
        GcmManager.setPushNotificationManager(new PushNotificationManager());
        // Set custom shop class
        StickersManager.setShopClass(ShopActivity.class);
        // Set prices
        StickersManager.setPrices(new Prices()
                        .setPricePointB("$0.99", 9.99f)
                        .setPricePointC("$1.99", 19.99f)
                // sku used for inapp purchases
//                .setSkuB("pack_b")
//                .setSkuC("pack_c")
        );
        // licence key for inapp purchases
        StickersManager.setLicenseKey("YOUR LICENCE KEY");
    }
}
