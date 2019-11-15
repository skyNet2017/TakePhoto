package com.sznq.finalcompress;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.hss01248.analytics.ad.AdUtil;
import com.hss01248.analytics.ReportUtil;
import com.hss01248.imginfo.ImageInfoFormater;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Created by hss on 2018/12/22.
 */

public class BaseApp extends Application {

    //NewPhotoAddedReceiver receiver;
    public static boolean isDebugable;
    static FileObserver observer;
    static Handler handler;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.e("willz", "App attachBaseContext");
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
       /* SuperHolderInitor.init(new IBindView() {
            @Override
            public void bind(Object o, View view) {
                ButterKnife.bind(o,view);
            }
        });*/
       handler = new Handler();
        isDebugable = BuildConfig.DEBUG;

        //registerFileReceiver(this);
        ImageInfoFormater.init(this);
        ReportUtil.init(this, "UA-131503834-1", false, isDebugable);
        // AdUtil.init(this,false,"ca-app-pub-2335840373239478~2863497563");

        //registerContentObserver();

        observerCamera();
        BlockCanary.install(this, new BlockCanaryContext(){
            @Override
            public int provideBlockThreshold() {
                return 400;
            }
        }).start();


    }

    private void observerCamera() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
        String cameraDir = file.getAbsolutePath();
         observer = new FileObserver(cameraDir,FileObserver.MOVED_TO) {
            // FileObserver.CREATE | FileObserver.MODIFY | FileObserver. |FileObserver.CLOSE_WRITE
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.w("FileObserver",event+",path:"+path);
                //FileObserver: 256,path:IMG_20190113_104421.jpg.tmp
                doCompress(path,file);

            }
        };
        observer.startWatching();
        /*new Thread(new Runnable() {
            @Override
            public void run() {


            }
        }).start();*/
    }

    private void doCompress(String fileName, File dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                File file = new File(dir,fileName);
                boolean shouldCompress =  PhotoCompressHelper.shouldCompress(file,true);
                if(shouldCompress){
                    PhotoCompressHelper.compressOneFile(file,true);
                    showToast(fileName);
                }

            }
        }).start();

    }

    private void showToast(String fileName) {
        if(handler != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),fileName+ " compressed!",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void registerContentObserver() {
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        getContentResolver().registerContentObserver(imageUri, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                Log.i("uri", uri.toString());
                //content://media/external/images/media
                Cursor cursor = getContentResolver().query(uri, null, null, null, "_data desc");
                if (cursor != null) {
                    Log.i("uri", "The number of data is:" + cursor.getCount());
                    StringBuffer sb = new StringBuffer();
                    while (cursor.moveToNext()) {
                        String fileName = cursor.getString(cursor.getColumnIndex("_data"));
                        String[] a = fileName.split("/");
                        Log.i("uri", a[a.length - 2] + a[a.length - 1]);

                    }
                }
            }
        });
    }

    private void registerFileReceiver(BaseApp baseApp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        //这里定义接受器监听广播的类型，这里添加相应的广播
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //实例化接收器
       /*receiver = new NewPhotoAddedReceiver();
        //注册事件，将监听类型赋给对应的广播接收器----所以这叫动态注册
        registerReceiver(receiver,intentFilter);*/
    }
}
