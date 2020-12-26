package com.xinshiyun.otaupgrade.upgrade;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.xinshiyun.otaupgrade.upgrade.misc.NetUtils;

import java.util.List;

/**
 * Created by SKY300581 on 2018/1/5.
 */

public class OTAUpgradeManager {
    private static final String TAG = OTAUpgradeManager.class.getSimpleName();

    public static final class MSG{
        private static final int MSG_ON_OTA_IDLE = 0x0000;
        private static final int MSG_ON_OTA_QUERYREQUEST = MSG_ON_OTA_IDLE +1;
        private static final int MSG_ON_OTA_STARTDOWNLOAD = MSG_ON_OTA_QUERYREQUEST +1;
        private static final int MSG_ON_OTA_STOPTDOWNLOAD = MSG_ON_OTA_STARTDOWNLOAD +1;
        private static final int MSG_ON_OTA_STARTINSTALL = MSG_ON_OTA_STOPTDOWNLOAD +1;
        private static final int MSG_ON_OTA_STOPINSTALL = MSG_ON_OTA_STARTINSTALL +1;
    }

    public static final class OTASTATE{
        public static final int OTA_IDLE = 0x0000;
        public static final int OTA_QUERYREQUEST_SUCCESS = OTA_IDLE +1;
        public static final int OTA_QUERYREQUEST_FAILED= OTA_QUERYREQUEST_SUCCESS +1;
        public static final int OTA_DOWNLOADING = OTA_QUERYREQUEST_FAILED +1;
        public static final int OTA_DOWNLOADSUCCESS = OTA_DOWNLOADING +1;
        public static final int OTA_DOWNLOADFAILED= OTA_DOWNLOADSUCCESS +1;
        public static final int OTA_INSTALLING= OTA_DOWNLOADFAILED +1;
        public static final int OTA_INSTALLSUCCESS= OTA_INSTALLING +1;
        public static final int OTA_INSTALLFAILED= OTA_INSTALLSUCCESS +1;
    }
    private static OTAUpgradeManager mOTAUpgradeManager = null;
    private static String INTENT_OTA_SERVICE = "com.xsy.otaupgrade.upgrade.OTAUpgradeService";
    private static String INTENT_OTA_CATEGORY = "com.xsy.otaupgrade.upgrade.default";

    public Context mContext;
    private IOTAUpgradeBinder mOTABinder;
    private ConnectionCallback mConnectionCallback;
    private OTAUpgradeInfoTTSCallback mUpgradeInfoTTSListener = null;
    private OTAUpgradeNofityUICallback mOTAUpgradeNofityUICallback = null;
    private OTAUpgradeListener mOTAUpgradeCallback = null;
    private static long downloadid = -1;


    public OTAUpgradeManager(Context context){
        Log.d(TAG,"OTAUpgradeManager()");
        this.mContext = context;
        mOTABinder = null;
        initOTAUpgrade();
    }

    public synchronized static OTAUpgradeManager getInstance(Context context) {
        Log.d(TAG,"OTAUpgradeManager getInstance()");
        if (mOTAUpgradeManager == null) {
            mOTAUpgradeManager = new OTAUpgradeManager(context);
        }

        return mOTAUpgradeManager;
    }

    public synchronized static void deInstance()
    {
        mOTAUpgradeManager = null;
    }

    void initOTAUpgrade()
    {
        Log.d(TAG,"initOTAUpgrade()");
        mOTAUpgradeCallback = new OTAUpgradeListener();
        mConnectionCallback = new ConnectionCallback();

        final Intent exlIntent = createExplicitIntentForSpecifiedService();
        if(exlIntent != null){
            mContext.bindService(exlIntent,mConnectionCallback, Context.BIND_AUTO_CREATE);
            Log.d(TAG,"initOTAUpgrade() bind OTAUpgradeService");
        }else{
            Log.d(TAG,"initOTAUpgrade() please check why OTAUpgradeService not existed!!");
        }
    }

    private Intent createExplicitIntentForSpecifiedService() {
        final Intent intent = new Intent();
        intent.setAction(INTENT_OTA_SERVICE);
        intent.addCategory(INTENT_OTA_CATEGORY);
        final Intent explicitIntent = new Intent(createExplicitFromImplicitIntent(mContext, intent));
        Log.d(TAG, "createExplicitIntentForSpecifiedService explicitIntent");
        return explicitIntent;
    }

