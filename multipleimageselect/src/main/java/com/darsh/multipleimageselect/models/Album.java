package com.darsh.multipleimageselect.models;

import android.os.Build;

import java.util.Objects;

/**
 * Created by Darshan on 4/14/2015.
 */
public class Album {
    public String name;
    public String cover;
    public int count;
    public String dir;
    public long fileSize;
    public boolean fromFileApi;

    public Album(String name, String cover) {
        this.name = name;
        this.cover = cover;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(dir, album.dir);
        }else {
          return   dir.equals(album.dir);
        }
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(dir);
        }else {
            return dir.hashCode()+90;
        }
    }
}
