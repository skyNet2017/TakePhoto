package com.sznq.finalcompress;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;
import android.util.Log;

import com.darsh.multipleimageselect.compress.StorageUtils;
import com.facebook.stetho.Stetho;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.ForegroundNotification;
import com.fanjun.keeplive.config.ForegroundNotificationClickListener;
import com.fanjun.keeplive.config.KeepLiveService;
import com.hss01248.analytics.ReportUtil;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.http.EverythingSearchParser;
import com.hss01248.media.mymediastore.http.HttpHelper;
import com.hss01248.media.mymediastore.smb.SmbjUtil;
import com.shizhefei.view.largeimage.BlockImageLoader;
import com.simple.spiderman.SpiderMan;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

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
        SafUtil.context = this;
       handler = new Handler();
        isDebugable = BuildConfig.DEBUG;
        SpiderMan.init(this);
        keepAlive();
        //registerFileReceiver(this);
        StorageUtils.context = this;
        ImageInfoFormater.init(this);
        ReportUtil.init(this, "UA-131503834-1", false, isDebugable);

        // AdUtil.init(this,false,"ca-app-pub-2335840373239478~2863497563");
       // BlockImageLoader.DEBUG = true;
        //registerContentObserver();

      boolean  isMainProcess = getApplicationContext().getPackageName().equals
                (getCurrentProcessName());

      if(isMainProcess){
          //开启保活后,不再走oncreate
          MyImageWatcher.init();
          Stetho.initializeWithDefaults(this);
          SmbUtil.init(this);

          //EverythingSearchParser.searchMediaType("http://122.226.210.62:121/");
          //EverythingSearchParser.searchDocType("http://122.226.210.62:121/");
         // EverythingSearchParser.searchDocType("http://192.168.3.8:9265/");
         // EverythingSearchParser.searchMediaType("http://192.168.3.8:9265/");
          new Thread(new Runnable() {
              @Override
              public void run() {
                 // SmbjUtil.connect();

                 /* HttpHelper.start("http://192.168.3.8:9265", new ScanFolderCallback() {
                      @Override
                      public void onComplete() {

                      }

                      @Override
                      public void onFromDB(List<BaseMediaFolderInfo> folderInfos) {

                      }

                      @Override
                      public void onScanEachFolder(List<BaseMediaFolderInfo> folderInfos) {

                      }

                      @Override
                      public void onScanFinished(List<BaseMediaFolderInfo> folderInfos) {

                      }
                  });*/
              }
          }).start();
      }






    }

    /**
     * 获取当前进程名
     */
    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService
                (Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    private void keepAlive() {
        //定义前台服务的默认样式。即标题、描述和图标
        ForegroundNotification foregroundNotification = new ForegroundNotification(
                getResources().getString(R.string.a_notify_keep_alive),
                getResources().getString(R.string.a_notify_keep_alive_desc), R.mipmap.ic_launcher,
                //定义前台服务的通知点击事件
                new ForegroundNotificationClickListener() {

                    @Override
                    public void foregroundNotificationClick(Context context, Intent intent) {
                        Intent intent1 = new Intent(context,MainActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);
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