    private Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent,0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            Log.d(TAG, "createExplicitFromImplicitIntent failed, please check!!!!!!!");
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    private class ConnectionCallback implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"ConnectionCallback onServiceConnected()");
            mOTABinder = IOTAUpgradeBinder.Stub.asInterface(service);
            try{
                mOTABinder.registerOTAUpgradeCallback(mOTAUpgradeCallback);
            }catch (RemoteException e){
                e.printStackTrace();
            }

            if(NetUtils.getNetWorkState(mContext) == NetUtils.NETWORK_NONE){
                if(mOTAUpgradeCallback != null) {
                    try {
                        mOTAUpgradeCallback.onOTARequestFailure();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onCreate() NetWorkState is NETWORK_NONE");
            }
            startOTAQueryRequest();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"ConnectionCallback onServiceDisconnected()");
            try{
                mOTABinder.unregisterOTAUpgradeCallback(mOTAUpgradeCallback);
                mOTABinder = null;
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
    }

    private final class OTAUpgradeListener extends IOTAUpgradeCallback.Stub{
        @Override
        public void onOTARequestSuccess(OTAUpgradeInfo info) throws RemoteException {
            Log.d(TAG,"onOTARequestSuccess() info:" + info.toString());
            //notify UI for user's interaction
            //sendOTAMSG(MSG.MSG_ON_OTA_STARTDOWNLOAD);
            if(mUpgradeInfoTTSListener != null){
                String text = "检测到新版本，即将进入下载，请勿断电！";
                mUpgradeInfoTTSListener.onSpeakOfflineText(text);
            }

            if(mOTAUpgradeNofityUICallback != null){
                mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_QUERYREQUEST_SUCCESS,0,info);
            }
        }

        @Override
        public void onOTARequestFailure() throws RemoteException {
            Log.d(TAG,"onOTARequestFailure()");
            //notify UI for user's interaction
            if(mUpgradeInfoTTSListener != null){
                String text = "未检测到新版本！";
                mUpgradeInfoTTSListener.onSpeakOfflineText(text);
            }

            if(mOTAUpgradeNofityUICallback != null){
                mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_QUERYREQUEST_FAILED,0,null);
            }
        }

        @Override
        public void onDownloadState(long downloadid, int state) throws RemoteException {
            Log.d(TAG,"onDownloadState() downloadid:" +downloadid + " state:"+state);
            //notify UI for user's interaction
            if(state == DownloadManager.STATUS_SUCCESSFUL){
                //sendOTAMSG(MSG.MSG_ON_OTA_STARTINSTALL);
                Log.d(TAG,"onDownloadState() Download sucessful");
                if(mUpgradeInfoTTSListener != null){
                    String text = "下载成功，即将重启更新！";
                    mUpgradeInfoTTSListener.onSpeakOfflineText(text);
                }

                if(mOTAUpgradeNofityUICallback != null){
                    mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_DOWNLOADSUCCESS,100,null);
                }
            }else if(state == DownloadManager.STATUS_FAILED){
                Log.d(TAG,"onDownloadState() Download failed");
                if(mUpgradeInfoTTSListener != null){
                    String text = "下载失败！";
                    mUpgradeInfoTTSListener.onSpeakOfflineText(text);
                }
                if(mOTAUpgradeNofityUICallback != null){
                    mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_DOWNLOADFAILED,100,null);
                }
            }
        }

        @Override
        public void onDownloadprogress(long downloadid, int percentage) throws RemoteException {
            Log.d(TAG,"onDownloadprogress() downloadid:" +downloadid + " percentage:"+percentage);
            //notify UI for user's interaction
            if(mOTAUpgradeNofityUICallback != null){
                mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_DOWNLOADING,percentage,null);
            }
        }

        @Override
        public void onSuccess() throws RemoteException {
            Log.d(TAG,"onSuccess()");
            //notify UI for user's interaction
            if(mOTAUpgradeNofityUICallback != null){
                mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_INSTALLING,0,null);
            }
        }

        @Override
        public void onFailure() throws RemoteException {
            Log.d(TAG,"onFailure()");
            //notify UI for user's interaction
            if(mUpgradeInfoTTSListener != null){
                String text = "更新失败！";
                mUpgradeInfoTTSListener.onSpeakOfflineText(text);
            }
            if(mOTAUpgradeNofityUICallback != null){
                mOTAUpgradeNofityUICallback.onNofityState(OTASTATE.OTA_INSTALLFAILED,0,null);
            }
        }
    }

    public void startOTAQueryRequest()
    {
        Log.d(TAG,"startOTAQueryRequest()");
        try{
            if(mOTABinder != null){
                mOTABinder.startOTAQueryRequest();
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public void startDownload()
    {
        Log.d(TAG,"startDownload()");
        try{
            if(mOTABinder != null){
                downloadid = mOTABinder.startDownload();
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public void stopDownload()
    {
        Log.d(TAG,"stopDownload()");
        try{
            if(mOTABinder != null && downloadid != -1){
                mOTABinder.stopDownload(downloadid);
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public void startInstall()
    {
        Log.d(TAG,"startInstall()");
        try{
            if(mOTABinder != null){
                //default path:/mnt/internal_sd/Download/update_***.zip,support:filepath ="" or null
                //String path = "/data/upgrade_20200612.zip";
                mOTABinder.startInstall(null);
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public void stopInstall()
    {
        Log.d(TAG,"stopInstall()");
        try{
            if(mOTABinder != null){
                mOTABinder.stopInstall();
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public void setOTAUpgradeInfoTTSCallback(OTAUpgradeInfoTTSCallback callback)
    {
        mUpgradeInfoTTSListener = callback;
    }

    public interface OTAUpgradeInfoTTSCallback{
        void onSpeakOfflineText(String text);
    }

    public void setOTAUpgradeNofityUICallback(OTAUpgradeNofityUICallback callback)
    {
        mOTAUpgradeNofityUICallback = callback;
    }

    public interface OTAUpgradeNofityUICallback{
        void onNofityState(int state, int percentage, OTAUpgradeInfo info);
    }
}
