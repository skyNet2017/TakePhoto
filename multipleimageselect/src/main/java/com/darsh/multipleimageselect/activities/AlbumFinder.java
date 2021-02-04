package com.darsh.multipleimageselect.activities;

import android.os.Environment;
import android.util.Log;

import com.darsh.multipleimageselect.models.Album;

import org.reactivestreams.Subscriber;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AlbumFinder {

    public static void listAllAlbum(Observer<List<Album>> observer){
        Observable.just(Environment.getExternalStorageDirectory())
                .map(new Function<File, List<Album>>() {
                    @Override
                    public List<Album> apply(File file) throws Exception {
                        List<Album> albums = new ArrayList<>();
                         getAlbums(file,albums);
                         return albums;
                    }
                })
                .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);

    }

    private static void getAlbums(File dir, List<Album> albums) {
        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            return ;
        }
        boolean havePic = false;
        for (File file : files) {
            if(file.isDirectory()){
                if("MicroMsg".equals(file.getName())){
                    continue;
                }
                if("MobileQQ".equals(file.getName())){
                    continue;
                }
                if(file.getName().startsWith(".")){
                    continue;
                }
                //Log.d("监听","进入文件夹遍历:"+dir.getAbsolutePath());
                getAlbums(file,albums);
            }else {
                if(havePic){
                    continue;
                }
                String name = file.getName();
                if(name.endsWith(".jpg")|| name.endsWith(".png") || name.endsWith(".gif")
                || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")){
                    havePic = true;
                    Album album = new Album(dir.getName(),file.getAbsolutePath());
                    albums.add(album);
                    Log.d("监听","添加有图文件夹:"+dir.getAbsolutePath());
                }
            }
        }
    }


}
