package com.hss01248.media.mymediastore.bean;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.InformationTrans;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.File;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.hss01248.media.mymediastore.smb.FileApiForSmb;
import com.hss01248.media.mymediastore.smb.SmbToHttp;
import com.hss01248.media.mymediastore.smb.SmbjUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.util.EnumSet;
import java.util.Objects;

@Entity
public class BaseMediaInfo {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_AUDIO = 3;

    public String folderPathOrUri;

    public String smbHost;
    public String smbRootDir;//不带/

    public String name;

    public IFile getFile() {
        if(file == null){
            genFile();
        }
        return file;
    }

    public transient IFile file;

    public void genFile(){
        if(pathOrUri.startsWith("smb://")){
            this.file = SmbToHttp.getFile(pathOrUri);
        }else if(pathOrUri.startsWith("content://")){
            file = new IDocumentFile(SafUtil.findFile(SafUtil.sdRoot,pathOrUri));
        }else if(pathOrUri.startsWith("/storage/")){
            file = new JavaFile(new java.io.File(pathOrUri));
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseMediaInfo)) return false;
        BaseMediaInfo that = (BaseMediaInfo) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(pathOrUri, that.pathOrUri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(pathOrUri);
        }
        return 908;
    }

    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    @Id
    public String pathOrUri;

    public long fileSize;
    public long updatedTime;

    public int maxSide;
    public int duration;

    public String path;

    public int type;

    @Generated(hash = 577682592)
    public BaseMediaInfo(String folderPathOrUri, String smbHost, String smbRootDir,
            String name, String pathOrUri, long fileSize, long updatedTime,
            int maxSide, int duration, String path, int type) {
        this.folderPathOrUri = folderPathOrUri;
        this.smbHost = smbHost;
        this.smbRootDir = smbRootDir;
        this.name = name;
        this.pathOrUri = pathOrUri;
        this.fileSize = fileSize;
        this.updatedTime = updatedTime;
        this.maxSide = maxSide;
        this.duration = duration;
        this.path = path;
        this.type = type;
    }

    @Generated(hash = 1446686172)
    public BaseMediaInfo() {
    }



    public boolean isImage(){
        return type == TYPE_IMAGE;
    }

    public boolean isVideo(){
        return type == TYPE_VIDEO;
    }

    public boolean isAudio(){
        return type == TYPE_AUDIO;
    }

    public String getFolderPathOrUri() {
        return this.folderPathOrUri;
    }

    public void setFolderPathOrUri(String folderPathOrUri) {
        this.folderPathOrUri = folderPathOrUri;
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPathOrUri() {
        return this.pathOrUri;
    }

    public void setPathOrUri(String pathOrUri) {
        this.pathOrUri = pathOrUri;
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

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }




}
