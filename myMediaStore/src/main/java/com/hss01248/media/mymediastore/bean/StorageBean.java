package com.hss01248.media.mymediastore.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

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
    public String smbRootDir;

    public String safDir;

    public String usbName;


}
