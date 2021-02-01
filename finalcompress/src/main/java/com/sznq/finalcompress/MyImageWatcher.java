package com.sznq.finalcompress;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.darsh.multipleimageselect.compress.PhotoCompressHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyImageWatcher {

    static Map<String, FileObserver> observerMap = new HashMap<>();
   static Map<Object,String> descs = getDescs(FileObserver.class);
   static ExecutorService service;
    static Handler handler;


    /**
     *
     * @param dir 必须是最后一层的目录.
     */
    public static void addFileObserver(File dir){
        Log.w("FileObserver","监听文件夹path:"+dir.getAbsolutePath());
       FileObserver dcimObserver = new FileObserver(dir,FileObserver.MOVED_TO |FileObserver.CREATE) {
            //FileObserver.MOVED_TO |FileObserver.CREATE | FileObserver.MODIFY
            // FileObserver.CREATE | FileObserver.MODIFY | FileObserver. |FileObserver.CLOSE_WRITE
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.i("监听","path:"+path+", event:"+event+", "+descs.get(event));
                //FileObserver: 256,path:IMG_20190113_104421.jpg.tmp
                //doCompress(path,new File(cameraDir));
                //if(event == FileObserver.MOVED_TO || event == FileObserver.CREATE){
                if(TextUtils.isEmpty(path)){
                    return;
                }
               /* if(path.endsWith(".tmp")){
                    return;
                }*/

                File file = new File(dir,path);
                if(file.isDirectory()){
                    Log.w("监听","文件夹新建事件:"+file.getAbsolutePath());
                    return;
                }
                if(path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".JPG")
                        || path.endsWith(".jpeg")){
                    String fullPath = new File(dir,path).getAbsolutePath();
                    Log.w("监听","文件新增,准备压缩-path:"+fullPath);
                    doCompress(path,dir);
                }else {
                    Log.w("监听","其他类型的文件新建:"+file.getAbsolutePath());
                }

            }
        };
        dcimObserver.startWatching();
        observerMap.put(dir.getAbsolutePath(),dcimObserver);
    }


    public static Map<Object,String> getDescs( Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Map<Object,String> valueToDesc = new HashMap<>();
        if(fields.length > 0){
            for (Field field : fields) {
                if(Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())){
                    field.setAccessible(true);
                    try {
                        valueToDesc.put(field.get(clazz),field.getName().toLowerCase());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return valueToDesc;
    }

    private static ComponentName mServiceComponent;
    private static int mJobId;
    static JobScheduler mJobScheduler;
    private static void doCompress(String fileName, File dir) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//根据JobService创建一个ComponentName对象
            mServiceComponent = new ComponentName(BaseApp.app, MyJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(++mJobId, mServiceComponent);
           // builder.setMinimumLatency(200);//设置延迟调度时间
            //builder.setOverrideDeadline(2000);//设置该Job截至时间，在截至时间前肯定会执行该Job
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//设置所需网络类型
            builder.setRequiresDeviceIdle(false);//设置在DeviceIdle时执行Job
            builder.setRequiresCharging(false);//设置在充电时执行Job
            builder.setPersisted(true);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString("dir",dir.getAbsolutePath());
            bundle.putString("fileName",fileName);
            builder.setExtras(bundle);//设置一个额外的附加项


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                //builder.setMinimumLatency(200);
            } else {
               // builder.setPeriodic(200);
            }



            JobInfo  mJobInfo = builder.build();

            if(mJobScheduler == null){
                mJobScheduler = (JobScheduler) BaseApp.app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            }

            mJobScheduler.schedule(mJobInfo);//调度Job
            Log.w("监听","发送任务: mJobScheduler.schedule: path: "+fileName);
            /*mBuilder = new JobInfo.Builder(id,new ComponentName(this, MyJobService.class));

            JobInfo  mJobInfo = mBuilder.build();
            mJobScheduler.schedule(builder.build());//调度Job
            mJobScheduler.cancel(jobId);//取消特定Job
            mJobScheduler.cancelAll();//取消应用所有的Job*/
        }

    }

    public static void doBg(String fileName, File dir){
        if(service == null){
            service = Executors.newSingleThreadExecutor();
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(dir,fileName);
                boolean shouldCompress =  PhotoCompressHelper.shouldCompress(file,true);
                if(shouldCompress){
                    PhotoCompressHelper.compressOneFile(file,true);
                    refreshMediaCenter(BaseApp.app,file.getAbsolutePath());
                    showToast(fileName);
                }else {
                    Log.w("dd","无需压缩:"+file.getAbsolutePath());
                }

            }
        });
    }

    private static void showToast(String fileName) {
        if(handler == null){
            handler = new Handler(Looper.getMainLooper());
        }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseApp.app,fileName+ " compressed!",Toast.LENGTH_SHORT).show();
                }
            });

    }


    public static  void refreshMediaCenter(Context activity, String filePath){
        if (Build.VERSION.SDK_INT>19){
            String mineType =getMineType(filePath);

            saveImageSendScanner(activity,new MyMediaScannerConnectionClient(filePath,mineType));
        }else {

            saveImageSendBroadcast(activity,filePath);
        }
    }

    public static String getMineType(String filePath) {

        String type = "text/plain";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;


       /* MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "text/plain";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;*/
    }

    /**
     * 保存后用广播扫描，Android4.4以下使用这个方法
     * @author YOLANDA
     */
    private static void saveImageSendBroadcast(Context activity, String filePath){
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
    }

    /**
     * 保存后用MediaScanner扫描，通用的方法
     *
     */
    private static void saveImageSendScanner (Context context, MyMediaScannerConnectionClient scannerClient) {

        final MediaScannerConnection scanner = new MediaScannerConnection(context, scannerClient);
        scannerClient.setScanner(scanner);
        scanner.connect();
    }
    private   static class MyMediaScannerConnectionClient implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mScanner;

        private String mScanPath;
        private String mimeType;

        public MyMediaScannerConnectionClient(String scanPath, String mimeType) {
            mScanPath = scanPath;
            this.mimeType = mimeType;
        }

        public void setScanner(MediaScannerConnection con) {
            mScanner = con;
        }

        @Override
        public void onMediaScannerConnected() {
            mScanner.scanFile(mScanPath, mimeType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mScanner.disconnect();
        }
    }
}
