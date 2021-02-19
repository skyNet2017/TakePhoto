package com.darsh.multipleimageselect.saf;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.darsh.multipleimageselect.models.Album;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TfAlbumFinder {

    public static void listSafAlbum(final Observer<Album> observer){
        if(SafUtil.sdRoot == null){
            Log.w(SafUtil.TAG,Thread.currentThread().getName()+"  SafUtil.sdRoot is null");
            return;
        }
        final List<Album> albums = new ArrayList<>(300);
        getAlbums(SafUtil.sdRoot,observer);

    }

  public   static ExecutorService executorService = Executors.newFixedThreadPool(20);

    private static void getAlbums(final DocumentFile dir, final Observer<Album> observer) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DocumentFile[] files = dir.listFiles();
                if(files == null || files.length ==0){
                    return;
                }
                Album album = null;
                int count = 0;
                Log.d(SafUtil.TAG,Thread.currentThread().getName()+"  展开文件夹:"+ Uri.decode(dir.getUri().toString()));
                for (DocumentFile file : files) {
                    if(file.isDirectory()){
                        //6500个文件夹
                        if("MicroMsg".equals(file.getName())){
                            continue;
                        }
                        //700多个
                        if("MobileQQ".equals(file.getName())){
                            continue;
                        }
                        getAlbums(file,observer);
                    }else {
                        String name = file.getName();
                        if(name.endsWith(".jpg")|| name.endsWith(".png") || name.endsWith(".gif")
                                || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")){
                            count++;
                            if(album != null){
                                album.fileSize = file.length() + album.fileSize;
                                continue;
                            }
                            album = new Album(dir.getName(),file.getUri().toString());
                            album.fromSAFApi = true;
                            album.fileSize = file.length();
                            album.dir = dir.getUri().toString();
                            album.cover2 = file.getUri();
                            album.dir2 = dir.getUri();
                            album.dirSaf = dir;
                            Log.d(SafUtil.TAG,Thread.currentThread().getName()+"  添加有图文件夹:"+album.dir);
                        }
                    }
                }
                if(album != null){
                    album.count = count;
                    observer.onNext(album);
                }
            }
        });


    }
}
