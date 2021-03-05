package com.hss01248.media.mymediastore.http;

public class HttpResponseBean {

    public String url;
    public String name;
    public boolean isDir;
    public long fileSize;
    public long lastModified;
    public String parentUrl;

    @Override
    public String toString() {
        return "HttpResponseBean{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", isDir=" + isDir +
                ", fileSize=" + fileSize +
                ", lastModified=" + lastModified +
                ", parentUrl='" + parentUrl + '\'' +
                '}';
    }

    public HttpFile[] listFiles(){
        if(!isDir){
            return null;
        }
        return EverythingParser.start(url);
    }
}
