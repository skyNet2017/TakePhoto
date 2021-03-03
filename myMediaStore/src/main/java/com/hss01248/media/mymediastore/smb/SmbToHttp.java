package com.hss01248.media.mymediastore.smb;

import android.net.Uri;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.InformationTrans;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.File;

import java.util.EnumSet;

public class SmbToHttp {

    private static FileServer fileServer = null;

    public static String ip;
    public static int port;
    public static String getHttpUrlFromSmb(String uri){
        String url = uri;
        Uri uri1 = Uri.parse(uri);
        if(uri.startsWith("smb")){
            String path = uri1.getPath();
            url =  "http://"+ip+":"+port+"/smb"+path;
        }
        Log.w("smb","getHttpUrlFromSmb:"+uri+"\nurl:"+url);
        return url;
    }

    public static void startServer(){
        fileServer = new FileServer();
        fileServer.start();
    }

    public static FileApiForSmb getFile(String smbUri){
        Uri uri = Uri.parse(smbUri);
        String path = uri.getPath();  //   /D/a/b.jpg
        Log.w("smb","getpath:"+path);
        path = path.substring(path.indexOf("/")+1);// D/a/b.jpg
        Log.w("smb","getpath2:"+path);
        String  root = path.substring(0,path.indexOf("/"));//D
        Log.w("smb","root:"+root);
        String parentPath = path.substring(root.length(),path.lastIndexOf("/"));
        Log.w("smb","parentPath:"+parentPath);
        String fileName = uri.getPath().substring(uri.getPath().lastIndexOf("/")+1);
        Log.w("smb","fileName:"+fileName);
        String subPath = path.substring(path.indexOf("/")+1);
        Log.w("smb","subPath:"+subPath);

        Log.w("smb","parentPath:"+parentPath+", path:"+path+",this.path:"+subPath);
        if( SmbjUtil.getShare(uri.getHost(),root) != null && SmbjUtil.getShare(uri.getHost(),root).fileExists(subPath)){
            File smbFile =  SmbjUtil.getShare(uri.getHost(),root).openFile(subPath, EnumSet.of(AccessMask.GENERIC_READ),
                    null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
            FileAllInformation information = SmbjUtil.getShare(uri.getHost(),root).getFileInformation(smbFile.getFileId());

            FileIdBothDirectoryInformation information1 = InformationTrans.trans(information,fileName);
            //information1.getFileName()
            Log.w("smb","FileAllInformation getFileName:"+information.getNameInformation());

            FileApiForSmb  file = new FileApiForSmb(information1);
            file.setContext(uri.getHost(),root);
            file.setShare(SmbjUtil.getShare(uri.getHost(),root),parentPath);
            file.setSmbFile(smbFile);
            file.printInfo();
            return file;
        }
        return null;
    }


}
