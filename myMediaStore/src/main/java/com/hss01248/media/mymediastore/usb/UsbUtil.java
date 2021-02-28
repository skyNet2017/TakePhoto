package com.hss01248.media.mymediastore.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.partition.Partition;

/**
 * https://www.codenong.com/cs70146041/
 * https://github.com/1hakr/AnExplorer
 */
public class UsbUtil {
    private static final String ACTION_USB_PERMISSION = "com.android.hss01248.USB_PERMISSION";

    public static final String  TAG = "usb";

    private  static BroadcastReceiver mUsbReceiver;
    static Context context;
    public static void regist(Context context){
        UsbUtil.context = context.getApplicationContext();
        if(mUsbReceiver == null){
            //监听otg插入 拔出
            IntentFilter usbDeviceStateFilter = new IntentFilter();
            usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//注册监听自定义广播
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            mUsbReceiver = initUsbReceiver();
            context.getApplicationContext().registerReceiver(mUsbReceiver, filter);
        }

    }

    private static BroadcastReceiver initUsbReceiver() {
        BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case ACTION_USB_PERMISSION://接受到自定义广播
                        synchronized (this) {
                            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) { //允许权限申请
                                if (usbDevice != null) {
                                    //Do something
                                    readUsbDevice(usbDevice);
                                }
                            } else {
                                TShow("用户未授权，读取失败");
                            }
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到存储设备插入广播
                        UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device_add != null) {
                            TShow("接收到存储设备插入广播");
                            readUsbDevice(device_add);
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到存储设备拔出广播
                        UsbDevice device_remove = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device_remove != null) {
                            TShow("接收到存储设备拔出广播");
                            //拔出或者碎片 Activity销毁时 释放引用
                            //device.close();
                        }
                        break;
                }
            }
        };
        return mUsbReceiver;
    }

    private static void readUsbDevice(UsbDevice device_add) {
        //获取管理者
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        //枚举设备
        UsbMassStorageDevice[] storageDevices = UsbMassStorageDevice.getMassStorageDevices(context);//获取存储设备
        //需要activity?
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        for (UsbMassStorageDevice device : storageDevices) {//可能有几个 一般只有一个 因为大部分手机只有1个otg插口
            if (usbManager.hasPermission(device.getUsbDevice())) {//有就直接读取设备是否有权限
                read(device);
            } else {//没有就去发起意图申请
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent); //该代码执行后，系统弹出一个对话框，
            }
        }
    }

    private static void read(UsbMassStorageDevice massDevice) {
        // before interacting with a device you need to call init()!
        try {
            massDevice.init();//初始化
            //Only uses the first partition on the device
            Partition partition = massDevice.getPartitions().get(0);
            FileSystem currentFs = partition.getFileSystem();
            //fileSystem.getVolumeLabel()可以获取到设备的标识
            //通过FileSystem可以获取当前U盘的一些存储信息，包括剩余空间大小，容量等等
            Log.d(TAG, "Capacity: " + currentFs.getCapacity());
            Log.d(TAG, "Occupied Space: " + currentFs.getOccupiedSpace());
            Log.d(TAG, "Free Space: " + currentFs.getFreeSpace());
            Log.d(TAG, "Chunk size: " + currentFs.getChunkSize());
            UsbFile root = currentFs.getRootDirectory();
        } catch (Throwable e) {
            e.printStackTrace();
            TShow("读取失败");
        }
    }

    private static void TShow(String str) {
        Toast.makeText(context,str,Toast.LENGTH_LONG).show();
    }
}
