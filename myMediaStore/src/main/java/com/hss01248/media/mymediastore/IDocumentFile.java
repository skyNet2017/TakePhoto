package com.hss01248.media.mymediastore;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class IDocumentFile implements IFile<DocumentFile>{
    DocumentFile file;

    public IDocumentFile(DocumentFile file) {
        this.file = file;
    }

    @Override
    public DocumentFile[] listFiles() {
        return new DocumentFile[0];
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public long lastModified() {
        return 0;
    }
}
