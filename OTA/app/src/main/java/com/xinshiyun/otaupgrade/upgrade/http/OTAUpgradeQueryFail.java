package com.xinshiyun.otaupgrade.upgrade.http;

public enum OTAUpgradeQueryFail {

    net_error("查询失败，请检查网络后重试!"),

    no_update("没有检查到更新!");

    private String msg;

    OTAUpgradeQueryFail(String msg) {
        this.msg =  msg;
    }

    public String getContent() {
        return  msg;
    }

}
