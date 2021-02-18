package com.darsh.multipleimageselect.activities;

import android.os.Environment;
import android.util.Log;

import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.models.Album;

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

    public static void listAllAlbum(final Observer<List<Album>> observer){
        final List<Album> albums = new ArrayList<>();
        Observable.just(Environment.getExternalStorageDirectory())

                .map(new Function<File, List<Album>>() {
                    @Override
                    public List<Album> apply(File file) throws Exception {
                         getAlbums(file,albums);
                        Log.d("donext",Thread.currentThread().getName()+", album--:"+albums.size());
                        /*Collections.sort(albums, new Comparator<Album>() {
                            @Override
                            public int compare(Album o1, Album o2) {
                                return (int) (o2.count - o1.count);
                            }
                        });*/

                         return albums;
                    }
                })
                .subscribeOn(Schedulers.io())
                /*.observeOn(Schedulers.io())
                //list在流中使用flatmap拆分
                .flatMap(new Function<List<Album>, ObservableSource<Album>>() {
                    @Override
                    public ObservableSource<Album> apply(List<Album> albums) throws Exception {
                        //将list拆成单个的任务,分配到各个线程执行
                        return Observable.fromIterable(albums)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io());
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Album>() {
                    @Override
                    public void accept(Album album) throws Exception {
                        Log.d("donext",Thread.currentThread().getName()+", album:"+album.dir);
                        File dir = new File(album.dir);
                        File[] files = dir.listFiles();
                        int count = 0;
                        long fileSize = 0;
                        for (File file1 : files){
                            if(PhotoCompressHelper.isImage(file1)){
                                count++;
                                fileSize = fileSize + file1.length();
                            }
                        }
                        album.count = count;
                        album.fileSize = fileSize;
                    }
                })*/
                //然后使用来聚合上述的分支:
                /*.compose(new ObservableTransformer<Album, List<Album>>() {
                    @Override
                    public ObservableSource<List<Album>> apply(Observable<Album> upstream) {
                        Log.d("donext",Thread.currentThread().getName()+", album--:");
                        return Observable.just(albums);
                    }
                })*/


        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( observer/*new Observer<Album>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Album album) {

            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onNext(albums);
                observer.onComplete();
            }
        }*/);

    }

    private static void getAlbums(File dir, List<Album> albums) {
        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            return ;
        }
        boolean havePic = false;
        for (File file : files) {
            if(file.isDirectory()){
                //6500个文件夹
                if("MicroMsg".equals(file.getName())){
                    continue;
                }
                //700多个
                if("MobileQQ".equals(file.getName())){
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
                    album.count = dir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if(name.endsWith(".jpg")|| name.endsWith(".png") || name.endsWith(".gif")
                                    || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")
                                    || name.endsWith(".PNG")){
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    album.fromFileApi = true;
                    album.dir = dir.getAbsolutePath();
                    albums.add(album);
                    Log.d("监听","添加有图文件夹:"+dir.getAbsolutePath());
                }
            }
        }
    }


}
