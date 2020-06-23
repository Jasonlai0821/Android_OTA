package com.xinshiyun.otaupgrade.upgrade.download;

import android.net.Uri;

import com.xinshiyun.otaupgrade.upgrade.OTAUpgradeInfo;

public class DownloadInfo {

    public enum TaskType {

        /**
         * the package for updating operate system
         * */
        system,

        /**
         * the apk file for updating apps
         * */
        app

    }

    /**
     * the id for the task
     * */
    private long id;

    /**
     * dowload uri
     */
    private Uri uri;

    /**
     * 本地的存储地址
     * */
    private Uri localUri;

    /**
     * 下载内容的类型
     * 将会影响以哪种方式进行安装
     */
    private TaskType taskType;

    /**
     * 是否支持后台运行
     * 如果不支持后台运行，在界面退出后将停止下载任务并且清除下载记录
     */
    private boolean isBackground;

    /**
     * 是否支持静默安装
     * 在下载完成后不经过提示界面直接进行安装
     */
    private boolean silentInstall;

    /**
     * 是否强制安装
     * 如果强制安装，提示界面只可以选择安装，不能选择放弃
     * 如果不强制安装，界面可以选择安装或者放弃
     */
    private boolean forceInstall;

    private String savePath;

    public DownloadInfo (OTAUpgradeInfo upgradeinfo) {

        id = 0; // 默认的，未被downloadmanager赋予id
        uri = Uri.parse(upgradeinfo.getDownloadUrl());
        taskType = TaskType.system;
        isBackground = true;
        silentInstall = false;

        //todo 是否强制下载的判断
        if (upgradeinfo.getChip() != null) {
            if (upgradeinfo.getChip().contains("FORCEALL")) {
                forceInstall = true;
            } else {
                forceInstall = false;
            }
        } else {
            forceInstall = false;
        }

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public void setIsBackground(boolean isBackground) {
        this.isBackground = isBackground;
    }

    public boolean isSilentInstall() {
        return silentInstall;
    }

    public void setSilentInstall(boolean silentInstall) {
        this.silentInstall = silentInstall;
    }

    public boolean isForceInstall() {
        return forceInstall;
    }

    public void setForceInstall(boolean forceInstall) {
        this.forceInstall = forceInstall;
    }

    public Uri getLocalUri() {
        return localUri;
    }

    public void setLocalUri(Uri localUri) {
        this.localUri = localUri;
    }

    public void setSavePath(String path){this.savePath = path;}

    public String getSavePath(){return this.savePath;}

}
