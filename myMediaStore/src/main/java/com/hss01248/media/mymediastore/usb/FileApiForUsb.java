package com.hss01248.media.mymediastore.usb;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.hss01248.media.mymediastore.fileapi.BaseFileApi;
import com.hss01248.media.mymediastore.fileapi.IFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileApiForUsb extends BaseFileApi<UsbFile> {
    public FileApiForUsb(UsbFile file) {
        super(file);
    }

    @Override
    public String storageId() {
        return null;
    }

    @Override
    public IFile[] listFiles() {
        if(isDirectory()){
            try {
                UsbFile[] usbFiles = file.listFiles();
                IFile[] files = new IFile[usbFiles.length];
                for (int i = 0; i < usbFiles.length; i++) {
                    files[i] = new FileApiForUsb(usbFiles[i]);
                }
                return files;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Uri getUri() {
        return null;
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
        return file.getLength();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public boolean exists() {
        return file.getLength()>0;
    }

    @Override
    public boolean delete() {
        try {
             file.delete();
             return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public IFile getParentFile() {
        return new FileApiForUsb(file.getParent());
    }

    @Override
    public IFile createDirectory(@NonNull String displayName) {
        try {
            return new FileApiForUsb(file.createDirectory(displayName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IFile createFile(@NonNull String mimeType, @NonNull String displayName) {
        if(file.isDirectory()){
            try {
                file.createFile(displayName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public IFile findFile(@NonNull String displayName) {
        return null;
    }

    @Override
    public boolean renameTo(@NonNull String displayName) {
        return false;
    }

    @Override
    public InputStream getInputStream() {
        return new UsbFileInputStream(file);
    }

    @Override
    public OutputStream getOutPutStream() {
        return new UsbFileOutputStream(file);
    }

    @Override
    public boolean isFile() {
        return !file.isDirectory();
    }
}
