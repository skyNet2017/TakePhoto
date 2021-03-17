package com.hss01248.media.mymediastore.bean;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hss01248.media.mymediastore.fileapi.IFile;

import java.io.InputStream;
import java.io.OutputStream;

public class BaseInfo implements IFile {


    public String id;
    public String name;
    public String path;
    public int diskType;
    public long diskId;
    public int mediaType;
    public long fileSize;
    public long updatedTime;
    public int  hidden = 0;
    public int getHidden() {
        return this.hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String storageId() {
        return null;
    }

    @Override
    public IFile[] listFiles() {
        return new IFile[0];
    }

    public String getName() {
        return name;
    }

    @Override
    public Uri getUri() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        return this instanceof BaseMediaFolderInfo;
    }

    @Override
    public long length() {
        return fileSize;
    }

    @Override
    public long lastModified() {
        return updatedTime;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public IFile getParentFile() {
        return null;
    }

    @Override
    public IFile createDirectory(@NonNull String displayName) {
        return null;
    }

    @Override
    public IFile createFile(@NonNull String mimeType, @NonNull String displayName) {
        return null;
    }

    @Override
    public boolean renameTo(@NonNull String displayName) {
        return false;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public OutputStream getOutPutStream() {
        return null;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDiskType() {
        return diskType;
    }

    public void setDiskType(int diskType) {
        this.diskType = diskType;
    }

    public long getDiskId() {
        return diskId;
    }

    public void setDiskId(long diskId) {
        this.diskId = diskId;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
