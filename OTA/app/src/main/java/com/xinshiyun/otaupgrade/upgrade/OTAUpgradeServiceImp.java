package com.xinshiyun.otaupgrade.upgrade;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xinshiyun.otaupgrade.upgrade.download.DownloadInfo;
import com.xinshiyun.otaupgrade.upgrade.download.DownloadProgress;
import com.xinshiyun.otaupgrade.upgrade.download.DownloadTaskExcutor;
import com.xinshiyun.otaupgrade.upgrade.http.OTAUpgradeQueryFail;
import com.xinshiyun.otaupgrade.upgrade.http.OTAUpgradeRequestManager;
import com.xinshiyun.otaupgrade.upgrade.install.InstallManager;
import com.xinshiyun.otaupgrade.upgrade.misc.MD5Utils;
import com.xinshiyun.otaupgrade.upgrade.misc.SysProperties;
import com.xinshiyun.otaupgrade.upgrade.misc.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by SKY300581 on 2018/1/8.
 */

public class OTAUpgradeServiceImp {
    private final static String TAG = OTAUpgradeServiceImp.class.getSimpleName();

    private static OTAUpgradeServiceImp mOTAUpgradeServiceImp = null;
    private OTAUpgradeRequestManager mOTAUpgradeRequestManager = null;
    private SysUpgradeQueryListener mSysUpgradeQueryListener = null;

    private DownloadInfo mCurrentDldInfo = null;
    private OTAUpgradeInfo mCurrentOTAUpgradeInfo;
    private DownloadTaskExcutor mDownloadTaskExcutor = null;
    ScheduledExecutorService mDownloadProgressExecutor = null;

    private InstallManager mInstallManager = null;
    private InstallManagerListener mInstallManagerListener = null;

    private OTAUpgradeServiceCallback mOTAUpgradeImpListener = null;
    private boolean mIsPackageValid = false;
    private static int mCurrentDownloadId = -1;

    public Context mContext;
    private static boolean isForceUpgrade = false;

    public OTAUpgradeServiceImp(Context context)
    {
        this.mContext = context;

        Log.d(TAG,"OTAUpgradeServiceImp()");
        mOTAUpgradeRequestManager = new OTAUpgradeRequestManager(context);
        mSysUpgradeQueryListener = new SysUpgradeQueryListener();
        mOTAUpgradeRequestManager.setSysUpgradeQueryCallback(mSysUpgradeQueryListener);

        mDownloadTaskExcutor = new DownloadTaskExcutor(context);

        mInstallManager = new InstallManager(context);
        mInstallManagerListener = new InstallManagerListener();
        mInstallManager.setInstallManagerCallback(mInstallManagerListener);
        Utils.deleteSysUpgradeFile("/data");
    }

    public synchronized static OTAUpgradeServiceImp getInstance(Context context) {
        Log.d(TAG,"OTAUpgradeManager getInstance()");
        if (mOTAUpgradeServiceImp == null) {
            mOTAUpgradeServiceImp = new OTAUpgradeServiceImp(context);
        }

        return mOTAUpgradeServiceImp;
    }

    public class SysUpgradeQueryListener implements OTAUpgradeRequestManager.SysUpgradeQueryCallback
    {
        @Override
        public void onSysUpgradeQuerySuccess(OTAUpgradeInfo upgradeinfo) {
            Log.d(TAG,"onSysUpgradeQuerySuccess()");

            mCurrentDldInfo = new DownloadInfo(upgradeinfo);
            mCurrentDldInfo.setSavePath(getDownloadPath());

            mCurrentOTAUpgradeInfo = upgradeinfo;

            if(!checkVersion(upgradeinfo.getVersion())){
                Log.d(TAG,"onSysUpgradeQuerySuccess() seach the same System version,clear the download task");

                if(OTAUpgradeSharePreference.getSysDownloadId(mContext) != -1){
                    removeDownloadTask(OTAUpgradeSharePreference.getSysDownloadId(mContext));
                    deleteObsoletingSysUpgradeFile();
                }
                releaseCurrentInfo();
                if(mOTAUpgradeImpListener != null) {
                    mOTAUpgradeImpListener.onOTAQueryResultFailure();
                }
            }else{
                if(OTAUpgradeSharePreference.getSysDownloadId(mContext) == -1){//current state without download task
                    Log.d(TAG,"onSysUpgradeQuerySuccess() current without download task!");
                    deleteObsoletingSysUpgradeFile();
                }else{
                    Log.d(TAG,"onSysUpgradeQuerySuccess() current with some download task!");

                    removeDownloadTask(OTAUpgradeSharePreference.getSysDownloadId(mContext));
                    deleteObsoletingSysUpgradeFile();
                }

                isForceUpgrade = isForceUpgrade(mCurrentOTAUpgradeInfo);

                if(isForceUpgrade){// force uprage without user's interaction
                    mCurrentDownloadId = onStartDownload();
                }else{//notify UI to process user's interaction
                    Log.d(TAG,"onSysUpgradeQuerySuccess() nofity user for interaction");
                }
                if(mOTAUpgradeImpListener != null){
                    mOTAUpgradeImpListener.onOTAQueryResultSuccess(upgradeinfo);
                }
            }
        }

