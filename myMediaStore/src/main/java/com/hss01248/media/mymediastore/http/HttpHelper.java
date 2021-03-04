package com.hss01248.media.mymediastore.http;

import com.hss01248.media.mymediastore.SafFileFinder22;
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
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            client = builder.build();
        }
        return client;
    }

    public static void login(String url,String name,String pwd){

    }

    public static void start(String url, final ScanFolderCallback observer){
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        HttpFile[] files = EverythingParser.start(url);
        if(files !=null && files.length > 0){
            for (HttpFile file : files) {
                new SafFileFinder22<HttpFile>().getAlbums(file, executorService, observer);
            }

        }
    }



    public static InputStream getInputStream(String url){
        Request request = new Request.Builder()
                .url(url)
                .get().build();
        try {
            Response response =   HttpHelper.getClient().newCall(request).execute();
            if(response.isSuccessful()){
                return response.body().byteStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
