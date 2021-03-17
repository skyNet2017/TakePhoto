package com.hss01248.media.mymediastore.http;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * https://www.voidtools.com/support/everything/http/
 * http://59.46.68.148:9999/?search=.jpg&c=200&o=3&j=1&path_column=1&size_column=1&date_modified_column=1&date_created_column=1&attributes_column=1
 *
 * { "totalResults":231, "results":[
 * { "type":"file", "name":"0d14b623d1e168cdbe08cd1699819375.jpg",
 * "path":"C:\\Program Files (x86)\\Thunder Network\\Thunder\\profiles\\AdData\\AdTips\\Market\\Banners",
 * "size":"100629", "date_modified":"132401088182087225" } ] }
 *
 *
 * http://59.46.68.148:9999/C%3A/Program%20Files%20(x86)/mydrivers/drivergenius/data?j=1
 *
 * { "results":[ { "type":"folder", "name":"desktip", "size":"", "date_modified":"132587960061373066" }....
 */
public class EverythingParser {

    //http://59.46.68.148:9999/
    public static HttpFile[]  start(String url){
        if(!url.startsWith("http")){
            url = "http://"+url;
        }
        url = url+"?j=1&path_column=1&date_modified_column=1&date_created_column=1&attributes_column=1";
        Request request = new Request.Builder()
                .url(url)
                .get().build();
        String prefix = "http://"+request.url().host()+":"+request.url().port();
        String path = url;
        if(path.contains("?")){
            path = path.substring(0,path.indexOf("?"));
        }
        if(path.endsWith("/")){
            path = path.substring(0,path.length()-1);
        }
        try {
            Response response =   HttpHelper.getClient().newCall(request).execute();
            if(response.isSuccessful()){
                String json = response.body().string();
                try {
                    //json = json.replaceAll("\"size\":\"\",","");
                    JSONObject object = new JSONObject(json);
                   // int totalResults = object.optInt("totalResults");
                    String results = object.optString("results");
                    List<HttpResponseBean> beans = GsonUtils.getGson().fromJson(results,new TypeToken<List<HttpResponseBean>>(){}.getType());
                    HttpFile[] files = new HttpFile[beans.size()];
                    for (int i = 0; i < beans.size(); i++) {
                        HttpResponseBean bean = beans.get(i);
                        bean.path = path;
                        //bean.setHost(prefix);
                        files[i] = new HttpFile(beans.get(i));
                    }
                    return files;
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                }

            }else {
                Log.w("http","error:"+response.code()+","+response.message());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    //http://122.226.210.62:121/?search=*.gif%7C*.jpg




   /* private static List<HttpResponseBean> parseHtml(String url, String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("table > tbody > tr");
        Uri uri = Uri.parse(url);
        String host = uri.getScheme()+"://"+uri.getHost()+":"+uri.getPort();

        List<HttpResponseBean> beans = new ArrayList<>();
        for (Element element : elements) {
            String className = element.className();
            if(TextUtils.isEmpty(className)){
                continue;
            }
            HttpResponseBean bean = new HttpResponseBean();
            //trdata1  trdata2

          String modifieddata =   element.selectFirst("td.modifieddata > span > nobr:nth-child(2)").text();

            String clazzName = element.selectFirst("td").className();

            String sizeData = element.selectFirst("td.sizedata > span > nobr").text();
            bean.lastModified = parseDate(modifieddata);
            bean.fileSize = paseSize(sizeData);
            String name = "";
            String href = "";
            boolean isDir = false;
            Element element1 = null;

            if("folder".equals(clazzName)){
                 element1 = element.selectFirst("td.folder > span > nobr > a");
                 isDir = true;
            }else if("file".equals(clazzName)){
                element1  = element.selectFirst("td.file > span > nobr > a");
            }else {
                Log.w("http","other type:"+clazzName);
            }
            if(element1 != null){
                name = element1.text();
                href = element1.attr("href");
                bean.name = name;
                bean.url = host+href;
                bean.isDir = isDir;
                beans.add(bean);
            }

            Log.i("httpbean","modifieddata "+modifieddata+","+sizeData+","+name+",href:"+href+", is file:"+isDir+"\n"+bean);

        }
        return beans;
        // doc.select("table > tbody > tr.trdata1 > td.folder > span > nobr > a");



        //body > center > table > tbody > tr:nth-child(22) > td.modifieddata > span > nobr:nth-child(2)

        //文件:
        //body > center > table > tbody > tr:nth-child(21) > td.sizedata > span > nobr
        //body > center > table > tbody > tr:nth-child(21) > td.file > span > nobr > a
    }*/

    //14.0 GB  xx MB  xx KB  0 B
    private static long paseSize(String sizeData) {
        if(TextUtils.isEmpty(sizeData)){
            return -1;
        }
        try {
            String[] strs = sizeData.split(" ");
            strs[0] = strs[0].replaceAll(",","");
            float f = Float.parseFloat(strs[0]);
            if("MB".equalsIgnoreCase(strs[1])){
                return (long) (f*1024*1024);
            }
            if("KB".equalsIgnoreCase(strs[1])){
                return (long) (f*1024);
            }
            if("GB".equalsIgnoreCase(strs[1])){
                return (long) (f*1024*1024*1024);
            }
            if("TB".equalsIgnoreCase(strs[1])){
                return (long) (f*1024*1024*1024*1024);
            }
            if("B".equalsIgnoreCase(strs[1])){
                return (long) (f);
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return -1;
    }

  public static SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    public static SimpleDateFormat sdf2 = new SimpleDateFormat( "yyyy/MM/dd HH:mm" );
    ////2020-10-12 7:30
    //"2021/1/6 21:09"
    private static long parseDate(String modifieddata) {
        Date date = null;
        try {
            if(modifieddata.contains("-")){
                date = sdf.parse( modifieddata);
            }else if(modifieddata.contains("/")){
                date = sdf2.parse( modifieddata);
            }
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }
}
