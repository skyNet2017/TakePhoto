package com.sznq.finalcompress;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.View;

import com.hss01248.adapter.IBindView;
import com.hss01248.adapter.SuperHolderInitor;
import com.hss01248.analytics.ReportUtil;
import com.hss01248.imginfo.ImageInfoFormater;


import butterknife.ButterKnife;

/**
 * Created by hss on 2018/12/22.
 */

public class BaseApp extends Application {

    //NewPhotoAddedReceiver receiver;
    public static boolean isDebugable;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.e("willz","App attachBaseContext");
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
       isDebugable = BuildConfig.DEBUG;

        //registerFileReceiver(this);
        ImageInfoFormater.init(this);
        ReportUtil.init(this,"UA-131503834-1",false,isDebugable);





    }

    private void registerFileReceiver(BaseApp baseApp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        IntentFilter intentFilter=new IntentFilter();
        //这里定义接受器监听广播的类型，这里添加相应的广播
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //实例化接收器
       /*receiver = new NewPhotoAddedReceiver();
        //注册事件，将监听类型赋给对应的广播接收器----所以这叫动态注册
        registerReceiver(receiver,intentFilter);*/
    }
}
