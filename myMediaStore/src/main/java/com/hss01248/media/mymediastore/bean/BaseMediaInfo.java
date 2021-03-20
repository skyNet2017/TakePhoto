package com.hss01248.media.mymediastore.bean;

import android.os.Build;

import androidx.annotation.Keep;

import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.hss01248.media.mymediastore.smb.SmbToHttp;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.Objects;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class BaseMediaInfo extends BaseInfo{

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_AUDIO = 3;
    public static final int TYPE_DOC_PDF = 4;//pdf
    public static final int TYPE_DOC_WORD = 5;//msword
    public static final int TYPE_DOC_EXCEL = 6;//excel
    public static final int TYPE_DOC_PPT = 7;//powerpoint
    public static final int TYPE_DOC_TXT = 8;  //文件名.txt

    public static final int TYPE_UNKNOWN = -1;
    @Index
    public String dir;

    @Override
    public String toString() {
        return "BaseMediaInfo{" +
                "dir='" + dir + '\'' +
                ", name='" + name + '\'' +
                ", hidden=" + hidden +
                ", file=" + file +
                ", path='" + path + '\'' +
                ", fileSize=" + fileSize +
                ", updatedTime=" + updatedTime +
                ", maxSide=" + maxSide +
                ", duration=" + duration +
                ", mediaType=" + mediaType +
                ", praiseCount=" + praiseCount +
                ", diskType=" + diskType +
                '}';
    }

    @Index
    public String name;

    public int  hidden = 0;

    public IFile getFile() {
        if(file == null){
            genFile();
        }
        return file;
    }

    public transient IFile file;

    public void genFile(){
        if(path.startsWith("smb://")){
            this.file = SmbToHttp.getFile(path);
        }else if(path.startsWith("content://")){
            file = new IDocumentFile(SafUtil.findFile(SafUtil.sdRoot, path));
        }else if(path.startsWith("/storage/")){
            file = new JavaFile(new java.io.File(path));
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseMediaInfo)) return false;
        BaseMediaInfo that = (BaseMediaInfo) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(path, that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(path);
        }
        return 908;
    }

    public void fillMediaInfo(){

    }

    public boolean isMedia(){
        return mediaType <4 && mediaType !=0;
    }

    public boolean isDoc(){
        return mediaType >3 ;
    }

    public String getDir() {
        return this.dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHidden() {
        return this.hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getUpdatedTime() {
        return this.updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public int getMaxSide() {
        return this.maxSide;
    }

    public void setMaxSide(int maxSide) {
        this.maxSide = maxSide;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getPraiseCount() {
        return this.praiseCount;
    }

    public void setPraiseCount(Integer praiseCount) {
        this.praiseCount = praiseCount;
    }

    public int getDiskType() {
        return this.diskType;
    }

    public void setDiskType(int diskType) {
        this.diskType = diskType;
    }









    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    @Id
    public String path;
    @Index
    public long fileSize;
    @Index
    public long updatedTime;
    @Index
    public int maxSide;
    @Index
    public int duration;


    public String getAbsolutePath(){
        return dir+"/"+name;
    }
    

    public int mediaType;
    public Integer praiseCount;
    public int diskType;

    @Generated(hash = 340852875)
    public BaseMediaInfo(String dir, String name, int hidden, String path,
            long fileSize, long updatedTime, int maxSide, int duration,
            int mediaType, Integer praiseCount, int diskType) {
        this.dir = dir;
        this.name = name;
        this.hidden = hidden;
        this.path = path;
        this.fileSize = fileSize;
        this.updatedTime = updatedTime;
        this.maxSide = maxSide;
        this.duration = duration;
        this.mediaType = mediaType;
        this.praiseCount = praiseCount;
        this.diskType = diskType;
    }

    @Generated(hash = 1446686172)
    public BaseMediaInfo() {
    }




    }















