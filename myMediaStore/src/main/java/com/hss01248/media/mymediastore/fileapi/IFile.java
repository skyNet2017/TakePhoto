package com.hss01248.media.mymediastore.fileapi;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hss01248.media.mymediastore.SafFileFinder;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;

import java.io.InputStream;
import java.io.OutputStream;

public interface IFile {

    String storageId();

    IFile[] listFiles();

   String getName();

    Uri getUri();

    String getPath();

   boolean isDirectory();

    long length();

    long lastModified();

    boolean exists();

    boolean delete();

    boolean canWrite();

    IFile getParentFile();



    IFile createDirectory(@NonNull String displayName);

    IFile createFile(@NonNull String mimeType, @NonNull String displayName);

   default IFile findFile(@NonNull String displayName){
       for (IFile doc : listFiles()) {
           if (displayName.equals(doc.getName())) {
               return doc;
           }
       }
       return null;
   }

    boolean renameTo(@NonNull String displayName);

   InputStream getInputStream();

   OutputStream getOutPutStream();

   default boolean isFile(){
       return !isDirectory();
   }

   default void printInfo(){
       try {

           String str = "unknown";
           boolean isOther = false;
           if(isDirectory()){
               str = "dir";
           }else if(isFile()){
               str = "file,size:"+ SafUtil.fmtSpace(length());
           }else {
               isOther = true;
           }

           if(isOther){
               Log.e("smb",str+",name:"+getName()+",path:"+getPath()+
                       ", lastModified:"+lastModified());
           }else {
               Log.w("smb",str+",name:"+getName()+",path:"+getPath()+
                       ", lastModified:"+lastModified());
           }


       }catch (Throwable throwable){
           throwable.printStackTrace();
       }
   }
}
