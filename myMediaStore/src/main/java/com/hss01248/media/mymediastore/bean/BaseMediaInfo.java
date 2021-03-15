package com.hss01248.media.mymediastore.bean;

import android.os.Build;

import androidx.annotation.Keep;

import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.hss01248.media.mymediastore.smb.SmbToHttp;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.Objects;
import org.greenrobot.greendao.annotation.Generated;

@Keep
@Entity
public class BaseMediaInfo extends BaseInfo{

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_AUDIO = 3;
    public static final int TYPE_DOC_PDF = 4;//pdf
    public static final int TYPE_DOC_WORD = 5;//msword
    public static final int TYPE_DOC_EXCEL = 6;//excel
    public static final int TYPE_DOC_PPT = 7;//powerpoint
    public static final int TYPE_DOC_TXT = 8;  //文件名.txt

    public static final int TYPE_UNKNOWN = -1;
    @Index
    public String dir;


    @Index
    public String name;

    public int  hidden = 0;

    public IFile getFile() {
        if(file == null){
            genFile();
        }
        return file;
    }

    public transient IFile file;

    public void genFile(){
        if(path.startsWith("smb://")){
            this.file = SmbToHttp.getFile(path);
        }else if(path.startsWith("content://")){
            file = new IDocumentFile(SafUtil.findFile(SafUtil.sdRoot, path));
        }else if(path.startsWith("/storage/")){
            file = new JavaFile(new java.io.File(path));
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseMediaInfo)) return false;
        BaseMediaInfo that = (BaseMediaInfo) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(path, that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(path);
        }
        return 908;
    }

    public void fillMediaInfo(){

    }

    public boolean isMedia(){
        return mediaType <4 && mediaType !=0;
    }

    public boolean isDoc(){
        return mediaType >3 ;
    }

    public String getDir() {
        return this.dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }









    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    @Id
    public String path;
    @Index
    public long fileSize;
    @Index
    public long updatedTime;
    @Index
    public int maxSide;
    @Index
    public int duration;


    

    public int mediaType;
    public Integer praiseCount;
    public int diskType;




    }















