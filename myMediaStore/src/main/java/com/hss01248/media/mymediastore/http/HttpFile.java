package com.hss01248.media.mymediastore.http;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hss01248.media.mymediastore.fileapi.BaseFileApi;
import com.hss01248.media.mymediastore.fileapi.IFile;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpFile extends BaseFileApi<HttpResponseBean> {


    public HttpFile(HttpResponseBean file) {
        super(file);
    }

    @Override
    public String storageId() {
        return null;
    }

    @Override
    public IFile[] listFiles() {
        return file.listFiles();
    }

    @Override
    public String getName() {
        return file.name;
    }

    @Override
    public Uri getUri() {
        return Uri.parse(file.getUrl());
    }

    @Override
    public String getPath() {
        return file.getUrl();
    }

    @Override
    public boolean isDirectory() {
        return file.isDir();
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
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
        HttpResponseBean bean = new HttpResponseBean();

        bean.name = file.path.substring(file.path.lastIndexOf("/")+1);
        bean.path = file.path.substring(0,file.path.lastIndexOf("/"));
        bean.type = "folder";
        return new HttpFile(bean);
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
        if(isDirectory()){
            return null;
        }
        return HttpHelper.getInputStream(file.getUrl(),null);
    }

    @Override
    public OutputStream getOutPutStream() {
        return null;
    }
}
