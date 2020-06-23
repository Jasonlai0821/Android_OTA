// IOTAUpgradeCallback.aidl
package com.xinshiyun.otaupgrade.upgrade;
import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeInfo;
// Declare any non-default types here with import statements

interface IOTAUpgradeCallback {
    void onOTARequestSuccess(in OTAUpgradeInfo info);
    void onOTARequestFailure();
    void onDownloadState(long downloadid,int state);
    void onDownloadprogress(long downloadid,int percentage);
    void onSuccess();
    void onFailure();
}
