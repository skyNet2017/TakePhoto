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
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.darsh.multipleimageselect.compress.PhotoCompressHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyImageWatcher {

    static Map<String, FileObserver> observerMap = new HashMap<>();
   static Map<Object,String> descs = getDescs(FileObserver.class);
   static ExecutorService service;
    static Handler handler = new Handler(Looper.getMainLooper());


    public static void init(){
        if(!observerMap.isEmpty()){
            Iterator<Map.Entry<String, FileObserver>> iterator = observerMap.entrySet().iterator();
            while (iterator.hasNext()){
                iterator.next().getValue().stopWatching();
            }
            observerMap.clear();
        }

        watchDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        watchDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        addFileObserver(new File(Environment.getExternalStorageDirectory(),"BaiduNetdisk"));
    }

    private static void watchDir(File dcim) {
        File[] files = dcim.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean isFile =  name.contains(".") || name.contains("temp")
                        || name.contains("-compressed") || name.equals("cache");
                return !isFile;
            }
        });
        if(files == null || files.length == 0){
            return;
        }
        for (File file : files) {
            if(file.isDirectory()){
                MyImageWatcher.addFileObserver(file);
                watchDir(file);
            }
        }
    }


    /**
     *
     * @param dir 必须是最后一层的目录.
     */
    public static void addFileObserver(File dir){
        Log.w("FileObserver","监听文件夹path:"+dir.getAbsolutePath());
       FileObserver dcimObserver = new FileObserver(dir,FileObserver.MOVED_TO | FileObserver.CREATE | FileObserver.CLOSE_NOWRITE) {//,FileObserver.MOVED_TO | FileObserver.CREATE | FileObserver.CLOSE_NOWRITE
            //FileObserver.MOVED_TO |FileObserver.CREATE | FileObserver.MODIFY
            // FileObserver.CREATE | FileObserver.MODIFY | FileObserver. |FileObserver.CLOSE_WRITE
           Map<String,Runnable> map = new HashMap();
            @Override
            public void onEvent(int event, @Nullable String fileName) {
                //连续两次响应的处理:
                Log.i("监听","path:"+fileName+", event:"+event+", "+descs.get(event));
                //FileObserver: 256,path:IMG_20190113_104421.jpg.tmp
                //doCompress(path,new File(cameraDir));
                //if(event == FileObserver.MOVED_TO || event == FileObserver.CREATE){

                if(TextUtils.isEmpty(fileName)){
                    return;
                }
                if(event == FileObserver.CLOSE_NOWRITE){
                    runTask(1300);
                }else if(event == FileObserver.MOVED_TO || event == FileObserver.CREATE){
                    //event:16, close_nowrite 可能多次
                    File file = new File(dir,fileName);
                    if(file.isDirectory()){
                        Log.v("监听","文件夹新建事件:"+file.getAbsolutePath());
                        return;
                    }
                    if(fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".JPG")
                            || fileName.endsWith(".jpeg")){
                        File file1 = new File(dir,fileName);
                        String fullPath = file1.getAbsolutePath();

                        //doCompress(path,dir);
                        // doCompressByWorkManager(path,dir);

                        //doByMainHandler(map,path,dir);

                        //加入等待队列
                        ToCompressFileInfo fileInfo = new ToCompressFileInfo();
                        fileInfo.file = file1;
                        fileInfo.startTime = System.currentTimeMillis();
                        if(!fileInfoQueue.contains(fileInfo)){
                            fileInfoQueue.add(fileInfo);
                            Log.d("监听",Thread.currentThread().getName()+" thread ,文件新增,加入等待队列,准备几秒后压缩-path:"+fullPath);
                            runTask(4000);
                        }else {
                            Log.v("监听","已经加入过压缩队列,不再加入:"+file.getAbsolutePath());
                            runTask(3000);
                        }
                        //fileInfoQueue.add(fileInfo);


                        //从队列头部取值,看是否有超过4s的任务,有的话,切割,将超过4s的任务全部执行掉:



                    }else {
                        Log.v("监听","其他类型的文件新建:"+file.getAbsolutePath());
                    }
                }



            }
        };
        dcimObserver.startWatching();
        observerMap.put(dir.getAbsolutePath(),dcimObserver);
    }

    private static void runTask(int time) {
        if(fileInfoQueue.isEmpty()){
            Log.w("监听",Thread.currentThread().getName()+" thread ,没有要压缩的任务 ");
            return;
        }
        // 从队列头部取值,看是否有超过4s的任务,有的话,切割,将超过4s的任务全部执行掉:
        List<ToCompressFileInfo> todoTasks = new ArrayList<>();
        Iterator<ToCompressFileInfo> infoIterator = fileInfoQueue.iterator();
        while (infoIterator.hasNext()){
            ToCompressFileInfo fileInfo1 = infoIterator.next();
            if(System.currentTimeMillis()  - fileInfo1.startTime >= time){
                todoTasks.add(fileInfo1);
                infoIterator.remove();
            }
        }
        if(todoTasks.size() > 0){
            for (ToCompressFileInfo todoTask : todoTasks) {
                doCompressOnCurrent(todoTask.file.getName(),todoTask.file.getParentFile());
            }
            Log.w("监听",Thread.currentThread().getName()+" thread ,压"+todoTasks.size()+"张,还有"+fileInfoQueue.size()+"张在等待队列中");
            if(fileInfoQueue.size() > 0){
                //还剩下一些:  永远有一张在等待队列中
                               /* try {
                                    Thread.sleep(wait);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                for (ToCompressFileInfo todoTask : fileInfoQueue) {
                                    doCompressOnCurrent(todoTask.file.getName(),todoTask.file.getParentFile());
                                }
                                Log.w("监听",Thread.currentThread().getName()+" thread ,4s已过,剩下压缩完成,队列清空x条:"+fileInfoQueue.size());
                                fileInfoQueue.clear();*/
            }
        }else {
            //都是要4s后压缩的:
            Log.w("监听",Thread.currentThread().getName()+" thread ,都是要4s后压缩的 "+fileInfoQueue.size()+"张在等待队列中");
                           /* try {
                                Thread.sleep(wait);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (ToCompressFileInfo todoTask : fileInfoQueue) {
                                doCompressOnCurrent(todoTask.file.getName(),todoTask.file.getParentFile());
                            }
                            Log.w("监听",Thread.currentThread().getName()+" thread ,4s已过,全部压缩完成,队列清空x条:"+fileInfoQueue.size());
                            fileInfoQueue.clear();*/

        }
                        /*if(fileInfoQueue.size() == 1){
                            try {
                                Thread.sleep(wait);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (ToCompressFileInfo todoTask : fileInfoQueue) {
                                doCompressOnCurrent(todoTask.file.getName(),todoTask.file.getParentFile());
                            }
                            Log.w("监听",Thread.currentThread().getName()+" thread ,4s已过,全部压缩完成,队列清空x条:"+fileInfoQueue.size());
                            fileInfoQueue.clear();
                        }*/


    }

    private static void doByMainHandler(Map<String, Runnable> map, String path, File dir) {
        Runnable runnable = map.get(path);
        if(runnable != null){
            handler.removeCallbacks(runnable);
        }
        String fullPath = new File(dir,path).getAbsolutePath();
        runnable = new Runnable() {
            @Override
            public void run() {
                map.remove(path);
                Log.d("监听",Thread.currentThread().getName()+" thread ,真正开始压缩-path:"+fullPath);
                //doCompressOnCurrent(path,dir);
            }
        };
        map.put(path,runnable);
        handler.postDelayed(runnable,4000);
    }


    static List<ToCompressFileInfo> fileInfoQueue = new LinkedList<ToCompressFileInfo>();

    /**
     * 需要自己维护任务等待队列
     * @param fileName
     * @param dir
     */
    private static void doCompressOnCurrent(String fileName, File dir) {
        File file = new File(dir,fileName);
        boolean shouldCompress =  PhotoCompressHelper.shouldCompress(file,true);
        if(shouldCompress){
            /*try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            Log.d("监听",Thread.currentThread().getName()+" thread ,真正开始压缩-path:"+file.getAbsolutePath());
            PhotoCompressHelper.compressOneFile(file,true);
            refreshMediaCenter(BaseApp.app,file.getAbsolutePath());
            showToast(fileName);
        }else {
            Log.v("监听","无需压缩:"+file.getAbsolutePath());
        }
    }

    private static void doCompressByWorkManager(String path, File dir) {
        /*WorkManager.getInstance(BaseApp.app)
    .beginWith(Arrays.asList(new OneTimeWorkRequest.Builder().))
                .then(workC)
                .enqueue();*/
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
           // builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//设置所需网络类型
            builder.setRequiresDeviceIdle(false);//设置在DeviceIdle时执行Job
            builder.setRequiresCharging(false);//设置在充电时执行Job
            builder.setOverrideDeadline(2000);//// 任务deadline，当到期没达到指定条件也会开始执行
           // builder .setBackoffCriteria(3000,JobInfo.BACKOFF_POLICY_LINEAR); //设置退避/重试策略
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
            Log.w("监听","发送任务: mJobScheduler.schedule: path: "+fileName+" , 任务id:"+mJobId);
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
