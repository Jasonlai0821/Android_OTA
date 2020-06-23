package com.xinshiyun.otaupgrade.upgrade.http;

import android.content.Context;
import android.util.Log;

import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeInfo;

/**
 * Created by SKY300581 on 2018/1/9.
 */

public class OTAUpgradeRequestManager {
    private final static String TAG = OTAUpgradeRequestManager.class.getSimpleName();

    private SysUpgradeQueryCallback mSysUpgradeQueryListener = null;
    private SysUpgradeQueryRequest mSysUpgradeQueryRequest = null;
    private HttpSysUpgradeRequestListener mHttpSysUpgradeRequestListener = null;
    private Context mContext;

    public OTAUpgradeRequestManager(Context context){
        Log.d(TAG, "OTAUpgradeRequestManager()");
        this.mContext = context;

        mSysUpgradeQueryRequest = new SysUpgradeQueryRequest(context);
        mHttpSysUpgradeRequestListener = new HttpSysUpgradeRequestListener();
        mSysUpgradeQueryRequest.setHttpSysUpdateQueryCallback(mHttpSysUpgradeRequestListener);
    }

    public void onSysUpgradeQueryRequest()
    {
        Log.d(TAG, "onSysUpgradeQueryRequest()");
        if(mSysUpgradeQueryRequest != null){
            mSysUpgradeQueryRequest.request(mContext);
        }
    }

    public class HttpSysUpgradeRequestListener implements SysUpgradeQueryRequest.HttpSysUpgradeRequestCallback {
        @Override
        public void onSysUpgradeRequestSuccess(OTAUpgradeInfo upgradeinfo) {
            Log.d(TAG, "onSysUpgradeRequestSuccess()");
            if(mSysUpgradeQueryListener != null){
                mSysUpgradeQueryListener.onSysUpgradeQuerySuccess(upgradeinfo);
            }
        }

        @Override
        public void onSysUpgradeRequestFailure(OTAUpgradeQueryFail upgradefail) {
            Log.d(TAG, "onSysUpgradeRequestFailure()");
            if(mSysUpgradeQueryListener != null){
                mSysUpgradeQueryListener.onSysUpgradeQueryFailure(upgradefail);
            }
        }
    }

    public interface SysUpgradeQueryCallback{
        void onSysUpgradeQuerySuccess(OTAUpgradeInfo upgradeinfo);

        void onSysUpgradeQueryFailure(OTAUpgradeQueryFail upgradefail);
    }

    public void setSysUpgradeQueryCallback(SysUpgradeQueryCallback callback)
    {
        Log.d(TAG, "setSysUpgradeQueryCallback()");
        mSysUpgradeQueryListener = callback;
    }
}
