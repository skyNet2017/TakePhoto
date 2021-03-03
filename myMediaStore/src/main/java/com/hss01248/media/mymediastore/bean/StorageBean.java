package com.hss01248.media.mymediastore.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class StorageBean {

    public static final int TYPE_EXTERNAL_STORAGE = 1;
    public static final int TYPE_SAF = 2;
    public static final int TYPE_SMB = 3;
    public static final int TYPE_USB = 4;

    @Id
    public int id;
    public int usable;
    public int type;
    public String diskId;

    public String smbHost;
    public String smbUName;
    public String smbPw;
    public String smbRootDirs;//不带/, String数组,逗号相连,比如D,F

    public String safRoot;

    public String usbName;

    @Generated(hash = 1915755558)
    public StorageBean(int id, int usable, int type, String diskId, String smbHost,
            String smbUName, String smbPw, String smbRootDirs, String safRoot,
            String usbName) {
        this.id = id;
        this.usable = usable;
        this.type = type;
        this.diskId = diskId;
        this.smbHost = smbHost;
        this.smbUName = smbUName;
        this.smbPw = smbPw;
        this.smbRootDirs = smbRootDirs;
        this.safRoot = safRoot;
        this.usbName = usbName;
    }

    @Generated(hash = 806242961)
    public StorageBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsable() {
        return this.usable;
    }

    public void setUsable(int usable) {
        this.usable = usable;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDiskId() {
        return this.diskId;
    }

    public void setDiskId(String diskId) {
        this.diskId = diskId;
    }

    public String getSmbHost() {
        return this.smbHost;
    }

    public void setSmbHost(String smbHost) {
        this.smbHost = smbHost;
    }

    public String getSmbUName() {
        return this.smbUName;
    }

    public void setSmbUName(String smbUName) {
        this.smbUName = smbUName;
    }

    public String getSmbPw() {
        return this.smbPw;
    }

    public void setSmbPw(String smbPw) {
        this.smbPw = smbPw;
    }

    public String getSmbRootDirs() {
        return this.smbRootDirs;
    }

    public void setSmbRootDirs(String smbRootDirs) {
        this.smbRootDirs = smbRootDirs;
    }

    public String getSafRoot() {
        return this.safRoot;
    }

    public void setSafRoot(String safRoot) {
        this.safRoot = safRoot;
    }

    public String getUsbName() {
        return this.usbName;
    }

    public void setUsbName(String usbName) {
        this.usbName = usbName;
    }









}
