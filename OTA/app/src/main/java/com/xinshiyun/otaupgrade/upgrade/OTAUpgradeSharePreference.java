package com.xinshiyun.otaupgrade.upgrade;

import android.content.Context;
import android.content.SharedPreferences;

public class OTAUpgradeSharePreference {

    private static OTAUpgradeInfo mOTAUpgradeInfo = null;

    public static long getSysDownloadId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getLong("sys", -1);
    }

    public static void setSysDownloadId(Context context, long id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("sys", id);
        editor.commit();
    }

    public static String getLocalInstallImage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getString("localInstallImage", "");
    }

    public static void setLocalInstallImage(Context context, String path) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("localInstallImage", path);
        editor.commit();
    }

    public static void setSysUpgradeExist(Context context, int isExist) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("exist", isExist);
        editor.commit();
    }

    public static int getSysUpgradeExist(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getInt("exist", 0);
    }

    public static OTAUpgradeInfo getOTAUpgradeInfo(Context context) {
        if (mOTAUpgradeInfo == null) {
            mOTAUpgradeInfo = new OTAUpgradeInfo();
            SharedPreferences sp = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
            mOTAUpgradeInfo.setVersion(sp.getString("updateInfo_Version",""));
            mOTAUpgradeInfo.setMd5(sp.getString("updateInfo_Md5",""));
            mOTAUpgradeInfo.setFilesize(sp.getString("updateInfo_Filesize",""));
            mOTAUpgradeInfo.setRemark(sp.getString("updateInfo_Remark",""));
            mOTAUpgradeInfo.filePath = sp.getString("updateInfo_filePath", "");
        }
        return mOTAUpgradeInfo;
    }

    public static void setOTAUpgradeInfo(Context context, OTAUpgradeInfo upgradeInfo) {
        mOTAUpgradeInfo = upgradeInfo;
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("updateInfo_Version", upgradeInfo.getVersion());
        editor.putString("updateInfo_Md5", upgradeInfo.getMd5());
        editor.putString("updateInfo_Filesize", upgradeInfo.getFilesize());
        editor.putString("updateInfo_Remark", upgradeInfo.getRemark());
        editor.putString("updateInfo_filePath", upgradeInfo.filePath);
        editor.commit();
    }

    public static void setForceInstall(Context context, Boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("update_isForceInstall", b);
        editor.commit();
    }

    public static boolean getForceInstall(Context context) {
        SharedPreferences sp = context.getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean("update_isForceInstall", false);
    }
}
