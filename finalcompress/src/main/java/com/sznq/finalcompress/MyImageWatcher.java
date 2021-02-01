package com.sznq.finalcompress;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
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
                if(path.endsWith(".tmp")){
                    return;
                }
                File file = new File(dir,path);
                if(file.isDirectory()){
                    Log.w("监听","文件夹新建事件:"+file.getAbsolutePath());
                    return;
                }

                String fullPath = new File(dir,path).getAbsolutePath();
                Log.w("监听","文件新增,准备压缩-path:"+fullPath);
                doCompress(path,dir);
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


    private static void doCompress(String fileName, File dir) {
        if(service == null){
            service = Executors.newSingleThreadExecutor();
        }



        service.execute(new Runnable() {
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
                    refreshMediaCenter(BaseApp.app,file.getAbsolutePath());
                    showToast(fileName);
                }else {
                    Log.w("dd","无需压缩:"+file.getAbsolutePath());
                }

            }
        });

    }

    private static void showToast(String fileName) {
        if(handler != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseApp.app,fileName+ " compressed!",Toast.LENGTH_SHORT).show();
                }
            });
        }
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
