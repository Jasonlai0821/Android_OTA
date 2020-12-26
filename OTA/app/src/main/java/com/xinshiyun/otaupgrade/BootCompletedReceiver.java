package com.xinshiyun.otaupgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeService;

public class BootCompletedReceiver extends BroadcastReceiver {
    private final static String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.d(TAG,"BOOT_COMPLETED onReceive");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            startUpgradeService(context);

        }else if(intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)){
            startUpgradeService(context);
        }
    }

    private void startUpgradeService(Context context)
    {
        Intent upgradeintent = new Intent(context, OTAUpgradeService.class);
        context.startService(upgradeintent);
    }
}
