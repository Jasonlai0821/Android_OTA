package com.xinshiyun.otaupgrade.upgrade;

import android.os.Parcel;
import android.os.Parcelable;

public class OTAUpgradeInfo implements Parcelable {

    private String id;

    private String packageId;

    private String policyId;

    private String initVersion;

    private String dependSysVersion;

    private String version;

    private String downloadUrl;

    private String filesize;

    private String md5;

    private String upgradeType;

    private String versionType;

    private String chip;

    private String model;

    private String packageOwnerName;

    private String fileName;

    private String remark;

    public String filePath;

    public OTAUpgradeInfo(){}

    protected OTAUpgradeInfo(Parcel in) {
        id = in.readString();
        packageId = in.readString();
        policyId = in.readString();
        initVersion = in.readString();
        dependSysVersion = in.readString();
        version = in.readString();
        downloadUrl = in.readString();
        filesize = in.readString();
        md5 = in.readString();
        upgradeType = in.readString();
        versionType = in.readString();
        chip = in.readString();
        model = in.readString();
        packageOwnerName = in.readString();
        fileName = in.readString();
        remark = in.readString();
        filePath = in.readString();
    }

    public static final Creator<OTAUpgradeInfo> CREATOR = new Creator<OTAUpgradeInfo>() {
        @Override
        public OTAUpgradeInfo createFromParcel(Parcel in) {
            return new OTAUpgradeInfo(in);
        }

        @Override
        public OTAUpgradeInfo[] newArray(int size) {
            return new OTAUpgradeInfo[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getInitVersion() {
        return initVersion;
    }

    public void setInitVersion(String initVersion) {
        this.initVersion = initVersion;
    }

    public String getDependSysVersion() {
        return dependSysVersion;
    }

    public void setDependSysVersion(String dependSysVersion) {
        this.dependSysVersion = dependSysVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(String upgradeType) {
        this.upgradeType = upgradeType;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getChip() {
        return chip;
    }

    public void setChip(String chip) {
        this.chip = chip;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPackageOwnerName() {
        return packageOwnerName;
    }

    public void setPackageOwnerName(String packageOwnerName) {
        this.packageOwnerName = packageOwnerName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setFilePath(String path){this.filePath = path;}

    public String getFilePath(){return this.filePath;}

    @Override
    public String toString() {

        return  "id:"               + id                    + "|" +
                "packageId:"        + packageId             + "|" +
                "policyId:"         + policyId              + "|" +
                "initVersion:"      + initVersion           + "|" +
                "dependSysVersion:" + dependSysVersion      + "|" +
                "version:"          + version               + "|" +
                "download_url:"     + downloadUrl           + "|" +
                "filesize:"         + filesize              + "|" +
                "md5:"              + md5                   + "|" +
                "upgradeType:"      + upgradeType           + "|" +
                "versionType:"      + versionType           + "|" +
                "chip:"             + chip                  + "|" +
                "model:"            + model                 + "|" +
                "packageOwnerName:" + packageOwnerName      + "|" +
                "fileName:"         + fileName              + "|" +
                "remark:"           + remark ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(packageId);
        dest.writeString(policyId);
        dest.writeString(initVersion);
        dest.writeString(dependSysVersion);
        dest.writeString(version);
        dest.writeString(downloadUrl);
        dest.writeString(filesize);
        dest.writeString(md5);
        dest.writeString(upgradeType);
        dest.writeString(versionType);
        dest.writeString(chip);
        dest.writeString(model);
        dest.writeString(packageOwnerName);
        dest.writeString(fileName);
        dest.writeString(remark);
        dest.writeString(filePath);
    }
}
