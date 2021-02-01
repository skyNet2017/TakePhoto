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
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.compress.StorageUtils;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.ForegroundNotification;
import com.fanjun.keeplive.config.ForegroundNotificationClickListener;
import com.fanjun.keeplive.config.KeepLiveService;
import com.hss01248.analytics.ReportUtil;
import com.hss01248.imginfo.ImageInfoFormater;
import com.simple.spiderman.SpiderMan;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hss on 2018/12/22.
 */

public class BaseApp extends Application {

    //NewPhotoAddedReceiver receiver;
    public static boolean isDebugable;
    static Handler handler;
    static Application app;

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
        app = this;
       handler = new Handler();
        isDebugable = BuildConfig.DEBUG;
        SpiderMan.init(this);
        keepAlive();
        //registerFileReceiver(this);
        StorageUtils.context = this;
        ImageInfoFormater.init(this);
        ReportUtil.init(this, "UA-131503834-1", false, isDebugable);
        // AdUtil.init(this,false,"ca-app-pub-2335840373239478~2863497563");

        //registerContentObserver();

        observerCamera();





    }

    private void keepAlive() {
        //定义前台服务的默认样式。即标题、描述和图标
        ForegroundNotification foregroundNotification = new ForegroundNotification("保活测试keepalive","用于及时压缩截图拍照图片", R.mipmap.ic_launcher,
                //定义前台服务的通知点击事件
                new ForegroundNotificationClickListener() {

                    @Override
                    public void foregroundNotificationClick(Context context, Intent intent) {
                    }
                });
        //启动保活服务
        KeepLive.startWork(this, KeepLive.RunMode.ROGUE, foregroundNotification,
                //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
                new KeepLiveService() {
                    /**
                     * 运行中
                     * 由于服务可能会多次自动启动，该方法可能重复调用
                     */
                    @Override
                    public void onWorking() {
                        Log.w("alive","onWorking()服务启动--->");
                        StorageUtils.context = BaseApp.app;
                        ImageInfoFormater.init(BaseApp.this);

                    }
                    /**
                     * 服务终止
                     * 由于服务可能会被多次终止，该方法可能重复调用，需同onWorking配套使用，如注册和注销broadcast
                     */
                    @Override
                    public void onStop() {
                        Log.w("alive","onStop()服务终止--->");
                    }
                }
        );
    }

    private void observerCamera() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");

        File screenshots = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Screenshots");

        File bilibili = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"bili/screenshot");

        File baidu = new File(Environment.getExternalStorageDirectory(),"BaiduNetdisk");

        MyImageWatcher.addFileObserver(file);
        MyImageWatcher.addFileObserver(screenshots);
        MyImageWatcher.addFileObserver(bilibili);
        MyImageWatcher.addFileObserver(baidu);

        ///sdcard/BaiduNetdisk
       /* String cameraDir = file.getAbsolutePath();
        Log.w("FileObserver","path:"+cameraDir);
        doObserver(file);
        doObserver2(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));*/

        //doObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

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
