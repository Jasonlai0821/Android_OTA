package com.xinshiyun.otaupgrade.upgrade.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetUtils {

    //没有连接网络
    public static final int NETWORK_NONE = -1;
    //移动网络
    public static final int NETWORK_MOBILE = 0;
    //无线网络
    public static final int NETWORK_WIFI = 1;

    public static final int NETWORK_ETHERNET = 2;

    public static int getNetWorkState(Context context)
    {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = mConnectivityManager.getAllNetworkInfo();
        if(networkInfo != null){
            Log.d("NetUtils","Net length:"+networkInfo.length);
            for(int i=0;i<networkInfo.length;i++){
                Log.d("NetUtils","Net type:"+networkInfo[i].getType()+" isConnected:"+networkInfo[i].isConnected());
                if(networkInfo[i].getType() == ConnectivityManager.TYPE_ETHERNET && networkInfo[i].isConnected()){
                    //有线网络连接成功，更新UI
                    return NETWORK_ETHERNET;
                }else if(networkInfo[i].getType() == ConnectivityManager.TYPE_WIFI && networkInfo[i].isConnected()){
                    return NETWORK_WIFI;
                }else if(networkInfo[i].getType() == ConnectivityManager.TYPE_MOBILE && networkInfo[i].isConnected()){
                    return NETWORK_MOBILE;
                }
            }
        }else{
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }
}
