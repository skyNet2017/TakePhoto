package com.hss01248.media.mymediastore.bean;

import android.os.Build;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Objects;

import static com.hss01248.media.mymediastore.bean.BaseMediaInfo.TYPE_AUDIO;
import static com.hss01248.media.mymediastore.bean.BaseMediaInfo.TYPE_IMAGE;
import static com.hss01248.media.mymediastore.bean.BaseMediaInfo.TYPE_VIDEO;

@Entity
public class BaseMediaFolderInfo {


    public String name;
    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    public String cover;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseMediaFolderInfo)) return false;
        BaseMediaFolderInfo that = (BaseMediaFolderInfo) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(pathAndType, that.pathAndType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(pathAndType);
        }
        return 9090;
    }

    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    public String pathOrUri;

    public String smbHost;
    public String smbRootDir;//不带/

    @Id
    public String pathAndType;

    public String path;


    public void generateTheId(){
        pathAndType = type+"-"+pathOrUri;
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

    public String getPathOrUri() {
        return this.pathOrUri;
    }

    public void setPathOrUri(String pathOrUri) {
        this.pathOrUri = pathOrUri;
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

    public String getPathAndType() {
        return this.pathAndType;
    }

    public void setPathAndType(String pathAndType) {
        this.pathAndType = pathAndType;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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

    public int type;

    @Generated(hash = 1788600874)
    public BaseMediaFolderInfo(String name, String cover, String pathOrUri,
            String smbHost, String smbRootDir, String pathAndType, String path,
            int count, long fileSize, int hidden, long updatedTime, long duration,
            int order, int type) {
        this.name = name;
        this.cover = cover;
        this.pathOrUri = pathOrUri;
        this.smbHost = smbHost;
        this.smbRootDir = smbRootDir;
        this.pathAndType = pathAndType;
        this.path = path;
        this.count = count;
        this.fileSize = fileSize;
        this.hidden = hidden;
        this.updatedTime = updatedTime;
        this.duration = duration;
        this.order = order;
        this.type = type;
    }

    @Generated(hash = 1055136609)
    public BaseMediaFolderInfo() {
    }








}
