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
            if(TextUtils.isEmpty(size)){
                return 0;
            }
            return Long.parseLong(size);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return 0;
        }
    }

    //文件时间用64位的数据长度存储，记录了从1601年1月1日00时00分00秒到现在以100纳秒(ns)为单位的UTC时间
    public long lastModified() {
        try{
            //epoch = (DateTime.Now.ToUniversalTime().Ticks - 621355968000000000) / 10000000
            //132587965293998560
            //132568539060000000
            //621355968000000000
            //1561025865348

            //132587959967616901   2021/3/16 15:40
            //1615880400
           // return ( Long.parseLong(date_modified)/10000 - 10^13 );
           return  (Long.parseLong(date_modified) - 116444736000000000L) / 10000000;
        }catch (Throwable throwable){
            throwable.printStackTrace();
            return 0;
        }
    }


    void TimeStampToFileTime(long timeStamp, int[] fileTime)
    {
        long nll = timeStamp * 10000000 + 116444736000000000L;
        fileTime[0] = (int) nll;
        fileTime[1] = (int) (nll >> 32);
        //fileTime.dwLowDateTime = (DWORD) nll;
       // fileTime.dwHighDateTime = nll >> 32;
    }

    void FileTimeToTimeStamp(int[] fileTime, long timeStamp) {
      // long timeStamp = ((INT64) fileTime.dwHighDateTime << 32) + fileTime.dwLowDateTime;
       // timeStamp = (timeStamp - 116444736000000000L) / 10000000;
    }

/*        版权声明：本文为CSDN博主「AlgoThinking」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
        原文链接：https://blog.csdn.net/AlgoThinking/article/details/39079273*/

    @Override
    public String toString() {
        return "HttpResponseBean{" +
                "name='" + name + '\'' +
                ", size='" + size + '\'' +
                ", date_modified='" + date_modified + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
