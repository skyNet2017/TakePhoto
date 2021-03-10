package com.sznq.finalcompress.filemanager.folder;

import com.hss01248.media.mymediastore.fileapi.IFile;

import java.util.Comparator;

public class BaseFolderSort implements Comparator<IFile> {
    @Override
    public int compare(IFile o1, IFile o2) {
        if(o1.isDirectory() && !o2.isDirectory()){
            return 1;
        }
        if(!o1.isDirectory() && o2.isDirectory()){
            return -1;
        }
        return 0;
    }
}