        @Override
        public void onSysUpgradeQueryFailure(OTAUpgradeQueryFail upgradefail) {
            Log.d(TAG,"onSysUpgradeQueryFailure()");
            releaseCurrentInfo();

            //notify UI for user interaction
            Log.d(TAG,"onSysUpgradeQueryFailure() nofity user for interaction");

            if(mOTAUpgradeImpListener != null) {
                mOTAUpgradeImpListener.onOTAQueryResultFailure();
            }
        }
    }

    public class InstallManagerListener implements InstallManager.InstallManagerCallback{
        @Override
        public void onInstallSuccess() {
            Log.d(TAG,"onInstallSuccess()");
            if(mOTAUpgradeImpListener != null){
                mOTAUpgradeImpListener.onInstallSuccess();
            }
        }

        @Override
        public void onInstallFailure() {
            Log.d(TAG,"onInstallFailure()");
            if(mOTAUpgradeImpListener != null){
                mOTAUpgradeImpListener.onInstallFailure();
            }
        }
    }

    public int getCurrentDownloadId()
    {
        long downloadId = -1;
        if(mCurrentDldInfo != null){
            downloadId = mCurrentDldInfo.getId();
        }
        Log.d(TAG, "getCurrentDownloadId() downloadId:"+downloadId);
        return (int)downloadId;
    }

