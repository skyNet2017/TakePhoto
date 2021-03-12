package com.hss01248.media.mymediastore.bean;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.EncryptUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class BaseMediaFolderInfo extends BaseInfo{


    public String name;
    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    public String cover;
    

    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    public String path;

    public String smbHost;
    public String smbRootDir;//不带/

    @Id
    public String id;


    public int diskType;

    public void generateTheId(){
        id = EncryptUtils.encryptMD5ToString(mediaType +"-"+ path);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDiskType() {
        return this.diskType;
    }

    public void setDiskType(int diskType) {
        this.diskType = diskType;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getHidden() {
        return this.hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public long getUpdatedTime() {
        return this.updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }






    public int count;
    public long fileSize;
    public int hidden;//1: true 0 :false
    public long updatedTime;

    /**
     * 总时长
     */
    public long duration;

    /**
     * 排序的序号.用于置顶功能
     */
    public int order;

    public int mediaType;

    @Generated(hash = 420904309)
    public BaseMediaFolderInfo(String name, String cover, String path,
            String smbHost, String smbRootDir, String id, int diskType, int count,
            long fileSize, int hidden, long updatedTime, long duration, int order,
            int mediaType) {
        this.name = name;
        this.cover = cover;
        this.path = path;
        this.smbHost = smbHost;
        this.smbRootDir = smbRootDir;
        this.id = id;
        this.diskType = diskType;
        this.count = count;
        this.fileSize = fileSize;
        this.hidden = hidden;
        this.updatedTime = updatedTime;
        this.duration = duration;
        this.order = order;
        this.mediaType = mediaType;
    }

    @Generated(hash = 1055136609)
    public BaseMediaFolderInfo() {
    }

   









}
