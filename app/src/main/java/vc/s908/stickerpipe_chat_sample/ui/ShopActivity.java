package vc.s908.stickerpipe_chat_sample.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import vc.s908.stickerpipe_chat_sample.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.billing.PricePoint;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class ShopActivity extends vc908.stickerfactory.ui.activity.ShopWebViewActivity {
    @Override
    protected void onPurchase(String packTitle, final String packName, PricePoint pricePoint) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Charge dialog")
                .setMessage("Purchase " + packTitle + " for " + pricePoint.getLabel() + "?")
                .setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        StickersManager.onPackPurchased(packName);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onPurchaseFail();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                int primaryColor = ContextCompat.getColor(ShopActivity.this, R.color.primary);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(primaryColor);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primaryColor);
            }
        });
        dialog.show();
    }
}
