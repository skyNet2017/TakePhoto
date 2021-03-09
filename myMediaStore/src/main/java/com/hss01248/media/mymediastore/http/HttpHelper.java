package com.hss01248.media.mymediastore.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hss01248.media.mymediastore.SafFileFinder;
import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.smb.FileApiForSmb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpHelper {

    static OkHttpClient client;
    public static OkHttpClient getClient(){
        if(client == null){
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    //.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            client = builder.build();
        }
        return client;
    }

    public static void login(String url,String name,String pwd){

    }

    public static boolean shouldScan(){
        SharedPreferences sp = SafUtil.context.getSharedPreferences(SafFileFinder.SP_NAME, Context.MODE_PRIVATE);
        long latScanFinishedTime = sp.getLong("lasthttpScanTime", 0);
        if(latScanFinishedTime ==0){
            return true;
        }
        return   System.currentTimeMillis() - latScanFinishedTime > 3 * 60 * 60 * 1000;
    }

    public static void start(String url, final ScanFolderCallback observer){
        if(!shouldScan()){
            Log.w("http","还没到扫描间隔");
            return;
        }
        //ExecutorService executorService = Executors.newFixedThreadPool(2);
        HttpFile[] files = EverythingParser.start(url);
        if(files !=null && files.length > 0){
            for (HttpFile file : files) {
                if(file.getPath().contains("/G%3A")||file.getPath().contains("/H%3A") ||file.getPath().contains("/I%3A")){
                    new SafFileFinder22<HttpFile>().getAlbums(file, Executors.newFixedThreadPool(2), observer);
                }

            }
        }
        SafUtil.context.getSharedPreferences(SafFileFinder.SP_NAME, Context.MODE_PRIVATE)
                .edit().putLong("lasthttpScanTime",System.currentTimeMillis()).commit();
    }



    public static InputStream getInputStream(String url,String[] msg){
        Request request = new Request.Builder()
                .url(url)
                .get().build();
        try {
            Response response =   HttpHelper.getClient().newCall(request).execute();
            if(!response.isSuccessful()){
                if(msg != null){
                    msg[0] = response.code()+":"+response.message();
                }
                return null;
            }
            return response.body().byteStream();

        } catch (Exception e) {
            e.printStackTrace();
            if(msg != null){
                msg[0] = e.getMessage();
            }
        }
        return null;
    }


}
