package com.xinshiyun.otaupgrade.upgrade.http;


import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeInfo;
import com.xinshiyun.otaupgrade.upgrade.misc.SysProperties;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SysUpgradeQueryRequest extends AbstractHttpGet {
    private static final String TAG = SysUpgradeQueryRequest.class.getSimpleName();

    //private final static String URL = "http://api.upgrade.skysrt.com/ied/v3/getUpgrader";
    private final static String URL = "http://192.168.60.126:20603/";
    private HttpSysUpgradeRequestCallback mHttpSysUpgradeRequestListener = null;
    private Context mContext;

    public SysUpgradeQueryRequest(Context context){
        mContext = context;
    }


    private JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            Log.i(TAG, "JsonHttpResponseHandler->onSuccess() 1:");
            super.onSuccess(statusCode, headers, response);
            Log.i(TAG,response.toString());
            try {
                int code = response.getInt("code");
                Log.i(TAG, "JsonHttpResponseHandler->onSuccess() 1: code="+code);
                if (code == 0) {
                    JSONObject data = response.getJSONObject("data");
                    OTAUpgradeInfo updateInfo = new OTAUpgradeInfo();
                    updateInfo.setId(data.getString("id"));
                    updateInfo.setPackageId(data.getString("packageId"));
                    updateInfo.setPolicyId(data.getString("policyId"));
                    updateInfo.setInitVersion(data.getString("initVersion"));
                    updateInfo.setDependSysVersion(data.getString("dependSysVersion"));
                    updateInfo.setVersion(data.getString("version"));
                    updateInfo.setDownloadUrl(data.getString("downloadUrl"));
                    updateInfo.setFilesize(data.getString("filesize"));
                    updateInfo.setMd5(data.getString("md5"));
                    updateInfo.setUpgradeType(data.getString("upgradeType"));
                    updateInfo.setVersionType(data.getString("versionType"));
                    updateInfo.setChip(data.getString("chip"));
                    updateInfo.setModel(data.getString("model"));
                    updateInfo.setPackageOwnerName(data.getString("packageOwnerName"));
                    updateInfo.setFileName(data.getString("fileName"));
                    updateInfo.setRemark(data.getString("remark"));

                    if(SysProperties.getChip(mContext).equals("2C01")
                            && SysProperties.getModel(mContext).equals("SBL3S")){
                        updateInfo.setUpgradeType("2");//升级类型0，手动升级，1自动升级，2，强制升级
                    }else{

                    }
                    if(mHttpSysUpgradeRequestListener != null) {
                        mHttpSysUpgradeRequestListener.onSysUpgradeRequestSuccess(updateInfo);
                    }
                }
                if (code == -1) {
                    if (mHttpSysUpgradeRequestListener != null){
                        mHttpSysUpgradeRequestListener.onSysUpgradeRequestFailure(OTAUpgradeQueryFail.no_update);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(mHttpSysUpgradeRequestListener != null) {
                    mHttpSysUpgradeRequestListener.onSysUpgradeRequestFailure(OTAUpgradeQueryFail.net_error);
                }
            }
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            Log.i(TAG, "JsonHttpResponseHandler->onSuccess() 2:");
            super.onSuccess(statusCode, headers, response);
            if(mHttpSysUpgradeRequestListener != null) {
                mHttpSysUpgradeRequestListener.onSysUpgradeRequestFailure(OTAUpgradeQueryFail.net_error);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            Log.i(TAG, "JsonHttpResponseHandler->onFailure() 1:");
            super.onFailure(statusCode, headers, throwable, errorResponse);
            if(mHttpSysUpgradeRequestListener != null) {
                mHttpSysUpgradeRequestListener.onSysUpgradeRequestFailure(OTAUpgradeQueryFail.net_error);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
            Log.i(TAG, "JsonHttpResponseHandler->onFailure() 2:");
            super.onFailure(statusCode, headers, throwable, errorResponse);
            if(mHttpSysUpgradeRequestListener != null) {
                mHttpSysUpgradeRequestListener.onSysUpgradeRequestFailure(OTAUpgradeQueryFail.net_error);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            Log.i(TAG, "JsonHttpResponseHandler->onFailure() 3:");
            super.onFailure(statusCode, headers, responseString, throwable);
            if(mHttpSysUpgradeRequestListener != null) {
                mHttpSysUpgradeRequestListener.onSysUpgradeRequestFailure(OTAUpgradeQueryFail.net_error);
            }
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            Log.d(TAG, "JsonHttpResponseHandler->onSuccess() 3:");
            super.onSuccess(statusCode, headers, responseString);
            //Log.i(TAG,responseString.toString());
        }
    };

    public void setHttpSysUpdateQueryCallback(HttpSysUpgradeRequestCallback callback) {
        Log.d(TAG, "setHttpSysUpdateQueryCallback()");
        this.mHttpSysUpgradeRequestListener = callback;
    }

    @Override
    String buildUrlString(Context context) {
        return URL;
    }

    @Override
    ResponseHandlerInterface buildHandler() {
        Log.d(TAG,"jsonHttpResponseHandler="+jsonHttpResponseHandler);
        return jsonHttpResponseHandler;
    }

    @Override
    Header[] buildHeaders() {
        return new Header[0];
    }

    public interface HttpSysUpgradeRequestCallback{
        void onSysUpgradeRequestSuccess(OTAUpgradeInfo upgradeinfo);

        void onSysUpgradeRequestFailure(OTAUpgradeQueryFail upgradefail);
    }
}
