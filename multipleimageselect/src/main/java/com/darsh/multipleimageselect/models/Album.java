package com.darsh.multipleimageselect.models;

/**
 * Created by Darshan on 4/14/2015.
 */
public class Album {
    public String name;
    public String cover;
    public int count;
    public String dir;
    public long fileSize;

    public Album(String name, String cover) {
        this.name = name;
        this.cover = cover;
        this.count = count;
    }
}
