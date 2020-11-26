package com.xinshiyun.otaupgrade.upgrade.http;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.ResponseHandlerInterface;
import com.xinshiyun.otaupgrade.upgrade.misc.SysProperties;

import org.apache.http.Header;

public abstract class AbstractHttpGet {

    public void request(Context context) {
        String url = buildUrlString(context);
        ResponseHandlerInterface handler = buildHandler();
        if (isUrlValid(url) && isHandlerValid(handler)) {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.addHeader("MAC", SysProperties.getMac(context));
            asyncHttpClient.addHeader("cModel", SysProperties.getModel(context));
            asyncHttpClient.addHeader("cChip", SysProperties.getChip(context));
            asyncHttpClient.addHeader("cSize", "21");
            asyncHttpClient.addHeader("cResolution", "1920*1080");
            asyncHttpClient.addHeader("cSerialNO", SysProperties.getSerialNO(context)); // 设备序列号
            asyncHttpClient.addHeader("cSystemVersion", SysProperties.getVersion(context)); //系统版本
            asyncHttpClient.addHeader("cSystemStatus", "0");
            asyncHttpClient.addHeader("cPRVersion",  SysProperties.getPRVersion(context));//产品迭代版本
            asyncHttpClient.addHeader("cPkg", "com.xinshiyun.otaupgrade");
            asyncHttpClient.addHeader("cBrand", "xinshiyun");
            asyncHttpClient.addHeader("cCountry", SysProperties.getRegion(context));
            asyncHttpClient.addHeader("cLanguage", SysProperties.getLanguage(context));
            asyncHttpClient.addHeader("cDevice", "2S");
            asyncHttpClient.addHeader("cAreaCode", "086");
            asyncHttpClient.post(url, handler);
            Log.d("HttpRequest","MAC:"+SysProperties.getMac(context)+" "
                                            +"cModel:"+ SysProperties.getModel(context)+" "
                                            +"cChip:"+SysProperties.getChip(context)+" "
                                            +"cSize:"+"21"+" "
                                            +"cResolution:"+"1920*1080"+" "
                                            +"cSerialNO:"+SysProperties.getSerialNO(context)+" "
                                            +"cSystemVersion:"+SysProperties.getVersion(context)+" "
                                            +"cSystemStatus:"+"0"+" "
                                            +"cPRVersion:"+SysProperties.getPRVersion(context)+" "
                                            +"cPkg:"+"com.xinshiyun.otaupgrade"+" "
                                            +"cBrand:"+"xinshiyun"+" "
                                            +"cCountry:"+SysProperties.getRegion(context)+" "
                                            +"cLanguage:"+SysProperties.getLanguage(context)+" "
                                            +"cDevice:"+"2S"+" "
                                            +"cAreaCode:"+"086"+" "
                                            +"url:"+url);
        }
    }

    private boolean isUrlValid(String url) {
        return true;
    }

    private boolean isHandlerValid(ResponseHandlerInterface handler) {
        return true;
    }

    abstract String buildUrlString(Context context);

    abstract ResponseHandlerInterface buildHandler();

    abstract Header[] buildHeaders();

}
