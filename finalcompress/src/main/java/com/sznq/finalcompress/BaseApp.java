package com.sznq.finalcompress;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.View;

import com.hss01248.adapter.IBindView;
import com.hss01248.adapter.SuperHolderInitor;
import com.hss01248.imginfo.ImageInfoFormater;


import butterknife.ButterKnife;

/**
 * Created by hss on 2018/12/22.
 */

public class BaseApp extends Application {

    //NewPhotoAddedReceiver receiver;
    @Override
    public void onCreate() {
        super.onCreate();
       /* SuperHolderInitor.init(new IBindView() {
            @Override
            public void bind(Object o, View view) {
                ButterKnife.bind(o,view);
            }
        });*/

        registerFileReceiver(this);
        ImageInfoFormater.init(this);


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
