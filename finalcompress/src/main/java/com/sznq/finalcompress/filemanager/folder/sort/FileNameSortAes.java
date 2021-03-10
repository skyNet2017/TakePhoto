package com.sznq.finalcompress.filemanager.folder.sort;

import com.hss01248.media.mymediastore.fileapi.IFile;
import com.sznq.finalcompress.filemanager.folder.BaseFolderSort;

public class FileNameSortAes extends BaseFolderSort {

    @Override
    public int compare(IFile o1, IFile o2) {
        if(o1.isDirectory() && !o2.isDirectory()){
            return -1;
        }
        if(!o1.isDirectory() && o2.isDirectory()){
            return 1;
        }

        return o1.getName().compareTo(o2.getName());
    }
}
