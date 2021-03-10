package com.hss01248.media.mymediastore.fileapi;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.hss01248.media.mymediastore.SafUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public class IDocumentFile extends BaseFileApi<DocumentFile> {


    public IDocumentFile(DocumentFile file) {
        super(file);
    }

    @Override
    public String storageId() {
        return "IDocumentFile"+1;
    }

    @Override
    public IFile[] listFiles() {
        DocumentFile[] documentFiles = file.listFiles();
        if(documentFiles == null){
            return null;
        }
        if(documentFiles.length ==0){
            return null;
        }
        IFile[] files = new IFile[documentFiles.length];
        for (int i = 0; i < documentFiles.length; i++) {
            files[i] = new IDocumentFile(documentFiles[i]);
        }
        return files;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Uri getUri() {
        return file.getUri();
    }

    @Override
    public String getPath() {
        return file.getUri().toString();
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
        if(file.getParentFile() == null){
            return null;
        }
        return new IDocumentFile(file.getParentFile());
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
        try {
            return SafUtil.context.getContentResolver().openInputStream(getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OutputStream getOutPutStream() {
        try {
            return SafUtil.context.getContentResolver().openOutputStream(getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
