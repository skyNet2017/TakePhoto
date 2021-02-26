package com.hss01248.media.mymediastore;

import android.net.Uri;

public interface IFile<T> {

    T[] listFiles();

   String getName();

    Uri getUri();

    String getPath();

   boolean isDirectory();

    long length();

    long lastModified();
}
