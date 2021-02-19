package com.darsh.multipleimageselect.saf;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.darsh.multipleimageselect.models.Album;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hss01248.imginfo.ImageInfoFormater;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TfAlbumFinder {

    public static void listSafAlbum(final Observer<Album> observer,final Observer<List<Album>> observer2){
        if(SafUtil.sdRoot == null){
            Log.w(SafUtil.TAG,Thread.currentThread().getName()+"  SafUtil.sdRoot is null");
            return;
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //使用文件缓存或者数据库缓存:
                File file = new File(ImageInfoFormater.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"safcache.json");
                if(file.exists()){
                    try {
                        String json = FileUtils.readFileToString(file);
                        if(!TextUtils.isEmpty(json)){
                            List<Album> albums = new Gson().fromJson(json, new TypeToken<List<Album>>(){}.getType());
                            if(albums.size() >0){
                                observer2.onNext(albums);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });



        //并行递归,如何判断最终完成?
        getAlbums(SafUtil.sdRoot,observer);

    }

  public   static ExecutorService executorService = Executors.newFixedThreadPool(20);

   static  AtomicInteger countGet = new AtomicInteger(0);
    private static void getAlbums(final DocumentFile dir, final Observer<Album> observer) {
        Log.d(SafUtil.TAG,"开始遍历当前文件夹,原子count计数:"+countGet.incrementAndGet()+", "+dir.getName());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DocumentFile[] files = dir.listFiles();
                if(files == null || files.length ==0){
                    int count0 = countGet.decrementAndGet();
                    if(count0 ==0){
                        observer.onComplete();
                    }
                    Log.d(SafUtil.TAG,"遍历当前一层文件夹完成,原子count计数:"+count0+", "+dir.getName());
                    return;
                }
                Album album = null;
                int count = 0;
                //Log.d(SafUtil.TAG,Thread.currentThread().getName()+"  展开文件夹:"+ Uri.decode(dir.getUri().toString()));
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
                            //Log.d(SafUtil.TAG,Thread.currentThread().getName()+"  添加有图文件夹:"+album.dir);
                        }
                    }
                }
                if(album != null){
                    album.count = count;
                    observer.onNext(album);
                }
                int count0 = countGet.decrementAndGet();
                if(count0 ==0){
                    observer.onComplete();
                }
                Log.d(SafUtil.TAG,"遍历当前一层文件夹完成,原子count计数:"+count0+", "+dir.getName());
            }
        });


    }
}
