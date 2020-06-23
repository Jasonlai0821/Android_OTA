package com.xinshiyun.otaupgrade.upgrade.install;

import android.content.Context;
import android.os.RecoverySystem;
import android.util.Log;

import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeSharePreference;
import com.xinshiyun.otaupgrade.upgrade.misc.MD5Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by SKY300581 on 2018/1/5.
 */

public class InstallManager {
    private final static String TAG = InstallManager.class.getSimpleName();

    private Context mContext;
    private InstallManagerCallback mInstallManagerListener = null;

    public InstallManager(Context context){
        this.mContext = context;
        Log.d(TAG, "InstallManager()");
    }

    private boolean checkUpgradeFileValid(String path)
    {
        boolean ret = false;
        String dlfileMD5 = "";

        File file = new File(path);
        try {
            dlfileMD5 = MD5Utils.getFileMD5String(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ret = dlfileMD5.equalsIgnoreCase(OTAUpgradeSharePreference.getOTAUpgradeInfo(mContext).getMd5());
        Log.d(TAG, "checkUpgradeFileValid() ret ="+ ret);
        return ret;
    }

    public synchronized void startInstall(String path)
    {
        Log.d(TAG, "startInstall()");

        if(checkUpgradeFileValid(path)){
            installUpgrade(path);
        }else{
            Log.d(TAG, "startInstall failed()");
            if(mInstallManagerListener != null){
                Log.d(TAG, "startInstall failed onInstallFailure()");
                mInstallManagerListener.onInstallFailure();
            }
        }
    }

    public synchronized boolean installUpgrade(String path)
    {
        boolean ret = false;
        Log.d(TAG, "installUpgrade()");
        try {
            File imgefile = new File(path);
            if(mInstallManagerListener != null){
                Log.d(TAG, "installUpgrade success()");
                mInstallManagerListener.onInstallSuccess();
            }
            RecoverySystem.installPackage(mContext, imgefile);
            ret = true;
        }catch (Exception e){
            if(mInstallManagerListener != null){
                Log.d(TAG, "installUpgrade failed()");
                mInstallManagerListener.onInstallFailure();
            }
            e.printStackTrace();
        }

        return ret;
    }

    public void setInstallManagerCallback(InstallManagerCallback callback){
        Log.d(TAG, "setInstallManagerCallback()");
        this.mInstallManagerListener = callback;
    }

    public interface InstallManagerCallback{
        void onInstallSuccess();

        void onInstallFailure();
    }
}
