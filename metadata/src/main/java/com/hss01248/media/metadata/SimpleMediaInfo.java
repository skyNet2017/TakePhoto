package com.hss01248.media.metadata;

public class SimpleMediaInfo {
    public int width;
    public int height;
    public int oration;
    public long duration;

    public String name;

    public SimpleMediaInfo(String uriOrPath) {
        this.uriOrPath = uriOrPath;
        parseSimpleInfo();
    }



    public String uriOrPath;

    private void parseSimpleInfo() {

    }






}
