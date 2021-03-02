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
    public String smbRootDir;

    public String safDir;

    public String usbName;

    @Generated(hash = 2138551919)
    public StorageBean(int id, int usable, int type, String diskId, String smbHost,
            String smbRootDir, String safDir, String usbName) {
        this.id = id;
        this.usable = usable;
        this.type = type;
        this.diskId = diskId;
        this.smbHost = smbHost;
        this.smbRootDir = smbRootDir;
        this.safDir = safDir;
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

    public String getSmbRootDir() {
        return this.smbRootDir;
    }

    public void setSmbRootDir(String smbRootDir) {
        this.smbRootDir = smbRootDir;
    }

    public String getSafDir() {
        return this.safDir;
    }

    public void setSafDir(String safDir) {
        this.safDir = safDir;
    }

    public String getUsbName() {
        return this.usbName;
    }

    public void setUsbName(String usbName) {
        this.usbName = usbName;
    }


}
