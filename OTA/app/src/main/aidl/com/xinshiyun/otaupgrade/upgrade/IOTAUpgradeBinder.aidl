// IOTAUpgradeBinder.aidl
package com.xinshiyun.otaupgrade.upgrade;

import com.xinshiyun.otaupgrade.upgrade.IOTAUpgradeCallback;

// Declare any non-default types here with import statements

interface IOTAUpgradeBinder {
    void registerOTAUpgradeCallback(IOTAUpgradeCallback callback);
    void unregisterOTAUpgradeCallback(IOTAUpgradeCallback callback);

    void startOTAQueryRequest();

    int startDownload();
    void stopDownload(long downloadId);

    void startInstall(String upgradefile);
    void stopInstall();
}
