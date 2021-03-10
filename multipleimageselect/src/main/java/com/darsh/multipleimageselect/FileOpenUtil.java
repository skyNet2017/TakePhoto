package com.darsh.multipleimageselect;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.darsh.multipleimageselect.activities.ImageSelectActivity;
import com.darsh.multipleimageselect.compress.CompressResultCompareActivity;
import com.hss01248.media.localvideoplayer.VideoPlayUtil;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.http.HttpHelper;
import com.hss01248.media.mymediastore.smb.SmbjUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

public class FileOpenUtil {

    public static void open(Context context, String path){
        open(context,path,null,0);
    }


    public static void open(Context context, String path, List<String> files,int position){
        if(files == null){
            files = new ArrayList<>();
            files.add(path);
            position = 0;
        }
        int type = FileTypeUtil.getTypeByFileName(path);
        if(type == BaseMediaInfo.TYPE_IMAGE){
            //点击去预览
            CompressResultCompareActivity.lauchForPreview(context, files, position);
        }else if(type == BaseMediaInfo.TYPE_VIDEO || type == BaseMediaInfo.TYPE_AUDIO){
            if(path.startsWith("smb:")){
                playByOther(context,path);
            }else {
                if(path.endsWith(".mp4")|| type == BaseMediaInfo.TYPE_AUDIO){
                    VideoPlayUtil.startPreviewInList(context,files,position);
                }else {
                    viewVideo(context,path);
                }

            }
        }else {
            viewVideo(context,path);
        }

    }

    private static void playByOther(Context context,String pathOrUri) {
        try {
            Uri uri = Uri.parse(pathOrUri);
            String host = uri.getHost();
            String newHost = SmbjUtil.username+":"+SmbjUtil.password+"@"+host;
            pathOrUri = pathOrUri.replace(host,newHost);
            uri = Uri.parse(pathOrUri);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(uri, "video/mp4");
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

    }


    private static void viewVideo(Context context,String pathOrUri) {
        try {
            if (pathOrUri.startsWith("/storage/") || pathOrUri.startsWith("/data/")) {
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //判断版本是否在7.0以上
                    uri =
                            MySelectFileProvider.getUriForFile(context.getApplicationContext(),
                                    context.getPackageName() + ".selectfileprovider",
                                    new File(pathOrUri));
                    //添加这一句表示对目标应用临时授权该Uri所代表的文件
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    uri = Uri.fromFile(new File(pathOrUri));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            } else {

                if(pathOrUri.startsWith("http")){
                    int type = FileTypeUtil.getTypeByFileName(pathOrUri);
                    if(type == BaseMediaInfo.TYPE_VIDEO ){
                        Uri uri = Uri.parse(pathOrUri);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(uri, "video/*");
                        context.startActivity(intent);
                    }else {
                        //先下载,下载完成后再调用此方法:
                        downloadAndCache(context,pathOrUri);
                    }
                }else {
                    Uri uri = Uri.parse(pathOrUri);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }


        }catch (Throwable throwable){
            throwable.printStackTrace();
            Toast.makeText(context,throwable.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private static void downloadAndCache(Context context,String pathOrUri) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("下载文件后打开:"+pathOrUri.substring(pathOrUri.lastIndexOf("/")+1));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String name = pathOrUri.substring(pathOrUri.lastIndexOf("/")+1,pathOrUri.lastIndexOf("."));
                String suffix = pathOrUri.substring(pathOrUri.lastIndexOf("."));
                String md5 = EncryptUtils.encryptMD2ToString(pathOrUri)+suffix;

                String fileName = name+"-"+md5+suffix;

                File dir  = new File(context.getExternalCacheDir(),"downloadxx");
                dir.mkdirs();
                File file = new File(dir,fileName);
                if(file.exists()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewVideo(context,file.getAbsolutePath());
                        }
                    });

                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.show();
                    }
                });

                String[] msg = new String[1];
                InputStream inputStream = HttpHelper.getInputStream(pathOrUri,msg);
                if(inputStream == null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ToastUtils.showLong("下载失败:"+msg[0]);

                        }
                    });
                    return;
                }
                FileIOUtils.writeFileFromIS(file,inputStream);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        viewVideo(context,file.getAbsolutePath());
                    }
                });
            }
        }).start();
    }
}
