package com.hss01248.media.mymediastore.http;

import androidx.annotation.Keep;

@Keep
public class HttpResponseBean {

    public String url;
    public String name;
    public boolean isDir;
    public long fileSize;
    public long lastModified;
    public String path;

    @Override
    public String toString() {
        return "HttpResponseBean{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", isDir=" + isDir +
                ", fileSize=" + fileSize +
                ", lastModified=" + lastModified +
                ", parentUrl='" + path + '\'' +
                '}';
    }

    public HttpFile[] listFiles(){
        if(!isDir){
            return null;
        }
        return EverythingParser.start(url);
    }
}
