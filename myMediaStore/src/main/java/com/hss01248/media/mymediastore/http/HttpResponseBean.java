package com.hss01248.media.mymediastore.http;

import android.text.TextUtils;

import androidx.annotation.Keep;

import java.net.URLEncoder;

@Keep
public class HttpResponseBean {

    public String name;
    public String size;
    public String date_modified;

    public String getParentPath() {
        return path;
    }

    public String path;
    public String type;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        transPath();
    }

    public String host;





    public HttpFile[] listFiles(){
        if(!isDir()){
            return null;
        }
        return EverythingParser.start(getUrl());
    }

    private void transPath(){
        if(TextUtils.isEmpty(path)){
            path = host;
        }else {
            path = path.replaceAll("\\\\","/");
            path = host+ "/"+URLEncoder.encode(path);
        }

    }

    public String getUrl() {
        return path+"/"+name;
    }

    public boolean isDir() {
        return "folder".equals(type);
    }

   public long length(){
        try{
            return Long.parseLong(size);
        }catch (Throwable throwable){
            //throwable.printStackTrace();
            return 0;
        }
    }

    public long lastModified() {
        try{
            //epoch = (DateTime.Now.ToUniversalTime().Ticks - 621355968000000000) / 10000000
            //132587965293998560
            //132568539060000000
            //621355968000000000
            //1561025865348

            //132587959967616901   2021/3/16 15:40
            //1615880400
            return ( Long.parseLong(date_modified)/10000 - 10^13 );
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return 0;
        }
    }
}
