package com.xinshiyun.otaupgrade.upgrade;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.xinshiyun.otaupgrade.upgrade.misc.NetUtils;

public class OTAUpgradeService extends Service {
    private final static String TAG = OTAUpgradeService.class.getSimpleName();

    public final RemoteCallbackList<IOTAUpgradeCallback> mOTAUpgradeCallbacks = new RemoteCallbackList<IOTAUpgradeCallback>();

    private static OTAUpgradeServiceImp mOTAUpgradeServiceImp = null;
    private static long currentDownloadId = -1;
    private static String DownloadPath = "";
    private OTAUpgrade mBinder;
    private OTAUpgradeImpListener mOTAUpgradeImpListener = null;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        mBinder = new OTAUpgrade();

        mOTAUpgradeServiceImp = OTAUpgradeServiceImp.getInstance(this);
        mOTAUpgradeImpListener = new OTAUpgradeImpListener();
        mOTAUpgradeServiceImp.setOTAUpgradeServiceCallback(mOTAUpgradeImpListener);

        if(NetUtils.getNetWorkState(this) == NetUtils.NETWORK_NONE){
            if(mOTAUpgradeImpListener != null)
                mOTAUpgradeImpListener.onOTAQueryResultFailure();
            Log.d(TAG, "onCreate() NetWorkState is NETWORK_NONE");
        }else{
            Log.d(TAG, "onCreate() onStartOTAQueryRequest");
            onStartOTAQueryRequest();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        mOTAUpgradeServiceImp.setOTAUpgradeServiceCallback(null);
        getCurrentDownloadId();
        if(currentDownloadId != -1){
            mOTAUpgradeServiceImp.onStopDownloadProgress();
        }

        mOTAUpgradeServiceImp = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    public class OTAUpgrade extends IOTAUpgradeBinder.Stub{
        @Override
        public void registerOTAUpgradeCallback(IOTAUpgradeCallback callback) throws RemoteException {
            Log.d(TAG, "registerOTAUpgradeCallback()");
            if (callback != null) {
                mOTAUpgradeCallbacks.register(callback);
            }
        }

        @Override
        public void unregisterOTAUpgradeCallback(IOTAUpgradeCallback callback) throws RemoteException {
            Log.d(TAG, "unregisterOTAUpgradeCallback()");
            if (callback != null) {
                mOTAUpgradeCallbacks.unregister(callback);
            }
        }

        @Override
        public void startOTAQueryRequest() throws RemoteException {
            Log.d(TAG, "startOTAQueryRequest()");
            onStartOTAQueryRequest();
        }

        @Override
        public int startDownload() throws RemoteException {
            Log.d(TAG, "startDownload()");
            return onStartDownload();
        }

        @Override
        public void stopDownload(long downloadId) throws RemoteException {
            Log.d(TAG, "stopDownload() downloadId:"+downloadId);
            onStopDownload(downloadId);
        }

        @Override
        public void startInstall(String upgradefile) throws RemoteException {
            Log.d(TAG, "startInstall()");
            onStartInstall(upgradefile);
        }

        @Override
        public void stopInstall() throws RemoteException {
            Log.d(TAG, "stopInstall()");
            onStopInstall();
        }
    }

    public class OTAUpgradeImpListener implements OTAUpgradeServiceImp.OTAUpgradeServiceCallback{
        @Override
        public void onOTAQueryResultSuccess(OTAUpgradeInfo upgradeinfo) {
            Log.d(TAG, "onOTAQueryResultSuccess()");
            if (mOTAUpgradeCallbacks == null) {
                return;
            }
            try {
                final int N = mOTAUpgradeCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    mOTAUpgradeCallbacks.getBroadcastItem(i).onOTARequestSuccess(upgradeinfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mOTAUpgradeCallbacks.finishBroadcast();
            }
        }

        @Override
        public void onOTAQueryResultFailure() {
            Log.d(TAG, "onOTAQueryResultFailure()");
            if (mOTAUpgradeCallbacks == null) {
                return;
            }
            try {
                final int N = mOTAUpgradeCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    mOTAUpgradeCallbacks.getBroadcastItem(i).onOTARequestFailure();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mOTAUpgradeCallbacks.finishBroadcast();
            }
        }

        @Override
        public void onDownloadState(long downloadid,int state) {
            Log.d(TAG, "onDownloadState() downloadid:"+downloadid +" state:" +state);
            if (mOTAUpgradeCallbacks == null) {
                return;
            }
            try {
                final int N = mOTAUpgradeCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    mOTAUpgradeCallbacks.getBroadcastItem(i).onDownloadState(downloadid,state);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mOTAUpgradeCallbacks.finishBroadcast();
            }
        }

        @Override
        public int onDownloadprogress(long downloadid,int percentage) {
            Log.d(TAG, "onDownloadState() downloadid:"+downloadid +" percentage:" +percentage);
            if (mOTAUpgradeCallbacks == null) {
                return 0;
            }
            try {
                final int N = mOTAUpgradeCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    mOTAUpgradeCallbacks.getBroadcastItem(i).onDownloadprogress(downloadid,percentage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mOTAUpgradeCallbacks.finishBroadcast();
            }
            return percentage;
        }

        @Override
        public void onInstallSuccess() {
            Log.d(TAG, "onInstallSuccess()");
            if (mOTAUpgradeCallbacks == null) {
                return;
            }
            try {
                final int N = mOTAUpgradeCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    mOTAUpgradeCallbacks.getBroadcastItem(i).onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mOTAUpgradeCallbacks.finishBroadcast();
            }
        }

        @Override
        public void onInstallFailure() {
            Log.d(TAG, "onInstallFailure()");
            if (mOTAUpgradeCallbacks == null) {
                return;
            }
            try {
                final int N = mOTAUpgradeCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    mOTAUpgradeCallbacks.getBroadcastItem(i).onFailure();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mOTAUpgradeCallbacks.finishBroadcast();
            }
        }
    }

    public void onStartOTAQueryRequest()
    {
        Log.d(TAG, "onStartOTAQueryRequest()");
        if(mOTAUpgradeServiceImp != null){
            mOTAUpgradeServiceImp.onUpgradeQueryRequest();
        }
    }

    public int onStartDownload()
    {
        int downloadId = -1;
        Log.d(TAG, "onStartDownload()");
        if(mOTAUpgradeServiceImp != null){
            downloadId = mOTAUpgradeServiceImp.onStartDownload();
        }
        return downloadId;
    }

    public void onStopDownload(long downloadId)
    {
        Log.d(TAG, "onStopDownload()");
        if(mOTAUpgradeServiceImp != null){
            mOTAUpgradeServiceImp.onStopDownload(downloadId);
        }
    }

    //support upgradefile is null or "",will download default path's file
    public void onStartInstall(String upgradefile)
    {
        Log.d(TAG, "onStartInstall()");

        if(mOTAUpgradeServiceImp != null){
            mOTAUpgradeServiceImp.onStartInstall(upgradefile);
        }
    }

    public void onStopInstall()
    {
        Log.d(TAG, "onStopInstall()");
        if(mOTAUpgradeServiceImp != null){
            mOTAUpgradeServiceImp.onStopInstall();
        }
    }

    private void getCurrentDownloadId()
    {
        Log.d(TAG, "getCurrentDownloadId()");
        if(mOTAUpgradeServiceImp != null){
            currentDownloadId = mOTAUpgradeServiceImp.getCurrentDownloadId();
        }
    }

    private String getDownloadPath()
    {
        Log.d(TAG, "getDownloadPath()");
        if(mOTAUpgradeServiceImp != null){
            DownloadPath = mOTAUpgradeServiceImp.getDownloadPath();
        }
        Log.d(TAG, "getDownloadPath() path:"+DownloadPath);
        return DownloadPath;
    }
}
