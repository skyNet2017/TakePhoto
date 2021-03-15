package com.hss01248.media.mymediastore.http;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.Response;

public class EverythingSearchParser {

  private   static int pageSize = 32;

    public static ExecutorService service = new ThreadPoolExecutor(0, 10,
            45, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());




    public static void searchMediaType(String rootUrl){
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_IMAGE,0);
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_VIDEO,0);
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_AUDIO,0);
    }

    public static void searchDocType(String rootUrl){
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_DOC_WORD,0);
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_DOC_TXT,0);
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_DOC_PDF,0);
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_DOC_EXCEL,0);
        new EverythingSearchParser().startSearch(rootUrl,BaseMediaInfo.TYPE_DOC_PPT,0);
    }

    int[] totalPageCount = new int[]{0};
    boolean hasScanAllPage;
    //http://59.46.68.148:9999/
    public  void  startSearch(String rootUrl,int type,int pageNum){
        service.execute(new Runnable() {
            @Override
            public void run() {
                String url = rootUrl+"?search="+getTypeSearchStr(type)+"&offset="+pageNum*pageSize;
                Request request = new Request.Builder()
                        .url(url)
                        .get().build();
                try {
                    Response response =   HttpHelper.getClient().newCall(request).execute();
                    if(response.isSuccessful()){
                        String html = response.body().string();
                        try {
                            List<HttpResponseBean> beans =   parseHtml(url,html,totalPageCount);

                            List<BaseMediaInfo> infos = new ArrayList<>();

                            // HttpFile[] files = new HttpFile[beans.size()];
                            for (int i = 0; i < beans.size(); i++) {
                                // files[i] = new HttpFile(beans.get(i));
                                HttpResponseBean bean = beans.get(i);
                                BaseMediaInfo info = new BaseMediaInfo();
                                info.path = bean.url;
                                info.mediaType = type;
                                info.dir = bean.url.substring(0,bean.url.lastIndexOf("/"));
                                info.fileSize = bean.fileSize;
                                info.hidden = 0;
                                info.updatedTime = bean.lastModified;
                                info.name = URLDecoder.decode(bean.url.substring(bean.url.lastIndexOf("/")+1));
                                infos.add(info);
                            }
                            writeDb(infos);


                            if(hasScanAllPage){
                                return;
                            }
                            if(totalPageCount[0] > 1){
                                //直接根据页面来请求,
                                hasScanAllPage = true;
                                for (int i = 1; i < totalPageCount[0]; i++) {
                                    startSearch(rootUrl, type, i);
                                }
                                return;
                            }
                            //没有拿到页面数,只能一页一页翻

                            if(beans != null && beans.size() > 0){
                                int num = pageNum+1;
                                startSearch(rootUrl, type, num);
                            }else {
                                Log.e("http","搜索页面解析终于结束了:"+url);
                            }
                            // return files;
                        }catch (Throwable throwable){
                            throwable.printStackTrace();
                        }

                    }else {
                        Log.w("http","error:"+response.code()+","+response.message());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static synchronized void writeDb(List<BaseMediaInfo> infos) {
       DbUtil.insertOrUpdate2(infos);
    }

    private static String getTypeSearchStr(int type) {
        List<String> strings = FileTypeUtil.getTypeExtension(type);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            sb.append(".")
                    .append(strings.get(i));
            if(i != strings.size() -1){
                sb.append("|");
            }
        }
        return sb.toString();
    }

    //http://122.226.210.62:121/?search=*.gif%7C*.jpg
    //body > center:nth-child(1) > table > tbody > tr:nth-child(4) > td.pathdata



    


    private static List<HttpResponseBean> parseHtml(String url, String html,@Nullable int[] totalPageCount) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("table > tbody > tr");
        Uri uri = Uri.parse(url);
        String host = uri.getScheme()+"://"+uri.getHost()+":"+uri.getPort();

        //body > center:nth-child(2) > span:nth-child(6) > a
        if(totalPageCount != null && totalPageCount[0]==0 ){
            Elements numEls = doc.select("a.num");
            if(numEls != null && numEls.size()> 0){
                //最后一个
                Element numEl = numEls.get(numEls.size()-1);
                String text = numEl.text();
                if(!TextUtils.isEmpty(text)){
                    totalPageCount[0] = Integer.parseInt(text);
                    Log.w("http","获取总页数:"+totalPageCount[0]);
                }

            }
        }



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
            Element parentPath = null;

            if("folder".equals(clazzName)){
                 element1 = element.selectFirst("td.folder > span > nobr > a");
                 isDir = true;
            }else if("file".equals(clazzName)){
                element1  = element.selectFirst("td.file > span > nobr > a");
                //body > center:nth-child(1) > table > tbody > tr:nth-child(4) > td.pathdata > span > a
                //body > center:nth-child(1) > table > tbody > tr:nth-child(12) > td.pathdata > span > a
                parentPath = element.selectFirst("td.pathdata > span > a");
            }else {
                Log.w("http","other type:"+clazzName);
            }
            if(element1 != null){
                name = element1.text();
                href = element1.attr("href");
                bean.name = name;
                bean.url = host+href;
                bean.isDir = isDir;
                if(parentPath != null){
                    bean.parentUrl = host+parentPath.attr("href");
                }else {
                    Log.d("http","parentUrl is null");
                }
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
    }

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
