package com.darsh.multipleimageselect.activities;

import android.os.Environment;
import android.util.Log;

import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.models.Album;
import com.darsh.multipleimageselect.saf.TfAlbumFinder;

import org.reactivestreams.Subscriber;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AlbumFinder {

    public static void listAllAlbum(final Observer<Album> observer) {
        final List<Album> albums = new ArrayList<>(512);
        getAlbums(Environment.getExternalStorageDirectory(), observer);

    }

    private static void getAlbums(final File dir, final Observer<Album> observer) {
        TfAlbumFinder.executorService.execute(new Runnable() {
            @Override
            public void run() {
                File[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    return;
                }
                Album album = null;
                int count = 0;
                for (File file : files) {
                    if (file.isDirectory()) {
                        //6500个文件夹
                        if ("MicroMsg".equals(file.getName())) {
                            continue;
                        }
                        //700多个
                        if ("MobileQQ".equals(file.getName())) {
                            continue;
                        }
                /*if("sogou".equals(file.getName())){
                    continue;
                }
                if("Huawei".equals(file.getName())){
                    continue;
                }*/
                /*if(file.getName().startsWith(".")){
                    continue;
                }*/
                        //Log.d("监听","进入文件夹遍历:"+dir.getAbsolutePath());
                        getAlbums(file, observer);
                    } else {
                        String name = file.getName();
                        if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif")
                                || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")) {
                            count++;

                            if (album != null) {
                                album.fileSize = album.fileSize + file.length();
                                continue;
                            }
                            album = new Album(dir.getName(), file.getAbsolutePath());
                            album.fromFileApi = true;
                            album.fileSize = file.length();
                            album.dir = dir.getAbsolutePath();
                            Log.d("监听", "添加有图文件夹:" + dir.getAbsolutePath());
                        }
                    }
                }
                if (album != null) {
                    album.count = count;
                    observer.onNext(album);
                }
            }
        });

    }


}
