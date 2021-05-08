package com.hss01248.media.mymediastore.fileapi;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class JavaFile extends BaseFileApi<File>{
    public JavaFile(File file) {
        super(file);
    }

    @Override
    public String storageId() {
        return null;
    }

    @Override
    public IFile[] listFiles() {
        if(isDirectory()){
            File[] files = file.listFiles();
            if(files == null){
                return null;
            }
            IFile[] iFiles = new IFile[files.length];
            if(files != null){
                for (int i = 0; i < files.length; i++) {
                    iFiles[i] = new JavaFile(files[i]);
                }
            }
            return iFiles;
        }
        return null;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Uri getUri() {
        return Uri.fromFile(file);
    }

    @Override
    public String getPath() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
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
        return file.exists();
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public boolean canWrite() {
        return file.canWrite();
    }

    @Override
    public IFile getParentFile() {
        if(file.getParentFile() ==  null){
            return null;
        }
        return new JavaFile(file.getParentFile());
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
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OutputStream getOutPutStream() {
        try {
            return new FileOutputStream(getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
