package com.hss01248.media.mymediastore.smb;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hss01248.media.mymediastore.fileapi.BaseFileApi;
import com.hss01248.media.mymediastore.fileapi.IFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FileApiForSmb extends BaseFileApi<FileIdBothDirectoryInformation> {

    public File getSmbFile() {
        return smbFile;
    }

    File smbFile;
    Directory directory;
    String path;
    String host;
    String rootDir;


public void setContext(String host,String rootDir){
    this.host = host;
    this.rootDir = rootDir;
}

    public void setShare(DiskShare share,String parentPath) {
        this.share = share;


        if(!TextUtils.isEmpty(parentPath)){
            if(parentPath.endsWith("/")){
                path =  parentPath +getName();//  D/a/bc.jpg. 加上host就是完整的uri
            }else {
                path =  parentPath +"/"+getName();//  D/a/bc.jpg. 加上host就是完整的uri
            }

        }else {
            path =  file.getFileName();//  D/a/bc.jpg. 加上host就是完整的uri
        }
        if(path.startsWith("/")){
            path = path.substring(1);
        }
        Log.w("smb2","setShare path:"+path+",name:"+getName());
        if(".".equals(file.getFileName()) || "..".equals(file.getFileName())){
            return;
        }

        try {
            if(isDirectory()){
                directory = share.openDirectory(path, EnumSet.of(AccessMask.GENERIC_READ),
                        null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
            }else if(share.fileExists(path)){
                smbFile = share.openFile(path, EnumSet.of(AccessMask.GENERIC_READ),
                        null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);


            }else {
                Log.e("smb","其他类型:filePath:"+ path +",type:"+getAttrDesc(file.getFileAttributes())+","+file.getFileName());
                //其他类型:filePath:360Downloads/2032943.jpg,type:32,  FILE_ATTRIBUTE_ARCHIVE
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }


    }


    private String getAttrDesc(long fileAttributes) {
        FileAttributes[] values = FileAttributes.values();
        for (FileAttributes value : values) {
            if(fileAttributes == value.getValue()){
                return value.name().toLowerCase();
            }
        }
        return "null-"+fileAttributes;
    }

    DiskShare share;

    //https://github.com/hierynomus/smbj/issues/345

    public FileApiForSmb(FileIdBothDirectoryInformation file) {
        super(file);

        //File remoteSmbjFile =activity.mCurrentDiskShare.openFile(activity.mCurrentSmb2Path+"\\"+item.fileName+"."+item.getExt()
        //                                                            , EnumSet.of(AccessMask.GENERIC_ALL) //AccessMask.GENERIC_READ
        //                                                            , null, s, null, null);
       // smbFile = DiskShare.open
        //DiskShare share = s.connectShare("share")
        //     return share.openFile("path/to/screenshots/image.png",
        //     EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)

        //com.hierynomus.smbj.share.Directory remoteSmbjDirectory = share.openDirectory(directoryPath,
        //EnumSet.of(AccessMask.MAXIMUM_ALLOWED), null, null, SMB2CreateDisposition.FILE_OPEN, null);
    }

    @Override
    public String storageId() {
        return null;
    }

    @Override
    public IFile[] listFiles() {
        if(directory != null){
            List<FileIdBothDirectoryInformation> list = directory.list();
            if(list != null && list.size() > 0){
                List<IFile> files = new ArrayList<>(list.size());
                for (FileIdBothDirectoryInformation info : list) {
                    FileApiForSmb iFile = new FileApiForSmb(info);
                    iFile.rootDir = rootDir;
                    iFile.host = host;
                    iFile.setShare(share,getPath());
                    if(iFile.isDirectory() || iFile.isFile()){
                        files.add(iFile);
                    }
                }
                IFile[] files1 = new IFile[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    files1[i] = files.get(i);
                }
                Log.w("smb",files1.length+" files in "+getPath());
                return files1;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return file.getFileName();
    }

    @Override
    public Uri getUri() {
        return Uri.parse("smb://"+host+"/"+rootDir+"/"+path);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        return file.getFileAttributes() == FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue();
    }

    @Override
    public boolean isFile() {
        return smbFile != null;
    }

    @Override
    public long length() {
        //FileStandardInformation info = diskEntry.getFileInformation(FileStandardInformation.class)
        //long endOfFile = info.getEndOfFile()
        return file.getEndOfFile();
    }

    @Override
    public long lastModified() {
        return file.getChangeTime().toEpochMillis();
    }

    @Override
    public boolean exists() {
        if(isDirectory()){
            return share.folderExists(path);
        }else {
            return share.fileExists(path);
        }
    }

    @Override
    public boolean delete() {
        if(isDirectory()){
            directory.deleteOnClose();
        }else {
            smbFile.deleteOnClose();
        }

        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public IFile getParentFile() {
        return null;
    }


    @Override
    public IFile createDirectory(@NonNull String displayName) {
        if(isDirectory()){
            share.mkdir(path+"/"+displayName);
        }
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
        return smbFile.getInputStream();
    }

    @Override
    public OutputStream getOutPutStream() {
        if(isDirectory()){
            return null;
        }
        //smbFile.read()
        return smbFile.getOutputStream();
    }
}
