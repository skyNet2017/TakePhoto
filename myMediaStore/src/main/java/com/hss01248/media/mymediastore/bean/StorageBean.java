package com.hss01248.media.mymediastore.bean;


import androidx.annotation.Keep;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
@Keep
@Entity
public class StorageBean {

    public static final int TYPE_EXTERNAL_STORAGE = 1;
    public static final int TYPE_SAF = 2;
    public static final int TYPE_SMB = 3;
    public static final int TYPE_USB = 4;
    public static final int TYPE_HTTP_Everything = 5;

    @Id
    public long id;
    public int usable;
    public int type;
    public String diskId;

    public String ip;
    public String uname;
    public String pw;
    public String rootDirs;//不带/, String数组,逗号相连,比如D,F

    public String safRoot;

    public String name;

    @Generated(hash = 1965211341)
    public StorageBean(long id, int usable, int type, String diskId, String ip,
            String uname, String pw, String rootDirs, String safRoot, String name) {
        this.id = id;
        this.usable = usable;
        this.type = type;
        this.diskId = diskId;
        this.ip = ip;
        this.uname = uname;
        this.pw = pw;
        this.rootDirs = rootDirs;
        this.safRoot = safRoot;
        this.name = name;
    }

    @Generated(hash = 806242961)
    public StorageBean() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
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

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUname() {
        return this.uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPw() {
        return this.pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getRootDirs() {
        return this.rootDirs;
    }

    public void setRootDirs(String rootDirs) {
        this.rootDirs = rootDirs;
    }

    public String getSafRoot() {
        return this.safRoot;
    }

    public void setSafRoot(String safRoot) {
        this.safRoot = safRoot;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }



    











}
