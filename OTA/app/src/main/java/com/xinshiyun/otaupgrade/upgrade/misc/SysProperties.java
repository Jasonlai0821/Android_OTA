package com.xinshiyun.otaupgrade.upgrade.misc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.quectel.modemtool.ModemTool;
import com.quectel.modemtool.NvConstants;
//import android.os.SystemProperties;

public class SysProperties {

    // 获取机芯
    public static String getChip(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.device", "msm8953_64");
    }

    // 获取机型
    public static String getModel(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.model", "XTS100");
    }

    // 获取当前系统版本号
    public static String getVersion(Context contex) {
        String version;
        String vers = SystemPropertiesProxy.get(contex, "ro.build.version.incremental","000000000");
        if(vers != null){
            String ver = vers.replaceAll("^(0+)", "");
            for(int i =0; i < 2; i++){
                ver = ver.substring(ver.indexOf(".")+1);
            }

            String result = ver.substring(1,8);
            StringBuffer strbf=new StringBuffer(result);
            strbf.insert(3,"0");
            strbf.append("0");
            String prversion = getPRVersion(contex);
            StringBuffer verpr=new StringBuffer(prversion);
            verpr.deleteCharAt(1);//1.0.4
            verpr.deleteCharAt(2);//1.0.4
            strbf.append(verpr);
            version = strbf.toString();
        }else{
            version = "000000000000";
        }
        return version;
    }

    @SuppressLint("MissingPermission")
    public static String getSerialNO(Context context) {
        if(getModel(context).equals("XTS100")){
            ModemTool modemTool = new ModemTool();
            String result = modemTool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND,"AT+QCSN?");
            if(result == null || result.contains("ERROR")){
                result = "000000000000000";
            }else if(result.contains("OK")){
                String temp = result.substring(result.indexOf("\"")+1,result.length());
                result = temp.substring(0,temp.indexOf("\""));
                if(result.equals("")){
                    result = "000000000000000";
                }
            }

            return result;
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            }else{
                return SystemPropertiesProxy.get(context, "ro.serialno", "000000000000000");
            }
        }
    }

    // 获取当前产品版本号
    public static String getPRVersion(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.version", "1.0.0");
    }

    public static String getRegion(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.region", "CN");
    }

    public static String getLanguage(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.locale.language:", "zh");
    }

    private static String removeDot(String s) {
        if (s.contains(".")) {
            return s.replace(".", "");
        }
        return s;
    }

    // 获取本机的mac地址
    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (info.getMacAddress() == null) {
            return "acd074d89d6f";
        }
        return stringFormat(info.getMacAddress());
    }

    private static String stringFormat(String mac) {
        StringBuilder builder = new StringBuilder();
        String[] res = mac.split(":");
        for (String s : res) {
            builder.append(s);
        }
        return builder.toString();
    }

    private static String stringFormat(String src,String regex) {
        StringBuilder builder = new StringBuilder();
        String[] res = src.split(regex);
        for (String s : res) {
            builder.append(s);
        }
        return builder.toString();
    }
}
