package com.hss01248.media.mymediastore.http;

import android.net.Uri;
import android.text.TextUtils;

import com.blankj.utilcode.util.EncodeUtils;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.StorageBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpAuthInterceptor implements Interceptor {
    public static Map<String, String> getAuthMap() {
        if(authMap == null){
            initMap();
        }
        return authMap;
    }

     volatile static Map<String,String> authMap ;
    @Override
    public Response intercept(Chain chain) throws IOException {
        if(authMap == null){
            initMap();
        }
        if(!authMap.containsKey(chain.request().url().host())){
            return chain.proceed(chain.request());
        }
        Request request = chain.request();

        return chain.proceed(request.newBuilder().header("Authorization",authMap.get(request.url().host())).build());
    }

    private static void initMap() {
        authMap = new HashMap<>();
        List<StorageBean> beans = DbUtil.getDaoSession().getStorageBeanDao().loadAll();
        if(beans != null && beans.size()>0){
            for (StorageBean bean : beans) {
                if(bean.type == StorageBean.TYPE_HTTP_Everything){
                    if(!TextUtils.isEmpty(bean.uname) && TextUtils.isEmpty(bean.pw)){
                        String ip = Uri.parse(bean.ip).getHost();
                        authMap.put(ip,"Basic "+ EncodeUtils.base64Encode2String((bean.uname+":"+bean.pw).getBytes()));
                    }
                }

            }
        }
    }
}
