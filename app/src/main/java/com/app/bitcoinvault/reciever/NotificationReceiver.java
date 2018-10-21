package com.app.bitcoinvault.reciever;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.Utils;
import com.app.bitcoinvault.view.activity.FingerprintAuthenticationActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import utils.SDKUtils;

import static com.app.bitcoinvault.util.IAppConstant.FROM_NOTIFICATION;
import static com.app.bitcoinvault.util.IAppConstant.NOTIFICATION_RECIEVER;

/**
 * Created by admin on 21-07-2017.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private String NOTIFICATION_FILTER = "com.embedded.download.intent.action.Notification";
    private String BUNDLE_DATA = "bundle_data";
    private String MESSAGE = "message";
    private String TAG = NotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NOTIFICATION_FILTER)) {
            Bundle mBundle = intent.getBundleExtra(BUNDLE_DATA);
            String mMessage = mBundle.getString(MESSAGE);
            SDKUtils.showLog(TAG, "Yipeee----Notification Message : " + mMessage);
            try {
                JSONObject mainObject = new JSONObject(mMessage);
                if (mainObject.has("tag") && mainObject.getString("tag").equalsIgnoreCase("bitcoin_transaction")) {
                    sendNotification(context, mainObject.getString("sender_address"), mainObject.getString("receiver_address"), mainObject.getString("bitcoins"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * this method is used to genrate notifcation
     *
     * @param context context
     * @param bitcoin Notifcation message
     */
    private void sendNotification(Context context, String sender, String rec, String bitcoin) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        double bit = Double.parseDouble(bitcoin) / 100000000;

        String mMessage = AppPreferences.getInstance(context).getWalletNameByWalletAdd(rec)+" has received " + Utils.convertDecimalFormatPattern(bit) + " BTC from " + AppPreferences.getInstance(context).getWalletNameByWalletAdd(sender);

        Intent intent = null;
        intent = new Intent(context, FingerprintAuthenticationActivity.class);
        intent.putExtra(FROM_NOTIFICATION, true);
        intent.putExtra(NOTIFICATION_RECIEVER, rec);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pendingIntent = PendingIntent.getActivity(context, m, intent, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon_lolipop)
                .setContentTitle("Bitcoin Vault")
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mMessage))
                .setContentText(mMessage).setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setColor(Color.parseColor("#ED6708"));
        }
        notificationManager.notify(m, mBuilder.build());
    }
}
