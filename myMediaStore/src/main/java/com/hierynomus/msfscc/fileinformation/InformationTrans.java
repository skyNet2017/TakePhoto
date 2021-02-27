package com.hierynomus.msfscc.fileinformation;

public class InformationTrans {

    public static FileIdBothDirectoryInformation trans(FileAllInformation info, String fileName){
        FileIdBothDirectoryInformation both  = new FileIdBothDirectoryInformation(0,0,
               fileName,
                info.getBasicInformation().getChangeTime(),
                info.getBasicInformation().getLastAccessTime(),
                info.getBasicInformation().getLastWriteTime(),
                info.getBasicInformation().getLastAccessTime(),
                info.getStandardInformation().getEndOfFile(),
                info.getStandardInformation().getAllocationSize(),
                info.getAccessInformation().getAccessFlags(),0,"",null);
        return both;
    }
}