    public String getDownloadPath()
    {
        String path = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            path = "/data";
        }else{
            File catchedDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //File catchedDir = Environment.getExternalStorageDirectory();
            path = catchedDir.getAbsolutePath();
        }
        Log.i(TAG, "download system package:" + path);
        return path;
    }


    public boolean isForceUpgrade(OTAUpgradeInfo info)
    {
        boolean ret = false;
        if(info != null){
            String type = info.getUpgradeType();
            if(type.equals("2")){
                ret = true;
            }
        }
        Log.i(TAG, "isForceUpgrade() ret:" + ret);
        return ret;
    }

    public boolean checkVersion(String updateVersion){
        boolean ret = false;
        String currentVersion = "";
        long current = -1,update = -1;

        Log.d(TAG, "checkVersion()");
        currentVersion = SysProperties.getVersion(mContext).replace(".", "");
        Log.d(TAG, "checkVersion(): currentVersion:" + currentVersion + "    updateVersion:" + updateVersion);

        try{
            String upver = updateVersion.substring(1,updateVersion.length());
            String curver = currentVersion.substring(1,currentVersion.length());
            update = Long.valueOf(upver);
            current = Long.valueOf(curver);
            Log.d(TAG, "checkVersion(): current:" + current + "    update:" + update);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(update > current){
            ret = true;
        }
        Log.d(TAG, "checkVersion() ret = "+ret);
        return ret;
    }

    public int removeDownloadTask(long downloadId){
        int ret = -1;
        Log.d(TAG, "removeDownloadTask(): downloadId="+downloadId);
        OTAUpgradeSharePreference.setSysDownloadId(mContext,-1);

        if(mDownloadTaskExcutor != null){
            ret = mDownloadTaskExcutor.remove(downloadId);
        }
        return ret;
    }

    public void deleteObsoletingSysUpgradeFile()
    {
        Log.d(TAG, "deleteObsoletingSysUpgradeFile()");
        File file = new File(getDownloadPath());

        if(file != null && file.exists() && file.isDirectory()){
            Log.d(TAG, "deleteObsoletingSysUpgradeFile() path:"+getDownloadPath());
            File[] fs = file.listFiles();

            if(fs != null){
                Log.d(TAG, "deleteObsoletingSysUpgradeFile() fs is not null");
                for (File f : fs) {
                    Log.d(TAG, "deleteObsoletingSysUpgradeFile() file");
                    if (f.exists()) {
                        Log.d(TAG, "deleteObsoletingSysUpgradeFile() file:"+f.getName());
                        if (f.getName().contains("downloadfile")||f.getName().contains("AIUI") || f.getName().contains("update_")) {
                            if (f.getName().contains("bin") || f.getName().contains("img")||f.getName().contains("zip")) {
                                if(f.delete()){
                                    Log.d(TAG, "deleteObsoletingSysUpgradeFile() success file:"+f.getName());
                                }else{
                                    Log.d(TAG, "deleteObsoletingSysUpgradeFile() failed file:"+f.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void releaseCurrentInfo()
    {
        Log.d(TAG, "releaseCurrentInfo()");
        mCurrentDldInfo = null;
        mCurrentOTAUpgradeInfo = null;
        OTAUpgradeSharePreference.setSysDownloadId(mContext,-1);
        OTAUpgradeSharePreference.setSysUpgradeExist(mContext,0);
    }

    public void onUpgradeQueryRequest()
    {
        Log.d(TAG, "onUpgradeQueryRequest()");
        mIsPackageValid = false;
        if(mOTAUpgradeRequestManager != null){
            mOTAUpgradeRequestManager.onSysUpgradeQueryRequest();
        }
    }

    public int onStartDownload()
    {
        int downloadId = -1;
        Log.d(TAG, "onStartDownload()");
        if(mCurrentOTAUpgradeInfo == null){
            Log.d(TAG, "onStartDownload() mCurrentOTAUpgradeInfo is null");
            return downloadId;
        }
        if(mCurrentDownloadId == 0){
            Log.d(TAG, "onStartDownload() current is downloading");
            return mCurrentDownloadId;
        }
        OTAUpgradeSharePreference.setForceInstall(mContext,isForceUpgrade);
        OTAUpgradeSharePreference.setOTAUpgradeInfo(mContext,mCurrentOTAUpgradeInfo);

        if(mDownloadTaskExcutor != null){
            downloadId = (int)mDownloadTaskExcutor.start(mCurrentDldInfo);

            OTAUpgradeSharePreference.setSysDownloadId(mContext,downloadId);
            OTAUpgradeSharePreference.setSysUpgradeExist(mContext,1);
            mCurrentDldInfo.setId(downloadId);

            onStartDownloadProgress(downloadId);
        }

        Log.d(TAG, "onStartDownload() downloadId:"+downloadId);

        mCurrentDownloadId = downloadId;
        return downloadId;
    }

    public int onStopDownload(long downloadId)
    {
        int ret = -1;
        Log.d(TAG, "onStopDownload() downloadId:"+downloadId);

        mCurrentDownloadId = -1;
        if(downloadId == getCurrentDownloadId()){
            removeDownloadTask(downloadId);
            deleteObsoletingSysUpgradeFile();
            releaseCurrentInfo();
        }else{
            Log.d(TAG, "onStopDownload() current downloadid is "+getCurrentDownloadId()+" can't find downloadid:"+downloadId);
        }
        return ret;
    }

    public void onStartInstall(String path)
    {
        Log.d(TAG, "onStartInstall() path:"+path);
        String upgradefile ="";
        OTAUpgradeInfo upgradeinfo = OTAUpgradeSharePreference.getOTAUpgradeInfo(mContext);
        if(mInstallManagerListener != null){
            mInstallManagerListener.onInstallSuccess();
        }

        if(path == null || path.equals("") || path.equals("null")){
            upgradefile = upgradeinfo.getFilePath();
            Log.d(TAG, "onStartInstall() getFilePath():"+upgradefile);
        }else{
            upgradefile = path;
            Log.d(TAG, "onStartInstall() path:"+path);
        }

        Log.d(TAG, "onStartInstall() upgradefile:"+upgradefile);

        OTAUpgradeSharePreference.setOTAUpgradeInfo(mContext,upgradeinfo);

        if(mInstallManager != null){
            Log.d(TAG, "InstallManager startInstall() upgradefile:"+upgradefile);
            //mInstallManager.startInstall(upgradefile);
            mInstallManager.startInstall(upgradefile,mIsPackageValid);
        }
    }

    public void onStopInstall()
    {
        Log.d(TAG, "onStopInstall()");
    }

    public void onStartDownloadProgress(long downloadId)
    {
        Log.d(TAG, "onStartDownloadProgress()");
        mDownloadProgressExecutor = Executors.newScheduledThreadPool(3);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mDownloadTaskExcutor != null){
                    int[] result = mDownloadTaskExcutor.getBytesAndStatus(OTAUpgradeSharePreference.getSysDownloadId(mContext));
                    Log.d(TAG, "Running onDownloadProgress() progress:"+result[0] + " ,total:" + result[1]);
                    DownloadProgress downloadProgress = new DownloadProgress();
                    downloadProgress.progress = result[0];
                    downloadProgress.total = result[1];
                    downloadProgress.status = result[2];
                    downloadProgress.reason = result[3];

                    Message msg = mDownloadHandler.obtainMessage();
                    msg.obj = downloadProgress;
                    mDownloadHandler.sendMessage(msg);
                }

            }
        };
        mDownloadProgressExecutor.scheduleAtFixedRate(runnable, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void onStopDownloadProgress()
    {
        Log.d(TAG, "onStopDownloadProgress()");
        if(mDownloadProgressExecutor != null){
            mDownloadProgressExecutor.shutdown();
        }
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

    private Handler mDownloadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            DownloadProgress downloadProgress = (DownloadProgress) msg.obj;
            long downloadId = OTAUpgradeSharePreference.getSysDownloadId(mContext);

            if(mOTAUpgradeImpListener != null){
                mOTAUpgradeImpListener.onDownloadState(downloadId,downloadProgress.status);
                if(DownloadManager.STATUS_SUCCESSFUL != downloadProgress.status
                        || DownloadManager.STATUS_FAILED != downloadProgress.status){
                    int percentage = (int)((downloadProgress.progress *100) / downloadProgress.total);
                    Log.d(TAG, "DownloadManager progress percentage:"+percentage);
                    if(percentage != 100) {
                        mOTAUpgradeImpListener.onDownloadprogress(downloadId, percentage);
                    }
                }else{
                    Log.d(TAG, "DownloadManager state compeleted, wait for user's interaction");
                }
            }

            switch (downloadProgress.status){
                case DownloadManager.STATUS_FAILED:
                    removeDownloadTask(OTAUpgradeSharePreference.getSysDownloadId(mContext));
                    OTAUpgradeSharePreference.setSysDownloadId(mContext,-1);
                    deleteObsoletingSysUpgradeFile();
                    onStopDownloadProgress();
                    break;
                case DownloadManager.STATUS_PAUSED:
                    break;
                case DownloadManager.STATUS_PENDING:
                    break;
                case DownloadManager.STATUS_RUNNING:
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    onStopDownloadProgress();
                    releaseCurrentInfo();
                    OTAUpgradeInfo upgradeinfo = OTAUpgradeSharePreference.getOTAUpgradeInfo(mContext);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        upgradeinfo.setFilePath(mDownloadTaskExcutor.getPath(downloadId));
                    }else{
                        Utils.copyFile(mDownloadTaskExcutor.getPath(downloadId), Utils.getFileName(mDownloadTaskExcutor.getPath(downloadId)));
                        Utils.deletefile(mDownloadTaskExcutor.getPath(downloadId));
                        upgradeinfo.setFilePath(Utils.getFileName(mDownloadTaskExcutor.getPath(downloadId)));
                    }
                    mIsPackageValid = checkUpgradeFileValid(upgradeinfo.getFilePath());

                    if(isForceUpgrade(upgradeinfo)){//not notify UI for user's interaction
                        onStartInstall(Utils.getFileName(mDownloadTaskExcutor.getPath(downloadId)));
                    }else{//notify UI for user's interaction
                        Log.d(TAG, "DownloadManager.STATUS_SUCCESSFUL wait for user's interaction");
                    }
                    break;
                default:
                    break;
            }

        }
    };

    public void setOTAUpgradeServiceCallback(OTAUpgradeServiceCallback callback)
    {
        Log.d(TAG, "setOTAUpgradeServiceCallback()");
        mOTAUpgradeImpListener = callback;
    }

    public interface OTAUpgradeServiceCallback{
        void onOTAQueryResultSuccess(OTAUpgradeInfo upgradeinfo);

        void onOTAQueryResultFailure();

        void onDownloadState(long downloadid, int state);

        int onDownloadprogress(long downloadid, int percentage);

        void onInstallSuccess();

        void onInstallFailure();
    }
}
