package com.xinshiyun.otaupgrade.upgrade.misc;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
//import android.os.SystemProperties;

public class SysProperties {

    // 获取机芯
    public static String getChip(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.device", "rk3399_all");
    }

    // 获取机型
    public static String getModel(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.model", "XTA-110");
    }

    // 获取当前系统版本号
    public static String getVersion(Context contex) {
        String version;
        String vers = SystemPropertiesProxy.get(contex, "ro.build.version.incremental","000000000");
        if(vers != null){
            String ver = vers.replaceAll("^(0+)", "");
            int start = ver.indexOf("troy.")+6;
            int end = start + 7;
            String result = ver.substring(start,end);
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

    public static String getSerialNO(Context context) {
        return SystemPropertiesProxy.get(context, "ro.serialno", "00000000000000");
    }

    // 获取当前产品版本号
    public static String getPRVersion(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.version", "1.0.0");
    }

    public static String getRegion(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.region", "CN");
    }

    public static String getLanguage(Context contex) {
        return SystemPropertiesProxy.get(contex, "ro.product.locale.language]:", "zh");
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
