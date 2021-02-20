package com.darsh.multipleimageselect.models;

import android.net.Uri;
import android.os.Build;

import androidx.documentfile.provider.DocumentFile;

import java.util.Objects;

/**
 * Created by Darshan on 4/14/2015.
 */
public class Album {
    public String name;
    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    public String cover;
    public int count;
    /**
     * 可能是纯文件路径,或者saf拿到的content://xxxx
     */
    public String dir;
    public long fileSize;
    public boolean fromFileApi;
    public boolean fromSAFApi;

   // public transient DocumentFile dirSaf;


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
