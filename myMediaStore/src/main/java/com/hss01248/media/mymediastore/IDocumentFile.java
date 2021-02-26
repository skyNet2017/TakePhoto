package com.hss01248.media.mymediastore;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class IDocumentFile implements IFile{
    DocumentFile file;

    public IDocumentFile(DocumentFile file) {
        this.file = file;
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
}
