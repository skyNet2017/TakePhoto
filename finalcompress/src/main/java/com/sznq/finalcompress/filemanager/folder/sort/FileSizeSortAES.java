package com.sznq.finalcompress.filemanager.folder.sort;

import com.hss01248.media.mymediastore.fileapi.IFile;
import com.sznq.finalcompress.filemanager.folder.BaseFolderSort;

public class FileSizeSortAES extends BaseFolderSort {

    @Override
    public int compare(IFile o1, IFile o2) {
        if(o1.isDirectory() && !o2.isDirectory()){
            return -1;
        }
        if(!o1.isDirectory() && o2.isDirectory()){
            return 1;
        }

        if(o2.length() > o1.length()){
            return -1;
        }
        if(o1.length() == o2.length()){
            return 0;
        }
        if(o2.length() < o1.length()){
            return 1;
        }
        return 0;
    }
}
